#!/bin/bash

# Credit App Deployment Script
# Complete deployment pipeline

set -e  # Exit on error

echo "🚀 Starting Credit App Deployment Pipeline..."
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="credit-app"
VERSION="1.0.0"
DOCKER_IMAGE="creditapp:latest"
DOCKER_REGISTRY=""  # Set your registry here if needed
PORT=8080

# Parse arguments
ACTION="all"
while [[ $# -gt 0 ]]; do
    case $1 in
        --build-only)
            ACTION="build"
            shift
            ;;
        --test-only)
            ACTION="test"
            shift
            ;;
        --docker-only)
            ACTION="docker"
            shift
            ;;
        --port)
            PORT="$2"
            shift 2
            ;;
        --version)
            VERSION="$2"
            shift 2
            ;;
        --help)
            echo "Usage: ./deploy.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --build-only    Only build the application"
            echo "  --test-only     Only run tests"
            echo "  --docker-only   Only build Docker image"
            echo "  --port PORT     Set application port (default: 8080)"
            echo "  --version VER   Set version (default: 1.0.0)"
            echo "  --help          Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Function to print section headers
section() {
    echo ""
    echo -e "${BLUE}=== $1 ===${NC}"
    echo ""
}

# Function to check command success
check_success() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Success${NC}"
    else
        echo -e "${RED}❌ Failed${NC}"
        exit 1
    fi
}

# 1. Stop existing application
if [[ "$ACTION" == "all" || "$ACTION" == "docker" ]]; then
    section "Stopping Existing Application"
    ./kill.sh --port $PORT
    check_success
fi

# 2. Clean and build
if [[ "$ACTION" == "all" || "$ACTION" == "build" ]]; then
    section "Building Application"
    echo "Cleaning previous builds..."
    mvn clean
    check_success
    
    echo "Compiling project..."
    mvn compile
    check_success
fi

# 3. Run tests
if [[ "$ACTION" == "all" || "$ACTION" == "test" ]]; then
    section "Running Tests"
    echo "Executing unit tests..."
    mvn test
    check_success
fi

# 4. Create JAR package
if [[ "$ACTION" == "all" || "$ACTION" == "build" ]]; then
    section "Creating JAR Package"
    echo "Packaging application..."
    mvn package -DskipTests
    check_success
    
    # Verify JAR exists
    if [ -f "target/credit-app-$VERSION.jar" ]; then
        echo -e "${GREEN}✅ JAR created: target/credit-app-$VERSION.jar${NC}"
    else
        echo -e "${RED}❌ JAR not found!${NC}"
        exit 1
    fi
fi

# 5. Docker build (if Dockerfile exists)
if [[ "$ACTION" == "all" || "$ACTION" == "docker" ]]; then
    if [ -f "Dockerfile" ]; then
        section "Building Docker Image"
        echo "Building Docker image: $DOCKER_IMAGE"
        docker build -t $DOCKER_IMAGE .
        check_success
        
        # Tag with version if provided
        if [ "$VERSION" != "latest" ]; then
            echo "Tagging image: $DOCKER_IMAGE -> creditapp:$VERSION"
            docker tag $DOCKER_IMAGE creditapp:$VERSION
            check_success
        fi
        
        # Push to registry if configured
        if [ -n "$DOCKER_REGISTRY" ]; then
            section "Pushing to Docker Registry"
            echo "Pushing to: $DOCKER_REGISTRY/$DOCKER_IMAGE"
            docker push $DOCKER_REGISTRY/$DOCKER_IMAGE
            check_success
        fi
    else
        echo -e "${YELLOW}⚠️  Dockerfile not found, skipping Docker build${NC}"
    fi
fi

# 6. Run application (optional)
if [[ "$ACTION" == "all" ]]; then
    section "Starting Application"
    echo "Application will start on port $PORT"
    echo "To run the application, use: ./run.sh --port $PORT"
    echo "Or with Docker: docker run -p $PORT:8080 $DOCKER_IMAGE"
fi

echo ""
echo -e "${GREEN}==========================================${NC}"
echo -e "${GREEN}🚀 Deployment Pipeline Completed Successfully!${NC}"
echo -e "${GREEN}==========================================${NC}"
echo ""
echo "Next steps:"
echo "1. Run the application: ./run.sh --port $PORT"
echo "2. Access the app: http://localhost:$PORT"
echo "3. Login with: parent/parent123 or child/child123"
echo ""