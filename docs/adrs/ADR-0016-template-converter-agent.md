# ADR-0016: Template Converter Agent Architecture

## Status
Accepted - **IMPLEMENTED AND OPERATIONAL**

## Context

The Template Converter Agent is a critical component of the Workshop Template System responsible for repository analysis, classification, and workflow determination. This agent analyzes GitHub repositories to determine their structure and recommends appropriate workshop creation workflows.

**Current Implementation Status:**
- ✅ **DEPLOYED**: Running in OpenShift workshop-system namespace
- ✅ **OPERATIONAL**: Successfully processing repository analysis requests
- ✅ **INTEGRATED**: Connected to Tekton pipelines and HTTP endpoints
- ✅ **TESTED**: Confirmed working with real repository analysis

## Decision

### **Agent Architecture**

#### **1. Deployment Pattern**
```yaml
# Single Container Image Strategy
image: image-registry.openshift-image-registry.svc:5000/workshop-system/workshop-agent-system:latest
command: ["python", "-m", "demos.workshop_template_system", "--agent-name", "template_converter", "--port", "8080"]
```

**Key Characteristics:**
- **Single Image, Multiple Agents**: One container image serves all agents via command-line selection
- **Standardized Port**: All agents use port 8080 for consistency
- **Health Check Endpoint**: `/agent-card` provides agent status and capabilities
- **ConfigMap Configuration**: External configuration via workshop-system-config

#### **2. HTTP API Structure**
```bash
# Agent Invocation Endpoint
POST /invoke
Content-Type: application/json

{
  "tool_name": "analyze_repository_tool",
  "parameters": {
    "repository_url": "https://github.com/example/repo.git",
    "analysis_depth": "comprehensive"
  }
}
```

**Response Format:**
```json
{
  "result": "# Repository Analysis Report\n\n## Repository Classification\n...",
  "status": "success",
  "execution_time": "2.3s"
}
```

#### **3. Repository Classification Logic**

**Classification Categories:**
1. **existing_workshop**: Repositories with antora.yml, showroom.yml, or workshop framework files
2. **tutorial_content**: Educational content without workshop framework
3. **application**: Standard application repositories

**Workflow Mapping:**
- `existing_workshop` → **Workflow 3** (Enhancement) + `original_repository` template
- `tutorial_content` → **Workflow 1** (New Workshop) + `showroom_template_default` template  
- `application` → **Workflow 1** (New Workshop) + `showroom_template_default` template

### **4. Tekton Pipeline Integration**

#### **Agent Task Definition**
```yaml
apiVersion: tekton.dev/v1beta1
kind: Task
metadata:
  name: agent-task-template-converter
spec:
  params:
  - name: repository-url
  - name: analysis-depth
    default: "comprehensive"
  - name: agent-endpoint
    default: "http://template-converter-agent:80"
  
  results:
  - name: analysis-result
  - name: repository-classification  
  - name: workflow-type
  - name: template-source
```

**Integration Pattern:**
1. **Pipeline Invocation**: Tekton task calls agent HTTP endpoint
2. **Result Processing**: Parse markdown analysis result for classification
3. **Workflow Determination**: Set workflow type (1 or 3) based on classification
4. **Template Selection**: Choose appropriate base template

### **5. Configuration Management**

#### **Environment Variables**
```yaml
env:
- name: AGENT_NAME
  value: "template_converter"
- name: LLAMA_STACK_ENDPOINT
  value: "http://llamastack-server.llama-serve.svc.cluster.local:8321"
- name: INFERENCE_MODEL_ID
  value: "meta-llama/Llama-3.2-3B-Instruct"
- name: GITHUB_TOKEN
  valueFrom:
    secretKeyRef:
      name: workshop-system-secrets
      key: GITHUB_TOKEN
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

### **6. Service and Route Configuration**

#### **ClusterIP Service**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: template-converter-agent
spec:
  selector:
    app: template-converter-agent
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

#### **HTTPS Route**
```yaml
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: template-converter-agent
spec:
  tls:
    termination: edge
    insecureEdgeTerminationPolicy: Redirect
```

## Consequences

### **Positive**
- ✅ **Operational Reliability**: Agent successfully processes real repository analysis
- ✅ **Standardized Integration**: Consistent HTTP API pattern across all agents
- ✅ **Pipeline Compatibility**: Seamless integration with Tekton workflows
- ✅ **Scalable Architecture**: Single image strategy simplifies deployment and updates
- ✅ **External Access**: HTTPS routes enable direct agent interaction

### **Negative**
- ⚠️ **Single Point of Failure**: One replica means no high availability
- ⚠️ **Resource Constraints**: Fixed resource limits may impact large repository analysis
- ⚠️ **Classification Accuracy**: Markdown parsing for classification could be brittle

### **Mitigation Strategies**
- **High Availability**: Consider increasing replicas for critical workloads
- **Resource Monitoring**: Monitor CPU/memory usage and adjust limits as needed
- **Classification Robustness**: Enhance parsing logic with multiple classification signals

## Implementation Evidence

### **Actual Implementation Files**

**Primary Implementation:**
- **File**: `demos/workshop_template_system/agents/template_converter/tools.py` (804 lines)
- **Configuration**: `demos/workshop_template_system/agents/template_converter/config.py`
- **Entry Point**: `demos/workshop_template_system/__main__.py`

### **Key Tool Functions**

<augment_code_snippet path="demos/workshop_template_system/agents/template_converter/tools.py" mode="EXCERPT">
````python
@client_tool
def analyze_repository_tool(repository_url: str, analysis_depth: str = "comprehensive") -> str:
    """
    :description: Analyze GitHub repository structure and classify for workshop creation workflow.
    :use_case: Use to determine repository type and recommend appropriate workshop creation strategy.
    :param repository_url: GitHub repository URL to analyze
    :param analysis_depth: Analysis depth (basic, comprehensive, detailed)
    :returns: Detailed repository analysis with workflow recommendations
    """

def fetch_repository_structure(repository_url: str) -> dict:
    """Fetch real repository structure from GitHub API"""
    # Parse repository URL to extract owner and repo
    parsed_url = urlparse(repository_url)
    path_parts = parsed_url.path.strip('/').split('/')

    # GitHub API endpoint for repository contents
    api_url = f"https://api.github.com/repos/{owner}/{repo}/contents"

    # Get GitHub token from environment if available
    github_token = os.getenv('GITHUB_TOKEN')
    headers = {}
    if github_token and github_token != 'placeholder-github-token':
        headers['Authorization'] = f'token {github_token}'
````
</augment_code_snippet>

### **Repository Classification Logic**

<augment_code_snippet path="demos/workshop_template_system/agents/template_converter/tools.py" mode="EXCERPT">
````python
def classify_repository_structure(files: list, directories: list, readme_content: str) -> dict:
    """Classify repository based on structure and content"""

    # Workshop framework indicators
    workshop_indicators = [
        'antora.yml', 'showroom.yml', 'site.yml',
        'modules/ROOT/', 'content/modules/',
        'workshop.yml', 'labguide/'
    ]

    # Tutorial content indicators
    tutorial_indicators = [
        'tutorial', 'guide', 'walkthrough', 'lesson',
        'step-by-step', 'hands-on', 'lab'
    ]

    # Application indicators
    application_indicators = [
        'src/', 'app/', 'main.py', 'index.js',
        'package.json', 'pom.xml', 'requirements.txt'
    ]
````
</augment_code_snippet>

**OpenShift Deployment Status:**
```bash
$ oc get pods -n workshop-system | grep template-converter
template-converter-agent-74cb498764-24cj4   1/1     Running   0          11h
```

**Successful Repository Analysis:**
- ✅ DDD Hexagonal Workshop processing completed successfully
- ✅ Repository classification working correctly
- ✅ Workflow determination functional
- ✅ Tekton pipeline integration operational

## Developer Quick Start

### **Local Development**
```bash
# Navigate to agent directory
cd demos/workshop_template_system/agents/template_converter

# View agent configuration
cat config.py

# Test agent tools directly
python -c "from tools import analyze_repository_tool; print(analyze_repository_tool('https://github.com/jeremyrdavis/dddhexagonalworkshop.git'))"
```

### **Agent Invocation**
```bash
# Start Template Converter Agent
python -m demos.workshop_template_system --agent-name template_converter --port 8080

# Test HTTP endpoint
curl -X POST http://localhost:8080/invoke \
  -H "Content-Type: application/json" \
  -d '{
    "tool_name": "analyze_repository_tool",
    "parameters": {
      "repository_url": "https://github.com/example/repo.git",
      "analysis_depth": "comprehensive"
    }
  }'
```

### **Key Files for Developers**
- **Tools**: `demos/workshop_template_system/agents/template_converter/tools.py`
- **Config**: `demos/workshop_template_system/agents/template_converter/config.py`
- **Deployment**: `kubernetes/workshop-template-system/base/agents-deployment.yaml`
- **Service**: `kubernetes/workshop-template-system/base/agents-service.yaml`

## Related ADRs

- **ADR-0001**: Workshop Template Strategy (defines classification requirements)
- **ADR-0006**: Tekton-Agent Integration Architecture (defines HTTP integration pattern)
- **ADR-0023**: OpenShift Deployment Strategy (defines deployment patterns)

---

**This ADR documents the actual implemented and operational Template Converter Agent architecture as deployed in the Workshop Template System.**
