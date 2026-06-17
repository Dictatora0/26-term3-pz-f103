from __future__ import annotations

import json
import logging
import math
import os
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from threading import Lock
from typing import Any, Callable, Dict, List, Optional, Tuple

try:
    import serial
except ImportError:  # pragma: no cover
    serial = None

try:
    import paho.mqtt.client as mqtt
except ImportError:  # pragma: no cover
    mqtt = None

try:
    from dotenv import load_dotenv
except ImportError:  # pragma: no cover
    load_dotenv = None

try:
    import yaml
except ImportError:  # pragma: no cover
    yaml = None


BASE_DIR = Path(__file__).resolve().parent
TB_TOKEN_PLACEHOLDER = "replace_with_thingsboard_device_access_token"
CAR_TB_TOKEN_PLACEHOLDER = "replace_with_hi3861_car_thingsboard_token"
CAR_STATUS_VALUES = {"RUNNING", "STOPPED", "IDLE", "ERROR"}
CAR_DIRECTION_VALUES = {"FORWARD", "BACKWARD", "LEFT", "RIGHT", "STOP"}

DEFAULTS: Dict[str, Any] = {
    "SERIAL_PORT": "COM3",
    "BAUD_RATE": 115200,
    "SERIAL_TIMEOUT": 1,
    "SERIAL_DTR": False,
    "SERIAL_RTS": False,
    "CONTROL_SERIAL_PORT": "",
    "CONTROL_BAUD_RATE": 115200,
    "CONTROL_SERIAL_TIMEOUT": 1,
    "CONTROL_SERIAL_DTR": False,
    "CONTROL_SERIAL_RTS": False,
    "DEVICE_ID": "f103_01",
    "F103_ENABLED": True,
    "F103_SERIAL_PORT": "",
    "F103_BAUD_RATE": 115200,
    "F103_SERIAL_TIMEOUT": 1,
    "F103_SERIAL_DTR": False,
    "F103_SERIAL_RTS": False,
    "F103_CONTROL_SERIAL_PORT": "",
    "F103_CONTROL_BAUD_RATE": 115200,
    "F103_CONTROL_SERIAL_TIMEOUT": 1,
    "F103_CONTROL_SERIAL_DTR": False,
    "F103_CONTROL_SERIAL_RTS": False,
    "F103_DEVICE_ID": "",
    "CAR_ENABLED": True,
    "CAR_INPUT_MODE": "serial",
    "CAR_SERIAL_PORT": "COM5",
    "CAR_BAUD_RATE": 115200,
    "CAR_SERIAL_TIMEOUT": 1,
    "CAR_SERIAL_DTR": False,
    "CAR_SERIAL_RTS": False,
    "CAR_DEVICE_ID": "hi3861_car_01",
    "SERIAL_RECONNECT_DELAY": 3,
    "HA_ENABLED": True,
    "HA_MQTT_HOST": "127.0.0.1",
    "HA_MQTT_PORT": 1883,
    "HA_MQTT_USERNAME": "",
    "HA_MQTT_PASSWORD": "",
    "HA_CONTROL_ENABLED": True,
    "HA_DISCOVERY_ENABLED": True,
    "HA_DISCOVERY_PREFIX": "homeassistant",
    "TB_ENABLED": True,
    "TB_MQTT_HOST": "127.0.0.1",
    "TB_MQTT_PORT": 1884,
    "TB_ACCESS_TOKEN": TB_TOKEN_PLACEHOLDER,
    "TB_RPC_ENABLED": True,
    "CAR_TB_ACCESS_TOKEN": CAR_TB_TOKEN_PLACEHOLDER,
    "MQTT_RECONNECT_MIN_DELAY": 1,
    "MQTT_RECONNECT_MAX_DELAY": 30,
    "COMMAND_CONFIRM_TIMEOUT": 8,
    "LOG_LEVEL": "INFO",
}


@dataclass(frozen=True)
class GatewayConfig:
    f103_enabled: bool
    f103_serial_port: str
    f103_baud_rate: int
    f103_serial_timeout: float
    f103_serial_dtr: bool
    f103_serial_rts: bool
    f103_control_serial_port: str
    f103_control_baud_rate: int
    f103_control_serial_timeout: float
    f103_control_serial_dtr: bool
    f103_control_serial_rts: bool
    f103_device_id: str
    car_enabled: bool
    car_input_mode: str
    car_serial_port: str
    car_baud_rate: int
    car_serial_timeout: float
    car_serial_dtr: bool
    car_serial_rts: bool
    car_device_id: str
    serial_reconnect_delay: float
    ha_enabled: bool
    ha_mqtt_host: str
    ha_mqtt_port: int
    ha_mqtt_username: str
    ha_mqtt_password: str
    ha_control_enabled: bool
    ha_discovery_enabled: bool
    ha_discovery_prefix: str
    tb_enabled: bool
    tb_mqtt_host: str
    tb_mqtt_port: int
    tb_access_token: str
    tb_rpc_enabled: bool
    car_tb_access_token: str
    mqtt_reconnect_min_delay: int
    mqtt_reconnect_max_delay: int
    command_confirm_timeout: float
    log_level: str
    sources: Tuple[str, ...]
    warnings: Tuple[str, ...]


@dataclass
class PendingCommand:
    device_id: str
    target: str
    requested_state: str
    command: str
    source: str
    issued_at: float


def configure_logging(level_name: str) -> None:
    level = getattr(logging, str(level_name).upper(), logging.INFO)
    logging.basicConfig(
        level=level,
        format="%(asctime)s %(levelname)s %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
        force=True,
    )


def read_yaml_config(path: Path, warnings: List[str], sources: List[str]) -> Dict[str, Any]:
    if not path.exists():
        return {}
    if yaml is None:
        warnings.append("[CONFIG] config.yaml exists but PyYAML is not installed; YAML config ignored")
        return {}

    try:
        with path.open("r", encoding="utf-8") as config_file:
            data = yaml.safe_load(config_file) or {}
    except Exception as exc:
        warnings.append(f"[CONFIG] failed to read config.yaml: {exc}")
        return {}

    if not isinstance(data, dict):
        warnings.append("[CONFIG] config.yaml must contain a top-level mapping; YAML config ignored")
        return {}

    sources.append(path.name)
    return {str(key).upper(): value for key, value in data.items()}


def load_dotenv_fallback(path: Path, warnings: List[str], sources: List[str]) -> None:
    if not path.exists():
        return

    try:
        with path.open("r", encoding="utf-8") as env_file:
            for raw_line in env_file:
                line = raw_line.strip()
                if not line or line.startswith("#"):
                    continue
                if line.startswith("export "):
                    line = line[len("export ") :].strip()
                if "=" not in line:
                    continue
                key, value = line.split("=", 1)
                key = key.strip()
                if not key:
                    continue
                value = value.strip()
                if len(value) >= 2 and value[0] == value[-1] and value[0] in {'"', "'"}:
                    value = value[1:-1]
                if key not in os.environ:
                    os.environ[key] = value
    except Exception as exc:
        warnings.append(f"[CONFIG] failed to read .env with fallback loader: {exc}")
        return

    sources.append(".env")


def parse_bool(value: Any, key: str, warnings: List[str]) -> bool:
    if isinstance(value, bool):
        return value

    text = str(value).strip().lower()
    if text in {"1", "true", "yes", "on", "y"}:
        return True
    if text in {"0", "false", "no", "off", "n"}:
        return False

    warnings.append(f"[CONFIG] invalid boolean for {key}={value!r}; using default={DEFAULTS[key]}")
    return bool(DEFAULTS[key])


def parse_int(value: Any, key: str, warnings: List[str]) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        warnings.append(f"[CONFIG] invalid integer for {key}={value!r}; using default={DEFAULTS[key]}")
        return int(DEFAULTS[key])


def parse_float(value: Any, key: str, warnings: List[str]) -> float:
    try:
        return float(value)
    except (TypeError, ValueError):
        warnings.append(f"[CONFIG] invalid float for {key}={value!r}; using default={DEFAULTS[key]}")
        return float(DEFAULTS[key])


def load_config() -> GatewayConfig:
    warnings: List[str] = []
    sources: List[str] = []
    values = dict(DEFAULTS)

    yaml_values = read_yaml_config(BASE_DIR / "config.yaml", warnings, sources)
    for key, value in yaml_values.items():
        if key in DEFAULTS:
            values[key] = value

    dotenv_path = BASE_DIR / ".env"
    if dotenv_path.exists():
        if load_dotenv is None:
            warnings.append("[CONFIG] python-dotenv is not installed; using fallback .env loader")
            load_dotenv_fallback(dotenv_path, warnings, sources)
        else:
            load_dotenv(dotenv_path=dotenv_path, override=False)
            sources.append(".env")

    env_provided: set[str] = set()
    for key in DEFAULTS:
        if key in os.environ:
            values[key] = os.environ[key]
            env_provided.add(key)

    provided_keys = set(yaml_values.keys()) | env_provided

    if not sources:
        sources.append("defaults/environment")

    def pick(primary_key: str, fallback_key: str | None = None) -> Any:
        if primary_key in provided_keys:
            return values[primary_key]
        if fallback_key is not None and fallback_key in provided_keys:
            return values[fallback_key]
        return values[primary_key]

    return GatewayConfig(
        f103_enabled=parse_bool(pick("F103_ENABLED"), "F103_ENABLED", warnings),
        f103_serial_port=str(pick("F103_SERIAL_PORT", "SERIAL_PORT")).strip() or str(DEFAULTS["SERIAL_PORT"]),
        f103_baud_rate=parse_int(pick("F103_BAUD_RATE", "BAUD_RATE"), "F103_BAUD_RATE", warnings),
        f103_serial_timeout=parse_float(
            pick("F103_SERIAL_TIMEOUT", "SERIAL_TIMEOUT"),
            "F103_SERIAL_TIMEOUT",
            warnings,
        ),
        f103_serial_dtr=parse_bool(pick("F103_SERIAL_DTR", "SERIAL_DTR"), "F103_SERIAL_DTR", warnings),
        f103_serial_rts=parse_bool(pick("F103_SERIAL_RTS", "SERIAL_RTS"), "F103_SERIAL_RTS", warnings),
        f103_control_serial_port=str(
            pick("F103_CONTROL_SERIAL_PORT", "CONTROL_SERIAL_PORT")
        ).strip(),
        f103_control_baud_rate=parse_int(
            pick("F103_CONTROL_BAUD_RATE", "CONTROL_BAUD_RATE"),
            "F103_CONTROL_BAUD_RATE",
            warnings,
        ),
        f103_control_serial_timeout=parse_float(
            pick("F103_CONTROL_SERIAL_TIMEOUT", "CONTROL_SERIAL_TIMEOUT"),
            "F103_CONTROL_SERIAL_TIMEOUT",
            warnings,
        ),
        f103_control_serial_dtr=parse_bool(
            pick("F103_CONTROL_SERIAL_DTR", "CONTROL_SERIAL_DTR"),
            "F103_CONTROL_SERIAL_DTR",
            warnings,
        ),
        f103_control_serial_rts=parse_bool(
            pick("F103_CONTROL_SERIAL_RTS", "CONTROL_SERIAL_RTS"),
            "F103_CONTROL_SERIAL_RTS",
            warnings,
        ),
        f103_device_id=str(pick("F103_DEVICE_ID", "DEVICE_ID")).strip() or str(DEFAULTS["DEVICE_ID"]),
        car_enabled=parse_bool(pick("CAR_ENABLED"), "CAR_ENABLED", warnings),
        car_input_mode=str(pick("CAR_INPUT_MODE")).strip().lower() or str(DEFAULTS["CAR_INPUT_MODE"]),
        car_serial_port=str(pick("CAR_SERIAL_PORT")).strip() or str(DEFAULTS["CAR_SERIAL_PORT"]),
        car_baud_rate=parse_int(pick("CAR_BAUD_RATE"), "CAR_BAUD_RATE", warnings),
        car_serial_timeout=parse_float(pick("CAR_SERIAL_TIMEOUT"), "CAR_SERIAL_TIMEOUT", warnings),
        car_serial_dtr=parse_bool(pick("CAR_SERIAL_DTR"), "CAR_SERIAL_DTR", warnings),
        car_serial_rts=parse_bool(pick("CAR_SERIAL_RTS"), "CAR_SERIAL_RTS", warnings),
        car_device_id=str(pick("CAR_DEVICE_ID")).strip() or str(DEFAULTS["CAR_DEVICE_ID"]),
        serial_reconnect_delay=parse_float(
            pick("SERIAL_RECONNECT_DELAY"),
            "SERIAL_RECONNECT_DELAY",
            warnings,
        ),
        ha_enabled=parse_bool(pick("HA_ENABLED"), "HA_ENABLED", warnings),
        ha_mqtt_host=str(pick("HA_MQTT_HOST")).strip() or str(DEFAULTS["HA_MQTT_HOST"]),
        ha_mqtt_port=parse_int(pick("HA_MQTT_PORT"), "HA_MQTT_PORT", warnings),
        ha_mqtt_username=str(pick("HA_MQTT_USERNAME")).strip(),
        ha_mqtt_password=str(pick("HA_MQTT_PASSWORD")).strip(),
        ha_control_enabled=parse_bool(pick("HA_CONTROL_ENABLED"), "HA_CONTROL_ENABLED", warnings),
        ha_discovery_enabled=parse_bool(
            pick("HA_DISCOVERY_ENABLED"),
            "HA_DISCOVERY_ENABLED",
            warnings,
        ),
        ha_discovery_prefix=str(pick("HA_DISCOVERY_PREFIX")).strip() or str(DEFAULTS["HA_DISCOVERY_PREFIX"]),
        tb_enabled=parse_bool(pick("TB_ENABLED"), "TB_ENABLED", warnings),
        tb_mqtt_host=str(pick("TB_MQTT_HOST")).strip() or str(DEFAULTS["TB_MQTT_HOST"]),
        tb_mqtt_port=parse_int(pick("TB_MQTT_PORT"), "TB_MQTT_PORT", warnings),
        tb_access_token=str(pick("TB_ACCESS_TOKEN")).strip(),
        tb_rpc_enabled=parse_bool(pick("TB_RPC_ENABLED"), "TB_RPC_ENABLED", warnings),
        car_tb_access_token=str(pick("CAR_TB_ACCESS_TOKEN")).strip(),
        mqtt_reconnect_min_delay=parse_int(
            pick("MQTT_RECONNECT_MIN_DELAY"),
            "MQTT_RECONNECT_MIN_DELAY",
            warnings,
        ),
        mqtt_reconnect_max_delay=parse_int(
            pick("MQTT_RECONNECT_MAX_DELAY"),
            "MQTT_RECONNECT_MAX_DELAY",
            warnings,
        ),
        command_confirm_timeout=parse_float(
            pick("COMMAND_CONFIRM_TIMEOUT"),
            "COMMAND_CONFIRM_TIMEOUT",
            warnings,
        ),
        log_level=str(pick("LOG_LEVEL")).strip() or str(DEFAULTS["LOG_LEVEL"]),
        sources=tuple(sources),
        warnings=tuple(warnings),
    )


def create_mqtt_client(client_id: str):
    if mqtt is None:
        raise RuntimeError("paho-mqtt is not installed")

    callback_api_version = getattr(mqtt, "CallbackAPIVersion", None)
    if callback_api_version is not None:
        try:
            return mqtt.Client(callback_api_version.VERSION2, client_id=client_id)
        except TypeError:
            pass

    return mqtt.Client(client_id=client_id)


def is_success_reason(reason_code: Any) -> bool:
    is_failure = getattr(reason_code, "is_failure", None)
    if is_failure is not None:
        return not bool(is_failure)

    try:
        return int(reason_code) == 0
    except (TypeError, ValueError):
        return str(reason_code).lower() in {"0", "success"}


def mask_token(token: str) -> str:
    if not token:
        return "(empty)"
    if token in {TB_TOKEN_PLACEHOLDER, CAR_TB_TOKEN_PLACEHOLDER}:
        return "(placeholder)"
    if len(token) <= 6:
        return "***"
    return f"{token[:3]}***{token[-3:]}"


def normalize_switch_payload(payload_text: str) -> Optional[str]:
    normalized = payload_text.strip().upper()
    if normalized in {"ON", "1", "TRUE", "YES"}:
        return "ON"
    if normalized in {"OFF", "0", "FALSE", "NO"}:
        return "OFF"
    return None


def build_serial_command(target: str, state: str) -> Optional[str]:
    normalized_state = normalize_switch_payload(state)
    if normalized_state is None:
        return None

    normalized_target = target.strip().lower()
    if normalized_target == "led":
        return "LED_ON" if normalized_state == "ON" else "LED_OFF"
    if normalized_target == "buzzer":
        return "BUZZER_ON" if normalized_state == "ON" else "BUZZER_OFF"
    return None


def parse_number(raw_value: str) -> int | float:
    value = float(raw_value)
    if not math.isfinite(value):
        raise ValueError("number must be finite")

    if any(marker in raw_value for marker in (".", "e", "E")):
        return value
    return int(raw_value)


def parse_float_number(raw_value: Any) -> float:
    value = float(raw_value)
    if not math.isfinite(value):
        raise ValueError("number must be finite")
    return value


def parse_f103_status_line(line: str) -> Dict[str, Any]:
    parsed: Dict[str, Any] = {}
    for field in line.split(","):
        if ":" not in field:
            logging.warning("[WARN] [SERIAL] malformed field ignored: %s", field)
            continue

        key, raw_value = field.split(":", 1)
        key = key.strip().upper()
        raw_value = raw_value.strip()

        if key == "TEMP":
            try:
                value = float(raw_value)
                if not math.isfinite(value):
                    raise ValueError("temperature must be finite")
                parsed["temperature"] = value
            except ValueError:
                logging.warning("[WARN] [SERIAL] invalid TEMP value=%r; field ignored", raw_value)
        elif key == "LIGHT":
            try:
                parsed["light"] = parse_number(raw_value)
            except ValueError:
                logging.warning("[WARN] [SERIAL] invalid LIGHT value=%r; field ignored", raw_value)
        elif key == "LED":
            state = raw_value.upper()
            if state in {"ON", "OFF"}:
                parsed["led"] = state
            else:
                logging.warning("[WARN] [SERIAL] invalid LED value=%r; expected ON or OFF", raw_value)
        elif key == "BUZZER":
            state = raw_value.upper()
            if state in {"ON", "OFF"}:
                parsed["buzzer"] = state
            else:
                logging.warning("[WARN] [SERIAL] invalid BUZZER value=%r; expected ON or OFF", raw_value)
        else:
            logging.debug("[SERIAL] unknown field ignored key=%s value=%s", key, raw_value)

    return parsed


def normalize_car_telemetry(raw: Dict[str, Any], source_label: str) -> Dict[str, Any]:
    parsed: Dict[str, Any] = {}

    if "status" in raw:
        status = str(raw["status"]).strip().upper()
        if status in CAR_STATUS_VALUES:
            parsed["status"] = status
        else:
            logging.warning("[WARN] [%s] invalid status value=%r", source_label, raw["status"])

    if "direction" in raw:
        direction = str(raw["direction"]).strip().upper()
        if direction in CAR_DIRECTION_VALUES:
            parsed["direction"] = direction
        else:
            logging.warning("[WARN] [%s] invalid direction value=%r", source_label, raw["direction"])

    if "speed" in raw:
        try:
            parsed["speed"] = parse_number(str(raw["speed"]))
        except ValueError:
            logging.warning("[WARN] [%s] invalid speed value=%r", source_label, raw["speed"])

    if "distance_cm" in raw:
        try:
            parsed["distance_cm"] = round(parse_float_number(raw["distance_cm"]), 1)
        except ValueError:
            logging.warning("[WARN] [%s] invalid distance_cm value=%r", source_label, raw["distance_cm"])

    return parsed


def parse_hi3861_json_line(line: str, expected_device_id: str) -> Optional[Dict[str, Any]]:
    stripped = line.strip()
    if not stripped.startswith("{"):
        return None

    try:
        payload = json.loads(stripped)
    except json.JSONDecodeError as exc:
        logging.warning("[WARN] [CAR] invalid JSON line ignored error=%s line=%s", exc.msg, line)
        return None

    if not isinstance(payload, dict):
        logging.warning("[WARN] [CAR] JSON payload is not an object; ignored")
        return None

    device_id = str(payload.get("device_id", "")).strip()
    device_type = str(payload.get("type", "")).strip().lower()
    telemetry_raw = payload.get("telemetry", payload)

    if not device_id:
        device_id = expected_device_id

    if device_id != expected_device_id and device_type != "car":
        return None

    if not isinstance(telemetry_raw, dict):
        logging.warning("[WARN] [CAR] telemetry JSON must be an object; ignored")
        return None

    telemetry = normalize_car_telemetry(telemetry_raw, "CAR")
    if not telemetry:
        logging.warning("[WARN] [CAR] JSON line has no valid telemetry fields: %s", line)
        return None

    return {
        "device_id": device_id,
        "device_type": "car",
        "telemetry": telemetry,
    }


def parse_hi3861_car_line(line: str, expected_device_id: str) -> Optional[Dict[str, Any]]:
    stripped = line.strip()
    if not stripped.startswith("CAR:"):
        return None

    segments = [segment.strip() for segment in stripped.split(",") if segment.strip()]
    if not segments:
        return None

    device_id = segments[0][len("CAR:") :].strip() or expected_device_id
    raw: Dict[str, Any] = {}
    field_map = {
        "STATUS": "status",
        "DIR": "direction",
        "SPEED": "speed",
        "DIST": "distance_cm",
    }

    for segment in segments[1:]:
        if ":" not in segment:
            logging.warning("[WARN] [CAR] malformed CAR field ignored: %s", segment)
            continue
        key, raw_value = segment.split(":", 1)
        key = key.strip().upper()
        mapped = field_map.get(key)
        if mapped is None:
            logging.debug("[CAR] unknown field ignored key=%s value=%s", key, raw_value)
            continue
        raw[mapped] = raw_value.strip()

    telemetry = normalize_car_telemetry(raw, "CAR")
    if not telemetry:
        logging.warning("[WARN] [CAR] CAR line has no valid telemetry fields: %s", line)
        return None

    return {
        "device_id": device_id,
        "device_type": "car",
        "telemetry": telemetry,
    }


class StateCache:
    def __init__(self) -> None:
        self._lock = Lock()
        self._telemetry_by_device: Dict[str, Dict[str, Any]] = {}
        self._pending_commands: Dict[str, PendingCommand] = {}

    def snapshot(self, device_id: str) -> Dict[str, Any]:
        with self._lock:
            return dict(self._telemetry_by_device.get(device_id, {}))

    def mark_command(self, device_id: str, target: str, requested_state: str, command: str, source: str) -> None:
        with self._lock:
            self._pending_commands[f"{device_id}:{target}"] = PendingCommand(
                device_id=device_id,
                target=target,
                requested_state=requested_state,
                command=command,
                source=source,
                issued_at=time.time(),
            )

    def apply_serial_update(
        self,
        device_id: str,
        data: Dict[str, Any],
        command_confirm_timeout: float,
    ) -> Tuple[Dict[str, Any], List[str], List[str]]:
        confirmations: List[str] = []
        warnings: List[str] = []

        with self._lock:
            device_state = self._telemetry_by_device.setdefault(device_id, {})
            device_state.update(data)

            now = time.time()
            for key, pending in list(self._pending_commands.items()):
                if pending.device_id != device_id:
                    continue

                if pending.target in data:
                    observed = str(data[pending.target]).upper()
                    if observed == pending.requested_state:
                        confirmations.append(
                            f"[STATE] command confirmed source={pending.source} "
                            f"device_id={device_id} target={pending.target} "
                            f"requested={pending.requested_state} observed={observed}"
                        )
                        del self._pending_commands[key]
                        continue

                if now - pending.issued_at >= command_confirm_timeout:
                    current_value = device_state.get(pending.target, "(unknown)")
                    warnings.append(
                        f"[WARN] [STATE] command readback timeout source={pending.source} "
                        f"device_id={device_id} target={pending.target} "
                        f"requested={pending.requested_state} observed={current_value}"
                    )
                    del self._pending_commands[key]

            snapshot = dict(device_state)

        return snapshot, confirmations, warnings


class TelemetrySerialSource:
    def __init__(
        self,
        label: str,
        port: str,
        baud_rate: int,
        timeout: float,
        dtr: bool,
        rts: bool,
        reconnect_delay: float,
    ) -> None:
        self.label = label
        self.port = port
        self.baud_rate = baud_rate
        self.timeout = timeout
        self.dtr = dtr
        self.rts = rts
        self.reconnect_delay = reconnect_delay
        self.serial_port = None
        self._write_lock = Lock()

    @staticmethod
    def _serial_exception_type():
        if serial is None:
            return Exception
        serial_exception = getattr(serial, "SerialException", None)
        if serial_exception is None:
            return Exception
        return serial_exception

    def is_configured(self) -> bool:
        return bool(self.port)

    def _open_port(self):
        if serial is None:
            raise RuntimeError("pyserial is not installed")

        logging.info(
            "[%s] opening %s baud_rate=%s timeout=%s dtr=%s rts=%s",
            self.label,
            self.port,
            self.baud_rate,
            self.timeout,
            self.dtr,
            self.rts,
        )

        # Open the port first, then apply DTR/RTS explicitly. This matches the
        # direct serial probing sequence that works reliably with the Hi3861
        # board on Windows.
        serial_port = serial.Serial(self.port, self.baud_rate, timeout=self.timeout)
        serial_port.dtr = self.dtr
        serial_port.rts = self.rts
        try:
            serial_port.reset_input_buffer()
        except Exception:
            pass
        return serial_port

    def ensure_port(self) -> bool:
        if not self.is_configured():
            return False

        if self.serial_port is not None and getattr(self.serial_port, "is_open", False):
            return True

        try:
            self.serial_port = self._open_port()
            return True
        except Exception as exc:
            logging.warning(
                "[WARN] [%s] open failed port=%s error=%s; retry in %.1fs",
                self.label,
                self.port,
                exc,
                self.reconnect_delay,
            )
            self.serial_port = None
            return False

    def read_line(self) -> Optional[bytes]:
        if not self.ensure_port():
            return None

        try:
            return self.serial_port.readline()
        except self._serial_exception_type() as exc:
            logging.warning(
                "[WARN] [%s] read failed port=%s error=%s; scheduling reconnect",
                self.label,
                self.port,
                exc,
            )
            self.close()
            return None

    def write_command(self, command: str) -> bool:
        if not self.ensure_port():
            return False

        payload = f"{command}\r\n".encode("ascii")
        try:
            with self._write_lock:
                self.serial_port.write(payload)
                self.serial_port.flush()
            logging.info("[%s] wrote command to device port=%s command=%s", self.label, self.port, command)
            return True
        except self._serial_exception_type() as exc:
            logging.warning(
                "[WARN] [%s] write failed port=%s command=%s error=%s",
                self.label,
                self.port,
                command,
                exc,
            )
            self.close()
            return False

    def close(self) -> None:
        if self.serial_port is None:
            return

        try:
            if getattr(self.serial_port, "is_open", False):
                logging.info("[%s] closing %s", self.label, self.port)
                self.serial_port.close()
        except Exception as exc:
            logging.warning("[WARN] [%s] close failed port=%s error=%s", self.label, self.port, exc)
        finally:
            self.serial_port = None


class F103SerialManager:
    def __init__(self, config: GatewayConfig) -> None:
        self.telemetry_source = TelemetrySerialSource(
            "F103-SERIAL",
            config.f103_serial_port,
            config.f103_baud_rate,
            config.f103_serial_timeout,
            config.f103_serial_dtr,
            config.f103_serial_rts,
            config.serial_reconnect_delay,
        )
        self.control_source: Optional[TelemetrySerialSource] = None
        if (
            config.f103_control_serial_port
            and config.f103_control_serial_port.upper() != config.f103_serial_port.upper()
        ):
            self.control_source = TelemetrySerialSource(
                "F103-CONTROL-SERIAL",
                config.f103_control_serial_port,
                config.f103_control_baud_rate,
                config.f103_control_serial_timeout,
                config.f103_control_serial_dtr,
                config.f103_control_serial_rts,
                config.serial_reconnect_delay,
            )

    def read_line(self) -> Optional[bytes]:
        return self.telemetry_source.read_line()

    def write_command(self, command: str) -> bool:
        if self.control_source is not None and self.control_source.write_command(command):
            return True
        return self.telemetry_source.write_command(command)

    def close(self) -> None:
        if self.control_source is not None:
            self.control_source.close()
        self.telemetry_source.close()


class ManagedMqttClient:
    def __init__(
        self,
        name: str,
        host: str,
        port: int,
        client_id: str,
        username: str = "",
        password: str = "",
        subscriptions: Optional[List[str]] = None,
        on_message=None,
        on_connected: Optional[Callable[["ManagedMqttClient"], None]] = None,
        reconnect_min_delay: int = 1,
        reconnect_max_delay: int = 30,
        will_topic: str = "",
        will_payload: str = "",
        will_retain: bool = False,
    ) -> None:
        self.name = name
        self.host = host
        self.port = port
        self.client_id = client_id
        self.username = username
        self.password = password
        self.subscriptions = subscriptions or []
        self.message_handler = on_message
        self.on_connected = on_connected
        self.reconnect_min_delay = reconnect_min_delay
        self.reconnect_max_delay = reconnect_max_delay
        self.will_topic = will_topic
        self.will_payload = will_payload
        self.will_retain = will_retain
        self.client = None
        self.connected = False
        self.loop_started = False
        self.stopping = False

    def start(self) -> None:
        if mqtt is None:
            logging.error("[ERROR] [%s] paho-mqtt is not installed; MQTT disabled", self.name)
            return

        self.stopping = False
        try:
            self.client = create_mqtt_client(self.client_id)
            self.client.on_connect = self._on_connect
            self.client.on_disconnect = self._on_disconnect
            if self.message_handler is not None:
                self.client.on_message = self.message_handler

            if self.username:
                self.client.username_pw_set(self.username, self.password or None)

            if self.will_topic:
                self.client.will_set(self.will_topic, payload=self.will_payload, qos=0, retain=self.will_retain)

            if hasattr(self.client, "reconnect_delay_set"):
                self.client.reconnect_delay_set(
                    min_delay=max(1, self.reconnect_min_delay),
                    max_delay=max(self.reconnect_min_delay, self.reconnect_max_delay),
                )

            logging.info(
                "[%s] connecting host=%s port=%s client_id=%s",
                self.name,
                self.host,
                self.port,
                self.client_id,
            )

            if hasattr(self.client, "connect_async"):
                self.client.connect_async(self.host, self.port, keepalive=60)
            else:
                self.client.connect(self.host, self.port, keepalive=60)

            self.client.loop_start()
            self.loop_started = True
        except Exception as exc:
            self.client = None
            self.loop_started = False
            logging.error("[ERROR] [%s] connect failed: %s", self.name, exc)

    def _on_connect(self, client, userdata, flags, reason_code, properties=None) -> None:  # noqa: ANN001
        if is_success_reason(reason_code):
            self.connected = True
            logging.info("[%s] connected", self.name)
            for topic in self.subscriptions:
                logging.info("[%s] subscribe topic=%s", self.name, topic)
                result, _mid = client.subscribe(topic, qos=0)
                success_code = getattr(mqtt, "MQTT_ERR_SUCCESS", 0) if mqtt is not None else 0
                if result != success_code:
                    logging.warning("[WARN] [%s] subscribe returned rc=%s topic=%s", self.name, result, topic)
            if self.on_connected is not None:
                self.on_connected(self)
        else:
            self.connected = False
            logging.error("[ERROR] [%s] connection rejected reason=%s", self.name, reason_code)

    def _on_disconnect(self, client, userdata, *args) -> None:  # noqa: ANN001
        self.connected = False
        reason = args[-2] if len(args) >= 2 else (args[-1] if args else "unknown")
        if self.stopping:
            logging.info("[%s] disconnected reason=%s", self.name, reason)
        else:
            logging.warning("[%s] disconnected reason=%s; auto reconnect is enabled", self.name, reason)

    def publish(self, topic: str, payload: str, retain: bool = False) -> None:
        if self.client is None:
            logging.warning("[WARN] [%s] publish skipped; MQTT client is not available", self.name)
            return

        logging.info("[%s] publish topic=%s payload=%s retain=%s", self.name, topic, payload, retain)
        try:
            result = self.client.publish(topic, payload=payload, qos=0, retain=retain)
            success_code = getattr(mqtt, "MQTT_ERR_SUCCESS", 0) if mqtt is not None else 0
            if getattr(result, "rc", success_code) != success_code:
                logging.warning("[WARN] [%s] publish returned rc=%s topic=%s", self.name, result.rc, topic)
        except Exception as exc:
            logging.error("[ERROR] [%s] publish failed topic=%s error=%s", self.name, topic, exc)

    def close(self) -> None:
        if self.client is None:
            return

        self.stopping = True
        try:
            logging.info("[%s] disconnecting", self.name)
            self.client.disconnect()
            if self.loop_started:
                logging.info("[%s] stopping MQTT loop", self.name)
                self.client.loop_stop()
                self.loop_started = False
        except Exception as exc:
            logging.error("[ERROR] [%s] shutdown error: %s", self.name, exc)
        finally:
            self.connected = False


class HomeAssistantGateway:
    def __init__(
        self,
        config: GatewayConfig,
        f103_serial_manager: Optional[F103SerialManager],
        state_cache: StateCache,
    ) -> None:
        self.config = config
        self.f103_serial_manager = f103_serial_manager
        self.state_cache = state_cache
        self.publisher: Optional[ManagedMqttClient] = None

    def start(self) -> None:
        if not self.config.ha_enabled:
            logging.info("[HA-MQTT] disabled by config")
            return

        subscriptions: List[str] = []
        if self.config.f103_enabled and self.config.ha_control_enabled:
            base_topic = self.f103_base_topic(self.config.f103_device_id)
            subscriptions.extend(
                [
                    f"{base_topic}/led/set",
                    f"{base_topic}/buzzer/set",
                ]
            )

        self.publisher = ManagedMqttClient(
            name="HA-MQTT",
            host=self.config.ha_mqtt_host,
            port=self.config.ha_mqtt_port,
            client_id="pz103_gateway_ha_multi_device",
            username=self.config.ha_mqtt_username,
            password=self.config.ha_mqtt_password,
            subscriptions=subscriptions,
            on_message=self._on_message if subscriptions else None,
            on_connected=self._on_connected,
            reconnect_min_delay=self.config.mqtt_reconnect_min_delay,
            reconnect_max_delay=self.config.mqtt_reconnect_max_delay,
        )
        self.publisher.start()

    @staticmethod
    def f103_base_topic(device_id: str) -> str:
        return f"pz103/{device_id}"

    @staticmethod
    def car_base_topic(device_id: str) -> str:
        return f"iot/{device_id}"

    def availability_topic(self, device_type: str, device_id: str) -> str:
        base_topic = self.f103_base_topic(device_id) if device_type == "f103" else self.car_base_topic(device_id)
        return f"{base_topic}/gateway/status"

    def _on_connected(self, publisher: ManagedMqttClient) -> None:
        if self.config.ha_discovery_enabled:
            self.publish_discovery()

        if self.config.f103_enabled:
            self.publish_availability("f103", self.config.f103_device_id, True)
            snapshot = self.state_cache.snapshot(self.config.f103_device_id)
            if snapshot:
                self.publish_device_state(self.config.f103_device_id, "f103", snapshot)

        if self.config.car_enabled:
            self.publish_availability("car", self.config.car_device_id, True)
            snapshot = self.state_cache.snapshot(self.config.car_device_id)
            if snapshot:
                self.publish_device_state(self.config.car_device_id, "car", snapshot)

    def publish_availability(self, device_type: str, device_id: str, is_online: bool) -> None:
        if self.publisher is None:
            return
        self.publisher.publish(
            self.availability_topic(device_type, device_id),
            "online" if is_online else "offline",
            retain=True,
        )

    def publish_discovery(self) -> None:
        if self.publisher is None:
            return

        if self.config.f103_enabled:
            self._publish_f103_discovery()
        if self.config.car_enabled:
            self._publish_car_discovery()

    def _publish_f103_discovery(self) -> None:
        if self.publisher is None:
            return

        prefix = self.config.ha_discovery_prefix.strip("/") or "homeassistant"
        device_id = self.config.f103_device_id
        device_identifier = f"pz_f103_{device_id}"
        device_payload = {
            "identifiers": [device_identifier],
            "name": "PZ F103 Board",
            "manufacturer": "PZ",
            "model": "STM32F103 UART Gateway",
            "sw_version": "python-gateway",
        }
        availability_topic = self.availability_topic("f103", device_id)
        base_topic = self.f103_base_topic(device_id)

        sensor_definitions = [
            (
                "temperature",
                {
                    "name": "PZ F103 Temperature",
                    "object_id": "pz_f103_temperature",
                    "unique_id": "pz_f103_temperature",
                    "state_topic": f"{base_topic}/temperature",
                    "unit_of_measurement": "C",
                    "device_class": "temperature",
                    "state_class": "measurement",
                },
            ),
            (
                "light",
                {
                    "name": "PZ F103 Light",
                    "object_id": "pz_f103_light",
                    "unique_id": "pz_f103_light",
                    "state_topic": f"{base_topic}/light",
                    "unit_of_measurement": "%",
                    "state_class": "measurement",
                },
            ),
            (
                "led_state",
                {
                    "name": "PZ F103 LED State",
                    "object_id": "pz_f103_led_state",
                    "unique_id": "pz_f103_led_state",
                    "state_topic": f"{base_topic}/led/state",
                    "icon": "mdi:led-on",
                },
            ),
            (
                "buzzer_state",
                {
                    "name": "PZ F103 Buzzer State",
                    "object_id": "pz_f103_buzzer_state",
                    "unique_id": "pz_f103_buzzer_state",
                    "state_topic": f"{base_topic}/buzzer/state",
                    "icon": "mdi:volume-high",
                },
            ),
        ]

        for object_suffix, payload in sensor_definitions:
            payload["availability_topic"] = availability_topic
            payload["payload_available"] = "online"
            payload["payload_not_available"] = "offline"
            payload["device"] = device_payload
            topic = f"{prefix}/sensor/{device_identifier}/{object_suffix}/config"
            self.publisher.publish(topic, json.dumps(payload, separators=(",", ":"), ensure_ascii=True), retain=True)

        if not self.config.ha_control_enabled:
            return

        switch_definitions = [
            (
                "led",
                {
                    "name": "PZ F103 LED",
                    "object_id": "pz_f103_led",
                    "unique_id": "pz_f103_led_switch",
                    "command_topic": f"{base_topic}/led/set",
                    "state_topic": f"{base_topic}/led/state",
                    "payload_on": "ON",
                    "payload_off": "OFF",
                    "state_on": "ON",
                    "state_off": "OFF",
                    "optimistic": False,
                    "retain": False,
                    "icon": "mdi:led-on",
                },
            ),
            (
                "buzzer",
                {
                    "name": "PZ F103 Buzzer",
                    "object_id": "pz_f103_buzzer",
                    "unique_id": "pz_f103_buzzer_switch",
                    "command_topic": f"{base_topic}/buzzer/set",
                    "state_topic": f"{base_topic}/buzzer/state",
                    "payload_on": "ON",
                    "payload_off": "OFF",
                    "state_on": "ON",
                    "state_off": "OFF",
                    "optimistic": False,
                    "retain": False,
                    "icon": "mdi:volume-high",
                },
            ),
        ]

        for object_suffix, payload in switch_definitions:
            payload["availability_topic"] = availability_topic
            payload["payload_available"] = "online"
            payload["payload_not_available"] = "offline"
            payload["device"] = device_payload
            topic = f"{prefix}/switch/{device_identifier}/{object_suffix}/config"
            self.publisher.publish(topic, json.dumps(payload, separators=(",", ":"), ensure_ascii=True), retain=True)

    def _publish_car_discovery(self) -> None:
        if self.publisher is None:
            return

        prefix = self.config.ha_discovery_prefix.strip("/") or "homeassistant"
        device_id = self.config.car_device_id
        device_identifier = f"hi3861_car_{device_id}"
        device_payload = {
            "identifiers": [device_identifier],
            "name": "Huawei Hi3861 Car",
            "manufacturer": "Huawei",
            "model": "Hi3861 Smart Car",
            "sw_version": "python-gateway",
        }
        availability_topic = self.availability_topic("car", device_id)
        base_topic = self.car_base_topic(device_id)

        # Remove retained discovery configs from the earlier battery schema.
        legacy_topics = [
            f"{prefix}/sensor/{device_identifier}/battery_voltage/config",
            f"{prefix}/sensor/{device_identifier}/battery_percent/config",
            f"{base_topic}/battery_voltage",
            f"{base_topic}/battery_percent",
        ]
        for topic in legacy_topics:
            self.publisher.publish(topic, "", retain=True)

        sensor_definitions = [
            (
                "status",
                {
                    "name": "Hi3861 Car Status",
                    "object_id": "hi3861_car_status",
                    "unique_id": "hi3861_car_status",
                    "state_topic": f"{base_topic}/status",
                },
            ),
            (
                "direction",
                {
                    "name": "Hi3861 Car Direction",
                    "object_id": "hi3861_car_direction",
                    "unique_id": "hi3861_car_direction",
                    "state_topic": f"{base_topic}/direction",
                },
            ),
            (
                "speed",
                {
                    "name": "Hi3861 Car Speed",
                    "object_id": "hi3861_car_speed",
                    "unique_id": "hi3861_car_speed",
                    "state_topic": f"{base_topic}/speed",
                    "unit_of_measurement": "%",
                    "state_class": "measurement",
                },
            ),
            (
                "distance_cm",
                {
                    "name": "Hi3861 Car Distance",
                    "object_id": "hi3861_car_distance",
                    "unique_id": "hi3861_car_distance",
                    "state_topic": f"{base_topic}/distance_cm",
                    "unit_of_measurement": "cm",
                    "device_class": "distance",
                    "state_class": "measurement",
                },
            ),
        ]

        for object_suffix, payload in sensor_definitions:
            payload["availability_topic"] = availability_topic
            payload["payload_available"] = "online"
            payload["payload_not_available"] = "offline"
            payload["device"] = device_payload
            topic = f"{prefix}/sensor/{device_identifier}/{object_suffix}/config"
            self.publisher.publish(topic, json.dumps(payload, separators=(",", ":"), ensure_ascii=True), retain=True)

    def _on_message(self, client, userdata, message) -> None:  # noqa: ANN001
        if self.f103_serial_manager is None:
            return

        topic = str(getattr(message, "topic", ""))
        payload_text = getattr(message, "payload", b"").decode("utf-8", errors="replace")
        logging.info("[HA-MQTT] received command topic=%s payload=%s", topic, payload_text)

        base_topic = self.f103_base_topic(self.config.f103_device_id)
        if not topic.startswith(base_topic + "/"):
            logging.warning("[WARN] [HA-MQTT] ignored command on unexpected topic=%s", topic)
            return

        requested_state = normalize_switch_payload(payload_text)
        if requested_state is None:
            logging.warning("[WARN] [HA-MQTT] invalid switch payload=%r", payload_text)
            return

        suffix = topic[len(base_topic) + 1 :]
        if suffix == "led/set":
            command = build_serial_command("led", requested_state)
            if command is None:
                return
            if self.f103_serial_manager.write_command(command):
                self.state_cache.mark_command(self.config.f103_device_id, "led", requested_state, command, "HA-MQTT")
            return

        if suffix == "buzzer/set":
            command = build_serial_command("buzzer", requested_state)
            if command is None:
                return
            if self.f103_serial_manager.write_command(command):
                self.state_cache.mark_command(
                    self.config.f103_device_id,
                    "buzzer",
                    requested_state,
                    command,
                    "HA-MQTT",
                )
            return

    def publish_device_state(self, device_id: str, device_type: str, data: Dict[str, Any]) -> None:
        if self.publisher is None:
            return

        if device_type == "f103":
            base_topic = self.f103_base_topic(device_id)
            topic_map = {
                "temperature": f"{base_topic}/temperature",
                "light": f"{base_topic}/light",
                "led": f"{base_topic}/led/state",
                "buzzer": f"{base_topic}/buzzer/state",
            }
        else:
            base_topic = self.car_base_topic(device_id)
            topic_map = {
                "status": f"{base_topic}/status",
                "direction": f"{base_topic}/direction",
                "speed": f"{base_topic}/speed",
                "distance_cm": f"{base_topic}/distance_cm",
            }

        for key, topic in topic_map.items():
            if key in data:
                self.publisher.publish(topic, str(data[key]), retain=True)

    def close(self) -> None:
        if self.publisher is None:
            return

        if self.config.f103_enabled:
            self.publish_availability("f103", self.config.f103_device_id, False)
        if self.config.car_enabled:
            self.publish_availability("car", self.config.car_device_id, False)
        self.publisher.close()


def extract_rpc_state(params: Any, target: str) -> Optional[str]:
    if isinstance(params, dict):
        candidate_keys = [target, "state", "value", "enabled", "on"]
        for key in candidate_keys:
            if key in params:
                return normalize_switch_payload(str(params[key]))
        if len(params) == 1:
            return normalize_switch_payload(str(next(iter(params.values()))))
        return None

    return normalize_switch_payload(str(params))


class ThingsBoardDeviceGateway:
    def __init__(
        self,
        name: str,
        device_id: str,
        host: str,
        port: int,
        access_token: str,
        state_cache: StateCache,
        reconnect_min_delay: int,
        reconnect_max_delay: int,
        rpc_handler: Optional[Callable[[str, str], Tuple[str, Dict[str, Any]]]] = None,
    ) -> None:
        self.name = name
        self.device_id = device_id
        self.host = host
        self.port = port
        self.access_token = access_token
        self.state_cache = state_cache
        self.reconnect_min_delay = reconnect_min_delay
        self.reconnect_max_delay = reconnect_max_delay
        self.rpc_handler = rpc_handler
        self.publisher: Optional[ManagedMqttClient] = None

    def start(self) -> None:
        if not self.access_token:
            logging.warning("[WARN] [%s] access token is empty; skipping ThingsBoard client", self.name)
            return

        subscriptions = ["v1/devices/me/rpc/request/+"] if self.rpc_handler is not None else []
        self.publisher = ManagedMqttClient(
            name=self.name,
            host=self.host,
            port=self.port,
            client_id=f"{self.name.lower().replace(' ', '_')}_{self.device_id}",
            username=self.access_token,
            password="",
            subscriptions=subscriptions,
            on_message=self._on_message if subscriptions else None,
            on_connected=self._on_connected,
            reconnect_min_delay=self.reconnect_min_delay,
            reconnect_max_delay=self.reconnect_max_delay,
        )
        self.publisher.start()

    def _on_connected(self, publisher: ManagedMqttClient) -> None:
        snapshot = self.state_cache.snapshot(self.device_id)
        if snapshot:
            self.publish_telemetry(snapshot)

    def publish_telemetry(self, data: Dict[str, Any]) -> None:
        if self.publisher is None:
            return
        payload = json.dumps(data, separators=(",", ":"), ensure_ascii=True)
        logging.info("[%s] publish telemetry payload=%s", self.name, payload)
        self.publisher.publish("v1/devices/me/telemetry", payload, retain=False)

    def _on_message(self, client, userdata, message) -> None:  # noqa: ANN001
        if self.rpc_handler is None or self.publisher is None:
            return

        topic = str(getattr(message, "topic", ""))
        payload_text = getattr(message, "payload", b"").decode("utf-8", errors="replace")
        logging.info("[%s] received rpc topic=%s payload=%s", self.name, topic, payload_text)

        request_id, response = self.rpc_handler(topic, payload_text)
        payload = json.dumps(response, separators=(",", ":"), ensure_ascii=True)
        self.publisher.publish(f"v1/devices/me/rpc/response/{request_id}", payload, retain=False)

    def close(self) -> None:
        if self.publisher is not None:
            self.publisher.close()


def build_f103_rpc_handler(
    config: GatewayConfig,
    f103_serial_manager: F103SerialManager,
    state_cache: StateCache,
) -> Callable[[str, str], Tuple[str, Dict[str, Any]]]:
    def dispatch(method: str, target: str, params: Any) -> Dict[str, Any]:
        requested_state = extract_rpc_state(params, target)
        if requested_state is None:
            return {
                "ok": False,
                "method": method,
                "error": f"invalid params for {target}; expected ON/OFF, true/false, 1/0",
            }

        command = build_serial_command(target, requested_state)
        if command is None:
            return {"ok": False, "method": method, "error": f"unsupported target {target}"}

        if not f103_serial_manager.write_command(command):
            return {
                "ok": False,
                "method": method,
                "target": target,
                "requestedState": requested_state,
                "error": "serial write failed",
            }

        state_cache.mark_command(config.f103_device_id, target, requested_state, command, "TB-RPC")
        return {
            "ok": True,
            "method": method,
            "target": target,
            "requestedState": requested_state,
            "command": command,
            "lastKnownState": state_cache.snapshot(config.f103_device_id),
        }

    def handle(topic: str, payload_text: str) -> Tuple[str, Dict[str, Any]]:
        request_id = topic.rsplit("/", 1)[-1]
        try:
            request = json.loads(payload_text) if payload_text.strip() else {}
        except json.JSONDecodeError as exc:
            return request_id, {"ok": False, "error": f"invalid JSON: {exc.msg}"}

        if not isinstance(request, dict):
            return request_id, {"ok": False, "error": "RPC payload must be a JSON object"}

        method = str(request.get("method", "")).strip()
        params = request.get("params")
        if not method:
            return request_id, {"ok": False, "error": "missing RPC method"}

        normalized = method.lower()
        if normalized in {"setled", "set_led", "setledstate", "set_led_state"}:
            return request_id, dispatch(method, "led", params)
        if normalized in {"setbuzzer", "set_buzzer", "setbuzzerstate", "set_buzzer_state"}:
            return request_id, dispatch(method, "buzzer", params)
        if normalized in {"getstatus", "get_status", "getstate", "get_state"}:
            return request_id, {
                "ok": True,
                "method": method,
                "state": state_cache.snapshot(config.f103_device_id),
            }

        return request_id, {
            "ok": False,
            "method": method,
            "error": "unsupported RPC method",
            "supportedMethods": ["setLed", "setBuzzer", "getStatus"],
        }

    return handle


def log_loaded_config(config: GatewayConfig) -> None:
    logging.info("[CONFIG] loaded sources=%s", ",".join(config.sources))
    for warning in config.warnings:
        logging.warning("[WARN] %s", warning)

    logging.info(
        "[CONFIG] f103_enabled=%s f103_serial_port=%s f103_baud_rate=%s f103_timeout=%s "
        "f103_control_serial_port=%s f103_device_id=%s car_enabled=%s car_input_mode=%s "
        "car_serial_port=%s car_baud_rate=%s car_timeout=%s car_device_id=%s "
        "serial_reconnect_delay=%s ha_enabled=%s ha_control_enabled=%s ha_discovery_enabled=%s "
        "ha=%s:%s tb_enabled=%s tb_rpc_enabled=%s tb=%s:%s f103_tb_token=%s car_tb_token=%s "
        "mqtt_reconnect_min_delay=%s mqtt_reconnect_max_delay=%s command_confirm_timeout=%s log_level=%s",
        config.f103_enabled,
        config.f103_serial_port,
        config.f103_baud_rate,
        config.f103_serial_timeout,
        config.f103_control_serial_port or "(same as F103 serial)",
        config.f103_device_id,
        config.car_enabled,
        config.car_input_mode,
        config.car_serial_port,
        config.car_baud_rate,
        config.car_serial_timeout,
        config.car_device_id,
        config.serial_reconnect_delay,
        config.ha_enabled,
        config.ha_control_enabled,
        config.ha_discovery_enabled,
        config.ha_mqtt_host,
        config.ha_mqtt_port,
        config.tb_enabled,
        config.tb_rpc_enabled,
        config.tb_mqtt_host,
        config.tb_mqtt_port,
        mask_token(config.tb_access_token),
        mask_token(config.car_tb_access_token),
        config.mqtt_reconnect_min_delay,
        config.mqtt_reconnect_max_delay,
        config.command_confirm_timeout,
        config.log_level,
    )


def main() -> int:
    config = load_config()
    configure_logging(config.log_level)
    log_loaded_config(config)

    if serial is None:
        logging.error("[ERROR] [SERIAL] pyserial is not installed; run: pip install -r requirements.txt")
        return 2

    state_cache = StateCache()
    f103_serial_manager: Optional[F103SerialManager] = None
    car_serial_source: Optional[TelemetrySerialSource] = None
    ha_gateway: Optional[HomeAssistantGateway] = None
    f103_tb_gateway: Optional[ThingsBoardDeviceGateway] = None
    car_tb_gateway: Optional[ThingsBoardDeviceGateway] = None

    if config.f103_enabled:
        f103_serial_manager = F103SerialManager(config)

    if config.car_enabled and config.car_input_mode == "serial":
        car_serial_source = TelemetrySerialSource(
            "CAR-SERIAL",
            config.car_serial_port,
            config.car_baud_rate,
            config.car_serial_timeout,
            config.car_serial_dtr,
            config.car_serial_rts,
            config.serial_reconnect_delay,
        )
    elif config.car_enabled:
        logging.warning("[WARN] [CAR] unsupported CAR_INPUT_MODE=%s; car reader disabled", config.car_input_mode)

    ha_gateway = HomeAssistantGateway(config, f103_serial_manager, state_cache)
    ha_gateway.start()

    if config.tb_enabled:
        if config.f103_enabled:
            if not config.tb_access_token or config.tb_access_token == TB_TOKEN_PLACEHOLDER:
                logging.warning("[WARN] [TB-MQTT-F103] TB_ACCESS_TOKEN is not configured; skipping F103 ThingsBoard")
            else:
                rpc_handler = (
                    build_f103_rpc_handler(config, f103_serial_manager, state_cache)
                    if config.tb_rpc_enabled and f103_serial_manager is not None
                    else None
                )
                f103_tb_gateway = ThingsBoardDeviceGateway(
                    "TB-MQTT-F103",
                    config.f103_device_id,
                    config.tb_mqtt_host,
                    config.tb_mqtt_port,
                    config.tb_access_token,
                    state_cache,
                    config.mqtt_reconnect_min_delay,
                    config.mqtt_reconnect_max_delay,
                    rpc_handler=rpc_handler,
                )
                f103_tb_gateway.start()

        if config.car_enabled:
            if not config.car_tb_access_token or config.car_tb_access_token == CAR_TB_TOKEN_PLACEHOLDER:
                logging.warning(
                    "[WARN] [TB-MQTT-CAR] CAR_TB_ACCESS_TOKEN is not configured; skipping car ThingsBoard"
                )
            else:
                car_tb_gateway = ThingsBoardDeviceGateway(
                    "TB-MQTT-CAR",
                    config.car_device_id,
                    config.tb_mqtt_host,
                    config.tb_mqtt_port,
                    config.car_tb_access_token,
                    state_cache,
                    config.mqtt_reconnect_min_delay,
                    config.mqtt_reconnect_max_delay,
                )
                car_tb_gateway.start()
    else:
        logging.info("[TB-MQTT] disabled by config")

    exit_code = 0
    f103_online = False
    car_online = False

    try:
        while True:
            did_work = False

            if f103_serial_manager is not None:
                raw = f103_serial_manager.read_line()
                if raw is None:
                    if f103_online:
                        f103_online = False
                        logging.warning("[WARN] [F103] telemetry port offline; waiting for reconnect")
                        if ha_gateway is not None:
                            ha_gateway.publish_availability("f103", config.f103_device_id, False)
                else:
                    if not f103_online:
                        f103_online = True
                        logging.info("[F103] telemetry port online")
                        if ha_gateway is not None:
                            ha_gateway.publish_availability("f103", config.f103_device_id, True)

                    if raw:
                        did_work = True
                        line = raw.decode("utf-8", errors="replace").rstrip("\r\n")
                        if line.strip():
                            logging.info("[SERIAL] received raw line: %s", line)
                            if line.startswith("TEMP:"):
                                telemetry = parse_f103_status_line(line)
                                if telemetry:
                                    snapshot, confirmations, warnings = state_cache.apply_serial_update(
                                        config.f103_device_id,
                                        telemetry,
                                        config.command_confirm_timeout,
                                    )
                                    logging.info("[SERIAL] parsed data: %s", telemetry)
                                    for message in confirmations:
                                        logging.info(message)
                                    for message in warnings:
                                        logging.warning(message)
                                    if ha_gateway is not None:
                                        ha_gateway.publish_device_state(config.f103_device_id, "f103", telemetry)
                                    if f103_tb_gateway is not None:
                                        f103_tb_gateway.publish_telemetry(snapshot)
                                else:
                                    logging.warning(
                                        "[WARN] [SERIAL] status line has no valid telemetry fields: %s",
                                        line,
                                    )
                            else:
                                logging.info("[SERIAL] ignored serial log line: %s", line)

            if car_serial_source is not None:
                raw = car_serial_source.read_line()
                if raw is None:
                    if car_online:
                        car_online = False
                        logging.warning("[WARN] [CAR] serial port offline; waiting for reconnect")
                        if ha_gateway is not None:
                            ha_gateway.publish_availability("car", config.car_device_id, False)
                else:
                    if not car_online:
                        car_online = True
                        logging.info("[CAR] serial port online")
                        if ha_gateway is not None:
                            ha_gateway.publish_availability("car", config.car_device_id, True)

                    if raw:
                        did_work = True
                        line = raw.decode("utf-8", errors="replace").rstrip("\r\n")
                        if line.strip():
                            logging.info("[CAR] raw %s", line)
                            packet = None
                            if line.lstrip().startswith("{"):
                                packet = parse_hi3861_json_line(line, config.car_device_id)
                            elif line.startswith("CAR:"):
                                packet = parse_hi3861_car_line(line, config.car_device_id)
                            else:
                                logging.info("[CAR] ignored serial log line: %s", line)

                            if packet is not None:
                                telemetry = packet["telemetry"]
                                snapshot, _confirmations, _warnings = state_cache.apply_serial_update(
                                    config.car_device_id,
                                    telemetry,
                                    config.command_confirm_timeout,
                                )
                                logging.info("[CAR] parsed %s", telemetry)
                                if ha_gateway is not None:
                                    ha_gateway.publish_device_state(config.car_device_id, "car", telemetry)
                                if car_tb_gateway is not None:
                                    car_tb_gateway.publish_telemetry(snapshot)

            if not did_work:
                time.sleep(0.1)
    except KeyboardInterrupt:
        logging.info("[GATEWAY] Ctrl+C received, shutting down")
    except Exception as exc:
        logging.exception("[ERROR] [GATEWAY] unhandled error: %s", exc)
        exit_code = 1
    finally:
        if ha_gateway is not None:
            ha_gateway.close()
        if f103_tb_gateway is not None:
            f103_tb_gateway.close()
        if car_tb_gateway is not None:
            car_tb_gateway.close()
        if f103_serial_manager is not None:
            f103_serial_manager.close()
        if car_serial_source is not None:
            car_serial_source.close()
        logging.info("[GATEWAY] stopped")

    return exit_code


if __name__ == "__main__":
    sys.exit(main())
