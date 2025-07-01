# ADR-0008: Shared PVC Implementation for Enhanced Workspace

## Status
**COMPLETED** ✅

## Context
Following ADR-0007 (Enhanced Workspace Strategy), we need to implement the shared storage infrastructure that enables true collaboration between agents and Tekton pipelines. The implementation must provide ReadWriteMany (RWX) access for multiple pods while maintaining data consistency and coordination.

## Decision
Implement a shared PVC-based storage solution using OpenShift Data Foundation (ODF) with the following specifications:

### Storage Configuration
- **Storage Class**: `ocs-storagecluster-cephfs`
- **Access Mode**: ReadWriteMany (RWX)
- **Capacity**: 10Gi (expandable)
- **PVC Name**: `shared-workspace-storage`

### Directory Structure
```
/workspace/shared-data/
├── pipelines/{pipeline-run-id}/     # Isolated per pipeline
│   ├── workspace-content/           # Git cloned content
│   ├── agent-artifacts/             # Agent outputs
│   ├── metadata/                    # Pipeline metadata
│   └── final-output/               # Ready for deployment
├── agents/{agent-name}/             # Agent working directories
├── shared/templates/                # Cached templates
└── completed/{date}/                # Archived pipelines
```

### Coordination Mechanisms
- **File Locking**: Bash-based locking scripts
- **Status Tracking**: JSON metadata files
- **Cleanup Management**: CronJob for automated maintenance

## Implementation Details

### 1. PVC Creation
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: shared-workspace-storage
  namespace: workshop-system
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 10Gi
  storageClassName: ocs-storagecluster-cephfs
```

### 2. Coordination Scripts
- `workspace-init.sh`: Initialize pipeline-specific structure
- `workspace-lock.sh`: File locking for concurrent access
- `workspace-status.sh`: Agent status management
- `workspace-cleanup.sh`: Cleanup and archival
- `workspace-monitor.sh`: Health monitoring

### 3. Automated Cleanup
CronJob running every 6 hours to:
- Archive completed pipelines (24h retention)
- Clean stale active pipelines (6h timeout)
- Manage template cache (7d retention)
- Clean shared cache (3d retention)

## Consequences

### Positive
- ✅ True workspace sharing between agents and pipelines
- ✅ Persistent storage survives pod restarts
- ✅ Cost-effective single storage resource
- ✅ Automated cleanup prevents storage bloat
- ✅ Structured coordination prevents conflicts

### Negative
- ⚠️ Requires OpenShift Data Foundation (ODF)
- ⚠️ Single point of failure for workspace operations
- ⚠️ Coordination complexity for concurrent access

## Dependencies
- **Requires**: ADR-0007 (Enhanced Workspace Strategy)
- **Enables**: ADR-0009 (Agent Workspace Integration)
- **Enables**: ADR-0011 (Tekton Pipeline Coordination)

## Implementation Status
- ✅ PVC created and bound
- ✅ Coordination scripts deployed
- ✅ Directory structure initialized
- ✅ Cleanup CronJob configured
- ✅ Validated with test pipelines

## Related Files
- `kubernetes/workshop-template-system/base/shared-workspace-pvc.yaml`
- `kubernetes/workshop-template-system/base/workspace-coordination-configmap.yaml`
- `deploy-enhanced-workspace.sh`

## Validation
```bash
# Verify PVC status
oc get pvc shared-workspace-storage -n workshop-system

# Check workspace structure
oc exec <agent-pod> -n workshop-system -- ls -la /workspace/shared-data

# Test coordination scripts
oc exec <agent-pod> -n workshop-system -- /opt/workspace-scripts/workspace-monitor.sh
```

## Date
2025-06-30

## Supersedes
None

## Superseded By
None
