#!/bin/bash
# =============================================================================
# GeoMeasure Pro - Build Script
# =============================================================================
# Builds release APKs split per ABI (armeabi-v7a, arm64-v8a, x86, x86_64).
#
# Usage:
#   ./scripts/build.sh
#
# Prerequisites: Run ./scripts/install.sh first.
#
# For Windows, use build.bat instead.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Locate Gradle
GRADLE_DIR="$HOME/gradle-8.5"
if [ ! -f "$GRADLE_DIR/bin/gradle" ]; then
    # Fall back to system gradle
    GRADLE_DIR=""
fi

# Locate Android SDK
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
if [ ! -d "$ANDROID_HOME" ]; then
    echo "ERROR: Android SDK not found at $ANDROID_HOME"
    echo "Run ./scripts/install.sh first."
    exit 1
fi

export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

echo "============================================"
echo " GeoMeasure Pro - Building Release APKs"
echo "============================================"
echo ""
echo "  Project: $PROJECT_DIR"
echo "  SDK:     $ANDROID_HOME"
echo "  Gradle:  ${GRADLE_DIR:-$HOME/gradle-8.5}"
echo ""

BUILD_TIME_START=$(date +%s)

cd "$PROJECT_DIR"

if [ -n "$GRADLE_DIR" ]; then
    "$GRADLE_DIR/bin/gradle" assembleRelease --daemon --console=plain
else
    gradle assembleRelease --daemon --console=plain
fi

BUILD_TIME_END=$(date +%s)
BUILD_DURATION=$((BUILD_TIME_END - BUILD_TIME_START))

echo ""
echo "============================================"
echo " Build completed in ${BUILD_DURATION}s"
echo "============================================"

# List APK outputs
APK_DIR="$PROJECT_DIR/app/build/outputs/apk/release"
if [ -d "$APK_DIR" ]; then
    echo ""
    echo " APK files:"
    for apk in "$APK_DIR"/*.apk; do
        size=$(du -h "$apk" | cut -f1)
        name=$(basename "$apk")
        echo "  - $name  ($size)"
    done
else
    echo " ERROR: No APK outputs found."
    exit 1
fi
echo ""
