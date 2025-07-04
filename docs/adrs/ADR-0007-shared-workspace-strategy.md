# ADR-0007: Shared Workspace Strategy for Tekton-Agent Integration

**Status**: Proposed  
**Date**: 2025-06-30  
**Supersedes**: None  
**Related**: ADR-0001 (Dual-Template Strategy), ADR-0006 (Tekton-Agent Integration)

## Context

The current Tekton-Agent integration (ADR-0006) passes workshop content between pipeline tasks as JSON strings through task results. This approach has limitations:

### Current Architecture Issues
1. **No File-Based Operations**: Agents work with text content only, not actual workshop files
2. **Empty Workspaces**: Pipeline workspaces are empty, agents cannot access real workshop structure
3. **Limited Collaboration**: Agents cannot incrementally build workshop content together
4. **API-Only Operations**: All file operations happen via Gitea API, no local file manipulation

### Requirements
- Enable agents to work with actual workshop files during pipeline execution
- Support incremental content building across multiple agent stages
- Maintain compatibility with existing ADR-0001 dual-template strategy
- Preserve current HTTP API patterns while adding file-based capabilities
- Ensure workspace persistence across Tekton task boundaries

## Decision

We will implement a **Hybrid Workspace Strategy** that combines file-based operations with existing API patterns:

### Core Components

#### 1. Workspace Initialization Task
```yaml
# New task: workspace-initialization
- name: workspace-initialization
  workspaces:
  - name: shared-data
  steps:
  - name: populate-workspace
    script: |
      if [ "$(params.workflow-type)" = "3" ]; then
        # Workflow 3: Clone original workshop
        git clone $(params.repository-url) /workspace/shared-data/workshop-content
      else
        # Workflow 1: Clone showroom template
        git clone https://github.com/rhpds/showroom_template_default.git /workspace/shared-data/workshop-content
      fi
```

#### 2. Enhanced Agent Tasks
```yaml
# All agent tasks get workspace access
- name: content-creation
  taskRef:
    name: agent-task-content-creator
  workspaces:
  - name: shared-data
    workspace: shared-data
  params:
  - name: workspace-mode
    value: "hybrid"  # Support both file and API operations
```

#### 3. Agent Tool Enhancement
```python
# Enhanced agent tools for hybrid operations
@client_tool
def create_workshop_content_tool(
    repository_url: str, 
    workspace_path: str = "/workspace/shared-data",
    operation_mode: str = "hybrid"
) -> str:
    """Create workshop content with file-based operations"""
    if operation_mode == "file-based":
        # Work directly with workspace files
        return create_content_from_files(workspace_path)
    else:
        # Use existing API-based approach
        return create_content_via_api(repository_url)
```

### Implementation Phases

#### Phase 1: Workspace Infrastructure
- [ ] Create workspace-initialization task
- [ ] Update pipeline definitions to include workspace access
- [ ] Test workspace persistence across tasks

#### Phase 2: Agent Enhancement
- [ ] Add file-based operation capabilities to agent tools
- [ ] Implement hybrid mode (file + API operations)
- [ ] Update agent HTTP endpoints to support workspace parameters

#### Phase 3: Pipeline Integration
- [ ] Update all agent tasks to use shared workspace
- [ ] Implement workspace-to-Gitea synchronization
- [ ] Test complete end-to-end workflow

#### Phase 4: Optimization
- [ ] Performance tuning for file operations
- [ ] Enhanced error handling and rollback
- [ ] Documentation and examples

## Consequences

### Positive
- **Real File Operations**: Agents can work with actual workshop files
- **Incremental Building**: Each agent contributes to growing workshop content
- **Better Validation**: File-based validation and testing capabilities
- **Reduced API Calls**: Local operations reduce Gitea API load
- **ADR-0001 Compliance**: Maintains dual-template strategy with file operations

### Negative
- **Increased Complexity**: Hybrid approach requires maintaining both patterns
- **Storage Requirements**: Workspace storage for cloned repositories
- **Agent Updates Required**: All agents need enhancement for file operations

### Risks
- **Workspace Corruption**: File conflicts between agents
- **Performance Impact**: Git cloning and file operations in pipelines
- **Compatibility**: Ensuring backward compatibility with existing API patterns

## Implementation Plan

### Agent Updates Required
1. **Template Converter Agent**: Add repository cloning to workspace
2. **Content Creator Agent**: Add file-based content generation
3. **Source Manager Agent**: Add workspace-to-Gitea synchronization
4. **Research Validation Agent**: Add file-based validation
5. **Documentation Pipeline Agent**: Add file-based documentation updates
6. **Workshop Chat Agent**: Add workspace content indexing

### Tekton Pipeline Updates Required
1. **Add workspace-initialization task** to all workflows
2. **Update agent task definitions** with workspace access
3. **Implement workspace synchronization** patterns
4. **Add error handling** for workspace operations

### Testing Strategy
1. **Unit Tests**: Individual agent file operations
2. **Integration Tests**: Workspace persistence across tasks
3. **End-to-End Tests**: Complete pipeline with file operations
4. **Performance Tests**: Workspace operation benchmarks

## Links
- [ADR-0001: Dual-Template Strategy](ADR-0001-dual-template-strategy.md)
- [ADR-0006: Tekton-Agent Integration](ADR-0006-tekton-agent-integration.md)
- [Implementation Tracking Issue](#) (TBD)
