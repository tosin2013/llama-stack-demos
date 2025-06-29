#!/bin/bash

# Deploy Workshop Evolution Engine to OpenShift
# This script deploys the complete evolution engine with all components

set -euo pipefail

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
NAMESPACE="workshop-system"
EVOLUTION_OVERLAY="evolution-engine"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if oc is installed and logged in
    if ! command -v oc &> /dev/null; then
        log_error "OpenShift CLI (oc) is not installed"
        exit 1
    fi
    
    # Check if logged into OpenShift
    if ! oc whoami &> /dev/null; then
        log_error "Not logged into OpenShift. Please run 'oc login' first"
        exit 1
    fi
    
    # Check if kustomize is available
    if ! command -v kustomize &> /dev/null; then
        log_warning "kustomize not found, using oc kustomize"
    fi
    
    # Check if kubectl is available
    if ! command -v kubectl &> /dev/null; then
        log_warning "kubectl not found, using oc instead"
        alias kubectl=oc
    fi
    
    log_success "Prerequisites check completed"
}

# Create or verify namespace
setup_namespace() {
    log_info "Setting up namespace: ${NAMESPACE}"
    
    if oc get namespace "${NAMESPACE}" &> /dev/null; then
        log_info "Namespace ${NAMESPACE} already exists"
    else
        log_info "Creating namespace ${NAMESPACE}"
        oc create namespace "${NAMESPACE}"
        oc label namespace "${NAMESPACE}" name="${NAMESPACE}"
    fi
    
    # Set current context to the namespace
    oc project "${NAMESPACE}"
    
    log_success "Namespace setup completed"
}

# Deploy Gitea if not already deployed
deploy_gitea() {
    log_info "Checking Gitea deployment..."
    
    if oc get deployment gitea -n "${NAMESPACE}" &> /dev/null; then
        log_info "Gitea already deployed"
    else
        log_info "Deploying Gitea..."
        if [[ -f "${PROJECT_ROOT}/scripts/deploy-gitea.sh" ]]; then
            bash "${PROJECT_ROOT}/scripts/deploy-gitea.sh"
        else
            log_warning "Gitea deployment script not found, skipping"
        fi
    fi
    
    log_success "Gitea deployment check completed"
}

# Build and push container images
build_images() {
    log_info "Building evolution engine container images..."
    
    # Get cluster registry
    REGISTRY=$(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}' 2>/dev/null || echo "image-registry.openshift-image-registry.svc:5000")
    
    # Build monitoring service with evolution features
    log_info "Building enhanced monitoring service..."
    cd "${PROJECT_ROOT}/workshop-monitoring-service"
    
    # Build with Quarkus
    if [[ -f "mvnw" ]]; then
        ./mvnw clean package -Dquarkus.package.type=uber-jar -DskipTests
    else
        mvn clean package -Dquarkus.package.type=uber-jar -DskipTests
    fi
    
    # Build container image
    oc new-build --binary --name=workshop-monitoring-service-evolution -n "${NAMESPACE}" || true
    oc start-build workshop-monitoring-service-evolution --from-dir=. --follow -n "${NAMESPACE}"
    
    # Tag for evolution
    oc tag "${NAMESPACE}/workshop-monitoring-service-evolution:latest" "${NAMESPACE}/workshop-monitoring-service:evolution-v1.0.0"
    
    cd "${PROJECT_ROOT}"
    
    log_success "Container images built successfully"
}

# Deploy base workshop template system
deploy_base_system() {
    log_info "Deploying base workshop template system..."
    
    # Check if base system is already deployed
    if oc get deployment workshop-monitoring-service -n "${NAMESPACE}" &> /dev/null; then
        log_info "Base system already deployed"
    else
        log_info "Deploying base system..."
        cd "${PROJECT_ROOT}/kubernetes/workshop-template-system/base"
        oc apply -k . -n "${NAMESPACE}"
        cd "${PROJECT_ROOT}"
    fi
    
    log_success "Base system deployment completed"
}

# Deploy evolution engine overlay
deploy_evolution_engine() {
    log_info "Deploying Workshop Evolution Engine..."
    
    cd "${PROJECT_ROOT}/kubernetes/workshop-template-system/overlays/${EVOLUTION_OVERLAY}"
    
    # Validate kustomization
    log_info "Validating kustomization..."
    if command -v kustomize &> /dev/null; then
        kustomize build . > /tmp/evolution-manifests.yaml
    else
        oc kustomize . > /tmp/evolution-manifests.yaml
    fi
    
    # Apply the evolution engine
    log_info "Applying evolution engine manifests..."
    oc apply -k . -n "${NAMESPACE}"
    
    cd "${PROJECT_ROOT}"
    
    log_success "Evolution engine deployed successfully"
}

# Wait for deployments to be ready
wait_for_deployments() {
    log_info "Waiting for deployments to be ready..."
    
    # List of deployments to wait for
    DEPLOYMENTS=(
        "workshop-monitoring-service-evolution"
        "source-manager-agent"
        "human-oversight-coordinator"
        "workshop-chat-agent"
        "template-converter-agent"
        "research-validation-agent"
    )
    
    for deployment in "${DEPLOYMENTS[@]}"; do
        log_info "Waiting for deployment: ${deployment}"
        oc rollout status deployment/"${deployment}" -n "${NAMESPACE}" --timeout=300s || {
            log_warning "Deployment ${deployment} did not become ready within timeout"
        }
    done
    
    log_success "Deployment readiness check completed"
}

# Verify evolution engine functionality
verify_deployment() {
    log_info "Verifying evolution engine deployment..."
    
    # Check if monitoring service is responding
    MONITORING_ROUTE=$(oc get route workshop-evolution-dashboard -n "${NAMESPACE}" -o jsonpath='{.spec.host}' 2>/dev/null || echo "")
    
    if [[ -n "${MONITORING_ROUTE}" ]]; then
        log_info "Evolution dashboard available at: https://${MONITORING_ROUTE}"
        
        # Test API endpoints
        log_info "Testing evolution API endpoints..."
        
        # Test evolution statistics endpoint
        if curl -s -k "https://${MONITORING_ROUTE}/api/evolution/statistics" > /dev/null; then
            log_success "Evolution statistics API is responding"
        else
            log_warning "Evolution statistics API is not responding"
        fi
        
        # Test impact assessment endpoint
        if curl -s -k "https://${MONITORING_ROUTE}/api/impact-assessment/statistics" > /dev/null; then
            log_success "Impact assessment API is responding"
        else
            log_warning "Impact assessment API is not responding"
        fi
    else
        log_warning "Evolution dashboard route not found"
    fi
    
    # Check pod status
    log_info "Checking pod status..."
    oc get pods -n "${NAMESPACE}" -l app.kubernetes.io/name=workshop-evolution-engine
    
    log_success "Deployment verification completed"
}

# Display deployment information
show_deployment_info() {
    log_info "Workshop Evolution Engine Deployment Information"
    echo "=================================================="
    
    # Get route information
    DASHBOARD_ROUTE=$(oc get route workshop-evolution-dashboard -n "${NAMESPACE}" -o jsonpath='{.spec.host}' 2>/dev/null || echo "Not available")
    
    echo "Namespace: ${NAMESPACE}"
    echo "Evolution Dashboard: https://${DASHBOARD_ROUTE}"
    echo ""
    
    echo "Available APIs:"
    echo "- Evolution Tracking: https://${DASHBOARD_ROUTE}/api/evolution"
    echo "- Impact Assessment: https://${DASHBOARD_ROUTE}/api/impact-assessment"
    echo "- Approval Management: https://${DASHBOARD_ROUTE}/api/approvals"
    echo "- System Monitoring: https://${DASHBOARD_ROUTE}/api/monitoring"
    echo ""
    
    echo "Key Features Deployed:"
    echo "✓ Evolution Queue Management"
    echo "✓ Workshop Version History"
    echo "✓ Impact Assessment Service"
    echo "✓ RAG Content Updates"
    echo "✓ Human Oversight Coordination"
    echo "✓ Real-time Evolution Tracking"
    echo ""
    
    echo "Agent Enhancements:"
    echo "✓ Source Manager: Evolution implementation capabilities"
    echo "✓ Human Oversight: Evolution coordination and monitoring"
    echo "✓ Workshop Chat: Dynamic RAG content updates"
    echo "✓ Template Converter: Impact assessment integration"
    echo "✓ Research Validation: Content validation for evolution"
    echo ""
    
    echo "To access the dashboard:"
    echo "1. Open: https://${DASHBOARD_ROUTE}"
    echo "2. Login with your OpenShift credentials"
    echo "3. Navigate to the Evolution tab"
    echo ""
    
    echo "To check deployment status:"
    echo "oc get pods -n ${NAMESPACE}"
    echo "oc logs -f deployment/workshop-monitoring-service-evolution -n ${NAMESPACE}"
}

# Cleanup function
cleanup() {
    if [[ "${1:-}" == "error" ]]; then
        log_error "Deployment failed. Check the logs above for details."
        echo ""
        echo "To troubleshoot:"
        echo "1. Check pod logs: oc logs -f deployment/workshop-monitoring-service-evolution -n ${NAMESPACE}"
        echo "2. Check events: oc get events -n ${NAMESPACE} --sort-by='.lastTimestamp'"
        echo "3. Check pod status: oc get pods -n ${NAMESPACE}"
    fi
}

# Main deployment function
main() {
    log_info "Starting Workshop Evolution Engine deployment..."
    echo "=================================================="
    
    # Set up error handling
    trap 'cleanup error' ERR
    
    # Run deployment steps
    check_prerequisites
    setup_namespace
    deploy_gitea
    build_images
    deploy_base_system
    deploy_evolution_engine
    wait_for_deployments
    verify_deployment
    show_deployment_info
    
    log_success "Workshop Evolution Engine deployment completed successfully!"
}

# Parse command line arguments
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "verify")
        verify_deployment
        ;;
    "info")
        show_deployment_info
        ;;
    "cleanup")
        log_info "Cleaning up evolution engine deployment..."
        oc delete -k "${PROJECT_ROOT}/kubernetes/workshop-template-system/overlays/${EVOLUTION_OVERLAY}" -n "${NAMESPACE}" || true
        log_success "Cleanup completed"
        ;;
    *)
        echo "Usage: $0 [deploy|verify|info|cleanup]"
        echo "  deploy  - Deploy the complete evolution engine (default)"
        echo "  verify  - Verify the deployment"
        echo "  info    - Show deployment information"
        echo "  cleanup - Remove the evolution engine deployment"
        exit 1
        ;;
esac
