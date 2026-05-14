#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

GRADLE_DIR="${GRADLE_DIR:-$PROJECT_DIR/../gradle-8.5}"
# Also check common locations
if [ ! -f "$GRADLE_DIR/bin/gradle" ]; then
    GRADLE_DIR="$HOME/gradle-8.5"
fi
if [ ! -f "$GRADLE_DIR/bin/gradle" ]; then
    GRADLE_DIR="/opt/gradle-8.5"
fi
if [ ! -f "$GRADLE_DIR/bin/gradle" ]; then
    GRADLE_DIR=""
fi

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
if [ ! -d "$ANDROID_HOME" ]; then
    echo "ERROR: Android SDK not found at $ANDROID_HOME"
    echo "Run ./scripts/install.sh first."
    exit 1
fi

export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

SIGNED=false
ENV_FILE="$PROJECT_DIR/.env"
if [ -f "$ENV_FILE" ]; then
    set -a
    source "$ENV_FILE"
    set +a
    SIGNED=true
fi

echo "============================================"
echo " GeoMeasure Pro - Building Release APKs"
echo "============================================"
echo ""
echo "  Project: $PROJECT_DIR"
echo "  SDK:     $ANDROID_HOME"
echo "  Gradle:  ${GRADLE_DIR:-$HOME/gradle-8.5}"
echo "  Signing: $([ "$SIGNED" = true ] && echo "enabled" || echo "disabled")"
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

APK_DIR="$PROJECT_DIR/app/build/outputs/apk/release"
if [ -d "$APK_DIR" ]; then
    # Rename all APKs to GeoMeasure-Pro naming scheme
    for apk in "$APK_DIR"/app-*-release.apk; do
        [ -f "$apk" ] || continue
        base=$(basename "$apk" .apk)
        arch="${base#app-}"
        arch="${arch%-release}"
        if [ "$SIGNED" = true ]; then
            mv "$apk" "$APK_DIR/GeoMeasure-Pro-$arch.apk"
        else
            mv "$apk" "$APK_DIR/GeoMeasure-Pro-unsigned-$arch.apk"
        fi
    done

    echo ""
    echo " APK files:"
    for apk in "$APK_DIR"/*.apk; do
        size=$(du -h "$apk" | cut -f1)
        name=$(basename "$apk")
        echo "  - $name  ($size)"
    done

    echo ""
    echo " Signing status: $([ "$SIGNED" = true ] && echo "signed" || echo "unsigned")"
else
    echo " ERROR: No APK outputs found."
    exit 1
fi
echo ""
