# Workspace Storage Requirements (ADR-0007)

**Date**: 2025-06-30  
**Related**: ADR-0007 (Shared Workspace Strategy)

## 📊 Agent Storage Analysis

### ✅ Agents Requiring Workspace Access

#### 1. Content Creator Agent
- **Storage Need**: ✅ **REQUIRED**
- **Reason**: Reads workspace files to generate workshop content
- **Operations**: 
  - Read cloned repository files from workspace
  - Analyze file structure and content
  - Write enhanced workshop content back to workspace
- **Rebuild Required**: ✅ **YES** (new file-based tools added)

#### 2. Source Manager Agent  
- **Storage Need**: ✅ **REQUIRED**
- **Reason**: Synchronizes workspace content to Gitea repositories
- **Operations**:
  - Read complete workspace file structure
  - Upload all files to Gitea with proper organization
  - Maintain file permissions and directory structure
- **Rebuild Required**: ✅ **YES** (workspace sync tools needed)

### ❌ Agents NOT Requiring Workspace Access

#### 3. Template Converter Agent
- **Storage Need**: ❌ **NOT REQUIRED**
- **Reason**: Only analyzes remote repositories via GitHub API
- **Operations**: API-based repository analysis, returns JSON results
- **Rebuild Required**: ❌ **NO**

#### 4. Research Validation Agent
- **Storage Need**: ❌ **NOT REQUIRED** 
- **Reason**: Validates content via web search and external APIs
- **Operations**: Text-based validation, no file operations
- **Rebuild Required**: ❌ **NO**

#### 5. Documentation Pipeline Agent
- **Storage Need**: ❌ **NOT REQUIRED**
- **Reason**: Monitors repositories via GitHub API, creates text proposals
- **Operations**: API-based monitoring, text-based updates
- **Rebuild Required**: ❌ **NO**

#### 6. Workshop Chat Agent
- **Storage Need**: ❌ **NOT REQUIRED**
- **Reason**: Works with RAG content via vector database
- **Operations**: Text-based queries, vector database operations
- **Rebuild Required**: ❌ **NO**

## 🔧 Storage Configuration

### Required Storage Class
```yaml
storageClassName: ocs-storagecluster-cephfs
```

### Storage Requirements
- **Access Mode**: ReadWriteMany (RWX)
- **Capacity**: 2Gi per pipeline run
- **Provider**: OpenShift Data Foundation (ODF)
- **Filesystem**: CephFS (supports RWX)

### Why RWX is Required
- **Multiple Task Access**: Different pipeline tasks need to access the same workspace
- **Concurrent Operations**: Agents may run in parallel and need shared access
- **File Sharing**: Content flows between tasks via shared files

## 📋 Implementation Checklist

### Prerequisites
- [ ] OpenShift Data Foundation (ODF) installed
- [ ] `ocs-storagecluster-cephfs` storage class available
- [ ] RWX access mode supported

### Agent Updates Required
- [ ] Content Creator Agent: Add file-based tools
- [ ] Source Manager Agent: Add workspace sync capabilities
- [ ] Rebuild only these 2 agents (others unchanged)

### Pipeline Updates Required
- [ ] Add workspace-initialization task
- [ ] Update Content Creator task with workspace access
- [ ] Update Source Manager task with workspace access
- [ ] Configure RWX storage for shared-data workspace

### Testing Steps
1. **Verify Storage**: `oc get storageclass ocs-storagecluster-cephfs`
2. **Rebuild Agents**: `./rebuild-agents-for-workspace.sh`
3. **Test Pipeline**: `./test-workspace-implementation.sh --full-test`

## 🎯 Benefits of This Approach

### Efficiency
- **Minimal Rebuilds**: Only 2 out of 6 agents need rebuilding
- **Targeted Storage**: Only workspace-enabled tasks use RWX storage
- **Preserved APIs**: 4 agents continue using existing API patterns

### Scalability  
- **Isolated Workspaces**: Each pipeline run gets its own workspace
- **Cleanup**: Workspaces automatically cleaned up after pipeline completion
- **Resource Optimization**: Storage only allocated when needed

### Compatibility
- **Backward Compatible**: Non-workspace agents unchanged
- **Hybrid Operations**: Agents support both file and API modes
- **Gradual Migration**: Can enable workspace features incrementally

## 🚀 Ready for Implementation

With `ocs-storagecluster-cephfs` available, the workspace strategy can be fully implemented:

1. **Phase 1**: Rebuild Content Creator and Source Manager agents
2. **Phase 2**: Test workspace pipeline with auto-approve
3. **Phase 3**: Validate complete end-to-end workflow
4. **Phase 4**: Enable for production use

This targeted approach minimizes complexity while delivering the full benefits of the shared workspace strategy outlined in ADR-0007.
