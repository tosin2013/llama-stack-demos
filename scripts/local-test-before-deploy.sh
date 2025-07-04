#!/bin/bash

# Local Testing Before OpenShift Deployment Script
# Implements rules from LOCAL-001 through LOCAL-010 and BUILD-003
# 
# This script ensures comprehensive local testing before OpenShift deployment
# to catch issues early and prevent deployment failures.

set -e

# Configuration
SERVICE_DIR="workshop-monitoring-service"
LOCAL_PORT="8086"
LOCAL_URL="http://localhost:${LOCAL_PORT}"
QUARKUS_PROFILE="dev"
TEST_TIMEOUT=30

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

# Function to check if service is running
check_service_running() {
    local url="$1"
    local timeout="$2"
    
    print_info "Checking if service is running at $url..."
    
    for i in $(seq 1 $timeout); do
        if curl -s -f "$url/q/health/ready" > /dev/null 2>&1; then
            print_success "Service is running and ready"
            return 0
        fi
        sleep 1
    done
    
    print_error "Service is not responding after ${timeout} seconds"
    return 1
}

# Function to test endpoint
test_endpoint() {
    local endpoint="$1"
    local method="${2:-GET}"
    local data="$3"
    local expected_status="${4:-200}"
    
    print_info "Testing endpoint: $method $endpoint"
    
    local curl_cmd="curl -s -w '%{http_code}' -o /tmp/response.json"
    
    if [ "$method" = "POST" ] && [ -n "$data" ]; then
        curl_cmd="$curl_cmd -X POST -H 'Content-Type: application/json' -d '$data'"
    fi
    
    local status_code=$(eval "$curl_cmd '$endpoint'")
    
    if [ "$status_code" = "$expected_status" ]; then
        print_success "✅ $endpoint returned $status_code"
        
        # Validate JSON if response is expected to be JSON
        if [ "$expected_status" = "200" ]; then
            if jq . /tmp/response.json > /dev/null 2>&1; then
                print_success "✅ Response is valid JSON"
            else
                print_warning "⚠️  Response is not valid JSON"
            fi
        fi
        return 0
    else
        print_error "❌ $endpoint returned $status_code, expected $expected_status"
        return 1
    fi
}

# Function to run local tests
run_local_tests() {
    print_info "=== RULE LOCAL-001: Test Quarkus Service Locally ==="
    
    # Check if we're in the right directory
    if [ ! -f "$SERVICE_DIR/pom.xml" ]; then
        print_error "Cannot find $SERVICE_DIR/pom.xml. Please run from project root."
        exit 1
    fi
    
    cd "$SERVICE_DIR"
    
    print_info "=== RULE LOCAL-003: Run Unit Tests Before Build ==="
    print_info "Running unit tests..."
    if mvn test -q; then
        print_success "Unit tests passed"
    else
        print_error "Unit tests failed"
        return 1
    fi
    
    print_info "=== RULE BUILD-003: Local Build Before OpenShift ==="
    print_info "Building project locally..."
    if mvn clean package -DskipTests -q; then
        print_success "Local build successful"
    else
        print_error "Local build failed"
        return 1
    fi
    
    print_info "=== RULE LOCAL-001: Starting Quarkus in dev mode ==="
    print_info "Starting Quarkus service in background..."
    
    # Start Quarkus in background
    mvn quarkus:dev -Dquarkus.profile=$QUARKUS_PROFILE > /tmp/quarkus.log 2>&1 &
    QUARKUS_PID=$!
    
    # Wait for service to start
    if check_service_running "$LOCAL_URL" $TEST_TIMEOUT; then
        print_success "Quarkus service started successfully"
    else
        print_error "Failed to start Quarkus service"
        kill $QUARKUS_PID 2>/dev/null || true
        return 1
    fi
    
    # Run endpoint tests
    run_endpoint_tests
    
    # Cleanup
    print_info "Stopping Quarkus service..."
    kill $QUARKUS_PID 2>/dev/null || true
    wait $QUARKUS_PID 2>/dev/null || true
    
    cd ..
}

# Function to test all endpoints
run_endpoint_tests() {
    print_info "=== RULE LOCAL-002: Validate Endpoints Locally ==="
    
    local failed_tests=0
    
    # Test health endpoint
    if test_endpoint "$LOCAL_URL/q/health/ready"; then
        print_success "Health endpoint working"
    else
        ((failed_tests++))
    fi
    
    # Test monitoring info endpoint
    if test_endpoint "$LOCAL_URL/api/monitoring/info"; then
        print_success "Monitoring info endpoint working"
    else
        ((failed_tests++))
    fi
    
    # Test new pipeline config endpoint
    if test_endpoint "$LOCAL_URL/api/pipeline/config"; then
        print_success "Pipeline config endpoint working"
    else
        ((failed_tests++))
    fi
    
    # Test pipeline validation endpoint
    local test_data='{"repository-url": "https://github.com/test/repo.git", "workshop-name": "test-workshop"}'
    if test_endpoint "$LOCAL_URL/api/pipeline/validate-parameters" "POST" "$test_data"; then
        print_success "Pipeline validation endpoint working"
    else
        ((failed_tests++))
    fi
    
    print_info "=== RULE LOCAL-007: Validate JSON Responses Locally ==="
    
    # Test JSON response format
    if curl -s "$LOCAL_URL/api/pipeline/config" | jq . > /dev/null 2>&1; then
        print_success "Pipeline config returns valid JSON"
    else
        print_error "Pipeline config does not return valid JSON"
        ((failed_tests++))
    fi
    
    print_info "=== RULE LOCAL-008: Test Error Handling Locally ==="
    
    # Test error scenarios
    if test_endpoint "$LOCAL_URL/api/nonexistent" "GET" "" "404"; then
        print_success "404 error handling working"
    else
        ((failed_tests++))
    fi
    
    # Test invalid parameter validation
    local invalid_data='{"invalid": "data"}'
    if test_endpoint "$LOCAL_URL/api/pipeline/validate-parameters" "POST" "$invalid_data" "400"; then
        print_success "Parameter validation error handling working"
    else
        print_warning "Parameter validation might not be working correctly"
    fi
    
    print_info "=== RULE LOCAL-009: Performance Test Locally ==="
    
    # Basic performance test
    print_info "Running basic performance test..."
    local response_time=$(curl -w '%{time_total}' -s -o /dev/null "$LOCAL_URL/api/monitoring/info")
    if (( $(echo "$response_time < 1.0" | bc -l) )); then
        print_success "Response time: ${response_time}s (< 1s)"
    else
        print_warning "Response time: ${response_time}s (> 1s)"
    fi
    
    if [ $failed_tests -eq 0 ]; then
        print_success "All endpoint tests passed!"
        return 0
    else
        print_error "$failed_tests endpoint tests failed"
        return 1
    fi
}

# Function to test different profiles
test_profiles() {
    print_info "=== RULE LOCAL-010: Test with Different Profiles ==="
    
    cd "$SERVICE_DIR"
    
    # Test dev profile build
    print_info "Testing dev profile build..."
    if mvn clean package -Dquarkus.profile=dev -DskipTests -q; then
        print_success "Dev profile build successful"
    else
        print_error "Dev profile build failed"
        return 1
    fi
    
    # Test prod profile build
    print_info "Testing prod profile build..."
    if mvn clean package -Dquarkus.profile=prod -DskipTests -q; then
        print_success "Prod profile build successful"
    else
        print_error "Prod profile build failed"
        return 1
    fi
    
    cd ..
}

# Function to validate git status
validate_git_status() {
    print_info "=== Validating Git Status ==="
    
    # Check for uncommitted changes
    if [ -n "$(git status --porcelain)" ]; then
        print_warning "You have uncommitted changes:"
        git status --short
        print_warning "Consider committing changes before deployment"
    else
        print_success "Git working directory is clean"
    fi
    
    # Check if we're ahead of origin
    local ahead=$(git rev-list --count HEAD ^origin/main 2>/dev/null || echo "0")
    if [ "$ahead" -gt 0 ]; then
        print_warning "You have $ahead unpushed commits"
        print_warning "Remember to push changes before OpenShift build"
    else
        print_success "Local branch is up to date with origin"
    fi
}

# Main function
main() {
    print_info "🚀 Starting Local Testing Before OpenShift Deployment"
    print_info "Implementing Local Testing Rules (LOCAL-001 through LOCAL-010)"
    echo
    
    # Validate prerequisites
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v curl &> /dev/null; then
        print_error "curl is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_warning "jq is not installed - JSON validation will be limited"
    fi
    
    # Run tests
    local exit_code=0
    
    validate_git_status
    echo
    
    if run_local_tests; then
        print_success "✅ Local service tests passed"
    else
        print_error "❌ Local service tests failed"
        exit_code=1
    fi
    echo
    
    if test_profiles; then
        print_success "✅ Profile tests passed"
    else
        print_error "❌ Profile tests failed"
        exit_code=1
    fi
    echo
    
    if [ $exit_code -eq 0 ]; then
        print_success "🎉 All local tests passed! Ready for OpenShift deployment."
        print_info "Next steps:"
        print_info "1. git add . && git commit -m 'your message'"
        print_info "2. git push origin main"
        print_info "3. oc start-build workshop-monitoring-service-build -n workshop-system"
    else
        print_error "❌ Some tests failed. Please fix issues before deploying to OpenShift."
    fi
    
    exit $exit_code
}

# Run main function
main "$@"
