#!/bin/bash

# Deploy Workshop Template System to OpenShift
# This script deploys both the 6-agent system and the two workshops

set -e

echo "🚀 Deploying Workshop Template System to OpenShift"
echo "=================================================="

# Configuration
NAMESPACE="workshop-system"
REGISTRY="quay.io/your-org"  # Update with your registry
IMAGE_TAG="latest"

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
    
    # Check if oc is installed
    if ! command -v oc &> /dev/null; then
        print_error "OpenShift CLI (oc) is not installed"
        exit 1
    fi
    
    # Check if logged into OpenShift
    if ! oc whoami &> /dev/null; then
        print_error "Not logged into OpenShift. Please run 'oc login'"
        exit 1
    fi
    
    # Check if podman/docker is available for building
    if ! command -v podman &> /dev/null && ! command -v docker &> /dev/null; then
        print_error "Neither podman nor docker is available for building images"
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Build container images
build_images() {
    print_status "Building container images..."
    
    # Create Dockerfile for workshop system
    cat > Dockerfile << 'EOF'
FROM registry.access.redhat.com/ubi8/python-39:latest

USER root

# Install system dependencies
RUN dnf update -y && \
    dnf install -y git curl && \
    dnf clean all

USER 1001

# Copy application code
COPY --chown=1001:0 . /opt/app-root/src/
WORKDIR /opt/app-root/src

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Expose agent port
EXPOSE 8080

# Start agent based on environment variable
CMD ["python", "-m", "demos.workshop_template_system"]
EOF

    # Build image
    if command -v podman &> /dev/null; then
        BUILD_CMD="podman"
    else
        BUILD_CMD="docker"
    fi
    
    print_status "Building workshop-system image..."
    $BUILD_CMD build -t ${REGISTRY}/workshop-system:${IMAGE_TAG} .
    
    print_status "Pushing image to registry..."
    $BUILD_CMD push ${REGISTRY}/workshop-system:${IMAGE_TAG}
    
    print_success "Images built and pushed successfully"
}

# Create namespace and basic resources
create_namespace() {
    print_status "Creating namespace and basic resources..."
    
    # Create namespace
    cat << EOF | oc apply -f -
apiVersion: v1
kind: Namespace
metadata:
  name: ${NAMESPACE}
  labels:
    name: ${NAMESPACE}
    app: workshop-template-system
EOF

    # Create service account
    cat << EOF | oc apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: workshop-system-sa
  namespace: ${NAMESPACE}
EOF

    # Create role and role binding
    cat << EOF | oc apply -f -
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: workshop-system-role
  namespace: ${NAMESPACE}
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets", "services", "pods"]
  verbs: ["get", "list", "watch", "create", "update", "patch"]
- apiGroups: ["apps"]
  resources: ["deployments", "replicasets"]
  verbs: ["get", "list", "watch", "create", "update", "patch"]
- apiGroups: ["route.openshift.io"]
  resources: ["routes"]
  verbs: ["get", "list", "watch", "create", "update", "patch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: workshop-system-rolebinding
  namespace: ${NAMESPACE}
subjects:
- kind: ServiceAccount
  name: workshop-system-sa
  namespace: ${NAMESPACE}
roleRef:
  kind: Role
  name: workshop-system-role
  apiGroup: rbac.authorization.k8s.io
EOF

    print_success "Namespace and basic resources created"
}

# Deploy secrets
deploy_secrets() {
    print_status "Deploying secrets..."
    
    # Prompt for secret values
    read -p "Enter Pinecone API Key (or press Enter to skip): " PINECONE_KEY
    read -p "Enter GitHub Token (or press Enter to skip): " GITHUB_TOKEN
    read -p "Enter OpenAI API Key (or press Enter to skip): " OPENAI_KEY
    
    # Create secrets
    cat << EOF | oc apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: workshop-system-secrets
  namespace: ${NAMESPACE}
type: Opaque
data:
  pinecone-api-key: $(echo -n "${PINECONE_KEY:-default-key}" | base64 -w 0)
  github-token: $(echo -n "${GITHUB_TOKEN:-default-token}" | base64 -w 0)
  openai-api-key: $(echo -n "${OPENAI_KEY:-default-key}" | base64 -w 0)
EOF

    print_success "Secrets deployed"
}

# Deploy Llama Stack server
deploy_llama_stack() {
    print_status "Deploying Llama Stack server..."
    
    # Update image reference in deployment file
    sed "s|workshop-system:latest|${REGISTRY}/workshop-system:${IMAGE_TAG}|g" \
        openshift-deployment/llama-stack-deployment.yaml | oc apply -f -
    
    # Wait for Llama Stack to be ready
    print_status "Waiting for Llama Stack server to be ready..."
    oc wait --for=condition=available --timeout=300s deployment/llama-stack-server -n ${NAMESPACE}
    
    print_success "Llama Stack server deployed and ready"
}

# Deploy workshop agents
deploy_agents() {
    print_status "Deploying workshop agents..."
    
    # Deploy remaining agents (Content Creator, Source Manager, etc.)
    for agent in content-creator source-manager documentation-pipeline research-validation; do
        print_status "Deploying ${agent} agent..."
        
        cat << EOF | oc apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${agent}-agent
  namespace: ${NAMESPACE}
  labels:
    app: ${agent}-agent
    component: workshop-agent
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${agent}-agent
  template:
    metadata:
      labels:
        app: ${agent}-agent
        component: workshop-agent
    spec:
      serviceAccountName: workshop-system-sa
      containers:
      - name: ${agent}-agent
        image: ${REGISTRY}/workshop-system:${IMAGE_TAG}
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: AGENT_NAME
          value: "${agent//-/_}"
        - name: AGENT_PORT
          value: "8080"
        - name: LLAMA_STACK_ENDPOINT
          value: "http://llama-stack-server:8321"
        - name: INFERENCE_MODEL_ID
          value: "meta-llama/Llama-3.2-3B-Instruct"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1"
        livenessProbe:
          httpGet:
            path: /agent-card
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /agent-card
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: ${agent}-agent
  namespace: ${NAMESPACE}
  labels:
    app: ${agent}-agent
spec:
  selector:
    app: ${agent}-agent
  ports:
  - port: 80
    targetPort: 8080
    name: http
  type: ClusterIP
EOF
    done
    
    # Wait for all agents to be ready
    print_status "Waiting for all agents to be ready..."
    for agent in workshop-chat template-converter content-creator source-manager documentation-pipeline research-validation; do
        oc wait --for=condition=available --timeout=300s deployment/${agent}-agent -n ${NAMESPACE}
    done
    
    print_success "All workshop agents deployed and ready"
}

# Deploy workshops
deploy_workshops() {
    print_status "Deploying workshops..."
    
    # Apply workshop deployments
    oc apply -f openshift-deployment/workshop-deployments.yaml
    
    # Wait for workshops to be ready
    print_status "Waiting for workshops to be ready..."
    oc wait --for=condition=available --timeout=300s deployment/healthcare-ml-workshop -n ${NAMESPACE}
    oc wait --for=condition=available --timeout=300s deployment/openshift-baremetal-workshop -n ${NAMESPACE}
    
    print_success "Workshops deployed and ready"
}

# Display deployment information
show_deployment_info() {
    print_success "🎉 Workshop Template System Deployment Complete!"
    echo ""
    echo "📊 Deployment Summary:"
    echo "======================"
    
    # Show pod status
    echo ""
    echo "🔧 System Components:"
    oc get pods -n ${NAMESPACE} -o wide
    
    echo ""
    echo "🌐 Workshop URLs:"
    echo "Healthcare ML Workshop: https://$(oc get route healthcare-ml-workshop -n ${NAMESPACE} -o jsonpath='{.spec.host}')"
    echo "OpenShift Bare Metal Workshop: https://$(oc get route openshift-baremetal-workshop -n ${NAMESPACE} -o jsonpath='{.spec.host}')"
    
    echo ""
    echo "🤖 Agent Endpoints:"
    for agent in workshop-chat template-converter content-creator source-manager documentation-pipeline research-validation; do
        echo "${agent}: http://${agent}-agent.${NAMESPACE}.svc.cluster.local"
    done
    
    echo ""
    echo "🎯 Next Steps:"
    echo "1. Access workshops via the URLs above"
    echo "2. Test Workshop Chat Agent integration"
    echo "3. Interact with agents to update workshop content"
    echo "4. Monitor agent coordination and live updates"
    
    echo ""
    print_success "Your complete workshop system is now running in OpenShift!"
}

# Main deployment flow
main() {
    echo "Starting Workshop Template System deployment..."
    echo ""
    
    check_prerequisites
    
    # Prompt for confirmation
    echo ""
    read -p "Deploy to namespace '${NAMESPACE}'? (y/N): " confirm
    if [[ ! $confirm =~ ^[Yy]$ ]]; then
        echo "Deployment cancelled"
        exit 0
    fi
    
    # Execute deployment steps
    build_images
    create_namespace
    deploy_secrets
    deploy_llama_stack
    deploy_agents
    deploy_workshops
    show_deployment_info
}

# Run main function
main "$@"
