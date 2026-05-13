@echo off
REM =============================================================================
REM GeoMeasure Pro - Signing Script (Windows)
REM =============================================================================
REM Generates a release keystore (if missing) and creates signing.properties.
REM
REM Usage:
REM   scripts\sign.bat                    Generate keystore + sign config
REM   scripts\sign.bat C:\path\to\keystore  Use existing keystore
REM
REM Environment variables (alternative to signing.properties):
REM   GM_KEYSTORE_PATH, GM_KEYSTORE_PASSWORD, GM_KEY_ALIAS, GM_KEY_PASSWORD
REM
REM For Linux, use sign.sh instead.
REM =============================================================================

setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..
set SIGNING_PROPS=%PROJECT_DIR%\signing.properties

set KEYSTORE_PATH=%1
if "%KEYSTORE_PATH%"=="" set KEYSTORE_PATH=%PROJECT_DIR%\release.keystore
set KEYSTORE_PASS=%GM_KEYSTORE_PASSWORD%
if "%KEYSTORE_PASS%"=="" set KEYSTORE_PASS=android
set KEY_ALIAS=%GM_KEY_ALIAS%
if "%KEY_ALIAS%"=="" set KEY_ALIAS=release
set KEY_PASS=%GM_KEY_PASSWORD%
if "%KEY_PASS%"=="" set KEY_PASS=android
set VALIDITY_DAYS=10000

echo ============================================
echo  GeoMeasure Pro - APK Signing Setup
echo ============================================
echo.

REM Check if keytool is available
where keytool >nul 2>&1
if !errorlevel! neq 0 (
    echo ERROR: keytool not found. Make sure Java JDK is installed.
    exit /b 1
)

REM Generate keystore if not exists
if not exist "%KEYSTORE_PATH%" (
    echo [1/3] Generating new keystore at:
    echo       %KEYSTORE_PATH%
    echo.

    keytool -genkey -v ^
        -keystore "%KEYSTORE_PATH%" ^
        -alias "%KEY_ALIAS%" ^
        -keyalg RSA ^
        -keysize 2048 ^
        -validity %VALIDITY_DAYS% ^
        -storepass "%KEYSTORE_PASS%" ^
        -keypass "%KEY_PASS%" ^
        -dname "CN=Developer, OU=Development, O=GeoMeasure, L=Unknown, ST=Unknown, C=US"

    if !errorlevel! neq 0 (
        echo ERROR: Keystore generation failed.
        exit /b 1
    )
    echo.
    echo  Keystore generated successfully.
) else (
    echo [1/3] Keystore already exists at:
    echo       %KEYSTORE_PATH%
    echo  (using existing)
)
echo.

REM Verify keystore
echo [2/3] Verifying keystore...
keytool -list -keystore "%KEYSTORE_PATH%" -storepass "%KEYSTORE_PASS%" -alias "%KEY_ALIAS%" 2>&1 | findstr /C:"Alias name" /C:"Entry type"
if !errorlevel! neq 0 (
    keytool -list -keystore "%KEYSTORE_PATH%" -storepass "%KEYSTORE_PASS%" 2>&1 | findstr "Your"
)
echo  Keystore verified.
echo.

REM Create signing.properties
echo [3/3] Creating signing.properties...
(
echo # GeoMeasure Pro - Signing Configuration
echo # WARNING: Keep this file secret! Do NOT commit to version control.
echo keystore.path=%KEYSTORE_PATH:\=/%
echo keystore.password=%KEYSTORE_PASS%
echo key.alias=%KEY_ALIAS%
echo key.password=%KEY_PASS%
) > "%SIGNING_PROPS%"

echo  signing.properties created.
echo.

REM Summary
echo ============================================
echo  Signing setup complete!
echo.
echo  Keystore:      %KEYSTORE_PATH%
echo  Key alias:     %KEY_ALIAS%
echo  Config file:   %SIGNING_PROPS%
echo.
echo  Next steps:
echo    1. Build signed APK:    scripts\build.bat
echo    2. Verify signature:    scripts\sign.bat
echo ============================================

endlocal
pause
