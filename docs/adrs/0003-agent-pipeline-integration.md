# ADR-0003: Agent-Pipeline Integration for Database Loading and Workshop Deployment

## Status
Proposed

## Context

The Workshop Template System currently uses OpenShift BuildConfigs for container builds and deployments, but lacks integration between the 7-agent system and CI/CD pipelines for:

1. **Database Loading**: RAG content, workshop metadata, and evolution tracking data need systematic loading
2. **Workshop Deployment**: Generated workshops need automated deployment to multiple platforms
3. **Content Synchronization**: Agent-generated content needs pipeline-driven validation and deployment
4. **Evolution Tracking**: Database updates from agent activities need pipeline coordination

### Current Infrastructure Analysis

**Existing BuildConfig Infrastructure:**
- `workshop-system-build`: Main agent system container builds
- `workshop-monitoring-service-build`: Monitoring service builds
- ~~`healthcare-ml-workshop-build`: Legacy workshop builds (TO BE REMOVED)~~
- ~~`openshift-baremetal-workshop-build`: Legacy workshop builds (TO BE REMOVED)~~

**Current Limitations:**
- No systematic database loading for RAG content
- Manual workshop deployment processes
- No pipeline integration for agent-generated content
- Limited evolution tracking data persistence
- Legacy static workshop builds need replacement with dynamic generation
- No automated pipeline for new workshop creation from agent output

**Integration Requirements:**
- Agents need to trigger pipeline workflows
- Pipelines need to load databases (Milvus, H2, Gitea)
- Workshop content needs automated deployment validation
- Evolution tracking needs persistent storage coordination

## Decision

We will implement **Agent-Pipeline Integration** using OpenShift Pipelines (Tekton) to coordinate database loading, content deployment, and evolution tracking.

### Core Architecture Decisions

#### 1. Pipeline Trigger Strategy
- **Agent-Initiated Triggers**: Agents trigger pipelines via webhook/API calls
- **Event-Driven Architecture**: Pipeline triggers based on agent workflow completion
- **Human Oversight Integration**: Approval workflows integrated with pipeline gates

#### 2. Database Loading Pipeline
- **RAG Content Pipeline**: Load workshop content into Milvus vector database
- **Metadata Pipeline**: Load workshop metadata into monitoring service database
- **Evolution Tracking Pipeline**: Initialize and update evolution tracking data

#### 3. Dynamic Workshop Creation Pipeline
- **Agent-Generated Content**: Process output from Content Creator and Template Converter agents
- **Multi-Platform Deployment**: Deploy to OpenShift, GitHub Pages, RHPDS, Showroom
- **Content Validation**: Automated testing of generated workshop content
- **Rollback Capabilities**: Version-controlled deployment with rollback support

#### 4. Legacy Build Removal Strategy
- **Remove Static Builds**: Eliminate `healthcare-ml-workshop-build` and `openshift-baremetal-workshop-build`
- **Dynamic Generation**: Replace with agent-driven workshop creation pipelines
- **Migration Path**: Convert existing workshops to agent-generated format
- **Backward Compatibility**: Maintain existing workshop URLs during transition

#### 5. Agent-Pipeline Communication
- **Webhook Integration**: Agents call pipeline webhooks with structured payloads
- **Status Monitoring**: Agents monitor pipeline execution status
- **Result Integration**: Pipeline results integrated back into agent workflows

## Implementation Strategy

### Phase 1: Database Loading Pipelines (2-3 weeks)

#### RAG Content Loading Pipeline
```yaml
apiVersion: tekton.dev/v1beta1
kind: Pipeline
metadata:
  name: rag-content-loading-pipeline
spec:
  params:
  - name: workshop-repository-url
  - name: content-type
  - name: vector-collection-name
  tasks:
  - name: extract-content
    taskRef:
      name: workshop-content-extractor
  - name: generate-embeddings
    taskRef:
      name: embedding-generator
    runAfter: [extract-content]
  - name: load-milvus
    taskRef:
      name: milvus-loader
    runAfter: [generate-embeddings]
  - name: update-metadata
    taskRef:
      name: metadata-updater
    runAfter: [load-milvus]
```

#### Evolution Tracking Database Pipeline
```yaml
apiVersion: tekton.dev/v1beta1
kind: Pipeline
metadata:
  name: evolution-tracking-pipeline
spec:
  params:
  - name: evolution-event-type
  - name: workshop-id
  - name: agent-id
  tasks:
  - name: validate-evolution-data
    taskRef:
      name: evolution-data-validator
  - name: update-tracking-database
    taskRef:
      name: h2-database-updater
    runAfter: [validate-evolution-data]
  - name: trigger-monitoring-update
    taskRef:
      name: monitoring-service-notifier
    runAfter: [update-tracking-database]
```

### Phase 2: Dynamic Workshop Creation Pipelines (2-3 weeks)

#### New Workshop Creation Pipeline
```yaml
apiVersion: tekton.dev/v1beta1
kind: Pipeline
metadata:
  name: new-workshop-creation-pipeline
spec:
  params:
  - name: workshop-name
  - name: source-repository-url
  - name: workshop-type  # repository-conversion, original-content
  - name: agent-output-data
  - name: approval-status
  tasks:
  - name: create-gitea-repository
    taskRef:
      name: gitea-repository-creator
  - name: process-agent-content
    taskRef:
      name: agent-content-processor
    runAfter: [create-gitea-repository]
  - name: generate-workshop-structure
    taskRef:
      name: workshop-structure-generator
    runAfter: [process-agent-content]
  - name: validate-workshop-content
    taskRef:
      name: workshop-content-validator
    runAfter: [generate-workshop-structure]
  - name: create-dynamic-buildconfig
    taskRef:
      name: dynamic-buildconfig-creator
    runAfter: [validate-workshop-content]
  - name: trigger-initial-build
    taskRef:
      name: workshop-builder-trigger
    runAfter: [create-dynamic-buildconfig]
  - name: deploy-to-platforms
    taskRef:
      name: multi-platform-deployer
    runAfter: [trigger-initial-build]
```

#### Legacy Workshop Removal Pipeline
```yaml
apiVersion: tekton.dev/v1beta1
kind: Pipeline
metadata:
  name: legacy-workshop-cleanup-pipeline
spec:
  params:
  - name: legacy-workshop-names
  tasks:
  - name: backup-legacy-content
    taskRef:
      name: workshop-content-backup
  - name: migrate-to-agent-format
    taskRef:
      name: legacy-workshop-migrator
    runAfter: [backup-legacy-content]
  - name: remove-legacy-buildconfigs
    taskRef:
      name: buildconfig-cleanup
    runAfter: [migrate-to-agent-format]
  - name: update-deployment-references
    taskRef:
      name: deployment-reference-updater
    runAfter: [remove-legacy-buildconfigs]
```

### Phase 3: Agent Integration (1-2 weeks)

#### Agent Pipeline Trigger Tools
```python
@client_tool
def trigger_rag_loading_pipeline(
    workshop_repository_url: str,
    content_type: str = "markdown",
    vector_collection_name: str = "workshop_content"
) -> str:
    """
    Trigger RAG content loading pipeline for workshop content
    """
    pipeline_payload = {
        "workshop-repository-url": workshop_repository_url,
        "content-type": content_type,
        "vector-collection-name": vector_collection_name
    }
    
    response = requests.post(
        f"{TEKTON_WEBHOOK_URL}/rag-content-loading-pipeline",
        json=pipeline_payload,
        headers={"Authorization": f"Bearer {PIPELINE_TOKEN}"}
    )
    
    return f"RAG loading pipeline triggered: {response.json()['pipelineRun']}"

@client_tool
def trigger_workshop_deployment_pipeline(
    workshop_repository_url: str,
    deployment_targets: List[str],
    approval_status: str = "approved"
) -> str:
    """
    Trigger workshop deployment pipeline for approved content
    """
    pipeline_payload = {
        "workshop-repository-url": workshop_repository_url,
        "deployment-targets": deployment_targets,
        "approval-status": approval_status
    }
    
    response = requests.post(
        f"{TEKTON_WEBHOOK_URL}/workshop-deployment-pipeline",
        json=pipeline_payload,
        headers={"Authorization": f"Bearer {PIPELINE_TOKEN}"}
    )
    
    return f"Deployment pipeline triggered: {response.json()['pipelineRun']}"
```

### Phase 4: Advanced Integration (2-3 weeks)

#### Pipeline Status Monitoring
```python
@client_tool
def monitor_pipeline_status(
    pipeline_run_name: str,
    timeout_minutes: int = 30
) -> str:
    """
    Monitor pipeline execution status and return results
    """
    start_time = time.time()
    timeout_seconds = timeout_minutes * 60
    
    while time.time() - start_time < timeout_seconds:
        response = requests.get(
            f"{TEKTON_API_URL}/pipelineruns/{pipeline_run_name}",
            headers={"Authorization": f"Bearer {PIPELINE_TOKEN}"}
        )
        
        status = response.json()["status"]["conditions"][0]["status"]
        
        if status == "True":
            return f"Pipeline {pipeline_run_name} completed successfully"
        elif status == "False":
            return f"Pipeline {pipeline_run_name} failed: {response.json()['status']['conditions'][0]['message']}"
        
        time.sleep(10)
    
    return f"Pipeline {pipeline_run_name} timeout after {timeout_minutes} minutes"
```

## Integration Points

### 1. Source Manager Agent Enhancement
- Add pipeline trigger tools for workshop deployment
- Integrate with BuildConfig and Tekton pipelines
- Coordinate multi-platform deployments

### 2. Workshop Chat Agent Enhancement  
- Add RAG content loading pipeline triggers
- Integrate with Milvus loading workflows
- Coordinate content updates with pipeline validation

### 3. Human Oversight Coordinator Enhancement
- Add approval workflow integration with pipeline gates
- Coordinate pipeline execution with human approval status
- Integrate evolution tracking with pipeline results
- **NEW**: Natural language chat interface for pipeline coordination
- **NEW**: Command execution interface for pipeline management
- **NEW**: Real-time workflow approval and rejection capabilities

### 4. Documentation Pipeline Agent Enhancement
- Add content monitoring pipeline triggers
- Integrate with automated content validation pipelines
- Coordinate update proposals with deployment pipelines

## Technical Requirements

### Pipeline Infrastructure
- **OpenShift Pipelines (Tekton)**: Primary pipeline execution engine
- **Webhook Triggers**: Agent-to-pipeline communication
- **Pipeline Storage**: Persistent volumes for pipeline artifacts
- **Secret Management**: Secure credential handling for pipeline tasks

### Database Integration
- **Milvus Integration**: RAG content loading and management
- **H2 Database Integration**: Evolution tracking data management
- **Gitea Integration**: Repository management and webhook coordination
- **Monitoring Database**: Workshop metadata and status tracking

### Security and Compliance
- **RBAC Integration**: Pipeline execution permissions
- **Audit Logging**: Complete pipeline execution audit trails
- **Secret Rotation**: Automated credential management
- **Network Policies**: Secure agent-pipeline communication

## Benefits

### Operational Benefits
- **Automated Database Loading**: Systematic RAG content and metadata management
- **Consistent Deployments**: Standardized workshop deployment across platforms
- **Evolution Tracking**: Persistent tracking of workshop evolution and changes
- **Reduced Manual Work**: Automated pipeline execution reduces manual intervention

### Quality Benefits
- **Content Validation**: Automated testing of generated workshop content
- **Deployment Verification**: Systematic validation of workshop deployments
- **Rollback Capabilities**: Version-controlled deployments with rollback support
- **Audit Trails**: Complete tracking of content changes and deployments

### Scalability Benefits
- **Parallel Processing**: Multiple pipeline executions for concurrent workshops
- **Resource Optimization**: Efficient resource utilization through pipeline scheduling
- **Platform Agnostic**: Consistent deployment across multiple target platforms
- **Load Distribution**: Distributed pipeline execution across cluster resources

## Risks and Mitigation

### Technical Risks
- **Pipeline Complexity**: Complex pipeline definitions may be difficult to maintain
  - *Mitigation*: Modular pipeline design with reusable tasks
- **Agent-Pipeline Coupling**: Tight coupling between agents and pipelines
  - *Mitigation*: Event-driven architecture with loose coupling
- **Database Consistency**: Concurrent pipeline executions may cause data conflicts
  - *Mitigation*: Database locking and transaction management

### Operational Risks
- **Pipeline Failures**: Failed pipelines may block workshop deployment
  - *Mitigation*: Comprehensive error handling and retry mechanisms
- **Resource Contention**: Multiple pipelines may compete for cluster resources
  - *Mitigation*: Resource quotas and pipeline scheduling policies
- **Security Vulnerabilities**: Pipeline execution may expose sensitive data
  - *Mitigation*: Secure secret management and network isolation

## Success Metrics

### Performance Metrics
- **Pipeline Execution Time**: Average time for database loading and deployment pipelines
- **Success Rate**: Percentage of successful pipeline executions
- **Resource Utilization**: Efficient use of cluster resources for pipeline execution

### Quality Metrics
- **Content Validation Rate**: Percentage of workshop content passing automated validation
- **Deployment Success Rate**: Percentage of successful workshop deployments
- **Rollback Frequency**: Number of deployments requiring rollback

### User Experience Metrics
- **Agent Response Time**: Time from agent trigger to pipeline completion
- **Workshop Availability**: Uptime of deployed workshops across platforms
- **Evolution Tracking Accuracy**: Accuracy of evolution tracking data

## Implementation Timeline

### Week 1-2: Pipeline Infrastructure Setup
- Install and configure OpenShift Pipelines (Tekton)
- Create basic pipeline tasks for database operations
- Set up webhook triggers and authentication

### Week 3-4: Database Loading Pipelines
- Implement RAG content loading pipeline
- Create evolution tracking database pipeline
- Integrate with Milvus and H2 databases

### Week 5-6: Workshop Deployment Pipelines
- Create multi-platform deployment pipeline
- Implement content validation and testing
- Add rollback and version management

### Week 7-8: Agent Integration
- Add pipeline trigger tools to agents
- Implement pipeline status monitoring
- Integrate with Human Oversight Coordinator

### Week 9-10: Testing and Optimization
- Comprehensive testing of all pipeline workflows
- Performance optimization and resource tuning
- Documentation and training materials

## Hybrid Tekton-Python Pipeline Integration

### Architecture Overview

The Workshop Template System now implements a **hybrid Tekton-Python architecture** that combines cloud-native pipeline orchestration with flexible human oversight capabilities:

- **Tekton Pipelines**: Handle core workflow orchestration for ADR-0001 processes
- **Python Interface**: Provides human-in-the-loop interaction with pipelines and agents
- **Human Oversight Integration**: Seamless integration with existing oversight APIs
- **Kustomize Configuration**: Repository-specific and environment-specific configurations

### Core Components

#### 1. Tekton Pipeline Orchestration
- **Workflow 1 Pipeline**: `tekton/pipelines/workflow-1-new-workshop.yaml` for new workshop creation
- **Workflow 3 Pipeline**: `tekton/pipelines/workflow-3-enhance-workshop.yaml` for existing workshop enhancement
- **Agent Task Integration**: Reusable Tekton tasks for each of the 6 agents
- **Human Approval Gates**: Manual Tekton tasks integrated with human oversight APIs

#### 2. Python Human Interface Layer
- **Pipeline Triggering**: Python scripts analyze repositories and trigger appropriate Tekton pipelines
- **Human Oversight Integration**: Real-time interaction with existing oversight APIs during pipeline execution
- **Monitoring and Control**: Pipeline progress monitoring and human intervention capabilities
- **End-to-End Testing**: Comprehensive testing framework for complete workflows

#### 3. Kustomize Configuration Management
- **Repository-Specific Overlays**: Configurations for DDD Hexagonal, Ansible CaC, Llama Stack Demos
- **Workflow Parameters**: Different configurations for Workflow 1 vs Workflow 3
- **Environment Management**: Dev, staging, production environment configurations

### Interactive Coordination Capabilities

#### 1. Natural Language Chat Interface
- **Pipeline Status Queries**: "What workflows need my attention?"
- **System Health Monitoring**: "Show me the current system status"
- **Agent Coordination**: "How are the agents performing?"
- **Contextual Responses**: Real-time system information with confidence scoring

#### 2. Command Execution Interface
- **System Commands**: `system health`, `agent status`, `quality check`
- **Pipeline Management**: `list workflows`, `approve workflow <id>`, `reject workflow <id>`
- **Real-time Execution**: Sub-millisecond command processing with audit trails
- **Integration**: Direct integration with AgentHealthService and monitoring infrastructure

#### 3. Workflow Approval Integration
- **Approval Workflows**: Human approval gates integrated with Tekton pipeline execution
- **Rejection Handling**: Comprehensive rejection workflows with audit trails
- **Decision Tracking**: Complete audit trail of human oversight decisions
- **Multi-platform Coordination**: Approval workflows across OpenShift, GitHub Pages, RHPDS

### Implementation Status

✅ **Human Oversight APIs**: All oversight APIs implemented and tested
- `/api/oversight/chat` - Natural language interaction
- `/api/oversight/coordinate` - Command execution and workflow coordination
- `/api/oversight/workflows/{id}/reject` - Workflow rejection with audit trail

✅ **Frontend Integration**: Dashboard with 4 interaction modes
- Overview: Real-time system status and metrics
- Chat: Natural language interaction with oversight coordinator
- Commands: Terminal-style command execution interface
- Approvals: Workflow approval and rejection management

✅ **System Integration**: Complete integration with existing infrastructure
- AgentHealthService integration for real system data
- 7 agents reporting HEALTHY status with performance metrics
- Real-time monitoring and coordination capabilities

🚧 **Tekton Pipeline Integration**: In Progress
- Tekton pipeline definitions for Workflow 1 and Workflow 3
- Kustomize overlays for repository-specific configurations
- Python interface for pipeline interaction and human oversight

### Testing and Validation

**Test Repositories**:
- https://github.com/jeremyrdavis/dddhexagonalworkshop (Workflow 3)
- https://github.com/tosin2013/ansible-controller-cac.git (Workflow 1)
- https://github.com/tosin2013/llama-stack-demos (Workflow 1)

**Test Scripts**:
- `test_human_oversight_workflow.sh` - Human oversight API testing
- `test_e2e_tekton_human_oversight.py` - Hybrid Tekton-Python workflow testing

**Validation Results**: Human oversight features operational, Tekton integration in progress

## Review and Monitoring

- **Implementation Review**: 30 days after completion
- **Performance Assessment**: Quarterly review of pipeline metrics
- **Process Optimization**: Continuous improvement based on usage patterns
- **Security Review**: Annual security assessment of pipeline infrastructure
- **Human Oversight Metrics**: Monthly review of approval efficiency and decision quality

## ADR-0005 Supersession Notice

**ADR-0005 (End-to-End Python Testing Workflow) has been superseded by this enhanced ADR-0003.**

The concepts from ADR-0005 have been integrated into this hybrid Tekton-Python approach:
- **Python Testing Framework**: Absorbed into Python Human Interface Layer
- **End-to-End Validation**: Implemented as Tekton pipeline testing with Python oversight
- **Repository Testing**: Integrated into Kustomize overlay configurations
- **Human Oversight Testing**: Maintained and enhanced with Tekton integration

**Migration Path**:
- Use `test_e2e_tekton_human_oversight.py` instead of the proposed ADR-0005 Python scripts
- Tekton pipelines provide the orchestration framework that ADR-0005 attempted to recreate in Python
- Human oversight integration remains unchanged and is enhanced with pipeline approval gates

---

**Date**: 2025-06-29
**Participants**: Workshop Template System Development Team
**Review Date**: 2025-09-29 (3 months)
**Dependencies**: ADR-0001 (Workshop Template Strategy), ADR-0002 (Human-in-the-Loop Integration)
**Related ADRs**: ADR-0006 (Tekton-Agent Integration Architecture) - Detailed implementation of hybrid approach
**Status Update**:
- Human Oversight Pipeline Integration COMPLETED (2025-06-29)
- Hybrid Tekton-Python Integration IN PROGRESS (2025-06-29)
- ADR-0005 SUPERSEDED by enhanced ADR-0003 (2025-06-29)
- ADR-0006 CREATED for detailed Tekton-Agent integration architecture (2025-06-29)
