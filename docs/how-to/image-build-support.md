# Image Build Support in Deploy Script

The `deploy-complete-system.sh` script now includes comprehensive image building support to ensure all required container images are available before deployment.

## 🔨 Build Process Overview

### 1. **Agent Image Building**
The script now builds the main workshop agent system image that contains all 6 agents:

```bash
# Creates BuildConfig for workshop-agent-system
oc apply -f kubernetes/workshop-template-system/base/workshop-system-buildconfig.yaml

# Builds the image from source
oc start-build workshop-system-build --wait --follow
```

### 2. **Workshop Content Building**
After repository import, the script triggers workshop content builds:

```bash
# Triggers OpenShift Bare Metal workshop build
oc start-build openshift-baremetal-workshop-build

# Triggers Healthcare ML workshop build (when repository is created by agents)
oc start-build healthcare-ml-workshop-build
```

## 📋 Build Configuration Details

### **Workshop Agent System BuildConfig**
- **Source**: `https://github.com/tosin2013/llama-stack-demos.git`
- **Dockerfile**: `kubernetes/workshop-template-system/Dockerfile`
- **Output Image**: `workshop-agent-system:latest`
- **Used By**: All 6 workshop template system agents

### **Workshop Content BuildConfigs**
- **OpenShift Bare Metal**: Builds from imported Gitea repository
- **Healthcare ML**: Builds from agent-generated repository
- **Output Images**: Workshop-specific container images

## 🚀 Deployment Flow

The updated deployment script follows this sequence:

1. **Check/Deploy Gitea** → Skip if already exists
2. **Import Repositories** → Import real workshop into Gitea
3. **Update BuildConfig URLs** → Use actual Gitea hostname
4. **Deploy Kustomize Resources** → All agents, infrastructure, BuildConfigs
5. **Build Agent Images** → Create workshop-agent-system image
6. **Wait for Infrastructure** → Milvus, MinIO, etcd ready
7. **Wait for Agents** → Agents start with proper images
8. **Trigger Workshop Builds** → Start workshop content builds
9. **Configure Workflows** → Provide agent interaction commands

## 🔍 Build Status Monitoring

The script provides build status information:

```bash
# Check build status
oc get builds -n workshop-system

# Monitor specific build
oc logs -f build/workshop-system-build-1
```

## 🎯 Benefits

### **Eliminates ImagePullBackOff Errors**
- Ensures agent images exist before deployment
- Builds from source instead of relying on external registries

### **Automated Build Process**
- No manual intervention required
- Builds triggered automatically during deployment

### **Proper Dependency Management**
- Infrastructure ready before agents
- Agent images built before agent deployment
- Workshop builds triggered after repository setup

## 🔧 Troubleshooting

### **Build Failures**
```bash
# Check build logs
oc logs build/workshop-system-build-1

# Restart failed build
oc start-build workshop-system-build
```

### **Image Pull Issues**
```bash
# Verify image exists
oc get imagestream workshop-agent-system -o yaml

# Check image tags
oc describe imagestream workshop-agent-system
```

### **Agent Startup Issues**
```bash
# Check agent pod status
oc get pods -l component=workshop-agent

# Check agent logs
oc logs deployment/template-converter-agent
```

## 📊 Expected Results

After successful deployment with image builds:

- ✅ **All agent pods running** (no ImagePullBackOff)
- ✅ **Infrastructure healthy** (Milvus, MinIO, etcd)
- ✅ **Workshop builds triggered** (content generation ready)
- ✅ **Agent routes accessible** (HTTPS endpoints working)

## 🎯 Next Steps

With images built and agents running:

1. **Test agent connectivity**: `curl -k https://template-converter-agent-workshop-system.apps.cluster-domain/agent-card`
2. **Start workflows**: Use provided curl commands for Workflow 1 and 3
3. **Monitor progress**: Watch agent logs and Gitea repositories
4. **Verify workshop generation**: Check for agent-generated content

---

*This build support ensures a complete, working Workshop Template System deployment from a clean OpenShift environment.*
