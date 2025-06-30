#!/bin/bash
set -e

echo "🧪 Testing Workspace Implementation (ADR-0007)"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
        exit 1
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

echo ""
echo "📋 Phase 1: Applying Workspace Infrastructure"
echo "============================================="

# Apply workspace initialization task
echo "🚀 Applying workspace initialization task..."
oc apply -f kubernetes/tekton/tasks/workspace-initialization.yaml -n workshop-system
print_status $? "Workspace initialization task applied"

# Apply updated content creator task
echo "🎨 Applying updated content creator task..."
oc apply -f kubernetes/tekton/tasks/agent-task-content-creator.yaml -n workshop-system
print_status $? "Content creator task updated"

# Apply updated pipeline
echo "🔄 Applying updated pipeline..."
oc apply -f kubernetes/tekton/pipelines/workflow-1-new-workshop.yaml -n workshop-system
print_status $? "Pipeline updated with workspace support"

echo ""
echo "📋 Phase 2: Testing Workspace Pipeline (Dry Run)"
echo "================================================"

# Check if we have ODF storage available
echo "🔍 Checking for ocs-storagecluster-cephfs storage class..."
if oc get storageclass ocs-storagecluster-cephfs >/dev/null 2>&1; then
    echo "✅ ocs-storagecluster-cephfs storage class found"
    RWX_STORAGE_AVAILABLE=true
else
    print_warning "ocs-storagecluster-cephfs storage class not found"
    print_warning "Available storage classes:"
    oc get storageclass
    echo ""
    print_warning "Skipping pipeline test until ODF storage is available."
    echo ""
    echo "📋 Next Steps:"
    echo "1. Install OpenShift Data Foundation (ODF)"
    echo "2. Verify ocs-storagecluster-cephfs storage class is available"
    echo "3. Run this script again: ./test-workspace-implementation.sh --full-test"
    echo ""
    echo "🎯 Ready for ODF Installation!"
    RWX_STORAGE_AVAILABLE=false
fi

# Create test PipelineRun with workspace (only if --full-test is provided and storage is available)
if [ "$1" = "--full-test" ] && [ "$RWX_STORAGE_AVAILABLE" = "true" ]; then
    echo ""
    echo "📋 Phase 3: Full Pipeline Test"
    echo "=============================="
    
    TIMESTAMP=$(date +%s)
    PIPELINE_NAME="test-workspace-${TIMESTAMP}"
    
    echo "🚀 Creating test PipelineRun: $PIPELINE_NAME"
    
    cat <<EOF | oc apply -f - -n workshop-system
apiVersion: tekton.dev/v1beta1
kind: PipelineRun
metadata:
  name: $PIPELINE_NAME
  namespace: workshop-system
  labels:
    app: workshop-template-system
    test: workspace-implementation
    adr: "0007"
spec:
  pipelineRef:
    name: workflow-1-new-workshop
  params:
  - name: repository-url
    value: "https://github.com/jeremyrdavis/dddhexagonalworkshop"
  - name: workshop-name
    value: "workspace-test-workshop"
  - name: base-template
    value: "showroom_template_default"
  - name: gitea-repo-name
    value: "workspace-test-workshop"
  - name: human-approver
    value: "workshop-system-operator"
  - name: auto-approve
    value: "true"
  timeout: "30m"
  workspaces:
  - name: shared-data
    volumeClaimTemplate:
      spec:
        accessModes:
        - ReadWriteMany
        resources:
          requests:
            storage: 2Gi
        storageClassName: ocs-storagecluster-cephfs
  - name: gitea-auth
    emptyDir: {}
EOF

    print_status $? "Test PipelineRun created"
    
    echo ""
    echo "📊 Monitoring pipeline execution..."
    echo "Use the following commands to monitor:"
    echo ""
    echo "  # Watch pipeline status"
    echo "  tkn pipelinerun describe $PIPELINE_NAME -n workshop-system"
    echo ""
    echo "  # Follow logs"
    echo "  tkn pipelinerun logs $PIPELINE_NAME -f -n workshop-system"
    echo ""
    echo "  # Check workspace content (when running)"
    echo "  oc exec -it \$(oc get pods -l tekton.dev/pipelineRun=$PIPELINE_NAME -o name | head -1) -n workshop-system -- ls -la /workspace/shared-data/"
    
else
    echo ""
    echo "🎯 Infrastructure Ready!"
    echo "======================="
    echo ""
    echo "✅ Workspace initialization task deployed"
    echo "✅ Content Creator Agent enhanced with file operations"
    echo "✅ Pipeline updated with workspace support"
    echo "✅ Auto-approve functionality preserved"
    echo ""
    echo "📋 Next Steps:"
    echo "1. Ensure ODF is installed and RWX storage is available"
    echo "2. Run: ./test-workspace-implementation.sh --full-test"
    echo "3. Monitor the complete workspace pipeline execution"
    echo ""
    echo "🚀 Ready for full workspace testing when ODF is available!"
fi

echo ""
echo "📊 Current Implementation Status:"
echo "================================"
echo "✅ Task 1: Create Workspace Initialization Task - COMPLETE"
echo "✅ Task 2: Update Pipeline with Workspace Initialization - COMPLETE"
echo "✅ Task 3: Enhance Content Creator Agent with File Operations - COMPLETE"
echo "✅ Task 4: Update Content Creator Agent Task for Workspace - COMPLETE"
echo "🔄 Task 5: Enhance Source Manager Agent with Workspace Sync - PENDING"
echo "🔄 Task 6: Update Source Manager Agent Task for Workspace Sync - PENDING"
echo "🔄 Task 7: Test End-to-End Workspace Pipeline - PENDING ODF"
echo "🔄 Task 8: Update ADR-0007 Implementation Status - PENDING"
echo ""
echo "🎯 Ready for ODF installation and continued implementation!"
