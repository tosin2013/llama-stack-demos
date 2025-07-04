#!/bin/bash

# Generate TKN Pipeline Commands from Middleware Configuration
# Implements TKN-001 through TKN-010 rules for dynamic parameter generation
#
# This script calls the middleware endpoint to get correct parameters
# and generates the proper tkn command instead of hardcoding values.

set -e

# Configuration
MIDDLEWARE_URL="${MIDDLEWARE_URL:-https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com}"
NAMESPACE="${NAMESPACE:-workshop-system}"
CONFIG_CACHE_FILE="/tmp/pipeline-config-$(date +%s).json"

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

# Function to check middleware availability (TKN-010)
check_middleware_health() {
    print_info "Checking middleware availability..."
    
    if curl -k -f "$MIDDLEWARE_URL/q/health/ready" > /dev/null 2>&1; then
        print_success "Middleware is available"
        return 0
    else
        print_error "Middleware is not available at $MIDDLEWARE_URL"
        return 1
    fi
}

# Function to get and cache configuration (TKN-009)
get_pipeline_config() {
    print_info "Fetching pipeline configuration from middleware..."
    
    if curl -k -f "$MIDDLEWARE_URL/api/pipeline/config" > "$CONFIG_CACHE_FILE" 2>/dev/null; then
        print_success "Configuration cached to $CONFIG_CACHE_FILE"
        return 0
    else
        print_error "Failed to fetch configuration from middleware"
        return 1
    fi
}

# Function to validate parameters using middleware (TKN-002)
validate_parameters() {
    local repo_url="$1"
    local workshop_name="$2"
    local validation_type="$3"
    
    print_info "Validating parameters with middleware..."
    
    local validation_payload='{"repository-url": "'$repo_url'", "workshop-name": "'$workshop_name'"'

    if [ -n "$validation_type" ]; then
        validation_payload+=', "validation-type": "'$validation_type'"'
    fi

    validation_payload+='}'
    
    local validation_response=$(curl -k -X POST -H "Content-Type: application/json" \
        -d "$validation_payload" \
        "$MIDDLEWARE_URL/api/pipeline/validate-parameters" 2>/dev/null)
    
    local is_valid=$(echo "$validation_response" | jq -r '.data.valid // false')
    
    if [ "$is_valid" = "true" ]; then
        print_success "Parameter validation passed"
        return 0
    else
        print_error "Parameter validation failed:"
        echo "$validation_response" | jq -r '.data.errors[]?' 2>/dev/null || echo "Unknown validation error"
        return 1
    fi
}

# Function to extract values from cached config (TKN-004, TKN-005, TKN-006)
extract_config_values() {
    local pipeline="$1"
    
    print_info "Extracting configuration values for pipeline: $pipeline"
    
    # Extract workspace PVC name (TKN-004)
    PVC_NAME=$(jq -r '.data.workspaces."shared-data".pvc_name // "workshop-shared-pvc"' "$CONFIG_CACHE_FILE")
    print_info "PVC Name: $PVC_NAME"
    
    # Extract validation types (TKN-005)
    VALIDATION_TYPES=$(jq -r '.data.validation_types."research-validation".supported_types[]?' "$CONFIG_CACHE_FILE" | head -1)
    if [ -z "$VALIDATION_TYPES" ]; then
        VALIDATION_TYPES="new-workshop-validation"
    fi
    print_info "Default Validation Type: $VALIDATION_TYPES"
    
    # Extract pipeline defaults (TKN-006)
    AUTO_APPROVE=$(jq -r ".data.pipelines.\"$pipeline\".parameter_defaults.\"auto-approve\" // \"false\"" "$CONFIG_CACHE_FILE")
    BASE_TEMPLATE=$(jq -r ".data.pipelines.\"$pipeline\".parameter_defaults.\"base-template\" // \"showroom_template_default\"" "$CONFIG_CACHE_FILE")
    HUMAN_APPROVER=$(jq -r ".data.pipelines.\"$pipeline\".parameter_defaults.\"human-approver\" // \"system-operator\"" "$CONFIG_CACHE_FILE")
    
    print_info "Pipeline Defaults:"
    print_info "  auto-approve: $AUTO_APPROVE"
    print_info "  base-template: $BASE_TEMPLATE"
    print_info "  human-approver: $HUMAN_APPROVER"
    
    # Extract required parameters (TKN-007)
    REQUIRED_PARAMS=$(jq -r ".data.pipelines.\"$pipeline\".required_parameters[]?" "$CONFIG_CACHE_FILE")
    print_info "Required Parameters: $(echo $REQUIRED_PARAMS | tr '\n' ' ')"
}

# Function to check required parameters (TKN-007)
check_required_parameters() {
    local pipeline="$1"
    local repo_url="$2"
    local workshop_name="$3"
    
    print_info "Checking required parameters for pipeline: $pipeline"
    
    local missing_params=()
    
    for param in $REQUIRED_PARAMS; do
        case $param in
            "repository-url")
                [ -z "$repo_url" ] && missing_params+=("repository-url")
                ;;
            "workshop-name")
                [ -z "$workshop_name" ] && missing_params+=("workshop-name")
                ;;
            "auto-detect-workflow"|"human-approver"|"auto-approve")
                # These have defaults, so they're not missing
                ;;
            *)
                print_warning "Unknown required parameter: $param"
                ;;
        esac
    done
    
    if [ ${#missing_params[@]} -gt 0 ]; then
        print_error "Missing required parameters: ${missing_params[*]}"
        return 1
    else
        print_success "All required parameters provided"
        return 0
    fi
}

# Function to generate tkn command (TKN-003)
generate_tkn_command() {
    local pipeline="$1"
    local repo_url="$2"
    local workshop_name="$3"
    local validation_type="$4"
    
    print_info "Generating tkn command for pipeline: $pipeline"
    
    # Start building the command
    local cmd="tkn pipeline start $pipeline -n $NAMESPACE"
    
    # Add required parameters
    cmd="$cmd --param repository-url=\"$repo_url\""
    cmd="$cmd --param workshop-name=\"$workshop_name\""
    
    # Add workspace
    cmd="$cmd --workspace name=shared-data,claimName=$PVC_NAME"
    
    # Add pipeline-specific parameters based on pipeline type
    case $pipeline in
        "workflow-1-intelligent-workshop")
            cmd="$cmd --param auto-detect-workflow=\"true\""
            cmd="$cmd --param human-approver=\"$HUMAN_APPROVER\""
            cmd="$cmd --param auto-approve=\"$AUTO_APPROVE\""
            if [ -n "$validation_type" ]; then
                cmd="$cmd --param validation-type=\"$validation_type\""
            else
                cmd="$cmd --param validation-type=\"$VALIDATION_TYPES\""
            fi
            # Add gitea-auth workspace for intelligent workflow
            cmd="$cmd --workspace name=gitea-auth,secret=gitea-auth-secret"
            ;;
        "workflow-1-simple-corrected")
            cmd="$cmd --param base-template=\"$BASE_TEMPLATE\""
            ;;
        "workflow-3-enhance-workshop")
            if [ -n "$validation_type" ]; then
                cmd="$cmd --param validation-type=\"$validation_type\""
            else
                cmd="$cmd --param validation-type=\"enhancement-analysis\""
            fi
            # Add gitea-auth workspace for enhancement workflow
            cmd="$cmd --workspace name=gitea-auth,secret=gitea-auth-secret"
            ;;
    esac
    
    echo "$cmd"
}

# Function to display usage
usage() {
    echo "Usage: $0 <pipeline> <repository-url> <workshop-name> [validation-type]"
    echo ""
    echo "Available pipelines:"
    echo "  workflow-1-simple-corrected     - Simple workshop creation"
    echo "  workflow-1-intelligent-workshop - Intelligent workshop creation with HITL"
    echo "  workflow-3-enhance-workshop     - Enhance existing workshop"
    echo ""
    echo "Examples:"
    echo "  $0 workflow-1-simple-corrected https://github.com/jeremyrdavis/dddhexagonalworkshop.git ddd-workshop"
    echo "  $0 workflow-1-intelligent-workshop https://github.com/tosin2013/ansible-controller-cac.git ansible-workshop new-workshop-validation"
    echo ""
    echo "Environment Variables:"
    echo "  MIDDLEWARE_URL - Middleware service URL (default: current OpenShift route)"
    echo "  NAMESPACE      - Tekton namespace (default: workshop-system)"
}

# Main function
main() {
    if [ $# -lt 3 ]; then
        usage
        exit 1
    fi
    
    local pipeline="$1"
    local repo_url="$2"
    local workshop_name="$3"
    local validation_type="$4"
    
    print_info "🚀 Generating TKN command using middleware configuration"
    print_info "Pipeline: $pipeline"
    print_info "Repository: $repo_url"
    print_info "Workshop Name: $workshop_name"
    [ -n "$validation_type" ] && print_info "Validation Type: $validation_type"
    echo
    
    # TKN-010: Check middleware availability with fallback
    if ! check_middleware_health; then
        print_warning "Using fallback configuration values"
        PVC_NAME="workshop-shared-pvc"
        VALIDATION_TYPES="new-workshop-validation"
        AUTO_APPROVE="false"
        BASE_TEMPLATE="showroom_template_default"
        HUMAN_APPROVER="system-operator"
    else
        # TKN-009: Get and cache configuration
        if ! get_pipeline_config; then
            print_error "Failed to get configuration, exiting"
            exit 1
        fi
        
        # Extract values from configuration
        extract_config_values "$pipeline"
        
        # TKN-007: Check required parameters
        if ! check_required_parameters "$pipeline" "$repo_url" "$workshop_name"; then
            exit 1
        fi
        
        # TKN-002: Validate parameters
        if ! validate_parameters "$repo_url" "$workshop_name" "$validation_type"; then
            exit 1
        fi
    fi
    
    # TKN-003: Generate the tkn command
    local tkn_command=$(generate_tkn_command "$pipeline" "$repo_url" "$workshop_name" "$validation_type")
    
    echo
    print_success "Generated TKN command:"
    echo
    echo "$tkn_command"
    echo
    
    # Ask for confirmation before execution
    read -p "Execute this command? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Executing tkn command..."
        eval "$tkn_command"
    else
        print_info "Command not executed. You can copy and run it manually."
    fi
    
    # Cleanup
    rm -f "$CONFIG_CACHE_FILE"
}

# Run main function
main "$@"
