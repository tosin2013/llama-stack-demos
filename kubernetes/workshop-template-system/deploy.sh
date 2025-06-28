#!/bin/bash

# Workshop Template System Kustomize Deployment Script
# Usage: ./deploy.sh [overlay-name]

set -e

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

# Function to show available overlays
show_overlays() {
    echo "Available overlays:"
    echo "=================="
    for overlay in overlays/*/; do
        if [ -d "$overlay" ]; then
            overlay_name=$(basename "$overlay")
            if [ -f "$overlay/kustomization.yaml" ]; then
                echo "  📁 $overlay_name"
                if [ -f "$overlay/README.md" ]; then
                    echo "     $(head -n 1 "$overlay/README.md" | sed 's/^# //')"
                fi
            fi
        fi
    done
    echo ""
    echo "Examples:"
    echo "  ./deploy.sh healthcare-ml-example"
    echo "  ./deploy.sh openshift-baremetal-example"
    echo "  ./deploy.sh my-custom-workshop"
}

# Function to validate overlay
validate_overlay() {
    local overlay_path="$1"
    
    if [ ! -d "$overlay_path" ]; then
        print_error "Overlay directory not found: $overlay_path"
        return 1
    fi
    
    if [ ! -f "$overlay_path/kustomization.yaml" ]; then
        print_error "kustomization.yaml not found in: $overlay_path"
        return 1
    fi
    
    if [ ! -f "$overlay_path/secrets.env" ]; then
        print_warning "secrets.env not found in: $overlay_path"
        print_warning "Make sure to create and customize secrets.env before deployment"
        return 1
    fi
    
    # Check if required secrets contain placeholder values
    local required_secrets=("GITHUB_TOKEN" "GITEA_ADMIN_TOKEN" "WEBHOOK_SECRET")
    local missing_secrets=()

    for secret in "${required_secrets[@]}"; do
        if ! grep -q "^${secret}=" "$overlay_path/secrets.env" || \
           grep -q "^${secret}=.*YOUR-.*-HERE" "$overlay_path/secrets.env" || \
           grep -q "^${secret}=your-.*-here" "$overlay_path/secrets.env"; then
            missing_secrets+=("$secret")
        fi
    done

    if [ ${#missing_secrets[@]} -ne 0 ]; then
        print_error "Required secrets missing or contain placeholder values:"
        for secret in "${missing_secrets[@]}"; do
            print_error "  - $secret"
        done
        print_error "Edit: $overlay_path/secrets.env"
        print_error "See: kubernetes/workshop-template-system/SECRETS_GUIDE.md"
        return 1
    fi

    # Check for optional keys (warn if present but not needed)
    if grep -q "^OPENAI_API_KEY=" "$overlay_path/secrets.env" && ! grep -q "^#.*OPENAI_API_KEY=" "$overlay_path/secrets.env"; then
        print_warning "OPENAI_API_KEY found but not required (system uses local Llama Stack)"
        print_warning "You can comment it out to save costs"
    fi

    if grep -q "^PINECONE_API_KEY=" "$overlay_path/secrets.env" && ! grep -q "^#.*PINECONE_API_KEY=" "$overlay_path/secrets.env"; then
        print_warning "PINECONE_API_KEY found but not required (system uses local Milvus)"
        print_warning "You can comment it out to save costs"
    fi
    
    return 0
}

# Function to check prerequisites
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

    if ! command -v kustomize &> /dev/null; then
        print_warning "kustomize not found, using 'oc apply -k' instead"
    fi

    if ! command -v curl &> /dev/null; then
        print_error "curl is required for Gitea installation"
        exit 1
    fi

    print_success "Prerequisites check passed"
}

# Function to check if Gitea is installed
check_gitea_installation() {
    print_status "Checking Gitea installation..."

    # Check if Gitea namespace exists
    if oc get namespace gitea &> /dev/null; then
        print_status "Gitea namespace found"

        # Check if Gitea is running (check for any gitea pod)
        if oc get pods -n gitea --field-selector=status.phase=Running | grep -q gitea; then
            local gitea_url=$(oc get route gitea-with-admin -n gitea -o jsonpath='{.spec.host}' 2>/dev/null || \
                             oc get route gitea -n gitea -o jsonpath='{.spec.host}' 2>/dev/null || echo "")
            if [ -n "$gitea_url" ]; then
                print_success "Gitea is running at: https://$gitea_url"
                return 0
            fi
        fi
    fi

    print_warning "Gitea not found or not running"
    return 1
}

# Function to install Gitea
install_gitea() {
    print_status "Installing Gitea..."

    # Download Gitea deployment script
    print_status "Downloading Gitea deployment script..."
    curl -OL https://raw.githubusercontent.com/tosin2013/openshift-demos/master/quick-scripts/deploy-gitea.sh
    chmod +x deploy-gitea.sh

    # Run Gitea deployment
    print_status "Running Gitea deployment..."
    ./deploy-gitea.sh

    # Wait for Gitea to be ready
    print_status "Waiting for Gitea to be ready..."
    sleep 30

    # Wait for Gitea pods to be running
    local timeout=300
    local elapsed=0
    while [ $elapsed -lt $timeout ]; do
        if oc get pods -n gitea -l app=gitea --field-selector=status.phase=Running &> /dev/null; then
            print_success "Gitea pods are running"
            break
        fi
        sleep 10
        elapsed=$((elapsed + 10))
        print_status "Waiting for Gitea pods... ($elapsed/$timeout seconds)"
    done

    if [ $elapsed -ge $timeout ]; then
        print_error "Timeout waiting for Gitea to be ready"
        return 1
    fi

    # Get Gitea URL
    local gitea_url=$(oc get route gitea-with-admin -n gitea -o jsonpath='{.spec.host}' 2>/dev/null || \
                     oc get route gitea -n gitea -o jsonpath='{.spec.host}' 2>/dev/null || echo "")
    if [ -n "$gitea_url" ]; then
        print_success "Gitea installed successfully at: https://$gitea_url"
        return 0
    else
        print_error "Failed to get Gitea URL"
        return 1
    fi
}

# Function to configure Gitea
configure_gitea() {
    print_status "Configuring Gitea for Workshop Template System..."

    local gitea_url=$(oc get route gitea -n gitea -o jsonpath='{.spec.host}' 2>/dev/null || echo "")
    if [ -z "$gitea_url" ]; then
        print_error "Cannot get Gitea URL for configuration"
        return 1
    fi

    print_status "Gitea URL: https://$gitea_url"

    # Wait for Gitea web interface to be ready
    print_status "Waiting for Gitea web interface..."
    local timeout=120
    local elapsed=0
    while [ $elapsed -lt $timeout ]; do
        if curl -s -k "https://$gitea_url" > /dev/null 2>&1; then
            print_success "Gitea web interface is ready"
            break
        fi
        sleep 5
        elapsed=$((elapsed + 5))
    done

    if [ $elapsed -ge $timeout ]; then
        print_warning "Timeout waiting for Gitea web interface, but continuing..."
    fi

    # Prompt for Gitea admin configuration
    echo ""
    print_status "Gitea Admin Configuration Required"
    echo "================================================"
    echo "Please configure Gitea admin user for Workshop Template System:"
    echo ""

    # Get admin credentials
    read -p "Enter Gitea admin username [workshop-admin]: " gitea_admin_user
    gitea_admin_user=${gitea_admin_user:-workshop-admin}

    read -p "Enter Gitea admin email [admin@workshop.local]: " gitea_admin_email
    gitea_admin_email=${gitea_admin_email:-admin@workshop.local}

    read -s -p "Enter Gitea admin password: " gitea_admin_password
    echo ""

    if [ -z "$gitea_admin_password" ]; then
        print_error "Admin password is required"
        return 1
    fi

    # Create Gitea configuration script
    cat > configure-gitea.sh << EOF
#!/bin/bash
# Gitea Configuration Script for Workshop Template System

GITEA_URL="https://$gitea_url"
ADMIN_USER="$gitea_admin_user"
ADMIN_EMAIL="$gitea_admin_email"
ADMIN_PASSWORD="$gitea_admin_password"

echo "Configuring Gitea at \$GITEA_URL..."

# Wait a bit more for Gitea to be fully ready
sleep 10

# Create admin user via Gitea CLI (if available in pod)
echo "Creating admin user..."
oc exec -n gitea deployment/gitea -- gitea admin user create \\
  --username "\$ADMIN_USER" \\
  --email "\$ADMIN_EMAIL" \\
  --password "\$ADMIN_PASSWORD" \\
  --admin \\
  --must-change-password=false 2>/dev/null || echo "Admin user may already exist"

# Create workshop-system organization
echo "Creating workshop-system organization..."
oc exec -n gitea deployment/gitea -- gitea admin user create \\
  --username "workshop-system" \\
  --email "system@workshop.local" \\
  --password "workshop-system-password" \\
  --must-change-password=false 2>/dev/null || echo "System user may already exist"

echo "Gitea configuration completed"
echo "Admin user: \$ADMIN_USER"
echo "Admin email: \$ADMIN_EMAIL"
echo "Gitea URL: \$GITEA_URL"
echo ""
echo "Next steps:"
echo "1. Login to Gitea at \$GITEA_URL"
echo "2. Create an admin token: User Settings > Applications > Generate New Token"
echo "3. Add the token to your secrets.env file as GITEA_ADMIN_TOKEN"
EOF

    chmod +x configure-gitea.sh

    # Run Gitea configuration
    print_status "Running Gitea configuration..."
    ./configure-gitea.sh

    # Show next steps
    echo ""
    print_success "Gitea configuration completed!"
    echo ""
    echo "🎯 Next Steps for Gitea Setup:"
    echo "=============================="
    echo "1. Open Gitea in your browser: https://$gitea_url"
    echo "2. Login with:"
    echo "   Username: $gitea_admin_user"
    echo "   Password: [the password you entered]"
    echo "3. Go to: User Settings > Applications > Generate New Token"
    echo "4. Create a token with 'repo' and 'admin:org' permissions"
    echo "5. Copy the token and add it to your secrets.env file:"
    echo "   GITEA_ADMIN_TOKEN=your-generated-token-here"
    echo ""

    # Ask if user wants to continue or pause for manual setup
    read -p "Have you created the Gitea admin token and updated secrets.env? (y/N): " token_ready
    if [[ ! $token_ready =~ ^[Yy]$ ]]; then
        echo ""
        print_warning "Please complete Gitea token setup before continuing deployment"
        print_warning "Run this script again when ready: ./deploy.sh $1"
        exit 0
    fi

    return 0
}

# Function to update overlay with actual cluster URLs
update_overlay_urls() {
    local overlay_path="$1"

    # Get actual Gitea URL
    local gitea_url=$(oc get route gitea-with-admin -n gitea -o jsonpath='{.spec.host}' 2>/dev/null || \
                     oc get route gitea -n gitea -o jsonpath='{.spec.host}' 2>/dev/null || echo "")

    # Get cluster domain from any existing route
    local cluster_domain=""
    if [ -n "$gitea_url" ]; then
        cluster_domain=$(echo "$gitea_url" | sed 's/^[^.]*\.//')
    fi

    if [ -z "$gitea_url" ] || [ -z "$cluster_domain" ]; then
        print_warning "Could not detect cluster URLs, using placeholders"
        return 0
    fi

    print_status "Detected cluster URLs:"
    print_status "  Gitea URL: https://$gitea_url"
    print_status "  Cluster domain: $cluster_domain"

    # Update kustomization.yaml with actual URLs
    if [ -f "$overlay_path/kustomization.yaml" ]; then
        # Create backup
        cp "$overlay_path/kustomization.yaml" "$overlay_path/kustomization.yaml.backup"

        # Update URLs in kustomization.yaml (temporary, for this deployment only)
        sed -i "s|gitea_url=https://gitea\.apps\..*|gitea_url=https://$gitea_url|g" "$overlay_path/kustomization.yaml"
        sed -i "s|workshop_domain=apps\..*|workshop_domain=$cluster_domain|g" "$overlay_path/kustomization.yaml"

        print_success "Updated overlay with actual cluster URLs (temporary)"
        print_status "Original template preserved for other users"
    fi
}

# Function to restore original overlay template
restore_overlay_template() {
    local overlay_path="$1"

    # Restore original kustomization.yaml if backup exists
    if [ -f "$overlay_path/kustomization.yaml.backup" ]; then
        mv "$overlay_path/kustomization.yaml.backup" "$overlay_path/kustomization.yaml"
        print_status "Restored original overlay template for future users"
    fi
}

# Function to deploy with kustomize
deploy_overlay() {
    local overlay_path="$1"
    local overlay_name=$(basename "$overlay_path")

    print_status "Deploying Workshop Template System with overlay: $overlay_name"

    # Check and install Gitea if needed (idempotent)
    if ! check_gitea_installation; then
        print_status "Gitea not found. Installing automatically..."
        if ! install_gitea; then
            print_error "Failed to install Gitea"
            exit 1
        fi

        # Configure Gitea
        if ! configure_gitea "$overlay_name"; then
            print_error "Failed to configure Gitea"
            exit 1
        fi
    else
        print_success "Gitea is already installed and running - skipping installation"
    fi

    # Update overlay with actual cluster URLs
    update_overlay_urls "$overlay_path"

    # Validate overlay
    if ! validate_overlay "$overlay_path"; then
        exit 1
    fi

    # Show what will be deployed
    print_status "Preview of resources to be deployed:"
    if command -v kustomize &> /dev/null; then
        kustomize build "$overlay_path" | grep -E "^(kind|metadata)" | head -20
    else
        oc kustomize "$overlay_path" | grep -E "^(kind|metadata)" | head -20
    fi
    
    echo ""
    read -p "Continue with deployment? (y/N): " confirm
    if [[ ! $confirm =~ ^[Yy]$ ]]; then
        echo "Deployment cancelled"
        exit 0
    fi
    
    # Deploy using kustomize
    print_status "Applying Kustomize configuration..."
    if command -v kustomize &> /dev/null; then
        kustomize build "$overlay_path" | oc apply -f -
    else
        oc apply -k "$overlay_path"
    fi
    
    # Wait for deployment
    print_status "Waiting for deployment to be ready..."
    oc wait --for=condition=available --timeout=300s deployment/llama-stack-server -n workshop-system || true
    
    # Show deployment status
    print_status "Deployment status:"
    oc get pods -n workshop-system
    
    echo ""
    print_status "Routes:"
    oc get routes -n workshop-system
    
    print_success "Workshop Template System deployed successfully!"

    # Restore original overlay template
    restore_overlay_template "$overlay_path"

    # Show next steps
    echo ""
    echo "🎯 Next Steps:"
    echo "=============="
    echo "1. Check that all pods are running: oc get pods -n workshop-system"
    echo "2. Access your workshop via the routes shown above"
    echo "3. Test the workshop chat functionality"
    echo "4. Interact with agents to update workshop content"
    echo ""
    echo "📚 Documentation: kubernetes/workshop-template-system/README.md"
}

# Main script logic
main() {
    echo "🚀 Workshop Template System Kustomize Deployment"
    echo "================================================"
    
    # Check if overlay specified
    if [ $# -eq 0 ]; then
        echo "Usage: $0 <overlay-name>"
        echo ""
        show_overlays
        exit 1
    fi
    
    local overlay_name="$1"
    local overlay_path="overlays/$overlay_name"
    
    # Check prerequisites
    check_prerequisites
    
    # Deploy the overlay
    deploy_overlay "$overlay_path"
}

# Run main function
main "$@"
