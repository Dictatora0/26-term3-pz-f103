#!/usr/bin/env bash
set -euo pipefail

TOPIC="verify/ping/$$"
PAYLOAD="hello_from_ubuntu_$$"
TMP_OUT="$(mktemp)"
SUB_PID=""

cleanup() {
  if [[ -n "${SUB_PID}" ]] && kill -0 "${SUB_PID}" 2>/dev/null; then
    kill "${SUB_PID}" 2>/dev/null || true
    wait "${SUB_PID}" 2>/dev/null || true
  fi
  rm -f "${TMP_OUT}"
}
trap cleanup EXIT

echo "[INFO] Ubuntu MQTT verify start"
echo "[INFO] Host IPv4: $(hostname -I | awk '{print $1}')"

echo "[STEP] Check mosquitto service"
if service mosquitto status >/dev/null 2>&1; then
  service mosquitto status --no-pager || true
else
  echo "[WARN] service status command did not return success. Continue with socket checks."
fi

echo "[STEP] Check listening socket on 1883"
if ! ss -lntp | grep 1883; then
  echo "[FAIL] Nothing is listening on TCP 1883."
  exit 1
fi

echo "[STEP] Run local pub/sub loopback test"
mosquitto_sub -h 127.0.0.1 -p 1883 -t "${TOPIC}" -C 1 -v > "${TMP_OUT}" &
SUB_PID=$!
sleep 1
mosquitto_pub -h 127.0.0.1 -p 1883 -t "${TOPIC}" -m "${PAYLOAD}"
wait "${SUB_PID}"
SUB_PID=""

RECEIVED="$(cat "${TMP_OUT}")"
EXPECTED="${TOPIC} ${PAYLOAD}"
echo "[INFO] Received: ${RECEIVED}"

if [[ "${RECEIVED}" != "${EXPECTED}" ]]; then
  echo "[FAIL] Local MQTT loopback mismatch."
  echo "[FAIL] Expected: ${EXPECTED}"
  exit 1
fi

cat <<EOF
[PASS] Ubuntu Mosquitto local verification OK.
[NEXT] Bind HC05 as a serial device, for example:
  sudo rfcomm bind 0 XX:XX:XX:XX:XX:XX 1
[NEXT] Then run the bridge:
  python3 scripts/ubuntu/bt_mqtt_bridge.py --device /dev/rfcomm0
EOF
