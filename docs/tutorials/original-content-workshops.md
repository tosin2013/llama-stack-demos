# Tutorial: Original Content Workshops

Learn how to create workshops from scratch using learning objectives and concepts, without requiring a source repository.

## 🎯 What You'll Learn

By the end of this tutorial, you'll be able to:
- Design workshops from learning objectives
- Create original educational content for any topic
- Generate hands-on exercises and activities
- Build workshops for concepts, tools, or cloud services

## 📋 Prerequisites

- Workshop Template System running (all 6 agents)
- Clear learning objectives for your workshop
- Understanding of your target audience

## 🚀 Step 1: Define Learning Objectives

Let's create a workshop on "Cloud-Native Security Fundamentals" from scratch.

### Learning Objectives Example

```
Workshop: Cloud-Native Security Fundamentals
Target Audience: Intermediate developers and DevOps engineers

Learning Objectives:
- Understand cloud-native security principles and threat models
- Implement container security best practices and scanning
- Configure network policies and micro-segmentation in Kubernetes
- Set up monitoring and alerting for security events
- Apply zero-trust architecture concepts to cloud deployments
```

## 🏗️ Step 2: Design Workshop Structure

Use the Content Creator Agent to design the workshop structure:

```bash
# Design workshop from learning objectives
curl -X POST http://localhost:10080/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "design-001",
    "params": {
      "id": "design-001",
      "sessionId": "original-workshop",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Design a workshop with these learning objectives:\n- Understand cloud-native security fundamentals\n- Implement container security best practices\n- Configure network policies in Kubernetes\n- Set up monitoring and alerting for security events\n- Apply zero-trust architecture concepts"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Expected Workshop Structure

The Content Creator Agent will generate:

```
Cloud-Native Security Workshop Structure:
├── Module 1: Introduction & Security Landscape (20 min)
├── Module 2: Container Security Fundamentals (45 min)
├── Module 3: Kubernetes Network Policies (60 min)
├── Module 4: Security Monitoring & Alerting (45 min)
├── Module 5: Zero-Trust Implementation (60 min)
├── Module 6: Integration & Best Practices (30 min)
└── Module 7: Wrap-up & Next Steps (15 min)

Total Duration: 4.5 hours
Workshop Type: Hybrid (theory + hands-on)
```

## 🎨 Step 3: Create Original Content

Generate detailed content for each module:

```bash
# Create content for Container Security module
curl -X POST http://localhost:10080/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "content-001",
    "params": {
      "id": "content-001",
      "sessionId": "original-workshop",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Create original content for Container Security Fundamentals module covering image scanning, runtime security, and vulnerability management for intermediate audience"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Generated Content Structure

```markdown
# Module 2: Container Security Fundamentals

## Learning Objectives
- Understand container security threat model
- Implement image scanning and vulnerability management
- Configure runtime security controls
- Apply security best practices for container lifecycle

## Core Concepts

### Container Security Threat Model
- Attack surface analysis
- Container escape scenarios
- Supply chain security risks
- Runtime threats and mitigations

### Image Security
- Base image selection and hardening
- Vulnerability scanning integration
- Signed images and content trust
- Registry security and access controls

## Hands-on Exercises
[Detailed exercises will be generated]
```

## 🔬 Step 4: Generate Hands-on Exercises

Create practical exercises for each module:

```bash
# Generate exercises for Network Policies module
curl -X POST http://localhost:10080/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "exercises-001",
    "params": {
      "id": "exercises-001",
      "sessionId": "original-workshop",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Generate hands-on exercises for Kubernetes Network Policies module focusing on micro-segmentation and traffic control for intermediate difficulty"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Exercise Examples

```yaml
# Exercise 1: Basic Network Policy Implementation
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: deny-all-ingress
  namespace: secure-app
spec:
  podSelector: {}
  policyTypes:
  - Ingress

# Exercise 2: Allow Specific Traffic
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-frontend-to-backend
  namespace: secure-app
spec:
  podSelector:
    matchLabels:
      app: backend
  policyTypes:
  - Ingress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: frontend
    ports:
    - protocol: TCP
      port: 8080
```

## 🔍 Step 5: Research and Validate Content

Use the Research & Validation Agent to ensure accuracy:

```bash
# Research current security best practices
curl -X POST http://localhost:10070/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "research-001",
    "params": {
      "id": "research-001",
      "sessionId": "original-workshop",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Research current cloud-native security best practices and validate our workshop content against latest NIST guidelines and industry standards"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Validation Areas

The Research Agent will validate:
- **Current Security Standards**: NIST, CIS benchmarks
- **Tool Versions**: Latest Kubernetes, security tools
- **Best Practices**: Industry-recommended approaches
- **Compliance Requirements**: Regulatory considerations

## 📦 Step 6: Set Up Showroom Template

Create professional workshop layout:

```bash
# Set up Showroom template for security workshop
curl -X POST http://localhost:10080/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "showroom-001",
    "params": {
      "id": "showroom-001",
      "sessionId": "original-workshop",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Clone Showroom template for Cloud-Native Security Fundamentals workshop with Kubernetes focus and standard customization"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Template Customization

```bash
# Clone and customize Showroom template
git clone https://github.com/rhpds/showroom_template_default.git
mv showroom_template_default cloud-security-workshop
cd cloud-security-workshop

# Update configuration
cat > content/antora.yml << EOF
name: cloud-security-workshop
title: Cloud-Native Security Fundamentals
version: '1.0'
nav:
- modules/ROOT/nav.adoc
EOF

# Create navigation structure
cat > content/modules/ROOT/nav.adoc << EOF
* xref:index.adoc[Workshop Overview]
* xref:module-01-introduction.adoc[Security Landscape]
* xref:module-02-container-security.adoc[Container Security]
* xref:module-03-network-policies.adoc[Network Policies]
* xref:module-04-monitoring.adoc[Security Monitoring]
* xref:module-05-zero-trust.adoc[Zero-Trust Architecture]
* xref:module-06-integration.adoc[Best Practices]
EOF
```

## 🔄 Step 7: Configure External Documentation Monitoring

Set up monitoring for security-related documentation:

```bash
# Configure security documentation monitoring
curl -X POST http://localhost:10050/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "monitor-001",
    "params": {
      "id": "monitor-001",
      "sessionId": "original-workshop",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Set up monitoring for security documentation: NIST guidelines, Kubernetes security docs, OWASP container security, and CIS benchmarks"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Security Sources Configuration

```json
{
  "workshop_name": "Cloud-Native Security Fundamentals",
  "external_sources": {
    "documentation_sites": [
      "https://kubernetes.io/docs/concepts/security/",
      "https://owasp.org/www-project-container-security/",
      "https://www.cisecurity.org/benchmark/kubernetes"
    ],
    "pdf_documents": [
      "https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-190.pdf",
      "https://media.defense.gov/2022/Aug/29/2003066362/-1/-1/0/CTR_KUBERNETES_HARDENING_GUIDANCE_1.2_20220829.PDF"
    ],
    "api_endpoints": [
      "https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.28/"
    ]
  },
  "monitoring_settings": {
    "frequency": "weekly",
    "change_threshold": 0.15,
    "rag_integration": true
  }
}
```

## 🚀 Step 8: Deploy and Test

Deploy the workshop and test all components:

```bash
# Deploy workshop
curl -X POST http://localhost:10060/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "deploy-001",
    "params": {
      "id": "deploy-001",
      "sessionId": "original-workshop",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Deploy cloud-security-workshop to Showroom platform with staging environment and security lab setup"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Test Workshop Chat Agent

```bash
# Test security-related questions
curl -X POST http://localhost:10040/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "chat-001",
    "params": {
      "id": "chat-001",
      "sessionId": "participant-session",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "What are the key differences between runtime security and image security in containers?"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

## ✅ What You've Accomplished

You've successfully created an original workshop:
- ✅ Designed workshop structure from learning objectives
- ✅ Generated comprehensive educational content
- ✅ Created hands-on exercises and practical activities
- ✅ Validated content against current security standards
- ✅ Set up professional Showroom template
- ✅ Configured security documentation monitoring
- ✅ Deployed workshop with participant assistance

## 🎯 Workshop Content Examples

### Module Structure
```
Module 3: Kubernetes Network Policies (60 minutes)

├── Concepts (15 min)
│   ├── Network Policy fundamentals
│   ├── Default deny vs. allow patterns
│   └── Ingress and egress rules
├── Hands-on Lab (35 min)
│   ├── Exercise 1: Implement default deny
│   ├── Exercise 2: Allow specific traffic
│   └── Exercise 3: Multi-tier application security
├── Discussion (10 min)
│   ├── Common pitfalls and solutions
│   └── Real-world scenarios
```

### Assessment Criteria
- **Understanding**: Can explain network policy concepts
- **Implementation**: Successfully creates working policies
- **Troubleshooting**: Can debug policy issues
- **Best Practices**: Applies security principles correctly

## 🔄 Next Steps

1. **Content Refinement**: Review and enhance generated content
2. **Lab Environment**: Set up hands-on lab infrastructure
3. **Pilot Testing**: Run workshop with test participants
4. **Feedback Integration**: Incorporate participant feedback
5. **Continuous Updates**: Monitor external documentation changes

## 📚 Related Documentation

- [How to Create Original Workshop Content](../how-to/create-original-content.md)
- [How to Generate Effective Exercises](../how-to/generate-exercises.md)
- [Content Creator Agent Reference](../reference/content-creator-agent.md)
- [Workshop Quality Guidelines](../reference/quality-guidelines.md)

---

*This tutorial demonstrates creating workshops from scratch using learning objectives and the Content Creator Agent's capabilities.*
