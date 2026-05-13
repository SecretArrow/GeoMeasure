#!/bin/bash
# =============================================================================
# GeoMeasure Pro - Signing Script
# =============================================================================
# Generates a release keystore (if missing) and creates signing.properties.
#
# Usage:
#   ./scripts/sign.sh                    # Generate keystore + sign config
#   ./scripts/sign.sh /path/to/keystore  # Use existing keystore
#
# Environment variables (alternative to signing.properties):
#   GM_KEYSTORE_PATH, GM_KEYSTORE_PASSWORD, GM_KEY_ALIAS, GM_KEY_PASSWORD
#
# For Windows, use sign.bat instead.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SIGNING_PROPS="$PROJECT_DIR/signing.properties"

KEYSTORE_PATH="${1:-$PROJECT_DIR/release.keystore}"
KEYSTORE_PASS="${GM_KEYSTORE_PASSWORD:-android}"
KEY_ALIAS="${GM_KEY_ALIAS:-release}"
KEY_PASS="${GM_KEY_PASSWORD:-android}"
VALIDITY_DAYS=10000

echo "============================================"
echo " GeoMeasure Pro - APK Signing Setup"
echo "============================================"
echo ""

# -------------------------------------------------
# 1. Generate keystore if not exists
# -------------------------------------------------
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "[1/3] Generating new keystore at:"
    echo "      $KEYSTORE_PATH"
    echo ""

    keytool -genkey -v \
        -keystore "$KEYSTORE_PATH" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity "$VALIDITY_DAYS" \
        -storepass "$KEYSTORE_PASS" \
        -keypass "$KEY_PASS" \
        -dname "CN=Developer, OU=Development, O=GeoMeasure, L=Unknown, ST=Unknown, C=US" 2>&1

    echo ""
    echo "  Keystore generated successfully."
else
    echo "[1/3] Keystore already exists at:"
    echo "      $KEYSTORE_PATH"
    echo "  (using existing)"
fi
echo ""

# -------------------------------------------------
# 2. Verify keystore
# -------------------------------------------------
echo "[2/3] Verifying keystore..."
keytool -list -keystore "$KEYSTORE_PATH" \
    -storepass "$KEYSTORE_PASS" \
    -alias "$KEY_ALIAS" 2>&1 | head -5
echo "  Keystore verified."
echo ""

# -------------------------------------------------
# 3. Create signing.properties
# -------------------------------------------------
echo "[3/3] Creating signing.properties..."

cat > "$SIGNING_PROPS" << EOF
# GeoMeasure Pro - Signing Configuration
# WARNING: Keep this file secret! Do NOT commit to version control.
keystore.path=$KEYSTORE_PATH
keystore.password=$KEYSTORE_PASS
key.alias=$KEY_ALIAS
key.password=$KEY_PASS
EOF

echo "  signing.properties created."
echo ""

# -------------------------------------------------
# Summary
# -------------------------------------------------
echo "============================================"
echo " Signing setup complete!"
echo ""
echo "  Keystore:      $KEYSTORE_PATH"
echo "  Key alias:     $KEY_ALIAS"
echo "  Config file:   $SIGNING_PROPS"
echo ""
echo " Next steps:"
echo "   1. Build signed APK:    ./scripts/build.sh"
echo "   2. Verify signature:    ./scripts/sign.sh --verify"
echo "============================================"
