#!/bin/bash

# Dynamic Pipeline Execution Script
# ADR-0036: Pipeline Parameter and Validation Type Standards
# 
# This script calls the middleware to get valid configuration values,
# then builds and executes the tkn command with correct parameters.

set -e

# Configuration
MIDDLEWARE_URL="https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
NAMESPACE="workshop-system"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }

# Function to get pipeline configuration from middleware
get_pipeline_config() {
    print_info "Fetching pipeline configuration from middleware..."
    
    local config_response=$(curl -s -k "${MIDDLEWARE_URL}/api/pipeline/config" 2>/dev/null)
    
    if [ $? -ne 0 ] || [ -z "$config_response" ]; then
        print_error "Failed to fetch configuration from middleware"
        print_info "Falling back to default values..."
        return 1
    fi
    
    echo "$config_response"
    return 0
}

# Function to validate parameters using middleware
validate_parameters() {
    local repo_url="$1"
    local workshop_name="$2"
    local validation_type="$3"
    
    print_info "Validating parameters with middleware..."
    
    local validation_payload=$(cat <<EOF
{
    "repository-url": "$repo_url",
    "workshop-name": "$workshop_name",
    "validation-type": "$validation_type"
}
EOF
)
    
    local validation_response=$(curl -s -k -X POST \
        -H "Content-Type: application/json" \
        -d "$validation_payload" \
        "${MIDDLEWARE_URL}/api/pipeline/validate-parameters" 2>/dev/null)
    
    if [ $? -ne 0 ]; then
        print_warning "Could not validate parameters with middleware"
        return 1
    fi
    
    local is_valid=$(echo "$validation_response" | jq -r '.data.valid // false' 2>/dev/null)
    
    if [ "$is_valid" = "true" ]; then
        print_success "Parameters validated successfully"
        return 0
    else
        print_error "Parameter validation failed:"
        echo "$validation_response" | jq -r '.data.errors[]?' 2>/dev/null || echo "Unknown validation error"
        return 1
    fi
}

# Function to get correct workspace PVC name
get_workspace_pvc() {
    local config="$1"
    
    # Extract PVC name from config
    local pvc_name=$(echo "$config" | jq -r '.data.workspaces."shared-data".pvc_name // "workshop-shared-pvc"' 2>/dev/null)
    
    if [ -z "$pvc_name" ] || [ "$pvc_name" = "null" ]; then
        # Fallback: check what PVCs actually exist
        print_info "Checking available PVCs..."
        local available_pvcs=$(oc get pvc -n "$NAMESPACE" -o name 2>/dev/null | grep -E "(shared|workshop)" | head -1)
        
        if [ -n "$available_pvcs" ]; then
            pvc_name=$(echo "$available_pvcs" | sed 's|persistentvolumeclaim/||')
            print_info "Found PVC: $pvc_name"
        else
            pvc_name="workshop-shared-pvc"
            print_warning "Using default PVC name: $pvc_name"
        fi
    fi
    
    echo "$pvc_name"
}

# Function to get supported validation types for an agent
get_validation_types() {
    local config="$1"
    local agent="$2"
    
    echo "$config" | jq -r ".data.validation_types.\"$agent\".supported_types[]?" 2>/dev/null
}

# Function to run pipeline with dynamic configuration
run_pipeline() {
    local pipeline_name="$1"
    local repo_url="$2"
    local workshop_name="$3"
    local additional_params="$4"
    
    print_info "Starting dynamic pipeline execution..."
    print_info "Pipeline: $pipeline_name"
    print_info "Repository: $repo_url"
    print_info "Workshop: $workshop_name"
    
    # Get configuration from middleware
    local config=$(get_pipeline_config)
    if [ $? -ne 0 ]; then
        print_error "Cannot proceed without middleware configuration"
        exit 1
    fi
    
    print_success "Configuration retrieved from middleware"
    
    # Get correct workspace PVC
    local pvc_name=$(get_workspace_pvc "$config")
    print_info "Using workspace PVC: $pvc_name"
    
    # Get supported validation types
    print_info "Supported validation types:"
    get_validation_types "$config" "research-validation" | while read -r type; do
        echo "  - $type"
    done
    
    # Determine correct validation type based on pipeline
    local validation_type="new-workshop-validation"
    if [[ "$pipeline_name" == *"enhance"* ]]; then
        validation_type="enhancement-analysis"
    fi
    
    print_info "Using validation type: $validation_type"
    
    # Validate parameters
    if ! validate_parameters "$repo_url" "$workshop_name" "$validation_type"; then
        print_error "Parameter validation failed. Aborting pipeline execution."
        exit 1
    fi
    
    # Build tkn command dynamically
    local tkn_cmd="tkn pipeline start $pipeline_name -n $NAMESPACE"
    tkn_cmd="$tkn_cmd --param repository-url=$repo_url"
    tkn_cmd="$tkn_cmd --param workshop-name=$workshop_name"
    tkn_cmd="$tkn_cmd --workspace name=shared-data,claimName=$pvc_name"
    
    # Add pipeline-specific parameters
    case "$pipeline_name" in
        "workflow-1-intelligent-workshop")
            tkn_cmd="$tkn_cmd --param auto-detect-workflow=true"
            tkn_cmd="$tkn_cmd --param human-approver=system-operator"
            tkn_cmd="$tkn_cmd --param auto-approve=true"
            tkn_cmd="$tkn_cmd --workspace name=gitea-auth,secret=gitea-auth-secret"
            ;;
        "workflow-1-simple-corrected")
            tkn_cmd="$tkn_cmd --param base-template=showroom_template_default"
            ;;
        "workflow-3-enhance-workshop")
            if [ -n "$additional_params" ]; then
                tkn_cmd="$tkn_cmd $additional_params"
            fi
            tkn_cmd="$tkn_cmd --workspace name=gitea-auth,secret=gitea-auth-secret"
            ;;
    esac
    
    print_info "Executing command:"
    echo "$tkn_cmd"
    echo
    
    # Execute the pipeline
    print_info "Starting pipeline execution..."
    eval "$tkn_cmd"
    
    if [ $? -eq 0 ]; then
        print_success "Pipeline started successfully!"
        print_info "Monitor progress with: tkn pipelinerun logs -f -n $NAMESPACE"
    else
        print_error "Failed to start pipeline"
        exit 1
    fi
}

# Main script logic
main() {
    if [ $# -lt 3 ]; then
        echo "Usage: $0 <pipeline-name> <repository-url> <workshop-name> [additional-params]"
        echo
        echo "Examples:"
        echo "  $0 workflow-1-simple-corrected https://github.com/jeremyrdavis/dddhexagonalworkshop.git test-workshop"
        echo "  $0 workflow-1-intelligent-workshop https://github.com/tosin2013/ansible-controller-cac.git ansible-workshop"
        echo "  $0 workflow-3-enhance-workshop https://github.com/user/repo.git enhanced-workshop '--param original-workshop-url=https://github.com/original/repo.git'"
        echo
        echo "Available pipelines:"
        echo "  - workflow-1-simple-corrected"
        echo "  - workflow-1-intelligent-workshop" 
        echo "  - workflow-3-enhance-workshop"
        exit 1
    fi
    
    local pipeline_name="$1"
    local repo_url="$2"
    local workshop_name="$3"
    local additional_params="$4"
    
    # Check if we're logged into OpenShift
    if ! oc whoami &>/dev/null; then
        print_error "Not logged into OpenShift. Please run 'oc login' first."
        exit 1
    fi
    
    # Check if tkn is available
    if ! command -v tkn &>/dev/null; then
        print_error "tkn CLI not found. Please install Tekton CLI."
        exit 1
    fi
    
    # Check if jq is available
    if ! command -v jq &>/dev/null; then
        print_warning "jq not found. Some features may not work properly."
    fi
    
    run_pipeline "$pipeline_name" "$repo_url" "$workshop_name" "$additional_params"
}

# Run main function with all arguments
main "$@"
