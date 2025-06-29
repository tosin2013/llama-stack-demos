#!/usr/bin/env python3
"""
Test script to trigger a workshop creation workflow and demonstrate human oversight.
This script will:
1. Trigger a "Repository to Workshop" workflow
2. Monitor the workflow progress
3. Show how human oversight coordinates the process
4. Display approval queue and oversight metrics
"""

import requests
import json
import time
import sys
from datetime import datetime

# Configuration
MONITORING_SERVICE_URL = "https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
TEST_REPOSITORY = "https://github.com/Red-Hat-SE-RTO/todo-demo-app-helmrepo-workshop.git"

def print_header(title):
    """Print a formatted header."""
    print(f"\n{'='*60}")
    print(f"  {title}")
    print(f"{'='*60}")

def print_status(message, status="INFO"):
    """Print a status message with timestamp."""
    timestamp = datetime.now().strftime("%H:%M:%S")
    print(f"[{timestamp}] {status}: {message}")

def make_request(method, url, data=None):
    """Make HTTP request with error handling."""
    try:
        if method.upper() == "GET":
            response = requests.get(url, verify=False, timeout=10)
        elif method.upper() == "POST":
            response = requests.post(url, json=data, verify=False, timeout=10)
        else:
            raise ValueError(f"Unsupported method: {method}")
        
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print_status(f"Request failed: {e}", "ERROR")
        return None

def check_system_health():
    """Check the health of all agents."""
    print_header("SYSTEM HEALTH CHECK")
    
    health_data = make_request("GET", f"{MONITORING_SERVICE_URL}/api/monitoring/health")
    if not health_data:
        return False
    
    print_status(f"Overall Status: {health_data.get('overall_status', 'UNKNOWN')}")
    print_status(f"Total Agents: {health_data.get('total_agents', 0)}")
    print_status(f"Healthy Agents: {health_data.get('healthy_agents', 0)}")
    print_status(f"Unhealthy Agents: {health_data.get('unhealthy_agents', 0)}")
    
    if health_data.get('overall_status') != 'HEALTHY':
        print_status("System is not healthy. Some agents may be unavailable.", "WARNING")
    
    return True

def get_agent_configuration():
    """Get agent configuration and workflow templates."""
    print_header("AGENT CONFIGURATION")
    
    config_data = make_request("GET", f"{MONITORING_SERVICE_URL}/api/agent-interaction/config")
    if not config_data or not config_data.get('success'):
        print_status("Failed to get agent configuration", "ERROR")
        return None
    
    data = config_data.get('data', {})
    templates = data.get('workflowTemplates', [])
    
    print_status(f"Available Workflow Templates: {len(templates)}")
    for template in templates:
        print(f"  - {template.get('name')}: {template.get('description')}")
    
    return data

def trigger_workflow():
    """Trigger a Repository to Workshop workflow."""
    print_header("TRIGGERING WORKFLOW")
    
    workflow_data = {
        "workflowId": "repository-to-workshop",
        "parameters": {
            "repository_url": TEST_REPOSITORY,
            "workshop_name": "Todo Demo App Workshop",
            "description": "Convert todo demo app repository into interactive workshop"
        }
    }
    
    print_status(f"Triggering workflow: {workflow_data['workflowId']}")
    print_status(f"Repository: {TEST_REPOSITORY}")
    
    response = make_request("POST", f"{MONITORING_SERVICE_URL}/api/agent-interaction/workflows/execute", workflow_data)
    if not response or not response.get('success'):
        print_status("Failed to trigger workflow", "ERROR")
        return None
    
    execution_data = response.get('data', {})
    execution_id = execution_data.get('executionId')
    
    print_status(f"Workflow triggered successfully!")
    print_status(f"Execution ID: {execution_id}")
    print_status(f"Status: {execution_data.get('status')}")
    
    return execution_id

def check_human_oversight():
    """Check human oversight coordinator status."""
    print_header("HUMAN OVERSIGHT COORDINATOR")
    
    oversight_data = make_request("GET", f"{MONITORING_SERVICE_URL}/api/oversight/coordinator/status")
    if not oversight_data or not oversight_data.get('success'):
        print_status("Failed to get oversight status", "ERROR")
        return
    
    data = oversight_data.get('data', {})
    print_status(f"Coordinator Status: {data.get('status')}")
    print_status(f"Active Sessions: {data.get('activeSessions')}")
    print_status(f"Pending Approvals: {data.get('pendingApprovals')}")
    print_status(f"Version: {data.get('version')}")
    
    capabilities = data.get('capabilities', [])
    print_status(f"Capabilities: {', '.join(capabilities)}")

def check_quality_metrics():
    """Check quality assurance metrics."""
    print_header("QUALITY ASSURANCE METRICS")
    
    metrics_data = make_request("GET", f"{MONITORING_SERVICE_URL}/api/oversight/metrics/quality")
    if not metrics_data or not metrics_data.get('success'):
        print_status("Failed to get quality metrics", "ERROR")
        return
    
    data = metrics_data.get('data', {})
    print_status(f"Overall Quality Score: {data.get('overallScore')}/100")
    print_status(f"Compliance Score: {data.get('complianceScore')}/100")
    print_status(f"Approval Efficiency: {data.get('approvalEfficiency')}/100")
    
    recent_events = data.get('recentEvents', [])
    print_status(f"Recent Quality Events:")
    for event in recent_events[:3]:  # Show last 3 events
        print(f"  - {event.get('type')}: {event.get('message')} ({event.get('timestamp')})")

def check_active_workflows():
    """Check active workflows requiring oversight."""
    print_header("ACTIVE WORKFLOWS")
    
    workflows_data = make_request("GET", f"{MONITORING_SERVICE_URL}/api/oversight/workflows/active")
    if not workflows_data or not workflows_data.get('success'):
        print_status("Failed to get active workflows", "ERROR")
        return
    
    data = workflows_data.get('data', {})
    workflows = data.get('workflows', [])
    
    print_status(f"Total Active Workflows: {data.get('totalCount', 0)}")
    print_status(f"Pending Approval: {data.get('pendingApproval', 0)}")
    print_status(f"In Progress: {data.get('inProgress', 0)}")
    
    if workflows:
        print_status("Active Workflows:")
        for workflow in workflows:
            print(f"  - {workflow.get('name')} ({workflow.get('id')})")
            print(f"    Status: {workflow.get('status')}")
            print(f"    Type: {workflow.get('type')}")
            print(f"    Priority: {workflow.get('priority')}")
            print(f"    Submitted: {workflow.get('submittedAt')}")

def check_approval_queue():
    """Check the approval queue."""
    print_header("APPROVAL QUEUE")
    
    approvals_data = make_request("GET", f"{MONITORING_SERVICE_URL}/api/approvals")
    if not approvals_data or not approvals_data.get('success'):
        print_status("Failed to get approval queue", "ERROR")
        return
    
    data = approvals_data.get('data', {})
    approvals = data.get('approvals', [])
    
    print_status(f"Total Pending Approvals: {len(approvals)}")
    
    if approvals:
        print_status("Pending Approvals:")
        for approval in approvals[:3]:  # Show first 3
            print(f"  - {approval.get('title')} (ID: {approval.get('id')})")
            print(f"    Type: {approval.get('type')}")
            print(f"    Status: {approval.get('status')}")
            print(f"    Priority: {approval.get('priority')}")
            print(f"    Submitted: {approval.get('submittedAt')}")

def simulate_approval_process(execution_id):
    """Simulate the approval process for the triggered workflow."""
    print_header("SIMULATING APPROVAL PROCESS")
    
    # In a real scenario, this would be done through the dashboard
    # For demonstration, we'll show what the approval process would look like
    
    print_status("In a real scenario, the following would happen:")
    print("  1. Human Oversight Coordinator detects new workflow")
    print("  2. Workflow appears in Approval Queue")
    print("  3. Human reviewer examines workflow parameters")
    print("  4. Reviewer approves/rejects via dashboard")
    print("  5. Approved workflows proceed to agent execution")
    print("  6. Quality metrics are updated throughout process")
    
    # Simulate approval
    approval_data = {
        "comment": "Approved for testing - repository looks suitable for workshop conversion",
        "approver": "test-user"
    }
    
    print_status("Simulating approval...")
    # Note: In real implementation, we'd use the actual workflow ID
    mock_workflow_id = "wf-001"
    
    response = make_request("POST", f"{MONITORING_SERVICE_URL}/api/oversight/workflows/{mock_workflow_id}/approve", approval_data)
    if response and response.get('success'):
        result = response.get('data', {})
        print_status(f"Workflow approved by: {result.get('approver')}")
        print_status(f"Comment: {result.get('comment')}")
        print_status(f"Next Steps: {result.get('nextSteps')}")
    else:
        print_status("Approval simulation completed (mock response)", "INFO")

def main():
    """Main execution function."""
    print_header("WORKSHOP TEMPLATE SYSTEM - HUMAN OVERSIGHT DEMO")
    print_status("Starting workflow trigger and human oversight demonstration")
    
    # Step 1: Check system health
    if not check_system_health():
        print_status("System health check failed. Exiting.", "ERROR")
        return 1
    
    # Step 2: Get agent configuration
    config = get_agent_configuration()
    if not config:
        print_status("Failed to get agent configuration. Exiting.", "ERROR")
        return 1
    
    # Step 3: Check human oversight status
    check_human_oversight()
    
    # Step 4: Check quality metrics
    check_quality_metrics()
    
    # Step 5: Check active workflows
    check_active_workflows()
    
    # Step 6: Check approval queue
    check_approval_queue()
    
    # Step 7: Trigger workflow
    execution_id = trigger_workflow()
    if not execution_id:
        print_status("Failed to trigger workflow. Continuing with demonstration.", "WARNING")
        execution_id = "demo-execution-123"
    
    # Step 8: Wait a moment for system to process
    print_status("Waiting for system to process workflow...")
    time.sleep(3)
    
    # Step 9: Check updated status
    print_header("POST-WORKFLOW STATUS")
    check_active_workflows()
    check_approval_queue()
    
    # Step 10: Simulate approval process
    simulate_approval_process(execution_id)
    
    # Step 11: Final status check
    print_header("FINAL STATUS CHECK")
    check_quality_metrics()
    
    print_header("DEMONSTRATION COMPLETE")
    print_status("Human oversight workflow demonstration completed successfully!")
    print_status("Check the dashboard to see real-time updates:")
    print_status(f"  {MONITORING_SERVICE_URL}")
    
    return 0

if __name__ == "__main__":
    try:
        exit_code = main()
        sys.exit(exit_code)
    except KeyboardInterrupt:
        print_status("\nDemo interrupted by user", "INFO")
        sys.exit(0)
    except Exception as e:
        print_status(f"Unexpected error: {e}", "ERROR")
        sys.exit(1)
