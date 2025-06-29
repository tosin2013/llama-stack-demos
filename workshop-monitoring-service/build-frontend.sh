#!/bin/bash

# Build Frontend Script for Workshop Monitoring Service
# This script builds the React frontend and copies it to Quarkus static resources

set -e

echo "🚀 Building Workshop Monitoring Dashboard Frontend..."

# Navigate to the webui directory
cd src/main/webui

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "📦 Installing npm dependencies..."
    npm install
fi

# Build the React application
echo "🔨 Building React application..."
npm run build

# Navigate back to project root
cd ../../..

# Create static resources directory if it doesn't exist
mkdir -p src/main/resources/META-INF/resources

# Copy built files to Quarkus static resources
echo "📁 Copying built files to Quarkus static resources..."
cp -r src/main/webui/build/* src/main/resources/META-INF/resources/

echo "✅ Frontend build completed successfully!"
echo ""
echo "📊 Dashboard will be available at: http://localhost:8086"
echo "📚 API Documentation at: http://localhost:8086/q/swagger-ui"
echo "🔍 OpenAPI Spec at: http://localhost:8086/q/openapi"
echo ""
echo "To start the application, run:"
echo "  mvn quarkus:dev"
echo ""
echo "🎯 All 6 workshop agents should be monitored in real-time!"
