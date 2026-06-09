#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CERT_DIR="${TLS_CERT_DIR:-$ROOT_DIR/deploy/iboot/certs}"
STORE_FILE="${TLS_STORE_FILE:-$CERT_DIR/iboot-local.p12}"
STORE_PASSWORD="${TLS_KEY_STORE_PASSWORD:-}"
KEY_ALIAS="${TLS_KEY_ALIAS:-iboot-local}"
VALID_DAYS="${TLS_VALID_DAYS:-3650}"

if [[ -z "$STORE_PASSWORD" ]]; then
  echo "ERROR: 请先设置 TLS_KEY_STORE_PASSWORD 环境变量" >&2
  exit 1
fi

if ! command -v keytool >/dev/null 2>&1; then
  echo "ERROR: 未找到 keytool，请先安装 JDK 并确保 keytool 在 PATH 中" >&2
  exit 1
fi

mkdir -p "$CERT_DIR"

if [[ -f "$STORE_FILE" ]]; then
  echo "INFO: 证书已存在，跳过生成: $STORE_FILE"
  exit 0
fi

keytool -genkeypair \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore "$STORE_FILE" \
  -storepass "$STORE_PASSWORD" \
  -keypass "$STORE_PASSWORD" \
  -validity "$VALID_DAYS" \
  -dname "CN=localhost, OU=Lab, O=iBOOT, L=Shanghai, ST=Shanghai, C=CN" \
  -ext "SAN=dns:localhost,dns:127.0.0.1,ip:127.0.0.1"

echo "PASS: 已生成本地测试证书 $STORE_FILE"
