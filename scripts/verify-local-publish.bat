@echo off
echo ================================================
echo  RadialMenu - Local Publish Verification
echo ================================================
echo.

echo Step 1: Publishing to mavenLocal...
call gradlew.bat :radialmenu:publishToMavenLocal
if %ERRORLEVEL% neq 0 (
    echo FAILED: publishToMavenLocal failed.
    exit /b 1
)

echo.
echo Step 2: Checking for artifacts...
set ARTIFACT_PATH=%USERPROFILE%\.m2\repository\io\github\gawwr4v\radialmenu-android\1.0.0

if exist "%ARTIFACT_PATH%" (
    echo Found artifact directory: %ARTIFACT_PATH%
    dir "%ARTIFACT_PATH%"
) else (
    echo FAILED: Artifact directory not found at %ARTIFACT_PATH%
    exit /b 1
)

echo.
echo Checking for .asc signature files...
dir "%ARTIFACT_PATH%\*.asc" >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo SUCCESS: GPG signature files found!
) else (
    echo FAILED: No .asc signature files found.
    echo Check your gradle.properties signing configuration.
    exit /b 1
)

echo.
echo ================================================
echo  All checks passed! Safe to publish to CI.
echo ================================================
