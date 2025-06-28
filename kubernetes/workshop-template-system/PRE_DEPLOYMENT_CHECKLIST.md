# Pre-Deployment Checklist

Before running `./deploy.sh`, ensure you have completed all the prerequisites and configurations.

## ✅ Prerequisites Checklist

### 1. **OpenShift Access**
- [ ] Logged into OpenShift cluster: `oc whoami`
- [ ] Have cluster-admin or sufficient permissions
- [ ] Can create projects/namespaces
- [ ] Can create routes and services

### 2. **Required Tools**
- [ ] OpenShift CLI (`oc`) installed and working
- [ ] `curl` available for Gitea installation
- [ ] `git` available for repository operations
- [ ] Optional: `kustomize` CLI (will use `oc apply -k` if not available)

### 3. **API Keys and Tokens**
- [ ] **GitHub Token** - Create at https://github.com/settings/tokens
  - Permissions needed: `repo` (for private repos) or `public_repo` (for public repos)
- [ ] **Gitea Admin Token** - Will be created during deployment
- [ ] **Webhook Secret** - Will be generated automatically
- [ ] **RAG System** - Uses local Milvus (no external API key needed)

## 🔧 Configuration Steps

### 1. **Choose Your Overlay**
Pick the overlay that matches your repository:

```bash
# For Healthcare ML application conversion
cp -r overlays/healthcare-ml-example overlays/my-healthcare-ml

# For OpenShift workshop enhancement  
cp -r overlays/openshift-baremetal-example overlays/my-openshift-workshop

# For any custom repository
cp -r overlays/custom-repo-template overlays/my-custom-workshop
```

### 2. **Customize Configuration**
Edit `overlays/your-overlay/kustomization.yaml`:

```yaml
# REQUIRED: Update these URLs for your cluster
- gitea_url=https://gitea.apps.YOUR-CLUSTER-DOMAIN
- workshop_domain=apps.YOUR-CLUSTER-DOMAIN

# REQUIRED: Update repository information
- source_repository=https://github.com/YOUR-ORG/YOUR-REPO.git
- workshop_name=Your Workshop Name
- workshop_subdomain=your-subdomain
```

### 3. **Configure Secrets**
Edit `overlays/your-overlay/secrets.env`:

```bash
# REQUIRED
GITHUB_TOKEN=ghp_your-actual-github-token
GITEA_ADMIN_TOKEN=will-be-created-during-deployment
WEBHOOK_SECRET=will-be-generated-automatically

# NOT REQUIRED (system uses local components)
# PINECONE_API_KEY=not-required-using-local-milvus
# OPENAI_API_KEY=not-required-using-local-llama-stack
```

## 🚀 Deployment Process

### 1. **Run Deployment Script**
```bash
./deploy.sh your-overlay-name
```

### 2. **Gitea Installation (Automatic)**
The script will:
- Detect if Gitea is installed
- Download and install Gitea if needed
- Wait for Gitea to be ready
- Prompt for admin user configuration

### 3. **Gitea Configuration (Interactive)**
You'll be prompted for:
- Admin username (default: workshop-admin)
- Admin email (default: admin@workshop.local)  
- Admin password (required)

### 4. **Manual Token Creation**
After Gitea is configured:
1. Open Gitea in browser (URL provided by script)
2. Login with admin credentials
3. Go to: User Settings → Applications → Generate New Token
4. Create token with permissions: `repo`, `admin:org`
5. Copy token and confirm in script prompt

### 5. **System Deployment**
Script will:
- Update overlay with actual Gitea URL
- Deploy all components to OpenShift
- Wait for pods to be ready
- Show deployment status and URLs

## 🔍 Verification Steps

### 1. **Check Pod Status**
```bash
oc get pods -n workshop-system
```
All pods should be `Running` and `Ready`.

### 2. **Check Routes**
```bash
oc get routes -n workshop-system
```
Should show workshop URLs.

### 3. **Test Workshop Access**
```bash
curl -I https://your-workshop.apps.your-cluster.local
```
Should return `HTTP/1.1 200 OK`.

### 4. **Test Agent Health**
```bash
oc logs -l component=workshop-agent -n workshop-system
```
Should show agents starting successfully.

## 🛠️ Troubleshooting

### Common Issues Before Deployment

1. **Not logged into OpenShift**
   ```bash
   oc login https://api.your-cluster.local:6443
   ```

2. **Insufficient permissions**
   - Contact cluster administrator
   - Need project creation and route creation permissions

3. **Missing API keys**
   - Create GitHub token at https://github.com/settings/tokens
   - RAG system uses local Milvus (no external API key needed)

4. **Invalid overlay configuration**
   - Check kustomization.yaml syntax
   - Ensure all placeholder values are replaced

### Common Issues During Deployment

1. **Gitea installation fails**
   ```bash
   # Check Gitea pods
   oc get pods -n gitea
   oc logs -l app=gitea -n gitea
   ```

2. **Agent pods not starting**
   ```bash
   # Check pod status
   oc describe pod -l component=workshop-agent -n workshop-system
   ```

3. **Secrets validation fails**
   - Ensure all required secrets are set
   - Check for placeholder values in secrets.env

4. **BuildConfig fails**
   - Verify Gitea URL is accessible
   - Check webhook secret configuration

## 📋 Quick Start Commands

```bash
# 1. Copy overlay
cp -r overlays/custom-repo-template overlays/my-workshop

# 2. Edit configuration
vim overlays/my-workshop/kustomization.yaml
vim overlays/my-workshop/secrets.env

# 3. Deploy
./deploy.sh my-workshop

# 4. Follow prompts for Gitea setup

# 5. Verify deployment
oc get pods -n workshop-system
oc get routes -n workshop-system
```

## 🎯 Success Criteria

Deployment is successful when:
- ✅ All pods in `workshop-system` namespace are Running
- ✅ Workshop routes are accessible via HTTPS
- ✅ Gitea is running and accessible
- ✅ BuildConfigs are created and ready
- ✅ Agent health checks pass

## 📚 Additional Resources

- [GITEA_SETUP.md](GITEA_SETUP.md) - Detailed Gitea configuration
- [SECRETS_GUIDE.md](SECRETS_GUIDE.md) - Required vs optional secrets
- [USAGE_EXAMPLES.md](USAGE_EXAMPLES.md) - Practical examples
- [README.md](README.md) - Complete documentation

---

**Ready to deploy?** Run `./deploy.sh your-overlay-name` and follow the interactive prompts!
