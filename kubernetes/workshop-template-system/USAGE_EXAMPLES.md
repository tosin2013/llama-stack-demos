# Workshop Template System - Usage Examples

This document shows practical examples of how to use the Kustomize-based deployment for different repositories and scenarios.

## 🎯 Example 1: Healthcare ML Workshop

### Step 1: Copy the Example
```bash
cd kubernetes/workshop-template-system/
cp -r overlays/healthcare-ml-example overlays/my-healthcare-ml
```

### Step 2: Customize URLs
Edit `overlays/my-healthcare-ml/kustomization.yaml`:
```yaml
configMapGenerator:
  - name: workshop-system-config
    behavior: replace
    literals:
      # Update these URLs for your cluster
      - gitea_url=https://gitea.apps.ocp4.example.com
      - workshop_domain=apps.ocp4.example.com
      
      # Repository stays the same
      - source_repository=https://github.com/tosin2013/healthcare-ml-genetic-predictor.git
      - workshop_name=Healthcare ML Genetic Predictor
      - workshop_subdomain=healthcare-ml
```

### Step 3: Add Your Secrets
Edit `overlays/my-healthcare-ml/secrets.env`:
```bash
PINECONE_API_KEY=pk-abc123def456...
GITHUB_TOKEN=ghp_xyz789abc123...
GITEA_ADMIN_TOKEN=gta_def456xyz789...
WEBHOOK_SECRET=healthcare-ml-webhook-1234567890
```

### Step 4: Deploy
```bash
./deploy.sh my-healthcare-ml
```

### Result
- Workshop URL: `https://healthcare-ml.apps.ocp4.example.com`
- Agents automatically convert the Healthcare ML app into an interactive workshop
- Chat assistance available for participants

---

## 🎯 Example 2: Custom Spring Boot Application

### Step 1: Create Custom Overlay
```bash
cp -r overlays/custom-repo-template overlays/spring-boot-microservices
```

### Step 2: Customize for Spring Boot
Edit `overlays/spring-boot-microservices/kustomization.yaml`:
```yaml
configMapGenerator:
  - name: workshop-system-config
    behavior: replace
    literals:
      # Your cluster URLs
      - gitea_url=https://gitea.apps.mycompany.com
      - workshop_domain=apps.mycompany.com
      
      # Your Spring Boot repository
      - source_repository=https://github.com/mycompany/spring-microservices-demo.git
      - workshop_name=Spring Boot Microservices on OpenShift
      - workshop_type=application-conversion
      - technologies=spring-boot,microservices,openshift,kafka
      - workshop_subdomain=spring-microservices
      
      # Workshop details
      - workshop_description=Learn to deploy Spring Boot microservices with Kafka messaging on OpenShift
```

### Step 3: Add Company-Specific Labels
```yaml
commonLabels:
  repository: spring-microservices-demo
  workshop-type: application-conversion
  technology-focus: spring-boot-microservices
  company: mycompany

commonAnnotations:
  source-repository: "https://github.com/mycompany/spring-microservices-demo.git"
  workshop-focus: "Spring Boot Microservices"
  target-audience: "Java Developers"
```

### Step 4: Deploy
```bash
./deploy.sh spring-boot-microservices
```

---

## 🎯 Example 3: Enhancing Existing Workshop

### Step 1: Copy OpenShift Example
```bash
cp -r overlays/openshift-baremetal-example overlays/my-existing-workshop
```

### Step 2: Point to Your Existing Workshop
Edit `overlays/my-existing-workshop/kustomization.yaml`:
```yaml
literals:
  # Your existing workshop repository
  - source_repository=https://github.com/myorg/kubernetes-security-workshop.git
  - workshop_name=Kubernetes Security Workshop - AI Enhanced
  - workshop_type=existing-workshop-enhancement
  - technologies=kubernetes,security,rbac,policies
  - workshop_subdomain=k8s-security
```

### Step 3: Deploy
```bash
./deploy.sh my-existing-workshop
```

### Result
- Agents enhance your existing workshop with AI chat assistance
- Participants get real-time help with security concepts
- Content stays current through automated validation

---

## 🎯 Example 4: Multi-Environment Deployment

### Development Environment
```bash
# Copy template
cp -r overlays/custom-repo-template overlays/my-workshop-dev

# Edit for dev cluster
# overlays/my-workshop-dev/kustomization.yaml
literals:
  - gitea_url=https://gitea.dev.mycompany.com
  - workshop_domain=dev.mycompany.com
  - workshop_subdomain=my-workshop-dev

# Deploy to dev
./deploy.sh my-workshop-dev
```

### Production Environment
```bash
# Copy template
cp -r overlays/custom-repo-template overlays/my-workshop-prod

# Edit for prod cluster
# overlays/my-workshop-prod/kustomization.yaml
literals:
  - gitea_url=https://gitea.mycompany.com
  - workshop_domain=mycompany.com
  - workshop_subdomain=my-workshop

# Add production-specific patches
patchesStrategicMerge:
  - patches/production-resources.yaml
  - patches/production-security.yaml

# Deploy to prod
./deploy.sh my-workshop-prod
```

---

## 🔧 Advanced Customizations

### Custom Resource Limits
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
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: workshop-chat-agent
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: workshop-chat-agent
        resources:
          requests:
            memory: "2Gi"
            cpu: "1"
```

### Custom Domain and TLS
Create `patches/custom-routes.yaml`:
```yaml
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: my-workshop
spec:
  host: workshop.mycompany.com
  tls:
    termination: edge
    certificate: |
      -----BEGIN CERTIFICATE-----
      [Your custom certificate]
      -----END CERTIFICATE-----
    key: |
      -----BEGIN PRIVATE KEY-----
      [Your private key]
      -----END PRIVATE KEY-----
```

### Environment-Specific Secrets
```bash
# Development secrets
cat > overlays/my-workshop-dev/secrets.env << EOF
PINECONE_API_KEY=pk-dev-key...
GITHUB_TOKEN=ghp_dev-token...
ENVIRONMENT=development
DEBUG_MODE=true
EOF

# Production secrets
cat > overlays/my-workshop-prod/secrets.env << EOF
PINECONE_API_KEY=pk-prod-key...
GITHUB_TOKEN=ghp_prod-token...
ENVIRONMENT=production
DEBUG_MODE=false
MONITORING_ENABLED=true
EOF
```

## 🚀 Quick Commands

```bash
# List available overlays
ls overlays/

# Preview what will be deployed
oc kustomize overlays/my-workshop/

# Deploy workshop
./deploy.sh my-workshop

# Check deployment status
oc get pods -n workshop-system
oc get routes -n workshop-system

# Update workshop
oc apply -k overlays/my-workshop/

# Delete workshop
oc delete -k overlays/my-workshop/
```

## 📚 Next Steps

1. **Create your overlay** by copying the appropriate example
2. **Customize URLs and repository** information
3. **Add your secrets** and any custom patches
4. **Deploy and test** your workshop
5. **Iterate and improve** based on participant feedback

The Kustomize structure makes it easy to maintain multiple workshops and environments while sharing common base configurations!
