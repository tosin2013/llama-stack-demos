#!/bin/bash

# Workshop Monitoring Service Container Build Script
# Builds and pushes the monitoring service container

set -e

# Configuration
QUAY_REGISTRY="quay.io"
QUAY_ORG="${QUAY_ORG:-your-org}"  # Override with: export QUAY_ORG=your-actual-org
IMAGE_NAME="workshop-monitoring-service"
TAG="${TAG:-latest}"
FULL_IMAGE="${QUAY_REGISTRY}/${QUAY_ORG}/${IMAGE_NAME}:${TAG}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if podman/docker is available
    if command -v podman &> /dev/null; then
        CONTAINER_CMD="podman"
    elif command -v docker &> /dev/null; then
        CONTAINER_CMD="docker"
    else
        print_error "Neither podman nor docker found. Please install one of them."
        exit 1
    fi
    
    # Check if Maven is available
    if ! command -v mvn &> /dev/null; then
        print_error "Maven not found. Please install Maven."
        exit 1
    fi
    
    # Check if Node.js and npm are available
    if ! command -v node &> /dev/null || ! command -v npm &> /dev/null; then
        print_error "Node.js and npm are required. Please install them."
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Build frontend
build_frontend() {
    print_status "Building React frontend..."
    
    if [ ! -f "./build-frontend.sh" ]; then
        print_error "build-frontend.sh not found. Please run this script from the workshop-monitoring-service directory."
        exit 1
    fi
    
    chmod +x ./build-frontend.sh
    ./build-frontend.sh
    
    print_success "Frontend build completed"
}

# Build backend
build_backend() {
    print_status "Building Quarkus backend..."
    
    # Clean and package the application
    mvn clean package -DskipTests
    
    if [ ! -f "target/quarkus-app/quarkus-run.jar" ]; then
        print_error "Quarkus build failed. JAR file not found."
        exit 1
    fi
    
    print_success "Backend build completed"
}

# Build container image
build_container() {
    print_status "Building container image: ${FULL_IMAGE}"

    # Choose Dockerfile based on whether frontend was built separately
    if [ "${SKIP_FRONTEND}" = "true" ] && [ -d "src/main/resources/META-INF/resources" ]; then
        print_status "Using single-stage Dockerfile (frontend already built)"
        DOCKERFILE="src/main/docker/Dockerfile.jvm"
    else
        print_status "Using multi-stage Dockerfile (includes frontend build)"
        DOCKERFILE="src/main/docker/Dockerfile.multistage"
    fi

    # Build the container image
    ${CONTAINER_CMD} build -f ${DOCKERFILE} -t ${FULL_IMAGE} .

    # Also tag as latest for local use
    ${CONTAINER_CMD} tag ${FULL_IMAGE} ${IMAGE_NAME}:latest

    print_success "Container image built successfully"
}

# Push container image
push_container() {
    if [ "${SKIP_PUSH}" = "true" ]; then
        print_warning "Skipping container push (SKIP_PUSH=true)"
        return
    fi
    
    print_status "Pushing container image to registry..."
    
    # Login to Quay if credentials are available
    if [ -n "${QUAY_USERNAME}" ] && [ -n "${QUAY_PASSWORD}" ]; then
        echo "${QUAY_PASSWORD}" | ${CONTAINER_CMD} login ${QUAY_REGISTRY} -u "${QUAY_USERNAME}" --password-stdin
    else
        print_warning "QUAY_USERNAME and QUAY_PASSWORD not set. You may need to login manually."
        print_status "Run: ${CONTAINER_CMD} login ${QUAY_REGISTRY}"
    fi
    
    ${CONTAINER_CMD} push ${FULL_IMAGE}
    
    print_success "Container image pushed successfully"
}

# Test container locally
test_container() {
    print_status "Testing container locally..."
    
    # Stop any existing container
    ${CONTAINER_CMD} stop workshop-monitoring-test 2>/dev/null || true
    ${CONTAINER_CMD} rm workshop-monitoring-test 2>/dev/null || true
    
    # Run container for testing
    print_status "Starting test container..."
    ${CONTAINER_CMD} run -d --name workshop-monitoring-test -p 8087:8086 ${IMAGE_NAME}:latest
    
    # Wait for container to start
    sleep 10
    
    # Test health endpoint
    if curl -f http://localhost:8087/q/health/ready &>/dev/null; then
        print_success "Container health check passed"
    else
        print_error "Container health check failed"
        ${CONTAINER_CMD} logs workshop-monitoring-test
        ${CONTAINER_CMD} stop workshop-monitoring-test
        ${CONTAINER_CMD} rm workshop-monitoring-test
        exit 1
    fi
    
    # Cleanup test container
    ${CONTAINER_CMD} stop workshop-monitoring-test
    ${CONTAINER_CMD} rm workshop-monitoring-test
    
    print_success "Container test completed successfully"
}

# Show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --skip-frontend    Skip frontend build"
    echo "  --skip-backend     Skip backend build"
    echo "  --skip-push        Skip pushing to registry"
    echo "  --skip-test        Skip local container test"
    echo "  --tag TAG          Set image tag (default: latest)"
    echo "  --org ORG          Set Quay organization"
    echo "  --help             Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  QUAY_ORG           Quay organization (default: your-org)"
    echo "  QUAY_USERNAME      Quay username for login"
    echo "  QUAY_PASSWORD      Quay password for login"
    echo "  TAG                Image tag (default: latest)"
    echo "  SKIP_PUSH          Skip push if set to 'true'"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Build and push with defaults"
    echo "  $0 --tag v1.0.0 --org myorg         # Build with custom tag and org"
    echo "  $0 --skip-push                       # Build locally only"
    echo "  QUAY_ORG=myorg TAG=v1.0.0 $0        # Using environment variables"
}

# Parse command line arguments
SKIP_FRONTEND=false
SKIP_BACKEND=false
SKIP_PUSH=false
SKIP_TEST=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-frontend)
            SKIP_FRONTEND=true
            shift
            ;;
        --skip-backend)
            SKIP_BACKEND=true
            shift
            ;;
        --skip-push)
            SKIP_PUSH=true
            shift
            ;;
        --skip-test)
            SKIP_TEST=true
            shift
            ;;
        --tag)
            TAG="$2"
            FULL_IMAGE="${QUAY_REGISTRY}/${QUAY_ORG}/${IMAGE_NAME}:${TAG}"
            shift 2
            ;;
        --org)
            QUAY_ORG="$2"
            FULL_IMAGE="${QUAY_REGISTRY}/${QUAY_ORG}/${IMAGE_NAME}:${TAG}"
            shift 2
            ;;
        --help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Main execution
main() {
    print_status "🚀 Building Workshop Monitoring Service Container"
    print_status "Image: ${FULL_IMAGE}"
    echo ""
    
    check_prerequisites
    
    if [ "${SKIP_FRONTEND}" != "true" ]; then
        build_frontend
    fi
    
    if [ "${SKIP_BACKEND}" != "true" ]; then
        build_backend
    fi
    
    build_container
    
    if [ "${SKIP_TEST}" != "true" ]; then
        test_container
    fi
    
    if [ "${SKIP_PUSH}" != "true" ]; then
        push_container
    fi
    
    echo ""
    print_success "🎉 Workshop Monitoring Service container build completed!"
    print_status "Image: ${FULL_IMAGE}"
    print_status "Local tag: ${IMAGE_NAME}:latest"
    echo ""
    print_status "To deploy to OpenShift:"
    print_status "  oc apply -k ../kubernetes/workshop-monitoring-service/overlays/development/"
    echo ""
}

# Run main function
main "$@"
