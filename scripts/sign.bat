@echo off
setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..
set KEYSTORE=%PROJECT_DIR%\release.keystore
set ENV_FILE=%PROJECT_DIR%\.env
set FORCE=false

if "%1"=="--force" set FORCE=true
if "%2"=="--force" set FORCE=true

echo ============================================
echo  GeoMeasure Pro - Release Signing Setup
echo ============================================
echo.

REM -------------------------------------------------
REM 1. Load or generate credentials
REM -------------------------------------------------
if exist "%ENV_FILE%" (
    echo [1/3] Loading credentials from .env
    for /f "usebackq tokens=1,* delims==" %%a in ("%ENV_FILE%") do (
        if "%%a"=="keystore_path" set KEYSTORE_PATH=%%b
        if "%%a"=="keystore_password" set KEYSTORE_PASS=%%b
        if "%%a"=="key_alias" set KEY_ALIAS=%%b
        if "%%a"=="key_password" set KEY_PASS=%%b
    )
) else (
    echo [1/3] Generating random credentials...

    for /f %%a in ('powershell -Command "[System.Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(12))"') do set STORE_PASS=%%a
    REM PKCS12 keystores require same password for store and key
    set KEY_PASS=!STORE_PASS!

    (
        echo keystore_path=%KEYSTORE%
        echo keystore_password=!STORE_PASS!
        echo key_alias=release
        echo key_password=!STORE_PASS!
    ) > "%ENV_FILE%"

    echo  Credentials written to %ENV_FILE%
    set KEYSTORE_PATH=%KEYSTORE%
    set KEYSTORE_PASS=!STORE_PASS!
    set KEY_ALIAS=release
    set KEY_PASS=!STORE_PASS!
)
echo.

REM -------------------------------------------------
REM 2. Generate keystore if needed
REM -------------------------------------------------
if "%FORCE%"=="true" (
    if exist "%KEYSTORE_PATH%" (
        echo [2/3] --force: removing existing keystore
        del /f /q "%KEYSTORE_PATH%"
    )
)

if not exist "%KEYSTORE_PATH%" (
    echo [2/3] Generating release keystore...

    keytool -genkey -v ^
        -keystore "%KEYSTORE_PATH%" ^
        -alias "%KEY_ALIAS%" ^
        -keyalg RSA -keysize 2048 -validity 10000 ^
        -storepass "%KEYSTORE_PASS%" ^
        -keypass "%KEY_PASS%" ^
        -dname "CN=Developer, OU=Development, O=GeoMeasure, L=Unknown, ST=Unknown, C=US"

    if !errorlevel! neq 0 (
        echo ERROR: Keystore generation failed.
        exit /b 1
    )
    echo  Keystore created: %KEYSTORE_PATH%
) else (
    echo [2/3] Keystore already exists: %KEYSTORE_PATH%
)
echo.

REM -------------------------------------------------
REM 3. Verify
REM -------------------------------------------------
echo [3/3] Verifying keystore...
keytool -list -keystore "%KEYSTORE_PATH%" -storepass "%KEYSTORE_PASS%" -alias "%KEY_ALIAS%" 2>&1 | findstr /C:"Alias name" /C:"Entry type"
echo  OK
echo.

echo ============================================
echo  Signing setup complete!
echo.
echo  Keystore:      %KEYSTORE_PATH%
echo  Key alias:     %KEY_ALIAS%
echo  Config file:   %ENV_FILE%
echo.
echo  Next: scripts\build.bat
echo ============================================

endlocal
