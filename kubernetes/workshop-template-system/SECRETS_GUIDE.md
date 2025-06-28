# Workshop Template System - Secrets Configuration Guide

This guide explains which secrets are required vs optional for OpenShift deployment.

## 🔑 Required Secrets

These secrets are **essential** for the system to function:

### 1. **RAG System** ✅ NO API KEY REQUIRED
```bash
# RAG functionality uses local Milvus vector DB (no external API key needed)
# PINECONE_API_KEY=not-required-using-local-milvus
```
**Why no key needed**: Uses local Milvus vector database deployed in OpenShift
**Powered by**: Local Milvus + Etcd + MinIO stack
**Used by**: Workshop Chat Agent for contextual responses

### 2. **GITHUB_TOKEN** ✅ REQUIRED
```bash
GITHUB_TOKEN=ghp_your-github-token-here
```
**Why needed**: Allows agents to analyze source repositories
**Get from**: https://github.com/settings/tokens
**Permissions needed**: `repo` (for private repos) or `public_repo` (for public repos)
**Used by**: Template Converter Agent, Content Creator Agent

### 3. **GITEA_ADMIN_TOKEN** ✅ REQUIRED
```bash
GITEA_ADMIN_TOKEN=your-gitea-admin-token-here
```
**Why needed**: Enables agents to create/manage workshop repositories in Gitea
**Get from**: Your Gitea instance → User Settings → Applications → Generate New Token
**Used by**: Source Manager Agent for Git operations

### 4. **WEBHOOK_SECRET** ✅ REQUIRED
```bash
WEBHOOK_SECRET=your-webhook-secret-$(date +%s)
```
**Why needed**: Secures BuildConfig webhooks from Gitea
**Generate**: Any random string (the example generates one automatically)
**Used by**: OpenShift BuildConfigs for secure webhook triggers

## 🔓 Optional Secrets

These secrets are **NOT required** for basic functionality:

### 1. **OPENAI_API_KEY** ❌ NOT REQUIRED
```bash
# OPENAI_API_KEY=sk-your-openai-api-key-here
```
**Why optional**: System uses local Llama Stack (meta-llama/Llama-3.2-3B-Instruct) running in OpenShift
**Only needed if**: You want to use external OpenAI models instead of local Llama
**Cost**: Local Llama = Free, OpenAI API = Paid usage

## 🏗️ Architecture: Why OpenAI API Key is Optional

```
Workshop Participant Question
           ↓
Workshop Chat Agent
           ↓
Local RAG System (Milvus Vector DB)
           ↓
Local Llama Stack Server (in OpenShift)
           ↓
meta-llama/Llama-3.2-3B-Instruct
           ↓
AI Response (all local, no external API calls)
```

### **Local AI Stack Benefits:**
- ✅ **No external API costs**
- ✅ **Data stays in your cluster**
- ✅ **No internet dependency for AI**
- ✅ **Consistent performance**
- ✅ **Privacy and security**

## 📋 Minimal Required Configuration

For a working OpenShift deployment, you only need:

```bash
# RAG functionality uses local Milvus (no API key needed)
# PINECONE_API_KEY=not-required-using-local-milvus

# Required for repository access
GITHUB_TOKEN=ghp_xyz789abc123...

# Required for Gitea operations
GITEA_ADMIN_TOKEN=gta_def456xyz789...

# Required for webhook security
WEBHOOK_SECRET=workshop-webhook-1234567890

# Optional: Repository-specific secrets
# Add any secrets your specific workshop needs
```

## 🔧 Environment-Specific Configurations

### Development Environment
```bash
# Minimal setup for testing
# PINECONE_API_KEY=not-required-using-local-milvus
GITHUB_TOKEN=ghp_dev-token...
GITEA_ADMIN_TOKEN=gta_dev-token...
WEBHOOK_SECRET=dev-webhook-secret
```

### Production Environment
```bash
# Full production setup
# PINECONE_API_KEY=not-required-using-local-milvus
GITHUB_TOKEN=ghp_prod-token...
GITEA_ADMIN_TOKEN=gta_prod-token...
WEBHOOK_SECRET=prod-webhook-secret-$(date +%s)

# Optional: Enhanced monitoring
MONITORING_ENABLED=true
LOG_LEVEL=info
```

## 🚨 Security Best Practices

### 1. **Secret Rotation**
```bash
# Rotate secrets regularly
WEBHOOK_SECRET=new-secret-$(date +%s)
```

### 2. **Least Privilege**
- GitHub Token: Only grant necessary repository permissions
- Gitea Token: Create dedicated service account
- Pinecone: Use project-specific API keys

### 3. **Environment Separation**
- Use different secrets for dev/staging/prod
- Never share production secrets across environments

## 🔍 Troubleshooting

### Common Issues

1. **Chat Agent not responding**
   - Check: Milvus service is running
   - Verify: Local vector database is accessible

2. **Repository analysis failing**
   - Check: GITHUB_TOKEN has correct permissions
   - Verify: Token can access the source repository

3. **BuildConfig not triggering**
   - Check: WEBHOOK_SECRET matches in Gitea and OpenShift
   - Verify: Gitea can reach OpenShift webhook URL

4. **Gitea operations failing**
   - Check: GITEA_ADMIN_TOKEN is valid
   - Verify: Token has admin permissions in Gitea

### Debug Commands

```bash
# Check secret values (be careful with output!)
oc get secret workshop-system-secrets -o yaml -n workshop-system

# Test GitHub token
curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user

# Test Milvus connection
oc port-forward svc/milvus 19530:19530 -n workshop-system &
curl http://localhost:19530/health

# Check Gitea token
curl -H "Authorization: token $GITEA_ADMIN_TOKEN" https://your-gitea.com/api/v1/user
```

## 📚 Summary

**Required for OpenShift deployment:**
- ✅ GITHUB_TOKEN
- ✅ GITEA_ADMIN_TOKEN
- ✅ WEBHOOK_SECRET

**NOT required (system uses local components):**
- ❌ PINECONE_API_KEY (uses local Milvus vector DB)
- ❌ OPENAI_API_KEY (uses local Llama Stack)

The Workshop Template System is designed to be **self-contained** and run entirely within your OpenShift cluster, minimizing external dependencies and costs!
