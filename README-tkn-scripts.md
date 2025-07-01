# Enhanced Workspace Testing Scripts (ADR-0007)

This directory contains powerful tkn-based scripts for testing and monitoring the Enhanced Shared Workspace strategy implementation.

## 🚀 Scripts Overview

### 1. `test-enhanced-workspace-tkn.sh`
**Main testing script** - Creates and monitors enhanced workspace pipeline runs with comprehensive reporting.

### 2. `monitor-pipeline-tkn.sh`
**Monitoring script** - Monitors existing pipeline runs and workspace status with real-time updates.

## 📋 Prerequisites

- ✅ **tkn CLI** installed and configured
- ✅ **oc CLI** with access to OpenShift cluster
- ✅ **Enhanced workspace infrastructure** deployed (shared PVC, agents, pipelines)
- ✅ **workshop-system namespace** with proper permissions

## 🎯 Quick Start

### Run a Basic Test
```bash
# Run with default settings (DDD Hexagonal Workshop)
./test-enhanced-workspace-tkn.sh

# Run with custom workshop name
./test-enhanced-workspace-tkn.sh --workshop-name my-test-workshop

# Run without following logs (faster for CI/CD)
./test-enhanced-workspace-tkn.sh --no-follow
```

### Monitor Pipeline Runs
```bash
# List all recent pipeline runs
./monitor-pipeline-tkn.sh --list

# Monitor specific pipeline run
./monitor-pipeline-tkn.sh --pipeline-run workflow-1-new-workshop-run-abc123

# Watch mode (auto-refresh every 10 seconds)
./monitor-pipeline-tkn.sh --watch

# Show logs for specific pipeline run
./monitor-pipeline-tkn.sh --pipeline-run workflow-1-new-workshop-run-abc123 --logs
```

## 🔧 Advanced Usage

### Test Different Repositories
```bash
# Test with different repository
./test-enhanced-workspace-tkn.sh \
  --repository-url "https://github.com/tosin2013/llama-stack-demos" \
  --workshop-name "llama-stack-test"

# Test with custom template
./test-enhanced-workspace-tkn.sh \
  --base-template "custom_template" \
  --workshop-name "custom-template-test"
```

### Monitoring and Debugging
```bash
# Follow logs in real-time
./monitor-pipeline-tkn.sh --pipeline-run workflow-1-new-workshop-run-abc123 --follow

# Clean up failed runs automatically
./test-enhanced-workspace-tkn.sh --cleanup-on-failure

# Use different namespace
./test-enhanced-workspace-tkn.sh --namespace my-workshop-system
```

## 📊 Script Features

### `test-enhanced-workspace-tkn.sh` Features:
- ✅ **Prerequisite checking** (tkn, namespace, pipeline, PVC)
- ✅ **Automatic pipeline creation** with unique names
- ✅ **Real-time log following** (optional)
- ✅ **Comprehensive result reporting**
- ✅ **Workspace content inspection**
- ✅ **Failure analysis** with task-specific logs
- ✅ **Cleanup options** for failed runs
- ✅ **Colorized output** for better readability

### `monitor-pipeline-tkn.sh` Features:
- ✅ **List all pipeline runs** with status
- ✅ **Detailed pipeline run analysis**
- ✅ **Task status summaries**
- ✅ **Workspace content inspection**
- ✅ **Real-time log following**
- ✅ **Watch mode** for continuous monitoring
- ✅ **Agent pod status checking**

## 🎨 Output Examples

### Successful Pipeline Run
```
================================
Enhanced Workspace Pipeline Test (ADR-0007)
================================
✅ tkn CLI available
✅ Namespace workshop-system exists
✅ Pipeline workflow-1-new-workshop exists
✅ Shared workspace PVC exists

🎉 Pipeline completed successfully!

================================
Workspace Contents
================================
Pipeline-specific content:
enhanced-workspace-test-1751251353/
├── workspace-content/
├── agent-artifacts/
├── metadata/
└── final-output/
```

### Failed Pipeline Analysis
```
❌ Pipeline failed

================================
Failure Analysis
================================
⚠️  Failed task: content-creation
Logs:
[content-creation] Error: Tool endpoint not found
[content-creation] Available tools: clone_showroom_template_tool, create_original_content_tool
```

## 🔍 Troubleshooting

### Common Issues and Solutions

#### 1. **tkn CLI not found**
```bash
# Install tkn CLI
curl -LO https://github.com/tektoncd/cli/releases/latest/download/tkn_Linux_x86_64.tar.gz
tar xvzf tkn_Linux_x86_64.tar.gz -C /usr/local/bin/ tkn
```

#### 2. **Pipeline not found**
```bash
# Check if pipeline exists
tkn pipeline list -n workshop-system

# Deploy pipeline if missing
oc apply -f kubernetes/tekton/pipelines/workflow-1-new-workshop.yaml -n workshop-system
```

#### 3. **Shared workspace PVC not found**
```bash
# Check PVC status
oc get pvc shared-workspace-storage -n workshop-system

# Deploy enhanced workspace if missing
./deploy-enhanced-workspace.sh
```

#### 4. **Agent pods not running**
```bash
# Check agent status
oc get pods -l component=workshop-agent -n workshop-system

# Restart agents if needed
oc rollout restart deployment/content-creator-agent -n workshop-system
oc rollout restart deployment/source-manager-agent -n workshop-system
```

## 📈 Performance Tips

### For CI/CD Integration
```bash
# Fast testing without log following
./test-enhanced-workspace-tkn.sh --no-follow --cleanup-on-failure

# Batch monitoring
./monitor-pipeline-tkn.sh --list | grep -E "(Failed|Succeeded)"
```

### For Development
```bash
# Watch mode for continuous monitoring
./monitor-pipeline-tkn.sh --watch

# Follow logs for debugging
./test-enhanced-workspace-tkn.sh --workshop-name debug-test
```

## 🎯 Integration Examples

### Jenkins Pipeline
```groovy
pipeline {
    stages {
        stage('Test Enhanced Workspace') {
            steps {
                sh './test-enhanced-workspace-tkn.sh --workshop-name jenkins-${BUILD_NUMBER} --no-follow'
            }
        }
    }
}
```

### GitHub Actions
```yaml
- name: Test Enhanced Workspace
  run: |
    ./test-enhanced-workspace-tkn.sh \
      --workshop-name "gh-action-${GITHUB_RUN_NUMBER}" \
      --no-follow \
      --cleanup-on-failure
```

## 🚀 Next Steps

1. **Run your first test**: `./test-enhanced-workspace-tkn.sh`
2. **Monitor the results**: `./monitor-pipeline-tkn.sh --list`
3. **Check workspace contents**: `./monitor-pipeline-tkn.sh --list` (includes workspace inspection)
4. **Set up continuous monitoring**: `./monitor-pipeline-tkn.sh --watch`

## 📞 Support

For issues or questions about the Enhanced Workspace strategy (ADR-0007):
1. Check the troubleshooting section above
2. Review the pipeline logs: `./monitor-pipeline-tkn.sh --pipeline-run <name> --logs`
3. Inspect workspace contents via agent pods
4. Verify all prerequisites are met

**Happy testing with the Enhanced Shared Workspace strategy!** 🎉
