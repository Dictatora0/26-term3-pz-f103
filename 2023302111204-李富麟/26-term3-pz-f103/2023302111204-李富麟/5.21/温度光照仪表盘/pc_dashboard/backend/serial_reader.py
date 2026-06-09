import argparse
import json
import re
import threading
import time
from collections import deque
from datetime import datetime
from typing import Any, Dict, Optional

import serial
import serial.tools.list_ports

KV_PATTERN = re.compile(r"\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*([^,]+)\s*")
FLOAT_PATTERN = re.compile(r"[-+]?\d+(?:\.\d+)?")
JSON_KV_PATTERN = re.compile(r'"?([A-Za-z_][A-Za-z0-9_]*)"?\s*:\s*([^,}]+)')


class SerialReader:
    def __init__(
        self,
        port: Optional[str],
        baudrate: int = 115200,
        history_size: int = 100,
        retry_interval: float = 2.0,
    ) -> None:
        self.port = port
        self.baudrate = baudrate
        self.retry_interval = retry_interval
        self._history: deque[Dict[str, Any]] = deque(maxlen=history_size)
        self._raw_lines: deque[str] = deque(maxlen=30)
        self._latest: Optional[Dict[str, Any]] = None
        self._connected = False
        self._parse_ok = 0
        self._parse_fail = 0
        self._read_count = 0
        self._last_exception: Optional[str] = None
        self._lock = threading.Lock()
        self._stop_event = threading.Event()
        self._thread: Optional[threading.Thread] = None

    @property
    def connected(self) -> bool:
        with self._lock:
            return self._connected

    def start(self) -> None:
        if self._thread and self._thread.is_alive():
            return
        self._stop_event.clear()
        self._thread = threading.Thread(target=self._run, daemon=True)
        self._thread.start()

    def stop(self) -> None:
        self._stop_event.set()
        if self._thread and self._thread.is_alive():
            self._thread.join(timeout=2)

    def latest(self) -> Optional[Dict[str, Any]]:
        with self._lock:
            if self._latest is None:
                return None
            return dict(self._latest)

    def history(self, limit: Optional[int] = None) -> list[Dict[str, Any]]:
        with self._lock:
            items = list(self._history)
        if limit is not None:
            return items[-limit:]
        return items

    def _set_connected(self, connected: bool) -> None:
        with self._lock:
            self._connected = connected

    def _append_data(self, item: Dict[str, Any]) -> None:
        with self._lock:
            self._latest = item
            self._history.append(item)
            self._parse_ok += 1

    def _append_raw_line(self, line: str) -> None:
        with self._lock:
            self._read_count += 1
            self._raw_lines.append(line)

    def _mark_parse_fail(self) -> None:
        with self._lock:
            self._parse_fail += 1

    def debug_snapshot(self) -> Dict[str, Any]:
        with self._lock:
            latest = dict(self._latest) if self._latest else None
            return {
                "connected": self._connected,
                "port": self.port,
                "baudrate": self.baudrate,
                "read_count": self._read_count,
                "parse_ok": self._parse_ok,
                "parse_fail": self._parse_fail,
                "history_count": len(self._history),
                "latest": latest,
                "last_exception": self._last_exception,
                "recent_raw_lines": list(self._raw_lines),
            }

    def _choose_port(self) -> Optional[str]:
        if self.port:
            return self.port
        ports = list(serial.tools.list_ports.comports())
        if not ports:
            return None
        return ports[0].device

    def _run(self) -> None:
        while not self._stop_event.is_set():
            port = self._choose_port()
            if not port:
                self._set_connected(False)
                time.sleep(self.retry_interval)
                continue

            try:
                with serial.Serial(
                    port=port,
                    baudrate=self.baudrate,
                    timeout=0.2,
                    bytesize=serial.EIGHTBITS,
                    parity=serial.PARITY_NONE,
                    stopbits=serial.STOPBITS_ONE,
                    xonxoff=False,
                    rtscts=False,
                    dsrdtr=False,
                ) as ser:
                    # Some CH340 boards wire reset/boot to control lines.
                    # Keep both deasserted to avoid holding the MCU in reset.
                    try:
                        ser.setDTR(False)
                        ser.setRTS(False)
                    except Exception:
                        pass
                    try:
                        ser.reset_input_buffer()
                    except Exception:
                        pass

                    self._set_connected(True)
                    while not self._stop_event.is_set():
                        raw = ser.readline()
                        if not raw:
                            continue
                        line = raw.decode("utf-8", errors="ignore").strip()
                        if not line:
                            continue
                        self._append_raw_line(line)

                        parsed = self._parse_line(line)
                        if parsed is None:
                            self._mark_parse_fail()
                            print(f"[WARN] parse failed: {line}")
                            continue
                        self._append_data(parsed)
            except Exception as exc:
                self._set_connected(False)
                with self._lock:
                    self._last_exception = str(exc)
                print(f"[WARN] serial disconnected ({port}): {exc}")
                time.sleep(self.retry_interval)

    def _parse_line(self, line: str) -> Optional[Dict[str, Any]]:
        # Preferred format: JSON line
        if line.startswith("{") and line.endswith("}"):
            try:
                obj = json.loads(line)
            except json.JSONDecodeError:
                # tolerant parsing for broken JSON (e.g. float printf not enabled)
                obj = self._parse_loose_json_object(line)
                if obj is not None:
                    normalized = self._normalize(obj)
                    if normalized is not None:
                        return normalized
            else:
                normalized = self._normalize(obj)
                if normalized is not None:
                    return normalized

        # Fallback format: TEMP=25.6,LIGHT=78,HUM=null
        pairs = [chunk for chunk in line.split(",") if chunk.strip()]
        if pairs:
            obj: Dict[str, Any] = {}
            ok = True
            for pair in pairs:
                m = KV_PATTERN.fullmatch(pair)
                if not m:
                    ok = False
                    break
                key, val = m.group(1), m.group(2)
                obj[key.lower()] = val

            if ok:
                normalized = self._normalize(obj)
                if normalized is not None:
                    return normalized

        # Legacy Chinese lines:
        # "内部温度检测值为：+25.63°C"
        # "光照强度：78"
        legacy = self._parse_legacy_line(line)
        if legacy is not None:
            return legacy

        return None

    def _parse_loose_json_object(self, line: str) -> Optional[Dict[str, Any]]:
        matches = JSON_KV_PATTERN.findall(line)
        if not matches:
            return None

        obj: Dict[str, Any] = {}
        for key, raw_val in matches:
            val = raw_val.strip().strip('"').strip("'")
            obj[key.lower()] = val
        return obj

    def _parse_legacy_line(self, line: str) -> Optional[Dict[str, Any]]:
        now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        with self._lock:
            last = dict(self._latest) if self._latest else {}

        temperature = last.get("temperature")
        light = last.get("light")
        humidity = last.get("humidity")
        board_timestamp_ms = last.get("board_timestamp_ms")

        changed = False
        lower_line = line.lower()

        if ("温度" in line) or ("temp" in lower_line):
            f = self._extract_float(line)
            if f is not None:
                temperature = f
                changed = True

        if ("光照" in line) or ("light" in lower_line):
            f = self._extract_float(line)
            if f is not None:
                light = int(round(f))
                changed = True

        if not changed:
            return None

        if temperature is None and light is None:
            return None

        return {
            "temperature": temperature,
            "light": light,
            "humidity": humidity,
            "timestamp": now_str,
            "board_timestamp_ms": board_timestamp_ms,
        }

    def _normalize(self, obj: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        temp_val = obj.get("temp", obj.get("temperature"))
        temp_centi_val = obj.get("temp_centi")
        light_val = obj.get("light")
        humidity_val = obj.get("humidity", obj.get("hum"))
        board_ts = obj.get("timestamp_ms")

        temperature = self._to_float_or_none(temp_val)
        if temperature is None:
            temp_centi = self._to_int_or_none(temp_centi_val)
            if temp_centi is not None:
                temperature = temp_centi / 100.0
        light = self._to_int_or_none(light_val)
        humidity = self._to_float_or_none(humidity_val)

        if temperature is None and light is None:
            return None

        return {
            "temperature": temperature,
            "light": light,
            "humidity": humidity,
            "timestamp": now_str,
            "board_timestamp_ms": self._to_int_or_none(board_ts),
        }

    @staticmethod
    def _extract_float(text: str) -> Optional[float]:
        m = FLOAT_PATTERN.search(text)
        if not m:
            return None
        try:
            return float(m.group(0))
        except ValueError:
            return None

    @staticmethod
    def _to_float_or_none(value: Any) -> Optional[float]:
        if value is None:
            return None
        if isinstance(value, str) and value.strip().lower() in {"null", "none", "nan", ""}:
            return None
        try:
            return float(value)
        except (TypeError, ValueError):
            return None

    @staticmethod
    def _to_int_or_none(value: Any) -> Optional[int]:
        if value is None:
            return None
        if isinstance(value, str) and value.strip().lower() in {"null", "none", "nan", ""}:
            return None
        try:
            return int(float(value))
        except (TypeError, ValueError):
            return None


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="STM32 serial reader")
    parser.add_argument("--port", default=None, help="Serial port, e.g. COM3")
    parser.add_argument("--baudrate", type=int, default=115200, help="Serial baudrate")
    parser.add_argument("--history", type=int, default=100, help="History size in memory")
    return parser
