#!/usr/bin/env python3
import argparse
import json
import logging
import socket
import sys
import threading
import time

try:
    import serial
except ImportError as exc:
    print("Missing dependency: pyserial", file=sys.stderr)
    print("Install with: pip install -r scripts/ubuntu/requirements.txt", file=sys.stderr)
    raise SystemExit(1) from exc

try:
    import paho.mqtt.client as mqtt
except ImportError as exc:
    print("Missing dependency: paho-mqtt", file=sys.stderr)
    print("Install with: pip install -r scripts/ubuntu/requirements.txt", file=sys.stderr)
    raise SystemExit(1) from exc


def clean_line(text):
    return text.replace("\r", "").replace("\n", "")


class BtMqttBridge:
    def __init__(self, args):
        self.args = args
        self.serial_port = None
        self.serial_lock = threading.RLock()
        self.stop_requested = False

        self.client = mqtt.Client(client_id=args.client_id, clean_session=True, protocol=mqtt.MQTTv311)
        if args.username:
            self.client.username_pw_set(args.username, args.password)
        self.client.on_connect = self.on_connect
        self.client.on_disconnect = self.on_disconnect
        self.client.on_message = self.on_message
        self.client.reconnect_delay_set(min_delay=1, max_delay=10)

    def publish_bridge_status(self, event, detail):
        payload = json.dumps(
            {
                "event": event,
                "detail": detail,
                "host": socket.gethostname(),
                "serial": self.args.device,
                "bridge_client": self.args.client_id,
            },
            ensure_ascii=False,
        )
        self.client.publish(self.args.topic_status, payload)

    def on_connect(self, client, userdata, flags, rc, properties=None):
        logging.info("mqtt connected rc=%s", rc)
        client.subscribe(self.args.topic_control)
        client.subscribe(self.args.topic_text_tx)
        client.subscribe(self.args.topic_bt_raw_tx)
        self.publish_bridge_status("bridge_online", "mqtt_connected")

    def on_disconnect(self, client, userdata, rc, properties=None):
        logging.warning("mqtt disconnected rc=%s", rc)

    def on_message(self, client, userdata, msg):
        payload = msg.payload.decode("utf-8", errors="replace")
        logging.info("mqtt->bridge topic=%s payload=%s", msg.topic, payload)

        if msg.topic == self.args.topic_control:
            line = clean_line(payload)
        elif msg.topic == self.args.topic_text_tx:
            line = "MSG:" + clean_line(payload)
        else:
            line = clean_line(payload)

        if not line:
            return

        self.write_serial_line(line)

    def open_serial(self):
        while not self.stop_requested:
            with self.serial_lock:
                if self.serial_port is not None:
                    return self.serial_port

                try:
                    logging.info("opening serial device %s @ %s", self.args.device, self.args.baud)
                    port = serial.Serial(
                        self.args.device,
                        self.args.baud,
                        timeout=self.args.read_timeout,
                    )
                    port.reset_input_buffer()
                    port.reset_output_buffer()
                    self.serial_port = port
                    logging.info("serial opened device=%s", self.args.device)
                    self.publish_bridge_status("bridge_serial", "opened")
                    return self.serial_port
                except serial.SerialException as exc:
                    logging.error("open serial failed: %s", exc)

            time.sleep(2)

        return None

    def close_serial(self):
        port = None

        with self.serial_lock:
            if self.serial_port is not None:
                port = self.serial_port
                self.serial_port = None

        if port is not None:
            try:
                port.close()
            except serial.SerialException:
                pass

    def write_serial_line(self, line):
        wire = (clean_line(line) + "\r\n").encode("utf-8")

        while not self.stop_requested:
            with self.serial_lock:
                port = self.serial_port

            if port is None:
                self.open_serial()
                with self.serial_lock:
                    port = self.serial_port
                if port is None:
                    return
            try:
                port.write(wire)
                port.flush()
                logging.info("bridge->bt line=%s", clean_line(line))
                return
            except serial.SerialException as exc:
                logging.error("serial write failed: %s", exc)
                self.close_serial()
                time.sleep(1)

    def handle_serial_line(self, line):
        prefix, sep, payload = line.partition(":")
        prefix_upper = prefix.upper()
        payload = payload.strip()

        logging.info("bt->bridge line=%s", line)
        self.client.publish(self.args.topic_bt_raw_rx, line)

        if sep and prefix_upper == "TELEMETRY":
            self.client.publish(self.args.topic_telemetry, payload)
        elif sep and prefix_upper in ("STATUS", "ACK"):
            self.client.publish(self.args.topic_status, payload)
        elif sep and prefix_upper in ("TEXT", "MSG_RX"):
            self.client.publish(self.args.topic_text_rx, payload)

    def run(self):
        try:
            self.client.connect(self.args.broker_host, self.args.broker_port, keepalive=60)
        except Exception as exc:
            logging.error("mqtt connect failed: %s", exc)
            return 1

        self.client.loop_start()
        self.open_serial()

        try:
            while not self.stop_requested:
                if self.serial_port is None:
                    self.open_serial()
                    continue

                try:
                    raw = self.serial_port.readline()
                except serial.SerialException as exc:
                    logging.error("serial read failed: %s", exc)
                    self.close_serial()
                    time.sleep(1)
                    continue

                if not raw:
                    continue

                line = raw.decode("utf-8", errors="replace").strip()
                if not line:
                    continue

                self.handle_serial_line(line)
        except KeyboardInterrupt:
            logging.info("stop requested by keyboard interrupt")
        finally:
            self.publish_bridge_status("bridge_offline", "stopped")
            self.client.loop_stop()
            self.client.disconnect()
            self.close_serial()

        return 0


def parse_args():
    parser = argparse.ArgumentParser(description="Bluetooth HC05 to MQTT bridge for STM32F103 text messaging.")
    parser.add_argument("--device", default="/dev/rfcomm0", help="Bluetooth serial device, e.g. /dev/rfcomm0")
    parser.add_argument("--baud", type=int, default=9600, help="Bluetooth UART baud rate")
    parser.add_argument("--broker-host", default="127.0.0.1", help="MQTT broker host")
    parser.add_argument("--broker-port", type=int, default=1883, help="MQTT broker port")
    parser.add_argument("--client-id", default="ubuntu_bt_bridge", help="MQTT client id for the bridge")
    parser.add_argument("--username", default="", help="MQTT username")
    parser.add_argument("--password", default="", help="MQTT password")
    parser.add_argument("--read-timeout", type=float, default=0.2, help="Serial read timeout in seconds")
    parser.add_argument("--topic-control", default="pz103/control", help="MQTT topic forwarded to board control")
    parser.add_argument("--topic-telemetry", default="pz103/telemetry", help="MQTT topic for board telemetry")
    parser.add_argument("--topic-status", default="pz103/status", help="MQTT topic for board and bridge status")
    parser.add_argument("--topic-text-tx", default="pz103/text/tx", help="MQTT topic for text sent to the board")
    parser.add_argument("--topic-text-rx", default="pz103/text/rx", help="MQTT topic for text received from the board")
    parser.add_argument("--topic-bt-raw-tx", default="pz103/bluetooth/tx", help="MQTT raw pass-through topic to board")
    parser.add_argument("--topic-bt-raw-rx", default="pz103/bluetooth/rx", help="MQTT raw mirror topic from board")
    parser.add_argument("--log-level", default="INFO", choices=["DEBUG", "INFO", "WARNING", "ERROR"], help="Logging level")
    return parser.parse_args()


def main():
    args = parse_args()
    logging.basicConfig(
        level=getattr(logging, args.log_level),
        format="%(asctime)s %(levelname)s %(message)s",
    )
    bridge = BtMqttBridge(args)
    return bridge.run()


if __name__ == "__main__":
    raise SystemExit(main())
