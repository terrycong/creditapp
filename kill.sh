#!/bin/bash

# Credit App Kill Script
# This script stops the running application

set -e  # Exit on error

echo "🛑 Stopping Credit App..."

# Function to kill process on port
kill_port() {
    local port=$1
    local pid=$(lsof -ti:$port)
    
    if [[ -n "$pid" ]]; then
        echo "🔫 Killing process $pid on port $port..."
        kill -9 $pid 2>/dev/null || true
        sleep 2
        
        # Verify process is killed
        if lsof -ti:$port >/dev/null; then
            echo "❌ Failed to kill process on port $port"
            return 1
        else
            echo "✅ Successfully killed process on port $port"
            return 0
        fi
    else
        echo "ℹ️  No process found on port $port"
        return 0
    fi
}

# Default port
PORT=8080

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --port)
            PORT="$2"
            shift 2
            ;;
        --all)
            # Kill all Java processes
            echo "💀 Killing all Java processes..."
            pkill -9 java 2>/dev/null || true
            echo "✅ All Java processes killed"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: ./kill.sh [--port PORT] [--all]"
            exit 1
            ;;
    esac
done

# Kill process on specified port
kill_port $PORT

# Also check for common alternative ports
for alt_port in 8081 8082 8083 8090; do
    if lsof -ti:$alt_port >/dev/null; then
        echo "⚠️  Found process on alternative port $alt_port, killing..."
        kill_port $alt_port
    fi
done

echo "✅ Cleanup complete!"