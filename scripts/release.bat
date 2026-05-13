@echo off
REM =============================================================================
REM GeoMeasure Pro - GitHub Release Script (Windows)
REM =============================================================================
REM Creates a GitHub release with the built APK files.
REM
REM Usage:
REM   scripts\release.bat v1.0.0 "Release title" "Release notes..."
REM
REM Prerequisites:
REM   - gh (GitHub CLI) installed and authenticated
REM     (https://cli.github.com/)
REM   - APKs already built via scripts\build.bat
REM
REM For Linux, use release.sh instead.
REM =============================================================================

setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..
set APK_DIR=%PROJECT_DIR%\app\build\outputs\apk\release

set TAG=%1
set TITLE=%2
set NOTES=%3

if "%TAG%"=="" (
    echo Usage: %0 ^<tag^> [title] [notes]
    echo.
    echo Examples:
    echo   %0 v1.0.0
    echo   %0 v1.0.0 "GeoMeasure Pro v1.0.0" "Release notes here..."
    exit /b 1
)

if "%TITLE%"=="" set TITLE=GeoMeasure Pro Release

echo ============================================
echo  GeoMeasure Pro - GitHub Release
echo ============================================
echo.

REM Check gh CLI
where gh >nul 2>&1
if !errorlevel! neq 0 (
    echo ERROR: GitHub CLI (gh) not found.
    echo Install from: https://cli.github.com/
    exit /b 1
)

REM Check authentication
gh auth status >nul 2>&1
if !errorlevel! neq 0 (
    echo ERROR: Not authenticated with GitHub.
    echo Run: gh auth login
    exit /b 1
)

echo  Authenticated as:
gh api user --jq .login 2>nul
echo.

REM Check APK files
set APK_COUNT=0
if exist "%APK_DIR%\app-arm64-v8a-release.apk" set /a APK_COUNT+=1
if exist "%APK_DIR%\app-armeabi-v7a-release.apk" set /a APK_COUNT+=1
if exist "%APK_DIR%\app-x86-release.apk" set /a APK_COUNT+=1
if exist "%APK_DIR%\app-x86_64-release.apk" set /a APK_COUNT+=1

if %APK_COUNT% equ 0 (
    echo ERROR: No APK files found at %APK_DIR%
    echo Run scripts\build.bat first.
    exit /b 1
)

echo  Found %APK_COUNT% APK files to upload.
echo.

REM Build asset arguments
set ASSETS=
if exist "%APK_DIR%\app-arm64-v8a-release.apk" set ASSETS=%ASSETS% "%APK_DIR%\app-arm64-v8a-release.apk#GeoMeasure-Pro-arm64-v8a"
if exist "%APK_DIR%\app-armeabi-v7a-release.apk" set ASSETS=%ASSETS% "%APK_DIR%\app-armeabi-v7a-release.apk#GeoMeasure-Pro-armeabi-v7a"
if exist "%APK_DIR%\app-x86-release.apk" set ASSETS=%ASSETS% "%APK_DIR%\app-x86-release.apk#GeoMeasure-Pro-x86"
if exist "%APK_DIR%\app-x86_64-release.apk" set ASSETS=%ASSETS% "%APK_DIR%\app-x86_64-release.apk#GeoMeasure-Pro-x86_64"

REM Create release
echo  Creating release: %TAG%
echo.

gh release create "%TAG%" --title "%TITLE%" %ASSETS%

if !errorlevel! neq 0 (
    echo ERROR: Release creation failed.
    exit /b 1
)

echo.
echo ============================================
echo  Release created successfully!
echo  View at: https://github.com/SecretArrow/GeoMeasure/releases/tag/%TAG%
echo ============================================

endlocal
pause
