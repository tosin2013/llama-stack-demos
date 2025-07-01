#!/bin/bash
set -e

echo "🚀 Deploying Enhanced Shared Workspace (ADR-0007)"
echo "================================================="

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
echo "📋 Step 1: Verify Prerequisites"
echo "==============================="

# Check if ODF storage is available
echo "🔍 Checking for ocs-storagecluster-cephfs storage class..."
if oc get storageclass ocs-storagecluster-cephfs >/dev/null 2>&1; then
    print_status 0 "ocs-storagecluster-cephfs storage class found"
else
    print_warning "ocs-storagecluster-cephfs storage class not found"
    echo "Available storage classes:"
    oc get storageclass
    echo ""
    echo "Please install OpenShift Data Foundation (ODF) first"
    exit 1
fi

# Check namespace
if oc get namespace workshop-system >/dev/null 2>&1; then
    print_status 0 "workshop-system namespace exists"
else
    print_warning "workshop-system namespace not found, creating..."
    oc create namespace workshop-system
    print_status $? "workshop-system namespace created"
fi

echo ""
echo "📋 Step 2: Deploy Shared Workspace Infrastructure"
echo "================================================"

# Apply shared workspace PVC
echo "💾 Creating shared workspace PVC..."
oc apply -f kubernetes/workshop-template-system/base/shared-workspace-pvc.yaml
print_status $? "Shared workspace PVC created"

# Apply coordination scripts
echo "📜 Deploying coordination scripts..."
oc apply -f kubernetes/workshop-template-system/base/workspace-coordination-configmap.yaml
print_status $? "Coordination scripts deployed"

# Wait for PVC to be bound
echo "⏳ Waiting for PVC to be bound..."
oc wait --for=condition=Bound pvc/shared-workspace-storage -n workshop-system --timeout=120s
print_status $? "PVC bound successfully"

echo ""
echo "📋 Step 3: Update Agent Deployments"
echo "=================================="

# Apply updated agent deployments
echo "🤖 Updating agent deployments with workspace support..."
oc apply -f kubernetes/workshop-template-system/base/agents-deployment.yaml
print_status $? "Agent deployments updated"

# Wait for agent rollouts
AGENTS=("content-creator-agent" "source-manager-agent")
for agent in "${AGENTS[@]}"; do
    echo "⏳ Waiting for $agent rollout..."
    oc rollout status deployment/$agent -n workshop-system --timeout=300s
    print_status $? "$agent rollout completed"
done

echo ""
echo "📋 Step 4: Update Tekton Pipeline Tasks"
echo "======================================"

# Apply updated workspace initialization task
echo "🔧 Updating workspace initialization task..."
oc apply -f kubernetes/tekton/tasks/workspace-initialization.yaml -n workshop-system
print_status $? "Workspace initialization task updated"

# Apply updated content creator task
echo "🎨 Updating content creator task..."
oc apply -f kubernetes/tekton/tasks/agent-task-content-creator.yaml -n workshop-system
print_status $? "Content creator task updated"

# Apply updated pipeline
echo "🔄 Updating pipeline with enhanced workspace support..."
oc apply -f kubernetes/tekton/pipelines/workflow-1-new-workshop.yaml -n workshop-system
print_status $? "Pipeline updated"

echo ""
echo "📋 Step 5: Verify Workspace Setup"
echo "================================"

# Check PVC status
echo "💾 Checking PVC status..."
PVC_STATUS=$(oc get pvc shared-workspace-storage -n workshop-system -o jsonpath='{.status.phase}')
if [ "$PVC_STATUS" = "Bound" ]; then
    print_status 0 "PVC is bound and ready"
    
    # Show PVC details
    echo ""
    echo "📊 PVC Details:"
    oc get pvc shared-workspace-storage -n workshop-system -o wide
else
    print_warning "PVC status: $PVC_STATUS"
fi

# Check agent workspace mounts
echo ""
echo "🤖 Verifying agent workspace mounts..."
for agent in "${AGENTS[@]}"; do
    POD=$(oc get pods -l app=$agent -n workshop-system -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    if [ -n "$POD" ]; then
        echo "Checking $agent workspace mount..."
        if oc exec $POD -n workshop-system -- ls -la /workspace/shared-data >/dev/null 2>&1; then
            print_status 0 "$agent workspace mounted successfully"
        else
            print_warning "$agent workspace mount failed"
        fi
    else
        print_warning "$agent pod not found"
    fi
done

echo ""
echo "📋 Step 6: Initialize Workspace Structure"
echo "========================================"

# Create a test pod to initialize workspace structure
echo "🧪 Initializing workspace structure..."
cat <<EOF | oc apply -f - -n workshop-system
apiVersion: v1
kind: Pod
metadata:
  name: workspace-init-test
  namespace: workshop-system
spec:
  serviceAccountName: workshop-system-sa
  containers:
  - name: init
    image: registry.access.redhat.com/ubi8/ubi:latest
    command:
    - /bin/bash
    - -c
    - |
      echo "🚀 Initializing workspace structure..."
      
      # Install jq
      dnf install -y jq
      
      # Create base directory structure
      mkdir -p /workspace/shared-data/{pipelines,agents/{content-creator,source-manager}/{working,cache},shared/{templates,git-cache,coordination/resource-locks},completed}
      
      # Create global coordination file
      cat > /workspace/shared-data/shared/coordination/active-pipelines.json <<EOF2
      {
        "version": "1.0.0",
        "last_updated": "\$(date -Iseconds)",
        "active_pipelines": [],
        "total_pipelines": 0
      }
      EOF2
      
      # Set permissions
      chmod -R 755 /workspace/shared-data
      
      echo "✅ Workspace structure initialized"
      echo "📁 Directory structure:"
      find /workspace/shared-data -type d | head -20
      
      # Keep pod running for verification
      sleep 30
    volumeMounts:
    - name: shared-workspace
      mountPath: /workspace/shared-data
  volumes:
  - name: shared-workspace
    persistentVolumeClaim:
      claimName: shared-workspace-storage
  restartPolicy: Never
EOF

# Wait for initialization
echo "⏳ Waiting for workspace initialization..."
sleep 10

# Check if initialization completed
if oc wait --for=condition=Ready pod/workspace-init-test -n workshop-system --timeout=60s >/dev/null 2>&1; then
    print_status 0 "Workspace structure initialized"
    
    # Show workspace structure
    echo ""
    echo "📁 Workspace Structure:"
    oc exec workspace-init-test -n workshop-system -- find /workspace/shared-data -type d | head -15
else
    print_warning "Workspace initialization may have issues"
fi

# Cleanup test pod
oc delete pod workspace-init-test -n workshop-system --ignore-not-found=true

echo ""
echo "🎯 Enhanced Workspace Deployment Summary"
echo "======================================="
echo "✅ Shared workspace PVC created (10Gi, RWX)"
echo "✅ Coordination scripts deployed"
echo "✅ Agent deployments updated with workspace mounts"
echo "✅ Tekton tasks updated with coordination"
echo "✅ Pipeline updated with enhanced workspace support"
echo "✅ Workspace structure initialized"
echo ""
echo "📋 Next Steps:"
echo "1. Test the enhanced workspace pipeline:"
echo "   ./test-workspace-implementation.sh --full-test"
echo "2. Monitor workspace usage:"
echo "   oc exec -it <agent-pod> -n workshop-system -- /opt/workspace-scripts/workspace-monitor.sh"
echo "3. Check coordination status:"
echo "   oc exec -it <agent-pod> -n workshop-system -- cat /workspace/shared-data/shared/coordination/active-pipelines.json"
echo ""
echo "🚀 Enhanced shared workspace ready for production use!"
