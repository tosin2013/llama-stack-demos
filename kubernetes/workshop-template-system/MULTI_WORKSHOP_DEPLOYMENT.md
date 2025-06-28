# Multiple Workshop Deployment Guide

This guide explains how to deploy multiple workshop instances to the same OpenShift cluster using different strategies.

## 🎯 **Deployment Strategies**

### **Strategy 1: Shared Infrastructure (Recommended)**
- **One shared infrastructure** (Llama Stack, Milvus, Agents, Gitea)
- **Multiple workshop deployments** in the same namespace
- **Cost effective** and **resource efficient**

### **Strategy 2: Separate Namespaces**
- **Complete isolation** per workshop
- **Independent scaling** and management
- **Higher resource usage** but **maximum isolation**

### **Strategy 3: Hybrid Approach**
- **Shared core services** (Llama Stack, Milvus, Gitea)
- **Separate workshop namespaces** for content and chat agents
- **Balance of efficiency and isolation**

## 🚀 **Strategy 1: Shared Infrastructure (Recommended)**

### **Architecture**
```
workshop-system namespace:
├── Shared Infrastructure
│   ├── Llama Stack Server (shared by all workshops)
│   ├── Milvus RAG System (shared vector database)
│   ├── 6-Agent System (manages all workshops)
│   └── Gitea Server (hosts all workshop repositories)
└── Multiple Workshops
    ├── healthcare-ml-workshop (pods + route)
    ├── spring-boot-workshop (pods + route)
    ├── k8s-security-workshop (pods + route)
    └── ... (more workshops)
```

### **Deployment Process**

#### **1. Deploy First Workshop (Infrastructure + Workshop)**
```bash
# Deploy infrastructure and first workshop
cp -r overlays/healthcare-ml-example overlays/my-healthcare-ml
# Customize overlays/my-healthcare-ml/kustomization.yaml
./deploy.sh my-healthcare-ml
```

#### **2. Add Additional Workshops**
```bash
# Add Spring Boot workshop
cp -r overlays/custom-repo-template overlays/spring-boot-workshop
```

Edit `overlays/spring-boot-workshop/kustomization.yaml`:
```yaml
# Same infrastructure, different workshop
namespace: workshop-system  # Same namespace

configMapGenerator:
  - name: spring-boot-workshop-config  # Different config name
    literals:
      - source_repository=https://github.com/mycompany/spring-boot-microservices.git
      - workshop_name=Spring Boot Microservices Workshop
      - workshop_subdomain=spring-microservices
      - gitea_repository=workshop-system/spring-boot-workshop.git

# Different labels to distinguish workshops
commonLabels:
  workshop-instance: spring-boot-workshop
```

```bash
# Deploy additional workshop
./deploy.sh spring-boot-workshop
```

#### **3. Add More Workshops**
```bash
# Kubernetes Security workshop
cp -r overlays/custom-repo-template overlays/k8s-security-workshop
# Customize for Kubernetes security content
./deploy.sh k8s-security-workshop

# DevOps workshop
cp -r overlays/custom-repo-template overlays/devops-workshop
# Customize for DevOps content
./deploy.sh devops-workshop
```

### **Benefits of Shared Infrastructure**
- ✅ **Resource efficient** - One Llama Stack serves all workshops
- ✅ **Cost effective** - Shared Milvus and agent infrastructure
- ✅ **Centralized management** - One Gitea for all repositories
- ✅ **Consistent experience** - Same AI quality across workshops
- ✅ **Easy scaling** - Scale infrastructure once for all workshops

### **Workshop URLs**
```
https://healthcare-ml.apps.your-cluster.local
https://spring-microservices.apps.your-cluster.local
https://k8s-security.apps.your-cluster.local
https://devops-pipeline.apps.your-cluster.local
```

## 🏗️ **Strategy 2: Separate Namespaces**

### **Architecture**
```
OpenShift Cluster:
├── workshop-system (shared core)
│   ├── Gitea Server (shared)
│   └── Shared utilities
├── healthcare-ml-workshop-ns
│   ├── Llama Stack Server
│   ├── Milvus RAG System
│   ├── 6-Agent System
│   └── Healthcare ML Workshop
├── spring-boot-workshop-ns
│   ├── Llama Stack Server
│   ├── Milvus RAG System
│   ├── 6-Agent System
│   └── Spring Boot Workshop
└── k8s-security-workshop-ns
    ├── Llama Stack Server
    ├── Milvus RAG System
    ├── 6-Agent System
    └── Kubernetes Security Workshop
```

### **Deployment Process**

#### **1. Deploy First Workshop**
```bash
cp -r overlays/healthcare-ml-example overlays/healthcare-ml-isolated
```

Edit `overlays/healthcare-ml-isolated/kustomization.yaml`:
```yaml
# Separate namespace for complete isolation
namespace: healthcare-ml-workshop

# All resources in this namespace
configMapGenerator:
  - name: workshop-system-config
    literals:
      - gitea_url=https://gitea.workshop-system.svc.cluster.local  # Cross-namespace
      - workshop_domain=apps.your-cluster.local
      - workshop_subdomain=healthcare-ml
```

```bash
./deploy.sh healthcare-ml-isolated
```

#### **2. Deploy Additional Isolated Workshops**
```bash
# Spring Boot workshop in separate namespace
cp -r overlays/custom-repo-template overlays/spring-boot-isolated
# Edit namespace: spring-boot-workshop
./deploy.sh spring-boot-isolated

# Kubernetes Security workshop in separate namespace  
cp -r overlays/custom-repo-template overlays/k8s-security-isolated
# Edit namespace: k8s-security-workshop
./deploy.sh k8s-security-isolated
```

### **Benefits of Separate Namespaces**
- ✅ **Complete isolation** - No resource sharing between workshops
- ✅ **Independent scaling** - Scale each workshop independently
- ✅ **Security isolation** - Network policies can isolate workshops
- ✅ **Independent updates** - Update one workshop without affecting others
- ✅ **Resource quotas** - Set different limits per workshop

### **Considerations**
- ❌ **Higher resource usage** - Multiple Llama Stack instances
- ❌ **Higher costs** - More infrastructure per workshop
- ❌ **More complex management** - Multiple instances to maintain

## 🔄 **Strategy 3: Hybrid Approach**

### **Architecture**
```
OpenShift Cluster:
├── workshop-core-system (shared infrastructure)
│   ├── Llama Stack Server (shared)
│   ├── Milvus RAG System (shared)
│   ├── Template Converter Agent (shared)
│   ├── Content Creator Agent (shared)
│   ├── Source Manager Agent (shared)
│   ├── Research Validation Agent (shared)
│   ├── Documentation Pipeline Agent (shared)
│   └── Gitea Server (shared)
├── healthcare-ml-workshop
│   ├── Workshop Chat Agent (dedicated)
│   └── Healthcare ML Workshop (dedicated)
├── spring-boot-workshop
│   ├── Workshop Chat Agent (dedicated)
│   └── Spring Boot Workshop (dedicated)
└── k8s-security-workshop
    ├── Workshop Chat Agent (dedicated)
    └── Kubernetes Security Workshop (dedicated)
```

### **Benefits of Hybrid Approach**
- ✅ **Efficient core infrastructure** - Shared expensive components
- ✅ **Isolated workshop experience** - Dedicated chat agents per workshop
- ✅ **Independent workshop scaling** - Scale chat and content separately
- ✅ **Balanced resource usage** - Optimal cost vs isolation

## 📊 **Comparison Matrix**

| Aspect | Shared Infrastructure | Separate Namespaces | Hybrid Approach |
|--------|----------------------|---------------------|-----------------|
| **Resource Usage** | ⭐⭐⭐ Low | ⭐ High | ⭐⭐ Medium |
| **Cost** | ⭐⭐⭐ Low | ⭐ High | ⭐⭐ Medium |
| **Isolation** | ⭐ Low | ⭐⭐⭐ High | ⭐⭐ Medium |
| **Management** | ⭐⭐⭐ Simple | ⭐ Complex | ⭐⭐ Medium |
| **Scaling** | ⭐⭐ Limited | ⭐⭐⭐ Flexible | ⭐⭐⭐ Flexible |
| **Security** | ⭐ Shared | ⭐⭐⭐ Isolated | ⭐⭐ Balanced |

## 🎯 **Recommendations**

### **For Most Use Cases: Shared Infrastructure**
- **Small to medium deployments** (2-10 workshops)
- **Cost-conscious environments**
- **Similar workshop audiences**
- **Centralized management preferred**

### **For Enterprise: Separate Namespaces**
- **Large deployments** (10+ workshops)
- **Different customer/tenant isolation required**
- **Compliance requirements for data isolation**
- **Independent SLA requirements**

### **For Balanced Approach: Hybrid**
- **Medium to large deployments** (5-20 workshops)
- **Need workshop-specific customization**
- **Balance of cost and isolation**
- **Different workshop performance requirements**

## 🚀 **Quick Commands**

### **Deploy Multiple Workshops (Shared)**
```bash
# First workshop (creates infrastructure)
./deploy.sh healthcare-ml-example

# Additional workshops (reuse infrastructure)
cp -r overlays/custom-repo-template overlays/workshop-2
# Customize overlays/workshop-2/kustomization.yaml
./deploy.sh workshop-2

cp -r overlays/custom-repo-template overlays/workshop-3
# Customize overlays/workshop-3/kustomization.yaml
./deploy.sh workshop-3
```

### **Check All Workshops**
```bash
# List all workshops
oc get routes -n workshop-system | grep workshop

# Check workshop pods
oc get pods -n workshop-system -l component=workshop

# Check agent status
oc get pods -n workshop-system -l component=workshop-agent
```

### **Scale Individual Workshops**
```bash
# Scale specific workshop
oc scale deployment healthcare-ml-workshop --replicas=3 -n workshop-system

# Scale chat agents for specific workshop
oc scale deployment workshop-chat-agent --replicas=5 -n workshop-system
```

## 📚 **Summary**

✅ **Yes, you can deploy multiple workshop instances** to the same OpenShift cluster
✅ **Three deployment strategies** available based on your needs
✅ **Shared infrastructure recommended** for most use cases
✅ **Complete isolation available** when needed
✅ **Flexible scaling and management** options

The Kustomize overlay structure makes it easy to deploy and manage multiple workshops while sharing infrastructure efficiently!
