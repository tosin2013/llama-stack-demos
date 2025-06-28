#!/bin/bash

# Update Gitea Credentials Script
# This script dynamically retrieves Gitea credentials and updates the Secret

set -e

NAMESPACE="workshop-system"
GITEA_NAMESPACE="gitea"
SECRET_NAME="gitea-credentials"

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

# Function to get Gitea credentials from OpenShift
get_gitea_credentials() {
    print_status "Retrieving Gitea credentials from OpenShift..."
    
    # Get Gitea admin user
    GITEA_ADMIN_USER=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.spec.giteaAdminUser}' 2>/dev/null || echo "")
    if [ -z "$GITEA_ADMIN_USER" ]; then
        print_error "Could not retrieve Gitea admin user"
        exit 1
    fi
    
    # Get Gitea admin password
    GITEA_ADMIN_PASSWORD=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.adminPassword}' 2>/dev/null || echo "")
    if [ -z "$GITEA_ADMIN_PASSWORD" ]; then
        print_error "Could not retrieve Gitea admin password"
        exit 1
    fi
    
    # Get Gitea URL
    GITEA_URL=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.giteaHostname}' 2>/dev/null || echo "")
    if [ -z "$GITEA_URL" ]; then
        print_error "Could not retrieve Gitea URL"
        exit 1
    fi
    
    print_success "Retrieved Gitea credentials:"
    print_status "  User: ${GITEA_ADMIN_USER}"
    print_status "  URL: https://${GITEA_URL}"
    print_status "  Password: [REDACTED]"
}

# Function to update the Secret
update_gitea_secret() {
    print_status "Updating Gitea credentials Secret..."
    
    # Base64 encode the password
    GITEA_PASSWORD_B64=$(echo -n "$GITEA_ADMIN_PASSWORD" | base64 -w 0)
    
    # Check if Secret exists
    if oc get secret ${SECRET_NAME} -n ${NAMESPACE} &>/dev/null; then
        print_status "Secret ${SECRET_NAME} exists, updating..."
        
        # Update existing Secret
        oc patch secret ${SECRET_NAME} -n ${NAMESPACE} --type='merge' -p="{
            \"data\": {
                \"GITEA_PASSWORD\": \"${GITEA_PASSWORD_B64}\"
            }
        }"
    else
        print_warning "Secret ${SECRET_NAME} does not exist, it should be created by Kustomize"
        print_status "Creating Secret with retrieved password..."
        
        # Create new Secret (this should normally be done by Kustomize)
        oc create secret generic ${SECRET_NAME} -n ${NAMESPACE} \
            --from-literal=GITEA_TOKEN="5064d47a5fdb598395a4eb57d3253c394467ca6c" \
            --from-literal=GITEA_PASSWORD="${GITEA_ADMIN_PASSWORD}"
    fi
    
    print_success "Gitea credentials Secret updated successfully"
}

# Function to update ConfigMap with dynamic values
update_gitea_configmap() {
    print_status "Updating Gitea configuration ConfigMap..."
    
    # Update ConfigMap with actual Gitea URL
    oc patch configmap gitea-config -n ${NAMESPACE} --type='merge' -p="{
        \"data\": {
            \"GITEA_URL\": \"https://${GITEA_URL}\",
            \"GITEA_API_URL\": \"https://${GITEA_URL}/api/v1\",
            \"GITEA_USERNAME\": \"${GITEA_ADMIN_USER}\"
        }
    }"
    
    print_success "Gitea configuration ConfigMap updated successfully"
}

# Function to restart Source Manager Agent to pick up new config
restart_source_manager() {
    print_status "Restarting Source Manager Agent to pick up new configuration..."
    
    oc rollout restart deployment/source-manager-agent -n ${NAMESPACE}
    
    print_status "Waiting for Source Manager Agent to be ready..."
    oc rollout status deployment/source-manager-agent -n ${NAMESPACE} --timeout=120s
    
    print_success "Source Manager Agent restarted successfully"
}

# Function to test Gitea connectivity
test_gitea_connectivity() {
    print_status "Testing Gitea connectivity..."
    
    # Test API endpoint
    response=$(curl -k -s -w "%{http_code}" -o /dev/null \
        -H "Authorization: token 5064d47a5fdb598395a4eb57d3253c394467ca6c" \
        "https://${GITEA_URL}/api/v1/user")
    
    if [ "$response" = "200" ]; then
        print_success "Gitea API connectivity test passed"
    else
        print_warning "Gitea API connectivity test failed (HTTP $response)"
        print_status "This may be normal if the token needs to be updated"
    fi
}

# Main execution
main() {
    echo "🔧 Gitea Credentials Update Script"
    echo "=================================="
    echo ""
    
    # Check if we're logged into OpenShift
    if ! oc whoami &>/dev/null; then
        print_error "Not logged into OpenShift. Please run 'oc login' first."
        exit 1
    fi
    
    # Check if namespaces exist
    if ! oc get namespace ${NAMESPACE} &>/dev/null; then
        print_error "Namespace ${NAMESPACE} does not exist"
        exit 1
    fi
    
    if ! oc get namespace ${GITEA_NAMESPACE} &>/dev/null; then
        print_error "Namespace ${GITEA_NAMESPACE} does not exist"
        exit 1
    fi
    
    # Execute steps
    get_gitea_credentials
    update_gitea_secret
    update_gitea_configmap
    restart_source_manager
    test_gitea_connectivity
    
    echo ""
    print_success "🎉 Gitea credentials update completed successfully!"
    echo ""
    print_status "Next steps:"
    print_status "1. Test Source Manager Agent: curl -k https://source-manager-agent-workshop-system.apps.cluster-domain/agent-card"
    print_status "2. Try workshop creation workflow again"
    print_status "3. Monitor agent logs: oc logs -f deployment/source-manager-agent -n ${NAMESPACE}"
}

# Run main function
main "$@"
