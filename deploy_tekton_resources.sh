#!/bin/bash

# Deploy Tekton Resources to Existing OpenShift Pipelines Installation
# Script: deploy_tekton_resources.sh
# Purpose: Deploy our custom Tekton pipelines and tasks to workshop-system namespace
# Reference: docs/TEKTON_TESTING_PLAN.md - Phase 2

set -e

echo "🚀 Deploying Tekton Resources to OpenShift"
echo "=========================================="
echo "Cluster: apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
echo "Namespace: workshop-system"
echo "Operator: openshift-pipelines-operator-rh.v1.18.1 (already installed)"
echo "Date: $(date)"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check command success
check_status() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
        return 0
    else
        echo -e "${RED}❌ $1${NC}"
        return 1
    fi
}

echo "📋 Deploying Custom Tekton Resources"
echo "===================================="

# 1. Verify OpenShift Pipelines operator is ready
echo ""
echo -e "${BLUE}1. Verifying OpenShift Pipelines operator status...${NC}"

OPERATOR_STATUS=$(oc get csv -n openshift-operators openshift-pipelines-operator-rh.v1.18.1 -o jsonpath='{.status.phase}' 2>/dev/null || echo "NotFound")
if [ "$OPERATOR_STATUS" = "Succeeded" ]; then
    echo -e "${GREEN}✅ OpenShift Pipelines operator v1.18.1 is ready${NC}"
else
    echo -e "${RED}❌ OpenShift Pipelines operator not ready: $OPERATOR_STATUS${NC}"
    exit 1
fi

TEKTON_CONFIG_STATUS=$(oc get tektonconfig config -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}' 2>/dev/null || echo "NotFound")
if [ "$TEKTON_CONFIG_STATUS" = "True" ]; then
    echo -e "${GREEN}✅ TektonConfig is ready${NC}"
else
    echo -e "${RED}❌ TektonConfig not ready${NC}"
    exit 1
fi

# 2. Ensure workshop-system namespace exists
echo ""
echo -e "${BLUE}2. Ensuring workshop-system namespace exists...${NC}"
oc project workshop-system > /dev/null 2>&1
check_status "workshop-system namespace accessible"

# 3. Deploy using Kustomize
echo ""
echo -e "${BLUE}3. Deploying Tekton resources using Kustomize...${NC}"

if [ -d "kubernetes/tekton/overlays/workshop-system" ]; then
    echo "   Using Kustomize overlay for workshop-system..."
    oc apply -k kubernetes/tekton/overlays/workshop-system
    check_status "Kustomize deployment completed"
else
    echo "   Using base Kustomize configuration..."
    oc apply -k kubernetes/tekton/base
    check_status "Base Kustomize deployment completed"
fi

# 4. Verify deployment
echo ""
echo -e "${BLUE}4. Verifying deployed resources...${NC}"

# Check tasks
echo "   Checking deployed tasks:"
DEPLOYED_TASKS=$(oc get tasks -n workshop-system --no-headers 2>/dev/null | wc -l)
echo "   - Tasks deployed: $DEPLOYED_TASKS"

if [ $DEPLOYED_TASKS -gt 0 ]; then
    echo "   Task list:"
    oc get tasks -n workshop-system --no-headers | awk '{print "     - " $1}'
fi

# Check pipelines
echo "   Checking deployed pipelines:"
DEPLOYED_PIPELINES=$(oc get pipelines -n workshop-system --no-headers 2>/dev/null | wc -l)
echo "   - Pipelines deployed: $DEPLOYED_PIPELINES"

if [ $DEPLOYED_PIPELINES -gt 0 ]; then
    echo "   Pipeline list:"
    oc get pipelines -n workshop-system --no-headers | awk '{print "     - " $1}'
fi

# Check ConfigMaps
echo "   Checking Tekton ConfigMaps:"
TEKTON_CONFIGMAPS=$(oc get configmaps -n workshop-system | grep tekton | wc -l)
echo "   - Tekton ConfigMaps: $TEKTON_CONFIGMAPS"

# 5. Test basic functionality
echo ""
echo -e "${BLUE}5. Testing basic Tekton functionality...${NC}"

# Create a simple test TaskRun
TEST_TASKRUN_NAME="test-tekton-integration-$(date +%s)"
echo "   Creating test TaskRun: $TEST_TASKRUN_NAME"

cat <<EOF | oc apply -f - -n workshop-system
apiVersion: tekton.dev/v1beta1
kind: TaskRun
metadata:
  name: $TEST_TASKRUN_NAME
  labels:
    app: workshop-template-system
    test: tekton-integration
spec:
  taskSpec:
    steps:
    - name: test-step
      image: registry.access.redhat.com/ubi8/ubi-minimal:latest
      script: |
        #!/bin/bash
        echo "✅ Tekton integration test successful"
        echo "Cluster: apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
        echo "Namespace: workshop-system"
        echo "Operator: openshift-pipelines-operator-rh.v1.18.1"
        echo "Date: $(date)"
        echo "Tasks available: $DEPLOYED_TASKS"
        echo "Pipelines available: $DEPLOYED_PIPELINES"
EOF

check_status "Test TaskRun created: $TEST_TASKRUN_NAME"

# Wait a moment and check TaskRun status
echo "   Waiting for TaskRun to start..."
sleep 5

TASKRUN_STATUS=$(oc get taskrun $TEST_TASKRUN_NAME -n workshop-system -o jsonpath='{.status.conditions[?(@.type=="Succeeded")].status}' 2>/dev/null || echo "Unknown")
echo "   TaskRun status: $TASKRUN_STATUS"

# 6. Summary
echo ""
echo "📊 Tekton Resources Deployment Summary"
echo "======================================"

if [ $DEPLOYED_TASKS -ge 6 ] && [ $DEPLOYED_PIPELINES -ge 2 ]; then
    echo -e "${GREEN}🎉 Tekton resources deployment SUCCESSFUL${NC}"
    echo ""
    echo "✅ OpenShift Pipelines operator: v1.18.1 (ready)"
    echo "✅ TektonConfig: ready"
    echo "✅ Tekton tasks deployed: $DEPLOYED_TASKS"
    echo "✅ Tekton pipelines deployed: $DEPLOYED_PIPELINES"
    echo "✅ Test TaskRun: $TEST_TASKRUN_NAME"
    echo ""
    echo -e "${GREEN}🚀 Ready for testing!${NC}"
    echo "   Next step: Run ./validate_cluster_environment.sh"
    echo "   Then run: python3 test_e2e_tekton_human_oversight.py"
else
    echo -e "${YELLOW}⚠️ Tekton resources deployment PARTIAL${NC}"
    echo ""
    echo "📊 Deployment status:"
    echo "   - Tasks deployed: $DEPLOYED_TASKS (expected: 8)"
    echo "   - Pipelines deployed: $DEPLOYED_PIPELINES (expected: 2)"
    echo ""
    echo "Please check the deployment and retry if needed."
fi

echo ""
echo "📋 For detailed testing plan, see: docs/TEKTON_TESTING_PLAN.md"
echo "🔧 To validate complete environment, run: ./validate_cluster_environment.sh"
echo "🕒 Deployment completed at: $(date)"
