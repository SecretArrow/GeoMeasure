@echo off
setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0..
set GRADLE_DIR=%USERPROFILE%\gradle-8.5
set ANDROID_SDK_DIR=%USERPROFILE%\android-sdk

if not exist "%ANDROID_SDK_DIR%" (
    echo ERROR: Android SDK not found at %ANDROID_SDK_DIR%
    echo Run scripts\install.bat first.
    goto :error
)

set ANDROID_HOME=%ANDROID_SDK_DIR%
set ANDROID_SDK_ROOT=%ANDROID_SDK_DIR%

set SIGNED=false
if exist "%PROJECT_DIR%\.env" (
    for /f "usebackq eol=# delims=" %%a in ("%PROJECT_DIR%\.env") do set "%%a"
    set SIGNED=true
)

echo ============================================
echo  GeoMeasure Pro - Building Release APKs
echo ============================================
echo.
echo  Project: %PROJECT_DIR%
echo  SDK:     %ANDROID_SDK_DIR%
echo  Signing: %SIGNED:true=enabled:false=disabled%
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

set APK_DIR=%PROJECT_DIR%\app\build\outputs\apk\release
if exist "%APK_DIR%" (
    if "!SIGNED!"=="false" (
        for %%f in ("%APK_DIR%\app-*-release.apk") do (
            if exist "%%f" (
                set "fname=%%~nf"
                set "arch=!fname:app-=!"
                set "arch=!arch:-release=!"
                rename "%%f" "GeoMeasure-Pro-unsigned-!arch!.apk"
            )
        )
    )

    echo  APK files:
    for %%f in ("%APK_DIR%\*.apk") do (
        echo   - %%~nxf (%%~zf bytes)
    )
    echo.
    if "!SIGNED!"=="true" (echo  Signing status: signed) else (echo  Signing status: unsigned)
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
