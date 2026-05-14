#!/bin/bash
# =============================================================================
# GeoMeasure Pro - GitHub Release Script
# =============================================================================
# Creates a GitHub release with signed APK uploads.
#
# Usage:
#   ./scripts/release.sh v1.0.0 "Release title"
#
# Prerequisites:
#   - gh (GitHub CLI) installed
#   - APKs already built via ./scripts/build.sh
#
# Tag format: v1.0.0, v1.0.1, etc.
# For Windows, use release.bat instead.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
APK_DIR="$PROJECT_DIR/app/build/outputs/apk/release"

# -------------------------------------------------
# Parse arguments
# -------------------------------------------------
TAG="${1:-}"
TITLE="${2:-GeoMeasure Pro Release}"

if [ -z "$TAG" ]; then
    echo "Usage: $0 <tag> [title]"
    echo ""
    echo "Examples:"
    echo "  $0 v1.0.0"
    echo "  $0 v1.0.0 \"GeoMeasure Pro v1.0.0\""
    exit 1
fi

# -------------------------------------------------
# Check prerequisites
# -------------------------------------------------
echo "============================================"
echo " GeoMeasure Pro - GitHub Release"
echo "============================================"
echo ""

if ! command -v gh &>/dev/null; then
    echo " ERROR: GitHub CLI (gh) not found."
    echo " Install it from: https://cli.github.com/"
    echo ""
    echo " Quick install on Linux:"
    echo "   sudo apt-get install gh"
    echo "   # or download binary:"
    echo "   wget https://github.com/cli/cli/releases/download/v2.45.0/gh_2.45.0_linux_amd64.tar.gz"
    echo "   tar xzf gh_2.45.0_linux_amd64.tar.gz"
    echo "   sudo mv gh_2.45.0_linux_amd64/bin/gh /usr/local/bin/"
    echo ""
    echo " Then authenticate:"
    echo "   gh auth login"
    echo "   # or: echo YOUR_TOKEN | gh auth login --with-token"
    exit 1
fi

if ! gh auth status &>/dev/null; then
    echo " ERROR: Not authenticated with GitHub."
    echo " Run: gh auth login"
    echo " Or set GITHUB_TOKEN environment variable."
    exit 1
fi

echo " Authenticated as: $(gh api user --jq .login 2>/dev/null || echo 'unknown')"
echo ""

# -------------------------------------------------
# Find signed APK files
# -------------------------------------------------
APK_FILES=()
while IFS= read -r -d '' apk; do
    name=$(basename "$apk")
    # Only include signed APKs (no "unsigned" in name)
    if [[ "$name" != *"unsigned"* ]]; then
        APK_FILES+=("$apk")
    fi
done < <(find "$APK_DIR" -maxdepth 1 -name 'GeoMeasure-Pro-*.apk' -print0 2>/dev/null)

if [ ${#APK_FILES[@]} -eq 0 ]; then
    echo " ERROR: No signed APK files found at $APK_DIR"
    echo " Expected files like: GeoMeasure-Pro-arm64-v8a.apk"
    echo " Run ./scripts/build.sh first."
    exit 1
fi

echo " Found ${#APK_FILES[@]} signed APK(s) to upload:"
for apk in "${APK_FILES[@]}"; do
    echo "  - $(basename "$apk") ($(du -h "$apk" | cut -f1))"
done
echo ""

# -------------------------------------------------
# Create release
# -------------------------------------------------
echo " Creating release: $TAG"
echo ""

ASSET_ARGS=()
for apk in "${APK_FILES[@]}"; do
    name=$(basename "$apk")
    ASSET_ARGS+=("${apk}#${name}")
done

gh release create "$TAG" \
    --title "$TITLE" \
    "${ASSET_ARGS[@]}"

echo ""
echo "============================================"
echo " Release created successfully!"
echo " View at: https://github.com/SecretArrow/GeoMeasure/releases/tag/$TAG"
echo "============================================"
