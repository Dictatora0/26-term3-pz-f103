#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-https://localhost:8443/api}"
AUTH_BASE="${AUTH_BASE:-https://localhost:8443}"
CLIENT_ID="${OAUTH2_CLIENT_ID:-iboot-local-web}"
REDIRECT_URI="${OAUTH2_REDIRECT_URI:-http://localhost:5173/oauth/callback.html}"
SCOPES="${OAUTH2_SCOPES:-device.read device.control user.manage}"
VIEWER_ACCOUNT="${VIEWER_ACCOUNT:-viewer}"
VIEWER_PASSWORD="${VIEWER_PASSWORD:-Viewer#2026}"
OPERATOR_ACCOUNT="${OPERATOR_ACCOUNT:-operator}"
OPERATOR_PASSWORD="${OPERATOR_PASSWORD:-Operator#2026}"
ADMIN_ACCOUNT="${ADMIN_ACCOUNT:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-}"
TEST_DEVICE_URL="${TEST_DEVICE_URL:-$BASE_URL/iot/panels/devices}"
TEST_LED_URL="${TEST_LED_URL:-$BASE_URL/iot/panels/switchCtrlStatus}"
TEST_USER_URL="${TEST_USER_URL:-$BASE_URL/core/admin/view}"
LED_BODY="${LED_BODY:-{\"id\":1832263091970150401,\"status\":\"1\"}}"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

pass() { echo "PASS: $1"; }
fail() { echo "FAIL: $1" >&2; exit 1; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "missing command: $1"
}

need_cmd curl
need_cmd openssl
if command -v python >/dev/null 2>&1; then
  PYTHON_BIN="python"
elif command -v python3 >/dev/null 2>&1; then
  PYTHON_BIN="python3"
else
  fail "missing command: python/python3"
fi

"$PYTHON_BIN" <<'PY' >"$TMP_DIR/pkce.json"
import base64, hashlib, json, secrets
verifier = base64.urlsafe_b64encode(secrets.token_bytes(48)).decode().rstrip("=")
challenge = base64.urlsafe_b64encode(hashlib.sha256(verifier.encode("ascii")).digest()).decode().rstrip("=")
state = secrets.token_urlsafe(16)
print(json.dumps({"verifier": verifier, "challenge": challenge, "state": state}))
PY

PKCE_JSON="$(cat "$TMP_DIR/pkce.json")"
CODE_VERIFIER="$("$PYTHON_BIN" - "$PKCE_JSON" <<'PY'
import json, sys
print(json.loads(sys.argv[1])["verifier"])
PY
)"
CODE_CHALLENGE="$("$PYTHON_BIN" - "$PKCE_JSON" <<'PY'
import json, sys
print(json.loads(sys.argv[1])["challenge"])
PY
)"
STATE_VALUE="$("$PYTHON_BIN" - "$PKCE_JSON" <<'PY'
import json, sys
print(json.loads(sys.argv[1])["state"])
PY
)"

curl -k -I "$AUTH_BASE" >/dev/null 2>&1 || fail "HTTPS endpoint is not reachable"
pass "HTTPS reachable"

TLS_CONNECT="$("$PYTHON_BIN" - "$AUTH_BASE" <<'PY'
import sys, urllib.parse
u = urllib.parse.urlparse(sys.argv[1])
host = u.hostname or "localhost"
port = u.port or (443 if u.scheme == "https" else 80)
print(f"{host}:{port}")
PY
)"
TLS_SERVERNAME="$("$PYTHON_BIN" - "$AUTH_BASE" <<'PY'
import sys, urllib.parse
u = urllib.parse.urlparse(sys.argv[1])
print(u.hostname or "localhost")
PY
)"

openssl s_client -connect "$TLS_CONNECT" -servername "$TLS_SERVERNAME" </dev/null >"$TMP_DIR/tls.txt" 2>&1 || fail "TLS handshake failed"
grep -q "BEGIN CERTIFICATE" "$TMP_DIR/tls.txt" || fail "TLS certificate not found in handshake output"
pass "TLS handshake succeeded"

ENCODED_SCOPES="$("$PYTHON_BIN" - "$SCOPES" <<'PY'
import sys, urllib.parse
print(urllib.parse.quote(sys.argv[1]))
PY
)"
authorize_url="$AUTH_BASE/oauth2/authorize?response_type=code&client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&scope=$ENCODED_SCOPES&state=$STATE_VALUE&code_challenge=$CODE_CHALLENGE&code_challenge_method=S256"

curl -k -sS -c "$TMP_DIR/cookies.txt" \
  -X POST "$AUTH_BASE/oauth2/doLogin" \
  -d "name=$VIEWER_ACCOUNT" \
  -d "pwd=$VIEWER_PASSWORD" >/dev/null || fail "viewer OAuth2 login failed"

auth_headers="$(curl -k -sSI -b "$TMP_DIR/cookies.txt" "$authorize_url")"
location_line="$(printf '%s\n' "$auth_headers" | tr -d '\r' | awk 'tolower($1)=="location:"{print $2}' | tail -n 1)"
[[ -n "$location_line" ]] || fail "authorization redirect location not found"

AUTH_CODE="$("$PYTHON_BIN" - "$location_line" <<'PY'
import sys, urllib.parse
u = urllib.parse.urlparse(sys.argv[1])
qs = urllib.parse.parse_qs(u.query)
print(qs.get("code", [""])[0])
PY
)"
[[ -n "$AUTH_CODE" ]] || fail "authorization code not found"
pass "Authorization Code acquired"

viewer_token_json="$(curl -k -sS -X POST "$AUTH_BASE/oauth2/token" \
  -d "grant_type=authorization_code" \
  -d "client_id=$CLIENT_ID" \
  -d "code=$AUTH_CODE" \
  -d "redirect_uri=$REDIRECT_URI" \
  -d "code_verifier=$CODE_VERIFIER")"

VIEWER_ACCESS_TOKEN="$("$PYTHON_BIN" - "$viewer_token_json" <<'PY'
import json, sys
data = json.loads(sys.argv[1])
payload = data.get("data", data)
print(payload.get("access_token", ""))
PY
)"
VIEWER_REFRESH_TOKEN="$("$PYTHON_BIN" - "$viewer_token_json" <<'PY'
import json, sys
data = json.loads(sys.argv[1])
payload = data.get("data", data)
print(payload.get("refresh_token", ""))
PY
)"

[[ -n "$VIEWER_ACCESS_TOKEN" ]] || fail "viewer access token missing"
[[ -n "$VIEWER_REFRESH_TOKEN" ]] || fail "viewer refresh token missing"
pass "Access Token acquired"
pass "Refresh Token acquired"

viewer_device_code="$(curl -k -sS -o "$TMP_DIR/viewer-device.txt" -w "%{http_code}" \
  -H "Authorization: Bearer $VIEWER_ACCESS_TOKEN" \
  "$TEST_DEVICE_URL")"
[[ "$viewer_device_code" == "200" ]] || fail "viewer device query failed: HTTP $viewer_device_code"
pass "VIEWER device query allowed"

viewer_led_code="$(curl -k -sS -o "$TMP_DIR/viewer-led.txt" -w "%{http_code}" \
  -H "Authorization: Bearer $VIEWER_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$LED_BODY" \
  "$TEST_LED_URL")"
[[ "$viewer_led_code" == "403" ]] || fail "viewer LED control should be 403, actual $viewer_led_code"
pass "VIEWER LED control denied"

no_token_code="$(curl -k -sS -o "$TMP_DIR/no-token.txt" -w "%{http_code}" "$TEST_DEVICE_URL")"
[[ "$no_token_code" == "401" ]] || fail "missing token should be 401, actual $no_token_code"
pass "Missing token returns 401"

bad_token_code="$(curl -k -sS -o "$TMP_DIR/bad-token.txt" -w "%{http_code}" \
  -H "Authorization: Bearer invalid-token" \
  "$TEST_DEVICE_URL")"
[[ "$bad_token_code" == "401" ]] || fail "invalid token should be 401, actual $bad_token_code"
pass "Invalid token returns 401"

refresh_json="$(curl -k -sS -X POST "$AUTH_BASE/oauth2/token" \
  -d "grant_type=refresh_token" \
  -d "client_id=$CLIENT_ID" \
  -d "refresh_token=$VIEWER_REFRESH_TOKEN")"
NEW_VIEWER_ACCESS_TOKEN="$("$PYTHON_BIN" - "$refresh_json" <<'PY'
import json, sys
data = json.loads(sys.argv[1])
payload = data.get("data", data)
print(payload.get("access_token", ""))
PY
)"
[[ -n "$NEW_VIEWER_ACCESS_TOKEN" ]] || fail "refresh token flow failed"
pass "Refresh Token exchange succeeded"

curl -k -sS -X POST "$AUTH_BASE/oauth2/revoke" \
  -d "client_id=$CLIENT_ID" \
  -d "token=$VIEWER_REFRESH_TOKEN" >"$TMP_DIR/revoke-refresh.json" || fail "refresh token revoke failed"
pass "Refresh Token revoked"

revoked_refresh_code="$(curl -k -sS -o "$TMP_DIR/revoked-refresh.txt" -w "%{http_code}" \
  -X POST "$AUTH_BASE/oauth2/token" \
  -d "grant_type=refresh_token" \
  -d "client_id=$CLIENT_ID" \
  -d "refresh_token=$VIEWER_REFRESH_TOKEN")"
[[ "$revoked_refresh_code" == "401" || "$revoked_refresh_code" == "500" ]] || fail "revoked refresh token should be rejected, actual $revoked_refresh_code"
pass "Revoked Refresh Token rejected"

curl -k -sS -c "$TMP_DIR/operator-cookies.txt" \
  -X POST "$AUTH_BASE/oauth2/doLogin" \
  -d "name=$OPERATOR_ACCOUNT" \
  -d "pwd=$OPERATOR_PASSWORD" >/dev/null || fail "operator OAuth2 login failed"

op_headers="$(curl -k -sSI -b "$TMP_DIR/operator-cookies.txt" "$authorize_url")"
op_location="$(printf '%s\n' "$op_headers" | tr -d '\r' | awk 'tolower($1)=="location:"{print $2}' | tail -n 1)"
OP_CODE="$("$PYTHON_BIN" - "$op_location" <<'PY'
import sys, urllib.parse
u = urllib.parse.urlparse(sys.argv[1])
qs = urllib.parse.parse_qs(u.query)
print(qs.get("code", [""])[0])
PY
)"
[[ -n "$OP_CODE" ]] || fail "operator authorization code missing"

operator_token_json="$(curl -k -sS -X POST "$AUTH_BASE/oauth2/token" \
  -d "grant_type=authorization_code" \
  -d "client_id=$CLIENT_ID" \
  -d "code=$OP_CODE" \
  -d "redirect_uri=$REDIRECT_URI" \
  -d "code_verifier=$CODE_VERIFIER")"
OPERATOR_ACCESS_TOKEN="$("$PYTHON_BIN" - "$operator_token_json" <<'PY'
import json, sys
data = json.loads(sys.argv[1])
payload = data.get("data", data)
print(payload.get("access_token", ""))
PY
)"
[[ -n "$OPERATOR_ACCESS_TOKEN" ]] || fail "operator access token missing"

operator_led_code="$(curl -k -sS -o "$TMP_DIR/operator-led.txt" -w "%{http_code}" \
  -H "Authorization: Bearer $OPERATOR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$LED_BODY" \
  "$TEST_LED_URL")"
[[ "$operator_led_code" == "200" ]] || fail "operator LED control failed: HTTP $operator_led_code"
pass "OPERATOR LED control allowed"

if [[ -n "$ADMIN_PASSWORD" ]]; then
  curl -k -sS -c "$TMP_DIR/admin-cookies.txt" \
    -X POST "$AUTH_BASE/oauth2/doLogin" \
    -d "name=$ADMIN_ACCOUNT" \
    -d "pwd=$ADMIN_PASSWORD" >/dev/null || fail "admin OAuth2 login failed"

  admin_headers="$(curl -k -sSI -b "$TMP_DIR/admin-cookies.txt" "$authorize_url")"
  admin_location="$(printf '%s\n' "$admin_headers" | tr -d '\r' | awk 'tolower($1)=="location:"{print $2}' | tail -n 1)"
  ADMIN_CODE="$("$PYTHON_BIN" - "$admin_location" <<'PY'
import sys, urllib.parse
u = urllib.parse.urlparse(sys.argv[1])
qs = urllib.parse.parse_qs(u.query)
print(qs.get("code", [""])[0])
PY
)"
  ADMIN_TOKEN_JSON="$(curl -k -sS -X POST "$AUTH_BASE/oauth2/token" \
    -d "grant_type=authorization_code" \
    -d "client_id=$CLIENT_ID" \
    -d "code=$ADMIN_CODE" \
    -d "redirect_uri=$REDIRECT_URI" \
    -d "code_verifier=$CODE_VERIFIER")"
  ADMIN_ACCESS_TOKEN="$("$PYTHON_BIN" - "$ADMIN_TOKEN_JSON" <<'PY'
import json, sys
data = json.loads(sys.argv[1])
payload = data.get("data", data)
print(payload.get("access_token", ""))
PY
)"
  admin_user_code="$(curl -k -sS -o "$TMP_DIR/admin-user.txt" -w "%{http_code}" \
    -H "Authorization: Bearer $ADMIN_ACCESS_TOKEN" \
    "$TEST_USER_URL")"
  [[ "$admin_user_code" == "200" ]] || fail "admin user management failed: HTTP $admin_user_code"
  pass "ADMIN user management allowed"
else
  echo "INFO: ADMIN_PASSWORD not set, skip admin runtime verification"
fi

echo "PASS: security flow test completed"
