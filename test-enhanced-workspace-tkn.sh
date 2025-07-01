#!/bin/bash
set -e

# Enhanced Workspace Testing Script using tkn CLI
# This script creates and monitors pipeline runs for the enhanced shared workspace strategy (ADR-0007)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
    fi
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}🎉 $1${NC}"
}

# Default values
REPOSITORY_URL="https://github.com/jeremyrdavis/dddhexagonalworkshop"
WORKSHOP_NAME="enhanced-workspace-test"
BASE_TEMPLATE="showroom_template_default"
AUTO_APPROVE="true"
NAMESPACE="workshop-system"
TIMEOUT="30m"
FOLLOW_LOGS="true"
CLEANUP_ON_FAILURE="false"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --repository-url)
            REPOSITORY_URL="$2"
            shift 2
            ;;
        --workshop-name)
            WORKSHOP_NAME="$2"
            shift 2
            ;;
        --base-template)
            BASE_TEMPLATE="$2"
            shift 2
            ;;
        --auto-approve)
            AUTO_APPROVE="$2"
            shift 2
            ;;
        --namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        --timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        --no-follow)
            FOLLOW_LOGS="false"
            shift
            ;;
        --cleanup-on-failure)
            CLEANUP_ON_FAILURE="true"
            shift
            ;;
        --help)
            echo "Enhanced Workspace Testing Script"
            echo ""
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --repository-url URL     Repository URL to test (default: dddhexagonalworkshop)"
            echo "  --workshop-name NAME     Workshop name (default: enhanced-workspace-test)"
            echo "  --base-template TEMPLATE Base template (default: showroom_template_default)"
            echo "  --auto-approve BOOL      Auto approve steps (default: true)"
            echo "  --namespace NS           Kubernetes namespace (default: workshop-system)"
            echo "  --timeout DURATION       Pipeline timeout (default: 30m)"
            echo "  --no-follow              Don't follow logs in real-time"
            echo "  --cleanup-on-failure     Delete failed pipeline runs"
            echo "  --help                   Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                           # Run with defaults"
            echo "  $0 --workshop-name my-test --no-follow      # Custom name, no log following"
            echo "  $0 --repository-url https://github.com/user/repo --cleanup-on-failure"
            exit 0
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

print_header "Enhanced Workspace Pipeline Test (ADR-0007)"

print_info "Configuration:"
echo "  Repository URL: $REPOSITORY_URL"
echo "  Workshop Name: $WORKSHOP_NAME"
echo "  Base Template: $BASE_TEMPLATE"
echo "  Auto Approve: $AUTO_APPROVE"
echo "  Namespace: $NAMESPACE"
echo "  Timeout: $TIMEOUT"
echo "  Follow Logs: $FOLLOW_LOGS"
echo ""

# Generate unique pipeline run name
TIMESTAMP=$(date +%s)
PIPELINE_RUN_NAME="test-enhanced-workspace-${TIMESTAMP}"
GITEA_REPO_NAME="${WORKSHOP_NAME}-${TIMESTAMP}"

print_info "Pipeline Run Name: $PIPELINE_RUN_NAME"
print_info "Gitea Repository: $GITEA_REPO_NAME"
echo ""

# Check prerequisites
print_header "Checking Prerequisites"

# Check if tkn is available
if ! command -v tkn &> /dev/null; then
    print_status 1 "tkn CLI not found"
    echo "Please install Tekton CLI: https://tekton.dev/docs/cli/"
    exit 1
fi
print_status 0 "tkn CLI available"

# Check if namespace exists
if ! oc get namespace $NAMESPACE &> /dev/null; then
    print_status 1 "Namespace $NAMESPACE not found"
    exit 1
fi
print_status 0 "Namespace $NAMESPACE exists"

# Check if pipeline exists
if ! tkn pipeline describe workflow-1-new-workshop -n $NAMESPACE &> /dev/null; then
    print_status 1 "Pipeline workflow-1-new-workshop not found"
    exit 1
fi
print_status 0 "Pipeline workflow-1-new-workshop exists"

# Check if shared workspace PVC exists
if ! oc get pvc shared-workspace-storage -n $NAMESPACE &> /dev/null; then
    print_status 1 "Shared workspace PVC not found"
    exit 1
fi
print_status 0 "Shared workspace PVC exists"

echo ""

# Create and start pipeline run
print_header "Creating Pipeline Run"

print_info "Starting pipeline run with tkn..."

# Create pipeline run using tkn
if [ "$FOLLOW_LOGS" = "true" ]; then
    tkn pipeline start workflow-1-new-workshop \
        --param repository-url="$REPOSITORY_URL" \
        --param workshop-name="$WORKSHOP_NAME" \
        --param base-template="$BASE_TEMPLATE" \
        --param gitea-repo-name="$GITEA_REPO_NAME" \
        --param human-approver="workshop-system-operator" \
        --param auto-approve="$AUTO_APPROVE" \
        --workspace name=shared-data,claimName=shared-workspace-storage \
        --workspace name=gitea-auth,emptyDir="" \
        --timeout="$TIMEOUT" \
        --namespace="$NAMESPACE" \
        --showlog
else
    # Create pipeline run and capture the name
    ACTUAL_PIPELINE_RUN=$(tkn pipeline start workflow-1-new-workshop \
        --param repository-url="$REPOSITORY_URL" \
        --param workshop-name="$WORKSHOP_NAME" \
        --param base-template="$BASE_TEMPLATE" \
        --param gitea-repo-name="$GITEA_REPO_NAME" \
        --param human-approver="workshop-system-operator" \
        --param auto-approve="$AUTO_APPROVE" \
        --workspace name=shared-data,claimName=shared-workspace-storage \
        --workspace name=gitea-auth,emptyDir="" \
        --timeout="$TIMEOUT" \
        --namespace="$NAMESPACE" \
        --output name)

    PIPELINE_RUN_NAME="$ACTUAL_PIPELINE_RUN"
    print_info "Created pipeline run: $PIPELINE_RUN_NAME"

    # Wait for completion
    print_info "Waiting for pipeline to complete..."
    tkn pipelinerun logs $PIPELINE_RUN_NAME -n $NAMESPACE -f
fi

# Get the final status
PIPELINE_STATUS=$?

echo ""
print_header "Pipeline Run Results"

# Get detailed pipeline run information
print_info "Getting pipeline run details..."
tkn pipelinerun describe $PIPELINE_RUN_NAME -n $NAMESPACE

echo ""

# Check final status
if [ $PIPELINE_STATUS -eq 0 ]; then
    print_success "Pipeline completed successfully!"
    
    # Show workspace contents
    print_header "Workspace Contents"
    print_info "Checking shared workspace contents..."
    
    # Find a running agent pod to check workspace
    AGENT_POD=$(oc get pods -l component=workshop-agent -n $NAMESPACE -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    
    if [ -n "$AGENT_POD" ]; then
        echo "Workspace structure:"
        oc exec $AGENT_POD -n $NAMESPACE -- find /workspace/shared-data -type d | head -20 2>/dev/null || echo "Could not access workspace"
        echo ""
        echo "Pipeline-specific content:"
        oc exec $AGENT_POD -n $NAMESPACE -- ls -la /workspace/shared-data/pipelines/ 2>/dev/null || echo "No pipeline directories found"
    else
        print_warning "No agent pods found to check workspace contents"
    fi
    
else
    print_status 1 "Pipeline failed"
    
    # Show failed task logs
    print_header "Failure Analysis"
    print_info "Getting failed task information..."
    
    # Get failed tasks
    FAILED_TASKS=$(tkn pipelinerun describe $PIPELINE_RUN_NAME -n $NAMESPACE -o jsonpath='{.status.taskRuns}' | jq -r 'to_entries[] | select(.value.status.conditions[0].reason == "Failed") | .key' 2>/dev/null || echo "")
    
    if [ -n "$FAILED_TASKS" ]; then
        for task in $FAILED_TASKS; do
            print_warning "Failed task: $task"
            echo "Logs:"
            tkn taskrun logs $task -n $NAMESPACE | tail -20
            echo ""
        done
    fi
    
    # Cleanup on failure if requested
    if [ "$CLEANUP_ON_FAILURE" = "true" ]; then
        print_info "Cleaning up failed pipeline run..."
        oc delete pipelinerun $PIPELINE_RUN_NAME -n $NAMESPACE
        print_status $? "Pipeline run cleaned up"
    fi
fi

echo ""
print_header "Summary"
echo "Pipeline Run: $PIPELINE_RUN_NAME"
echo "Status: $([ $PIPELINE_STATUS -eq 0 ] && echo "SUCCESS" || echo "FAILED")"
echo "Repository: $REPOSITORY_URL"
echo "Workshop: $WORKSHOP_NAME"
echo ""

if [ $PIPELINE_STATUS -eq 0 ]; then
    print_success "Enhanced Workspace Strategy (ADR-0007) validation completed successfully!"
else
    print_warning "Pipeline failed. Check the logs above for details."
    echo "To retry: $0 --workshop-name $WORKSHOP_NAME-retry"
fi

exit $PIPELINE_STATUS
