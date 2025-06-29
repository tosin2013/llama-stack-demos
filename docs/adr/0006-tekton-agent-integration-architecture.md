# ADR-0006: Tekton-Agent Integration Architecture

## Status
Proposed

## Context

The Workshop Template System has evolved to include both a 6-agent system (ADR-0002) and pipeline integration capabilities (ADR-0003). However, the integration between Tekton pipelines and the agent system requires a clear architectural definition to ensure:

1. **Agent Autonomy Preservation**: Agents must remain callable independently for testing and development
2. **Pipeline Orchestration**: Tekton pipelines need to coordinate agent workflows systematically
3. **Human Oversight Integration**: Human approval gates must work seamlessly within pipeline execution
4. **Code Update Workflows**: Agents must be able to update workshop code through pipeline orchestration
5. **Backward Compatibility**: Existing agent communication patterns must be preserved

### Current Architecture Analysis

**Existing Agent Infrastructure (ADR-0002):**
- 6 specialized agents with HTTP endpoints
- @client_tool decorated functions for agent capabilities
- AgentInteractionResource.java providing standardized endpoints
- Human Oversight Coordinator with chat, command, and approval APIs

**Existing Pipeline Infrastructure (ADR-0003):**
- Tekton pipelines for Workflow 1 (new workshop creation) and Workflow 3 (existing workshop enhancement)
- BuildConfig integration for OpenShift deployment
- Human oversight integration for approval workflows

**Integration Challenges:**
- How do Tekton tasks call agent endpoints?
- How do agents return results to pipeline tasks?
- How does human oversight work within pipeline execution?
- How do agents update code through pipeline orchestration?
- How to maintain agent independence while enabling pipeline coordination?

## Decision

We will implement **Tekton-Agent Integration Architecture** that preserves agent autonomy while enabling robust pipeline orchestration through HTTP-based communication patterns.

### Core Architecture Decisions

#### 1. HTTP-Based Agent Communication
- **Tekton Tasks as Agent Wrappers**: Each agent gets corresponding Tekton task that calls agent HTTP endpoint
- **Curl-Based Communication**: Tasks use curl to call existing agent endpoints via HTTP
- **JSON Request-Response**: Standard HTTP/JSON communication pattern
- **No Agent Modifications**: Agents remain unchanged, preserving existing @client_tool functions

#### 2. Dual Access Pattern
- **Pipeline Access**: Tekton pipelines → Agent HTTP endpoints → Tool execution
- **Direct Access**: Python scripts → Agent HTTP endpoints → Tool execution (unchanged)
- **Human Interface**: Dashboard → Human Oversight APIs → Agent coordination (unchanged)

#### 3. Code Update Workflow Integration
- **Workspace Sharing**: Tekton shared workspaces enable code flow between agent tasks
- **Git Integration**: Source Manager Agent handles all Git operations (clone, commit, push)
- **Content Enhancement**: Content Creator Agent modifies workshop code and content
- **Deployment Automation**: BuildConfig triggers automatic deployment of updated code

#### 4. Human Oversight Pipeline Integration
- **Approval Gates**: Manual Tekton tasks that call existing Human Oversight APIs
- **Real-time Interaction**: Python scripts provide human interface during pipeline execution
- **Existing API Reuse**: Leverages /api/oversight/chat, /api/oversight/coordinate, approval endpoints

## Implementation Strategy

### Phase 1: Tekton Agent Task Templates

#### Agent Task Template Pattern
```yaml
apiVersion: tekton.dev/v1beta1
kind: Task
metadata:
  name: agent-task-template-converter
spec:
  params:
  - name: repository-url
  - name: analysis-type
  results:
  - name: analysis-result
  steps:
  - name: call-agent
    image: registry.access.redhat.com/ubi8/ubi:latest
    script: |
      #!/bin/bash
      set -e
      
      # Call existing agent endpoint
      RESPONSE=$(curl -s -X POST http://template-converter-agent:80/tools/analyze_repository_structure_tool \
        -H "Content-Type: application/json" \
        -d "{\"repository_url\": \"$(params.repository-url)\", \"analysis_type\": \"$(params.analysis-type)\"}")
      
      # Parse JSON response and extract result
      echo "$RESPONSE" | jq -r '.result' > $(results.analysis-result.path)
      
      # Check for errors
      if echo "$RESPONSE" | jq -e '.error' > /dev/null; then
        echo "Agent error: $(echo "$RESPONSE" | jq -r '.error')"
        exit 1
      fi
```

#### Agent Task Coverage
- **agent-task-template-converter**: Repository analysis and classification
- **agent-task-content-creator**: Workshop content creation and enhancement
- **agent-task-source-manager**: Git operations and repository management
- **agent-task-research-validation**: Content validation and accuracy checking
- **agent-task-documentation-pipeline**: Documentation generation and updates
- **agent-task-workshop-chat**: Workshop chat interface setup

### Phase 2: Human Oversight Integration

#### Human Approval Gate Pattern
```yaml
apiVersion: tekton.dev/v1beta1
kind: Task
metadata:
  name: human-oversight-approval
spec:
  params:
  - name: workflow-id
  - name: approval-type
  - name: approval-data
  results:
  - name: approval-status
  steps:
  - name: request-approval
    image: registry.access.redhat.com/ubi8/ubi:latest
    script: |
      #!/bin/bash
      
      # Use existing human oversight APIs
      curl -X POST http://workshop-monitoring-service:8080/api/oversight/chat \
        -H "Content-Type: application/json" \
        -d "{\"message\": \"Approval required for $(params.approval-type): $(params.workflow-id)\"}"
      
      # Poll for approval decision
      while true; do
        STATUS=$(curl -s http://workshop-monitoring-service:8080/api/oversight/workflows/$(params.workflow-id)/status)
        if echo "$STATUS" | jq -e '.approved' > /dev/null; then
          echo "approved" > $(results.approval-status.path)
          break
        fi
        sleep 30
      done
```

### Phase 3: Code Update Workflow Implementation

#### Workflow 3: Existing Workshop Enhancement Code Update Flow
```yaml
# 1. Clone original workshop code
- name: original-workshop-cloning
  taskRef:
    name: agent-task-source-manager
  params:
  - name: action
    value: "clone-original-workshop"
  workspaces:
  - name: output
    workspace: shared-data

# 2. Enhance workshop content and code
- name: content-enhancement
  taskRef:
    name: agent-task-content-creator
  params:
  - name: original-content
    value: "$(tasks.original-workshop-cloning.results.cloned-content)"
  - name: enhancement-plan
    value: "$(tasks.enhancement-analysis.results.enhancement-plan)"
  workspaces:
  - name: input
    workspace: shared-data
  - name: output
    workspace: shared-data

# 3. Commit enhanced code to Gitea
- name: gitea-repository-update
  taskRef:
    name: agent-task-source-manager
  params:
  - name: action
    value: "update-gitea-repository"
  - name: enhanced-content
    value: "$(tasks.content-enhancement.results.enhanced-content)"
  workspaces:
  - name: input
    workspace: shared-data
```

### Phase 4: Python Pipeline Interface

#### TektonPipelineInterface Class
```python
class TektonPipelineInterface:
    def trigger_workflow_1(self, repo_url, workshop_name):
        """Trigger Workflow 1 pipeline with human oversight"""
        # Analyze repository to determine parameters
        analysis = self.analyze_repository(repo_url)
        
        # Create Tekton PipelineRun
        pipeline_run = self.create_pipeline_run(
            pipeline_name="workflow-1-new-workshop",
            params={
                "repository-url": repo_url,
                "workshop-name": workshop_name,
                "base-template": "showroom_template_default"
            }
        )
        
        # Monitor pipeline with human oversight integration
        return self.monitor_pipeline_with_oversight(pipeline_run.metadata.name)
    
    def monitor_pipeline_with_oversight(self, pipeline_run_name):
        """Monitor pipeline progress and handle human approval gates"""
        while True:
            status = self.get_pipeline_status(pipeline_run_name)
            
            # Check for human approval gates
            if self.is_waiting_for_approval(status):
                self.handle_human_approval_gate(status)
            
            if status.completion_time:
                break
                
            time.sleep(30)
```

## Benefits

### 1. Agent Autonomy Preservation
- **Zero Agent Modifications**: Existing agent code remains unchanged
- **Independent Testing**: Agents still callable directly for development
- **Preserved Functionality**: All existing @client_tool functions continue to work

### 2. Robust Pipeline Orchestration
- **Reliable Execution**: Tekton provides enterprise-grade pipeline reliability
- **Scalable Architecture**: Tekton native scaling capabilities
- **Error Handling**: Built-in retry mechanisms and failure handling

### 3. Enhanced Human Oversight
- **Seamless Integration**: Existing human oversight APIs work within pipelines
- **Real-time Interaction**: Python interface provides human control during execution
- **Approval Workflows**: Manual gates at critical decision points

### 4. Code Update Capabilities
- **Systematic Updates**: Agents can modify workshop code through pipeline orchestration
- **Version Control**: All changes tracked through Git integration
- **Automated Deployment**: Updated code automatically triggers new workshop builds

## Consequences

### Positive
- **Maintains Backward Compatibility**: All existing patterns continue to work
- **Adds Pipeline Benefits**: Reliability, scalability, monitoring
- **Enables Code Updates**: Systematic workshop enhancement capabilities
- **Preserves Human Control**: Oversight and approval workflows maintained

### Negative
- **Additional Complexity**: Pipeline layer adds operational overhead
- **Network Dependencies**: HTTP communication between tasks and agents
- **Learning Curve**: Team needs Tekton knowledge for pipeline management

## Related ADRs

- **ADR-0001**: Workshop Template Strategy - Provides Workflow 1 and Workflow 3 definitions that this architecture implements
- **ADR-0002**: Human-in-the-Loop Integration - Defines the 6-agent system and human oversight coordinator that this architecture integrates
- **ADR-0003**: Agent-Pipeline Integration - Enhanced with this hybrid Tekton-Python approach, supersedes ADR-0005
- **ADR-0004**: Monitoring Dashboard Integration - Monitoring infrastructure that supports pipeline and agent visibility
- **ADR-0005**: End-to-End Python Testing Workflow - Superseded by ADR-0003 enhanced with this architecture

## Implementation Timeline

- **Week 1**: Tekton Agent Task Templates creation
- **Week 2**: Human Oversight Approval Task integration
- **Week 3**: BuildConfig Trigger Task and pipeline updates
- **Week 4**: Python Pipeline Interface implementation
- **Week 5**: End-to-end testing and validation

---

**Date**: 2025-06-29  
**Participants**: Workshop Template System Development Team  
**Review Date**: 2025-09-29 (3 months)  
**Dependencies**: ADR-0001, ADR-0002, ADR-0003, ADR-0004  
**Status Update**: Tekton-Agent Integration Architecture PROPOSED (2025-06-29)
