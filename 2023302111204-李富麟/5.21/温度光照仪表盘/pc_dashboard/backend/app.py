import argparse
from pathlib import Path

from flask import Flask, jsonify, send_from_directory

from serial_reader import SerialReader


def create_app(reader: SerialReader) -> Flask:
    frontend_dir = Path(__file__).resolve().parents[1] / "frontend"
    app = Flask(__name__, static_folder=str(frontend_dir), static_url_path="")

    @app.get("/")
    def index():
        return send_from_directory(frontend_dir, "index.html")

    @app.get("/api/latest")
    def api_latest():
        latest = reader.latest()
        if latest is None:
            return jsonify(
                {
                    "connected": reader.connected,
                    "temperature": None,
                    "light": None,
                    "humidity": None,
                    "timestamp": None,
                    "message": "waiting_data",
                }
            )

        return jsonify(
            {
                "connected": reader.connected,
                "temperature": latest.get("temperature"),
                "light": latest.get("light"),
                "humidity": latest.get("humidity"),
                "timestamp": latest.get("timestamp"),
                "board_timestamp_ms": latest.get("board_timestamp_ms"),
                "message": "ok",
            }
        )

    @app.get("/api/history")
    def api_history():
        items = reader.history(limit=100)
        return jsonify(
            {
                "connected": reader.connected,
                "count": len(items),
                "items": items,
            }
        )

    @app.get("/api/debug")
    def api_debug():
        return jsonify(reader.debug_snapshot())

    @app.get("/<path:asset>")
    def static_assets(asset: str):
        return send_from_directory(frontend_dir, asset)

    return app


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="STM32 Dashboard Backend")
    parser.add_argument("--port", default=None, help="Serial port, e.g. COM3")
    parser.add_argument("--baudrate", type=int, default=115200)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--http-port", type=int, default=5000)
    parser.add_argument("--history", type=int, default=100)
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    reader = SerialReader(
        port=args.port,
        baudrate=args.baudrate,
        history_size=args.history,
    )
    reader.start()

    app = create_app(reader)

    try:
        app.run(host=args.host, port=args.http_port, debug=False)
    finally:
        reader.stop()


if __name__ == "__main__":
    main()
