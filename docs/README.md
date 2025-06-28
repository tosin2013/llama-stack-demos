# Workshop Template System Documentation

A comprehensive 6-agent system for creating, maintaining, and deploying interactive workshops from GitHub repositories or original content.

## 🎯 Quick Start

- **New to the system?** Start with [Getting Started Tutorial](tutorials/getting-started.md)
- **Need to solve a specific problem?** Check our [How-To Guides](how-to/index.md)
- **Want to understand the architecture?** Read [System Overview](explanation/architecture.md)
- **Looking for specific details?** Browse the [Reference Documentation](reference/index.md)

## 📋 Documentation Structure

This documentation follows the [Diátaxis framework](https://diataxis.fr/) for systematic documentation:

### 🎓 [Tutorials](tutorials/index.md)
Step-by-step learning-oriented guides:
- [Getting Started: Repository-Based Workshops](tutorials/repository-based-workshops.md)
- [Getting Started: Original Content Workshops](tutorials/original-content-workshops.md)
- [Your First Workshop with Showroom Template](tutorials/showroom-template-workshop.md)

### 🛠️ [How-To Guides](how-to/index.md)
Problem-solving oriented guides:
- [How to Add a New Workshop from GitHub Repository](how-to/add-repository-workshop.md)
- [How to Create Original Workshop Content](how-to/create-original-content.md)
- [How to Deploy Workshops to GitHub Pages](how-to/deploy-github-pages.md)
- [How to Set Up External Documentation Monitoring](how-to/external-monitoring.md)
- [How to Deploy Workshops to RHPDS/Showroom](how-to/deploy-workshops.md)
- [How to Configure Multi-Agent Coordination](how-to/agent-coordination.md)

### 📖 [Explanation](explanation/index.md)
Understanding-oriented documentation:
- [6-Agent Workshop Template System Architecture](explanation/architecture.md)
- [Multi-Agent Coordination Workflows](explanation/workflows.md)
- [RAG Integration and External Sources](explanation/rag-integration.md)
- [Workshop Detection and Classification](explanation/workshop-detection.md)

### 📚 [Reference](reference/index.md)
Information-oriented documentation:
- [Agent API Reference](reference/agent-api.md)
- [Configuration Schema Reference](reference/configuration.md)
- [Deployment Guide](reference/deployment.md)
- [Troubleshooting Guide](reference/troubleshooting.md)

## 🚀 System Overview

The Workshop Template System consists of 6 specialized agents:

1. **Workshop Chat Agent** (Port 10040) - RAG-based participant assistance
2. **Template Converter Agent** (Port 10041) - Repository analysis and conversion
3. **Documentation Pipeline Agent** (Port 10050) - Automated content monitoring
4. **Source Manager Agent** (Port 10060) - Repository and deployment management
5. **Research & Validation Agent** (Port 10070) - Internet-grounded fact-checking
6. **Content Creator Agent** (Port 10080) - Original workshop creation

## 🎯 Use Cases

### Repository-Based Workshops
Convert existing GitHub repositories into interactive workshops:
- **Existing Workshops**: Enhance and modernize (e.g., OpenShift Bare Metal Workshop)
- **Applications**: Convert to educational content (e.g., Healthcare ML Genetic Predictor)

### Original Content Workshops
Create workshops from learning objectives without source repositories:
- **Conceptual Topics**: Cloud security, DevOps practices, architecture patterns
- **Tool Training**: Platform-specific workshops, vendor tools
- **Certification Prep**: Structured learning paths

## 🏗️ Quick Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Workshop Chat  │    │Template Convert │    │Documentation    │
│     Agent       │    │     Agent       │    │Pipeline Agent   │
│   (Port 10040)  │    │   (Port 10041)  │    │   (Port 10050)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│Source Manager   │    │Research & Valid │    │Content Creator  │
│     Agent       │    │     Agent       │    │     Agent       │
│   (Port 10060)  │    │   (Port 10070)  │    │   (Port 10080)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔧 Prerequisites

- **Python 3.8+** with required packages
- **Ollama** with Llama 3.2 3B model
- **Llama Stack** server running
- **Podman/Docker** for containerization
- **OpenShift/Kubernetes** for deployment (optional)

## 📞 Support

- **Issues**: Report bugs and feature requests in the GitHub repository
- **Documentation**: This comprehensive guide covers all system aspects
- **Community**: Join discussions about workshop creation and best practices

---

*This documentation is maintained by the Workshop Template System team and follows the Diátaxis framework for systematic, user-focused documentation.*
