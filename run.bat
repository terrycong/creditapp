@echo off
REM Credit App Run Script for Windows
REM This script runs the Spring Boot application

echo 🚀 Starting Credit App...

REM Check if port 8080 is available
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    set PID=%%a
)

if defined PID (
    echo ⚠️  Port 8080 is already in use by PID %PID%!
    echo    You can:
    echo    1. Stop the existing process
    echo    2. Use a different port with: run.bat --port 8081
    echo    3. Kill the process with: kill.bat
    exit /b 1
)

REM Default port
set PORT=8080
set PROFILE_ARG=

REM Parse arguments
:parse_args
if "%1"=="" goto run_app
if "%1"=="--port" (
    set PORT=%2
    shift
    shift
    goto parse_args
)
if "%1"=="--dev" (
    set PROFILE_ARG=-Dspring.profiles.active=dev
    shift
    goto parse_args
)
if "%1"=="--prod" (
    set PROFILE_ARG=-Dspring.profiles.active=prod
    shift
    goto parse_args
)

echo Unknown option: %1
echo Usage: run.bat [--port PORT] [--dev^|--prod]
exit /b 1

:run_app
echo 🌐 Starting application on port %PORT%...
if "%PROFILE_ARG%"=="" (
    echo 📊 Profile: default
) else (
    echo 📊 Profile: %PROFILE_ARG:-Dspring.profiles.active=%
)

REM Run the application
call mvn spring-boot:run %PROFILE_ARG% -Dserver.port=%PORT%