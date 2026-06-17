from __future__ import annotations

try:
    from serial.tools import list_ports
except ImportError:
    print("pyserial is not installed. Run: pip install -r requirements.txt")
    raise SystemExit(2)


def main() -> int:
    ports = list(list_ports.comports())
    if not ports:
        print("No serial ports found.")
        return 1

    for port in ports:
        hwid = port.hwid or ""
        print(f"{port.device}\t{port.description}\t{hwid}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
