from __future__ import annotations

import sys


def main() -> int:
    try:
        from serial.tools import list_ports
    except ImportError:
        print("pyserial is not installed. Run: pip install -r requirements.txt")
        return 1

    ports = list(list_ports.comports())
    if not ports:
        print("No serial ports found.")
        return 0

    for port in ports:
        print(f"{port.device}\t{port.description}\t{port.hwid}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
