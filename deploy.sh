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
EXPORT_DIR="./docker-exports"
EXPORT_COMPRESS=true

# SFTP Configuration for deployment
SFTP_HOST="192.168.9.113"
SFTP_USER="admin"
SFTP_PASS="qswjcg123fn"
SFTP_PATH="/vol1/1000/dockerimages"
SFTP_PORT=22

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
        --export)
            ACTION="export"
            shift
            ;;
        --deploy-sftp)
            ACTION="deploy-sftp"
            shift
            ;;
        --export-dir)
            EXPORT_DIR="$2"
            shift 2
            ;;
        --no-compress)
            EXPORT_COMPRESS=false
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
            echo "  --build-only      Only build the application"
            echo "  --test-only       Only run tests"
            echo "  --docker-only     Only build Docker image"
            echo "  --export          Export Docker image as .tar file"
            echo "  --export-dir DIR  Set export directory (default: ./docker-exports)"
            echo "  --no-compress     Disable gzip compression for export"
            echo "  --deploy-sftp     Build, export, upload to SFTP server, and run"
            echo "  --port PORT       Set application port (default: 8080)"
            echo "  --version VER     Set version (default: 1.0.0)"
            echo "  --help            Show this help message"
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
if [[ "$ACTION" == "all" || "$ACTION" == "docker" || "$ACTION" == "export" ]]; then
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

# 6. Export Docker image as file
if [[ "$ACTION" == "export" ]]; then
    section "Exporting Docker Image"

    # Check if image exists
    if ! docker image inspect $DOCKER_IMAGE &> /dev/null; then
        echo -e "${RED}❌ Docker image not found: $DOCKER_IMAGE${NC}"
        echo "Build the image first with: ./deploy.sh --docker-only"
        exit 1
    fi

    # Create export directory
    mkdir -p "$EXPORT_DIR"

    # Generate filename with timestamp
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    FILENAME="creditapp_${VERSION}_${TIMESTAMP}"

    # Export the image
    echo "Exporting Docker image: $DOCKER_IMAGE"
    echo "Export directory: $EXPORT_DIR"

    EXPORT_PATH="$EXPORT_DIR/${FILENAME}.tar"
    echo "Saving to: $EXPORT_PATH"
    docker save -o "$EXPORT_PATH" $DOCKER_IMAGE
    check_success

    # Compress if enabled
    if [ "$EXPORT_COMPRESS" = true ]; then
        echo "Compressing image with gzip..."
        EXPORT_PATH="$EXPORT_PATH.gz"
        gzip "$EXPORT_DIR/${FILENAME}.tar"
        check_success
    fi

    # Show export details
    FILE_SIZE=$(du -h "$EXPORT_PATH" | cut -f1)
    echo -e "${GREEN}✅ Image exported successfully!${NC}"
    echo "  File: $EXPORT_PATH"
    echo "  Size: $FILE_SIZE"
    echo ""
    echo "To import this image on another machine:"
    echo "  docker load -i $EXPORT_PATH"
fi

# 7. Deploy via SFTP/SSH to remote server
if [[ "$ACTION" == "deploy-sftp" ]]; then
    section "Deploying to Remote Server via SSH/SFTP"

    # Create export directory if not exists
    mkdir -p "$EXPORT_DIR"

    # Generate filename
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    FILENAME="creditapp_${VERSION}_${TIMESTAMP}.tar.gz"
    EXPORT_PATH="$EXPORT_DIR/$FILENAME"

    # Step 1: Export the Docker image
    echo "Step 1: Exporting Docker image..."
    echo "  Source image: $DOCKER_IMAGE"

    # Check if image exists
    if ! docker image inspect $DOCKER_IMAGE &> /dev/null; then
        echo -e "${YELLOW}⚠️  Docker image not found, building first...${NC}"
        if [ -f "Dockerfile" ]; then
            docker build -t $DOCKER_IMAGE .
            check_success
        else
            echo -e "${RED}❌ Dockerfile not found!${NC}"
            exit 1
        fi
    fi

    # Save and compress the image
    echo "  Saving image to: $EXPORT_PATH"
    docker save $DOCKER_IMAGE | gzip > "$EXPORT_PATH"
    check_success

    FILE_SIZE=$(du -h "$EXPORT_PATH" | cut -f1)
    echo -e "${GREEN}✅ Image exported: $FILE_SIZE${NC}"

    # Step 2: Upload to remote server
    echo ""
    echo "Step 2: Uploading to remote server..."
    echo "  Server: $SFTP_HOST:$SFTP_PORT"
    echo "  Remote path: $SFTP_PATH/$FILENAME"
    echo ""

    # Check if sshpass is available
    if command -v sshpass &> /dev/null; then
        echo "  Using sshpass for automated authentication..."

        # Create remote directory via SSH
        sshpass -p "$SFTP_PASS" ssh -o StrictHostKeyChecking=no -p $SFTP_PORT "$SFTP_USER@$SFTP_HOST" "mkdir -p $SFTP_PATH"
        check_success

        # Upload file using SFTP
        echo "  Uploading file..."
        sshpass -p "$SFTP_PASS" sftp -o StrictHostKeyChecking=no -P $SFTP_PORT "$SFTP_USER@$SFTP_HOST" <<EOF
put "$EXPORT_PATH" "$SFTP_PATH/$FILENAME"
bye
EOF
        check_success
    else
        echo "  sshpass not found, using interactive SSH (you will be prompted for password)..."
        echo "  If you want passwordless authentication, setup SSH keys:"
        echo "    ssh-keygen -t rsa"
        echo "    ssh-copy-id -p $SFTP_PORT $SFTP_USER@$SFTP_HOST"
        echo ""

        # Create remote directory via SSH (interactive)
        echo "  Creating remote directory..."
        ssh -p $SFTP_PORT "$SFTP_USER@$SFTP_HOST" "mkdir -p $SFTP_PATH"
        if [ $? -ne 0 ]; then
            echo -e "${RED}❌ Failed to create remote directory${NC}"
            echo "Please check your SSH connection and credentials"
            exit 1
        fi

        # Upload file using SCP (works on both Windows and Linux)
        echo "  Uploading file via SCP..."
        if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
            # Windows with OpenSSH
            scp -P $SFTP_PORT "$EXPORT_PATH" "$SFTP_USER@$SFTP_HOST:$SFTP_PATH/$FILENAME"
        else
            # Linux/Mac
            scp -P $SFTP_PORT "$EXPORT_PATH" "$SFTP_USER@$SFTP_HOST:$SFTP_PATH/$FILENAME"
        fi

        if [ $? -ne 0 ]; then
            echo -e "${RED}❌ Failed to upload file${NC}"
            exit 1
        fi
    fi

    echo -e "${GREEN}✅ File uploaded successfully${NC}"

    # Step 3: Deploy on remote server
    echo ""
    echo "Step 3: Deploying on remote server..."
    echo "  Loading image with sudo..."
    echo ""

    if command -v sshpass &> /dev/null; then
        # Automated deployment with sshpass
        sshpass -p "$SFTP_PASS" ssh -o StrictHostKeyChecking=no -p $SFTP_PORT "$SFTP_USER@$SFTP_HOST" <<EOF
echo "Loading Docker image..."
docker load -i "$SFTP_PATH/$FILENAME"
echo "Tagging image to latest..."
docker tag $DOCKER_IMAGE creditapp:latest 2>/dev/null || true
echo "Stopping existing container..."
docker stop creditapp-container 2>/dev/null || true
docker rm creditapp-container 2>/dev/null || true
echo "Starting new container..."
docker run -d \\
    --name creditapp-container \\
    -p $PORT:$PORT \\
    --restart unless-stopped \\
    creditapp:latest
echo ""
echo "Container status:"
docker ps --filter name=creditapp-container
EOF
    else
        # Interactive deployment
        echo "  You may be prompted for password again..."
        ssh -p $SFTP_PORT "$SFTP_USER@$SFTP_HOST" <<EOF
echo "Loading Docker image..."
docker load -i "$SFTP_PATH/$FILENAME"
echo "Tagging image to latest..."
docker tag $DOCKER_IMAGE creditapp:latest 2>/dev/null || true
echo "Stopping existing container..."
docker stop creditapp-container 2>/dev/null || true
docker rm creditapp-container 2>/dev/null || true
echo "Starting new container..."
docker run -d \\
    --name creditapp-container \\
    -p $PORT:$PORT \\
    --restart unless-stopped \\
    creditapp:latest
echo ""
echo "Container status:"
docker ps --filter name=creditapp-container
EOF
    fi

    if [ $? -eq 0 ]; then
        echo ""
        echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
        echo ""
        echo "Access the application at:"
        echo "  http://$SFTP_HOST:$PORT"
    else
        echo -e "${RED}❌ Deployment failed${NC}"
        exit 1
    fi
fi

# 8. Run application (optional)
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

# Show next steps based on action
if [[ "$ACTION" == "export" ]]; then
    echo "Export complete! The Docker image has been saved to: $EXPORT_DIR"
    echo ""
    echo "To import this image on another machine:"
    echo "  docker load -i <exported-file.tar.gz>"
elif [[ "$ACTION" == "all" ]]; then
    echo "Next steps:"
    echo "1. Run the application: ./run.sh --port $PORT"
    echo "2. Access the app: http://localhost:$PORT"
    echo "3. Login with: parent/parent123 or child/child123"
fi
echo ""