@echo off
REM =============================================================================
REM GeoMeasure Pro - Build Script (Windows)
REM =============================================================================
REM Builds release APKs split per ABI (armeabi-v7a, arm64-v8a, x86, x86_64).
REM
REM Usage:
REM   scripts\build.bat
REM
REM Prerequisites: Run scripts\install.bat first.
REM For Linux, use build.sh instead.
REM =============================================================================

setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..
set GRADLE_DIR=%USERPROFILE%\gradle-8.5
set ANDROID_SDK_DIR=%USERPROFILE%\android-sdk

REM Check Android SDK
if not exist "%ANDROID_SDK_DIR%" (
    echo ERROR: Android SDK not found at %ANDROID_SDK_DIR%
    echo Run scripts\install.bat first.
    goto :error
)

set ANDROID_HOME=%ANDROID_SDK_DIR%
set ANDROID_SDK_ROOT=%ANDROID_SDK_DIR%

echo ============================================
echo  GeoMeasure Pro - Building Release APKs
echo ============================================
echo.
echo  Project: %PROJECT_DIR%
echo  SDK:     %ANDROID_SDK_DIR%
echo.

cd /d "%PROJECT_DIR%"

if exist "%GRADLE_DIR%\bin\gradle.bat" (
    "%GRADLE_DIR%\bin\gradle.bat" assembleRelease --daemon --console=plain
) else (
    gradle assembleRelease --daemon --console=plain
)

if !errorlevel! neq 0 (
    echo ERROR: Build failed.
    goto :error
)

echo.
echo ============================================
echo  Build completed!
echo ============================================
echo.

REM List APK outputs
set APK_DIR=%PROJECT_DIR%\app\build\outputs\apk\release
if exist "%APK_DIR%" (
    echo  APK files:
    for %%f in ("%APK_DIR%\*.apk") do (
        for /f "usebackq" %%s in ('powershell -Command "(Get-Item '%%f').Length / 1MB"') do set SIZE=%%s
        echo   - %%~nxf (%~zf bytes)
    )
) else (
    echo  ERROR: No APK outputs found.
    goto :error
)
echo.

goto :end

:error
exit /b 1

:end
endlocal
pause
