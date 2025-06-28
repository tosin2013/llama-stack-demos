#!/bin/bash

# Workshop Template System Container Build Script
# Builds and pushes containers to Quay registry

set -e

# Configuration
QUAY_REGISTRY="quay.io"
QUAY_ORG="${QUAY_ORG:-your-org}"  # Override with: export QUAY_ORG=your-actual-org
IMAGE_NAME="workshop-system"
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
    
    if ! command -v podman &> /dev/null; then
        print_error "podman is required but not installed"
        exit 1
    fi
    
    if ! command -v oc &> /dev/null; then
        print_error "oc (OpenShift CLI) is required but not installed"
        exit 1
    fi
    
    # Check if logged into Quay
    if ! podman login --get-login ${QUAY_REGISTRY} &> /dev/null; then
        print_warning "Not logged into ${QUAY_REGISTRY}"
        print_status "Please login to Quay:"
        echo "podman login ${QUAY_REGISTRY}"
        read -p "Press Enter after logging in..."
    fi
    
    print_success "Prerequisites check passed"
}

# Create Dockerfile for workshop system
create_dockerfile() {
    print_status "Creating Dockerfile for workshop system..."
    
    cat > Dockerfile << 'EOF'
# Workshop Template System - Multi-Agent Container
FROM registry.access.redhat.com/ubi8/python-39:latest

# Set working directory
WORKDIR /opt/app-root/src

# Install system dependencies
USER root
RUN dnf update -y && \
    dnf install -y git curl wget && \
    dnf clean all

# Switch back to default user
USER 1001

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy agent source code
COPY demos/workshop_template_system/ ./workshop_template_system/
COPY demos/a2a_llama_stack/ ./a2a_llama_stack/

# Copy configuration files
COPY kubernetes/workshop-template-system/config/ ./config/

# Create startup script
COPY start-agent.sh ./
RUN chmod +x start-agent.sh

# Expose agent port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/agent-card || exit 1

# Start the agent
CMD ["./start-agent.sh"]
EOF

    print_success "Dockerfile created"
}

# Create startup script for agents
create_startup_script() {
    print_status "Creating agent startup script..."
    
    cat > start-agent.sh << 'EOF'
#!/bin/bash

# Workshop Template System Agent Startup Script
set -e

# Default values
AGENT_NAME="${AGENT_NAME:-workshop_chat}"
AGENT_PORT="${AGENT_PORT:-8080}"
LLAMA_STACK_ENDPOINT="${LLAMA_STACK_ENDPOINT:-http://llama-stack-server:8321}"
INFERENCE_MODEL_ID="${INFERENCE_MODEL_ID:-meta-llama/Llama-3.2-3B-Instruct}"

echo "Starting Workshop Template System Agent: ${AGENT_NAME}"
echo "Port: ${AGENT_PORT}"
echo "Llama Stack: ${LLAMA_STACK_ENDPOINT}"
echo "Model: ${INFERENCE_MODEL_ID}"

# Set up environment
export PYTHONPATH="/opt/app-root/src:${PYTHONPATH}"

# Start the appropriate agent based on AGENT_NAME
case "${AGENT_NAME}" in
    "workshop_chat")
        echo "Starting Workshop Chat Agent..."
        cd workshop_template_system/agents/workshop_chat
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "template_converter")
        echo "Starting Template Converter Agent..."
        cd workshop_template_system/agents/template_converter
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "content_creator")
        echo "Starting Content Creator Agent..."
        cd workshop_template_system/agents/content_creator
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "source_manager")
        echo "Starting Source Manager Agent..."
        cd workshop_template_system/agents/source_manager
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "research_validation")
        echo "Starting Research Validation Agent..."
        cd workshop_template_system/agents/research_validation
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "documentation_pipeline")
        echo "Starting Documentation Pipeline Agent..."
        cd workshop_template_system/agents/documentation_pipeline
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    *)
        echo "Unknown agent: ${AGENT_NAME}"
        echo "Starting simple HTTP server as fallback..."
        python -m http.server ${AGENT_PORT}
        ;;
esac
EOF

    chmod +x start-agent.sh
    print_success "Startup script created"
}

# Build the container image
build_image() {
    print_status "Building workshop system container image..."
    print_status "Image: ${FULL_IMAGE}"
    
    # Build the image
    podman build -t ${FULL_IMAGE} .
    
    if [ $? -eq 0 ]; then
        print_success "Container image built successfully"
    else
        print_error "Failed to build container image"
        exit 1
    fi
}

# Push image to registry
push_image() {
    print_status "Pushing image to ${QUAY_REGISTRY}..."
    
    podman push ${FULL_IMAGE}
    
    if [ $? -eq 0 ]; then
        print_success "Image pushed successfully to ${FULL_IMAGE}"
    else
        print_error "Failed to push image"
        exit 1
    fi
}

# Update OpenShift deployments
update_deployments() {
    print_status "Updating OpenShift deployments with new image..."
    
    # List of agent deployments to update
    AGENTS=(
        "workshop-chat-agent"
        "template-converter-agent"
        "content-creator-agent"
        "source-manager-agent"
        "research-validation-agent"
        "documentation-pipeline-agent"
    )
    
    for agent in "${AGENTS[@]}"; do
        print_status "Updating ${agent} deployment..."
        oc set image deployment/${agent} ${agent}=${FULL_IMAGE} -n workshop-system
        
        if [ $? -eq 0 ]; then
            print_success "Updated ${agent} deployment"
        else
            print_warning "Failed to update ${agent} deployment"
        fi
    done
    
    print_status "Waiting for deployments to roll out..."
    for agent in "${AGENTS[@]}"; do
        oc rollout status deployment/${agent} -n workshop-system --timeout=300s
    done
}

# Main execution
main() {
    echo "🚀 Workshop Template System Container Build"
    echo "=========================================="
    
    # Check if QUAY_ORG is set
    if [ "${QUAY_ORG}" = "your-org" ]; then
        print_warning "QUAY_ORG is not set!"
        print_status "Please set your Quay organization:"
        read -p "Enter your Quay.io organization name: " user_org
        export QUAY_ORG="${user_org}"
        FULL_IMAGE="${QUAY_REGISTRY}/${QUAY_ORG}/${IMAGE_NAME}:${TAG}"
        print_status "Using image: ${FULL_IMAGE}"
    fi
    
    check_prerequisites
    create_dockerfile
    create_startup_script
    build_image
    
    # Ask before pushing
    echo ""
    print_status "Ready to push image: ${FULL_IMAGE}"
    read -p "Continue with push and deployment update? (y/N): " confirm
    
    if [[ $confirm =~ ^[Yy]$ ]]; then
        push_image
        update_deployments
        
        echo ""
        print_success "🎉 Workshop Template System deployment complete!"
        print_status "Check pod status with: oc get pods -n workshop-system"
        print_status "View logs with: oc logs -l component=workshop-agent -n workshop-system"
    else
        print_status "Build complete. Image available locally: ${FULL_IMAGE}"
        print_status "To push manually: podman push ${FULL_IMAGE}"
    fi
}

# Run main function
main "$@"
