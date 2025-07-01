#!/bin/bash

# Start Workshop Agent Network with A2A Protocol Communication
# Tests real agent integration with Quarkus middleware

set -e

echo "🚀 Starting Workshop Agent Network with A2A Protocol"
echo "===================================================="

# Configuration
AGENT_IMAGE="localhost/workshop-agent-system:test-fix"
NETWORK="workshop-rag-network"
MILVUS_ENDPOINT="http://workshop-milvus:19530"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to start an agent
start_agent() {
    local agent_name=$1
    local port=$2
    local container_name="workshop-${agent_name}-agent"
    
    echo -e "${BLUE}🔧 Starting ${agent_name} agent on port ${port}...${NC}"
    
    podman run -d \
        --name "$container_name" \
        --network "$NETWORK" \
        -p "${port}:80" \
        -e MILVUS_ENDPOINT="$MILVUS_ENDPOINT" \
        -e RAG_ENABLED=true \
        -e VDB_PROVIDER=milvus \
        -e AGENT_NAME="$agent_name" \
        -e AGENT_PORT=80 \
        -e A2A_PROTOCOL_ENABLED=true \
        -e MIDDLEWARE_ENDPOINT="http://host.containers.internal:8086" \
        "$AGENT_IMAGE"
    
    # Wait a moment for startup
    sleep 3
    
    # Test agent health
    if curl -s "http://localhost:${port}/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ ${agent_name} agent started successfully${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  ${agent_name} agent starting (may take a moment)${NC}"
        return 1
    fi
}

# Function to test A2A protocol
test_a2a_protocol() {
    local agent_name=$1
    local port=$2
    local tool_name=$3
    local test_params=$4
    
    echo -e "${BLUE}🧪 Testing A2A protocol for ${agent_name}...${NC}"
    
    local a2a_request=$(cat <<EOF
{
  "tool_name": "$tool_name",
  "parameters": $test_params
}
EOF
    )
    
    response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "http://localhost:${port}/invoke" \
        -H "Content-Type: application/json" \
        -d "$a2a_request")
    
    http_status=$(echo "$response" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$http_status" = "200" ]; then
        echo -e "${GREEN}✅ A2A protocol working for ${agent_name}${NC}"
        return 0
    else
        echo -e "${RED}❌ A2A protocol failed for ${agent_name} (HTTP $http_status)${NC}"
        echo "   Response: $response_body"
        return 1
    fi
}

# Check prerequisites
echo "🔧 Checking prerequisites..."

# Check if RAG stack is running
if ! podman ps | grep -q workshop-milvus; then
    echo -e "${RED}❌ RAG stack not running. Start with: ./scripts/start-rag-stack-local.sh${NC}"
    exit 1
fi
echo -e "${GREEN}✅ RAG stack is running${NC}"

# Check if Quarkus middleware is running
if ! curl -s "http://localhost:8086/api/pipeline/health" > /dev/null 2>&1; then
    echo -e "${RED}❌ Quarkus middleware not running${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Quarkus middleware is running${NC}"

# Clean up any existing agent containers
echo "🧹 Cleaning up existing agent containers..."
podman rm -f workshop-chat-agent workshop-content-creator-agent workshop-template-converter-agent \
    workshop-source-manager-agent workshop-research-validation-agent workshop-documentation-pipeline-agent \
    2>/dev/null || true

echo ""
echo "🚀 Starting Agent Network"
echo "========================="

# Start agents with different ports
start_agent "chat" 8091
start_agent "content-creator" 8092  
start_agent "template-converter" 8093
start_agent "source-manager" 8094
start_agent "research-validation" 8095
start_agent "documentation-pipeline" 8096

echo ""
echo "⏳ Waiting for all agents to fully initialize..."
sleep 10

echo ""
echo "🧪 Testing A2A Protocol Communication"
echo "====================================="

# Test each agent with A2A protocol
test_a2a_protocol "chat" 8091 "setup_workshop_rag_tool" '{"workshop_name": "test", "workshop_content": "test content"}'

test_a2a_protocol "content-creator" 8092 "clone_showroom_template_tool" '{"template_name": "showroom_template_default", "workshop_name": "test-workshop"}'

test_a2a_protocol "template-converter" 8093 "analyze_repository_tool" '{"repository_url": "https://github.com/example/repo"}'

test_a2a_protocol "source-manager" 8094 "manage_workshop_repository_tool" '{"repository_name": "test-repo", "workshop_content": "test"}'

test_a2a_protocol "research-validation" 8095 "validate_workshop_content_tool" '{"workshop_content": "test content", "validation_scope": "basic"}'

test_a2a_protocol "documentation-pipeline" 8096 "generate_workshop_documentation_tool" '{"workshop_content": "test", "documentation_type": "basic"}'

echo ""
echo "📊 Agent Network Status"
echo "======================="
echo -e "${GREEN}✅ Agent Network Started Successfully${NC}"
echo ""
echo "🔗 Agent Endpoints:"
echo "   • Workshop Chat:           http://localhost:8091"
echo "   • Content Creator:         http://localhost:8092"
echo "   • Template Converter:      http://localhost:8093"
echo "   • Source Manager:          http://localhost:8094"
echo "   • Research Validation:     http://localhost:8095"
echo "   • Documentation Pipeline:  http://localhost:8096"
echo ""
echo "🎯 Next Steps:"
echo "1. Test middleware → agent integration"
echo "2. Run complete workshop creation workflow"
echo "3. Test RAG content quality with real agents"
echo "4. Validate human oversight workflows"
echo ""
echo -e "${GREEN}🎉 Agent Network ready for A2A protocol testing!${NC}"
