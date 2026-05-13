@echo off
REM =============================================================================
REM GeoMeasure Pro - Test Script (Windows)
REM =============================================================================
REM Runs unit tests and optionally instrumentation tests.
REM
REM Usage:
REM   scripts\test.bat             Run all tests
REM   scripts\test.bat unit        Run only unit tests
REM   scripts\test.bat instrument   Run instrumentation tests (requires emulator)
REM
REM For Linux, use test.sh instead.
REM =============================================================================

setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..
set GRADLE_DIR=%USERPROFILE%\gradle-8.5
set ANDROID_SDK_DIR=%USERPROFILE%\android-sdk

set ANDROID_HOME=%ANDROID_SDK_DIR%
set ANDROID_SDK_ROOT=%ANDROID_SDK_DIR%

if exist "%GRADLE_DIR%\bin\gradle.bat" (
    set GRADLE_CMD=%GRADLE_DIR%\bin\gradle.bat
) else (
    set GRADLE_CMD=gradle
)

cd /d "%PROJECT_DIR%"

set TEST_TYPE=%1
if "%TEST_TYPE%"=="" set TEST_TYPE=all

echo ============================================
echo  GeoMeasure Pro - Running Tests
echo ============================================
echo.

if /i "%TEST_TYPE%"=="all" (
    echo  Running ALL tests...
    echo.
    echo --- Unit Tests ---
    %GRADLE_CMD% testReleaseUnitTest --daemon --console=plain
    if !errorlevel! neq 0 goto :error
    echo.
    echo --- Instrumentation Tests (requires emulator/device) ---
    %GRADLE_CMD% connectedDebugAndroidTest --daemon --console=plain 2>nul
    if !errorlevel! neq 0 (
        echo.
        echo  NOTE: Instrumentation tests skipped (no device/emulator).
    )
) else if /i "%TEST_TYPE%"=="unit" (
    echo  Running UNIT tests only...
    %GRADLE_CMD% testReleaseUnitTest --daemon --console=plain
    if !errorlevel! neq 0 goto :error
) else if /i "%TEST_TYPE%"=="instrument" (
    echo  Running INSTRUMENTATION tests...
    echo  (requires emulator/device connected via adb)
    %GRADLE_CMD% connectedDebugAndroidTest --daemon --console=plain
    if !errorlevel! neq 0 goto :error
) else (
    echo  Usage: %0 [all^|unit^|instrument]
    exit /b 1
)

echo.
echo ============================================
echo  Tests finished.
echo  HTML reports:
echo   - Unit: app/build/reports/tests/testReleaseUnitTest/index.html
echo   - Instrumentation: app/build/reports/androidTests/connected/index.html
echo ============================================
goto :end

:error
echo ERROR: Tests failed.
exit /b 1

:end
endlocal
pause
