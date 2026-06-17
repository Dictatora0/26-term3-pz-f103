from __future__ import annotations

import json
import logging
import math
import os
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

try:
    import serial
except ImportError:  # pragma: no cover - handled at runtime.
    serial = None

try:
    import paho.mqtt.client as mqtt
except ImportError:  # pragma: no cover - handled at runtime.
    mqtt = None

try:
    from dotenv import load_dotenv
except ImportError:  # pragma: no cover - handled at runtime.
    load_dotenv = None


BASE_DIR = Path(__file__).resolve().parent
TB_TOKEN_PLACEHOLDER = "replace_with_thingsboard_device_access_token"

DEFAULTS: Dict[str, Any] = {
    "SERIAL_PORT": "COM3",
    "BAUD_RATE": 115200,
    "SERIAL_TIMEOUT": 1,
    "SERIAL_DTR": False,
    "SERIAL_RTS": False,
    "DEVICE_ID": "f103_01",
    "HA_ENABLED": True,
    "HA_MQTT_HOST": "127.0.0.1",
    "HA_MQTT_PORT": 1883,
    "HA_MQTT_USERNAME": "",
    "HA_MQTT_PASSWORD": "",
    "TB_ENABLED": True,
    "TB_MQTT_HOST": "127.0.0.1",
    "TB_MQTT_PORT": 1884,
    "TB_ACCESS_TOKEN": TB_TOKEN_PLACEHOLDER,
    "LOG_LEVEL": "INFO",
}


@dataclass(frozen=True)
class GatewayConfig:
    serial_port: str
    baud_rate: int
    serial_timeout: float
    serial_dtr: bool
    serial_rts: bool
    device_id: str
    ha_enabled: bool
    ha_mqtt_host: str
    ha_mqtt_port: int
    ha_mqtt_username: str
    ha_mqtt_password: str
    tb_enabled: bool
    tb_mqtt_host: str
    tb_mqtt_port: int
    tb_access_token: str
    log_level: str
    sources: Tuple[str, ...]
    warnings: Tuple[str, ...]


def configure_logging(level_name: str) -> None:
    level = getattr(logging, str(level_name).upper(), logging.INFO)
    logging.basicConfig(
        level=level,
        format="%(asctime)s %(levelname)s %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
        force=True,
    )


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

    dotenv_path = BASE_DIR / ".env"
    if dotenv_path.exists():
        if load_dotenv is None:
            warnings.append("[CONFIG] .env exists but python-dotenv is not installed; .env ignored")
        else:
            load_dotenv(dotenv_path=dotenv_path, override=False)
            sources.append(".env")

    for key in DEFAULTS:
        if key in os.environ:
            values[key] = os.environ[key]

    if not sources:
        sources.append("defaults/environment")

    return GatewayConfig(
        serial_port=str(values["SERIAL_PORT"]).strip() or str(DEFAULTS["SERIAL_PORT"]),
        baud_rate=parse_int(values["BAUD_RATE"], "BAUD_RATE", warnings),
        serial_timeout=parse_float(values["SERIAL_TIMEOUT"], "SERIAL_TIMEOUT", warnings),
        serial_dtr=parse_bool(values["SERIAL_DTR"], "SERIAL_DTR", warnings),
        serial_rts=parse_bool(values["SERIAL_RTS"], "SERIAL_RTS", warnings),
        device_id=str(values["DEVICE_ID"]).strip() or str(DEFAULTS["DEVICE_ID"]),
        ha_enabled=parse_bool(values["HA_ENABLED"], "HA_ENABLED", warnings),
        ha_mqtt_host=str(values["HA_MQTT_HOST"]).strip() or str(DEFAULTS["HA_MQTT_HOST"]),
        ha_mqtt_port=parse_int(values["HA_MQTT_PORT"], "HA_MQTT_PORT", warnings),
        ha_mqtt_username=str(values["HA_MQTT_USERNAME"]).strip(),
        ha_mqtt_password=str(values["HA_MQTT_PASSWORD"]).strip(),
        tb_enabled=parse_bool(values["TB_ENABLED"], "TB_ENABLED", warnings),
        tb_mqtt_host=str(values["TB_MQTT_HOST"]).strip() or str(DEFAULTS["TB_MQTT_HOST"]),
        tb_mqtt_port=parse_int(values["TB_MQTT_PORT"], "TB_MQTT_PORT", warnings),
        tb_access_token=str(values["TB_ACCESS_TOKEN"]).strip(),
        log_level=str(values["LOG_LEVEL"]).strip() or str(DEFAULTS["LOG_LEVEL"]),
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
        except (AttributeError, TypeError):
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


class MqttPublisher:
    def __init__(
        self,
        name: str,
        host: str,
        port: int,
        client_id: str,
        username: str = "",
        password: str = "",
    ) -> None:
        self.name = name
        self.host = host
        self.port = port
        self.client_id = client_id
        self.username = username
        self.password = password
        self.client = None
        self.loop_started = False

    def connect(self) -> None:
        if mqtt is None:
            logging.error("[ERROR] [%s] paho-mqtt is not installed; MQTT disabled", self.name)
            return

        try:
            self.client = create_mqtt_client(self.client_id)
            self.client.on_connect = self._on_connect
            self.client.on_disconnect = self._on_disconnect
            if self.username:
                self.client.username_pw_set(self.username, self.password or None)

            logging.info("[%s] connecting %s:%s client_id=%s", self.name, self.host, self.port, self.client_id)
            self.client.connect(self.host, self.port, keepalive=60)
            self.client.loop_start()
            self.loop_started = True
        except Exception as exc:
            self.client = None
            self.loop_started = False
            logging.error("[ERROR] [%s] connect failed: %s", self.name, exc)

    def _on_connect(self, client, userdata, flags, reason_code, properties=None) -> None:  # noqa: ANN001
        if is_success_reason(reason_code):
            logging.info("[%s] connected", self.name)
        else:
            logging.error("[ERROR] [%s] connection rejected reason=%s", self.name, reason_code)

    def _on_disconnect(self, client, userdata, *args) -> None:  # noqa: ANN001
        reason = args[-2] if len(args) >= 2 else (args[-1] if args else "unknown")
        logging.info("[%s] disconnected reason=%s", self.name, reason)

    def publish(self, topic: str, payload: str) -> None:
        if self.client is None:
            logging.warning("[WARN] [%s] publish skipped; MQTT client is not available", self.name)
            return

        logging.info("[%s] publish %s %s", self.name, topic, payload)
        try:
            result = self.client.publish(topic, payload=payload, qos=0, retain=False)
            success_code = getattr(mqtt, "MQTT_ERR_SUCCESS", 0) if mqtt is not None else 0
            if getattr(result, "rc", success_code) != success_code:
                logging.warning("[WARN] [%s] publish returned rc=%s topic=%s", self.name, result.rc, topic)
        except Exception as exc:
            logging.error("[ERROR] [%s] publish failed topic=%s error=%s", self.name, topic, exc)

    def close(self) -> None:
        if self.client is None:
            return

        try:
            if self.loop_started:
                logging.info("[%s] stopping MQTT loop", self.name)
                self.client.loop_stop()
                self.loop_started = False
            logging.info("[%s] disconnecting", self.name)
            self.client.disconnect()
        except Exception as exc:
            logging.error("[ERROR] [%s] shutdown error: %s", self.name, exc)


def parse_number(raw_value: str) -> int | float:
    value = float(raw_value)
    if not math.isfinite(value):
        raise ValueError("number must be finite")

    if any(marker in raw_value for marker in (".", "e", "E")):
        return value
    return int(raw_value)


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
                temperature = float(raw_value)
                if not math.isfinite(temperature):
                    raise ValueError("temperature must be finite")
                parsed["temperature"] = temperature
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


def publish_home_assistant(publisher: Optional[MqttPublisher], config: GatewayConfig, data: Dict[str, Any]) -> None:
    if publisher is None:
        return

    base_topic = f"pz103/{config.device_id}"
    topic_map = {
        "temperature": f"{base_topic}/temperature",
        "light": f"{base_topic}/light",
        "led": f"{base_topic}/led/state",
        "buzzer": f"{base_topic}/buzzer/state",
    }

    for key, topic in topic_map.items():
        if key in data:
            publisher.publish(topic, str(data[key]))


def publish_thingsboard(publisher: Optional[MqttPublisher], data: Dict[str, Any]) -> None:
    if publisher is None:
        return

    payload = json.dumps(data, separators=(",", ":"), ensure_ascii=True)
    logging.info("[TB-MQTT] publish v1/devices/me/telemetry %s", payload)
    publisher.publish("v1/devices/me/telemetry", payload)


def open_serial(config: GatewayConfig):
    if serial is None:
        raise RuntimeError("pyserial is not installed")

    logging.info("[SERIAL] opening %s at %s", config.serial_port, config.baud_rate)
    serial_port = serial.Serial()
    serial_port.port = config.serial_port
    serial_port.baudrate = config.baud_rate
    serial_port.timeout = config.serial_timeout
    serial_port.dtr = config.serial_dtr
    serial_port.rts = config.serial_rts
    serial_port.open()
    return serial_port


def log_loaded_config(config: GatewayConfig) -> None:
    logging.info("[CONFIG] loaded %s", ",".join(config.sources))
    for warning in config.warnings:
        logging.warning("[WARN] %s", warning)

    token_state = "configured"
    if not config.tb_access_token:
        token_state = "empty"
    elif config.tb_access_token == TB_TOKEN_PLACEHOLDER:
        token_state = "placeholder"

    logging.info(
        "[CONFIG] serial_port=%s baud_rate=%s serial_timeout=%s device_id=%s ha_enabled=%s ha=%s:%s tb_enabled=%s tb=%s:%s tb_token=%s log_level=%s",
        config.serial_port,
        config.baud_rate,
        config.serial_timeout,
        config.device_id,
        config.ha_enabled,
        config.ha_mqtt_host,
        config.ha_mqtt_port,
        config.tb_enabled,
        config.tb_mqtt_host,
        config.tb_mqtt_port,
        token_state,
        config.log_level,
    )


def main() -> int:
    config = load_config()
    configure_logging(config.log_level)
    log_loaded_config(config)

    if serial is None:
        logging.error("[ERROR] [SERIAL] pyserial is not installed; run: pip install -r requirements.txt")
        return 2

    ha_publisher: Optional[MqttPublisher] = None
    tb_publisher: Optional[MqttPublisher] = None
    serial_port = None
    exit_code = 0

    try:
        serial_port = open_serial(config)

        if config.ha_enabled:
            ha_publisher = MqttPublisher(
                name="HA-MQTT",
                host=config.ha_mqtt_host,
                port=config.ha_mqtt_port,
                client_id=f"pz103_gateway_ha_{config.device_id}",
                username=config.ha_mqtt_username,
                password=config.ha_mqtt_password,
            )
            ha_publisher.connect()
        else:
            logging.info("[HA-MQTT] disabled by config")

        if config.tb_enabled:
            if not config.tb_access_token or config.tb_access_token == TB_TOKEN_PLACEHOLDER:
                logging.warning("[WARN] [TB-MQTT] TB_ACCESS_TOKEN is not configured; skipping ThingsBoard publishing")
            else:
                tb_publisher = MqttPublisher(
                    name="TB-MQTT",
                    host=config.tb_mqtt_host,
                    port=config.tb_mqtt_port,
                    client_id=f"pz103_gateway_tb_{config.device_id}",
                    username=config.tb_access_token,
                    password="",
                )
                tb_publisher.connect()
        else:
            logging.info("[TB-MQTT] disabled by config")

        while True:
            raw = serial_port.readline()
            if not raw:
                continue

            line = raw.decode("utf-8", errors="replace").rstrip("\r\n")
            if not line.strip():
                continue

            logging.info("[SERIAL] raw %s", line)
            if not line.startswith("TEMP:"):
                logging.info("[SERIAL] board log ignored: %s", line)
                continue

            data = parse_f103_status_line(line)
            if not data:
                logging.warning("[WARN] [SERIAL] status line has no valid telemetry fields: %s", line)
                continue

            logging.info("[SERIAL] parsed %s", data)
            publish_home_assistant(ha_publisher, config, data)
            publish_thingsboard(tb_publisher, data)
    except KeyboardInterrupt:
        logging.info("[GATEWAY] Ctrl+C received, shutting down")
    except Exception as exc:
        serial_exception = getattr(serial, "SerialException", None)
        if serial_exception is not None and isinstance(exc, serial_exception):
            logging.error("[ERROR] [SERIAL] serial error: %s", exc)
        else:
            logging.exception("[ERROR] [GATEWAY] unhandled error: %s", exc)
        exit_code = 1
    finally:
        if ha_publisher is not None:
            ha_publisher.close()
        if tb_publisher is not None:
            tb_publisher.close()
        if serial_port is not None and getattr(serial_port, "is_open", False):
            logging.info("[SERIAL] closing %s", config.serial_port)
            serial_port.close()
        logging.info("[GATEWAY] stopped")

    return exit_code


if __name__ == "__main__":
    sys.exit(main())
