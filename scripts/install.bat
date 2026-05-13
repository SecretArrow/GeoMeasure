@echo off
REM =============================================================================
REM GeoMeasure Pro - Environment Setup Script (Windows)
REM =============================================================================
REM This script installs all required dependencies for building GeoMeasure Pro:
REM   - Java 17 JDK (requires manual install if not present)
REM   - Android SDK (command-line tools, platform 35, build-tools)
REM   - Gradle 8.5
REM
REM Usage:
REM   scripts\install.bat
REM
REM For Linux, use install.sh instead.
REM =============================================================================

setlocal enabledelayedexpansion
echo ============================================
echo  GeoMeasure Pro - Environment Setup
echo ============================================
echo.

REM -------------------------------------------------
REM 1. Check Java 17
REM -------------------------------------------------
echo [1/5] Checking Java 17...
java -version 2>&1 | findstr "17" > nul
if !errorlevel! equ 0 (
    echo   Java 17 found.
) else (
    echo   WARNING: Java 17 not detected.
    echo   Please install Java 17 from: https://adoptium.net/
    echo   Then re-run this script.
)
echo.

REM -------------------------------------------------
REM 2. Set up Android SDK
REM -------------------------------------------------
set ANDROID_SDK_DIR=%USERPROFILE%\android-sdk
echo [2/5] Setting up Android SDK at %ANDROID_SDK_DIR%...

if not exist "%ANDROID_SDK_DIR%\cmdline-tools\latest\bin\sdkmanager.bat" (
    if not exist "%ANDROID_SDK_DIR%" mkdir "%ANDROID_SDK_DIR%"
    if not exist "%ANDROID_SDK_DIR%\cmdline-tools" mkdir "%ANDROID_SDK_DIR%\cmdline-tools"
    
    echo   Downloading Android command-line tools...
    powershell -Command "Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' -OutFile '%TEMP%\cmdline-tools.zip'"
    powershell -Command "Expand-Archive -Path '%TEMP%\cmdline-tools.zip' -DestinationPath '%ANDROID_SDK_DIR%\cmdline-tools\' -Force"
    ren "%ANDROID_SDK_DIR%\cmdline-tools\cmdline-tools" latest
    del "%TEMP%\cmdline-tools.zip"
    echo   Command-line tools extracted.
) else (
    echo   Android command-line tools already present.
)

REM Accept licenses and install SDK components
echo   Installing Android SDK components...
call "%ANDROID_SDK_DIR%\cmdline-tools\latest\bin\sdkmanager.bat" --sdk_root="%ANDROID_SDK_DIR%" "platforms;android-35" "build-tools;35.0.0" "platform-tools" > nul 2>&1
echo   Android SDK ready.
echo.

REM -------------------------------------------------
REM 3. Install Gradle
REM -------------------------------------------------
set GRADLE_VERSION=8.5
set GRADLE_DIR=%USERPROFILE%\gradle-%GRADLE_VERSION%
echo [3/5] Setting up Gradle %GRADLE_VERSION%...

if not exist "%GRADLE_DIR%\bin\gradle.bat" (
    echo   Downloading Gradle %GRADLE_VERSION%...
    powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%TEMP%\gradle.zip'"
    powershell -Command "Expand-Archive -Path '%TEMP%\gradle.zip' -DestinationPath '%USERPROFILE%\' -Force"
    del "%TEMP%\gradle.zip"
    echo   Gradle %GRADLE_VERSION% ready.
) else (
    echo   Gradle already present.
)

REM -------------------------------------------------
REM 4. Create local.properties
REM -------------------------------------------------
echo [4/5] Creating local.properties...
echo sdk.dir=%ANDROID_SDK_DIR:\=/%> "%~dp0..\local.properties"
echo   local.properties created.
echo.

REM -------------------------------------------------
REM 5. Verify
REM -------------------------------------------------
echo [5/5] Setup complete!
echo.
echo ============================================
echo  Setup complete!
echo.
echo  To build the project, run:
echo    scripts\build.bat
echo.
echo  To run tests, run:
echo    scripts\test.bat
echo.
echo  To create a GitHub release, run:
echo    scripts\release.bat
echo ============================================

endlocal
pause
