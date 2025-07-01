# Tekton-Agent Integration Testing Plan - OpenShift Cluster Validation

**Document**: TEKTON_TESTING_PLAN.md
**Created**: 2025-06-29
**Updated**: 2025-06-30
**Purpose**: Test complete Tekton pipeline integration with Quarkus middleware architecture
**Target**: 100% success rate for all workshop workflows including maintenance
**Cluster**: apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com
**ADR Reference**: ADR-0018 Quarkus Middleware Architecture, ADR-0006 Tekton-Agent Integration

## 🎯 Live Cluster Testing Objectives

Test the complete Tekton-Agent integration with Quarkus middleware against our actual OpenShift cluster to validate:
- **Middleware Architecture**: Tekton pipelines → Quarkus service → Agents (ADR-0018)
- **Workshop Creation**: New workshop generation from repositories (Workflow 1)
- **Workshop Enhancement**: Existing workshop updates (Workflow 3)
- **Workshop Maintenance**: Content updates with human approval (NEW)
- **Human Oversight Integration**: Approval gates and monitoring UI
- **Live Gitea Operations**: Repository creation, updates, and deployment
- **End-to-end Functionality**: Complete workshop lifecycle management

## �️ Current Cluster Infrastructure

### OpenShift Cluster Details
- **Cluster URL**: `https://api.cluster-9cfzr.9cfzr.sandbox180.opentlc.com:6443`
- **Console URL**: `https://console-openshift-console.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`
- **Namespace**: `workshop-system`
- **Tekton**: OpenShift Pipelines operator installed

### Deployed Services
- **Workshop Monitoring Service (Middleware)**: `https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`
  - Pipeline integration endpoints: `/api/pipeline/*`
  - Human oversight endpoints: `/api/oversight/*`
  - Mock endpoints for testing: `/api/pipeline/mock/*`
- **6 Agent Services**: Running in workshop-system namespace (A2A protocol)
- **Gitea Service**: Available for repository operations and workshop hosting
- **BuildConfig Infrastructure**: Ready for workshop deployment and updates

### 📊 Test Repositories & Workflows

| Repository | Workflow Type | Pipeline | Description |
|------------|---------------|----------|-------------|
| **DDD Hexagonal Workshop** | Workflow 3 | `workflow-3-enhance-workshop` | Existing workshop enhancement |
| **Ansible Controller CaC** | Workflow 1 | `workflow-1-new-workshop` | Application → New workshop |
| **Llama Stack Demos** | Workflow 1 | `workflow-1-new-workshop` | Tutorial → New workshop |
| **Existing Workshop Maintenance** | Maintenance | `workshop-maintenance-pipeline` | **NEW**: Content updates with approval |

### 🔄 Pipeline Architecture (ADR-0018)
```
Tekton Pipeline → Quarkus Middleware → Agent A2A Protocol
                ↓
        Human Oversight Approval
                ↓
        Gitea Repository Operations
```

## � Test Execution Plan

### Phase 1: Environment Validation (30 minutes)
**Status**: ⏳ PENDING

**Pre-Test Checklist**:
- [ ] Verify OpenShift cluster access: `oc whoami`
- [ ] Check workshop-system namespace: `oc project workshop-system`
- [ ] Validate agent deployments: `oc get pods | grep agent`
- [ ] Test middleware service: `curl -k https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/api/pipeline/health`
- [ ] Check Tekton installation: `oc get pipelines,tasks -n workshop-system`
- [ ] Validate middleware endpoints: Test `/api/pipeline/mock/*` endpoints
- [ ] Check shared workspace: `oc get pvc shared-workspace-storage -n workshop-system`

**Environment Validation Script**:
```bash
#!/bin/bash
echo "🔍 Validating OpenShift Cluster Environment"
echo "Cluster: apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
echo "Namespace: workshop-system"
echo ""

# Check cluster access
oc whoami
oc project workshop-system

# Check agent pods
echo "📊 Agent Status:"
oc get pods -l app=workshop-template-system

# Check Tekton resources
echo "🔧 Tekton Resources:"
oc get pipelines,tasks -n workshop-system

# Test human oversight service
echo "👥 Human Oversight Service:"
curl -k -s https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/health || echo "Service not accessible"
```

**Progress Log**:
```
[Date] [Status] [Notes]
2025-06-29 PENDING Ready to begin cluster validation
```

### Phase 2: Deploy Middleware and Tekton Resources (45 minutes)
**Status**: ⏳ PENDING
**Dependencies**: Phase 1 complete

**Current Status**: ✅ OpenShift Pipelines operator v1.18.1 already installed and ready

**Deployment Steps**:
- [ ] Deploy updated Quarkus middleware service with pipeline endpoints
- [ ] Verify middleware health: `curl -k .../api/pipeline/health`
- [ ] Test mock endpoints: `curl -k .../api/pipeline/mock/content-creator/create-workshop`
- [ ] Deploy custom Tekton resources using Kustomize: `./deploy_tekton_resources.sh`
- [ ] Verify all agent tasks use middleware endpoints (not direct agent calls)
- [ ] Verify 3 pipelines deployed: workflow-1, workflow-3, workshop-maintenance
- [ ] Test basic Tekton functionality with middleware integration

**Deployment Commands**:
```bash
# Verify OpenShift Pipelines operator
oc get csv -n openshift-operators | grep openshift-pipelines-operator-rh
oc get tektonconfig config

# Deploy our custom Tekton resources
./deploy_tekton_resources.sh

# Alternative: Manual Kustomize deployment
oc apply -k kubernetes/tekton/overlays/workshop-system

# Verify deployment
oc get tasks,pipelines -n workshop-system
```

**Progress Log**:
```
[Date] [Status] [Notes]
2025-06-29 COMPLETE OpenShift Pipelines operator v1.18.1 confirmed installed
2025-06-30 PENDING Updated Quarkus middleware with pipeline integration endpoints
2025-06-30 PENDING Deploy workshop-maintenance-task and pipeline
2025-06-30 PENDING Test middleware endpoint integration
2025-06-30 PENDING Validate 3 pipelines: workflow-1, workflow-3, maintenance
```

---

### Phase 3: Test Middleware Integration (60 minutes)
**Status**: ⏳ PENDING
**Dependencies**: Phase 2 complete

**Objectives**:
- Test Quarkus middleware pipeline integration endpoints
- Validate Tekton → Middleware → Agent communication
- Test mock endpoints for development workflow
- Verify human oversight approval integration

**Testing Steps**:
- [ ] Test Content Creator middleware endpoint
- [ ] Test Template Converter middleware endpoint
- [ ] Test Source Manager middleware endpoint
- [ ] Test Workshop Maintenance endpoint (NEW)
- [ ] Test Human Oversight approval endpoint (NEW)
- [ ] Validate error handling and retry logic
- [ ] Test mock endpoints in dev profile

**Test Commands**:
```bash
# Test middleware endpoints
curl -X POST https://workshop-monitoring-service.../api/pipeline/content-creator/create-workshop \
  -H "Content-Type: application/json" \
  -d '{"workshop_name":"test","repository_url":"https://github.com/example/repo"}'

# Test workshop maintenance (NEW)
curl -X POST https://workshop-monitoring-service.../api/pipeline/source-manager/update-workshop \
  -H "Content-Type: application/json" \
  -d '{"repository_name":"existing-workshop","workshop_name":"test-maintenance"}'

# Test human approval (NEW)
curl -X POST https://workshop-monitoring-service.../api/pipeline/human-oversight/approve-workshop-update \
  -H "Content-Type: application/json" \
  -d '{"approval_id":"test-123","repository_name":"test","approver":"admin","approval_decision":"approved"}'
```

**Progress Log**:
```
[Date] [Status] [Notes]
2025-06-30 PENDING Ready to test middleware integration
```

---

### Phase 4: Test Workshop Creation Pipelines (90 minutes)
**Status**: ⏳ PENDING
**Dependencies**: Phase 3 complete

**Objectives**:
- Test workflow-1-new-workshop pipeline with middleware
- Test workflow-3-enhance-workshop pipeline with middleware
- Validate human oversight approval gates
- Test complete workshop creation and deployment

**Test Scenarios**:
- [ ] **Workflow 1**: Ansible Controller CaC → New Workshop
- [ ] **Workflow 1**: Llama Stack Demos → New Workshop
- [ ] **Workflow 3**: DDD Hexagonal Workshop Enhancement
- [ ] **Human Approval**: Test approval/rejection workflow
- [ ] **Error Handling**: Test failure scenarios and recovery

**Test Commands**:
```bash
# Test Workflow 1 - New Workshop Creation
tkn pipeline start workflow-1-new-workshop \
  --param repository-url="https://github.com/tosin2013/ansible-controller-cac.git" \
  --param workshop-name="ansible-cac-workshop-test" \
  --param auto-approve="false" \
  --workspace name=shared-data,claimName=shared-workspace-storage \
  --namespace="workshop-system"

# Test Workflow 3 - Workshop Enhancement
tkn pipeline start workflow-3-enhance-workshop \
  --param repository-url="https://github.com/jeremyrdavis/dddhexagonalworkshop.git" \
  --param workshop-name="ddd-enhanced-test" \
  --workspace name=shared-data,claimName=shared-workspace-storage \
  --namespace="workshop-system"
```

**Progress Notes**:
```
[Date] [Status] [Notes]
2025-06-30 PENDING Waiting for Phase 3 completion
```

---

### Phase 5: Test Workshop Maintenance Pipeline (NEW - 60 minutes)
**Status**: ⏳ PENDING
**Dependencies**: Phase 4 complete

**Objectives**:
- Test workshop-maintenance-pipeline with human approval
- Validate existing workshop update functionality
- Test content updates with source repository integration
- Verify human-in-the-loop approval workflow

**Test Scenarios**:
- [ ] **Content Update**: Update existing workshop from source repo
- [ ] **Human Approval**: Test maintenance approval workflow
- [ ] **Approval Rejection**: Test rejection and feedback loop
- [ ] **Auto-Approval**: Test maintenance without human oversight
- [ ] **Error Recovery**: Test failure handling in maintenance

**Test Commands**:
```bash
# Test Workshop Maintenance with Human Approval
tkn pipeline start workshop-maintenance-pipeline \
  --param repository-name="workshop-ddd-hexagonal-workshop-demo-1751149405809" \
  --param workshop-name="ddd-maintenance-test" \
  --param update-type="content-update" \
  --param source-repository-url="https://github.com/jeremyrdavis/dddhexagonalworkshop.git" \
  --param require-approval="true" \
  --param approver="workshop-system-operator" \
  --param change-summary="Testing maintenance pipeline with human approval" \
  --workspace name=shared-data,claimName=shared-workspace-storage \
  --namespace="workshop-system"
```

**Progress Notes**:
```
[Date] [Status] [Notes]
2025-06-30 PENDING New maintenance pipeline ready for testing
```

---

### Task 4: Execute Test Iterations and Track Script Improvements
**Status**: ⏳ PENDING  
**Dependencies**: Task 3  
**Estimated Time**: 6-8 hours (iterative)  

**Objectives**:
- ✅ Execute systematic test iterations
- ✅ Track and categorize all failures
- ✅ Implement targeted fixes for each issue
- ✅ Document all script improvements
- ✅ Achieve incremental success rate improvements

**Deliverables**:
- [ ] `test_iteration_tracker.py` - Iteration tracking system
- [ ] Test iteration logs and failure analysis
- [ ] Script improvement documentation

**Progress Notes**:
```
[Date] [Status] [Notes]
2025-06-29 PENDING Waiting for Task 3 completion
```

---

### Task 5: Execute Final End-to-End Validation and Generate Comprehensive Report
**Status**: ⏳ PENDING  
**Dependencies**: Task 4  
**Estimated Time**: 2-3 hours  

**Objectives**:
- ✅ Execute final comprehensive validation
- ✅ Validate 100% success rate for all repositories
- ✅ Generate performance metrics and analysis
- ✅ Document lessons learned and best practices
- ✅ Create comprehensive final report

**Deliverables**:
- [ ] `final_validation_report.py` - Final validation execution
- [ ] `test_results/final_report.json` - Comprehensive report
- [ ] `docs/TEKTON_TESTING_RESULTS.md` - Results documentation

**Progress Notes**:
```
[Date] [Status] [Notes]
2025-06-29 PENDING Waiting for Task 4 completion
```

## 📈 Progress Tracking

### Overall Progress
- **Tasks Completed**: 0/5 (0%)
- **Current Phase**: Environment Setup
- **Next Milestone**: Environment Validation Complete

### Success Rate Tracking

| Test Iteration | Workflow 1 (Ansible) | Workflow 1 (Llama) | Workflow 3 (DDD) | Maintenance | Overall |
|----------------|----------------------|-------------------|------------------|-------------|---------|
| Baseline       | ❌ 0%               | ❌ 0%            | ❌ 0%           | ❌ 0%      | ❌ 0%  |
| Middleware     | - | - | - | - | - |
| Integration    | - | - | - | - | - |
| End-to-End     | - | - | - | - | - |
| **Target**     | ✅ 100%             | ✅ 100%          | ✅ 100%         | ✅ 100%    | ✅ 100% |

### Pipeline Testing Matrix

| Pipeline | Middleware | Human Approval | Gitea Integration | Deployment | Status |
|----------|------------|----------------|-------------------|------------|--------|
| workflow-1-new-workshop | ⏳ | ⏳ | ⏳ | ⏳ | PENDING |
| workflow-3-enhance-workshop | ⏳ | ⏳ | ⏳ | ⏳ | PENDING |
| workshop-maintenance-pipeline | ⏳ | ⏳ | ⏳ | ⏳ | PENDING |

### Issue Tracking

| Issue ID | Category | Description | Status | Resolution |
|----------|----------|-------------|--------|------------|
| - | - | - | - | - |

## 🔧 Infrastructure Requirements

### Prerequisites Checklist
- [ ] OpenShift cluster access with `oc` CLI configured
- [ ] `workshop-system` namespace exists and accessible
- [ ] **Quarkus Middleware Service** deployed with pipeline integration endpoints
- [ ] Tekton pipelines deployed: `workflow-1-new-workshop`, `workflow-3-enhance-workshop`, `workshop-maintenance-pipeline`
- [ ] All Tekton agent tasks updated to use middleware endpoints (not direct agent calls)
- [ ] Workshop maintenance task deployed: `workshop-maintenance-task`
- [ ] All 6 agents running and accessible via A2A protocol
- [ ] Human oversight monitoring service with approval workflow
- [ ] Gitea service available for repository operations and updates
- [ ] Shared workspace storage (RWX) for pipeline coordination

### Environment Configuration
- **OpenShift Namespace**: `workshop-system`
- **Monitoring Service URL**: `https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`
- **Agent Endpoints**: HTTP endpoints for 6 agents
- **Test Repositories**: 3 configured repositories with expected workflows

## 📝 Testing Methodology

### Incremental Testing Approach
1. **Environment Validation** - Verify all prerequisites
2. **Component Testing** - Test individual components
3. **Integration Testing** - Test component interactions
4. **End-to-End Testing** - Complete workflow validation
5. **Performance Testing** - Validate performance metrics

### Failure Analysis Framework
- **Environment Failures**: Connectivity, deployment, configuration
- **Agent Failures**: API errors, timeouts, response parsing
- **Pipeline Failures**: YAML errors, task execution, parameter passing
- **Human Oversight Failures**: API integration, approval gates
- **Integration Failures**: Component interaction issues

### Success Validation
- All repository workflows complete successfully
- All human approval gates function correctly
- Workshop deployment URLs are accessible
- Performance metrics meet acceptable thresholds
- Error handling and recovery mechanisms work properly

## 🚀 OpenShift Implementation Guide

### Step 1: Deploy Updated Middleware Service
```bash
# Build and deploy updated Quarkus service with pipeline endpoints
cd workshop-monitoring-service
mvn clean package -Dquarkus.package.type=uber-jar
oc start-build workshop-monitoring-service --from-file=target/workshop-monitoring-service-1.0.0-SNAPSHOT-runner.jar -n workshop-system

# Wait for deployment
oc rollout status deployment/workshop-monitoring-service -n workshop-system

# Test middleware endpoints
curl -k https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/api/pipeline/health
```

### Step 2: Deploy Workshop Maintenance Components
```bash
# Deploy new maintenance task
oc apply -f kubernetes/tekton/tasks/workshop-maintenance-task.yaml -n workshop-system

# Deploy maintenance pipeline
oc apply -f kubernetes/tekton/pipelines/workshop-maintenance-pipeline.yaml -n workshop-system

# Verify deployment
oc get tasks,pipelines -n workshop-system | grep maintenance
```

### Step 3: Update Existing Tekton Tasks
```bash
# Apply updated agent tasks with middleware endpoints
oc apply -f kubernetes/tekton/tasks/agent-task-content-creator.yaml -n workshop-system
oc apply -f kubernetes/tekton/tasks/agent-task-template-converter.yaml -n workshop-system
oc apply -f kubernetes/tekton/tasks/agent-task-source-manager.yaml -n workshop-system

# Verify tasks are updated
oc get tasks -n workshop-system -o yaml | grep middleware-endpoint
```

### Step 4: Test Implementation
```bash
# Run the test script
./test-workshop-maintenance-pipeline.sh

# Monitor pipeline execution
tkn pipelinerun list -n workshop-system
tkn pipelinerun logs --last -f -n workshop-system
```

---

**Last Updated**: 2025-06-30
**Next Review**: After middleware implementation
**Maintainer**: Workshop Template System Development Team
