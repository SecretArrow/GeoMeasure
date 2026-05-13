#!/bin/bash
# =============================================================================
# GeoMeasure Pro - Test Script
# =============================================================================
# Runs unit tests and optionally instrumentation tests.
#
# Usage:
#   ./scripts/test.sh              # Run all tests
#   ./scripts/test.sh unit         # Run only unit tests
#   ./scripts/test.sh instrument   # Run instrumentation tests (requires emulator)
#
# For Windows, use test.bat instead.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Locate Gradle
GRADLE_DIR="$HOME/gradle-8.5"
if [ ! -f "$GRADLE_DIR/bin/gradle" ]; then
    GRADLE_DIR=""
fi

# Locate Android SDK
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

GRADLE_CMD="${GRADLE_DIR:+$GRADLE_DIR/bin/gradle}"
GRADLE_CMD="${GRADLE_CMD:-gradle}"

cd "$PROJECT_DIR"

# Determine test scope
TEST_TYPE="${1:-all}"

echo "============================================"
echo " GeoMeasure Pro - Running Tests"
echo "============================================"
echo ""

case "$TEST_TYPE" in
    all)
        echo " Running ALL tests..."
        echo ""
        echo "--- Unit Tests ---"
        $GRADLE_CMD testReleaseUnitTest --daemon --console=plain
        echo ""
        echo "--- Instrumentation Tests (requires emulator/device) ---"
        set +e
        $GRADLE_CMD connectedDebugAndroidTest --daemon --console=plain 2>&1
        if [ $? -ne 0 ]; then
            echo ""
            echo " NOTE: Instrumentation tests skipped (no device/emulator connected)."
            echo " To run them, connect a device or start an emulator, then run:"
            echo "   ./scripts/test.sh instrument"
        fi
        set -e
        ;;
    unit)
        echo " Running UNIT tests only..."
        $GRADLE_CMD testReleaseUnitTest --daemon --console=plain
        ;;
    instrument)
        echo " Running INSTRUMENTATION tests only..."
        echo " (requires emulator/device connected via adb)"
        $GRADLE_CMD connectedDebugAndroidTest --daemon --console=plain
        ;;
    *)
        echo " Usage: $0 [all|unit|instrument]"
        exit 1
        ;;
esac

echo ""
echo "============================================"
echo " Tests finished. Check results above."
echo " HTML reports:"
echo "  - Unit: app/build/reports/tests/testReleaseUnitTest/index.html"
echo "  - Instrumentation: app/build/reports/androidTests/connected/index.html"
echo "============================================"
