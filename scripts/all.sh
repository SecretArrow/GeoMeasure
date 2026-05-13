#!/bin/bash
# =============================================================================
# GeoMeasure Pro - Combined CI/CD Script
# =============================================================================
# Runs the full pipeline: install → sign → build → test → release
#
# Usage:
#   ./scripts/all.sh                          # Full pipeline
#   ./scripts/all.sh --skip-install           # Skip dependency installation
#   ./scripts/all.sh --skip-tests             # Skip tests
#   ./scripts/all.sh --tag v1.0.0             # Specify release tag
#
# Environment variables (optional):
#   GM_KEYSTORE_PATH, GM_KEYSTORE_PASSWORD, GM_KEY_ALIAS, GM_KEY_PASSWORD
#
# For Windows, use all.bat instead.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

SKIP_INSTALL=false
SKIP_TESTS=false
RELEASE_TAG=""

# Parse arguments
for arg in "$@"; do
    case "$arg" in
        --skip-install) SKIP_INSTALL=true ;;
        --skip-tests)   SKIP_TESTS=true ;;
        --tag=*)        RELEASE_TAG="${arg#*=}" ;;
        --tag)          SKIP_TESTS=true ;; # placeholder, actual tag is next arg
        *)              RELEASE_TAG="$arg" ;;
    esac
done

echo "============================================"
echo " GeoMeasure Pro - CI/CD Pipeline"
echo "============================================"
echo "  Skip install: $SKIP_INSTALL"
echo "  Skip tests:   $SKIP_TESTS"
echo "  Release tag:  ${RELEASE_TAG:-none}"
echo "============================================"
echo ""

# -------------------------------------------------
# Step 0: Install dependencies
# -------------------------------------------------
if [ "$SKIP_INSTALL" = false ]; then
    echo ">>> [1/5] Installing dependencies..."
    bash "$SCRIPT_DIR/install.sh"
    echo ""
fi

# -------------------------------------------------
# Step 1: Generate keystore (if not exists)
# -------------------------------------------------
echo ">>> [2/5] Setting up signing..."
bash "$SCRIPT_DIR/sign.sh"
echo ""

# -------------------------------------------------
# Step 2: Build APKs
# -------------------------------------------------
echo ">>> [3/5] Building release APKs..."
bash "$SCRIPT_DIR/build.sh"
echo ""

# -------------------------------------------------
# Step 3: Run tests
# -------------------------------------------------
if [ "$SKIP_TESTS" = false ]; then
    echo ">>> [4/5] Running unit tests..."
    bash "$SCRIPT_DIR/test.sh" unit
    echo ""
fi

# -------------------------------------------------
# Step 4: Create GitHub release
# -------------------------------------------------
if [ -n "$RELEASE_TAG" ]; then
    echo ">>> [5/5] Creating GitHub release $RELEASE_TAG..."
    bash "$SCRIPT_DIR/release.sh" "$RELEASE_TAG"
    echo ""
fi

echo "============================================"
echo " CI/CD Pipeline complete!"
echo "============================================"
