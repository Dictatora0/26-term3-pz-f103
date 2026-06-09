import argparse
import struct
import sys
import time

try:
    import cv2
except ImportError as exc:
    print(f"Missing dependency cv2: {exc}", file=sys.stderr)
    sys.exit(2)

try:
    import serial
except ImportError as exc:
    print(f"Missing dependency pyserial: {exc}", file=sys.stderr)
    sys.exit(2)


FRAME_SYNC = b"\xA5\x5A"
FRAME_ACK = 0x06
FRAME_NAK = 0x15
FRAME_DBG = 0x21

STATUS_NAMES = {
    0: "WAIT",
    1: "SYNC_TIMEOUT",
    2: "HEADER_ERROR",
    3: "HEADER_TIMEOUT",
    4: "LENGTH_ERROR",
    5: "PAYLOAD_TIMEOUT",
    6: "CRC_TIMEOUT",
    7: "CRC_ERROR",
    8: "OK",
}


def crc16_ccitt(data: bytes) -> int:
    crc = 0xFFFF
    for byte in data:
        crc ^= byte << 8
        for _ in range(8):
            if crc & 0x8000:
                crc = ((crc << 1) ^ 0x1021) & 0xFFFF
            else:
                crc = (crc << 1) & 0xFFFF
    return crc


def bgr_to_rgb565_bytes(frame_bgr, width: int, height: int) -> bytes:
    resized = cv2.resize(frame_bgr, (width, height), interpolation=cv2.INTER_AREA)
    rgb = cv2.cvtColor(resized, cv2.COLOR_BGR2RGB)

    r = (rgb[:, :, 0] >> 3).astype("uint16")
    g = (rgb[:, :, 1] >> 2).astype("uint16")
    b = (rgb[:, :, 2] >> 3).astype("uint16")
    rgb565 = ((r << 11) | (g << 5) | b).astype("uint16")
    return rgb565.tobytes()


def build_frame(seq: int, frame_bgr, width: int, height: int) -> bytes:
    payload = bgr_to_rgb565_bytes(frame_bgr, width, height)
    header = struct.pack("<2sBBBH", FRAME_SYNC, width, height, seq & 0xFF, len(payload))
    crc = crc16_ccitt(payload)
    return header + payload + struct.pack("<H", crc)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Send webcam frames to STM32 TFT LCD over UART")
    parser.add_argument("--port", required=True, help="Serial port, e.g. COM5")
    parser.add_argument("--baud", type=int, default=460800, help="UART baud rate")
    parser.add_argument("--width", type=int, default=80, help="Frame width, max 80")
    parser.add_argument("--height", type=int, default=60, help="Frame height, max 60")
    parser.add_argument("--camera", type=int, default=0, help="OpenCV camera index")
    parser.add_argument("--fps", type=float, default=3.0, help="Target send FPS")
    parser.add_argument("--show", action="store_true", help="Show local preview")
    parser.add_argument("--verbose", action="store_true", help="Print per-frame UART details")
    parser.add_argument("--boot-log-seconds", type=float, default=1.5, help="Seconds to read MCU boot logs before streaming")
    parser.add_argument("--listen-only", action="store_true", help="Only listen to MCU serial logs, do not open camera")
    parser.add_argument("--listen-seconds", type=float, default=8.0, help="Seconds to listen in --listen-only mode")
    return parser.parse_args()


def decode_serial_bytes(data: bytes) -> str:
    return data.decode("utf-8", errors="replace").replace("\r", "").strip()


def read_exact(ser: serial.Serial, size: int, timeout: float) -> bytes:
    original_timeout = ser.timeout
    ser.timeout = timeout
    try:
        return ser.read(size)
    finally:
        ser.timeout = original_timeout


def parse_debug_packet(packet: bytes) -> dict:
    return {
        "status_code": packet[1],
        "status": STATUS_NAMES.get(packet[1], f"UNKNOWN_{packet[1]}"),
        "seq": packet[2],
        "width": packet[3],
        "height": packet[4],
        "payload_len": packet[5] | (packet[6] << 8),
        "detail": packet[7] | (packet[8] << 8),
    }


def format_debug_packet(info: dict) -> str:
    return (
        f"status={info['status']} seq={info['seq']:03d} "
        f"size={info['width']}x{info['height']} payload={info['payload_len']} "
        f"detail=0x{info['detail']:04X}"
    )


def read_debug_packet_after_prefix(ser: serial.Serial) -> dict | None:
    rest = read_exact(ser, 9, 0.15)
    if len(rest) != 9:
        print(f"[uart] short debug packet: {(bytes([FRAME_DBG]) + rest).hex(' ')}")
        return None
    return parse_debug_packet(bytes([FRAME_DBG]) + rest)


def read_followup_messages(ser: serial.Serial, wait_seconds: float) -> dict | None:
    deadline = time.monotonic() + wait_seconds
    last_debug = None
    while time.monotonic() < deadline:
        if getattr(ser, "in_waiting", 0) <= 0:
            time.sleep(0.01)
            continue

        first = read_exact(ser, 1, 0.05)
        if not first:
            continue

        if first[0] == FRAME_DBG:
            last_debug = read_debug_packet_after_prefix(ser)
            if last_debug is not None:
                print(f"[mcu-debug] {format_debug_packet(last_debug)}")
            continue

        extra = b""
        if getattr(ser, "in_waiting", 0) > 0:
            extra = read_exact(ser, getattr(ser, "in_waiting", 0), 0.02)
        raw = first + extra
        text = decode_serial_bytes(raw)
        if text:
            print(f"[mcu] {text}")
        else:
            print(f"[uart] unexpected bytes: {raw.hex(' ')}")
    return last_debug


def wait_for_frame_reply(ser: serial.Serial) -> tuple[str, dict | None]:
    deadline = time.monotonic() + 0.8
    last_debug = None

    while time.monotonic() < deadline:
        first = read_exact(ser, 1, 0.2)
        if not first:
            continue

        code = first[0]
        if code == FRAME_ACK:
            read_followup_messages(ser, 0.03)
            return "ACK", last_debug
        if code == FRAME_NAK:
            last_debug = read_followup_messages(ser, 0.15) or last_debug
            return "NAK", last_debug
        if code == FRAME_DBG:
            last_debug = read_debug_packet_after_prefix(ser)
            if last_debug is not None:
                print(f"[mcu-debug] {format_debug_packet(last_debug)}")
            continue

        extra = b""
        if getattr(ser, "in_waiting", 0) > 0:
            extra = read_exact(ser, getattr(ser, "in_waiting", 0), 0.02)
        raw = first + extra
        text = decode_serial_bytes(raw)
        if text:
            print(f"[mcu] stray: {text}")
        else:
            print(f"[uart] unexpected reply bytes: {raw.hex(' ')}")

    return "TIMEOUT", last_debug


def read_serial_logs(ser: serial.Serial, seconds: float, prefix: str) -> None:
    if seconds <= 0:
        return

    deadline = time.monotonic() + seconds
    print(f"[serial] {prefix}: listening {seconds:.1f}s for MCU logs")
    while time.monotonic() < deadline:
        raw = ser.readline()
        if not raw:
            continue
        text = decode_serial_bytes(raw)
        if text:
            print(f"[mcu] {text}")


def main() -> int:
    args = parse_args()

    if args.width <= 0 or args.width > 80 or args.height <= 0 or args.height > 60:
        print("width/height must be within 1..80 and 1..60", file=sys.stderr)
        return 2
    if args.fps <= 0:
        print("fps must be > 0", file=sys.stderr)
        return 2

    ser = serial.Serial(
        port=args.port,
        baudrate=args.baud,
        bytesize=serial.EIGHTBITS,
        parity=serial.PARITY_NONE,
        stopbits=serial.STOPBITS_ONE,
        timeout=0.6,
        write_timeout=2.0,
        xonxoff=False,
        rtscts=False,
        dsrdtr=False,
    )

    try:
        ser.setDTR(False)
        ser.setRTS(False)
    except Exception:
        pass

    time.sleep(1.0)

    print(f"[serial] open port={args.port} baud={args.baud}")
    read_serial_logs(ser, args.boot_log_seconds, "boot-log")
    try:
        ser.reset_input_buffer()
    except Exception:
        pass

    if args.listen_only:
        print("[serial] listen-only mode")
        read_serial_logs(ser, args.listen_seconds, "listen-only")
        ser.close()
        return 0

    cap = cv2.VideoCapture(args.camera, cv2.CAP_DSHOW)
    if not cap.isOpened():
        cap = cv2.VideoCapture(args.camera)
    if not cap.isOpened():
        ser.close()
        print("Failed to open webcam", file=sys.stderr)
        return 1
    print(f"[camera] open index={args.camera} frame={args.width}x{args.height} fps={args.fps}")

    seq = 0
    sent = 0
    acked = 0
    naks = 0
    timeouts = 0
    period = 1.0 / args.fps
    last_report = time.perf_counter()
    last_reply = "none"

    try:
        while True:
            start = time.perf_counter()
            ok, frame = cap.read()
            if not ok:
                print("Camera frame read failed", file=sys.stderr)
                break

            frame = cv2.flip(frame, 1)
            packet = build_frame(seq, frame, args.width, args.height)
            payload_crc = struct.unpack("<H", packet[-2:])[0]
            payload_len = len(packet) - 9
            ser.write(packet)
            ser.flush()
            sent += 1

            reply_name, debug_info = wait_for_frame_reply(ser)
            if reply_name == "ACK":
                acked += 1
                last_reply = "ACK"
                seq = (seq + 1) & 0xFF
            elif reply_name == "NAK":
                naks += 1
                last_reply = "NAK"
            elif reply_name == "TIMEOUT":
                print("[uart] reply timeout")
                timeouts += 1
                last_reply = "TIMEOUT"
            else:
                last_reply = reply_name

            if debug_info is not None and args.verbose:
                print(f"[frame] mcu_debug {format_debug_packet(debug_info)}")

            if args.verbose:
                print(
                    f"[frame] seq={seq:03d} sent={sent} size={args.width}x{args.height} "
                    f"payload={payload_len} crc=0x{payload_crc:04X} reply={last_reply}"
                )
            elif (time.perf_counter() - last_report) >= 1.0:
                print(
                    f"[stats] sent={sent} ack={acked} nak={naks} timeout={timeouts} "
                    f"last_reply={last_reply}"
                )
                last_report = time.perf_counter()

            if args.show:
                preview = cv2.resize(frame, (args.width * 4, args.height * 4), interpolation=cv2.INTER_NEAREST)
                cv2.putText(
                    preview,
                    f"sent={sent} ack={acked} nak={naks} to={timeouts} {last_reply}",
                    (10, 22),
                    cv2.FONT_HERSHEY_SIMPLEX,
                    0.6,
                    (0, 255, 0),
                    2,
                    cv2.LINE_AA,
                )
                cv2.imshow("camera_to_stm32", preview)
                if cv2.waitKey(1) & 0xFF == 27:
                    break

            elapsed = time.perf_counter() - start
            if elapsed < period:
                time.sleep(period - elapsed)
    finally:
        cap.release()
        ser.close()
        cv2.destroyAllWindows()

    print(f"done: sent={sent} ack={acked} nak={naks} timeout={timeouts}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
