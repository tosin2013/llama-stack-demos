# ADR-0023: OpenShift Deployment Strategy

## Status
Accepted - **IMPLEMENTED AND OPERATIONAL**

## Context

The OpenShift deployment strategy defines the comprehensive approach for deploying, managing, and scaling the Workshop Template System in OpenShift environments. This strategy encompasses agent deployment patterns, service configurations, resource management, and operational procedures.

**Current Implementation Status:**
- ✅ **FULLY DEPLOYED**: Complete system operational in OpenShift
- ✅ **6 AGENTS RUNNING**: All workshop agents deployed and functional
- ✅ **SERVICES OPERATIONAL**: All services, routes, and supporting infrastructure working
- ✅ **PROVEN SCALABLE**: Successfully processing real workshop creation workflows

## Decision

### **Deployment Architecture Overview**

#### **1. Namespace Organization**
```yaml
# Primary Namespaces
workshop-system:          # Main workshop agents and services
  - 6 Workshop Agents
  - Workshop Monitoring Service
  - Supporting Infrastructure (Milvus, etcd, MinIO)
  
llama-serve:             # LLM Infrastructure
  - Llama Stack Server
  - Model serving components
  
gitea:                   # Repository Management
  - Gitea server and operator
  - Repository storage and management
```

**Namespace Benefits:**
- **Isolation**: Clear separation of concerns and resource isolation
- **Security**: RBAC and network policies per namespace
- **Resource Management**: Granular resource allocation and monitoring
- **Operational Clarity**: Simplified troubleshooting and maintenance

### **2. Single Container Image Strategy**

#### **Agent Deployment Pattern**
```yaml
# Unified Agent Container Strategy
spec:
  containers:
  - name: {agent-name}
    image: image-registry.openshift-image-registry.svc:5000/workshop-system/workshop-agent-system:latest
    command: ["python", "-m", "demos.workshop_template_system", "--agent-name", "{agent_name}", "--port", "8080"]
    ports:
    - containerPort: 8080
      name: http
```

**Strategic Advantages:**
- **Simplified Deployment**: Single image for all agents reduces complexity
- **Consistent Updates**: All agents updated simultaneously with single image build
- **Resource Efficiency**: Shared base image layers reduce storage requirements
- **Operational Simplicity**: Single BuildConfig and ImageStream for all agents

#### **Agent Differentiation**
```bash
# Command-line Agent Selection
workshop-chat:              --agent-name workshop_chat
template-converter:         --agent-name template_converter
content-creator:           --agent-name content_creator
source-manager:            --agent-name source_manager
research-validation:       --agent-name research_validation
documentation-pipeline:    --agent-name documentation_pipeline
```

### **3. Service and Route Configuration**

#### **Standardized Service Pattern**
```yaml
# ClusterIP Service Template
apiVersion: v1
kind: Service
metadata:
  name: {agent-name}
  namespace: workshop-system
spec:
  selector:
    app: {agent-name}
  ports:
  - port: 80
    targetPort: 8080
    name: http
  type: ClusterIP
```

#### **HTTPS Route Configuration**
```yaml
# Secure Route Template
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: {agent-name}
  namespace: workshop-system
spec:
  to:
    kind: Service
    name: {agent-name}
  port:
    targetPort: http
  tls:
    termination: edge
    insecureEdgeTerminationPolicy: Redirect
  wildcardPolicy: None
```

**Security Features:**
- **TLS Termination**: All external traffic encrypted via HTTPS
- **Redirect Policy**: HTTP traffic automatically redirected to HTTPS
- **Internal Communication**: ClusterIP services for secure internal communication

### **4. Resource Allocation Strategy**

#### **Standard Agent Resource Profile**
```yaml
# Resource Configuration Template
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1"
```

#### **Specialized Resource Profiles**
```yaml
# Workshop Chat Agent (High Availability)
workshop-chat-agent:
  replicas: 2
  resources:
    requests:
      memory: "1Gi"
      cpu: "500m"
    limits:
      memory: "2Gi"
      cpu: "1"

# Workshop Monitoring Service (Frontend + Backend)
workshop-monitoring-service:
  replicas: 1
  resources:
    requests:
      memory: "2Gi"
      cpu: "1"
    limits:
      memory: "4Gi"
      cpu: "2"
```

### **5. Configuration Management**

#### **ConfigMap Strategy**
```yaml
# Central Configuration Management
apiVersion: v1
kind: ConfigMap
metadata:
  name: workshop-system-config
  namespace: workshop-system
data:
  llama_stack_endpoint: "http://llamastack-server.llama-serve.svc.cluster.local:8321"
  inference_model_id: "meta-llama/Llama-3.2-3B-Instruct"
  milvus_endpoint: "http://milvus:19530"
  gitea_base_url: "https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
```

#### **Secret Management**
```yaml
# Secure Credential Storage
apiVersion: v1
kind: Secret
metadata:
  name: workshop-system-secrets
  namespace: workshop-system
type: Opaque
data:
  GITHUB_TOKEN: <base64-encoded>
  
---
apiVersion: v1
kind: Secret
metadata:
  name: gitea-credentials
  namespace: workshop-system
type: Opaque
data:
  GITEA_TOKEN: <base64-encoded>
```

### **6. Health Monitoring and Probes**

#### **Standardized Health Checks**
```yaml
# Health Probe Configuration
livenessProbe:
  httpGet:
    path: /agent-card
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  
readinessProbe:
  httpGet:
    path: /agent-card
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

**Health Check Features:**
- **Agent Card Endpoint**: Standardized health and capability reporting
- **Startup Delays**: Appropriate delays for agent initialization
- **Failure Recovery**: Automatic pod restart on health check failures

### **7. Storage and Persistence**

#### **Shared Workspace Strategy**
```yaml
# RWX Persistent Volume Claim
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: workshop-shared-workspace
  namespace: workshop-system
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 100Gi
  storageClassName: ocs-storagecluster-cephfs
```

**Storage Integration:**
- **Shared Workspace**: RWX PVC for agent coordination and content sharing
- **Content Creator**: Workspace-enabled with shared PVC mounting
- **Source Manager**: Workspace access for content synchronization
- **Coordination Scripts**: Helper scripts mounted for workspace management

### **8. CI/CD and Build Strategy**

#### **BuildConfig Integration**
```yaml
# Workshop System BuildConfig
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: workshop-system-build
  namespace: workshop-system
spec:
  source:
    type: Git
    git:
      uri: "https://github.com/tosin2013/llama-stack-demos.git"
      ref: "main"
  strategy:
    type: Docker
    dockerStrategy:
      dockerfilePath: "Dockerfile"
  output:
    to:
      kind: ImageStreamTag
      name: "workshop-agent-system:latest"
```

**Build and Deployment Process:**
1. **Git Integration**: Automatic builds from Git repository changes
2. **Container Registry**: Internal OpenShift registry for image storage
3. **Automatic Deployment**: Rolling updates triggered by new builds
4. **Version Management**: ImageStream tags for version control

### **9. Network and Security**

#### **Service Mesh Integration**
```yaml
# Internal Service Communication
workshop-chat-agent:80 → template-converter-agent:80
content-creator-agent:80 → source-manager-agent:80
milvus:19530 → workshop-chat-agent (RAG integration)
llamastack-server.llama-serve:8321 → all agents
```

#### **RBAC Configuration**
```yaml
# Service Account and RBAC
apiVersion: v1
kind: ServiceAccount
metadata:
  name: workshop-system-sa
  namespace: workshop-system
  
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: workshop-system-role
rules:
- apiGroups: [""]
  resources: ["pods", "services", "configmaps"]
  verbs: ["get", "list", "watch"]
```

### **10. Monitoring and Observability**

#### **Operational Monitoring**
```yaml
# Workshop Monitoring Service
workshop-monitoring-service:
  - Real-time agent health monitoring
  - System status dashboard
  - Chat interface for human oversight
  - Approval workflow management
  - Performance metrics collection
```

**Monitoring Capabilities:**
- **Agent Health**: Real-time health status for all 6 agents
- **System Metrics**: CPU, memory, and network utilization
- **Workflow Tracking**: Pipeline execution status and progress
- **User Interface**: React dashboard for operational oversight

## Consequences

### **Positive**
- ✅ **Proven Scalability**: Successfully deployed and operational with real workloads
- ✅ **Operational Simplicity**: Single image strategy simplifies deployment and updates
- ✅ **Security**: HTTPS termination and secure internal communication
- ✅ **Resource Efficiency**: Optimized resource allocation and shared infrastructure
- ✅ **High Availability**: Multiple replicas for critical components
- ✅ **Monitoring**: Comprehensive monitoring and observability

### **Negative**
- ⚠️ **Single Image Coupling**: All agents coupled to single container image
- ⚠️ **Resource Constraints**: Fixed resource limits may impact performance
- ⚠️ **Storage Dependency**: Requires RWX storage for workspace functionality

### **Mitigation Strategies**
- **Resource Monitoring**: Continuous monitoring and adjustment of resource limits
- **Storage Backup**: Regular backup of shared workspace and critical data
- **Deployment Automation**: Automated deployment and rollback procedures

## Implementation Evidence

### **Actual Implementation Files**

**Container Build:**
- **Dockerfile**: `kubernetes/workshop-template-system/Dockerfile`
- **Entry Point**: `demos/workshop_template_system/__main__.py`
- **Dependencies**: `demos/workshop_template_system/requirements.txt` (73 packages)

### **Container Strategy Implementation**

<augment_code_snippet path="kubernetes/workshop-template-system/Dockerfile" mode="EXCERPT">
````dockerfile
FROM registry.access.redhat.com/ubi8/python-39:latest

USER root

# Install system dependencies
RUN dnf update -y && \
    dnf install -y git curl && \
    dnf clean all

USER 1001

# Set working directory
WORKDIR /opt/app-root/src

# Copy requirements first for better caching
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY demos/ ./demos/
COPY common/ ./common/
COPY *.py ./

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/agent-card || exit 1

# Start agent based on environment variable
CMD ["python", "-m", "demos.workshop_template_system"]
````
</augment_code_snippet>

### **Agent Selection Implementation**

<augment_code_snippet path="demos/workshop_template_system/__main__.py" mode="EXCERPT">
````python
import fire
from .agents.workshop_chat.agent import WorkshopChatAgent
from .agents.template_converter.agent import TemplateConverterAgent
from .agents.content_creator.agent import ContentCreatorAgent
from .agents.source_manager.agent import SourceManagerAgent
from .agents.research_validation.agent import ResearchValidationAgent
from .agents.documentation_pipeline.agent import DocumentationPipelineAgent

def main(agent_name: str = "workshop_chat", port: int = 8080):
    """Main entry point for Workshop Template System agents"""

    agents = {
        "workshop_chat": WorkshopChatAgent,
        "template_converter": TemplateConverterAgent,
        "content_creator": ContentCreatorAgent,
        "source_manager": SourceManagerAgent,
        "research_validation": ResearchValidationAgent,
        "documentation_pipeline": DocumentationPipelineAgent,
    }

    if agent_name not in agents:
        raise ValueError(f"Unknown agent: {agent_name}")

    agent_class = agents[agent_name]
    agent = agent_class(port=port)
    agent.run()

if __name__ == "__main__":
    fire.Fire(main)
````
</augment_code_snippet>

### **Dependencies Implementation**

<augment_code_snippet path="demos/workshop_template_system/requirements.txt" mode="EXCERPT">
````text
# Core A2A and Llama Stack dependencies
llama_stack_client==0.2.2
requests==2.32.3
PyYAML==6.0.2

# Workshop-specific dependencies
PyGithub==2.5.0
beautifulsoup4==4.12.3
lxml==5.3.0
aiohttp==3.11.11
Jinja2==3.1.5

# Development and testing
pytest==8.3.4
pytest-asyncio==0.25.0
````
</augment_code_snippet>

**Complete System Deployment:**
```bash
$ oc get pods -n workshop-system
NAME                                           READY   STATUS    RESTARTS   AGE
content-creator-agent-5bd649c4d-qhdc9          1/1     Running   0          86m
documentation-pipeline-agent-6d47c8c48f-ph4nq 1/1     Running   0          11h
human-oversight-coordinator-78d759dc7f-4sfw2   1/1     Running   0          6h28m
milvus-59866955f8-lclgb                        1/1     Running   0          11h
research-validation-agent-5ff47d76d9-jv5cb     1/1     Running   0          11h
source-manager-agent-85bc46dd98-zp5f2          1/1     Running   0          86m
template-converter-agent-74cb498764-24cj4      1/1     Running   0          11h
workshop-chat-agent-6b7bc64469-59nlr           1/1     Running   0          11h
workshop-chat-agent-6b7bc64469-z8hv2           1/1     Running   0          11h
workshop-monitoring-service-674c4d5c65-pdvk9   1/1     Running   0          4h34m
```

**Successful Workshop Processing:**
- ✅ DDD Hexagonal Workshop created successfully
- ✅ Repository created in Gitea: `workshop-ddd-hexagonal-workshop-demo-1751149405809`
- ✅ All agents completed workflow successfully
- ✅ End-to-end functionality proven

## Developer Quick Start

### **Local Container Build**
```bash
# Build workshop agent container
cd kubernetes/workshop-template-system
podman build -t workshop-agent-system:latest .

# Test agent locally
podman run -p 8080:8080 -e AGENT_NAME=template_converter workshop-agent-system:latest
```

### **OpenShift Deployment**
```bash
# Deploy complete system
oc apply -k kubernetes/workshop-template-system/base/

# Check deployment status
oc get pods -n workshop-system

# View agent logs
oc logs -f deployment/template-converter-agent -n workshop-system
```

### **Agent Development**
```bash
# Start specific agent for development
python -m demos.workshop_template_system --agent-name content_creator --port 8080

# Test agent health
curl http://localhost:8080/agent-card

# Invoke agent tool
curl -X POST http://localhost:8080/invoke \
  -H "Content-Type: application/json" \
  -d '{"tool_name": "create_workshop_content_tool", "parameters": {...}}'
```

### **Key Files for Developers**
- **Container**: `kubernetes/workshop-template-system/Dockerfile`
- **Entry Point**: `demos/workshop_template_system/__main__.py`
- **Dependencies**: `demos/workshop_template_system/requirements.txt`
- **Deployment**: `kubernetes/workshop-template-system/base/`
- **Agents**: `demos/workshop_template_system/agents/`

## Related ADRs

- **ADR-0007**: Enhanced Workspace Strategy (defines storage requirements)
- **ADR-0008**: Shared PVC Implementation (defines storage implementation)
- **ADR-0016-0020**: Individual Agent Architectures (define agent-specific requirements)
- **ADR-0021**: Human-in-the-Loop Integration (defines monitoring service requirements)
- **ADR-0022**: RAG System Integration (defines vector database requirements)

---

**This ADR documents the actual implemented and operational OpenShift deployment strategy with proven scalability and complete system functionality as deployed in the Workshop Template System.**
