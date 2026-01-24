#!/bin/bash

# Credit App Run Script
# This script runs the Spring Boot application

set -e  # Exit on error

echo "🚀 Starting Credit App..."

# Check if port 8080 is available
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  Port 8080 is already in use!"
    echo "   You can:"
    echo "   1. Stop the existing process"
    echo "   2. Use a different port with: ./run.sh --port 8081"
    echo "   3. Kill the process with: ./kill.sh"
    exit 1
fi

# Default port
PORT=8080

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --port)
            PORT="$2"
            shift 2
            ;;
        --dev)
            PROFILE="dev"
            shift
            ;;
        --prod)
            PROFILE="prod"
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: ./run.sh [--port PORT] [--dev|--prod]"
            exit 1
            ;;
    esac
done

# Build profile argument
PROFILE_ARG=""
if [[ -n "$PROFILE" ]]; then
    PROFILE_ARG="-Dspring.profiles.active=$PROFILE"
fi

echo "🌐 Starting application on port $PORT..."
echo "📊 Profile: ${PROFILE:-default}"

# Run the application
mvn spring-boot:run $PROFILE_ARG -Dserver.port=$PORT