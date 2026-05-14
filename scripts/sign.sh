#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
KEYSTORE="$PROJECT_DIR/release.keystore"
ENV_FILE="$PROJECT_DIR/.env"

FORCE=false
for arg in "$@"; do
  case "$arg" in
    --force) FORCE=true ;;
  esac
done

echo "============================================"
echo " GeoMeasure Pro - Release Signing Setup"
echo "============================================"
echo ""

# -------------------------------------------------
# 1. Load or generate credentials
# -------------------------------------------------
if [ -f "$ENV_FILE" ]; then
  echo "[1/3] Loading credentials from .env"
  set -a
  source "$ENV_FILE"
  set +a
else
  echo "[1/3] Generating random credentials..."

  STORE_PASS=$(openssl rand -base64 12 2>/dev/null || date +%s | sha256sum | base64 | head -c 16)
  # PKCS12 keystores require same password for store and key
  KEY_PASS="$STORE_PASS"

  cat > "$ENV_FILE" <<-EOF
keystore_path=$KEYSTORE
keystore_password=$STORE_PASS
key_alias=release
key_password=$STORE_PASS
EOF

  echo "  Credentials written to $ENV_FILE"

  # Source the newly created .env
  set -a
  source "$ENV_FILE"
  set +a
fi

# shellcheck disable=SC2153
KEYSTORE_PATH="${keystore_path:-$KEYSTORE}"
KEYSTORE_PASS="${keystore_password:-}"
KEY_ALIAS="${key_alias:-release}"
KEY_PASS="${key_password:-}"

echo ""

# -------------------------------------------------
# 2. Generate keystore if needed
# -------------------------------------------------
if [ -f "$KEYSTORE_PATH" ] && [ "$FORCE" = true ]; then
  echo "[2/3] --force: removing existing keystore"
  rm -f "$KEYSTORE_PATH"
fi

if [ ! -f "$KEYSTORE_PATH" ]; then
  echo "[2/3] Generating release keystore..."
  keytool -genkey -v \
    -keystore "$KEYSTORE_PATH" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass "$KEYSTORE_PASS" \
    -keypass "$KEY_PASS" \
    -dname "CN=Developer, OU=Development, O=GeoMeasure, L=Unknown, ST=Unknown, C=US"

  echo "  Keystore created: $KEYSTORE_PATH"
else
  echo "[2/3] Keystore already exists: $KEYSTORE_PATH"
fi
echo ""

# -------------------------------------------------
# 3. Verify
# -------------------------------------------------
echo "[3/3] Verifying keystore..."
keytool -list -keystore "$KEYSTORE_PATH" \
  -storepass "$KEYSTORE_PASS" \
  -alias "$KEY_ALIAS" 2>&1 | head -5
echo "  OK"
echo ""

echo "============================================"
echo " Signing setup complete!"
echo ""
echo "  Keystore:      $KEYSTORE_PATH"
echo "  Key alias:     $KEY_ALIAS"
echo "  Config file:   $ENV_FILE"
echo ""
echo " Next: ./scripts/build.sh"
echo "============================================"
