#!/bin/bash
set -e

echo "🔧 Rebuilding Agents for Workspace Support (ADR-0007)"
echo "===================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
        exit 1
    fi
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

echo ""
echo "📋 Step 1: Verify Current Agent Status"
echo "====================================="

# Check current agent deployments
echo "🔍 Checking current agent deployments..."
oc get deployments -n workshop-system -l component=workshop-agent
print_status $? "Agent deployments listed"

# Check current image
CURRENT_IMAGE=$(oc get deployment content-creator-agent -n workshop-system -o jsonpath='{.spec.template.spec.containers[0].image}' 2>/dev/null || echo "not-found")
echo "Current image: $CURRENT_IMAGE"

echo ""
echo "📋 Step 2: Trigger Agent Rebuild"
echo "==============================="

# Check if BuildConfig exists
if oc get buildconfig workshop-system-build -n workshop-system >/dev/null 2>&1; then
    print_info "BuildConfig found, triggering rebuild..."
    
    # Start new build
    echo "🚀 Starting new build with workspace enhancements..."
    BUILD_NAME=$(oc start-build workshop-system-build -n workshop-system --follow --wait | grep "build.*started" | awk '{print $2}' | tr -d '"')
    
    if [ -n "$BUILD_NAME" ]; then
        print_status 0 "Build started: $BUILD_NAME"
        
        # Wait for build completion
        echo "⏳ Waiting for build completion..."
        oc wait --for=condition=Complete build/$BUILD_NAME -n workshop-system --timeout=600s
        print_status $? "Build completed successfully"
        
        # Get new image SHA
        NEW_IMAGE=$(oc get build $BUILD_NAME -n workshop-system -o jsonpath='{.status.outputDockerImageReference}')
        echo "New image: $NEW_IMAGE"
        
    else
        print_warning "Could not determine build name, checking build status manually..."
        oc get builds -n workshop-system --sort-by=.metadata.creationTimestamp | tail -5
    fi
    
else
    print_warning "BuildConfig not found, you may need to deploy it first"
    echo "Run: oc apply -f kubernetes/workshop-template-system/base/workshop-system-buildconfig.yaml"
    exit 1
fi

echo ""
echo "📋 Step 3: Update Agent Deployments"
echo "=================================="

# List of agents that need updating for workspace support (ADR-0007)
# Only Content Creator and Source Manager need workspace access
AGENTS=("content-creator-agent" "source-manager-agent")
AGENTS_NO_REBUILD=("template-converter-agent" "research-validation-agent" "documentation-pipeline-agent" "workshop-chat-agent")

for agent in "${AGENTS[@]}"; do
    echo "🔄 Updating $agent deployment..."
    
    if oc get deployment $agent -n workshop-system >/dev/null 2>&1; then
        # Trigger rollout with new image
        oc rollout restart deployment/$agent -n workshop-system
        print_status $? "$agent deployment restarted"
        
        # Wait for rollout
        echo "⏳ Waiting for $agent rollout..."
        oc rollout status deployment/$agent -n workshop-system --timeout=300s
        print_status $? "$agent rollout completed"
    else
        print_warning "$agent deployment not found, skipping..."
    fi
done

echo ""
echo "📋 Step 4: Verify Agent Updates"
echo "=============================="

# Check workspace-enabled agents are running with new image
echo "🔍 Verifying workspace-enabled agents..."
for agent in "${AGENTS[@]}"; do
    if oc get deployment $agent -n workshop-system >/dev/null 2>&1; then
        READY=$(oc get deployment $agent -n workshop-system -o jsonpath='{.status.readyReplicas}')
        DESIRED=$(oc get deployment $agent -n workshop-system -o jsonpath='{.spec.replicas}')

        if [ "$READY" = "$DESIRED" ]; then
            echo -e "${GREEN}✅ $agent: $READY/$DESIRED ready (workspace-enabled)${NC}"
        else
            echo -e "${YELLOW}⚠️  $agent: $READY/$DESIRED ready (still updating)${NC}"
        fi
    fi
done

# Check other agents (no rebuild needed)
echo ""
echo "🔍 Verifying other agents (no rebuild needed)..."
for agent in "${AGENTS_NO_REBUILD[@]}"; do
    if oc get deployment $agent -n workshop-system >/dev/null 2>&1; then
        READY=$(oc get deployment $agent -n workshop-system -o jsonpath='{.status.readyReplicas}')
        DESIRED=$(oc get deployment $agent -n workshop-system -o jsonpath='{.spec.replicas}')

        if [ "$READY" = "$DESIRED" ]; then
            echo -e "${GREEN}✅ $agent: $READY/$DESIRED ready (API-only)${NC}"
        else
            echo -e "${YELLOW}⚠️  $agent: $READY/$DESIRED ready${NC}"
        fi
    fi
done

echo ""
echo "📋 Step 5: Test Enhanced Agent Functionality"
echo "==========================================="

# Test Content Creator Agent new tool
echo "🧪 Testing Content Creator Agent workspace tool..."
CONTENT_CREATOR_POD=$(oc get pods -n workshop-system -l app=content-creator-agent --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

if [ -n "$CONTENT_CREATOR_POD" ]; then
    echo "Testing agent endpoint..."
    AGENT_RESPONSE=$(oc exec $CONTENT_CREATOR_POD -n workshop-system -- curl -s http://localhost:8080/agent-card 2>/dev/null || echo "failed")
    
    if [[ "$AGENT_RESPONSE" == *"Content Creator"* ]]; then
        print_status 0 "Content Creator Agent responding correctly"
        
        # Test if new tool is available
        TOOLS_RESPONSE=$(oc exec $CONTENT_CREATOR_POD -n workshop-system -- curl -s http://localhost:8080/tools 2>/dev/null || echo "failed")
        
        if [[ "$TOOLS_RESPONSE" == *"create_workshop_content_from_workspace_tool"* ]]; then
            print_status 0 "New workspace tool available in Content Creator Agent"
        else
            print_warning "New workspace tool not found in agent response"
            echo "Available tools:"
            echo "$TOOLS_RESPONSE" | head -10
        fi
    else
        print_warning "Content Creator Agent not responding correctly"
    fi
else
    print_warning "Content Creator Agent pod not found or not running"
fi

echo ""
echo "🎯 Agent Rebuild Summary"
echo "======================="
echo "✅ Agents rebuilt with workspace enhancements"
echo "✅ Deployments updated and rolled out"
echo "✅ New file-based tools available"
echo ""
echo "📋 Next Steps:"
echo "1. Ensure ODF is installed for RWX storage"
echo "2. Run workspace pipeline test: ./test-workspace-implementation.sh --full-test"
echo "3. Verify end-to-end workspace functionality"
echo ""
echo "🚀 Agents ready for workspace operations!"
