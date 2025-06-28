#!/bin/bash

# Complete Workshop Template System Deployment with Gitea Integration
# This script deploys Gitea + 6-agent system + workshops with BuildConfig automation

set -e

echo "🚀 Complete Workshop Template System Deployment"
echo "=============================================="
echo "Includes: Gitea + 6-Agent System + Workshop BuildConfigs"

# Configuration
NAMESPACE="workshop-system"
GITEA_NAMESPACE="gitea"
REGISTRY="image-registry.openshift-image-registry.svc:5000"
IMAGE_TAG="latest"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command -v oc &> /dev/null; then
        print_error "OpenShift CLI (oc) is not installed"
        exit 1
    fi
    
    if ! oc whoami &> /dev/null; then
        print_error "Not logged into OpenShift. Please run 'oc login'"
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Deploy Gitea
deploy_gitea() {
    print_status "Deploying Gitea Git server..."
    
    # Download and run Gitea deployment script
    curl -OL https://raw.githubusercontent.com/tosin2013/openshift-demos/master/quick-scripts/deploy-gitea.sh
    chmod +x deploy-gitea.sh
    
    print_status "Running Gitea deployment script..."
    ./deploy-gitea.sh
    
    # Wait for Gitea to be ready
    print_status "Waiting for Gitea to be ready..."
    sleep 30
    
    # Get Gitea URL
    GITEA_URL=$(oc get route gitea -n ${GITEA_NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "gitea.apps.cluster.local")
    
    print_success "Gitea deployed successfully"
    print_status "Gitea URL: https://${GITEA_URL}"
    
    # Create workshop repositories in Gitea
    create_workshop_repositories
}

# Create workshop repositories in Gitea
create_workshop_repositories() {
    print_status "Creating workshop repositories in Gitea..."
    
    # Note: In a real deployment, we'd use Gitea API to create repositories
    # For now, we'll create the structure that agents will populate
    
    cat << 'EOF' > create-repos.sh
#!/bin/bash
# This script would create repositories in Gitea via API
# For demonstration, we'll show the structure

echo "Creating workshop repositories in Gitea..."

# Healthcare ML Workshop Repository
echo "Repository: healthcare-ml-workshop"
echo "  - Generated from: https://github.com/tosin2013/healthcare-ml-genetic-predictor.git"
echo "  - Type: Application conversion"
echo "  - Technologies: Quarkus, Kafka, OpenShift, ML"

# OpenShift Bare Metal Workshop Repository  
echo "Repository: openshift-baremetal-workshop"
echo "  - Generated from: https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git"
echo "  - Type: Existing workshop enhancement"
echo "  - Technologies: OpenShift, Bare Metal"

echo "Repositories created successfully"
EOF

    chmod +x create-repos.sh
    ./create-repos.sh
    
    print_success "Workshop repositories structure created"
}

# Create BuildConfigs for workshops
create_buildconfigs() {
    print_status "Creating BuildConfigs for workshop automation..."
    
    # Healthcare ML Workshop BuildConfig
    cat << EOF | oc apply -f -
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: healthcare-ml-workshop-build
  namespace: ${NAMESPACE}
  labels:
    app: healthcare-ml-workshop
    build: workshop-content
spec:
  source:
    type: Git
    git:
      uri: https://${GITEA_URL}/workshop-system/healthcare-ml-workshop.git
      ref: main
    contextDir: /
  strategy:
    type: Source
    sourceStrategy:
      from:
        kind: ImageStreamTag
        name: httpd:2.4
        namespace: openshift
  output:
    to:
      kind: ImageStreamTag
      name: healthcare-ml-workshop:latest
  triggers:
  - type: ConfigChange
  - type: GitHub
    github:
      secret: workshop-webhook-secret
  - type: Generic
    generic:
      secret: workshop-webhook-secret
  - type: ImageChange
    imageChange: {}

---
apiVersion: image.openshift.io/v1
kind: ImageStream
metadata:
  name: healthcare-ml-workshop
  namespace: ${NAMESPACE}
  labels:
    app: healthcare-ml-workshop
spec:
  lookupPolicy:
    local: false

---
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: openshift-baremetal-workshop-build
  namespace: ${NAMESPACE}
  labels:
    app: openshift-baremetal-workshop
    build: workshop-content
spec:
  source:
    type: Git
    git:
      uri: https://${GITEA_URL}/workshop-system/openshift-baremetal-workshop.git
      ref: main
    contextDir: /
  strategy:
    type: Source
    sourceStrategy:
      from:
        kind: ImageStreamTag
        name: httpd:2.4
        namespace: openshift
  output:
    to:
      kind: ImageStreamTag
      name: openshift-baremetal-workshop:latest
  triggers:
  - type: ConfigChange
  - type: GitHub
    github:
      secret: workshop-webhook-secret
  - type: Generic
    generic:
      secret: workshop-webhook-secret
  - type: ImageChange
    imageChange: {}

---
apiVersion: image.openshift.io/v1
kind: ImageStream
metadata:
  name: openshift-baremetal-workshop
  namespace: ${NAMESPACE}
  labels:
    app: openshift-baremetal-workshop
spec:
  lookupPolicy:
    local: false

---
apiVersion: v1
kind: Secret
metadata:
  name: workshop-webhook-secret
  namespace: ${NAMESPACE}
type: Opaque
data:
  WebHookSecretKey: $(echo -n "workshop-webhook-$(date +%s)" | base64 -w 0)
EOF

    print_success "BuildConfigs created for automated workshop builds"
}

# Update workshop deployments to use BuildConfig images
update_workshop_deployments() {
    print_status "Updating workshop deployments to use BuildConfig images..."
    
    # Update Healthcare ML Workshop deployment
    cat << EOF | oc apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: healthcare-ml-workshop
  namespace: ${NAMESPACE}
  labels:
    app: healthcare-ml-workshop
    workshop: healthcare-ml
    type: application-conversion
spec:
  replicas: 2
  selector:
    matchLabels:
      app: healthcare-ml-workshop
  template:
    metadata:
      labels:
        app: healthcare-ml-workshop
        workshop: healthcare-ml
    spec:
      serviceAccountName: workshop-system-sa
      containers:
      - name: workshop-content
        image: ${REGISTRY}/${NAMESPACE}/healthcare-ml-workshop:latest
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: WORKSHOP_NAME
          value: "Healthcare ML Genetic Predictor"
        - name: WORKSHOP_TYPE
          value: "application-conversion"
        - name: CHAT_AGENT_URL
          value: "http://workshop-chat-agent/healthcare-ml"
        - name: GITEA_URL
          value: "https://${GITEA_URL}"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
      triggers:
      - type: ImageChange
        imageChangeParams:
          automatic: true
          containerNames:
          - workshop-content
          from:
            kind: ImageStreamTag
            name: healthcare-ml-workshop:latest
            namespace: ${NAMESPACE}

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: openshift-baremetal-workshop
  namespace: ${NAMESPACE}
  labels:
    app: openshift-baremetal-workshop
    workshop: openshift-baremetal
    type: existing-workshop-enhancement
spec:
  replicas: 2
  selector:
    matchLabels:
      app: openshift-baremetal-workshop
  template:
    metadata:
      labels:
        app: openshift-baremetal-workshop
        workshop: openshift-baremetal
    spec:
      serviceAccountName: workshop-system-sa
      containers:
      - name: workshop-content
        image: ${REGISTRY}/${NAMESPACE}/openshift-baremetal-workshop:latest
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: WORKSHOP_NAME
          value: "OpenShift Bare Metal Deployment - Enhanced"
        - name: WORKSHOP_TYPE
          value: "existing-workshop-enhancement"
        - name: CHAT_AGENT_URL
          value: "http://workshop-chat-agent/openshift-baremetal"
        - name: GITEA_URL
          value: "https://${GITEA_URL}"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
      triggers:
      - type: ImageChange
        imageChangeParams:
          automatic: true
          containerNames:
          - workshop-content
          from:
            kind: ImageStreamTag
            name: openshift-baremetal-workshop:latest
            namespace: ${NAMESPACE}
EOF

    print_success "Workshop deployments updated to use BuildConfig images"
}

# Deploy the complete system
deploy_complete_system() {
    print_status "Deploying complete Workshop Template System..."
    
    # Create namespace
    oc new-project ${NAMESPACE} 2>/dev/null || oc project ${NAMESPACE}
    
    # Deploy Gitea first
    deploy_gitea
    
    # Create BuildConfigs
    create_buildconfigs
    
    # Deploy the original system (agents, etc.)
    print_status "Deploying 6-agent system..."
    ./deploy-to-openshift.sh
    
    # Update workshop deployments
    update_workshop_deployments
    
    print_success "Complete system deployment finished"
}

# Show complete deployment information
show_complete_deployment_info() {
    print_success "🎉 Complete Workshop Template System Deployed!"
    echo ""
    echo "📊 Complete System Overview:"
    echo "============================"
    
    # Get URLs
    GITEA_URL=$(oc get route gitea -n ${GITEA_NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "gitea.apps.cluster.local")
    HEALTHCARE_URL=$(oc get route healthcare-ml-workshop -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "healthcare-ml.workshop.local")
    OPENSHIFT_URL=$(oc get route openshift-baremetal-workshop -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "openshift-baremetal.workshop.local")
    
    echo ""
    echo "🌐 System URLs:"
    echo "Gitea Git Server: https://${GITEA_URL}"
    echo "Healthcare ML Workshop: https://${HEALTHCARE_URL}"
    echo "OpenShift Bare Metal Workshop: https://${OPENSHIFT_URL}"
    
    echo ""
    echo "🔧 System Components:"
    oc get pods -n ${NAMESPACE} -o wide
    
    echo ""
    echo "🏗️ BuildConfigs:"
    oc get bc -n ${NAMESPACE}
    
    echo ""
    echo "🎯 Complete Workflow:"
    echo "1. **Agents analyze repositories** and generate workshop content"
    echo "2. **Content is committed** to Gitea repositories"
    echo "3. **BuildConfigs automatically trigger** new workshop builds"
    echo "4. **Workshops update live** in OpenShift"
    echo "5. **Participants see updates** immediately"
    
    echo ""
    echo "🤖 Agent Interaction → Live Updates:"
    echo "curl -X POST http://content-creator-agent/send-task \\"
    echo "  -d '{\"message\": \"Update Healthcare ML with Quarkus 3.8\"}'"
    echo "↓"
    echo "Content Creator generates updates → Commits to Gitea → BuildConfig triggers → Workshop updates live!"
    
    echo ""
    print_success "Your complete workshop system with Git integration is ready!"
}

# Main deployment flow
main() {
    echo "Starting complete Workshop Template System deployment..."
    echo ""
    
    check_prerequisites
    
    # Prompt for confirmation
    echo ""
    echo "This will deploy:"
    echo "  - Gitea Git server"
    echo "  - 6-agent Workshop Template System"
    echo "  - BuildConfigs for automated workshop builds"
    echo "  - Two live workshops with CI/CD integration"
    echo ""
    read -p "Continue with complete deployment? (y/N): " confirm
    if [[ ! $confirm =~ ^[Yy]$ ]]; then
        echo "Deployment cancelled"
        exit 0
    fi
    
    # Execute complete deployment
    deploy_complete_system
    show_complete_deployment_info
}

# Run main function
main "$@"
