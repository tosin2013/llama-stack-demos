#!/bin/bash

# Workshop Template System - Setup Validation Script
# Run this before deployment to check prerequisites

set -e

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

# Validation results
VALIDATION_PASSED=true

# Function to validate prerequisites
validate_prerequisites() {
    print_status "Validating prerequisites..."
    
    # Check OpenShift CLI
    if command -v oc &> /dev/null; then
        print_success "OpenShift CLI (oc) is installed"
        
        # Check if logged in
        if oc whoami &> /dev/null; then
            local user=$(oc whoami)
            local server=$(oc whoami --show-server)
            print_success "Logged into OpenShift as: $user"
            print_status "Server: $server"
        else
            print_error "Not logged into OpenShift. Run: oc login"
            VALIDATION_PASSED=false
        fi
    else
        print_error "OpenShift CLI (oc) not found. Please install it."
        VALIDATION_PASSED=false
    fi
    
    # Check curl
    if command -v curl &> /dev/null; then
        print_success "curl is available"
    else
        print_error "curl not found. Required for Gitea installation."
        VALIDATION_PASSED=false
    fi
    
    # Check git
    if command -v git &> /dev/null; then
        print_success "git is available"
    else
        print_warning "git not found. May be needed for some operations."
    fi
    
    # Check kustomize (optional)
    if command -v kustomize &> /dev/null; then
        print_success "kustomize CLI is available"
    else
        print_warning "kustomize CLI not found. Will use 'oc apply -k' instead."
    fi
}

# Function to validate directory structure
validate_structure() {
    print_status "Validating directory structure..."
    
    # Check base directory
    if [ -d "base" ]; then
        print_success "Base directory exists"
        
        # Check required base files
        local required_files=(
            "base/kustomization.yaml"
            "base/namespace.yaml"
            "base/serviceaccount.yaml"
            "base/llama-stack-deployment.yaml"
            "base/agents-deployment.yaml"
            "base/workshops-deployment.yaml"
        )
        
        for file in "${required_files[@]}"; do
            if [ -f "$file" ]; then
                print_success "Found: $file"
            else
                print_error "Missing: $file"
                VALIDATION_PASSED=false
            fi
        done
    else
        print_error "Base directory not found"
        VALIDATION_PASSED=false
    fi
    
    # Check overlays directory
    if [ -d "overlays" ]; then
        print_success "Overlays directory exists"
        
        local overlay_count=$(find overlays -name "kustomization.yaml" | wc -l)
        print_status "Found $overlay_count overlay configurations"
        
        if [ $overlay_count -eq 0 ]; then
            print_warning "No overlay configurations found"
        fi
    else
        print_error "Overlays directory not found"
        VALIDATION_PASSED=false
    fi
}

# Function to validate overlay configuration
validate_overlay() {
    local overlay_name="$1"
    
    if [ -z "$overlay_name" ]; then
        print_status "No specific overlay provided for validation"
        return 0
    fi
    
    print_status "Validating overlay: $overlay_name"
    
    local overlay_path="overlays/$overlay_name"
    
    if [ ! -d "$overlay_path" ]; then
        print_error "Overlay directory not found: $overlay_path"
        VALIDATION_PASSED=false
        return 1
    fi
    
    # Check kustomization.yaml
    if [ -f "$overlay_path/kustomization.yaml" ]; then
        print_success "Found: $overlay_path/kustomization.yaml"
        
        # Check for placeholder values
        if grep -q "YOUR-.*" "$overlay_path/kustomization.yaml"; then
            print_warning "Placeholder values found in kustomization.yaml"
            print_warning "Please customize with your actual values"
        else
            print_success "No placeholder values in kustomization.yaml"
        fi
    else
        print_error "Missing: $overlay_path/kustomization.yaml"
        VALIDATION_PASSED=false
    fi
    
    # Check secrets.env
    if [ -f "$overlay_path/secrets.env" ]; then
        print_success "Found: $overlay_path/secrets.env"
        
        # Check required secrets
        local required_secrets=("GITHUB_TOKEN")
        local missing_secrets=()
        
        for secret in "${required_secrets[@]}"; do
            if ! grep -q "^${secret}=" "$overlay_path/secrets.env" || \
               grep -q "^${secret}=.*your-.*-here" "$overlay_path/secrets.env"; then
                missing_secrets+=("$secret")
            fi
        done
        
        if [ ${#missing_secrets[@]} -eq 0 ]; then
            print_success "Required secrets appear to be configured"
        else
            print_warning "Missing or placeholder secrets:"
            for secret in "${missing_secrets[@]}"; do
                print_warning "  - $secret"
            done
        fi
    else
        print_error "Missing: $overlay_path/secrets.env"
        VALIDATION_PASSED=false
    fi
}

# Function to check OpenShift permissions
validate_permissions() {
    print_status "Validating OpenShift permissions..."
    
    # Test project creation (dry-run)
    if oc auth can-i create projects &> /dev/null; then
        print_success "Can create projects"
    else
        print_warning "Cannot create projects. May need cluster-admin help."
    fi
    
    # Test namespace operations
    if oc auth can-i create namespaces &> /dev/null; then
        print_success "Can create namespaces"
    else
        print_warning "Cannot create namespaces directly"
    fi
    
    # Test route creation
    if oc auth can-i create routes &> /dev/null; then
        print_success "Can create routes"
    else
        print_error "Cannot create routes. This is required for workshop access."
        VALIDATION_PASSED=false
    fi
    
    # Test deployment creation
    if oc auth can-i create deployments &> /dev/null; then
        print_success "Can create deployments"
    else
        print_error "Cannot create deployments. This is required."
        VALIDATION_PASSED=false
    fi
}

# Function to show available overlays
show_available_overlays() {
    print_status "Available overlays:"
    
    for overlay in overlays/*/; do
        if [ -d "$overlay" ]; then
            local overlay_name=$(basename "$overlay")
            if [ -f "$overlay/kustomization.yaml" ]; then
                echo "  📁 $overlay_name"
            fi
        fi
    done
}

# Main validation function
main() {
    echo "🔍 Workshop Template System - Setup Validation"
    echo "=============================================="
    
    # Change to script directory
    cd "$(dirname "$0")"
    
    # Run validations
    validate_prerequisites
    validate_structure
    validate_permissions
    
    # Validate specific overlay if provided
    if [ $# -gt 0 ]; then
        validate_overlay "$1"
    else
        show_available_overlays
        echo ""
        echo "To validate a specific overlay, run:"
        echo "  ./validate-setup.sh overlay-name"
    fi
    
    echo ""
    echo "🎯 Validation Summary"
    echo "===================="
    
    if [ "$VALIDATION_PASSED" = true ]; then
        print_success "All validations passed!"
        echo ""
        echo "✅ Ready to deploy!"
        echo "Run: ./deploy.sh overlay-name"
    else
        print_error "Some validations failed!"
        echo ""
        echo "❌ Please fix the issues above before deployment"
        echo "See: PRE_DEPLOYMENT_CHECKLIST.md for guidance"
    fi
    
    echo ""
    echo "📚 Documentation:"
    echo "  - PRE_DEPLOYMENT_CHECKLIST.md - Complete setup guide"
    echo "  - GITEA_SETUP.md - Gitea configuration details"
    echo "  - SECRETS_GUIDE.md - Required secrets information"
}

# Run main function
main "$@"
