#!/bin/bash

# Test Agent Fixes Script
# Tests the fixed AgentTaskManager and Content Creator functionality

set -e

echo "🧪 Testing Agent Fixes"
echo "======================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test configuration
WORKSHOP_NAME="test-agent-fixes-$(date +%s)"
REPOSITORY_URL="https://github.com/jeremyrdavis/dddhexagonalworkshop.git"
MIDDLEWARE_URL="https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"

echo -e "${YELLOW}📋 Test Configuration:${NC}"
echo "  Workshop Name: $WORKSHOP_NAME"
echo "  Repository URL: $REPOSITORY_URL"
echo "  Middleware URL: $MIDDLEWARE_URL"
echo ""

# Function to check if agents are healthy
check_agent_health() {
    echo -e "${YELLOW}🏥 Checking Agent Health...${NC}"
    
    local health_response=$(curl -s -k "$MIDDLEWARE_URL/api/monitoring/agents" | jq -r '.[0].health' 2>/dev/null || echo "ERROR")
    
    if [ "$health_response" = "HEALTHY" ]; then
        echo -e "${GREEN}✅ Agents are healthy${NC}"
        return 0
    else
        echo -e "${RED}❌ Agents are not healthy: $health_response${NC}"
        return 1
    fi
}

# Function to test content creator via middleware
test_content_creator() {
    echo -e "${YELLOW}🎨 Testing Content Creator Agent...${NC}"
    
    local payload=$(cat <<EOF
{
  "workshop_name": "$WORKSHOP_NAME",
  "repository_url": "$REPOSITORY_URL",
  "base_template": "showroom_template_default",
  "workspace_mode": "file-based",
  "target_directory": "/workspace/shared-data/final-output"
}
EOF
)
    
    echo "📤 Sending request to Content Creator..."
    local response=$(curl -s -k -X POST \
        -H "Content-Type: application/json" \
        -d "$payload" \
        "$MIDDLEWARE_URL/api/pipeline/content-creator/create-workshop")
    
    echo "📥 Response received:"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
    
    # Check if response contains "simplified implementation"
    if echo "$response" | grep -q "simplified implementation"; then
        echo -e "${RED}❌ Content Creator still returning simplified implementation${NC}"
        return 1
    elif echo "$response" | grep -q "success"; then
        echo -e "${GREEN}✅ Content Creator returned success response${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️ Content Creator response unclear${NC}"
        return 1
    fi
}

# Function to check workspace content
check_workspace_content() {
    echo -e "${YELLOW}📁 Checking Workspace Content...${NC}"
    
    # Check if content was created in shared workspace
    echo "🔍 Checking for workshop content in agent pods..."
    
    local pod_check=$(oc exec -n workshop-system deployment/content-creator-agent -- \
        ls -la "/workspace/shared-data/final-output/$WORKSHOP_NAME" 2>/dev/null || echo "NOT_FOUND")
    
    if [ "$pod_check" = "NOT_FOUND" ]; then
        echo -e "${RED}❌ No workshop content found in shared workspace${NC}"
        return 1
    else
        echo -e "${GREEN}✅ Workshop content found in shared workspace:${NC}"
        echo "$pod_check"
        return 0
    fi
}

# Function to test pipeline execution
test_pipeline_execution() {
    echo -e "${YELLOW}🚀 Testing Pipeline Execution...${NC}"
    
    echo "📤 Starting pipeline with fixed agents..."
    local pipeline_run=$(tkn pipeline start workflow-1-simple-corrected -n workshop-system \
        --param repository-url="$REPOSITORY_URL" \
        --param workshop-name="$WORKSHOP_NAME" \
        --workspace name=shared-data,claimName=workshop-shared-pvc \
        --param base-template="showroom_template_default" \
        --output name 2>/dev/null || echo "FAILED")
    
    if [ "$pipeline_run" = "FAILED" ]; then
        echo -e "${RED}❌ Failed to start pipeline${NC}"
        return 1
    fi
    
    echo "📋 Pipeline started: $pipeline_run"
    echo "⏳ Waiting for pipeline to complete..."
    
    # Wait for pipeline completion (max 5 minutes)
    local timeout=300
    local elapsed=0
    local status="Unknown"
    
    while [ $elapsed -lt $timeout ]; do
        status=$(tkn pipelinerun describe "$pipeline_run" -n workshop-system -o jsonpath='{.status.conditions[0].reason}' 2>/dev/null || echo "Unknown")
        
        if [ "$status" = "Succeeded" ]; then
            echo -e "${GREEN}✅ Pipeline completed successfully${NC}"
            return 0
        elif [ "$status" = "Failed" ]; then
            echo -e "${RED}❌ Pipeline failed${NC}"
            return 1
        fi
        
        sleep 10
        elapsed=$((elapsed + 10))
        echo "⏳ Pipeline status: $status (${elapsed}s elapsed)"
    done
    
    echo -e "${RED}❌ Pipeline timed out after ${timeout}s${NC}"
    return 1
}

# Function to run all tests
run_tests() {
    local tests_passed=0
    local tests_total=4
    
    echo -e "${YELLOW}🧪 Running Agent Fix Tests...${NC}"
    echo ""
    
    # Test 1: Agent Health
    if check_agent_health; then
        tests_passed=$((tests_passed + 1))
    fi
    echo ""
    
    # Test 2: Content Creator
    if test_content_creator; then
        tests_passed=$((tests_passed + 1))
    fi
    echo ""
    
    # Test 3: Workspace Content
    if check_workspace_content; then
        tests_passed=$((tests_passed + 1))
    fi
    echo ""
    
    # Test 4: Pipeline Execution
    if test_pipeline_execution; then
        tests_passed=$((tests_passed + 1))
    fi
    echo ""
    
    # Summary
    echo -e "${YELLOW}📊 Test Results Summary:${NC}"
    echo "  Tests Passed: $tests_passed/$tests_total"
    
    if [ $tests_passed -eq $tests_total ]; then
        echo -e "${GREEN}🎉 All tests passed! Agent fixes are working correctly.${NC}"
        return 0
    else
        echo -e "${RED}❌ Some tests failed. Agent fixes need more work.${NC}"
        return 1
    fi
}

# Main execution
main() {
    echo "🚀 Starting Agent Fix Tests..."
    echo ""
    
    # Check prerequisites
    if ! command -v oc &> /dev/null; then
        echo -e "${RED}❌ oc CLI not found${NC}"
        exit 1
    fi
    
    if ! command -v tkn &> /dev/null; then
        echo -e "${RED}❌ tkn CLI not found${NC}"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}❌ jq not found${NC}"
        exit 1
    fi
    
    # Run tests
    if run_tests; then
        echo -e "${GREEN}✅ Agent fixes validation completed successfully!${NC}"
        exit 0
    else
        echo -e "${RED}❌ Agent fixes validation failed!${NC}"
        exit 1
    fi
}

# Execute main function
main "$@"
