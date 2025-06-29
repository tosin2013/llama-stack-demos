#!/bin/bash

# Workshop Monitoring Service Deployment Script
# Deploys the monitoring service to OpenShift

set -e

# Configuration
NAMESPACE="${NAMESPACE:-workshop-system}"
ENVIRONMENT="${ENVIRONMENT:-development}"

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
    
    # Check if oc is available and logged in
    if ! command -v oc &> /dev/null; then
        print_error "OpenShift CLI (oc) not found. Please install it."
        exit 1
    fi
    
    # Check if logged into OpenShift
    if ! oc whoami &>/dev/null; then
        print_error "Not logged into OpenShift. Please run 'oc login' first."
        exit 1
    fi
    
    # Check if kustomize is available
    if ! command -v kustomize &> /dev/null; then
        print_warning "Kustomize not found. Using 'oc apply -k' instead."
    fi
    
    print_success "Prerequisites check passed"
}

# Create namespace if it doesn't exist
create_namespace() {
    print_status "Ensuring namespace ${NAMESPACE} exists..."
    
    if oc get namespace ${NAMESPACE} &>/dev/null; then
        print_status "Namespace ${NAMESPACE} already exists"
    else
        print_status "Creating namespace ${NAMESPACE}..."
        oc new-project ${NAMESPACE}
    fi
    
    # Switch to the namespace
    oc project ${NAMESPACE}
    
    print_success "Using namespace: ${NAMESPACE}"
}

# Deploy monitoring service
deploy_monitoring_service() {
    print_status "Deploying Workshop Monitoring Service (${ENVIRONMENT} environment)..."
    
    # Check if overlay exists
    OVERLAY_PATH="overlays/${ENVIRONMENT}"
    if [ ! -d "${OVERLAY_PATH}" ]; then
        print_error "Environment overlay not found: ${OVERLAY_PATH}"
        print_status "Available environments:"
        ls -1 overlays/ 2>/dev/null || echo "  No overlays found"
        exit 1
    fi
    
    # Apply the configuration
    print_status "Applying Kustomize configuration..."
    oc apply -k ${OVERLAY_PATH}/
    
    print_success "Configuration applied successfully"
}

# Wait for deployment
wait_for_deployment() {
    print_status "Waiting for deployment to be ready..."
    
    # Wait for the deployment to be available
    if oc rollout status deployment workshop-monitoring-service -n ${NAMESPACE} --timeout=300s; then
        print_success "Deployment is ready"
    else
        print_error "Deployment failed or timed out"
        print_status "Checking pod status..."
        oc get pods -l app=workshop-monitoring-service -n ${NAMESPACE}
        print_status "Checking events..."
        oc get events --sort-by='.lastTimestamp' -n ${NAMESPACE} | tail -10
        exit 1
    fi
}

# Get service information
get_service_info() {
    print_status "Getting service information..."
    
    # Get route URL
    ROUTE_URL=$(oc get route workshop-monitoring-service -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "route-not-found")
    
    # Get service status
    SERVICE_STATUS=$(oc get service workshop-monitoring-service -n ${NAMESPACE} -o jsonpath='{.metadata.name}' 2>/dev/null || echo "service-not-found")
    
    # Get pod status
    POD_STATUS=$(oc get pods -l app=workshop-monitoring-service -n ${NAMESPACE} -o jsonpath='{.items[0].status.phase}' 2>/dev/null || echo "pod-not-found")
    
    echo ""
    print_success "🎉 Workshop Monitoring Service Deployed Successfully!"
    echo ""
    echo "📊 Service Information:"
    echo "======================="
    echo "Namespace: ${NAMESPACE}"
    echo "Environment: ${ENVIRONMENT}"
    echo "Pod Status: ${POD_STATUS}"
    echo "Service: ${SERVICE_STATUS}"
    echo ""
    echo "🌐 Access URLs:"
    echo "Dashboard: https://${ROUTE_URL}"
    echo "API Documentation: https://${ROUTE_URL}/q/swagger-ui"
    echo "OpenAPI Spec: https://${ROUTE_URL}/q/openapi"
    echo "Health Check: https://${ROUTE_URL}/q/health"
    echo ""
    echo "🔍 Monitoring Capabilities:"
    echo "- Real-time agent health monitoring"
    echo "- Response time tracking"
    echo "- System health aggregation"
    echo "- Interactive dashboard"
    echo ""
    echo "📋 Useful Commands:"
    echo "View logs: oc logs -f deployment/workshop-monitoring-service -n ${NAMESPACE}"
    echo "Scale service: oc scale deployment workshop-monitoring-service --replicas=2 -n ${NAMESPACE}"
    echo "Delete service: oc delete -k ${OVERLAY_PATH}/"
    echo ""
}

# Show pod logs
show_logs() {
    print_status "Showing recent logs..."
    oc logs deployment/workshop-monitoring-service -n ${NAMESPACE} --tail=20
}

# Test service health
test_service() {
    print_status "Testing service health..."
    
    ROUTE_URL=$(oc get route workshop-monitoring-service -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null)
    
    if [ -z "${ROUTE_URL}" ]; then
        print_warning "Route not found, skipping health test"
        return
    fi
    
    # Test health endpoint
    if curl -f -k "https://${ROUTE_URL}/q/health/ready" &>/dev/null; then
        print_success "Service health check passed"
    else
        print_warning "Service health check failed (service may still be starting)"
    fi
}

# Show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --environment ENV  Set deployment environment (default: development)"
    echo "  --namespace NS     Set OpenShift namespace (default: workshop-system)"
    echo "  --logs             Show service logs after deployment"
    echo "  --test             Test service health after deployment"
    echo "  --help             Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  NAMESPACE          OpenShift namespace (default: workshop-system)"
    echo "  ENVIRONMENT        Deployment environment (default: development)"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Deploy to development"
    echo "  $0 --environment production          # Deploy to production"
    echo "  $0 --namespace my-workshop --logs    # Deploy with custom namespace and show logs"
}

# Parse command line arguments
SHOW_LOGS=false
TEST_SERVICE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        --logs)
            SHOW_LOGS=true
            shift
            ;;
        --test)
            TEST_SERVICE=true
            shift
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
    print_status "🚀 Deploying Workshop Monitoring Service"
    print_status "Environment: ${ENVIRONMENT}"
    print_status "Namespace: ${NAMESPACE}"
    echo ""
    
    check_prerequisites
    create_namespace
    deploy_monitoring_service
    wait_for_deployment
    get_service_info
    
    if [ "${SHOW_LOGS}" = "true" ]; then
        echo ""
        show_logs
    fi
    
    if [ "${TEST_SERVICE}" = "true" ]; then
        echo ""
        test_service
    fi
}

# Run main function
main "$@"
