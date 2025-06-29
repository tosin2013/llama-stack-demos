#!/bin/bash

# Test script for Workshop Monitoring Service Kustomize configuration

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

# Test Kustomize configurations
test_kustomize() {
    print_status "Testing Kustomize configurations..."
    
    # Test base configuration
    print_status "Testing base configuration..."
    if kustomize build base/ > /dev/null; then
        print_success "Base configuration is valid"
    else
        print_error "Base configuration has errors"
        return 1
    fi
    
    # Test development overlay
    print_status "Testing development overlay..."
    if kustomize build overlays/development/ > /dev/null; then
        print_success "Development overlay is valid"
    else
        print_error "Development overlay has errors"
        return 1
    fi
    
    # Test production overlay
    print_status "Testing production overlay..."
    if kustomize build overlays/production/ > /dev/null; then
        print_success "Production overlay is valid"
    else
        print_error "Production overlay has errors"
        return 1
    fi
    
    print_success "All Kustomize configurations are valid"
}

# Count resources
count_resources() {
    print_status "Counting generated resources..."
    
    local dev_resources=$(kustomize build overlays/development/ | grep -c "^kind:" || echo "0")
    local prod_resources=$(kustomize build overlays/production/ | grep -c "^kind:" || echo "0")
    
    echo ""
    echo "📊 Resource Summary:"
    echo "==================="
    echo "Development environment: ${dev_resources} resources"
    echo "Production environment: ${prod_resources} resources"
    echo ""
    
    # List resource types
    print_status "Resource types in development:"
    kustomize build overlays/development/ | grep "^kind:" | sort | uniq -c | sed 's/^/  /'
    
    echo ""
}

# Validate resource names
validate_names() {
    print_status "Validating resource names..."
    
    # Check for naming conflicts
    local names=$(kustomize build overlays/development/ | grep -E "^  name:" | awk '{print $2}' | sort)
    local unique_names=$(echo "$names" | uniq)
    
    if [ "$(echo "$names" | wc -l)" = "$(echo "$unique_names" | wc -l)" ]; then
        print_success "No naming conflicts found"
    else
        print_warning "Potential naming conflicts detected"
        echo "All names:"
        echo "$names" | sed 's/^/  /'
        echo "Unique names:"
        echo "$unique_names" | sed 's/^/  /'
    fi
}

# Main execution
main() {
    print_status "🧪 Testing Workshop Monitoring Service Kustomize Configuration"
    echo ""
    
    # Check if kustomize is available
    if ! command -v kustomize &> /dev/null; then
        print_error "Kustomize not found. Please install it first."
        exit 1
    fi
    
    test_kustomize
    count_resources
    validate_names
    
    echo ""
    print_success "🎉 All tests passed! Kustomize configuration is ready for deployment."
    echo ""
    print_status "To deploy:"
    print_status "  Development: oc apply -k overlays/development/"
    print_status "  Production:  oc apply -k overlays/production/"
    echo ""
}

# Run main function
main "$@"
