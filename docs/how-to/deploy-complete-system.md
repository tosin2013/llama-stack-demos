# Complete System Deployment Guide

This guide explains how to use the updated `deploy-complete-system.sh` script to deploy the Workshop Template System with proper workflows.

## 🎯 What the Script Does

The updated deployment script provides a complete, production-ready Workshop Template System deployment with:

### 1. **Kustomize-Based Deployment**
- Uses `kubernetes/workshop-template-system/base/` for consistent deployments
- Deploys all 6 agents with proper configuration
- Sets up infrastructure (Milvus, MinIO, etcd) with correct volume handling

### 2. **Gitea Integration with Real Repositories**
- Deploys Gitea git server automatically
- Imports actual workshop repository: `https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git`
- Creates workshop-system organization for agent-managed repositories

### 3. **Proper Workflow Configuration**
- **Workflow 1 (Repository-Based)**: healthcare-ml-genetic-predictor → Showroom template → new workshop
- **Workflow 3 (Enhancement)**: openshift-baremetal-workshop → analysis → modernization

### 4. **Kustomize-Managed BuildConfigs**
- BuildConfigs defined in `kubernetes/workshop-template-system/base/buildconfigs.yaml`
- Script updates Gitea URLs dynamically before deployment
- Automatic builds when agents commit to Gitea
- Workshop deployments update automatically

## 🚀 Usage

```bash
# Make script executable
chmod +x deploy-complete-system.sh

# Run complete deployment
./deploy-complete-system.sh
```

## 📋 What Gets Deployed

### Core System
- ✅ **6 Workshop Template System Agents** (all with HTTPS routes)
- ✅ **Infrastructure Services** (Milvus, MinIO, etcd)
- ✅ **Gitea Git Server** with admin access

### Workshop Repositories
- ✅ **OpenShift Bare Metal Workshop** (imported from Red Hat SE RTO)
- 🔄 **Healthcare ML Workshop** (will be created by agents)

### Agent Routes (HTTPS)
- `https://template-converter-agent-workshop-system.apps.cluster-domain`
- `https://content-creator-agent-workshop-system.apps.cluster-domain`
- `https://workshop-chat-agent-workshop-system.apps.cluster-domain`
- `https://research-validation-agent-workshop-system.apps.cluster-domain`
- `https://source-manager-agent-workshop-system.apps.cluster-domain`
- `https://documentation-pipeline-agent-workshop-system.apps.cluster-domain`

## 🎯 Testing Workflows

### Workflow 1: Repository-Based Workshop Creation
```bash
# Convert healthcare-ml-genetic-predictor into workshop
curl -X POST https://template-converter-agent-workshop-system.apps.cluster-domain/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "healthcare-ml-analysis",
    "params": {
      "sessionId": "workflow-1-session",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Analyze https://github.com/tosin2013/healthcare-ml-genetic-predictor.git for workshop conversion using Showroom template"
        }]
      }
    }
  }'
```

### Workflow 3: Workshop Enhancement
```bash
# Enhance existing OpenShift Bare Metal workshop
curl -X POST https://template-converter-agent-workshop-system.apps.cluster-domain/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "openshift-baremetal-enhancement",
    "params": {
      "sessionId": "workflow-3-session",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Enhance the OpenShift Bare Metal workshop from Gitea repository with modern practices"
        }]
      }
    }
  }'
```

## 🔍 Verification

### Check Agent Health
```bash
# Test agent connectivity
curl -k https://template-converter-agent-workshop-system.apps.cluster-domain/agent-card

# Check all agents
oc get pods -n workshop-system -l component=workshop-agent
```

### Monitor Workflows
```bash
# Check agent logs
oc logs -f deployment/template-converter-agent -n workshop-system

# Monitor Gitea repositories
# Visit: https://gitea-with-admin-gitea.apps.cluster-domain/workshop-system
```

### Verify BuildConfigs
```bash
# Check build status
oc get builds -n workshop-system

# Monitor workshop deployments
oc get pods -n workshop-system -l 'app in (healthcare-ml-workshop,openshift-baremetal-workshop)'
```

## 🎯 Expected Results

After successful deployment and workflow execution:

1. **Healthcare ML Workshop**: New Showroom-based workshop created from application
2. **OpenShift Bare Metal Workshop**: Enhanced version with modern practices
3. **Automatic Updates**: Changes committed to Gitea trigger new builds
4. **Live Workshops**: Participants can access updated content immediately

## 🔧 Troubleshooting

### Common Issues
- **Agent connectivity**: Verify HTTPS routes are accessible
- **Gitea access**: Check admin credentials (gitea/openshift)
- **BuildConfig failures**: Review build logs for errors
- **Volume issues**: Ensure PVCs are properly bound

### Useful Commands
```bash
# Check overall system health
oc get pods -n workshop-system

# Verify routes
oc get routes -n workshop-system

# Check agent logs
oc logs -l component=workshop-agent -n workshop-system

# Monitor builds
oc get builds -w -n workshop-system
```

---

*This deployment script provides a complete, production-ready Workshop Template System for testing and development.*
