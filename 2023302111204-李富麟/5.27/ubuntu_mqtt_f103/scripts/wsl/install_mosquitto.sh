#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
CONFIG_SRC="$REPO_ROOT/config/mosquitto/mosquitto.conf"
TARGET_DIR="/etc/mosquitto/conf.d"
TARGET_FILE="$TARGET_DIR/ubuntu_mqtt_f103.conf"

echo "[INFO] Installing Mosquitto inside Ubuntu/WSL"
echo "[INFO] This script uses sudo and will write: $TARGET_FILE"

sudo apt update
sudo apt install -y mosquitto mosquitto-clients

if [[ ! -f "$CONFIG_SRC" ]]; then
  echo "[ERROR] Missing config template: $CONFIG_SRC" >&2
  exit 1
fi

if sudo test -f "$TARGET_FILE" && sudo cmp -s "$CONFIG_SRC" "$TARGET_FILE"; then
  echo "[INFO] Mosquitto config already up to date: $TARGET_FILE"
else
  echo "[INFO] Installing Mosquitto config: $TARGET_FILE"
  sudo install -m 644 "$CONFIG_SRC" "$TARGET_FILE"
fi

echo "[INFO] Restarting Mosquitto"
sudo service mosquitto restart
sudo service mosquitto status --no-pager || true

echo "[INFO] Listening sockets on port 1883"
ss -lntp | grep 1883 || true

cat <<'EOF'
[TEST] Run these commands in two WSL terminals:
  mosquitto_sub -h 127.0.0.1 -p 1883 -t "test/#" -v
  mosquitto_pub -h 127.0.0.1 -p 1883 -t "test/ping" -m "hello"
EOF
