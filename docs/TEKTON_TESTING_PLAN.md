# Tekton-Agent Integration Testing Plan - OpenShift Cluster Validation

**Document**: TEKTON_TESTING_PLAN.md
**Created**: 2025-06-29
**Purpose**: Test test_e2e_tekton_human_oversight.py against live OpenShift cluster
**Target**: 100% success rate for all three repository workflows on real infrastructure
**Cluster**: apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com
**ADR Reference**: ADR-0006 Tekton-Agent Integration Architecture

## 🎯 Live Cluster Testing Objectives

Test the complete Tekton-Agent integration against our actual OpenShift cluster to validate:
- Real Tekton pipeline execution with live agents
- Actual human oversight API integration
- Live Gitea repository creation and management
- Real BuildConfig triggering and workshop deployment
- End-to-end functionality with production-like environment

## �️ Current Cluster Infrastructure

### OpenShift Cluster Details
- **Cluster URL**: `https://api.cluster-9cfzr.9cfzr.sandbox180.opentlc.com:6443`
- **Console URL**: `https://console-openshift-console.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`
- **Namespace**: `workshop-system`
- **Tekton**: OpenShift Pipelines operator installed

### Deployed Services
- **Human Oversight Service**: `https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`
- **6 Agent Services**: Running in workshop-system namespace
- **Gitea Service**: Available for repository operations
- **BuildConfig Infrastructure**: Ready for workshop deployment

### 📊 Test Repositories

| Repository | Expected Workflow | Description |
|------------|------------------|-------------|
| **DDD Hexagonal Workshop** | Workflow 3 | Existing workshop enhancement |
| **Ansible Controller CaC** | Workflow 1 | Application → New workshop |
| **Llama Stack Demos** | Workflow 1 | Tutorial → New workshop |

## � Test Execution Plan

### Phase 1: Environment Validation (30 minutes)
**Status**: ⏳ PENDING

**Pre-Test Checklist**:
- [ ] Verify OpenShift cluster access: `oc whoami`
- [ ] Check workshop-system namespace: `oc project workshop-system`
- [ ] Validate agent deployments: `oc get pods | grep agent`
- [ ] Test human oversight service: `curl -k https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/health`
- [ ] Check Tekton installation: `oc get pipelines,tasks -n workshop-system`

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

### Phase 2: Deploy Tekton Pipelines and Tasks (45 minutes)
**Status**: ⏳ PENDING
**Dependencies**: Phase 1 complete

**Deployment Steps**:
- [ ] Deploy all 6 agent tasks: `oc apply -f kubernetes/tekton/tasks/`
- [ ] Deploy human oversight approval task
- [ ] Deploy BuildConfig trigger task
- [ ] Deploy Workflow 1 pipeline: `oc apply -f kubernetes/tekton/pipelines/workflow-1-new-workshop.yaml`
- [ ] Deploy Workflow 3 pipeline: `oc apply -f kubernetes/tekton/pipelines/workflow-3-enhance-workshop.yaml`
- [ ] Verify all resources deployed successfully

**Deployment Commands**:
```bash
# Deploy Tekton tasks
echo "🚀 Deploying Tekton Agent Tasks..."
oc apply -f kubernetes/tekton/tasks/ -n workshop-system

# Deploy pipelines
echo "🔄 Deploying Tekton Pipelines..."
oc apply -f kubernetes/tekton/pipelines/ -n workshop-system

# Verify deployment
echo "✅ Verifying Deployment..."
oc get tasks,pipelines -n workshop-system
```

**Progress Log**:
```
[Date] [Status] [Notes]
2025-06-29 PENDING Waiting for Phase 1 completion
```

---

### Task 3: Implement Incremental Testing Framework with Pytest Integration
**Status**: ⏳ PENDING  
**Dependencies**: Task 2  
**Estimated Time**: 4-5 hours  

**Objectives**:
- ✅ Create pytest-based incremental testing
- ✅ Test Level 1: Repository analysis only
- ✅ Test Level 2: Pipeline YAML generation
- ✅ Test Level 3: Single agent task execution
- ✅ Test Level 4: Human oversight integration
- ✅ Test Level 5: Complete pipeline execution

**Deliverables**:
- [ ] `test_tekton_incremental.py` - Incremental test framework
- [ ] Pytest fixtures and markers
- [ ] Test execution reports

**Progress Notes**:
```
[Date] [Status] [Notes]
2025-06-29 PENDING Waiting for Task 2 completion
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

| Test Iteration | DDD Hexagonal | Ansible CaC | Llama Stack | Overall |
|----------------|---------------|-------------|-------------|---------|
| Baseline       | ❌ 0%        | ❌ 0%      | ❌ 0%      | ❌ 0%  |
| Iteration 1    | - | - | - | - |
| Iteration 2    | - | - | - | - |
| Iteration 3    | - | - | - | - |
| **Target**     | ✅ 100%      | ✅ 100%    | ✅ 100%    | ✅ 100% |

### Issue Tracking

| Issue ID | Category | Description | Status | Resolution |
|----------|----------|-------------|--------|------------|
| - | - | - | - | - |

## 🔧 Infrastructure Requirements

### Prerequisites Checklist
- [ ] OpenShift cluster access with `oc` CLI configured
- [ ] `workshop-system` namespace exists and accessible
- [ ] Tekton pipelines deployed (`workflow-1-new-workshop`, `workflow-3-enhance-workshop`)
- [ ] All 8 Tekton agent tasks deployed and available
- [ ] All 6 agents running and accessible
- [ ] Human oversight monitoring service deployed and functional
- [ ] Gitea service available for repository operations

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

---

**Last Updated**: 2025-06-29  
**Next Review**: After each task completion  
**Maintainer**: Workshop Template System Development Team
