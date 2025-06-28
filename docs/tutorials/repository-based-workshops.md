# Tutorial: Repository-Based Workshops

Learn how to convert GitHub repositories into interactive workshops using the Workshop Template System.

## 🎯 What You'll Learn

By the end of this tutorial, you'll be able to:
- Analyze any GitHub repository for workshop potential
- Convert applications into educational workshops
- Enhance existing workshops with modern features
- Deploy workshops to RHPDS/Showroom platforms

## 📋 Prerequisites

- Workshop Template System running (all 6 agents)
- Access to GitHub repositories
- Basic understanding of workshop concepts

## 🚀 Step 1: Analyze a Repository

Let's start by analyzing the Healthcare ML Genetic Predictor repository.

### Using the Template Converter Agent

```bash
# Send analysis request to Template Converter Agent
curl -X POST http://localhost:10041/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "analysis-001",
    "params": {
      "id": "analysis-001",
      "sessionId": "tutorial-session",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Analyze https://github.com/tosin2013/healthcare-ml-genetic-predictor.git for workshop conversion potential"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Expected Response

The Template Converter Agent will detect:
- **Repository Type**: Application (not existing workshop)
- **Technologies**: Quarkus, Kafka, OpenShift, Machine Learning
- **Workshop Potential**: High - complex application with multiple learning opportunities
- **Recommended Approach**: Convert using Showroom template

## 🏗️ Step 2: Generate Workshop Structure

Based on the analysis, create a workshop structure:

```bash
# Request workshop structure generation
curl -X POST http://localhost:10041/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "structure-001",
    "params": {
      "id": "structure-001", 
      "sessionId": "tutorial-session",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Generate workshop structure for Healthcare ML application focusing on Quarkus WebSockets, Kafka streaming, and ML inference deployment on OpenShift"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Generated Structure

The system will create a structure like:

```
Healthcare ML Workshop/
├── Module 1: Introduction to Healthcare ML
├── Module 2: Environment Setup (OpenShift + Tools)
├── Module 3: Quarkus WebSockets for Real-time Data
├── Module 4: Kafka Event Streaming
├── Module 5: ML Model Inference and Deployment
├── Module 6: Monitoring and Scaling
└── Module 7: Troubleshooting and Best Practices
```

## 🎨 Step 3: Create Workshop Content

Use the Content Creator Agent to generate detailed content:

```bash
# Generate content for Module 3
curl -X POST http://localhost:10080/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "content-001",
    "params": {
      "id": "content-001",
      "sessionId": "tutorial-session", 
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Create original content for Quarkus WebSockets module in Healthcare ML workshop, including hands-on exercises for real-time genetic data processing"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

## 🔍 Step 4: Validate Content Accuracy

Use the Research & Validation Agent to ensure technical accuracy:

```bash
# Validate Quarkus WebSocket content
curl -X POST http://localhost:10070/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "validate-001",
    "params": {
      "id": "validate-001",
      "sessionId": "tutorial-session",
      "message": {
        "role": "user", 
        "parts": [{
          "type": "text",
          "text": "Research current Quarkus WebSocket best practices and validate our workshop content for accuracy with latest documentation"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

## 📦 Step 5: Set Up Showroom Template

Clone and customize the official Showroom template:

```bash
# Use Content Creator Agent to set up Showroom template
curl -X POST http://localhost:10080/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "showroom-001",
    "params": {
      "id": "showroom-001",
      "sessionId": "tutorial-session",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text", 
          "text": "Clone Showroom template for Healthcare ML Genetic Risk Prediction workshop with OpenShift focus and extensive customization"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### Manual Template Setup

```bash
# Clone the official Showroom template
git clone https://github.com/rhpds/showroom_template_default.git
mv showroom_template_default healthcare-ml-workshop
cd healthcare-ml-workshop

# Remove original git history
rm -rf .git
git init
git add .
git commit -m "Initial commit: Healthcare ML workshop from Showroom template"
```

## 🔄 Step 6: Set Up External Documentation Monitoring

Configure monitoring for external documentation sources:

```bash
# Configure external sources monitoring
curl -X POST http://localhost:10050/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "monitor-001",
    "params": {
      "id": "monitor-001",
      "sessionId": "tutorial-session",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Set up monitoring for external sources: OpenShift docs, Quarkus guides, Kafka documentation, and ML best practices for Healthcare ML workshop"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

### External Sources Configuration

Create a configuration file:

```json
{
  "workshop_name": "Healthcare ML Genetic Predictor",
  "external_sources": {
    "documentation_sites": [
      "https://docs.openshift.com/container-platform/latest/",
      "https://quarkus.io/guides/websockets",
      "https://kafka.apache.org/documentation/",
      "https://scikit-learn.org/stable/user_guide.html"
    ],
    "pdf_documents": [
      "https://www.redhat.com/en/resources/openshift-container-platform-datasheet"
    ],
    "api_endpoints": [
      "https://docs.openshift.com/container-platform/4.16/rest_api/"
    ]
  },
  "monitoring_settings": {
    "frequency": "daily",
    "rag_integration": true
  }
}
```

## 🚀 Step 7: Deploy Workshop

Use the Source Manager Agent to deploy to RHPDS/Showroom:

```bash
# Deploy to Showroom platform
curl -X POST http://localhost:10060/send-task \
  -H "Content-Type: application/json" \
  -d '{
    "id": "deploy-001",
    "params": {
      "id": "deploy-001",
      "sessionId": "tutorial-session",
      "message": {
        "role": "user",
        "parts": [{
          "type": "text",
          "text": "Deploy healthcare-ml-workshop to Showroom platform with staging environment setup"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

## ✅ Step 8: Test Workshop Chat Agent

Verify the Workshop Chat Agent can assist participants:

```bash
# Test participant assistance
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
          "text": "How do I set up the Quarkus WebSocket connection for real-time genetic data processing?"
        }]
      },
      "acceptedOutputModes": ["text/plain"]
    }
  }'
```

## 🎯 What You've Accomplished

You've successfully:
- ✅ Analyzed a GitHub repository for workshop potential
- ✅ Generated a comprehensive workshop structure
- ✅ Created detailed educational content
- ✅ Validated content for technical accuracy
- ✅ Set up professional Showroom template
- ✅ Configured external documentation monitoring
- ✅ Deployed workshop to target platform
- ✅ Enabled participant assistance through chat agent

## 🔄 Next Steps

1. **Customize Content**: Refine the generated content for your specific audience
2. **Add Exercises**: Create hands-on labs and practical activities
3. **Test Workshop**: Run through the complete workshop experience
4. **Gather Feedback**: Collect participant feedback and iterate
5. **Monitor Updates**: Let the system track external documentation changes

## 📚 Related Documentation

- [How to Create Original Workshop Content](../how-to/create-original-content.md)
- [How to Set Up External Documentation Monitoring](../how-to/external-monitoring.md)
- [Multi-Agent Coordination Workflows](../explanation/workflows.md)
- [Agent API Reference](../reference/agent-api.md)

---

*This tutorial demonstrates the complete repository-to-workshop conversion process using all 6 agents in coordination.*
