#!/bin/bash

# Test Complete Workshop Creation Workflow with PhiloAgents Course
# Tests the transformation of a regular repository into a workshop

set -e

echo "🎓 Testing Workshop Creation from PhiloAgents Course Repository"
echo "=============================================================="

# Configuration
MIDDLEWARE_URL="http://localhost:8086"
REPO_URL="https://github.com/neural-maze/philoagents-course"
WORKSHOP_NAME="philoagents-ai-philosophy-workshop"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to test endpoint
test_step() {
    local step_name=$1
    local endpoint=$2
    local data=$3
    
    echo ""
    echo -e "${BLUE}📋 Step: $step_name${NC}"
    echo "   Endpoint: $endpoint"
    
    response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$endpoint" \
        -H "Content-Type: application/json" \
        -d "$data")
    
    http_status=$(echo "$response" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$http_status" = "200" ]; then
        echo -e "${GREEN}✅ SUCCESS${NC}"
        echo "   Response: $(echo "$response_body" | jq -r '.status // "unknown"')"
        
        # Extract key information
        if echo "$response_body" | jq -e '.workshop_type' > /dev/null 2>&1; then
            workshop_type=$(echo "$response_body" | jq -r '.workshop_type')
            echo -e "${GREEN}   📚 Workshop Type: $workshop_type${NC}"
        fi
        
        if echo "$response_body" | jq -e '.files_created' > /dev/null 2>&1; then
            files_created=$(echo "$response_body" | jq -r '.files_created')
            echo -e "${GREEN}   📁 Files Created: $files_created${NC}"
        fi
        
        if echo "$response_body" | jq -e '.quality_score_after' > /dev/null 2>&1; then
            quality_score=$(echo "$response_body" | jq -r '.quality_score_after')
            echo -e "${GREEN}   📊 Quality Score: $quality_score${NC}"
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
if ! curl -s "$MIDDLEWARE_URL/api/pipeline/health" > /dev/null 2>&1; then
    echo -e "${RED}❌ Quarkus middleware not running at $MIDDLEWARE_URL${NC}"
    echo "   Start with: cd workshop-monitoring-service && mvn quarkus:dev"
    exit 1
fi
echo -e "${GREEN}✅ Quarkus middleware is running${NC}"

echo ""
echo "🚀 Starting Workshop Creation Workflow"
echo "======================================"

# Step 1: Analyze Repository
echo -e "${BLUE}Phase 1: Repository Analysis${NC}"
analyze_data=$(cat <<EOF
{
  "repository_url": "$REPO_URL",
  "analysis_depth": "comprehensive",
  "target_format": "rhpds_showroom"
}
EOF
)

test_step "Analyze PhiloAgents Repository" \
    "$MIDDLEWARE_URL/api/pipeline/mock/template-converter/analyze-repository" \
    "$analyze_data"

# Step 2: Create Workshop Content
echo -e "${BLUE}Phase 2: Workshop Content Creation${NC}"
create_data=$(cat <<EOF
{
  "workshop_name": "$WORKSHOP_NAME",
  "repository_url": "$REPO_URL",
  "base_template": "showroom_template_default",
  "technology_focus": "AI Agents and Philosophy",
  "customization_level": "comprehensive"
}
EOF
)

test_step "Create Workshop Content" \
    "$MIDDLEWARE_URL/api/pipeline/mock/content-creator/create-workshop" \
    "$create_data"

# Step 3: Validate Content Quality
echo -e "${BLUE}Phase 3: Content Validation${NC}"
validate_data=$(cat <<EOF
{
  "workshop_name": "$WORKSHOP_NAME",
  "workshop_content": "Generated workshop content about AI agents and philosophy",
  "validation_scope": "comprehensive"
}
EOF
)

test_step "Validate Workshop Content" \
    "$MIDDLEWARE_URL/api/pipeline/mock/research-validation/validate-content" \
    "$validate_data"

# Step 4: Create Repository
echo -e "${BLUE}Phase 4: Repository Creation${NC}"
repo_data=$(cat <<EOF
{
  "repository_name": "$WORKSHOP_NAME",
  "workshop_content": "Generated workshop content",
  "gitea_url": "http://gitea-example.com",
  "visibility": "public"
}
EOF
)

test_step "Create Workshop Repository" \
    "$MIDDLEWARE_URL/api/pipeline/mock/source-manager/create-repository" \
    "$repo_data"

# Step 5: Generate Documentation
echo -e "${BLUE}Phase 5: Documentation Generation${NC}"
docs_data=$(cat <<EOF
{
  "workshop_name": "$WORKSHOP_NAME",
  "workshop_content": "Generated workshop content",
  "documentation_type": "comprehensive"
}
EOF
)

test_step "Generate Workshop Documentation" \
    "$MIDDLEWARE_URL/api/pipeline/mock/documentation-pipeline/generate-docs" \
    "$docs_data"

# Step 6: Setup RAG for Workshop Chat
echo -e "${BLUE}Phase 6: RAG Setup for Workshop Chat${NC}"
rag_data=$(cat <<EOF
{
  "workshop_name": "$WORKSHOP_NAME",
  "workshop_content": "Generated workshop content",
  "rag_configuration": "default"
}
EOF
)

test_step "Setup Workshop RAG" \
    "$MIDDLEWARE_URL/api/pipeline/mock/workshop-chat/setup-rag" \
    "$rag_data"

# Summary
echo ""
echo "📊 Workshop Creation Summary"
echo "============================"
echo -e "${GREEN}✅ Complete Workshop Creation Workflow Tested${NC}"
echo ""
echo "🎯 Workflow Steps Completed:"
echo "   ✅ Repository Analysis (PhiloAgents Course)"
echo "   ✅ Workshop Content Creation"
echo "   ✅ Content Quality Validation"
echo "   ✅ Repository Creation in Gitea"
echo "   ✅ Documentation Generation"
echo "   ✅ RAG Setup for Workshop Chat"
echo ""
echo "📋 Repository Transformation:"
echo "   📚 Source: $REPO_URL"
echo "   🎓 Workshop: $WORKSHOP_NAME"
echo "   🎯 Focus: AI Agents and Philosophy"
echo "   📖 Format: RHPDS Showroom Template"
echo ""
echo "🔄 Next Steps:"
echo "1. Test with real agents (start agent network)"
echo "2. Test workshop maintenance workflow"
echo "3. Test RAG content quality workflow"
echo "4. Deploy to OpenShift for full integration"
echo ""
echo -e "${GREEN}🎉 PhiloAgents Course → Workshop transformation tested successfully!${NC}"
