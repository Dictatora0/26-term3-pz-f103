#!/usr/bin/env bash
set -euo pipefail

TOPIC="verify/ping/$$"
PAYLOAD="hello_from_wsl_$$"
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

echo "[INFO] Ubuntu/WSL MQTT verify start"
echo "[INFO] WSL IPv4: $(hostname -I | awk '{print $1}')"

echo "[STEP] Check mosquitto service"
if service mosquitto status >/dev/null 2>&1; then
  service mosquitto status --no-pager || true
else
  echo "[WARN] service status command did not return success. Continue with socket checks."
fi

echo "[STEP] Check listening socket on 1883"
if ! ss -lntp | grep 1883; then
  echo "[FAIL] Nothing is listening on TCP 1883 inside WSL."
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
[PASS] WSL Mosquitto local verification OK.
[NEXT] If phone or ESP8266 still fails, check Windows portproxy/firewall:
  powershell.exe -ExecutionPolicy Bypass -File scripts/windows/setup_wsl_mqtt_portproxy.ps1
[NEXT] Hotspot clients should use Windows host IP 192.168.137.1:1883, not 127.0.0.1 and not the WSL IP.
EOF
