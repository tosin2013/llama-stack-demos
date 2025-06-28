# Deployment and Maintenance Procedures

Comprehensive guide for deploying, operating, and maintaining the Workshop Template System in production environments.

## 🎯 Deployment Overview

The Workshop Template System supports multiple deployment scenarios:
- **Local Development**: Single-machine setup for development and testing
- **OpenShift/Kubernetes**: Container-based production deployment
- **RHPDS Integration**: Red Hat Product Demo System deployment
- **Showroom Platform**: Red Hat Showroom platform integration

## 🏗️ Prerequisites

### System Requirements
```yaml
Minimum Requirements:
  CPU: 4 cores
  Memory: 8 GB RAM
  Storage: 50 GB available space
  Network: Internet connectivity for external sources

Recommended Requirements:
  CPU: 8 cores
  Memory: 16 GB RAM
  Storage: 100 GB available space
  Network: High-speed internet connection
```

### Software Dependencies
```bash
# Core Dependencies
- Python 3.8+
- Ollama with Llama 3.2 3B model
- Llama Stack server
- Git
- curl/wget

# Container Dependencies (for production)
- Podman or Docker
- OpenShift CLI (oc) or kubectl
- Helm (optional)

# External Services
- Pinecone account (for RAG functionality)
- GitHub access (for repository management)
```

## 🚀 Local Development Deployment

### Step 1: Environment Setup
```bash
# Clone the workshop system
git clone https://github.com/your-org/workshop-template-system.git
cd workshop-template-system

# Create Python virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

### Step 2: Ollama and Llama Stack Setup
```bash
# Install and start Ollama
curl -fsSL https://ollama.ai/install.sh | sh
ollama serve &

# Pull Llama model
ollama pull llama3.2:3b

# Start Llama Stack server
LLAMA_STACK_ENDPOINT=http://localhost:8321 \
INFERENCE_MODEL_ID=meta-llama/Llama-3.2-3B-Instruct \
python -m llama_stack.distribution.server.server \
  --yaml-config llama_stack_config.yaml &
```

### Step 3: Agent Deployment
```bash
# Start all 6 agents in separate terminals or use screen/tmux

# Terminal 1: Workshop Chat Agent
LLAMA_STACK_ENDPOINT=http://localhost:8321 \
INFERENCE_MODEL_ID=meta-llama/Llama-3.2-3B-Instruct \
python -m demos.workshop_template_system --agent-name workshop_chat --port 10040

# Terminal 2: Template Converter Agent
LLAMA_STACK_ENDPOINT=http://localhost:8321 \
INFERENCE_MODEL_ID=meta-llama/Llama-3.2-3B-Instruct \
python -m demos.workshop_template_system --agent-name template_converter --port 10041

# Terminal 3: Documentation Pipeline Agent
LLAMA_STACK_ENDPOINT=http://localhost:8321 \
INFERENCE_MODEL_ID=meta-llama/Llama-3.2-3B-Instruct \
python -m demos.workshop_template_system --agent-name documentation_pipeline --port 10050

# Terminal 4: Source Manager Agent
LLAMA_STACK_ENDPOINT=http://localhost:8321 \
INFERENCE_MODEL_ID=meta-llama/Llama-3.2-3B-Instruct \
python -m demos.workshop_template_system --agent-name source_manager --port 10060

# Terminal 5: Research & Validation Agent
LLAMA_STACK_ENDPOINT=http://localhost:8321 \
INFERENCE_MODEL_ID=meta-llama/Llama-3.2-3B-Instruct \
python -m demos.workshop_template_system --agent-name research_validation --port 10070

# Terminal 6: Content Creator Agent
LLAMA_STACK_ENDPOINT=http://localhost:8321 \
INFERENCE_MODEL_ID=meta-llama/Llama-3.2-3B-Instruct \
python -m demos.workshop_template_system --agent-name content_creator --port 10080
```

### Step 4: Verification
```bash
# Check agent health
for port in 10040 10041 10050 10060 10070 10080; do
  echo "Checking agent on port $port:"
  curl -s http://localhost:$port/agent-card | jq '.name' || echo "Agent not responding"
done

# Test basic functionality
curl -X POST http://localhost:10041/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-001",
    "params": {
      "id": "test-001",
      "sessionId": "test-session",
      "message": {
        "role": "user",
        "parts": [{"type": "text", "text": "Test message"}]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

## ☁️ OpenShift/Kubernetes Production Deployment

### Step 1: Container Images
```dockerfile
# Dockerfile for workshop agents
FROM registry.access.redhat.com/ubi8/python-39:latest

USER root

# Install system dependencies
RUN dnf update -y && \
    dnf install -y git curl && \
    dnf clean all

USER 1001

# Copy application code
COPY --chown=1001:0 . /opt/app-root/src/
WORKDIR /opt/app-root/src

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Expose agent port
EXPOSE 8080

# Start agent
CMD ["python", "-m", "demos.workshop_template_system"]
```

### Step 2: Kubernetes Manifests
```yaml
# workshop-system-namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: workshop-system
  labels:
    name: workshop-system

---
# llama-stack-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: llama-stack-server
  namespace: workshop-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: llama-stack-server
  template:
    metadata:
      labels:
        app: llama-stack-server
    spec:
      containers:
      - name: llama-stack
        image: llama-stack:latest
        ports:
        - containerPort: 8321
        env:
        - name: INFERENCE_MODEL_ID
          value: "meta-llama/Llama-3.2-3B-Instruct"
        resources:
          requests:
            memory: "4Gi"
            cpu: "2"
          limits:
            memory: "8Gi"
            cpu: "4"

---
# workshop-agents-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: workshop-chat-agent
  namespace: workshop-system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: workshop-chat-agent
  template:
    metadata:
      labels:
        app: workshop-chat-agent
    spec:
      containers:
      - name: workshop-chat-agent
        image: workshop-system:latest
        ports:
        - containerPort: 8080
        env:
        - name: AGENT_NAME
          value: "workshop_chat"
        - name: AGENT_PORT
          value: "8080"
        - name: LLAMA_STACK_ENDPOINT
          value: "http://llama-stack-server:8321"
        - name: INFERENCE_MODEL_ID
          value: "meta-llama/Llama-3.2-3B-Instruct"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1"
        livenessProbe:
          httpGet:
            path: /agent-card
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /agent-card
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

### Step 3: Services and Routes
```yaml
# workshop-services.yaml
apiVersion: v1
kind: Service
metadata:
  name: llama-stack-server
  namespace: workshop-system
spec:
  selector:
    app: llama-stack-server
  ports:
  - port: 8321
    targetPort: 8321

---
apiVersion: v1
kind: Service
metadata:
  name: workshop-chat-agent
  namespace: workshop-system
spec:
  selector:
    app: workshop-chat-agent
  ports:
  - port: 80
    targetPort: 8080

---
# OpenShift Route
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: workshop-chat-agent
  namespace: workshop-system
spec:
  to:
    kind: Service
    name: workshop-chat-agent
  port:
    targetPort: 8080
  tls:
    termination: edge
```

### Step 4: ConfigMaps and Secrets
```yaml
# workshop-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: workshop-system-config
  namespace: workshop-system
data:
  config.json: |
    {
      "agents": {
        "workshop_chat": {
          "port": 8080,
          "rag_enabled": true
        },
        "template_converter": {
          "port": 8080,
          "analysis_depth": "standard"
        }
      }
    }

---
apiVersion: v1
kind: Secret
metadata:
  name: workshop-system-secrets
  namespace: workshop-system
type: Opaque
data:
  pinecone-api-key: <base64-encoded-key>
  github-token: <base64-encoded-token>
```

### Step 5: Deployment Commands
```bash
# Deploy to OpenShift
oc apply -f workshop-system-namespace.yaml
oc apply -f workshop-config.yaml
oc apply -f llama-stack-deployment.yaml
oc apply -f workshop-agents-deployment.yaml
oc apply -f workshop-services.yaml

# Verify deployment
oc get pods -n workshop-system
oc get services -n workshop-system
oc get routes -n workshop-system

# Check logs
oc logs -f deployment/workshop-chat-agent -n workshop-system
```

## 🔄 Monitoring and Maintenance

### Health Monitoring
```bash
# Health check script
#!/bin/bash
NAMESPACE="workshop-system"
AGENTS=("workshop-chat-agent" "template-converter-agent" "documentation-pipeline-agent" "source-manager-agent" "research-validation-agent" "content-creator-agent")

for agent in "${AGENTS[@]}"; do
  echo "Checking $agent..."
  oc get pods -l app=$agent -n $NAMESPACE
  
  # Check if pods are ready
  ready_pods=$(oc get pods -l app=$agent -n $NAMESPACE -o jsonpath='{.items[*].status.conditions[?(@.type=="Ready")].status}' | grep -o True | wc -l)
  total_pods=$(oc get pods -l app=$agent -n $NAMESPACE --no-headers | wc -l)
  
  echo "$agent: $ready_pods/$total_pods pods ready"
done
```

### Log Aggregation
```yaml
# fluentd-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: fluentd-config
  namespace: workshop-system
data:
  fluent.conf: |
    <source>
      @type tail
      path /var/log/containers/*workshop-system*.log
      pos_file /var/log/fluentd-workshop-system.log.pos
      tag kubernetes.*
      format json
    </source>
    
    <match kubernetes.**>
      @type elasticsearch
      host elasticsearch.logging.svc.cluster.local
      port 9200
      index_name workshop-system
    </match>
```

### Backup Procedures
```bash
# Backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/workshop-system/$DATE"

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup configurations
oc get configmaps -n workshop-system -o yaml > $BACKUP_DIR/configmaps.yaml
oc get secrets -n workshop-system -o yaml > $BACKUP_DIR/secrets.yaml

# Backup persistent data (if any)
# oc rsync pod-name:/data $BACKUP_DIR/data

# Backup Pinecone index metadata
# curl -X GET "https://api.pinecone.io/indexes" \
#   -H "Api-Key: $PINECONE_API_KEY" > $BACKUP_DIR/pinecone-indexes.json

echo "Backup completed: $BACKUP_DIR"
```

### Update Procedures
```bash
# Rolling update script
#!/bin/bash
NAMESPACE="workshop-system"
NEW_IMAGE="workshop-system:v2.0.0"

# Update deployment images
oc set image deployment/workshop-chat-agent workshop-chat-agent=$NEW_IMAGE -n $NAMESPACE
oc set image deployment/template-converter-agent template-converter-agent=$NEW_IMAGE -n $NAMESPACE

# Wait for rollout to complete
oc rollout status deployment/workshop-chat-agent -n $NAMESPACE
oc rollout status deployment/template-converter-agent -n $NAMESPACE

# Verify new version
oc get pods -n $NAMESPACE -o jsonpath='{.items[*].spec.containers[*].image}' | tr ' ' '\n' | sort | uniq
```

## 📊 Performance Tuning

### Resource Optimization
```yaml
# Optimized resource requests and limits
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"

# Horizontal Pod Autoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: workshop-chat-agent-hpa
  namespace: workshop-system
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: workshop-chat-agent
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Caching Configuration
```yaml
# Redis cache for agent responses
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-cache
  namespace: workshop-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis-cache
  template:
    metadata:
      labels:
        app: redis-cache
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "200m"
```

## 🔒 Security Considerations

### Network Policies
```yaml
# workshop-network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: workshop-system-network-policy
  namespace: workshop-system
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: workshop-system
    - podSelector: {}
  egress:
  - to: []
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
  - to:
    - namespaceSelector:
        matchLabels:
          name: workshop-system
```

### RBAC Configuration
```yaml
# workshop-rbac.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: workshop-system-sa
  namespace: workshop-system

---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: workshop-system-role
  namespace: workshop-system
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list", "watch"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: workshop-system-rolebinding
  namespace: workshop-system
subjects:
- kind: ServiceAccount
  name: workshop-system-sa
  namespace: workshop-system
roleRef:
  kind: Role
  name: workshop-system-role
  apiGroup: rbac.authorization.k8s.io
```

## 🎯 Troubleshooting Common Issues

### Agent Communication Issues
```bash
# Check agent connectivity
for port in 10040 10041 10050 10060 10070 10080; do
  echo "Testing agent on port $port:"
  curl -s http://localhost:$port/agent-card || echo "Connection failed"
done

# Check Llama Stack connectivity
curl -s http://localhost:8321/v1/models || echo "Llama Stack not responding"
```

### Performance Issues
```bash
# Check resource usage
oc top pods -n workshop-system
oc describe hpa -n workshop-system

# Check logs for errors
oc logs -f deployment/workshop-chat-agent -n workshop-system | grep ERROR
```

### External Service Issues
```bash
# Test external connectivity
curl -s https://docs.openshift.com/ || echo "External docs unreachable"
curl -s https://api.pinecone.io/indexes -H "Api-Key: $PINECONE_API_KEY" || echo "Pinecone unreachable"
```

---

*This deployment guide provides comprehensive procedures for production deployment and ongoing maintenance of the Workshop Template System.*
