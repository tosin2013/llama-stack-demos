#!/bin/bash

# Test Workshop Maintenance Pipeline
# Tests the new workshop maintenance functionality with human-in-the-loop approval

set -e

echo "🔧 Testing Workshop Maintenance Pipeline"
echo "========================================"

# Configuration
NAMESPACE="workshop-system"
PIPELINE_NAME="workshop-maintenance-pipeline"
REPOSITORY_NAME="workshop-ddd-hexagonal-workshop-demo-1751149405809"  # Existing workshop
WORKSHOP_NAME="ddd-hexagonal-workshop-maintenance"
UPDATE_TYPE="content-update"
SOURCE_REPO="https://github.com/jeremyrdavis/dddhexagonalworkshop.git"
APPROVER="workshop-system-operator"
AUTO_APPROVE="false"  # Set to true for automated testing

echo "📋 Test Configuration:"
echo "  Namespace: $NAMESPACE"
echo "  Pipeline: $PIPELINE_NAME"
echo "  Repository: $REPOSITORY_NAME"
echo "  Workshop: $WORKSHOP_NAME"
echo "  Update Type: $UPDATE_TYPE"
echo "  Source Repo: $SOURCE_REPO"
echo "  Approver: $APPROVER"
echo "  Auto Approve: $AUTO_APPROVE"
echo ""

echo "🚀 Workshop maintenance pipeline with human-in-the-loop approval ready for testing!"
echo ""
echo "📋 Next Steps:"
echo "1. Deploy the pipeline and task:"
echo "   oc apply -f kubernetes/tekton/tasks/workshop-maintenance-task.yaml"
echo "   oc apply -f kubernetes/tekton/pipelines/workshop-maintenance-pipeline.yaml"
echo ""
echo "2. Test the middleware endpoints:"
echo "   curl -X POST http://workshop-monitoring-service:8080/api/pipeline/source-manager/update-workshop \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"repository_name\":\"test-repo\",\"workshop_name\":\"test-workshop\"}'"
echo ""
echo "3. Run the maintenance pipeline:"
echo "   tkn pipeline start workshop-maintenance-pipeline \\"
echo "     --param repository-name=\"$REPOSITORY_NAME\" \\"
echo "     --param workshop-name=\"$WORKSHOP_NAME\" \\"
echo "     --param update-type=\"$UPDATE_TYPE\" \\"
echo "     --param source-repository-url=\"$SOURCE_REPO\" \\"
echo "     --workspace name=shared-data,claimName=shared-workspace-storage \\"
echo "     --namespace=\"$NAMESPACE\""
echo ""
echo "✅ Workshop maintenance infrastructure ready!"
