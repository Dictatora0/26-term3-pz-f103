import os
import time

import serial


def main() -> None:
    port = os.environ.get("CONTROL_SERIAL_PORT") or os.environ.get("SERIAL_PORT", "COM3")
    baud_rate = int(os.environ.get("CONTROL_BAUD_RATE") or os.environ.get("BAUD_RATE", "115200"))
    command = os.environ.get("F103_COMMAND", "LED_ON").encode("ascii") + b"\n"

    print("If this sends to USART3/PB11, watch the USART1 telemetry log for [UART3-RX].")
    for dtr, rts in ((False, False), (False, True), (True, False), (True, True)):
        print(f"--- port={port} baud={baud_rate} dtr={dtr} rts={rts} command={command!r} ---")
        ser = serial.Serial()
        ser.port = port
        ser.baudrate = baud_rate
        ser.timeout = 0.25
        ser.dtr = dtr
        ser.rts = rts
        ser.open()
        time.sleep(1)
        ser.write(command)
        ser.flush()

        end = time.time() + 3
        while time.time() < end:
            line = ser.readline()
            if line:
                print(line.decode(errors="replace").rstrip())

        ser.close()
        time.sleep(1)


if __name__ == "__main__":
    main()
