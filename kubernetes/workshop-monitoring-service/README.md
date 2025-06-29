# Workshop Monitoring Service - Kubernetes Deployment

This directory contains Kubernetes manifests and Kustomize configurations for deploying the Workshop Monitoring Service to OpenShift.

## 🎯 Overview

The Workshop Monitoring Service provides real-time monitoring and health tracking for all 6 workshop agents in the Workshop Template System:

- **workshop-chat** - RAG-based participant assistance
- **template-converter** - Repository-to-workshop transformation  
- **content-creator** - Original workshop content creation
- **source-manager** - Repository management and deployment
- **research-validation** - Internet-grounded fact-checking
- **documentation-pipeline** - Content monitoring and updates

## 📁 Directory Structure

```
kubernetes/workshop-monitoring-service/
├── base/                           # Base Kustomize configuration
│   ├── buildconfig.yaml           # OpenShift BuildConfig
│   ├── configmap.yaml             # Application configuration
│   ├── deployment.yaml            # Kubernetes Deployment
│   ├── imagestream.yaml           # OpenShift ImageStream
│   ├── kustomization.yaml         # Base Kustomize file
│   ├── route.yaml                 # OpenShift Route
│   └── service.yaml               # Kubernetes Service
├── overlays/                      # Environment-specific overlays
│   ├── development/               # Development environment
│   │   ├── deployment-patch.yaml  # Development-specific patches
│   │   └── kustomization.yaml     # Development Kustomize
│   └── production/                # Production environment
│       ├── deployment-patch.yaml  # Production-specific patches
│       └── kustomization.yaml     # Production Kustomize
├── deploy-monitoring.sh           # Deployment script
└── README.md                      # This file
```

## 🚀 Quick Deployment

### Option 1: Using the Deployment Script (Recommended)

```bash
# Deploy to development environment
./deploy-monitoring.sh

# Deploy to production environment
./deploy-monitoring.sh --environment production

# Deploy with custom namespace
./deploy-monitoring.sh --namespace my-workshop-system --logs
```

### Option 2: Using Kustomize Directly

```bash
# Create namespace
oc new-project workshop-system

# Deploy development environment
oc apply -k overlays/development/

# Deploy production environment
oc apply -k overlays/production/
```

### Option 3: Using the Complete System Script

The monitoring service is automatically deployed as part of the complete system:

```bash
# From the project root
./deploy-complete-system.sh
```

## 🔧 Configuration

### Environment Variables

The monitoring service can be configured using environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `QUARKUS_HTTP_PORT` | HTTP port | `8086` |
| `WORKSHOP_AGENTS_ENDPOINTS_*` | Agent endpoint URLs | `http://agent-name:port` |
| `WORKSHOP_HEALTH_CHECK_INTERVAL` | Health check interval | `30s` |
| `WORKSHOP_HEALTH_TIMEOUT` | Health check timeout | `5s` |
| `WORKSHOP_HEALTH_RETRY_ATTEMPTS` | Retry attempts | `3` |

### Agent Endpoints

The service monitors these agent endpoints by default:

```yaml
WORKSHOP_AGENTS_ENDPOINTS_WORKSHOP_CHAT: "http://workshop-chat:8080"
WORKSHOP_AGENTS_ENDPOINTS_TEMPLATE_CONVERTER: "http://template-converter:8081"
WORKSHOP_AGENTS_ENDPOINTS_CONTENT_CREATOR: "http://content-creator:8082"
WORKSHOP_AGENTS_ENDPOINTS_SOURCE_MANAGER: "http://source-manager:8083"
WORKSHOP_AGENTS_ENDPOINTS_RESEARCH_VALIDATION: "http://research-validation:8084"
WORKSHOP_AGENTS_ENDPOINTS_DOCUMENTATION_PIPELINE: "http://documentation-pipeline:8085"
```

## 🏗 Building Container Images

### Option 1: Using OpenShift BuildConfig

The BuildConfig automatically builds from the Git repository:

```bash
# Trigger a build
oc start-build workshop-monitoring-service -n workshop-system

# Follow build logs
oc logs -f bc/workshop-monitoring-service -n workshop-system
```

### Option 2: Building Locally

```bash
# From the workshop-monitoring-service directory
cd ../../workshop-monitoring-service/

# Build and push to registry
./build-container.sh --org your-quay-org --tag v1.0.0

# Build locally only
./build-container.sh --skip-push
```

## 🌍 Environment Configurations

### Development Environment

- **Namespace**: `workshop-system`
- **Replicas**: 1
- **Resources**: Lower limits for development
- **Health Check Interval**: 15 seconds (faster for testing)
- **Log Level**: DEBUG

### Production Environment

- **Namespace**: `workshop-system`
- **Replicas**: 2 (for high availability)
- **Resources**: Higher limits for production workloads
- **Health Check Interval**: 30 seconds
- **Log Level**: INFO
- **Enhanced health checks**: Longer timeouts and more retries

## 📊 Accessing the Dashboard

After deployment, the monitoring service will be available at:

```bash
# Get the route URL
ROUTE_URL=$(oc get route workshop-monitoring-service -n workshop-system -o jsonpath='{.spec.host}')

echo "Dashboard: https://${ROUTE_URL}"
echo "API Docs: https://${ROUTE_URL}/q/swagger-ui"
echo "Health: https://${ROUTE_URL}/q/health"
```

## 🔍 Monitoring and Troubleshooting

### Check Deployment Status

```bash
# Check pod status
oc get pods -l app=workshop-monitoring-service -n workshop-system

# Check deployment status
oc rollout status deployment workshop-monitoring-service -n workshop-system

# View logs
oc logs -f deployment/workshop-monitoring-service -n workshop-system
```

### Health Checks

```bash
# Test readiness
oc exec deployment/workshop-monitoring-service -n workshop-system -- curl -f http://localhost:8086/q/health/ready

# Test liveness
oc exec deployment/workshop-monitoring-service -n workshop-system -- curl -f http://localhost:8086/q/health/live

# Test API endpoints
curl -k https://$(oc get route workshop-monitoring-service -n workshop-system -o jsonpath='{.spec.host}')/api/monitoring/health
```

### Common Issues

1. **Pod not starting**: Check resource limits and image availability
2. **Health checks failing**: Verify agent endpoints are accessible
3. **Route not accessible**: Check OpenShift router configuration
4. **Build failures**: Verify source code and BuildConfig settings

## 🔄 Updates and Maintenance

### Updating the Service

```bash
# Update image tag in kustomization
# Then apply changes
oc apply -k overlays/development/

# Or trigger a new build
oc start-build workshop-monitoring-service -n workshop-system
```

### Scaling

```bash
# Scale up for high availability
oc scale deployment workshop-monitoring-service --replicas=3 -n workshop-system

# Scale down for resource conservation
oc scale deployment workshop-monitoring-service --replicas=1 -n workshop-system
```

### Configuration Updates

```bash
# Update ConfigMap
oc edit configmap workshop-monitoring-config -n workshop-system

# Restart deployment to pick up changes
oc rollout restart deployment workshop-monitoring-service -n workshop-system
```

## 🛡 Security Considerations

- **HTTPS**: All routes use TLS termination
- **RBAC**: Service runs with minimal required permissions
- **Network Policies**: Consider implementing network policies for production
- **Secrets**: Sensitive configuration should use OpenShift Secrets

## 📈 Performance Tuning

### Resource Requests and Limits

Development:
```yaml
resources:
  requests:
    memory: "128Mi"
    cpu: "50m"
  limits:
    memory: "256Mi"
    cpu: "250m"
```

Production:
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "200m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

### JVM Tuning

The service uses optimized JVM settings for container environments:
- `-Dquarkus.http.host=0.0.0.0`
- `-Djava.util.logging.manager=org.jboss.logmanager.LogManager`

## 🤝 Integration

The monitoring service integrates with:

- **Workshop Template System**: Monitors all 6 agents
- **OpenShift Routes**: Provides external access
- **OpenShift Health Checks**: Kubernetes-native health monitoring
- **OpenShift Logging**: Centralized log collection
- **OpenShift Metrics**: Resource usage monitoring

---

**🎯 Ready to monitor your Workshop Template System in OpenShift!**

For more information, see the main project documentation or contact the development team.
