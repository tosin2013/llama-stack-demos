# Workshop Template System - Kubernetes Deployment

This directory contains Kustomize-based deployments for the Workshop Template System, organized by repository for easy customization and deployment.

## 🎯 Quick Start

### 1. Choose Your Repository Type

Pick the overlay that matches your use case:

- **`overlays/healthcare-ml-example/`** - Example for converting applications to workshops
- **`overlays/openshift-baremetal-example/`** - Example for enhancing existing workshops  
- **`overlays/custom-repo-template/`** - Template for any custom repository

### 2. Copy and Customize

```bash
# Copy the template that matches your needs
cp -r overlays/custom-repo-template overlays/my-workshop

# Edit the configuration
vim overlays/my-workshop/kustomization.yaml
vim overlays/my-workshop/secrets.env
```

### 3. Deploy

```bash
# Deploy your customized workshop system (includes automatic Gitea setup)
./deploy.sh my-workshop
```

**Note**: The deployment script automatically installs and configures Gitea if not present. See [GITEA_SETUP.md](GITEA_SETUP.md) for details.

## 📁 Directory Structure

```
kubernetes/workshop-template-system/
├── base/                           # Base Kubernetes resources
│   ├── kustomization.yaml         # Base configuration
│   ├── namespace.yaml             # Namespace and RBAC
│   ├── serviceaccount.yaml        # Service accounts and permissions
│   ├── llama-stack-*.yaml         # Llama Stack deployment
│   ├── agents-*.yaml              # 6-agent system deployment
│   ├── workshops-*.yaml           # Workshop hosting deployment
│   └── buildconfigs.yaml          # OpenShift BuildConfigs
├── overlays/                       # Environment-specific customizations
│   ├── healthcare-ml-example/     # Healthcare ML repository example
│   ├── openshift-baremetal-example/ # OpenShift workshop example
│   └── custom-repo-template/      # Template for any repository
└── README.md                      # This file
```

## 🔧 Customization Guide

### Required Customizations

When creating a new overlay, you **must** customize these values:

#### 1. URLs and Domains (`kustomization.yaml`)
```yaml
literals:
  - gitea_url=https://gitea.apps.YOUR-CLUSTER-DOMAIN
  - workshop_domain=apps.YOUR-CLUSTER-DOMAIN
```

#### 2. Repository Information
```yaml
literals:
  - source_repository=https://github.com/YOUR-ORG/YOUR-REPO.git
  - workshop_name=Your Workshop Name
  - workshop_subdomain=your-subdomain
```

#### 3. Secrets (`secrets.env`)
```bash
PINECONE_API_KEY=pk-your-actual-key
GITHUB_TOKEN=ghp_your-actual-token
GITEA_ADMIN_TOKEN=your-actual-token
```

### Optional Customizations

#### Resource Limits
Create `patches/custom-resources.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: llama-stack-server
spec:
  template:
    spec:
      containers:
      - name: llama-stack
        resources:
          requests:
            memory: "8Gi"
            cpu: "4"
          limits:
            memory: "16Gi"
            cpu: "8"
```

#### Custom Routes
Create `patches/custom-routes.yaml`:
```yaml
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: your-workshop
spec:
  host: your-workshop.apps.your-domain.com
```

## 🚀 Deployment Examples

### Healthcare ML Workshop
```bash
# Deploy the Healthcare ML example
oc apply -k overlays/healthcare-ml-example/

# Access the workshop
open https://healthcare-ml.apps.your-cluster.local
```

### OpenShift Bare Metal Workshop
```bash
# Deploy the OpenShift workshop example
oc apply -k overlays/openshift-baremetal-example/

# Access the workshop
open https://openshift-baremetal.apps.your-cluster.local
```

### Custom Repository
```bash
# Copy template and customize
cp -r overlays/custom-repo-template overlays/my-custom-workshop
# Edit overlays/my-custom-workshop/kustomization.yaml
# Edit overlays/my-custom-workshop/secrets.env

# Deploy your custom workshop
oc apply -k overlays/my-custom-workshop/
```

## 🔍 Verification

After deployment, verify the system is working:

```bash
# Check all pods are running
oc get pods -n workshop-system

# Check routes are accessible
oc get routes -n workshop-system

# Test workshop accessibility
curl -I https://your-workshop.apps.your-cluster.local

# Check agent health
oc logs -l app=workshop-chat-agent -n workshop-system
```

## 🛠️ Troubleshooting

### Common Issues

1. **Pods not starting**: Check resource limits and node capacity
2. **Routes not accessible**: Verify domain configuration in kustomization.yaml
3. **Agents not responding**: Check secrets and Llama Stack connectivity
4. **BuildConfigs failing**: Verify Gitea URL and webhook configuration

### Debug Commands

```bash
# Check pod status
oc describe pod -l app=llama-stack-server -n workshop-system

# Check agent logs
oc logs -l component=workshop-agent -n workshop-system

# Check BuildConfig status
oc get bc -n workshop-system
oc logs -f bc/your-workshop-build -n workshop-system
```

## 📚 Next Steps

1. **Customize for your repository** using the template overlay
2. **Deploy with automatic Gitea setup**: `./deploy.sh my-workshop`
3. **Complete Gitea token setup** when prompted (see [GITEA_SETUP.md](GITEA_SETUP.md))
4. **Test agent interaction** via the workshop chat interface
5. **Monitor workshop updates** as agents modify content
6. **Scale resources** based on usage patterns

## 🤝 Contributing

To add support for new repository types:

1. Create a new overlay directory: `overlays/your-repo-type/`
2. Copy from `custom-repo-template` and customize
3. Add repository-specific patches as needed
4. Update this README with your example

## 📖 Documentation

- [Multi-Workshop Deployment Guide](MULTI_WORKSHOP_DEPLOYMENT.md) - Deploy multiple workshops to same cluster
- [Gitea Setup Guide](GITEA_SETUP.md) - Automatic Gitea installation and configuration
- [Secrets Configuration Guide](SECRETS_GUIDE.md) - Required vs optional secrets
- [Usage Examples](USAGE_EXAMPLES.md) - Practical deployment examples
- [Pre-Deployment Checklist](PRE_DEPLOYMENT_CHECKLIST.md) - Complete setup validation
- [Workshop Template System Overview](../../docs/overview.md)
- [Agent Configuration Guide](../../docs/agents.md)
- [Deployment Troubleshooting](../../docs/troubleshooting.md)
- [Contributing Guidelines](../../docs/contributing.md)
