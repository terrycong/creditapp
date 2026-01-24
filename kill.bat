@echo off
REM Credit App Kill Script for Windows
REM This script stops the running application

echo 🛑 Stopping Credit App...

REM Default port
set PORT=8080

REM Parse arguments
:parse_args
if "%1"=="" goto kill_port
if "%1"=="--port" (
    set PORT=%2
    shift
    shift
    goto parse_args
)
if "%1"=="--all" (
    echo 💀 Killing all Java processes...
    taskkill /F /IM java.exe 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo ✅ All Java processes killed
    ) else (
        echo ℹ️  No Java processes found
    )
    exit /b 0
)

echo Unknown option: %1
echo Usage: kill.bat [--port PORT] [--all]
exit /b 1

:kill_port
REM Find PID using the port
set PID=
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT% ^| findstr LISTENING') do (
    set PID=%%a
)

if defined PID (
    echo 🔫 Killing process %PID% on port %PORT%...
    taskkill /F /PID %PID% 2>nul
    timeout /t 2 /nobreak >nul
    
    REM Verify process is killed
    set PID2=
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT% ^| findstr LISTENING') do (
        set PID2=%%a
    )
    
    if defined PID2 (
        echo ❌ Failed to kill process on port %PORT%
        exit /b 1
    ) else (
        echo ✅ Successfully killed process on port %PORT%
    )
) else (
    echo ℹ️  No process found on port %PORT%
)

REM Also check for common alternative ports
for %%p in (8081 8082 8083 8090) do (
    set ALT_PID=
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%%p ^| findstr LISTENING') do (
        set ALT_PID=%%a
    )
    
    if defined ALT_PID (
        echo ⚠️  Found process on alternative port %%p, killing...
        taskkill /F /PID !ALT_PID! 2>nul
    )
)

echo ✅ Cleanup complete!