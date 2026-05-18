@echo off
setLocal EnableDelayedExpansion

:: ───【CONFIGURATION】Change this to match your app name ───
set "APP_NAME=RainMIDI"
:: ─────────────────────────────────────────────────────────

:: Build the absolute path to the Roaming AppData folder
set "TARGET_DIR=%APPDATA%\%APP_NAME%"

echo ===================================================
echo   User Configuration Initialization Utility
echo ===================================================
echo.
echo The following directory and all its contents will be permanently deleted:
echo "%TARGET_DIR%"
echo.

set /p ANS="Are you sure you want to initialize? (Y/N): "

if /i "%ANS%"=="y" goto :yes
if /i "%ANS%"=="yes" goto :yes

echo.
echo Operation cancelled.
goto :end

:yes
echo.
if exist "%TARGET_DIR%" (
    echo Initializing and clearing data...
    :: /s removes all subdirectories, /q runs it quietly without prompting for confirmations
    rd /s /q "%TARGET_DIR%"
    
    if not exist "%TARGET_DIR%" (
        echo Initialization completed successfully.
    ) else (
        echo [ERROR] Some files could not be deleted.
        echo ^(If the application is currently running, please close it and try again.^)
    )
) else (
    echo Configuration folder not found. It might have already been deleted or never created.
)

rd /s /q plugins
rd /s /q data
del activate

:end
echo.
pause