#!/bin/bash

# Credit App Build Script
# This script builds the Spring Boot application

set -e  # Exit on error

echo "🔨 Building Credit App..."

# Clean and compile
echo "📦 Cleaning previous builds..."
mvn clean

echo "🔧 Compiling project..."
mvn compile

echo "✅ Build completed successfully!"

# Optional: Run tests
if [[ "$1" == "--test" ]]; then
    echo "🧪 Running tests..."
    mvn test
fi

# Optional: Create package
if [[ "$1" == "--package" ]]; then
    echo "📦 Creating JAR package..."
    mvn package -DskipTests
    echo "✅ JAR created in target/ directory"
fi