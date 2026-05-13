@echo off
REM =============================================================================
REM GeoMeasure Pro - Combined CI/CD Script (Windows)
REM =============================================================================
REM Runs the full pipeline: install → sign → build → test → release
REM
REM Usage:
REM   scripts\all.bat                          Full pipeline
REM   scripts\all.bat --skip-install           Skip dependency installation
REM   scripts\all.bat --skip-tests             Skip tests
REM   scripts\all.bat --tag v1.0.0             Specify release tag
REM
REM For Linux, use all.sh instead.
REM =============================================================================

setlocal enabledelayedexpansion

set SKIP_INSTALL=false
set SKIP_TESTS=false
set RELEASE_TAG=

REM Parse arguments
:parse
if "%1"=="" goto :parsed
if /i "%1"=="--skip-install" set SKIP_INSTALL=true
if /i "%1"=="--skip-tests" set SKIP_TESTS=true
if /i "%1"=="--tag" (
    shift
    set RELEASE_TAG=%1
)
if "%1"=="" goto :parsed
shift
goto :parse
:parsed

echo ============================================
echo  GeoMeasure Pro - CI/CD Pipeline
echo ============================================
echo  Skip install: %SKIP_INSTALL%
echo  Skip tests:   %SKIP_TESTS%
echo  Release tag:  %RELEASE_TAG:-=none%
echo ============================================
echo.

if /i "%SKIP_INSTALL%"=="false" (
    echo ^>^>^> [1/5] Installing dependencies...
    call scripts\install.bat
    if !errorlevel! neq 0 exit /b !errorlevel!
    echo.
)

echo ^>^>^> [2/5] Setting up signing...
call scripts\sign.bat
if !errorlevel! neq 0 exit /b !errorlevel!
echo.

echo ^>^>^> [3/5] Building release APKs...
call scripts\build.bat
if !errorlevel! neq 0 exit /b !errorlevel!
echo.

if /i "%SKIP_TESTS%"=="false" (
    echo ^>^>^> [4/5] Running unit tests...
    call scripts\test.bat unit
    if !errorlevel! neq 0 exit /b !errorlevel!
    echo.
)

if not "%RELEASE_TAG%"=="" (
    echo ^>^>^> [5/5] Creating GitHub release %RELEASE_TAG%...
    call scripts\release.bat %RELEASE_TAG%
    if !errorlevel! neq 0 exit /b !errorlevel!
    echo.
)

echo ============================================
echo  CI/CD Pipeline complete!
echo ============================================

endlocal
pause
