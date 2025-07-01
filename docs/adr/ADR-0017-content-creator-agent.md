# ADR-0017: Content Creator Agent Architecture

## Status
Accepted - **IMPLEMENTED AND OPERATIONAL**

## Context

The Content Creator Agent is responsible for generating and enhancing workshop content based on repository analysis and template requirements. This agent creates workshop-specific content, integrates with showroom templates, and coordinates with shared workspace for content management.

**Current Implementation Status:**
- ✅ **DEPLOYED**: Running in OpenShift workshop-system namespace
- ✅ **WORKSPACE-ENABLED**: Integrated with shared PVC for content coordination
- ✅ **OPERATIONAL**: Successfully creating workshop content
- ✅ **PIPELINE-INTEGRATED**: Connected to Tekton workflows

## Decision

### **Agent Architecture**

#### **1. Workspace-Enabled Deployment**
```yaml
# Enhanced Deployment with Workspace Integration
spec:
  containers:
  - name: content-creator-agent
    image: image-registry.openshift-image-registry.svc:5000/workshop-system/workshop-agent-system:latest
    command: ["python", "-m", "demos.workshop_template_system", "--agent-name", "content_creator", "--port", "8080"]
    env:
    - name: WORKSPACE_PATH
      value: "/workspace/shared-data"
    - name: WORKSPACE_ENABLED
      value: "true"
    - name: WORKSPACE_SCRIPTS_PATH
      value: "/opt/workspace-scripts"
    volumeMounts:
    - name: shared-workspace
      mountPath: /workspace/shared-data
    - name: coordination-scripts
      mountPath: /opt/workspace-scripts
```

**Key Features:**
- **Shared Workspace**: RWX PVC mounted at `/workspace/shared-data`
- **Coordination Scripts**: Helper scripts for workspace management
- **Content Persistence**: Workshop content persisted across pipeline tasks
- **Multi-Agent Coordination**: Shared workspace enables agent collaboration

#### **2. Content Creation Capabilities**

**Primary Functions:**
1. **New Workshop Creation**: Generate content from repository analysis using showroom_template_default
2. **Workshop Enhancement**: Improve existing workshop content with new features
3. **Template Integration**: Seamlessly integrate with RHPDS-compatible templates
4. **Content Validation**: Ensure generated content meets workshop standards

**Tool Integration:**
```python
# Content Creation Tools
- create_workshop_content_tool: Generate new workshop content
- enhance_workshop_content_tool: Improve existing workshops  
- integrate_template_tool: Apply showroom template structure
- validate_content_tool: Verify content quality and standards
```

#### **3. HTTP API Structure**
```bash
# Content Creation Endpoint
POST /invoke
Content-Type: application/json

{
  "tool_name": "create_workshop_content_tool",
  "parameters": {
    "repository_url": "https://github.com/example/repo.git",
    "workshop_name": "example-workshop",
    "base_template": "showroom_template_default",
    "analysis_result": "Repository analysis markdown...",
    "workspace_mode": "hybrid"
  }
}
```

**Response Format:**
```json
{
  "result": "# Workshop Content Creation Report\n\n## Generated Content\n...",
  "status": "success",
  "workspace_path": "/workspace/shared-data/example-workshop",
  "content_files": ["index.adoc", "modules/", "assets/"]
}
```

### **4. Tekton Pipeline Integration**

#### **Agent Task Definition**
```yaml
apiVersion: tekton.dev/v1beta1
kind: Task
metadata:
  name: agent-task-content-creator
spec:
  params:
  - name: repository-url
  - name: workshop-name
  - name: base-template
  - name: analysis-result
  - name: workspace-mode
    default: "hybrid"
  
  workspaces:
  - name: shared-data
    description: Shared workspace for content coordination
  
  results:
  - name: content-status
  - name: workshop-path
  - name: content-summary
```

**Workspace Coordination:**
1. **Content Generation**: Create workshop content in shared workspace
2. **File Organization**: Structure content according to showroom template
3. **Asset Management**: Handle images, code samples, and resources
4. **Metadata Creation**: Generate workshop metadata and configuration

### **5. Template Integration Strategy**

#### **Showroom Template Default Integration**
```yaml
# Template Structure Applied
workshop-content/
├── antora.yml              # Antora configuration
├── showroom.yml           # Showroom configuration  
├── modules/
│   └── ROOT/
│       ├── nav.adoc       # Navigation structure
│       ├── pages/         # Workshop pages
│       └── assets/        # Images and resources
└── site.yml               # Site configuration
```

**Content Generation Process:**
1. **Template Cloning**: Clone showroom_template_default structure
2. **Content Adaptation**: Adapt repository content to workshop format
3. **Navigation Generation**: Create workshop navigation structure
4. **Asset Processing**: Process and optimize workshop assets
5. **Configuration Setup**: Configure Antora and Showroom settings

### **6. Resource Configuration**

#### **Environment Variables**
```yaml
env:
- name: AGENT_NAME
  value: "content_creator"
- name: LLAMA_STACK_ENDPOINT
  value: "http://llamastack-server.llama-serve.svc.cluster.local:8321"
- name: INFERENCE_MODEL_ID
  value: "meta-llama/Llama-3.2-3B-Instruct"
- name: WORKSPACE_PATH
  value: "/workspace/shared-data"
- name: WORKSPACE_ENABLED
  value: "true"
```

#### **Resource Allocation**
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1"
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
- ✅ **Workspace Coordination**: Shared PVC enables seamless content sharing between agents
- ✅ **Template Compatibility**: Full integration with showroom_template_default
- ✅ **Content Persistence**: Workshop content persists across pipeline executions
- ✅ **Multi-Modal Operation**: Supports both new creation and enhancement workflows
- ✅ **Quality Assurance**: Built-in content validation and standards compliance

### **Negative**
- ⚠️ **Storage Dependency**: Requires shared RWX storage for workspace functionality
- ⚠️ **Content Complexity**: Large repositories may require significant processing time
- ⚠️ **Template Coupling**: Tightly coupled to showroom template structure

### **Mitigation Strategies**
- **Storage Monitoring**: Monitor workspace usage and implement cleanup policies
- **Processing Optimization**: Implement content chunking for large repositories
- **Template Flexibility**: Design for multiple template support in future versions

## Implementation Evidence

### **Actual Implementation Files**

**Primary Implementation:**
- **File**: `demos/workshop_template_system/agents/content_creator/tools.py`
- **Configuration**: `demos/workshop_template_system/agents/content_creator/config.py`
- **Workspace Scripts**: `kubernetes/workshop-template-system/base/workspace-coordination-configmap.yaml`

### **Key Workspace Tools**

<augment_code_snippet path="demos/workshop_template_system/agents/content_creator/tools.py" mode="EXCERPT">
````python
@client_tool
def create_workshop_content_from_workspace_tool(
    workspace_path: str = "/workspace/shared-data",
    workshop_name: str = "",
    operation_mode: str = "hybrid"
) -> str:
    """
    :description: Create workshop content using workspace files (ADR-0007 implementation).
    :use_case: Use when workspace is available to create content from actual cloned repository files.
    :param workspace_path: Path to shared workspace containing workshop content
    :param workshop_name: Name of the workshop being created
    :param operation_mode: Operation mode (file-based, hybrid, api-only)
    :returns: Structured workshop content based on workspace files
    """
    content_dir = os.path.join(workspace_path, "workshop-content")
    metadata_dir = os.path.join(workspace_path, "metadata")

def read_workspace_files(content_dir: str) -> dict:
    """Read workshop files from workspace directory"""
    files = {}
    for root, dirs, filenames in os.walk(content_dir):
        for filename in filenames:
            file_path = os.path.join(root, filename)
            rel_path = os.path.relpath(file_path, content_dir)
            with open(file_path, 'r', encoding='utf-8') as f:
                files[rel_path] = f.read()
    return files
````
</augment_code_snippet>

### **Workspace Coordination Scripts**

<augment_code_snippet path="kubernetes/workshop-template-system/base/workspace-coordination-configmap.yaml" mode="EXCERPT">
````yaml
data:
  workspace-init.sh: |
    #!/bin/bash
    # Workspace Initialization Script (ADR-0007)
    WORKSPACE_ROOT="/workspace/shared-data"
    PIPELINE_ID="${1:-unknown}"

    # Create pipeline-specific directory structure
    PIPELINE_DIR="$WORKSPACE_ROOT/pipelines/$PIPELINE_ID"
    mkdir -p "$PIPELINE_DIR"/{workspace-content,agent-artifacts/content-creator,metadata/locks,final-output}

    # Create agent working directories
    mkdir -p "$WORKSPACE_ROOT/agents/content-creator"/{working,cache}

  workspace-lock.sh: |
    #!/bin/bash
    # Workspace File Locking Script (ADR-0007)
    WORKSPACE_ROOT="/workspace/shared-data"
    PIPELINE_ID="$1"
    RESOURCE="$2"
    ACTION="$3"  # acquire, release, check
````
</augment_code_snippet>

**OpenShift Deployment Status:**
```bash
$ oc get pods -n workshop-system | grep content-creator
content-creator-agent-5bd649c4d-qhdc9   1/1     Running   0          86m
```

**Workspace Integration:**
```bash
$ oc describe pod content-creator-agent-5bd649c4d-qhdc9 -n workshop-system
Volumes:
  shared-workspace:
    Type:       PersistentVolumeClaim
    ClaimName:  workshop-shared-workspace
```

**Successful Content Creation:**
- ✅ DDD Hexagonal Workshop content generated successfully
- ✅ Showroom template integration working
- ✅ Workspace coordination functional
- ✅ Pipeline integration operational

## Related ADRs

- **ADR-0001**: Workshop Template Strategy (defines content creation requirements)
- **ADR-0007**: Enhanced Workspace Strategy (defines workspace architecture)
- **ADR-0008**: Shared PVC Implementation (defines storage strategy)
- **ADR-0016**: Template Converter Agent (provides analysis input)
- **ADR-0018**: Source Manager Agent (handles content deployment)

---

**This ADR documents the actual implemented and operational Content Creator Agent architecture with workspace integration as deployed in the Workshop Template System.**
