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

# Check if Gitea is already deployed
check_gitea_status() {
    print_status "Checking Gitea deployment status..."

    if oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} &>/dev/null; then
        GITEA_STATUS=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.adminSetupComplete}' 2>/dev/null || echo "false")
        if [ "$GITEA_STATUS" = "true" ]; then
            print_success "Gitea is already deployed and ready"
            return 0
        else
            print_warning "Gitea exists but is not ready yet"
            return 1
        fi
    else
        print_status "Gitea not found, will deploy"
        return 1
    fi
}

# Deploy Gitea
deploy_gitea() {
    print_status "Deploying Gitea Git server..."

    # Check if Gitea is already deployed
    if check_gitea_status; then
        print_status "Skipping Gitea deployment - already exists and ready"
    else
        # Download and run Gitea deployment script
        print_status "Downloading Gitea deployment script..."
        curl -OL https://raw.githubusercontent.com/tosin2013/openshift-demos/master/quick-scripts/deploy-gitea.sh
        chmod +x deploy-gitea.sh

        print_status "Running Gitea deployment script..."
        ./deploy-gitea.sh

        # Wait for Gitea to be ready
        print_status "Waiting for Gitea to be ready..."
        for i in {1..12}; do
            if check_gitea_status; then
                break
            fi
            print_status "Waiting for Gitea setup to complete... (${i}/12)"
            sleep 30
        done

        if ! check_gitea_status; then
            print_error "Gitea deployment failed or timed out"
            exit 1
        fi
    fi

    # Get final Gitea URL
    GITEA_URL=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.giteaHostname}' 2>/dev/null || echo "gitea.apps.cluster.local")

    print_success "Gitea is ready"
    print_status "Gitea URL: https://${GITEA_URL}"

    # Import workshop repositories into Gitea
    import_workshop_repositories
}

# Import workshop repositories into Gitea
import_workshop_repositories() {
    print_status "Importing workshop repositories into Gitea..."

    # Get Gitea URL and credentials from OpenShift
    GITEA_URL=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.giteaHostname}' 2>/dev/null || echo "gitea.apps.cluster.local")
    GITEA_ADMIN_USER=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.spec.giteaAdminUser}' 2>/dev/null || echo "opentlc-mgr")
    GITEA_ADMIN_PASSWORD=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.adminPassword}' 2>/dev/null || echo "")

    if [ -z "$GITEA_ADMIN_PASSWORD" ]; then
        print_error "Could not retrieve Gitea admin password from OpenShift"
        print_status "Please check if Gitea is properly deployed: oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE}"
        exit 1
    fi

    print_status "Gitea URL: https://${GITEA_URL}"
    print_status "Admin user: ${GITEA_ADMIN_USER}"
    print_status "Admin password: [Retrieved from OpenShift secret]"

    # Create organization for workshop repositories
    print_status "Creating workshop-system organization in Gitea..."
    curl -X POST "https://${GITEA_URL}/api/v1/orgs" \
        -H "Content-Type: application/json" \
        -u "${GITEA_ADMIN_USER}:${GITEA_ADMIN_PASSWORD}" \
        -d '{
            "username": "workshop-system",
            "full_name": "Workshop Template System",
            "description": "Repositories for Workshop Template System agents"
        }' -k || print_warning "Organization may already exist"

    # Import OpenShift Bare Metal Workshop (for Workflow 3: Enhancement)
    print_status "Importing OpenShift Bare Metal Workshop repository..."
    curl -X POST "https://${GITEA_URL}/api/v1/repos/migrate" \
        -H "Content-Type: application/json" \
        -u "${GITEA_ADMIN_USER}:${GITEA_ADMIN_PASSWORD}" \
        -d '{
            "clone_addr": "https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git",
            "uid": 2,
            "repo_name": "openshift-baremetal-workshop",
            "description": "OpenShift Bare Metal Deployment Workshop - Enhanced by Workshop Template System",
            "private": false
        }' -k

    # Note: healthcare-ml-genetic-predictor will be processed by agents through Workflow 1
    # Agents will create the workshop repository after converting the application

    print_success "Workshop repositories imported into Gitea"
    print_status "Repository URLs:"
    print_status "  - OpenShift Bare Metal: https://${GITEA_URL}/workshop-system/openshift-baremetal-workshop"
    print_status "  - Healthcare ML: Will be created by agents from https://github.com/tosin2013/healthcare-ml-genetic-predictor.git"
}

# Build agent container images
build_agent_images() {
    print_status "Building Workshop Template System agent images..."

    # Check if workshop-system BuildConfig exists
    if ! oc get buildconfig workshop-system-build -n ${NAMESPACE} &>/dev/null; then
        print_status "Creating workshop-system BuildConfig..."
        oc apply -f kubernetes/workshop-template-system/base/workshop-system-buildconfig.yaml
    fi

    # Start the build for agent images
    print_status "Starting workshop-system agent image build..."
    oc start-build workshop-system-build -n ${NAMESPACE} --wait --follow

    # Verify the image was built successfully
    if oc get imagestream workshop-agent-system -n ${NAMESPACE} -o jsonpath='{.status.tags[0].tag}' &>/dev/null; then
        print_success "Workshop agent system image built successfully"
    else
        print_error "Failed to build workshop agent system image"
        exit 1
    fi
}

# Deploy Workshop Template System using Kustomize
deploy_workshop_system() {
    print_status "Deploying Workshop Template System using Kustomize..."

    # Check if Kustomize configuration exists
    if [ ! -d "kubernetes/workshop-template-system/base" ]; then
        print_error "Kustomize configuration not found. Please ensure kubernetes/workshop-template-system/base exists."
        exit 1
    fi

    # Deploy using Kustomize
    print_status "Applying Workshop Template System configuration..."
    oc apply -k kubernetes/workshop-template-system/base/

    # Build agent images before waiting for deployments
    build_agent_images

    # Wait for infrastructure to be ready first
    print_status "Waiting for infrastructure components..."
    oc rollout status deployment milvus -n ${NAMESPACE} --timeout=300s
    oc rollout status deployment minio -n ${NAMESPACE} --timeout=300s
    oc rollout status deployment etcd -n ${NAMESPACE} --timeout=300s

    # Wait for agents to be ready (they should start after images are built)
    print_status "Waiting for agents to be ready..."
    oc rollout status deployment workshop-chat-agent -n ${NAMESPACE} --timeout=300s
    oc rollout status deployment template-converter-agent -n ${NAMESPACE} --timeout=300s
    oc rollout status deployment content-creator-agent -n ${NAMESPACE} --timeout=300s
    oc rollout status deployment source-manager-agent -n ${NAMESPACE} --timeout=300s
    oc rollout status deployment research-validation-agent -n ${NAMESPACE} --timeout=300s
    oc rollout status deployment documentation-pipeline-agent -n ${NAMESPACE} --timeout=300s

    print_success "Workshop Template System deployed successfully"
}

# Deploy Workshop Monitoring Service
deploy_monitoring_service() {
    print_status "Deploying Workshop Monitoring Service..."

    # Check if monitoring service configuration exists
    if [ ! -d "kubernetes/workshop-monitoring-service/base" ]; then
        print_error "Workshop Monitoring Service configuration not found. Please ensure kubernetes/workshop-monitoring-service/base exists."
        exit 1
    fi

    # Deploy monitoring service using Kustomize
    print_status "Applying Workshop Monitoring Service configuration..."
    oc apply -k kubernetes/workshop-monitoring-service/overlays/development/

    # Wait for monitoring service to be ready
    print_status "Waiting for Workshop Monitoring Service to be ready..."
    oc rollout status deployment workshop-monitoring-service -n ${NAMESPACE} --timeout=300s

    # Get monitoring service route
    MONITORING_ROUTE=$(oc get route workshop-monitoring-service -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "monitoring.apps.cluster.local")

    print_success "Workshop Monitoring Service deployed successfully"
    print_info "Monitoring Dashboard: https://${MONITORING_ROUTE}"
    print_info "API Documentation: https://${MONITORING_ROUTE}/q/swagger-ui"
}

# Update Kustomize BuildConfigs with correct Gitea URL
update_buildconfig_urls() {
    print_status "Updating BuildConfig URLs to use deployed Gitea instance..."

    # Get actual Gitea URL from the deployed instance
    GITEA_URL=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.giteaHostname}' 2>/dev/null || echo "gitea.apps.cluster.local")

    if [ "$GITEA_URL" = "gitea.apps.cluster.local" ]; then
        print_warning "Could not retrieve Gitea hostname, using placeholder"
    fi

    print_status "Updating BuildConfigs to use Gitea URL: https://${GITEA_URL}"

    # Update the buildconfigs.yaml file with the correct Gitea URL
    sed -i "s|https://gitea.apps.cluster.local|https://${GITEA_URL}|g" kubernetes/workshop-template-system/base/buildconfigs.yaml

    # Update the monitoring service BuildConfig if it exists
    if [ -f "kubernetes/workshop-monitoring-service/base/buildconfig.yaml" ]; then
        sed -i "s|https://github.com/tosin2013/llama-stack-demos.git|https://${GITEA_URL}/workshop-system/llama-stack-demos.git|g" kubernetes/workshop-monitoring-service/base/buildconfig.yaml
        print_status "Updated Workshop Monitoring Service BuildConfig URL"
    fi

    print_success "BuildConfig URLs updated for Gitea integration"
    print_status "BuildConfigs will be deployed by Kustomize with correct repository URLs"
}

# Trigger workshop builds if repositories are available
trigger_workshop_builds() {
    print_status "Checking and triggering workshop builds..."

    # Check if workshop BuildConfigs exist and trigger builds
    if oc get buildconfig openshift-baremetal-workshop-build -n ${NAMESPACE} &>/dev/null; then
        print_status "Triggering OpenShift Bare Metal workshop build..."
        oc start-build openshift-baremetal-workshop-build -n ${NAMESPACE} || print_warning "OpenShift Bare Metal workshop build failed to start (repository may not be ready)"
    fi

    if oc get buildconfig healthcare-ml-workshop-build -n ${NAMESPACE} &>/dev/null; then
        print_status "Triggering Healthcare ML workshop build..."
        oc start-build healthcare-ml-workshop-build -n ${NAMESPACE} || print_warning "Healthcare ML workshop build failed to start (repository may not be ready)"
    fi

    # Trigger monitoring service build if it exists
    if oc get buildconfig workshop-monitoring-service-build -n ${NAMESPACE} &>/dev/null; then
        print_status "Triggering Workshop Monitoring Service build..."
        oc start-build workshop-monitoring-service-build -n ${NAMESPACE} || print_warning "Workshop Monitoring Service build failed to start (repository may not be ready)"
    fi

    print_status "Workshop builds triggered (they will complete when repositories are available)"
}

# Configure agent workflows for workshop processing
configure_agent_workflows() {
    print_status "Configuring agent workflows for workshop processing..."

    # Get agent URLs
    TEMPLATE_CONVERTER_URL=$(oc get route template-converter-agent -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "template-converter.local")
    CONTENT_CREATOR_URL=$(oc get route content-creator-agent -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "content-creator.local")

    print_status "Agent URLs:"
    print_status "  - Template Converter: https://${TEMPLATE_CONVERTER_URL}"
    print_status "  - Content Creator: https://${CONTENT_CREATOR_URL}"

    # Create workflow configuration
    cat << 'EOF' > configure-workflows.sh
#!/bin/bash
# Configure Workshop Template System workflows

echo "🎯 Workshop Template System Workflow Configuration"
echo "=================================================="

echo ""
echo "📋 Workflow 1: Repository-Based Workshop Creation"
echo "Source: https://github.com/tosin2013/healthcare-ml-genetic-predictor.git"
echo "Foundation: https://github.com/rhpds/showroom_template_default.git"
echo "Process: Application → Analysis → Showroom Template → Workshop"
echo ""
echo "Example command to start Workflow 1:"
echo "curl -X POST https://${TEMPLATE_CONVERTER_URL}/send-task \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{"
echo "    \"id\": \"healthcare-ml-analysis\","
echo "    \"params\": {"
echo "      \"sessionId\": \"workflow-1-session\","
echo "      \"message\": {"
echo "        \"role\": \"user\","
echo "        \"parts\": [{"
echo "          \"type\": \"text\","
echo "          \"text\": \"Analyze https://github.com/tosin2013/healthcare-ml-genetic-predictor.git for workshop conversion using Showroom template\""
echo "        }]"
echo "      }"
echo "    }"
echo "  }'"

echo ""
echo "🔧 Workflow 3: Workshop Enhancement"
echo "Source: Gitea repository (imported from Red Hat SE RTO)"
echo "Process: Existing Workshop → Analysis → Enhancement → Modernization"
echo ""
echo "Example command to start Workflow 3:"
echo "curl -X POST https://${TEMPLATE_CONVERTER_URL}/send-task \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{"
echo "    \"id\": \"openshift-baremetal-enhancement\","
echo "    \"params\": {"
echo "      \"sessionId\": \"workflow-3-session\","
echo "      \"message\": {"
echo "        \"role\": \"user\","
echo "        \"parts\": [{"
echo "          \"type\": \"text\","
echo "          \"text\": \"Enhance the OpenShift Bare Metal workshop from Gitea repository with modern practices\""
echo "        }]"
echo "      }"
echo "    }"
echo "  }'"

echo ""
echo "🚀 Next Steps:"
echo "1. Test agent connectivity with /agent-card endpoints"
echo "2. Start Workflow 1 for healthcare-ml-genetic-predictor"
echo "3. Start Workflow 3 for openshift-baremetal-workshop"
echo "4. Monitor agent progress and generated content"
echo "5. Verify BuildConfig triggers and workshop deployments"
EOF

    chmod +x configure-workflows.sh
    ./configure-workflows.sh

    print_success "Agent workflows configured"
}



# Deploy the complete system
deploy_complete_system() {
    print_status "Deploying complete Workshop Template System..."

    # Create namespace
    oc new-project ${NAMESPACE} 2>/dev/null || oc project ${NAMESPACE}

    # Deploy Gitea first
    deploy_gitea

    # Import workshop repositories
    import_workshop_repositories

    # Update BuildConfig URLs for Gitea integration
    update_buildconfig_urls

    # Deploy Workshop Template System using Kustomize
    deploy_workshop_system

    # BuildConfigs are managed by Kustomize in kubernetes/workshop-template-system/base/buildconfigs.yaml

    # Trigger workshop builds
    trigger_workshop_builds

    # Deploy Workshop Monitoring Service
    deploy_monitoring_service

    # Configure agent workflows
    configure_agent_workflows

    print_success "Complete system deployment finished"
}

# Show complete deployment information
show_complete_deployment_info() {
    print_success "🎉 Complete Workshop Template System Deployed!"
    echo ""
    echo "📊 Complete System Overview:"
    echo "============================"

    # Get URLs and credentials
    GITEA_URL=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.giteaHostname}' 2>/dev/null || echo "gitea.apps.cluster.local")
    GITEA_ADMIN_USER=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.spec.giteaAdminUser}' 2>/dev/null || echo "opentlc-mgr")
    GITEA_ADMIN_PASSWORD=$(oc get gitea gitea-with-admin -n ${GITEA_NAMESPACE} -o jsonpath='{.status.adminPassword}' 2>/dev/null || echo "[not-found]")
    TEMPLATE_CONVERTER_URL=$(oc get route template-converter-agent -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "template-converter.local")
    CONTENT_CREATOR_URL=$(oc get route content-creator-agent -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "content-creator.local")
    WORKSHOP_CHAT_URL=$(oc get route workshop-chat-agent -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "workshop-chat.local")
    MONITORING_URL=$(oc get route workshop-monitoring-service -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "monitoring.local")

    echo ""
    echo "🌐 System URLs:"
    echo "Gitea Git Server: https://${GITEA_URL}"
    echo "  - Admin: ${GITEA_ADMIN_USER}/${GITEA_ADMIN_PASSWORD}"
    echo "  - OpenShift Bare Metal Workshop: https://${GITEA_URL}/workshop-system/openshift-baremetal-workshop"
    echo ""
    echo "📊 Workshop Monitoring Dashboard: https://${MONITORING_URL}"
    echo "  - API Documentation: https://${MONITORING_URL}/q/swagger-ui"
    echo "  - OpenAPI Spec: https://${MONITORING_URL}/q/openapi"
    echo ""
    echo "🤖 Workshop Template System Agents:"
    echo "Template Converter Agent: https://${TEMPLATE_CONVERTER_URL}"
    echo "Content Creator Agent: https://${CONTENT_CREATOR_URL}"
    echo "Workshop Chat Agent: https://${WORKSHOP_CHAT_URL}"
    echo "Research Validation Agent: https://$(oc get route research-validation-agent -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "research-validation.local")"
    echo "Source Manager Agent: https://$(oc get route source-manager-agent -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "source-manager.local")"
    echo "Documentation Pipeline Agent: https://$(oc get route documentation-pipeline-agent -n ${NAMESPACE} -o jsonpath='{.spec.host}' 2>/dev/null || echo "documentation-pipeline.local")"

    echo ""
    echo "🔧 System Components:"
    oc get pods -n ${NAMESPACE} -l component=workshop-agent

    echo ""
    echo "🏗️ Infrastructure:"
    oc get pods -n ${NAMESPACE} -l 'app in (milvus,minio,etcd)'

    echo ""
    echo "� Build Status:"
    oc get builds -n ${NAMESPACE} 2>/dev/null || echo "No builds found"

    echo ""
    echo "�📋 Workshop Processing Workflows:"
    echo "================================="
    echo ""
    echo "🎯 Workflow 1: Repository-Based Workshop Creation"
    echo "Source: https://github.com/tosin2013/healthcare-ml-genetic-predictor.git"
    echo "Foundation: https://github.com/rhpds/showroom_template_default.git"
    echo "Command: curl -X POST https://${TEMPLATE_CONVERTER_URL}/send-task -H 'Content-Type: application/json' -d '{\"id\":\"healthcare-ml\",\"params\":{\"sessionId\":\"workflow-1\",\"message\":{\"role\":\"user\",\"parts\":[{\"type\":\"text\",\"text\":\"Analyze https://github.com/tosin2013/healthcare-ml-genetic-predictor.git for workshop conversion using Showroom template\"}]}}}'"
    echo ""
    echo "🔧 Workflow 3: Workshop Enhancement"
    echo "Source: https://${GITEA_URL}/workshop-system/openshift-baremetal-workshop"
    echo "Command: curl -X POST https://${TEMPLATE_CONVERTER_URL}/send-task -H 'Content-Type: application/json' -d '{\"id\":\"openshift-baremetal\",\"params\":{\"sessionId\":\"workflow-3\",\"message\":{\"role\":\"user\",\"parts\":[{\"type\":\"text\",\"text\":\"Enhance the OpenShift Bare Metal workshop from Gitea repository with modern practices\"}]}}}'"

    echo ""
    echo "🚀 Next Steps:"
    echo "1. Test agent connectivity: curl -k https://${TEMPLATE_CONVERTER_URL}/agent-card"
    echo "2. Start Workflow 1 for healthcare-ml-genetic-predictor conversion"
    echo "3. Start Workflow 3 for openshift-baremetal-workshop enhancement"
    echo "4. Monitor agent progress and generated content in Gitea"
    echo "5. Verify BuildConfig triggers and workshop deployments"

    echo ""
    print_success "Your complete Workshop Template System with proper workflows is ready!"
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
