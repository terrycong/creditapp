@echo off
echo ================================================
echo Credit App Docker Management Script
echo ================================================
echo.

if "%1"=="start" (
    echo Starting all services...
    docker-compose up -d
    echo.
    echo Waiting for services to start...
    timeout /t 10 /nobreak
    echo.
    echo Services status:
    docker-compose ps
    echo.
    echo Application: http://localhost:8080
    echo phpMyAdmin: http://localhost:8081
    echo.
    echo Login credentials:
    echo   Parent: parent / parent123
    echo   Child: child / child123
    goto :end
)

if "%1"=="stop" (
    echo Stopping all services...
    docker-compose down
    goto :end
)

if "%1"=="restart" (
    echo Restarting all services...
    docker-compose restart
    goto :end
)

if "%1"=="logs" (
    docker-compose logs -f
    goto :end
)

if "%1"=="rebuild" (
    echo Rebuilding Docker image...
    docker build -t creditapp:latest .
    echo.
    echo Restarting services...
    docker-compose down
    docker-compose up -d
    goto :end
)

if "%1"=="clean" (
    echo WARNING: This will remove all data!
    set /p confirm="Continue? (y/n): "
    if /i "%confirm%"=="y" (
        docker-compose down -v
        echo All data removed.
    )
    goto :end
)

echo Usage: docker.bat [command]
echo.
echo Commands:
echo   start   - Start all services
echo   stop    - Stop all services
echo   restart - Restart all services
echo   logs    - View logs
echo   rebuild - Rebuild and restart
echo   clean   - Remove all data and containers
echo.

:end
