# Workshop Template System Troubleshooting Guide

**Last Updated**: June 28, 2025  
**Version**: 1.0

## 🚨 Critical Issues and Solutions

### Agent CrashLoopBackOff with Module Import Errors

**Symptoms**:
- Agents stuck in CrashLoopBackOff status
- Logs show: `/opt/app-root/bin/python: No module named demos.workshop_template_system`
- Some agents work while others fail with identical container images

**Root Cause**: 
Volume mount configuration overwrites container's built-in Python modules.

**Solution**:
```yaml
# CORRECT volume configuration (config-only):
volumeMounts:
- name: config-volume
  mountPath: /opt/app-root/src/config
volumes:
- name: config-volume
  configMap:
    name: workshop-system-config

# INCORRECT (causes module import failures):
volumeMounts:
- name: workshop-code
  mountPath: /opt/app-root/src/demos  # ❌ Overwrites container modules
```

**Fix Steps**:
1. Remove `workshop-code` volume mounts from agent deployments
2. Keep only `config-volume` mounted at `/opt/app-root/src/config`
3. Apply with Kustomize: `oc apply -k kubernetes/workshop-template-system/base/`

### ConfigMap Hash Suffix Issues

**Symptoms**:
- ConfigMap references not found
- Pods fail to start due to missing ConfigMaps

**Root Cause**: 
Kustomize generates hash suffixes for ConfigMaps that must be properly referenced.

**Solution**:
- Always use `oc apply -k` instead of `oc apply -f`
- Reference base ConfigMap names in YAML; Kustomize handles hash mapping
- Verify ConfigMap exists: `oc get configmap -n workshop-system`

### Container Build Failures

**Symptoms**:
- BuildConfig pods in Error or Init:Error status
- Module structure missing in containers

**Solution**:
1. Check Gitea repository accessibility
2. Verify webhook secret configuration
3. Trigger new build: `oc start-build workshop-agent-system-build -n workshop-system`
4. Monitor build logs: `oc logs -f bc/workshop-agent-system-build -n workshop-system`

## 🔍 Diagnostic Commands

### Check Agent Status
```bash
# All workshop agents
oc get pods -n workshop-system -l component=workshop-agent

# Specific agent logs
oc logs <pod-name> -n workshop-system

# Agent health check
oc port-forward svc/<agent-name> 8080:80 -n workshop-system
curl http://localhost:8080/agent-card
```

### Verify Configuration
```bash
# Check ConfigMaps
oc get configmap -n workshop-system

# Check volume mounts
oc describe pod <pod-name> -n workshop-system | grep -A 10 "Mounts:"

# Check container module structure
oc exec <pod-name> -n workshop-system -- ls -la /opt/app-root/src/demos/
```

### Network and Service Discovery
```bash
# Check services
oc get svc -n workshop-system

# Test service connectivity
oc exec <pod-name> -n workshop-system -- curl http://<service-name>/agent-card
```

## 📋 Common Deployment Patterns

### Successful Agent Configuration
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: example-agent
spec:
  template:
    spec:
      containers:
      - name: agent
        image: workshop-agent-system:latest
        command: ["python", "-m", "demos.workshop_template_system", "--agent-name", "agent_name", "--port", "8080"]
        volumeMounts:
        - name: config-volume
          mountPath: /opt/app-root/src/config
        livenessProbe:
          httpGet:
            path: /agent-card
            port: 8080
          initialDelaySeconds: 30
        readinessProbe:
          httpGet:
            path: /agent-card
            port: 8080
          initialDelaySeconds: 10
      volumes:
      - name: config-volume
        configMap:
          name: workshop-system-config
```

### Health Check Validation
```bash
# Verify all agents are healthy
for agent in workshop-chat template-converter content-creator source-manager research-validation documentation-pipeline; do
  echo "Testing $agent..."
  oc port-forward svc/${agent}-agent 8080:80 -n workshop-system &
  sleep 2
  curl -s http://localhost:8080/agent-card | jq '.name' || echo "FAILED"
  pkill -f "port-forward.*${agent}"
done
```

## 🛠️ Maintenance Procedures

### Clean Up Failed Builds
```bash
# Remove failed build pods
oc delete pods -n workshop-system -l openshift.io/build.name --field-selector=status.phase=Failed

# Remove error build pods  
oc delete pods -n workshop-system -l openshift.io/build.name --field-selector=status.phase=Error
```

### Agent Restart Procedure
```bash
# Restart specific agent
oc rollout restart deployment/<agent-name> -n workshop-system

# Restart all agents
oc rollout restart deployment -l component=workshop-agent -n workshop-system
```

### Configuration Updates
```bash
# Update ConfigMap and trigger restart
oc apply -k kubernetes/workshop-template-system/base/
oc rollout restart deployment -l component=workshop-agent -n workshop-system
```

## 🌐 External Agent Access

### Working HTTPS Routes
All agents are accessible via HTTPS with proper TLS termination:

- **Workshop Chat**: https://workshop-chat-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com
- **Template Converter**: https://template-converter-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com
- **Content Creator**: https://content-creator-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com
- **Source Manager**: https://source-manager-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com
- **Research Validation**: https://research-validation-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com
- **Documentation Pipeline**: https://documentation-pipeline-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com

### Route Configuration Best Practices
```bash
# Create routes with auto-generated hostnames
oc expose svc <service-name> -n workshop-system

# Add HTTPS termination
oc patch route <route-name> -n workshop-system -p '{"spec":{"tls":{"termination":"edge","insecureEdgeTerminationPolicy":"Redirect"}}}'

# Verify route accessibility
curl -k https://<route-hostname>/agent-card
```

## 📞 Support Information

- **GitHub Issues**: Report bugs in the llama-stack-demos repository
- **Documentation**: Comprehensive guides in docs/ directory
- **Research Reports**: Latest findings in docs/research/
- **Configuration Reference**: docs/reference/configuration.md

---

*This troubleshooting guide is based on real deployment experiences and lessons learned from the Workshop Template System implementation.*
