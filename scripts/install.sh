#!/bin/bash
# =============================================================================
# GeoMeasure Pro - Environment Setup Script
# =============================================================================
# This script installs all required dependencies for building GeoMeasure Pro:
#   - Java 17 JDK
#   - Android SDK (command-line tools, platform 35, build-tools)
#   - Gradle 8.5
#
# Usage:
#   chmod +x scripts/install.sh
#   sudo ./scripts/install.sh
#
# For Windows, use install.bat instead.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "============================================"
echo " GeoMeasure Pro - Environment Setup"
echo "============================================"
echo ""

# -------------------------------------------------
# 1. Install Java 17 JDK
# -------------------------------------------------
echo "[1/5] Installing Java 17 JDK..."
if command -v java &>/dev/null && java -version 2>&1 | grep -q "17"; then
    echo "  Java 17 already installed. Skipping."
else
    if command -v apt-get &>/dev/null; then
        sudo apt-get update -qq
        sudo apt-get install -y -qq openjdk-17-jdk unzip wget
    elif command -v brew &>/dev/null; then
        brew install openjdk@17
    else
        echo "  WARNING: Could not install Java 17 automatically."
        echo "  Please install Java 17 manually and re-run this script."
    fi
fi
echo "  Java version: $(java -version 2>&1 | head -1)"
echo ""

# -------------------------------------------------
# 2. Set up Android SDK
# -------------------------------------------------
ANDROID_SDK_DIR="$HOME/android-sdk"
export ANDROID_HOME="$ANDROID_SDK_DIR"
export ANDROID_SDK_ROOT="$ANDROID_SDK_DIR"

echo "[2/5] Setting up Android SDK at $ANDROID_SDK_DIR..."

if [ ! -f "$ANDROID_SDK_DIR/cmdline-tools/latest/bin/sdkmanager" ]; then
    mkdir -p "$ANDROID_SDK_DIR/cmdline-tools"
    CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    echo "  Downloading Android command-line tools..."
    wget -q "$CMDLINE_TOOLS_URL" -O /tmp/cmdline-tools.zip
    unzip -q /tmp/cmdline-tools.zip -d "$ANDROID_SDK_DIR/cmdline-tools/"
    mv "$ANDROID_SDK_DIR/cmdline-tools/cmdline-tools" "$ANDROID_SDK_DIR/cmdline-tools/latest"
    rm /tmp/cmdline-tools.zip
    echo "  Command-line tools extracted."
else
    echo "  Android command-line tools already present. Skipping download."
fi

# Accept licenses and install SDK components
echo "  Accepting licenses and installing SDK components..."
yes | "$ANDROID_SDK_DIR/cmdline-tools/latest/bin/sdkmanager" \
    --sdk_root="$ANDROID_SDK_DIR" \
    "platforms;android-35" \
    "build-tools;35.0.0" \
    "platform-tools" > /dev/null 2>&1 || true
echo "  Android SDK ready."
echo ""

# -------------------------------------------------
# 3. Install Gradle
# -------------------------------------------------
GRADLE_VERSION="8.5"
GRADLE_DIR="$HOME/gradle-$GRADLE_VERSION"

echo "[3/5] Setting up Gradle $GRADLE_VERSION..."

if [ ! -f "$GRADLE_DIR/bin/gradle" ]; then
    echo "  Downloading Gradle $GRADLE_VERSION..."
    wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -O /tmp/gradle.zip
    unzip -q /tmp/gradle.zip -d "$HOME/"
    rm /tmp/gradle.zip
    echo "  Gradle $GRADLE_VERSION ready at $GRADLE_DIR."
else
    echo "  Gradle already present. Skipping."
fi

# -------------------------------------------------
# 4. Create local.properties
# -------------------------------------------------
echo "[4/5] Creating local.properties..."
cat > "$PROJECT_DIR/local.properties" << EOF
sdk.dir=$ANDROID_SDK_DIR
EOF
echo "  local.properties created."
echo ""

# -------------------------------------------------
# 5. Verify installation
# -------------------------------------------------
echo "[5/5] Verifying installation..."
echo "  Java:    $(java -version 2>&1 | head -1)"
echo "  Gradle:  $("$GRADLE_DIR/bin/gradle" --version 2>&1 | grep "Gradle " | head -1)"
echo "  SDK:     $("$ANDROID_SDK_DIR/cmdline-tools/latest/bin/sdkmanager" --list 2>/dev/null | head -1 || echo "available")"
echo ""

echo "============================================"
echo " Setup complete!"
echo ""
echo " To build the project, run:"
echo "   ./scripts/build.sh"
echo ""
echo " To run tests, run:"
echo "   ./scripts/test.sh"
echo ""
echo " To create a GitHub release, run:"
echo "   ./scripts/release.sh"
echo "============================================"
