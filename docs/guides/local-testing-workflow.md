# Local Testing Workflow Before OpenShift Deployment

**Version**: 1.0.0  
**Date**: 2025-01-04  
**Status**: Active  

## 🎯 **Overview**

This guide documents the mandatory local testing workflow that must be completed before deploying any changes to OpenShift. Following these rules prevents deployment failures, reduces debugging time, and ensures code quality.

## 📋 **Local Testing Rules**

### **Critical Rules (Must Follow)**

| Rule ID | Rule Name | Severity | Description |
|---------|-----------|----------|-------------|
| LOCAL-001 | Test Quarkus Service Locally | Error | Always test Quarkus services locally before building for OpenShift |
| LOCAL-002 | Validate Endpoints Locally | Error | Test all new endpoints locally before pushing to OpenShift |
| BUILD-003 | Local Build Before OpenShift | Error | Always build and test locally before triggering OpenShift builds |
| LOCAL-005 | Validate Configuration Locally | Error | Test configuration changes locally before deployment |

### **Recommended Rules (Should Follow)**

| Rule ID | Rule Name | Severity | Description |
|---------|-----------|----------|-------------|
| LOCAL-003 | Run Unit Tests Before Build | Warning | Execute test suite to catch regressions |
| LOCAL-007 | Validate JSON Responses Locally | Warning | Test API responses for correct JSON format |
| LOCAL-008 | Test Error Handling Locally | Warning | Verify error conditions are handled gracefully |
| LOCAL-010 | Test with Different Profiles | Warning | Test with dev, test, and prod profiles |

## 🚀 **Quick Start**

### **Automated Testing (Recommended)**
```bash
# Run comprehensive local testing
./scripts/local-test-before-deploy.sh

# If all tests pass, proceed with deployment
git add .
git commit -m "feat: your changes"
git push origin main
oc start-build workshop-monitoring-service-build -n workshop-system
```

### **Manual Testing (Step by Step)**
```bash
# 1. LOCAL-003: Run unit tests
cd workshop-monitoring-service
mvn test

# 2. BUILD-003: Local build
mvn clean package

# 3. LOCAL-001: Test service locally
mvn quarkus:dev

# 4. LOCAL-002: Test endpoints (in another terminal)
curl http://localhost:8086/api/pipeline/config
curl http://localhost:8086/api/monitoring/health

# 5. LOCAL-007: Validate JSON responses
curl http://localhost:8086/api/pipeline/config | jq '.'

# 6. Stop local service (Ctrl+C) and proceed with deployment
```

## 🔧 **Detailed Testing Procedures**

### **Step 1: Unit Testing (LOCAL-003)**
```bash
cd workshop-monitoring-service

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PipelineIntegrationResourceTest

# Run tests with coverage
mvn test jacoco:report
```

**Expected Output:**
```
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### **Step 2: Local Build (BUILD-003)**
```bash
# Clean build
mvn clean package

# Build with specific profile
mvn clean package -Dquarkus.profile=dev

# Build without tests (after tests pass)
mvn clean package -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

### **Step 3: Local Service Testing (LOCAL-001)**
```bash
# Start Quarkus in dev mode
mvn quarkus:dev

# Alternative: Start with specific profile
mvn quarkus:dev -Dquarkus.profile=dev

# Alternative: Start on different port
mvn quarkus:dev -Dquarkus.http.port=8087
```

**Expected Output:**
```
Listening for transport dt_socket at address: 5005
__  ____  __  _____   ___  __ ____  ____
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
INFO  [io.quarkus] workshop-monitoring-service X.X.X on JVM started in X.XXXs.
INFO  [io.quarkus] Profile dev activated. Live Coding activated.
INFO  [io.quarkus] Installed features: [...]
```

### **Step 4: Endpoint Testing (LOCAL-002)**

#### **Health Checks**
```bash
# Basic health check
curl http://localhost:8086/q/health

# Readiness check
curl http://localhost:8086/q/health/ready

# Liveness check
curl http://localhost:8086/q/health/live
```

#### **API Endpoints**
```bash
# Test monitoring endpoints
curl http://localhost:8086/api/monitoring/info
curl http://localhost:8086/api/monitoring/health
curl http://localhost:8086/api/monitoring/agents

# Test new pipeline endpoints
curl http://localhost:8086/api/pipeline/config
curl -X POST -H "Content-Type: application/json" \
  -d '{"repository-url": "https://github.com/test/repo.git", "workshop-name": "test"}' \
  http://localhost:8086/api/pipeline/validate-parameters
```

### **Step 5: JSON Validation (LOCAL-007)**
```bash
# Validate JSON responses
curl http://localhost:8086/api/pipeline/config | jq '.'
curl http://localhost:8086/api/monitoring/agents | jq '.data'

# Check specific fields
curl http://localhost:8086/api/pipeline/config | jq '.data.validation_types'
curl http://localhost:8086/api/pipeline/config | jq '.data.parameters.required'
```

### **Step 6: Error Testing (LOCAL-008)**
```bash
# Test 404 errors
curl -i http://localhost:8086/api/nonexistent

# Test validation errors
curl -X POST -H "Content-Type: application/json" \
  -d '{"invalid": "data"}' \
  http://localhost:8086/api/pipeline/validate-parameters

# Test malformed JSON
curl -X POST -H "Content-Type: application/json" \
  -d '{invalid json}' \
  http://localhost:8086/api/pipeline/validate-parameters
```

### **Step 7: Performance Testing (LOCAL-009)**
```bash
# Basic response time test
curl -w '%{time_total}\n' -o /dev/null -s http://localhost:8086/api/monitoring/health

# Simple load test (if ab is available)
ab -n 100 -c 10 http://localhost:8086/api/pipeline/config

# Alternative with curl
for i in {1..10}; do
  curl -w '%{time_total}\n' -o /dev/null -s http://localhost:8086/api/monitoring/health
done
```

## ⚠️ **Common Issues and Solutions**

### **Issue 1: Port Already in Use**
```bash
# Error: Port 8086 already in use
# Solution: Use different port
mvn quarkus:dev -Dquarkus.http.port=8087

# Or kill existing process
lsof -ti:8086 | xargs kill -9
```

### **Issue 2: Tests Failing**
```bash
# Error: Tests fail during mvn test
# Solution: Check test logs
mvn test -X

# Run specific failing test
mvn test -Dtest=FailingTestClass -X
```

### **Issue 3: Build Failures**
```bash
# Error: Compilation errors during build
# Solution: Check for syntax errors
mvn compile

# Clean and rebuild
mvn clean compile
```

### **Issue 4: Endpoint Not Found**
```bash
# Error: 404 when testing endpoints
# Solution: Check if service is running
curl http://localhost:8086/q/health/ready

# Check application logs
tail -f target/quarkus.log
```

## ✅ **Success Criteria**

Before proceeding to OpenShift deployment, ensure:

- ✅ All unit tests pass (`mvn test`)
- ✅ Local build succeeds (`mvn clean package`)
- ✅ Service starts without errors (`mvn quarkus:dev`)
- ✅ All endpoints return expected responses
- ✅ JSON responses are valid and well-formed
- ✅ Error scenarios are handled gracefully
- ✅ Response times are acceptable (< 1 second)
- ✅ No compilation or runtime errors

## 🔄 **Integration with Deployment Workflow**

### **Complete Workflow**
```bash
# 1. Local testing
./scripts/local-test-before-deploy.sh

# 2. Git workflow (if tests pass)
git add .
git commit -m "feat: add new pipeline configuration endpoints"
git push origin main

# 3. OpenShift deployment
oc start-build workshop-monitoring-service-build -n workshop-system

# 4. Verify deployment
oc rollout status deployment/workshop-monitoring-service -n workshop-system

# 5. Test in OpenShift
curl -k https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/api/pipeline/config
```

### **Rollback Procedure**
```bash
# If deployment fails, rollback
oc rollout undo deployment/workshop-monitoring-service -n workshop-system

# Check rollback status
oc rollout status deployment/workshop-monitoring-service -n workshop-system
```

## 📚 **Related Documentation**

- [Local Testing Rules](../../rules/local-testing-rules.json)
- [Git and Build Workflow Safety Rules](../../rules/git-build-workflow-safety-rules.json)
- [Tekton Pipeline Testing Workflow](./tekton-pipeline-testing-workflow.md)
- [ADR-0037: Workflow Safety Rules](../adrs/ADR-0037-workflow-safety-rules.md)

---

**Maintained by**: Workshop Template System Development Team  
**Last Updated**: 2025-01-04  
**Next Review**: 2025-02-04
