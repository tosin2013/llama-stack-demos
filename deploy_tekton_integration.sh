#!/bin/bash

# Deploy Tekton-Agent Integration to OpenShift Cluster
# Script: deploy_tekton_integration.sh
# Purpose: Deploy Tekton pipelines and tasks to existing OpenShift cluster with Tekton operator
# Reference: docs/TEKTON_TESTING_PLAN.md - Phase 2

set -e

echo "🚀 Deploying Tekton-Agent Integration to OpenShift"
echo "=================================================="
echo "Cluster: apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
echo "Namespace: workshop-system"
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

echo "📋 Phase 2: Deploy Tekton Pipelines and Tasks"
echo "============================================="

# 1. Verify OpenShift Pipelines operator is installed
echo ""
echo -e "${BLUE}1. Verifying OpenShift Pipelines operator installation...${NC}"

# Check if operator is installed
OPERATOR_STATUS=$(oc get csv -n openshift-operators | grep openshift-pipelines-operator-rh | awk '{print $4}' || echo "NotFound")
if [ "$OPERATOR_STATUS" = "Succeeded" ]; then
    echo -e "${GREEN}✅ OpenShift Pipelines operator is installed and running${NC}"
    OPERATOR_VERSION=$(oc get csv -n openshift-operators | grep openshift-pipelines-operator-rh | awk '{print $3}')
    echo "   Version: $OPERATOR_VERSION"
else
    echo -e "${RED}❌ OpenShift Pipelines operator not found or not ready${NC}"
    echo "   Please install the operator first"
    exit 1
fi

# Check TektonConfig
TEKTON_CONFIG_STATUS=$(oc get tektonconfig config -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}' 2>/dev/null || echo "NotFound")
if [ "$TEKTON_CONFIG_STATUS" = "True" ]; then
    echo -e "${GREEN}✅ TektonConfig is ready${NC}"
    TEKTON_VERSION=$(oc get tektonconfig config -o jsonpath='{.status.version}')
    echo "   Tekton Version: $TEKTON_VERSION"
else
    echo -e "${YELLOW}⚠️ TektonConfig not ready, but proceeding...${NC}"
fi

# 2. Ensure workshop-system namespace exists
echo ""
echo -e "${BLUE}2. Ensuring workshop-system namespace exists...${NC}"
oc project workshop-system > /dev/null 2>&1
check_status "workshop-system namespace accessible"

# 3. Deploy Tekton tasks
echo ""
echo -e "${BLUE}3. Deploying Tekton agent tasks...${NC}"

TASK_FILES=(
    "kubernetes/tekton/tasks/agent-task-template-converter.yaml"
    "kubernetes/tekton/tasks/agent-task-content-creator.yaml"
    "kubernetes/tekton/tasks/agent-task-source-manager.yaml"
    "kubernetes/tekton/tasks/agent-task-research-validation.yaml"
    "kubernetes/tekton/tasks/agent-task-documentation-pipeline.yaml"
    "kubernetes/tekton/tasks/agent-task-workshop-chat.yaml"
    "kubernetes/tekton/tasks/human-oversight-approval.yaml"
    "kubernetes/tekton/tasks/openshift-buildconfig-trigger.yaml"
)

for task_file in "${TASK_FILES[@]}"; do
    if [ -f "$task_file" ]; then
        echo "   Deploying $(basename $task_file)..."
        oc apply -f "$task_file" -n workshop-system
        check_status "$(basename $task_file) deployed"
    else
        echo -e "${YELLOW}⚠️ Task file not found: $task_file${NC}"
    fi
done

# 4. Deploy Tekton pipelines
echo ""
echo -e "${BLUE}4. Deploying Tekton pipelines...${NC}"

PIPELINE_FILES=(
    "kubernetes/tekton/pipelines/workflow-1-new-workshop.yaml"
    "kubernetes/tekton/pipelines/workflow-3-enhance-workshop.yaml"
)

for pipeline_file in "${PIPELINE_FILES[@]}"; do
    if [ -f "$pipeline_file" ]; then
        echo "   Deploying $(basename $pipeline_file)..."
        oc apply -f "$pipeline_file" -n workshop-system
        check_status "$(basename $pipeline_file) deployed"
    else
        echo -e "${YELLOW}⚠️ Pipeline file not found: $pipeline_file${NC}"
    fi
done

# 5. Verify deployment
echo ""
echo -e "${BLUE}5. Verifying Tekton resource deployment...${NC}"

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

# 6. Test basic functionality
echo ""
echo -e "${BLUE}6. Testing basic Tekton functionality...${NC}"

# Test if we can create a simple TaskRun
echo "   Testing TaskRun creation capability..."
cat <<EOF | oc apply -f - -n workshop-system
apiVersion: tekton.dev/v1beta1
kind: TaskRun
metadata:
  name: test-tekton-integration-$(date +%s)
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
        echo "Date: $(date)"
EOF

check_status "Test TaskRun created successfully"

# 7. Summary and next steps
echo ""
echo "📊 Tekton Integration Deployment Summary"
echo "========================================"

if [ $DEPLOYED_TASKS -ge 8 ] && [ $DEPLOYED_PIPELINES -ge 2 ]; then
    echo -e "${GREEN}🎉 Tekton integration deployment SUCCESSFUL${NC}"
    echo ""
    echo "✅ OpenShift Pipelines operator: $OPERATOR_VERSION"
    echo "✅ Tekton tasks deployed: $DEPLOYED_TASKS/8"
    echo "✅ Tekton pipelines deployed: $DEPLOYED_PIPELINES/2"
    echo "✅ Test TaskRun: Created successfully"
    echo ""
    echo -e "${GREEN}🚀 Ready for testing!${NC}"
    echo "   Next step: Run enhanced test_e2e_tekton_human_oversight.py"
    echo "   Command: python3 test_e2e_tekton_human_oversight.py --cluster-mode"
else
    echo -e "${YELLOW}⚠️ Tekton integration deployment PARTIAL${NC}"
    echo ""
    echo "📊 Deployment status:"
    echo "   - Tasks deployed: $DEPLOYED_TASKS/8"
    echo "   - Pipelines deployed: $DEPLOYED_PIPELINES/2"
    echo ""
    echo "Please check the deployment logs and retry if needed."
fi

echo ""
echo "📋 For detailed testing plan, see: docs/TEKTON_TESTING_PLAN.md"
echo "🔧 To validate environment, run: ./validate_cluster_environment.sh"
echo "🕒 Deployment completed at: $(date)"
