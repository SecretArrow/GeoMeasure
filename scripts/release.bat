@echo off
REM =============================================================================
REM GeoMeasure Pro - GitHub Release Script (Windows)
REM =============================================================================
REM Creates a GitHub release with signed APK uploads.
REM
REM Usage:
REM   scripts\release.bat v1.0.0 "Release title"
REM
REM Prerequisites:
REM   - gh (GitHub CLI) installed and authenticated (https://cli.github.com/)
REM   - APKs already built via scripts\build.bat
REM
REM Tag format: v1.0.0, v1.0.1, etc.
REM For Linux, use release.sh instead.
REM =============================================================================

setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..
set APK_DIR=%PROJECT_DIR%\app\build\outputs\apk\release

set TAG=%1
set TITLE=%2

if "%TAG%"=="" (
    echo Usage: %0 ^<tag^> [title]
    echo.
    echo Examples:
    echo   %0 v1.0.0
    echo   %0 v1.0.0 "GeoMeasure Pro v1.0.0"
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

REM Find signed APK files (exclude "unsigned" variants)
set APK_COUNT=0
set ASSETS=
for %%f in ("%APK_DIR%\GeoMeasure-Pro-*.apk") do (
    set "FNAME=%%~nxf"
    REM Skip unsigned APKs
    echo !FNAME! | findstr /C:"unsigned" >nul
    if !errorlevel! neq 0 (
        set /a APK_COUNT+=1
        set ASSETS=!ASSETS! "%%f#%%~nxf"
    )
)

if %APK_COUNT% equ 0 (
    echo ERROR: No signed APK files found at %APK_DIR%
    echo Expected files like: GeoMeasure-Pro-arm64-v8a.apk
    echo Run scripts\build.bat first.
    exit /b 1
)

echo  Found %APK_COUNT% signed APK(s) to upload.
echo.

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
