#!/bin/bash

# Setup Gitea Repositories for Workshop Template System
# Creates the workshop repositories that BuildConfigs expect

set -e

# Configuration
GITEA_URL="https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
GITEA_USER="opentlc-mgr"
GITEA_ORG="workshop-system"

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

# Get Gitea admin password
get_gitea_credentials() {
    print_status "Getting Gitea admin credentials..."

    # Get password from Gitea custom resource status
    GITEA_PASSWORD=$(oc get gitea gitea-with-admin -n gitea -o jsonpath='{.status.adminPassword}' 2>/dev/null || echo "")

    if [ -z "$GITEA_PASSWORD" ]; then
        print_warning "Could not retrieve Gitea password from Gitea CR"
        print_status "Please enter the Gitea admin password:"
        read -s GITEA_PASSWORD
    else
        print_success "Retrieved Gitea password from custom resource"
    fi

    print_success "Gitea credentials obtained"
}

# Create organization if it doesn't exist
create_organization() {
    print_status "Creating organization: ${GITEA_ORG}"
    
    curl -k -s -X POST \
        -H "Content-Type: application/json" \
        -u "${GITEA_USER}:${GITEA_PASSWORD}" \
        -d "{
            \"username\": \"${GITEA_ORG}\",
            \"full_name\": \"Workshop Template System\",
            \"description\": \"Organization for workshop repositories managed by the Workshop Template System\",
            \"website\": \"https://github.com/meta-llama/llama-stack-demos\",
            \"visibility\": \"public\"
        }" \
        "${GITEA_URL}/api/v1/orgs" > /dev/null
    
    print_success "Organization ${GITEA_ORG} created/verified"
}

# Create a repository
create_repository() {
    local repo_name="$1"
    local description="$2"
    local source_repo="$3"
    
    print_status "Creating repository: ${repo_name}"
    
    # Create the repository
    curl -k -s -X POST \
        -H "Content-Type: application/json" \
        -u "${GITEA_USER}:${GITEA_PASSWORD}" \
        -d "{
            \"name\": \"${repo_name}\",
            \"description\": \"${description}\",
            \"private\": false,
            \"auto_init\": true,
            \"default_branch\": \"main\",
            \"readme\": \"Default\"
        }" \
        "${GITEA_URL}/api/v1/orgs/${GITEA_ORG}/repos" > /dev/null
    
    print_success "Repository ${repo_name} created"
    
    # Add initial content
    add_initial_content "$repo_name" "$source_repo"
}

# Add initial content to repository
add_initial_content() {
    local repo_name="$1"
    local source_repo="$2"
    
    print_status "Adding initial content to ${repo_name}..."
    
    # Create a temporary directory
    local temp_dir=$(mktemp -d)
    cd "$temp_dir"
    
    # Clone the repository
    git clone "${GITEA_URL}/${GITEA_ORG}/${repo_name}.git" .
    
    # Add initial workshop content
    cat > index.html << EOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${repo_name} Workshop</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .header { background: #ee0000; color: white; padding: 20px; border-radius: 5px; }
        .content { margin: 20px 0; }
        .status { background: #f0f0f0; padding: 15px; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="header">
        <h1>${repo_name} Workshop</h1>
        <p>Interactive Workshop powered by Workshop Template System</p>
    </div>
    
    <div class="content">
        <h2>Welcome to the Workshop</h2>
        <p>This workshop is being generated from the source repository:</p>
        <p><strong>${source_repo}</strong></p>
        
        <div class="status">
            <h3>🤖 AI-Powered Workshop System</h3>
            <p>This workshop content is dynamically generated and maintained by a 6-agent AI system:</p>
            <ul>
                <li><strong>Template Converter Agent</strong> - Analyzes source repositories</li>
                <li><strong>Content Creator Agent</strong> - Generates workshop content</li>
                <li><strong>Source Manager Agent</strong> - Manages repository integration</li>
                <li><strong>Research Validation Agent</strong> - Validates technical accuracy</li>
                <li><strong>Documentation Pipeline Agent</strong> - Maintains content freshness</li>
                <li><strong>Workshop Chat Agent</strong> - Provides real-time assistance</li>
            </ul>
        </div>
        
        <h3>Workshop Status</h3>
        <p>✅ Repository created and initialized</p>
        <p>🔄 Content generation in progress...</p>
        <p>🤖 Agents will update this content automatically</p>
        
        <h3>Next Steps</h3>
        <p>The Workshop Template System agents will:</p>
        <ol>
            <li>Analyze the source repository structure</li>
            <li>Generate comprehensive workshop content</li>
            <li>Create interactive exercises and labs</li>
            <li>Set up AI-powered chat assistance</li>
            <li>Deploy the complete workshop experience</li>
        </ol>
    </div>
    
    <script>
        console.log('Workshop Template System - ${repo_name}');
        console.log('Source: ${source_repo}');
        console.log('Status: Initializing...');
    </script>
</body>
</html>
EOF

    # Add README
    cat > README.md << EOF
# ${repo_name} Workshop

This repository contains the workshop content for **${repo_name}**, automatically generated and maintained by the Workshop Template System.

## Source Repository
- **Original Repository**: ${source_repo}
- **Workshop Type**: Application Conversion Workshop
- **Generated**: $(date)

## Workshop Template System

This workshop is powered by a 6-agent AI system that:

- 🔍 **Analyzes** the source repository structure and content
- 📝 **Generates** comprehensive workshop materials
- 🔄 **Maintains** content freshness with repository changes
- 🤖 **Provides** real-time AI assistance during workshops
- 🚀 **Deploys** complete workshop experiences

## Agents

1. **Template Converter Agent** - Repository analysis and workshop detection
2. **Content Creator Agent** - Workshop content generation
3. **Source Manager Agent** - Repository and deployment management
4. **Research Validation Agent** - Technical accuracy validation
5. **Documentation Pipeline Agent** - Content maintenance and updates
6. **Workshop Chat Agent** - Real-time participant assistance

## Status

- ✅ Repository initialized
- 🔄 Content generation in progress
- 🤖 Agents processing source repository
- 📋 Workshop materials being created

## Deployment

This workshop will be automatically deployed to OpenShift and accessible via:
- **Workshop URL**: Will be provided once deployment completes
- **Chat Integration**: AI-powered assistance available during workshop
- **Live Updates**: Content automatically updates with source changes

---

*Generated by Workshop Template System - $(date)*
EOF

    # Commit and push
    git add .
    git commit -m "Initial workshop content generated by Workshop Template System

Source repository: ${source_repo}
Generated: $(date)
Status: Ready for agent processing"
    
    git push origin main
    
    # Clean up
    cd /
    rm -rf "$temp_dir"
    
    print_success "Initial content added to ${repo_name}"
}

# Main execution
main() {
    echo "🏗️ Gitea Repository Setup for Workshop Template System"
    echo "===================================================="
    
    get_gitea_credentials
    create_organization
    
    # Create workshop repositories
    create_repository "healthcare-ml-workshop" \
        "Healthcare ML Genetic Predictor Workshop - AI-generated content from source repository" \
        "https://github.com/tosin2013/healthcare-ml-genetic-predictor.git"
    
    create_repository "openshift-baremetal-workshop" \
        "OpenShift Bare Metal Deployment Workshop - Enhanced with AI-generated content" \
        "https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git"
    
    echo ""
    print_success "🎉 Gitea repositories created successfully!"
    print_status "Repository URLs:"
    print_status "  Healthcare ML: ${GITEA_URL}/${GITEA_ORG}/healthcare-ml-workshop"
    print_status "  OpenShift Bare Metal: ${GITEA_URL}/${GITEA_ORG}/openshift-baremetal-workshop"
    
    echo ""
    print_status "Next steps:"
    print_status "  1. Trigger BuildConfigs: oc start-build healthcare-ml-workshop-build -n workshop-system"
    print_status "  2. Check build status: oc get builds -n workshop-system"
    print_status "  3. Monitor workshop deployment: oc get pods -n workshop-system"
}

# Run main function
main "$@"
