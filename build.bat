@echo off
REM Credit App Build Script for Windows
REM This script builds the Spring Boot application

echo 🔨 Building Credit App...

REM Clean and compile
echo 📦 Cleaning previous builds...
call mvn clean

echo 🔧 Compiling project...
call mvn compile

if %ERRORLEVEL% EQU 0 (
    echo ✅ Build completed successfully!
) else (
    echo ❌ Build failed!
    exit /b 1
)

REM Optional: Run tests
if "%1"=="--test" (
    echo 🧪 Running tests...
    call mvn test
)

REM Optional: Create package
if "%1"=="--package" (
    echo 📦 Creating JAR package...
    call mvn package -DskipTests
    if %ERRORLEVEL% EQU 0 (
        echo ✅ JAR created in target\ directory
    )
)