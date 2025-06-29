# Tutorial: Python Script-Based Workshop Deployment

Learn how to deploy workshops dynamically using Python scripts instead of static Kubernetes configurations.

## 🎯 What You'll Learn

By the end of this tutorial, you'll be able to:
- Deploy workshops programmatically using Python scripts
- Trigger workshop creation through the 6-agent system
- Integrate with OpenShift BuildConfigs for automated deployment
- Replace static workshop configurations with dynamic deployment
- Monitor and manage workshop deployments via scripts

## 📋 Prerequisites

- Workshop Template System deployed (all 6 agents running)
- OpenShift cluster access with `oc` CLI
- Python 3.9+ environment
- Gitea server deployed and configured
- Workshop Monitoring Service running

## 🚀 Overview: Dynamic vs Static Deployment

### Traditional Static Approach (Deprecated)
```yaml
# Static Kubernetes deployment (causes resource waste)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: healthcare-ml-workshop
spec:
  replicas: 2  # Always running, even when not needed
  template:
    spec:
      containers:
      - name: workshop-content
        image: registry.access.redhat.com/ubi8/httpd-24:latest
        # Often fails due to missing content
```

### New Python Script Approach (Recommended)
```python
# Dynamic deployment via Python script
from demos.workshop_template_system.agents.source_manager.tools import (
    commit_to_gitea_tool, 
    trigger_buildconfig_tool
)

# Deploy workshop on-demand
deploy_workshop("healthcare-ml-workshop", "production")
```

## 🛠️ Step 1: Understanding the Core Scripts

### Main Workshop Deployment Script

The primary script is located at: `test-complete-git-workflow.py`

<augment_code_snippet path="test-complete-git-workflow.py" mode="EXCERPT">
````python
#!/usr/bin/env python3
"""
Test the complete Git-integrated Workshop Template System workflow
"""

import sys
import os
sys.path.append('/home/ec2-user/llama-stack-demos')

from demos.workshop_template_system.agents.source_manager.tools import (
    commit_to_gitea_tool, 
    trigger_buildconfig_tool
)
````
</augment_code_snippet>

### Key Functions Available

1. **`commit_to_gitea_tool()`** - Commits workshop content to Gitea and triggers builds
2. **`trigger_buildconfig_tool()`** - Manually triggers OpenShift BuildConfigs
3. **`coordinate_deployment_tool()`** - Orchestrates multi-platform deployments

## 🚀 Step 2: Deploy Your First Workshop

### Method 1: Using the Test Script

```bash
# Navigate to project root
cd /home/ec2-user/llama-stack-demos

# Run the complete workflow test
python test-complete-git-workflow.py
```

### Method 2: Custom Python Script

Create your own deployment script:

```python
#!/usr/bin/env python3
"""
Custom Workshop Deployment Script
"""

import sys
import os
sys.path.append('/home/ec2-user/llama-stack-demos')

from demos.workshop_template_system.agents.source_manager.tools import (
    commit_to_gitea_tool, 
    trigger_buildconfig_tool,
    coordinate_deployment_tool
)

def deploy_healthcare_ml_workshop():
    """Deploy Healthcare ML workshop dynamically"""
    
    print("🚀 Deploying Healthcare ML Workshop...")
    
    # Step 1: Commit content to Gitea (triggers automatic build)
    result = commit_to_gitea_tool(
        workshop_name="healthcare-ml-workshop",
        content_description="Updated with Quarkus 3.8 WebSocket features and ML inference improvements",
        gitea_url="https://gitea-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
    )
    
    print("✅ Content committed to Gitea")
    print("✅ BuildConfig automatically triggered")
    
    return result

def deploy_openshift_baremetal_workshop():
    """Deploy OpenShift Bare Metal workshop dynamically"""
    
    print("🔧 Deploying OpenShift Bare Metal Workshop...")
    
    # Step 1: Trigger manual build (if automatic didn't work)
    result = trigger_buildconfig_tool(
        workshop_name="openshift-baremetal-workshop",
        build_reason="content-update"
    )
    
    print("✅ BuildConfig manually triggered")
    print("✅ Workshop deployment initiated")
    
    return result

if __name__ == "__main__":
    # Deploy both test workshops
    deploy_healthcare_ml_workshop()
    print("\n" + "="*60 + "\n")
    deploy_openshift_baremetal_workshop()
```

## 🔧 Step 3: Integration with Deploy Script

### Update deploy-complete-system.sh

The main deployment script includes the `update_buildconfig_urls()` function:

<augment_code_snippet path="deploy-complete-system.sh" mode="EXCERPT">
````bash
# Update Kustomize BuildConfigs with correct Gitea URL
update_buildconfig_urls() {
    print_status "Updating BuildConfig URLs to use deployed Gitea instance..."

    # Get actual Gitea URL from the deployed instance
    GITEA_URL=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.giteaHostname}' 2>/dev/null || echo "gitea.apps.cluster.local")

    print_status "Updating BuildConfigs to use Gitea URL: https://${GITEA_URL}"

    # Update the buildconfigs.yaml file with the correct Gitea URL
    sed -i "s|https://gitea.apps.cluster.local|https://${GITEA_URL}|g" kubernetes/workshop-template-system/base/buildconfigs.yaml
}
````
</augment_code_snippet>

### Trigger Workshop Builds Function

<augment_code_snippet path="deploy-complete-system.sh" mode="EXCERPT">
````bash
# Trigger workshop builds if repositories are available
trigger_workshop_builds() {
    print_status "Checking and triggering workshop builds..."

    # Check if workshop BuildConfigs exist and trigger builds
    if oc get buildconfig openshift-baremetal-workshop-build -n ${NAMESPACE} &>/dev/null; then
        print_status "Triggering OpenShift Bare Metal workshop build..."
        oc start-build openshift-baremetal-workshop-build -n ${NAMESPACE}
    fi

    if oc get buildconfig healthcare-ml-workshop-build -n ${NAMESPACE} &>/dev/null; then
        print_status "Triggering Healthcare ML workshop build..."
        oc start-build healthcare-ml-workshop-build -n ${NAMESPACE}
    fi
}
````
</augment_code_snippet>

## 📊 Step 4: Monitor Workshop Deployments

### Using the Workshop Monitoring Service

Check workshop status via the monitoring dashboard:

```bash
# Get monitoring service URL
MONITORING_URL=$(oc get route workshop-monitoring-service -n workshop-system -o jsonpath='{.spec.host}')

# Check workshop health
curl -s https://${MONITORING_URL}/api/monitoring/summary | jq .

# View monitoring dashboard
echo "Dashboard: https://${MONITORING_URL}/"
```

### Monitor Build Progress

```bash
# Watch build logs
oc logs -f build/healthcare-ml-workshop-build-1 -n workshop-system

# Check build status
oc get builds -n workshop-system

# Monitor deployment rollout
oc rollout status deployment/healthcare-ml-workshop -n workshop-system
```

## 🎯 Step 5: Advanced Deployment Patterns

### Multi-Platform Deployment

```python
def deploy_to_multiple_platforms(workshop_name: str):
    """Deploy workshop to multiple platforms"""
    
    platforms = ["openshift", "github-pages", "showroom"]
    results = []
    
    for platform in platforms:
        result = coordinate_deployment_tool(
            platform=platform,
            repository_name=workshop_name,
            deployment_type="production"
        )
        results.append(f"{platform}: {result}")
    
    return results
```

### Conditional Deployment

```python
def smart_workshop_deployment(repo_url: str):
    """Intelligently deploy based on repository analysis"""
    
    # Let agents analyze the repository first
    analysis_result = analyze_repository_via_agents(repo_url)
    
    if "existing-workshop" in analysis_result:
        # Use Workflow 3: Enhancement
        return enhance_existing_workshop(repo_url)
    else:
        # Use Workflow 1: New workshop creation
        return create_new_workshop(repo_url)
```

## 🔍 Step 6: Troubleshooting

### Common Issues and Solutions

1. **BuildConfig Not Found**
   ```bash
   # Check if BuildConfigs exist
   oc get buildconfig -n workshop-system
   
   # If missing, redeploy base system
   ./deploy-complete-system.sh
   ```

2. **Gitea Connection Issues**
   ```bash
   # Check Gitea status
   oc get gitea gitea-with-admin -n gitea
   
   # Get actual Gitea URL
   oc get route gitea-with-admin -n gitea -o jsonpath='{.spec.host}'
   ```

3. **Agent Communication Problems**
   ```bash
   # Check agent health
   curl -s https://${MONITORING_URL}/api/monitoring/summary
   
   # Restart agents if needed
   oc rollout restart deployment/source-manager-agent -n workshop-system
   ```

## 🎯 Step 7: Human Oversight Integration

### Overview of Human Oversight Features

The Workshop Template System now includes comprehensive human oversight capabilities through the Human Oversight Coordinator. This provides 4 interaction modes for managing workshop deployments:

1. **💬 Chat Interface** - Natural language interaction
2. **🖥️ Command Interface** - Terminal-style command execution
3. **✅ Approval Interface** - Workflow approval and rejection
4. **📊 Overview Interface** - Real-time system monitoring

### Using DDD Hexagonal Workshop as Example

Let's demonstrate the complete human oversight workflow using the DDD Hexagonal Workshop repository:

```bash
# Run the comprehensive human oversight test
./test_human_oversight_workflow.sh
```

This script demonstrates:
- Repository submission and analysis
- Human oversight decision points
- Chat interface for coordination
- Command execution for system management
- Approval workflows for quality control

### Step-by-Step Human Oversight Example

#### 1. Repository Submission
```bash
# Submit DDD Hexagonal Workshop for processing
REPO_URL="https://github.com/jeremyrdavis/dddhexagonalworkshop"
echo "Processing repository: $REPO_URL"

# The system will analyze the repository and create approval workflows
```

#### 2. Chat Interface Interaction
```bash
# Test natural language queries
curl -k -X POST "${MONITORING_URL}/api/oversight/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "What workflows need my attention?"}'

# Expected response: Information about pending approvals and system status
```

#### 3. Command Execution
```bash
# Execute system management commands
curl -k -X POST "${MONITORING_URL}/api/oversight/coordinate" \
  -H "Content-Type: application/json" \
  -d '{"action": "execute_command", "command": "system health", "executor": "human-operator"}'

# Available commands:
# - "system health" - Overall system status
# - "agent status" - Individual agent performance
# - "quality check" - Quality assurance metrics
# - "list workflows" - Current workflow queue
```

#### 4. Approval Workflow Management
```bash
# Approve a workflow
curl -k -X POST "${MONITORING_URL}/api/oversight/workflows/wf-001/approve" \
  -H "Content-Type: application/json" \
  -d '{"comment": "Repository structure looks good", "approver": "human-operator"}'

# Reject a workflow
curl -k -X POST "${MONITORING_URL}/api/oversight/workflows/wf-002/reject" \
  -H "Content-Type: application/json" \
  -d '{"comment": "Needs documentation improvements", "approver": "human-operator"}'
```

### Integration with Existing Python Scripts

Enhance your existing deployment scripts with human oversight:

```python
#!/usr/bin/env python3
"""
Enhanced Workshop Deployment with Human Oversight
"""

import requests
import json
import time

def request_human_approval(workflow_id, description):
    """Request human approval for workflow"""

    # Submit for human review via chat
    chat_response = requests.post(
        f"{MONITORING_URL}/api/oversight/chat",
        json={
            "message": f"Please review workflow {workflow_id}: {description}",
            "sessionId": "deployment-session"
        }
    )

    print(f"Human oversight guidance: {chat_response.json()['data']['message']['content']}")

    # Wait for human decision (in practice, this would be event-driven)
    print(f"Workflow {workflow_id} submitted for human approval...")
    return workflow_id

def deploy_with_oversight(repo_url):
    """Deploy workshop with human oversight integration"""

    # Step 1: Submit repository for analysis
    workflow_id = f"wf-{int(time.time())}"

    # Step 2: Request human oversight
    approval_id = request_human_approval(workflow_id, f"Repository: {repo_url}")

    # Step 3: Check system health before deployment
    health_response = requests.post(
        f"{MONITORING_URL}/api/oversight/coordinate",
        json={
            "action": "execute_command",
            "command": "system health",
            "executor": "deployment-script"
        }
    )

    if health_response.json()['data']['result']['overall_status'] == 'HEALTHY':
        print("✅ System healthy - proceeding with deployment")
        # Continue with deployment...
    else:
        print("⚠️ System issues detected - deployment paused")
        return False

# Example usage
deploy_with_oversight("https://github.com/jeremyrdavis/dddhexagonalworkshop")
```

### Human Oversight Dashboard

Access the interactive dashboard for visual management:

```bash
# Get dashboard URL
DASHBOARD_URL="https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/"
echo "Human Oversight Dashboard: $DASHBOARD_URL"

# The dashboard provides:
# - Real-time system status
# - Interactive chat interface
# - Command execution terminal
# - Workflow approval queue
```

### Troubleshooting Human Oversight

1. **Chat Interface Not Responding**
   ```bash
   # Check chat service health
   curl -k "${MONITORING_URL}/api/oversight/chat" \
     -X POST -H "Content-Type: application/json" \
     -d '{"message": "test"}'
   ```

2. **Command Execution Failures**
   ```bash
   # Verify command service
   curl -k "${MONITORING_URL}/api/oversight/coordinate" \
     -X POST -H "Content-Type: application/json" \
     -d '{"action": "execute_command", "command": "system health"}'
   ```

3. **Approval Workflow Issues**
   ```bash
   # Check workflow endpoints
   curl -k "${MONITORING_URL}/api/oversight/workflows/test/approve" \
     -X POST -H "Content-Type: application/json" \
     -d '{"comment": "test", "approver": "test"}'
   ```

## 🎉 Next Steps

1. **Create Custom Deployment Scripts** - Build your own Python scripts for specific workshop types
2. **Integrate with CI/CD** - Add workshop deployment to your existing pipelines
3. **Monitor and Scale** - Use the monitoring service to track workshop performance
4. **Extend Agent Capabilities** - Add custom tools to the source manager agent
5. **🆕 Use Human Oversight** - Integrate approval workflows and interactive coordination
6. **🆕 Test with DDD Repository** - Use the provided test script for validation

## 📚 Related Documentation

- [Repository-Based Workshops](./repository-based-workshops.md)
- [Original Content Workshops](./original-content-workshops.md)
- [Workshop Monitoring Service](../reference/monitoring-service.md)
- [Agent System Architecture](../explanation/agent-architecture.md)
- [ADR-0003: Agent Pipeline Integration](../adr/0003-agent-pipeline-integration.md)

---

**🚀 Ready to deploy workshops dynamically with human oversight!** Your enhanced system now provides intelligent coordination, approval workflows, and interactive management capabilities.
