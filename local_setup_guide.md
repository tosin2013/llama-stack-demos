# Local Development Setup Guide

This guide covers two main development scenarios:
1. **Basic Llama Stack Setup** - Running Llama Stack server with Ollama and Podman
2. **Workshop Template System Development** - Local testing with Quarkus middleware and agents

**Related ADRs**:
- [ADR-0018: Quarkus Middleware Architecture](docs/adr/ADR-0018-quarkus-middleware-architecture.md)
- [ADR-0012: Testing Framework](docs/adr/ADR-0012-testing-framework.md)

---

# Part 1: Basic Llama Stack Setup

## **1. Prerequisites**
Ensure you have the following installed:
- **Podman** ([Install Podman](https://podman.io/docs/installation))
- **Python 3.10+**
- **pip** ([Install pip](https://pip.pypa.io/en/stable/installation/))
- **Ollama** ([Install Ollama](https://ollama.com/download))


Verify installation:
```bash
podman --version
python3 --version
pip --version
ollama --version
```

---

## **2. Start Ollama**
Before running Llama Stack, start the Ollama server with:
```bash
ollama run llama3.2:3b-instruct-fp16 --keepalive 60m
```
This ensures the model stays loaded in memory for 60 minutes.

---

## **3. Set Up Environment Variables**
Set up the required environment variables:
```bash
export INFERENCE_MODEL="meta-llama/Llama-3.2-3B-Instruct"
export LLAMA_STACK_PORT=8321
```

---

## **4. Run Llama Stack Server with Podman**
Pull the required image:
```bash
podman pull docker.io/llamastack/distribution-ollama
```
Before executing the next command, make sure to create a local directory to mount into the container’s file system.

```bash
mkdir -p ~/.llama
```

Now run the server using:
```bash
podman run -it \
  -p $LLAMA_STACK_PORT:$LLAMA_STACK_PORT \
  -v ~/.llama:/root/.llama \
  --env INFERENCE_MODEL=$INFERENCE_MODEL \
  --env OLLAMA_URL=http://host.containers.internal:11434 \
  llamastack/distribution-ollama \
  --port $LLAMA_STACK_PORT
```
If needed, create and use a network:
```bash
podman network create llama-net
podman run --privileged --network llama-net -it \
  -p $LLAMA_STACK_PORT:$LLAMA_STACK_PORT \
  llamastack/distribution-ollama \
  --port $LLAMA_STACK_PORT
```

Verify the container is running:
```bash
podman ps
```

---

## **5. Set Up Python Environment**
Create a virtual environment using `uv` and install required libraries:

```bash
pip install uv
uv sync
source .venv/bin/activate # macOS/Linux
# On Windows: llama-stack-demo\Scripts\activate
```
Verify installation:
```bash
pip list | grep llama-stack-client
```
---

## **6. Configure the Client**
Set up the client to connect to the Llama Stack server:
```bash
llama-stack-client configure --endpoint http://host.containers.internal:$LLAMA_STACK_PORT
```
List available models:
```bash
llama-stack-client models list
```

---

## **7. Quickly setting up your environment**

Now that your environemnt has gone through the initial set up, you can quickly return to a running ollama and llama stack server using the `setup_local` command available in the [Makefile](./Makefile).

```bash
make setup_local
```

---

## **8. Debugging Common Issues**
**Check if Podman is Running:**
```bash
podman ps
```

**Ensure the Virtual Environment is Activated:**
```bash
source llama-stack-demo/bin/activate
```

**Reinstall the Client if Necessary:**
```bash
pip uninstall llama-stack-client
pip install llama-stack-client
```

**Test Importing the Client in Python:**
```bash
python -c "from llama_stack_client import LlamaStackClient; print(LlamaStackClient)"
```

---

# Part 2: Workshop Template System Development

## **9. Workshop Template System Local Testing**

This section covers local development and testing of the Workshop Template System with Quarkus middleware and agents, following **ADR-0018 Quarkus Middleware Architecture**.

### **9.1 Prerequisites for Workshop Development**
In addition to the basic prerequisites, ensure you have:
- **Java 17+** (required for Quarkus)
- **Maven 3.8+**
- **Docker or Podman** (for agent containers)
- **Git** (for repository operations)

Verify additional tools:
```bash
java -version
mvn -version
git --version
```

### **9.2 Architecture Overview**
```
Local Development Architecture (ADR-0018):

┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Test Scripts  │───▶│ Quarkus Service  │───▶│  Agent Network  │
│                 │    │   (Middleware)   │    │   (Podman)      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │   Mock Endpoints │
                       │  (Development)   │
                       └──────────────────┘
```

### **9.3 Start Quarkus Middleware Service**

**Option A: Development Mode (Recommended)**
```bash
cd workshop-monitoring-service
mvn quarkus:dev
```
This starts the service on `http://localhost:8080` with hot reload.

**Option B: Production Mode**
```bash
cd workshop-monitoring-service
mvn clean package -Dquarkus.package.type=uber-jar
java -jar target/workshop-monitoring-service-1.0.0-SNAPSHOT-runner.jar
```

**Verify Middleware Service:**
```bash
# Test health endpoint
curl http://localhost:8080/api/pipeline/health

# Test mock endpoints (dev profile only)
curl -X POST http://localhost:8080/api/pipeline/mock/content-creator/create-workshop \
  -H "Content-Type: application/json" \
  -d '{"workshop_name":"test-workshop","repository_url":"https://github.com/example/repo"}'
```

### **9.4 Start RAG Stack (Required for RAG Content Quality Testing)**

For RAG content quality workflow testing, you need the complete RAG stack:

```bash
# Start RAG infrastructure (Milvus + etcd + MinIO)
./scripts/start-rag-stack-local.sh
```

This will start:
- **Milvus** (Vector Database) - `http://localhost:19530`
- **etcd** (Metadata Storage) - `http://localhost:2379`
- **MinIO** (Object Storage) - `http://localhost:9000`

**Verify RAG Stack:**
```bash
# Check RAG containers
podman ps | grep workshop-

# Test Milvus health
curl http://localhost:9091/healthz

# Test MinIO (credentials: minioadmin/minioadmin)
curl http://localhost:9000
```

### **9.5 Start Agent Network (Optional)**

For full integration testing, start the agent network:

```bash
# Start all 6 agents using Podman with RAG configuration
MILVUS_ENDPOINT=http://localhost:19530 RAG_ENABLED=true ./scripts/start-agents-local.sh

# Or start individual agents with RAG support
podman run -d --name workshop-chat-agent \
  -p 8081:80 \
  -e MILVUS_ENDPOINT=http://localhost:19530 \
  -e RAG_ENABLED=true \
  workshop-chat-agent:latest
```

**Verify Agent Network:**
```bash
# Check running agents
podman ps | grep agent

# Test agent connectivity
curl http://localhost:8081/health  # workshop-chat-agent
curl http://localhost:8082/health  # content-creator-agent
```

### **9.6 Test Workshop Creation Workflows**

**Test Workflow 1: New Workshop Creation**
```bash
# Test with mock endpoints (no agents required)
curl -X POST http://localhost:8080/api/pipeline/mock/content-creator/create-workshop \
  -H "Content-Type: application/json" \
  -d '{
    "workshop_name": "local-test-workshop",
    "repository_url": "https://github.com/tosin2013/ansible-controller-cac.git",
    "base_template": "showroom_template_default"
  }'

# Test with real agents (requires agent network)
curl -X POST http://localhost:8080/api/pipeline/content-creator/create-workshop \
  -H "Content-Type: application/json" \
  -d '{
    "workshop_name": "local-test-workshop",
    "repository_url": "https://github.com/tosin2013/ansible-controller-cac.git",
    "base_template": "showroom_template_default"
  }'
```

**Test Workshop Maintenance (NEW)**
```bash
# Test workshop update with human approval
curl -X POST http://localhost:8080/api/pipeline/mock/source-manager/update-workshop \
  -H "Content-Type: application/json" \
  -d '{
    "repository_name": "existing-workshop-repo",
    "workshop_name": "maintenance-test",
    "update_type": "content-update",
    "require_approval": true,
    "approver": "developer"
  }'

# Test human approval workflow
curl -X POST http://localhost:8080/api/pipeline/mock/human-oversight/approve-workshop-update \
  -H "Content-Type: application/json" \
  -d '{
    "approval_id": "test-approval-123",
    "repository_name": "existing-workshop-repo",
    "approver": "developer",
    "approval_decision": "approved"
  }'
```

**Test RAG Content Quality Workflow (NEW)**
```bash
# Test external reference validation
curl -X POST http://localhost:8080/api/pipeline/mock/research-validation/validate-external-references \
  -H "Content-Type: application/json" \
  -d '{
    "workshop_name": "quality-test-workshop",
    "workshop_content": "Sample workshop content with external links",
    "reference_types": "all",
    "check_accessibility": true,
    "check_freshness": true,
    "quality_scoring": true
  }'

# Test RAG content update with external references
curl -X POST http://localhost:8080/api/pipeline/mock/research-validation/update-rag-content \
  -H "Content-Type: application/json" \
  -d '{
    "workshop_name": "quality-test-workshop",
    "workshop_content": "Workshop content to enhance",
    "external_references": [
      {
        "url": "https://docs.example.com/api",
        "type": "documentation",
        "authority_score": 0.9
      }
    ],
    "quality_threshold": 0.75,
    "update_mode": "incremental"
  }'

# Test content enhancement with validated references
curl -X POST http://localhost:8080/api/pipeline/mock/content-creator/enhance-with-references \
  -H "Content-Type: application/json" \
  -d '{
    "workshop_name": "quality-test-workshop",
    "workshop_content": "Original workshop content",
    "validated_references": [
      {
        "url": "https://docs.example.com/tutorial",
        "title": "Official Tutorial",
        "quality_score": 0.95,
        "relevance_score": 0.88
      }
    ],
    "enhancement_strategy": "contextual",
    "quality_threshold": 0.8
  }'
```

### **9.7 Run Comprehensive Test Suite**

**Python Test Suite:**
```bash
# Activate virtual environment
source .venv/bin/activate

# Run middleware integration tests
python -m pytest tests/test_middleware_integration.py -v

# Run workshop creation tests
python -m pytest tests/test_workshop_creation.py -v

# Run workshop maintenance tests (NEW)
python -m pytest tests/test_workshop_maintenance.py -v

# Run all workshop system tests
python -m pytest tests/workshop/ -v
```

**Manual Test Script:**
```bash
# Run the comprehensive test script
./test-workshop-maintenance-pipeline.sh

# Test specific workflows
./scripts/test-workflow-1-local.sh
./scripts/test-workflow-3-local.sh
./scripts/test-maintenance-workflow-local.sh  # NEW
```

### **9.7 Development Workflow**

**Typical Development Cycle:**
1. **Start Services:**
   ```bash
   # Terminal 1: Start Quarkus in dev mode
   cd workshop-monitoring-service && mvn quarkus:dev

   # Terminal 2: Start Ollama (if needed)
   ollama run llama3.2:3b-instruct-fp16 --keepalive 60m
   ```

2. **Make Changes:**
   - Edit middleware endpoints in `PipelineIntegrationResource.java`
   - Update request models in `model/pipeline/`
   - Modify agent integration logic

3. **Test Changes:**
   ```bash
   # Test mock endpoints (immediate feedback)
   curl -X POST http://localhost:8080/api/pipeline/mock/...

   # Test with real agents (full integration)
   curl -X POST http://localhost:8080/api/pipeline/...
   ```

4. **Validate:**
   ```bash
   # Run test suite
   python -m pytest tests/workshop/ -v

   # Check logs
   tail -f workshop-monitoring-service/target/quarkus.log
   ```

### **9.8 Debugging and Troubleshooting**

**Common Issues:**

**Quarkus Service Won't Start:**
```bash
# Check Java version
java -version  # Should be 17+

# Check port availability
lsof -i :8080

# Clean and rebuild
cd workshop-monitoring-service
mvn clean compile
```

**Agent Network Issues:**
```bash
# Check agent containers
podman ps -a | grep agent

# Check agent logs
podman logs workshop-chat-agent

# Restart agent network
./scripts/restart-agents-local.sh
```

**Mock Endpoints Not Working:**
```bash
# Verify dev profile is active
curl http://localhost:8080/api/pipeline/health | jq .profile

# Should return "dev" for mock endpoints to work
```

**Integration Test Failures:**
```bash
# Check middleware connectivity
curl http://localhost:8080/api/pipeline/health

# Check agent connectivity (if using real agents)
curl http://localhost:8081/health

# Run tests with verbose output
python -m pytest tests/workshop/ -v -s
```

### **9.9 Integration with OpenShift Testing**

**Prepare for OpenShift Deployment:**
```bash
# Test locally first
./test-workshop-maintenance-pipeline.sh

# Build container images
podman build -t workshop-monitoring-service:latest workshop-monitoring-service/

# Test container locally
podman run -p 8080:8080 workshop-monitoring-service:latest

# Push to registry (when ready)
podman tag workshop-monitoring-service:latest quay.io/your-org/workshop-monitoring-service:latest
podman push quay.io/your-org/workshop-monitoring-service:latest
```

**Validate Configuration:**
```bash
# Check application.properties
cat workshop-monitoring-service/src/main/resources/application.properties

# Verify agent endpoints are correctly configured
grep -r "workshop.agents.endpoints" workshop-monitoring-service/
```

---

## **10. Quick Reference Commands**

### **Basic Llama Stack:**
```bash
# Start everything
make setup_local

# Just Ollama
ollama run llama3.2:3b-instruct-fp16 --keepalive 60m

# Just Llama Stack
podman run -it -p 8321:8321 llamastack/distribution-ollama --port 8321
```

### **Workshop Template System:**
```bash
# Start Quarkus middleware
cd workshop-monitoring-service && mvn quarkus:dev

# Test mock endpoints
curl http://localhost:8080/api/pipeline/mock/content-creator/create-workshop \
  -H "Content-Type: application/json" \
  -d '{"workshop_name":"test","repository_url":"https://github.com/example/repo"}'

# Run test suite
python -m pytest tests/workshop/ -v

# Test workshop maintenance
curl -X POST http://localhost:8080/api/pipeline/mock/source-manager/update-workshop \
  -H "Content-Type: application/json" \
  -d '{"repository_name":"test-repo","workshop_name":"test","require_approval":true}'

# Test RAG content quality (NEW)
curl -X POST http://localhost:8080/api/pipeline/mock/research-validation/validate-external-references \
  -H "Content-Type: application/json" \
  -d '{"workshop_name":"test","workshop_content":"content","quality_scoring":true}'
```

---
