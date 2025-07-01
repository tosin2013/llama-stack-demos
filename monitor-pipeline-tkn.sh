#!/bin/bash
set -e

# Pipeline Monitoring Script using tkn CLI
# Monitor existing pipeline runs and show detailed status

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
    case $1 in
        "Succeeded") echo -e "${GREEN}✅ $1${NC}" ;;
        "Failed") echo -e "${RED}❌ $1${NC}" ;;
        "Running") echo -e "${YELLOW}🔄 $1${NC}" ;;
        "Pending") echo -e "${CYAN}⏳ $1${NC}" ;;
        *) echo -e "${PURPLE}📊 $1${NC}" ;;
    esac
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# Default values
NAMESPACE="workshop-system"
PIPELINE_RUN_NAME=""
SHOW_LOGS="false"
FOLLOW_LOGS="false"
LIST_ALL="false"
WATCH_MODE="false"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --pipeline-run)
            PIPELINE_RUN_NAME="$2"
            shift 2
            ;;
        --namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        --logs)
            SHOW_LOGS="true"
            shift
            ;;
        --follow)
            FOLLOW_LOGS="true"
            shift
            ;;
        --list)
            LIST_ALL="true"
            shift
            ;;
        --watch)
            WATCH_MODE="true"
            shift
            ;;
        --help)
            echo "Pipeline Monitoring Script"
            echo ""
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --pipeline-run NAME      Specific pipeline run to monitor"
            echo "  --namespace NS           Kubernetes namespace (default: workshop-system)"
            echo "  --logs                   Show task logs"
            echo "  --follow                 Follow logs in real-time"
            echo "  --list                   List all pipeline runs"
            echo "  --watch                  Watch mode (refresh every 10 seconds)"
            echo "  --help                   Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 --list                                    # List all pipeline runs"
            echo "  $0 --pipeline-run test-enhanced-workspace-123 --logs"
            echo "  $0 --pipeline-run test-enhanced-workspace-123 --follow"
            echo "  $0 --watch                                   # Watch all pipeline runs"
            exit 0
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

# Check prerequisites
if ! command -v tkn &> /dev/null; then
    echo "❌ tkn CLI not found"
    echo "Please install Tekton CLI: https://tekton.dev/docs/cli/"
    exit 1
fi

if ! oc get namespace $NAMESPACE &> /dev/null; then
    echo "❌ Namespace $NAMESPACE not found"
    exit 1
fi

# Function to list all pipeline runs
list_pipeline_runs() {
    print_header "Pipeline Runs in $NAMESPACE"
    
    echo "Recent Enhanced Workspace Pipeline Runs:"
    tkn pipelinerun list -n $NAMESPACE --limit 10 | grep -E "(NAME|test-enhanced-workspace)" || echo "No enhanced workspace pipeline runs found"
    
    echo ""
    echo "All Recent Pipeline Runs:"
    tkn pipelinerun list -n $NAMESPACE --limit 5
}

# Function to show detailed pipeline run status
show_pipeline_status() {
    local pr_name=$1
    
    print_header "Pipeline Run: $pr_name"
    
    # Get basic status
    local status=$(tkn pipelinerun describe $pr_name -n $NAMESPACE -o jsonpath='{.status.conditions[0].reason}' 2>/dev/null || echo "Unknown")
    local start_time=$(tkn pipelinerun describe $pr_name -n $NAMESPACE -o jsonpath='{.status.startTime}' 2>/dev/null || echo "Unknown")
    local completion_time=$(tkn pipelinerun describe $pr_name -n $NAMESPACE -o jsonpath='{.status.completionTime}' 2>/dev/null || echo "")
    
    print_status "$status"
    echo "Start Time: $start_time"
    if [ -n "$completion_time" ]; then
        echo "Completion Time: $completion_time"
    fi
    echo ""
    
    # Show detailed description
    tkn pipelinerun describe $pr_name -n $NAMESPACE
    
    echo ""
    
    # Show task status summary
    print_info "Task Status Summary:"
    tkn pipelinerun describe $pr_name -n $NAMESPACE -o jsonpath='{.status.taskRuns}' | jq -r '
        to_entries[] | 
        "\(.value.status.taskSpec.displayName // .key): \(.value.status.conditions[0].reason // "Unknown")"
    ' 2>/dev/null | sort || echo "Could not get task status"
    
    echo ""
    
    # Show workspace information
    print_info "Workspace Information:"
    tkn pipelinerun describe $pr_name -n $NAMESPACE -o jsonpath='{.spec.workspaces}' | jq -r '
        .[] | 
        "- \(.name): \(.persistentVolumeClaim.claimName // .emptyDir // "Unknown")"
    ' 2>/dev/null || echo "Could not get workspace info"
}

# Function to show logs
show_logs() {
    local pr_name=$1
    local follow=$2
    
    print_header "Logs for Pipeline Run: $pr_name"
    
    if [ "$follow" = "true" ]; then
        print_info "Following logs (Ctrl+C to stop)..."
        tkn pipelinerun logs $pr_name -n $NAMESPACE -f
    else
        print_info "Showing recent logs..."
        tkn pipelinerun logs $pr_name -n $NAMESPACE
    fi
}

# Function to check workspace contents
check_workspace() {
    print_header "Workspace Contents"
    
    # Find a running agent pod to check workspace
    local agent_pod=$(oc get pods -l component=workshop-agent -n $NAMESPACE -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    
    if [ -n "$agent_pod" ]; then
        print_info "Checking workspace via agent pod: $agent_pod"
        echo ""
        echo "Active pipelines:"
        oc exec $agent_pod -n $NAMESPACE -- ls -la /workspace/shared-data/pipelines/ 2>/dev/null || echo "No pipeline directories found"
        echo ""
        echo "Shared resources:"
        oc exec $agent_pod -n $NAMESPACE -- ls -la /workspace/shared-data/shared/ 2>/dev/null || echo "No shared resources found"
        echo ""
        echo "Agent workspaces:"
        oc exec $agent_pod -n $NAMESPACE -- ls -la /workspace/shared-data/agents/ 2>/dev/null || echo "No agent workspaces found"
    else
        print_info "No agent pods found to check workspace contents"
        
        # Try to check PVC directly
        print_info "Checking PVC status..."
        oc get pvc shared-workspace-storage -n $NAMESPACE -o wide 2>/dev/null || echo "Shared workspace PVC not found"
    fi
}

# Function for watch mode
watch_mode() {
    print_header "Watch Mode - Enhanced Workspace Pipelines"
    print_info "Refreshing every 10 seconds (Ctrl+C to stop)..."
    echo ""
    
    while true; do
        clear
        echo "$(date): Enhanced Workspace Pipeline Status"
        echo "=========================================="
        
        # Show recent enhanced workspace pipeline runs
        echo "Recent Enhanced Workspace Runs:"
        tkn pipelinerun list -n $NAMESPACE --limit 5 | grep -E "(NAME|test-enhanced-workspace)" || echo "No enhanced workspace pipeline runs found"
        
        echo ""
        echo "Active Pipeline Runs:"
        tkn pipelinerun list -n $NAMESPACE --limit 3 | grep -E "(NAME|Running|Pending)" || echo "No active pipeline runs"
        
        echo ""
        echo "Workspace Status:"
        oc get pvc shared-workspace-storage -n $NAMESPACE 2>/dev/null || echo "Shared workspace PVC not found"
        
        echo ""
        echo "Agent Status:"
        oc get pods -l component=workshop-agent -n $NAMESPACE 2>/dev/null || echo "No agent pods found"
        
        sleep 10
    done
}

# Main logic
if [ "$LIST_ALL" = "true" ]; then
    list_pipeline_runs
    echo ""
    check_workspace
elif [ "$WATCH_MODE" = "true" ]; then
    watch_mode
elif [ -n "$PIPELINE_RUN_NAME" ]; then
    show_pipeline_status "$PIPELINE_RUN_NAME"
    
    if [ "$SHOW_LOGS" = "true" ] || [ "$FOLLOW_LOGS" = "true" ]; then
        echo ""
        show_logs "$PIPELINE_RUN_NAME" "$FOLLOW_LOGS"
    fi
else
    # Default: show latest enhanced workspace pipeline run
    print_header "Latest Enhanced Workspace Pipeline Run"
    
    LATEST_PR=$(tkn pipelinerun list -n $NAMESPACE --limit 10 -o jsonpath='{.items[*].metadata.name}' | tr ' ' '\n' | grep "test-enhanced-workspace" | head -1 || echo "")
    
    if [ -n "$LATEST_PR" ]; then
        show_pipeline_status "$LATEST_PR"
        echo ""
        check_workspace
    else
        print_info "No enhanced workspace pipeline runs found"
        echo ""
        list_pipeline_runs
    fi
fi
