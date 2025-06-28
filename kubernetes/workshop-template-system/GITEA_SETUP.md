# Gitea Setup Guide for Workshop Template System

This guide explains how Gitea is automatically installed and configured during Workshop Template System deployment.

## 🎯 Automatic Gitea Installation

The deployment script automatically handles Gitea installation and configuration:

### What Happens Automatically

1. **Gitea Detection**: Script checks if Gitea is already installed
2. **Automatic Installation**: Downloads and runs Gitea deployment script if needed
3. **Configuration**: Sets up admin user and workshop-system organization
4. **URL Integration**: Updates overlay configuration with actual Gitea URL

### Installation Process

```bash
# The script automatically:
curl -OL https://raw.githubusercontent.com/tosin2013/openshift-demos/master/quick-scripts/deploy-gitea.sh
chmod +x deploy-gitea.sh
./deploy-gitea.sh
```

## 🔧 Gitea Configuration Steps

### 1. Admin User Creation

The script will prompt you for:
- **Admin Username** (default: `workshop-admin`)
- **Admin Email** (default: `admin@workshop.local`)
- **Admin Password** (required)

### 2. System Organization

Automatically creates:
- **workshop-system** organization for workshop repositories
- **System user** for agent operations

### 3. Token Generation

**Manual step required:**
1. Login to Gitea web interface
2. Go to: User Settings → Applications → Generate New Token
3. Create token with permissions:
   - `repo` (repository access)
   - `admin:org` (organization management)
4. Copy token to `secrets.env` file

## 🚀 Deployment Flow with Gitea

### Complete Deployment Process

```bash
# 1. Run deployment script
./deploy.sh my-workshop

# 2. Script checks for Gitea
# If not found, automatically installs

# 3. Gitea configuration prompts
# Enter admin credentials

# 4. Manual token creation
# Login to Gitea and create admin token

# 5. Update secrets.env
# Add GITEA_ADMIN_TOKEN=your-token

# 6. Continue deployment
# Workshop Template System deploys with Gitea integration
```

### Example Session

```bash
$ ./deploy.sh healthcare-ml-example

🚀 Workshop Template System Kustomize Deployment
================================================
[INFO] Checking prerequisites...
[SUCCESS] Prerequisites check passed
[INFO] Deploying Workshop Template System with overlay: healthcare-ml-example
[INFO] Checking Gitea installation...
[WARNING] Gitea not found or not running

Gitea is required but not found. Install Gitea now? (Y/n): y

[INFO] Installing Gitea...
[INFO] Downloading Gitea deployment script...
[INFO] Running Gitea deployment...
[INFO] Waiting for Gitea to be ready...
[SUCCESS] Gitea installed successfully at: https://gitea.apps.cluster.local

[INFO] Configuring Gitea for Workshop Template System...
[INFO] Gitea URL: https://gitea.apps.cluster.local

Gitea Admin Configuration Required
================================================
Please configure Gitea admin user for Workshop Template System:

Enter Gitea admin username [workshop-admin]: admin
Enter Gitea admin email [admin@workshop.local]: admin@mycompany.com
Enter Gitea admin password: [hidden]

[INFO] Running Gitea configuration...
[SUCCESS] Gitea configuration completed!

🎯 Next Steps for Gitea Setup:
==============================
1. Open Gitea in your browser: https://gitea.apps.cluster.local
2. Login with:
   Username: admin
   Password: [the password you entered]
3. Go to: User Settings > Applications > Generate New Token
4. Create a token with 'repo' and 'admin:org' permissions
5. Copy the token and add it to your secrets.env file:
   GITEA_ADMIN_TOKEN=your-generated-token-here

Have you created the Gitea admin token and updated secrets.env? (y/N): y

[INFO] Updating overlay with Gitea URL: https://gitea.apps.cluster.local
[SUCCESS] Updated overlay with actual Gitea URL
[INFO] Preview of resources to be deployed...
```

## 🔍 Gitea Verification

### Check Gitea Installation

```bash
# Check Gitea pods
oc get pods -n gitea

# Check Gitea route
oc get route gitea -n gitea

# Test Gitea web interface
curl -I https://$(oc get route gitea -n gitea -o jsonpath='{.spec.host}')
```

### Verify Configuration

```bash
# Check if admin user exists
oc exec -n gitea deployment/gitea -- gitea admin user list

# Check organizations
oc exec -n gitea deployment/gitea -- gitea admin org list
```

## 🛠️ Manual Gitea Setup (Alternative)

If you prefer to install Gitea manually:

### 1. Install Gitea Separately

```bash
# Download and run Gitea deployment script
curl -OL https://raw.githubusercontent.com/tosin2013/openshift-demos/master/quick-scripts/deploy-gitea.sh
chmod +x deploy-gitea.sh
./deploy-gitea.sh
```

### 2. Configure Admin User

```bash
# Get Gitea URL
GITEA_URL=$(oc get route gitea -n gitea -o jsonpath='{.spec.host}')

# Create admin user
oc exec -n gitea deployment/gitea -- gitea admin user create \
  --username "workshop-admin" \
  --email "admin@workshop.local" \
  --password "your-secure-password" \
  --admin \
  --must-change-password=false
```

### 3. Create Admin Token

1. Login to Gitea: `https://$GITEA_URL`
2. User Settings → Applications → Generate New Token
3. Permissions: `repo`, `admin:org`
4. Copy token to `secrets.env`

### 4. Run Workshop Deployment

```bash
# Deploy workshop system (Gitea will be detected)
./deploy.sh my-workshop
```

## 🔧 Troubleshooting

### Common Issues

1. **Gitea pods not starting**
   ```bash
   # Check pod status
   oc describe pods -n gitea
   
   # Check logs
   oc logs -l app=gitea -n gitea
   ```

2. **Cannot access Gitea web interface**
   ```bash
   # Check route
   oc get route gitea -n gitea
   
   # Test connectivity
   curl -I https://$(oc get route gitea -n gitea -o jsonpath='{.spec.host}')
   ```

3. **Admin user creation fails**
   ```bash
   # Check if user already exists
   oc exec -n gitea deployment/gitea -- gitea admin user list
   
   # Reset admin password
   oc exec -n gitea deployment/gitea -- gitea admin user change-password \
     --username workshop-admin --password new-password
   ```

4. **Token authentication fails**
   - Verify token has correct permissions
   - Check token is not expired
   - Ensure token is added to `secrets.env` correctly

### Debug Commands

```bash
# Check Gitea deployment status
oc get all -n gitea

# Check Gitea configuration
oc exec -n gitea deployment/gitea -- cat /data/gitea/conf/app.ini

# Test API access with token
curl -H "Authorization: token YOUR_TOKEN" \
  https://$(oc get route gitea -n gitea -o jsonpath='{.spec.host}')/api/v1/user
```

## 📚 Summary

The Workshop Template System deployment script provides:

✅ **Automatic Gitea installation** if not present
✅ **Guided configuration** with prompts for admin setup
✅ **URL integration** with overlay configuration
✅ **Validation** of required components
✅ **Clear next steps** for manual token creation

This ensures a smooth deployment experience while maintaining security through manual token generation.
