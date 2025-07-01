#!/bin/bash

# Test RAG Content Quality Workflow Locally
# Tests the new RAG content quality and external reference validation functionality

set -e

echo "🔍 Testing RAG Content Quality Workflow"
echo "======================================="

# Configuration
MIDDLEWARE_URL="http://localhost:8086"
WORKSHOP_NAME="philoagents-course-workshop"
TEST_REPO="https://github.com/neural-maze/philoagents-course"
TEST_CONTENT="This is a workshop about AI agents and philosophy based on the PhiloAgents course. It references https://docs.python.org, https://pytorch.org/docs, and https://huggingface.co/docs for technical implementation."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to test endpoint
test_endpoint() {
    local endpoint=$1
    local data=$2
    local description=$3
    
    echo ""
    echo -e "${BLUE}🧪 Testing: $description${NC}"
    echo "   Endpoint: $endpoint"
    echo "   Data: $(echo "$data" | jq -c .)"
    
    response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$endpoint" \
        -H "Content-Type: application/json" \
        -d "$data")
    
    http_status=$(echo "$response" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$http_status" = "200" ]; then
        echo -e "${GREEN}✅ SUCCESS (HTTP $http_status)${NC}"
        echo "   Response: $(echo "$response_body" | jq -r '.status // "unknown"')"
        
        # Extract key metrics if available
        if echo "$response_body" | jq -e '.quality_score_improvement' > /dev/null 2>&1; then
            improvement=$(echo "$response_body" | jq -r '.quality_score_improvement')
            echo -e "${GREEN}   📊 Quality Improvement: +$improvement${NC}"
        fi
        
        if echo "$response_body" | jq -e '.references_validated' > /dev/null 2>&1; then
            validated=$(echo "$response_body" | jq -r '.references_validated')
            echo -e "${GREEN}   🔗 References Validated: $validated${NC}"
        fi
        
        return 0
    else
        echo -e "${RED}❌ FAILED (HTTP $http_status)${NC}"
        echo "   Error: $response_body"
        return 1
    fi
}

# Check prerequisites
echo "🔧 Checking prerequisites..."

# Check if Quarkus middleware is running
if ! curl -s "$MIDDLEWARE_URL/api/pipeline/health" > /dev/null 2>&1; then
    echo -e "${RED}❌ Quarkus middleware not running at $MIDDLEWARE_URL${NC}"
    echo "   Start with: cd workshop-monitoring-service && mvn quarkus:dev"
    exit 1
fi
echo -e "${GREEN}✅ Quarkus middleware is running${NC}"

# Check if RAG stack is running (optional for mock testing)
if curl -s "http://localhost:19530" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Milvus RAG stack is running${NC}"
    RAG_AVAILABLE=true
else
    echo -e "${YELLOW}⚠️  Milvus RAG stack not running - using mock endpoints only${NC}"
    echo "   Start RAG stack with: ./scripts/start-rag-stack-local.sh"
    RAG_AVAILABLE=false
fi

echo ""
echo "🚀 Starting RAG Content Quality Tests"
echo "====================================="

# Test 1: Validate External References
echo -e "${BLUE}📋 Test 1: External Reference Validation${NC}"
reference_validation_data=$(cat <<EOF
{
  "workshop_name": "$WORKSHOP_NAME",
  "workshop_content": "$TEST_CONTENT",
  "reference_types": "all",
  "check_accessibility": true,
  "check_freshness": true,
  "quality_scoring": true,
  "timeout_seconds": 30
}
EOF
)

test_endpoint "$MIDDLEWARE_URL/api/pipeline/mock/research-validation/validate-external-references" \
    "$reference_validation_data" \
    "External Reference Validation (Mock)"

# Test 2: Update RAG Content with External References
echo -e "${BLUE}📋 Test 2: RAG Content Update${NC}"
rag_update_data=$(cat <<EOF
{
  "workshop_name": "$WORKSHOP_NAME",
  "workshop_content": "$TEST_CONTENT",
  "external_references": [
    {
      "url": "https://docs.python.org/3/library/asyncio.html",
      "type": "documentation",
      "authority_score": 0.98,
      "last_validated": "2025-06-30",
      "content_summary": "Python asyncio documentation for agent communication"
    },
    {
      "url": "https://pytorch.org/docs/stable/index.html",
      "type": "documentation",
      "authority_score": 0.95,
      "last_validated": "2025-06-30",
      "content_summary": "PyTorch documentation for neural network implementations"
    },
    {
      "url": "https://huggingface.co/docs/transformers/index",
      "type": "documentation",
      "authority_score": 0.92,
      "last_validated": "2025-06-30",
      "content_summary": "Hugging Face Transformers for language model integration"
    }
  ],
  "quality_threshold": 0.75,
  "update_mode": "incremental",
  "validation_required": true
}
EOF
)

test_endpoint "$MIDDLEWARE_URL/api/pipeline/mock/research-validation/update-rag-content" \
    "$rag_update_data" \
    "RAG Content Update with External References (Mock)"

# Test 3: Enhance Content with Validated References
echo -e "${BLUE}📋 Test 3: Content Enhancement${NC}"
content_enhancement_data=$(cat <<EOF
{
  "workshop_name": "$WORKSHOP_NAME",
  "workshop_content": "$TEST_CONTENT",
  "validated_references": [
    {
      "url": "https://docs.python.org/3/tutorial/classes.html",
      "title": "Python Classes Tutorial",
      "quality_score": 0.98,
      "relevance_score": 0.92,
      "content_type": "tutorial",
      "integration_suggestion": "Add as prerequisite for agent architecture"
    },
    {
      "url": "https://pytorch.org/tutorials/beginner/basics/intro.html",
      "title": "PyTorch Basics Tutorial",
      "quality_score": 0.95,
      "relevance_score": 0.88,
      "content_type": "tutorial",
      "integration_suggestion": "Reference for neural network implementation"
    },
    {
      "url": "https://huggingface.co/course/chapter1/1",
      "title": "Hugging Face Course - Introduction",
      "quality_score": 0.93,
      "relevance_score": 0.90,
      "content_type": "course",
      "integration_suggestion": "Include as advanced reading for transformer models"
    }
  ],
  "enhancement_strategy": "contextual",
  "quality_threshold": 0.8,
  "preserve_original": true
}
EOF
)

test_endpoint "$MIDDLEWARE_URL/api/pipeline/mock/content-creator/enhance-with-references" \
    "$content_enhancement_data" \
    "Content Enhancement with Validated References (Mock)"

# Test 4: Real endpoints (if RAG stack is available)
if [ "$RAG_AVAILABLE" = true ]; then
    echo ""
    echo -e "${BLUE}🔄 Testing Real RAG Integration${NC}"
    echo "================================"
    
    # Test real RAG update endpoint
    test_endpoint "$MIDDLEWARE_URL/api/pipeline/research-validation/update-rag-content" \
        "$rag_update_data" \
        "RAG Content Update (Real Milvus Integration)"
    
    # Test real reference validation
    test_endpoint "$MIDDLEWARE_URL/api/pipeline/research-validation/validate-external-references" \
        "$reference_validation_data" \
        "External Reference Validation (Real Integration)"
fi

# Test 5: Health and Status Checks
echo ""
echo -e "${BLUE}📋 Test 5: System Health Checks${NC}"
echo "================================="

# Check middleware health
echo "🔍 Checking middleware health..."
health_response=$(curl -s "$MIDDLEWARE_URL/api/pipeline/health")
if echo "$health_response" | jq -e '.status' > /dev/null 2>&1; then
    status=$(echo "$health_response" | jq -r '.status')
    profile=$(echo "$health_response" | jq -r '.profile // "unknown"')
    echo -e "${GREEN}✅ Middleware Status: $status (Profile: $profile)${NC}"
else
    echo -e "${RED}❌ Middleware health check failed${NC}"
fi

# Summary
echo ""
echo "📊 Test Summary"
echo "==============="
echo -e "${GREEN}✅ RAG Content Quality Workflow Tests Complete${NC}"
echo ""
echo "🎯 Key Features Tested:"
echo "   ✅ External reference validation"
echo "   ✅ RAG content updates with quality scoring"
echo "   ✅ Content enhancement with validated references"
echo "   ✅ Mock endpoint functionality"
if [ "$RAG_AVAILABLE" = true ]; then
    echo "   ✅ Real Milvus RAG integration"
fi
echo ""
echo "📋 Next Steps:"
echo "1. Review test results above"
echo "2. Check Quarkus logs: tail -f workshop-monitoring-service/target/quarkus.log"
if [ "$RAG_AVAILABLE" = true ]; then
    echo "3. Check Milvus logs: podman logs workshop-milvus"
fi
echo "4. Test with real agents: ./scripts/start-agents-local.sh"
echo "5. Deploy to OpenShift: Follow docs/TEKTON_TESTING_PLAN.md"
echo ""
echo -e "${GREEN}🎉 RAG Content Quality testing completed successfully!${NC}"
