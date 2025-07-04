# ADR-0010: Workspace Tool Implementation

## Status
**SUPERSEDED** ⚠️ (Replaced by ADR-0018 Quarkus Middleware Architecture)

## Context
Following ADR-0009 (Agent Workspace Integration), agents now have access to the shared workspace at `/workspace/shared-data`. This ADR originally proposed implementing workspace-aware tools with direct `/tools/` endpoints, but analysis revealed that a middleware approach using the existing Quarkus workshop-monitoring-service would be more effective.

**SUPERSEDED BY**: ADR-0018 (Quarkus Middleware Architecture) provides a superior solution that:
- Eliminates endpoint mismatch issues
- Leverages existing infrastructure (80% reuse)
- Provides better error handling and observability
- Simplifies Tekton pipeline development

## Decision
**ORIGINAL DECISION (SUPERSEDED)**: Use the existing A2A protocol `/invoke` endpoint structure for Tekton pipeline integration instead of implementing separate `/tools/` endpoints.

**NEW DECISION**: See ADR-0018 for the current architectural approach using Quarkus middleware, which provides a more robust and maintainable solution.

### Required Tool Implementations

#### Content Creator Agent Tools
1. **`clone_showroom_template_tool`**
   - Clone RHPDS Showroom template to workspace
   - Customize template for specific workshop
   - Return workspace path for further processing

2. **`create_workshop_content_from_workspace_tool`**
   - Generate workshop content from workspace files
   - Analyze existing repository content
   - Create structured workshop materials

3. **`generate_exercises_from_workspace_tool`**
   - Create hands-on exercises from workspace content
   - Generate practical learning activities
   - Integrate with existing code examples

#### Source Manager Agent Tools
1. **`sync_workspace_to_gitea_tool`**
   - Deploy workspace content to Gitea repository
   - Manage repository structure and permissions
   - Handle version control operations

2. **`validate_workspace_structure_tool`**
   - Verify workspace content structure
   - Check for required files and formats
   - Validate workshop completeness

#### Template Converter Agent Tools
1. **`analyze_repository_to_workspace_tool`**
   - Clone and analyze source repository
   - Extract workshop structure and content
   - Prepare workspace for enhancement

2. **`convert_template_in_workspace_tool`**
   - Convert between workshop template formats
   - Migrate content to RHPDS standards
   - Preserve existing workshop structure

## Implementation Architecture

```mermaid
graph TB
    subgraph "Agent Container"
        API[Agent API Server]
        WT[Workspace Tools]
        FS[File System Operations]
    end
    
    subgraph "Shared Workspace"
        WS[/workspace/shared-data]
        PD[Pipeline Directories]
        AC[Agent Cache]
        ST[Shared Templates]
    end
    
    subgraph "Tekton Pipeline"
        PT[Pipeline Task]
        TC[Tool Call]
        WM[Workspace Mount]
    end
    
    PT --> TC
    TC --> API
    API --> WT
    WT --> FS
    FS --> WS
    WM --> WS
```

## Tool Implementation Details

### 1. Base Workspace Tool Class
```python
from abc import ABC, abstractmethod
from pathlib import Path
import json
import logging

class WorkspaceToolBase(ABC):
    def __init__(self, workspace_path: str = "/workspace/shared-data"):
        self.workspace_path = Path(workspace_path)
        self.logger = logging.getLogger(self.__class__.__name__)
    
    def get_pipeline_workspace(self, pipeline_id: str) -> Path:
        """Get pipeline-specific workspace directory"""
        return self.workspace_path / "pipelines" / pipeline_id
    
    def get_agent_workspace(self, agent_name: str) -> Path:
        """Get agent-specific workspace directory"""
        return self.workspace_path / "agents" / agent_name
    
    def acquire_lock(self, resource: str, pipeline_id: str) -> bool:
        """Acquire file lock for workspace coordination"""
        # Implementation using coordination scripts
        pass
    
    def release_lock(self, resource: str, pipeline_id: str) -> None:
        """Release file lock"""
        pass
    
    @abstractmethod
    def execute(self, **kwargs) -> dict:
        """Execute the workspace tool operation"""
        pass
```

### 2. Clone Showroom Template Tool
```python
class CloneShowroomTemplateTool(WorkspaceToolBase):
    def execute(self, template_name: str, workshop_name: str, target_directory: str) -> dict:
        """Clone and customize Showroom template"""
        try:
            # Determine template source
            if template_name == "showroom_template_default":
                template_url = "https://github.com/rhpds/showroom_template_default.git"
            else:
                template_url = f"https://github.com/rhpds/{template_name}.git"
            
            target_path = Path(target_directory)
            target_path.mkdir(parents=True, exist_ok=True)
            
            # Clone template
            import subprocess
            result = subprocess.run([
                "git", "clone", template_url, str(target_path / "template")
            ], capture_output=True, text=True)
            
            if result.returncode != 0:
                raise Exception(f"Git clone failed: {result.stderr}")
            
            # Customize template
            self._customize_template(target_path / "template", workshop_name)
            
            return {
                "status": "success",
                "template_path": str(target_path / "template"),
                "workshop_name": workshop_name,
                "files_created": self._count_files(target_path / "template")
            }
            
        except Exception as e:
            self.logger.error(f"Template cloning failed: {e}")
            return {
                "status": "error",
                "error": str(e)
            }
    
    def _customize_template(self, template_path: Path, workshop_name: str):
        """Customize template for specific workshop"""
        # Update antora.yml
        antora_file = template_path / "antora.yml"
        if antora_file.exists():
            content = antora_file.read_text()
            content = content.replace("WORKSHOP_NAME", workshop_name)
            antora_file.write_text(content)
        
        # Update showroom.yml
        showroom_file = template_path / "showroom.yml"
        if showroom_file.exists():
            content = showroom_file.read_text()
            content = content.replace("WORKSHOP_NAME", workshop_name)
            showroom_file.write_text(content)
```

### 3. Create Workshop Content Tool
```python
class CreateWorkshopContentFromWorkspaceTool(WorkspaceToolBase):
    def execute(self, workspace_path: str, workshop_name: str, operation_mode: str = "hybrid") -> dict:
        """Create workshop content from workspace files"""
        try:
            workspace_dir = Path(workspace_path)
            
            # Find pipeline workspace
            pipeline_dirs = list((workspace_dir / "pipelines").glob("*"))
            if not pipeline_dirs:
                raise Exception("No pipeline workspace found")
            
            pipeline_workspace = pipeline_dirs[0]  # Use most recent
            content_dir = pipeline_workspace / "workspace-content"
            
            if not content_dir.exists():
                raise Exception(f"Content directory not found: {content_dir}")
            
            # Analyze existing content
            analysis = self._analyze_workspace_content(content_dir)
            
            # Generate workshop structure
            workshop_content = self._generate_workshop_content(
                analysis, workshop_name, operation_mode
            )
            
            # Write to final output
            output_dir = pipeline_workspace / "final-output"
            output_dir.mkdir(parents=True, exist_ok=True)
            
            self._write_workshop_files(workshop_content, output_dir)
            
            return {
                "status": "success",
                "workshop_name": workshop_name,
                "output_path": str(output_dir),
                "content_analysis": analysis,
                "files_generated": len(workshop_content)
            }
            
        except Exception as e:
            self.logger.error(f"Content creation failed: {e}")
            return {
                "status": "error",
                "error": str(e)
            }
```

### 4. A2A Protocol Integration (IMPLEMENTED)
```python
# A2A Server provides /invoke endpoint automatically
# From common/server/server.py

class A2ARequestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        parsed_path = urlparse(self.path)

        if parsed_path.path == "/invoke":
            self._handle_tool_invoke()
        # ... other endpoints

# Tekton Pipeline Integration
POST /invoke
Content-Type: application/json

{
  "tool_name": "clone_showroom_template_tool",
  "parameters": {
    "template_name": "showroom_template_default",
    "workshop_name": "example-workshop",
    "technology_focus": "https://github.com/example/repo.git",
    "customization_level": "comprehensive"
  }
}
```

**Agent Tools Already Available:**
- ✅ `clone_showroom_template_tool` - Content Creator Agent
- ✅ `create_workshop_content_from_workspace_tool` - Content Creator Agent
- ✅ `create_original_content_tool` - Content Creator Agent
- ✅ `manage_workshop_repository_tool` - Source Manager Agent
- ✅ `analyze_repository_tool` - Template Converter Agent

## File System Operations

### Workspace Coordination
```python
class WorkspaceCoordinator:
    def __init__(self, workspace_path: str):
        self.workspace_path = Path(workspace_path)
        self.scripts_path = Path("/opt/workspace-scripts")
    
    def acquire_lock(self, pipeline_id: str, resource: str, timeout: int = 30) -> bool:
        """Acquire workspace lock using coordination scripts"""
        import subprocess
        result = subprocess.run([
            str(self.scripts_path / "workspace-lock.sh"),
            pipeline_id, resource, "acquire", str(timeout)
        ], capture_output=True)
        return result.returncode == 0
    
    def update_status(self, pipeline_id: str, agent: str, status: str):
        """Update agent status using coordination scripts"""
        import subprocess
        subprocess.run([
            str(self.scripts_path / "workspace-status.sh"),
            pipeline_id, agent, status
        ])
```

## Error Handling and Validation

### Input Validation
```python
from pydantic import validator

class WorkspaceToolRequest(BaseModel):
    @validator('workspace_path')
    def validate_workspace_path(cls, v):
        if not v.startswith('/workspace/shared-data'):
            raise ValueError('Invalid workspace path')
        return v
    
    @validator('workshop_name')
    def validate_workshop_name(cls, v):
        if not v or len(v) < 3:
            raise ValueError('Workshop name must be at least 3 characters')
        return v
```

### Error Recovery
- **File operation failures**: Retry with exponential backoff
- **Lock acquisition timeouts**: Queue operations and retry
- **Workspace corruption**: Automatic cleanup and recovery
- **Network failures**: Graceful degradation with local operations

## Testing Strategy

### Unit Tests
```python
import pytest
from unittest.mock import patch, MagicMock

class TestCloneShowroomTemplateTool:
    def test_successful_clone(self):
        tool = CloneShowroomTemplateTool()
        with patch('subprocess.run') as mock_run:
            mock_run.return_value.returncode = 0
            result = tool.execute(
                template_name="showroom_template_default",
                workshop_name="test-workshop",
                target_directory="/tmp/test"
            )
            assert result["status"] == "success"
```

### Integration Tests
- **Workspace file operations**: Test actual file system operations
- **Coordination scripts**: Test locking and status mechanisms
- **Pipeline integration**: Test end-to-end pipeline execution
- **Error scenarios**: Test failure handling and recovery

## Consequences

### Positive
- ✅ Enables complete pipeline functionality
- ✅ True workspace-based agent operations
- ✅ Seamless Tekton-agent integration
- ✅ Coordinated file system access
- ✅ Scalable tool architecture

### Negative
- ⚠️ Increased agent complexity
- ⚠️ File system operation risks
- ⚠️ Coordination overhead
- ⚠️ Testing complexity

## Dependencies
- **Requires**: ADR-0009 (Agent Workspace Integration)
- **Requires**: ADR-0008 (Shared PVC Implementation)
- **Enables**: Complete pipeline execution
- **Enables**: ADR-0017 (Content Creator Agent)

## Implementation Priority
**CRITICAL** - This ADR is blocking pipeline completion and must be implemented immediately.

## Implementation Plan
1. **Phase 1**: Implement base workspace tool classes
2. **Phase 2**: Add Content Creator Agent workspace tools
3. **Phase 3**: Update agent HTTP endpoints
4. **Phase 4**: Test with existing pipelines
5. **Phase 5**: Implement remaining agent tools

## Related Files
- `agents/content-creator/src/workspace_tools/`
- `agents/content-creator/src/api/endpoints.py`
- `agents/source-manager/src/workspace_tools/`
- `agents/template-converter/src/workspace_tools/`

## Validation Criteria
- [ ] `clone_showroom_template_tool` endpoint returns 200
- [ ] `create_workshop_content_from_workspace_tool` endpoint functional
- [ ] Pipeline tasks successfully call agent tools
- [ ] Workspace file operations coordinated properly
- [ ] Error handling and recovery working

## Date
2025-06-30

## Supersedes
None

## Superseded By
**ADR-0018: Quarkus Middleware Architecture**

## Migration to ADR-0018
This ADR has been superseded by **ADR-0018: Quarkus Middleware Architecture**, which provides:
- ✅ **Better Architecture**: Leverages existing infrastructure (80% reuse)
- ✅ **Improved Reliability**: Centralized error handling and circuit breakers
- ✅ **Enhanced Observability**: Comprehensive logging and monitoring
- ✅ **Simplified Development**: Reduces Tekton pipeline complexity
- ✅ **Testing Support**: Mock endpoints for local development

**Action Required**: Implement ADR-0018 instead of this ADR for Tekton pipeline integration.

## Related ADRs
- **ADR-0009**: Agent Workspace Integration (prerequisite)
- **ADR-0018**: Quarkus Middleware Architecture (supersedes this ADR)
- **ADR-0017**: Content Creator Agent (implementation target)
- **ADR-0016**: Template Converter Agent (implementation target)
- **ADR-0030**: Source Manager Agent (implementation target)
