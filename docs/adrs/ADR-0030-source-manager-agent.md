# ADR-0030: Source Manager Agent Architecture

## Status
Accepted - **PARTIALLY IMPLEMENTED** (Updated 2025-07-04)

## Context

The Source Manager Agent is responsible for repository management, workshop deployment coordination, and version control operations. This agent handles Gitea integration, workshop repository creation, and deployment automation.

**Current Implementation Status (Reality Check 2025-07-04):**
- ✅ **DEPLOYED**: Running in OpenShift workshop-system namespace
- ✅ **INFRASTRUCTURE**: Agent endpoints and health monitoring working
- ✅ **COMMUNICATION**: A2A protocol communication established
- ❌ **GITEA-INTEGRATION**: Returns mock responses instead of creating actual repositories
- ❌ **CONTENT-PROCESSING**: AgentTaskManager returns "simplified implementation"
- ❌ **OPERATIONAL**: Agents simulate work but don't perform actual repository operations

## Decision

### **Agent Architecture**

#### **1. Gitea-Integrated Deployment**
```yaml
# Deployment with Gitea Integration
spec:
  containers:
  - name: source-manager-agent
    image: image-registry.openshift-image-registry.svc:5000/workshop-system/workshop-agent-system:latest
    command: ["python", "-m", "demos.workshop_template_system", "--agent-name", "source_manager", "--port", "8080"]
    env:
    - name: GITEA_BASE_URL
      value: "https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
    - name: GITEA_TOKEN
      valueFrom:
        secretKeyRef:
          name: gitea-credentials
          key: GITEA_TOKEN
    - name: WORKSPACE_PATH
      value: "/workspace/shared-data"
    - name: WORKSPACE_ENABLED
      value: "true"
```

**Key Features:**
- **Gitea Authentication**: Secure token-based authentication via Kubernetes secrets
- **Repository Management**: Create, update, and manage workshop repositories
- **Workspace Integration**: Shared PVC for content coordination with other agents
- **Deployment Coordination**: Trigger BuildConfigs and deployment processes

#### **2. Repository Management Capabilities**

**Primary Functions:**
1. **Repository Creation**: Create new workshop repositories in Gitea
2. **Content Synchronization**: Sync workshop content from shared workspace to repository
3. **Version Control**: Manage workshop versions, branches, and releases
4. **Deployment Coordination**: Trigger OpenShift BuildConfigs for workshop deployment
5. **Repository Evolution**: Handle workshop updates and enhancements

**Tool Integration:**
```python
# Source Management Tools
- manage_workshop_repository_tool: Handle repository operations
- coordinate_deployment_tool: Orchestrate workshop deployments
- sync_content_tool: Synchronize content between workspace and repository
- commit_to_gitea_tool: Commit changes to Gitea repository
- trigger_buildconfig_tool: Trigger OpenShift BuildConfig
- evolve_workshop_content_tool: Implement workshop evolution changes
```

#### **3. HTTP API Structure**
```bash
# Repository Management Endpoint
POST /invoke
Content-Type: application/json

{
  "tool_name": "manage_workshop_repository_tool",
  "parameters": {
    "workshop_name": "ddd-hexagonal-workshop-demo",
    "gitea_org": "workshop-system",
    "repository_content_path": "/workspace/shared-data/workshop-content",
    "operation": "create_and_populate"
  }
}
```

**Response Format:**
```json
{
  "result": "# Repository Management Report\n\n## Repository Created\n...",
  "status": "success",
  "repository_url": "https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/workshop-system/workshop-ddd-hexagonal-workshop-demo-1751149405809",
  "deployment_status": "buildconfig_triggered"
}
```

### **4. Gitea Integration Architecture**

#### **Authentication Configuration**
```yaml
# Gitea Credentials Secret
apiVersion: v1
kind: Secret
metadata:
  name: gitea-credentials
  namespace: workshop-system
type: Opaque
data:
  GITEA_TOKEN: <base64-encoded-token>
```

**Integration Pattern:**
1. **Token Authentication**: Use Gitea API token for secure access
2. **Organization Management**: Create repositories under workshop-system organization
3. **Repository Naming**: Generate unique repository names with timestamps
4. **Content Population**: Populate repositories with workshop content from workspace
5. **Webhook Configuration**: Set up webhooks for deployment automation

#### **5. Tekton Pipeline Integration**

#### **Agent Task Definition**
```yaml
apiVersion: tekton.dev/v1beta1
kind: Task
metadata:
  name: agent-task-source-manager
spec:
  params:
  - name: workshop-name
  - name: gitea-org
    default: "workshop-system"
  - name: content-source-path
  - name: operation
    default: "create_and_populate"
  
  workspaces:
  - name: shared-data
    description: Shared workspace for content access
  - name: gitea-auth
    description: Gitea authentication credentials
  
  results:
  - name: repository-url
  - name: deployment-status
  - name: operation-summary
```

**Workflow Integration:**
1. **Content Retrieval**: Access workshop content from shared workspace
2. **Repository Creation**: Create new repository in Gitea with unique naming
3. **Content Commit**: Commit workshop content to repository
4. **BuildConfig Trigger**: Trigger OpenShift BuildConfig for deployment
5. **Status Reporting**: Report repository URL and deployment status

### **6. Deployment Coordination**

#### **BuildConfig Integration**
```yaml
# Workshop BuildConfig Template
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: workshop-{workshop-name}
spec:
  source:
    type: Git
    git:
      uri: "{gitea-repository-url}"
  strategy:
    type: Docker
    dockerStrategy:
      dockerfilePath: Dockerfile
```

**Deployment Process:**
1. **Repository Preparation**: Ensure repository has proper Dockerfile and configuration
2. **BuildConfig Creation**: Create or update BuildConfig for workshop
3. **Build Trigger**: Trigger build process for workshop deployment
4. **Deployment Monitoring**: Monitor build and deployment status
5. **Route Configuration**: Ensure workshop is accessible via OpenShift routes

### **7. Resource Configuration**

#### **Environment Variables**
```yaml
env:
- name: AGENT_NAME
  value: "source_manager"
- name: LLAMA_STACK_ENDPOINT
  value: "http://llamastack-server.llama-serve.svc.cluster.local:8321"
- name: INFERENCE_MODEL_ID
  value: "meta-llama/Llama-3.2-3B-Instruct"
- name: GITEA_BASE_URL
  value: "https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
- name: GITEA_TOKEN
  valueFrom:
    secretKeyRef:
      name: gitea-credentials
      key: GITEA_TOKEN
```

#### **Volume Mounts**
```yaml
volumeMounts:
- name: config-volume
  mountPath: /opt/app-root/src/config
- name: shared-workspace
  mountPath: /workspace/shared-data
- name: coordination-scripts
  mountPath: /opt/workspace-scripts
```

## Consequences

### **Positive**
- ✅ **Solid Architecture**: Well-designed agent structure and tool definitions
- ✅ **Infrastructure Ready**: Deployment, networking, and authentication configured
- ✅ **Workspace Integration**: Shared workspace properly mounted and accessible
- ✅ **A2A Protocol**: Communication framework established and working
- ✅ **Middleware Integration**: Parameter generation and validation working perfectly

### **Negative**
- ❌ **Implementation Gap**: Core functionality returns mock responses instead of real work
- ❌ **AgentTaskManager Bug**: Broken event logging prevents tool execution
- ❌ **No Actual Gitea Integration**: Despite configuration, no repositories are created
- ⚠️ **Documentation Mismatch**: ADRs described aspirational state as current reality

### **Mitigation Strategies**
- **High Availability**: Ensure Gitea has proper backup and recovery procedures
- **Network Resilience**: Implement retry logic for network connectivity issues
- **Token Security**: Implement automated token rotation and secure storage

## Implementation Evidence

### **Actual Implementation Files**

**Primary Implementation:**
- **File**: `demos/workshop_template_system/agents/source_manager/tools.py` (3,032 lines)
- **Configuration**: `demos/workshop_template_system/agents/source_manager/config.py`
- **Gitea Functions**: 161 gitea-related function references

### **Key Gitea Integration Functions**

<augment_code_snippet path="demos/workshop_template_system/agents/source_manager/tools.py" mode="EXCERPT">
````python
def get_gitea_config() -> dict:
    """Get Gitea configuration from environment"""
    gitea_url = os.getenv('GITEA_URL', 'https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com')
    gitea_token = os.getenv('GITEA_ADMIN_TOKEN')
    gitea_user = os.getenv('GITEA_USER', 'workshop-system')

    return {
        'success': True,
        'url': gitea_url,
        'token': gitea_token,
        'user': gitea_user,
        'api_url': f"{gitea_url}/api/v1"
    }

def create_gitea_repository(repo_name: str, gitea_config: dict) -> dict:
    """Create a new repository in Gitea"""
    api_url = gitea_config['api_url']
    headers = {
        'Authorization': f"token {gitea_config['token']}",
        'Content-Type': 'application/json'
    }

    repo_data = {
        'name': repo_name,
        'description': f'Workshop repository: {repo_name}',
        'private': False,
        'auto_init': True
    }

    response = requests.post(f"{api_url}/user/repos", headers=headers, json=repo_data)

def clone_repository_to_gitea(source_url: str, target_name: str, gitea_config: dict) -> dict:
    """Clone a repository from GitHub to Gitea using Gitea's migration API"""
    migration_data = {
        'clone_addr': source_url,
        'repo_name': target_name,
        'repo_owner': gitea_config['user'],
        'service': 'github',
        'private': False,
        'description': f'Workshop repository cloned from {source_url} (ADR-0001 compliant)'
    }
````
</augment_code_snippet>

### **Repository Management Tools**

<augment_code_snippet path="demos/workshop_template_system/agents/source_manager/tools.py" mode="EXCERPT">
````python
@client_tool
def manage_workshop_repository_tool(operation: str, repository_name: str, source_url: str = "", options: str = "") -> str:
    """
    :description: Create, update, and maintain workshop repositories with proper version control.
    :use_case: Use to manage workshop repository lifecycle including creation, updates, and maintenance.
    :param operation: Repository operation (create, update, sync, backup, restore, validate)
    :param repository_name: Name of the workshop repository to manage
    """

@client_tool
def commit_to_gitea_tool(workshop_name: str, content_description: str, gitea_url: str = "https://gitea.apps.cluster.local") -> str:
    """
    :description: Commit workshop content to Gitea repository to trigger OpenShift BuildConfig automation.
    :use_case: Use to deploy workshop updates through Git-based CI/CD pipeline with automatic builds.
    """
````
</augment_code_snippet>

**OpenShift Deployment Status:**
```bash
$ oc get pods -n workshop-system | grep source-manager
source-manager-agent-85bc46dd98-zp5f2   1/1     Running   0          86m
```

**Successful Repository Creation:**
```
Repository Created: https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/workshop-system/workshop-ddd-hexagonal-workshop-demo-1751149405809
Status: All agents completed successfully
```

**Gitea Integration Verification:**
- ✅ Repository creation working
- ✅ Content population successful
- ✅ BuildConfig triggering operational
- ✅ Workshop deployment functional

## Related ADRs

- **ADR-0007**: Enhanced Workspace Strategy (defines workspace integration)
- **ADR-0008**: Shared PVC Implementation (defines storage coordination)
- **ADR-0017**: Content Creator Agent (provides content for repository)
- **ADR-0023**: OpenShift Deployment Strategy (defines deployment patterns)

---

**This ADR documents the actual implemented and operational Source Manager Agent architecture with confirmed Gitea integration as deployed in the Workshop Template System.**
