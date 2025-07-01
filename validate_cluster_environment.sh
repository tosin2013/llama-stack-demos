#!/bin/bash

# Tekton-Agent Integration - Cluster Environment Validation
# Script: validate_cluster_environment.sh
# Purpose: Validate OpenShift cluster environment before testing
# Reference: docs/TEKTON_TESTING_PLAN.md - Phase 1

set -e

echo "🔍 Tekton-Agent Integration - Cluster Environment Validation"
echo "============================================================"
echo "Cluster: apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
echo "Namespace: workshop-system"
echo "Date: $(date)"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check command success
check_status() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
        return 0
    else
        echo -e "${RED}❌ $1${NC}"
        return 1
    fi
}

# Function to check optional command
check_optional() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
    else
        echo -e "${YELLOW}⚠️ $1 (optional)${NC}"
    fi
}

echo "📋 Phase 1: Environment Validation Checklist"
echo "============================================="

# 1. Check OpenShift CLI access
echo ""
echo -e "${BLUE}1. Checking OpenShift CLI access...${NC}"
oc whoami > /dev/null 2>&1
check_status "OpenShift CLI authenticated"

# Get current user and cluster info
CURRENT_USER=$(oc whoami 2>/dev/null || echo "unknown")
CURRENT_SERVER=$(oc whoami --show-server 2>/dev/null || echo "unknown")
echo "   User: $CURRENT_USER"
echo "   Server: $CURRENT_SERVER"

# 2. Check workshop-system namespace access
echo ""
echo -e "${BLUE}2. Checking workshop-system namespace access...${NC}"
oc project workshop-system > /dev/null 2>&1
check_status "workshop-system namespace accessible"

# 3. Check agent deployments
echo ""
echo -e "${BLUE}3. Checking agent deployments...${NC}"
echo "   Agent pods status:"
oc get pods -l app=workshop-template-system --no-headers 2>/dev/null | while read line; do
    if [ -n "$line" ]; then
        POD_NAME=$(echo $line | awk '{print $1}')
        POD_STATUS=$(echo $line | awk '{print $3}')
        if [ "$POD_STATUS" = "Running" ]; then
            echo -e "   ${GREEN}✅ $POD_NAME: $POD_STATUS${NC}"
        else
            echo -e "   ${RED}❌ $POD_NAME: $POD_STATUS${NC}"
        fi
    fi
done

AGENT_COUNT=$(oc get pods -l app=workshop-template-system --no-headers 2>/dev/null | wc -l)
echo "   Total agent pods found: $AGENT_COUNT"

# 4. Check human oversight service
echo ""
echo -e "${BLUE}4. Checking human oversight service...${NC}"
MONITORING_URL="https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"

# Check if service exists
oc get service workshop-monitoring-service > /dev/null 2>&1
check_optional "Human oversight service exists"

# Check if route exists
oc get route workshop-monitoring-service > /dev/null 2>&1
check_optional "Human oversight route exists"

# Test service accessibility
echo "   Testing service accessibility..."
HTTP_STATUS=$(curl -k -s -o /dev/null -w "%{http_code}" "$MONITORING_URL/health" 2>/dev/null || echo "000")
if [ "$HTTP_STATUS" = "200" ]; then
    echo -e "   ${GREEN}✅ Human oversight service accessible (HTTP $HTTP_STATUS)${NC}"
elif [ "$HTTP_STATUS" = "000" ]; then
    echo -e "   ${YELLOW}⚠️ Human oversight service not accessible (connection failed)${NC}"
else
    echo -e "   ${YELLOW}⚠️ Human oversight service returned HTTP $HTTP_STATUS${NC}"
fi

# 5. Check Tekton installation
echo ""
echo -e "${BLUE}5. Checking Tekton installation...${NC}"

# Check if Tekton is installed
oc get crd pipelines.tekton.dev > /dev/null 2>&1
check_status "Tekton CRDs installed"

# Check existing pipelines and tasks
PIPELINE_COUNT=$(oc get pipelines --no-headers 2>/dev/null | wc -l)
TASK_COUNT=$(oc get tasks --no-headers 2>/dev/null | wc -l)

echo "   Current Tekton resources:"
echo "   - Pipelines: $PIPELINE_COUNT"
echo "   - Tasks: $TASK_COUNT"

if [ $PIPELINE_COUNT -gt 0 ]; then
    echo "   Existing pipelines:"
    oc get pipelines --no-headers 2>/dev/null | awk '{print "     - " $1}' || true
fi

if [ $TASK_COUNT -gt 0 ]; then
    echo "   Existing tasks:"
    oc get tasks --no-headers 2>/dev/null | head -10 | awk '{print "     - " $1}' || true
    if [ $TASK_COUNT -gt 10 ]; then
        echo "     ... and $((TASK_COUNT - 10)) more"
    fi
fi

# 6. Check required Tekton pipelines and tasks
echo ""
echo -e "${BLUE}6. Checking required Tekton resources...${NC}"

# Check for our specific pipelines
REQUIRED_PIPELINES=("workflow-1-new-workshop" "workflow-3-enhance-workshop")
for pipeline in "${REQUIRED_PIPELINES[@]}"; do
    oc get pipeline "$pipeline" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo -e "   ${GREEN}✅ Pipeline $pipeline exists${NC}"
    else
        echo -e "   ${RED}❌ Pipeline $pipeline missing${NC}"
    fi
done

# Check for our specific tasks
REQUIRED_TASKS=("agent-task-template-converter" "agent-task-content-creator" "agent-task-source-manager" "agent-task-research-validation" "agent-task-documentation-pipeline" "agent-task-workshop-chat" "human-oversight-approval" "openshift-buildconfig-trigger")
MISSING_TASKS=0
for task in "${REQUIRED_TASKS[@]}"; do
    oc get task "$task" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo -e "   ${GREEN}✅ Task $task exists${NC}"
    else
        echo -e "   ${RED}❌ Task $task missing${NC}"
        MISSING_TASKS=$((MISSING_TASKS + 1))
    fi
done

# 7. Summary and recommendations
echo ""
echo "📊 Environment Validation Summary"
echo "================================="

# Calculate readiness score
TOTAL_CHECKS=6
CRITICAL_ISSUES=0

# Check critical requirements
oc whoami > /dev/null 2>&1 || CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
oc project workshop-system > /dev/null 2>&1 || CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
oc get crd pipelines.tekton.dev > /dev/null 2>&1 || CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))

if [ $CRITICAL_ISSUES -eq 0 ]; then
    echo -e "${GREEN}🎉 Environment validation PASSED${NC}"
    echo ""
    echo "✅ OpenShift cluster access: OK"
    echo "✅ workshop-system namespace: OK"
    echo "✅ Tekton installation: OK"
    echo "📊 Agent pods found: $AGENT_COUNT"
    echo "🔧 Tekton resources: $PIPELINE_COUNT pipelines, $TASK_COUNT tasks"
    
    if [ $MISSING_TASKS -gt 0 ]; then
        echo ""
        echo -e "${YELLOW}⚠️ Next step: Deploy missing Tekton resources${NC}"
        echo "   Run: oc apply -f kubernetes/tekton/tasks/ -n workshop-system"
        echo "   Run: oc apply -f kubernetes/tekton/pipelines/ -n workshop-system"
    else
        echo ""
        echo -e "${GREEN}🚀 Ready to proceed with testing!${NC}"
        echo "   Next step: Run test_e2e_tekton_human_oversight.py"
    fi
else
    echo -e "${RED}❌ Environment validation FAILED${NC}"
    echo ""
    echo "Critical issues found:"
    oc whoami > /dev/null 2>&1 || echo "   - OpenShift CLI not authenticated"
    oc project workshop-system > /dev/null 2>&1 || echo "   - workshop-system namespace not accessible"
    oc get crd pipelines.tekton.dev > /dev/null 2>&1 || echo "   - Tekton not installed"
    echo ""
    echo "Please resolve these issues before proceeding with testing."
fi

echo ""
echo "📋 For detailed testing plan, see: docs/TEKTON_TESTING_PLAN.md"
echo "🕒 Validation completed at: $(date)"
