@echo off
REM ===================================================================
REM Credit App Kill Script for Windows
REM Kills processes on port 8080 or specified port
REM ===================================================================我的积分钱包
REM Usage:
REM   kill.bat              - Kill process on default port 8080
REM   kill.bat --port 8080  - Kill process on specific port
REM   kill.bat --all        - Kill all Java processes
REM   kill.bat --help       - Show this help message
REM ===================================================================

setlocal

REM Default port
set PORT=8080
set KILLED=false

REM Check for --all flag
if "%~1"=="--all" goto kill_java
if "%~1"=="--java" goto kill_java
if "%~1"=="--help" goto help
if "%~1"=="-h" goto help

REM Check for --port flag
if "%~1"=="--port" set PORT=%~2
if "%~1"=="-p" set PORT=%~2

:main
echo [INFO] Searching for process on port %PORT%...

REM Create temp file to track killed PIDs
set "TEMP_FILE=%TEMP%\kill_pids_%RANDOM%.tmp"

REM Find and kill unique PIDs on the port
for /f "tokens=5" %%a in ('netstat -ano ^| findstr /C":%PORT%" ^| findstr /C:"LISTENING"') do (
    set "PID=%%a"
    
    REM Check if already killed this PID
    findstr /C:"!PID!" "%TEMP_FILE%" >nul 2>&1
    if errorlevel 1 (
        echo [INFO] Found process !PID! on port %PORT%
        
        REM Get process name
        for /f "tokens=1" %%n in ('tasklist /FI "PID eq !PID!" /NH 2^>nul') do (
            echo [INFO] Process name: %%n
        )
        
        echo [INFO] Killing process !PID!...
        taskkill /F /PID !PID! 2>&1
        
        if errorlevel 0 (
            echo [SUCCESS] Process !PID! killed
            set KILLED=true
        ) else (
            echo [ERROR] Failed to kill process !PID!
        )
        
        REM Mark PID as killed
        echo !PID! >> "%TEMP_FILE%"
    )
)

REM Cleanup temp file
if exist "%TEMP_FILE%" del "%TEMP_FILE%"

if "%KILLED%"=="false" (
    echo [INFO] No process found on port %PORT%
) else (
    echo [SUCCESS] Port %PORT% cleanup complete
)

goto check_status

:kill_java
echo [INFO] Killing all Java processes...
taskkill /F /IM java.exe 2>&1
echo [SUCCESS] Java processes cleanup complete
goto end

:check_status
echo.
echo [INFO] Current port status:
netstat -ano ^| findstr /C":%PORT%" ^| findstr /C:"LISTENING" >nul 2>&1
if errorlevel 1 (
    echo [SUCCESS] Port %PORT% is now free
) else (
    echo [WARNING] Port %PORT% is still in use
    netstat -ano ^| findstr /C":%PORT%" ^| findstr /C:"LISTENING"
)

goto end

:help
echo Credit App Kill Script for Windows
echo.
echo Usage: kill.bat [OPTIONS]
echo.
echo Options:
echo   --port PORT, -p PORT  Kill process on specific port (default: 8080)
echo   --all, --java         Kill all Java processes
echo   --help, -h            Show this help message
echo.
echo Examples:
echo   kill.bat              Kill process on port 8080
echo   kill.bat --port 8081  Kill process on port 8081
echo   kill.bat --all        Kill all Java processes
goto end

:end
endlocal
