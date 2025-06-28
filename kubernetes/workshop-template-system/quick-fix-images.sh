#!/bin/bash

# Quick fix to get the cluster running with placeholder images
# This will update all agent deployments to use a working base image temporarily

set -e

echo "🔧 Quick Fix: Updating agent images to working base images"
echo "========================================================="

# Use a working Python image that's publicly available
TEMP_IMAGE="registry.access.redhat.com/ubi8/python-39:latest"

# List of agent deployments to update
AGENTS=(
    "workshop-chat-agent"
    "template-converter-agent" 
    "content-creator-agent"
    "source-manager-agent"
    "research-validation-agent"
    "documentation-pipeline-agent"
)

echo "Updating agent deployments to use: ${TEMP_IMAGE}"

for agent in "${AGENTS[@]}"; do
    echo "Updating ${agent}..."
    oc set image deployment/${agent} ${agent}=${TEMP_IMAGE} -n workshop-system
    
    # Also add a simple command to keep the container running
    oc patch deployment ${agent} -n workshop-system --type='json' -p='[
        {
            "op": "replace", 
            "path": "/spec/template/spec/containers/0/command", 
            "value": ["python", "-c", "import time; print(\"'${agent}' placeholder running...\"); time.sleep(3600)"]
        }
    ]'
done

echo ""
echo "✅ All agent deployments updated with working images"
echo "🔄 Waiting for pods to restart..."

# Wait for rollouts
for agent in "${AGENTS[@]}"; do
    echo "Waiting for ${agent} rollout..."
    oc rollout status deployment/${agent} -n workshop-system --timeout=120s
done

echo ""
echo "🎉 Quick fix complete! All agents should be running now."
echo ""
echo "📊 Check status with:"
echo "   oc get pods -n workshop-system"
echo ""
echo "🏗️ Next steps:"
echo "   1. Set your Quay organization: export QUAY_ORG=your-actual-org"
echo "   2. Login to Quay: podman login quay.io"
echo "   3. Build proper images: ./build-containers.sh"
echo ""
