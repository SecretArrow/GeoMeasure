#!/bin/bash
# =============================================================================
# GeoMeasure Pro - GitHub Release Script
# =============================================================================
# Creates a GitHub release with the built APK files.
#
# Usage:
#   ./scripts/release.sh v1.0.0 "Release title" "Release notes..."
#
# Prerequisites:
#   - gh (GitHub CLI) installed
#   - GITHUB_TOKEN environment variable set, OR run gh auth login first
#   - APKs already built via ./scripts/build.sh
#
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
NOTES="${3:-}"

if [ -z "$TAG" ]; then
    echo "Usage: $0 <tag> [title] [notes]"
    echo ""
    echo "Examples:"
    echo "  $0 v1.0.0"
    echo "  $0 v1.0.0 \"GeoMeasure Pro v1.0.0\" \"Release notes here...\""
    exit 1
fi

# -------------------------------------------------
# Check prerequisites
# -------------------------------------------------
echo "============================================"
echo " GeoMeasure Pro - GitHub Release"
echo "============================================"
echo ""

# Check gh CLI
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

# Check authentication
if ! gh auth status &>/dev/null; then
    echo " ERROR: Not authenticated with GitHub."
    echo " Run: gh auth login"
    echo " Or set GITHUB_TOKEN environment variable."
    exit 1
fi

echo " Authenticated as: $(gh api user --jq .login 2>/dev/null || echo 'unknown')"
echo ""

# Check APK files
APK_FILES=()
for arch in arm64-v8a armeabi-v7a x86 x86_64; do
    apk="$APK_DIR/app-${arch}-release.apk"
    if [ -f "$apk" ]; then
        APK_FILES+=("$apk")
    fi
done

if [ ${#APK_FILES[@]} -eq 0 ]; then
    echo " ERROR: No APK files found at $APK_DIR"
    echo " Run ./scripts/build.sh first."
    exit 1
fi

echo " Found ${#APK_FILES[@]} APK files to upload:"
for apk in "${APK_FILES[@]}"; do
    echo "  - $(basename "$apk") ($(du -h "$apk" | cut -f1))"
done
echo ""

# -------------------------------------------------
# Create release
# -------------------------------------------------
echo " Creating release: $TAG"
echo ""

if [ -z "$NOTES" ]; then
    # Generate default notes
    NOTES="## GeoMeasure Pro $TAG

Precision Land Measurement — Offline, Private, Free.

### Features
- OSMDroid offline map engine with tap-to-measure
- GPS walk-to-measure with foreground service
- Area & perimeter calculation (spherical + haversine)
- 7 area units × 5 distance units
- Encrypted SQLCipher database + Android KeyStore
- Export: GeoJSON, KML, GPX, CSV, PDF
- Import: KML, GeoJSON, GPX
- Google Drive sync (optional)
- Material You dynamic theme
- 82 unit tests (all passing)
"
fi

# Build asset arguments
ASSET_ARGS=()
for apk in "${APK_FILES[@]}"; do
    name=$(basename "$apk" | sed 's/app-/GeoMeasure-Pro-/; s/-release//')
    ASSET_ARGS+=("${apk}#${name}")
done

gh release create "$TAG" \
    --title "$TITLE" \
    --notes "$NOTES" \
    "${ASSET_ARGS[@]}"

echo ""
echo "============================================"
echo " Release created successfully!"
echo " View at: https://github.com/SecretArrow/GeoMeasure/releases/tag/$TAG"
echo "============================================"
