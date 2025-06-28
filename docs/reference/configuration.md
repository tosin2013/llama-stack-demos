# Configuration Schema Reference

Comprehensive configuration examples for real-world workshop scenarios using the Workshop Template System.

## 🎯 Configuration Overview

The Workshop Template System uses JSON-based configuration files to define workshop parameters, agent settings, external sources, and deployment targets. This reference provides complete examples for common scenarios.

## 📋 Base Configuration Schema

### Workshop Metadata Schema
```json
{
  "workshop": {
    "name": "string",
    "title": "string", 
    "description": "string",
    "version": "string",
    "type": "repository-based|original-content",
    "difficulty": "beginner|intermediate|advanced",
    "duration": "string (e.g., '4 hours', '2 days')",
    "target_audience": "string",
    "prerequisites": ["string"],
    "learning_objectives": ["string"],
    "technologies": ["string"],
    "created_date": "ISO 8601 date",
    "last_updated": "ISO 8601 date",
    "maintainers": ["string"]
  }
}
```

### Agent Configuration Schema
```json
{
  "agents": {
    "workshop_chat": {
      "port": 10040,
      "rag_enabled": true,
      "vector_db_config": {},
      "response_style": "helpful|technical|beginner-friendly"
    },
    "template_converter": {
      "port": 10041,
      "analysis_depth": "quick|standard|deep",
      "workshop_detection": true
    },
    "documentation_pipeline": {
      "port": 10050,
      "monitoring_frequency": "hourly|daily|weekly",
      "external_sources": {}
    },
    "source_manager": {
      "port": 10060,
      "deployment_targets": [],
      "repository_settings": {}
    },
    "research_validation": {
      "port": 10070,
      "search_enabled": true,
      "fact_checking": true
    },
    "content_creator": {
      "port": 10080,
      "showroom_integration": true,
      "content_style": "formal|conversational|technical"
    }
  }
}
```

## 🧬 Example 1: Healthcare ML Genetic Predictor Workshop

**Scenario**: Converting a complex ML application repository into an educational workshop

```json
{
  "workshop": {
    "name": "healthcare-ml-genetic-predictor",
    "title": "Healthcare ML: Genetic Risk Prediction on OpenShift",
    "description": "Learn to build and deploy machine learning applications for genetic risk prediction using Quarkus, Kafka, and OpenShift",
    "version": "1.0.0",
    "type": "repository-based",
    "difficulty": "intermediate",
    "duration": "6 hours",
    "target_audience": "Developers and data scientists with basic ML knowledge",
    "prerequisites": [
      "Basic Java/Quarkus experience",
      "Understanding of machine learning concepts",
      "Familiarity with containerization"
    ],
    "learning_objectives": [
      "Deploy ML applications on OpenShift",
      "Implement real-time data processing with Quarkus WebSockets",
      "Build event-driven architectures with Kafka",
      "Apply ML inference in production environments",
      "Monitor and scale ML applications"
    ],
    "technologies": ["Quarkus", "Kafka", "OpenShift", "Machine Learning", "WebSockets"],
    "source_repository": "https://github.com/tosin2013/healthcare-ml-genetic-predictor.git"
  },
  "agents": {
    "template_converter": {
      "analysis_depth": "deep",
      "workshop_detection": true,
      "technology_focus": ["quarkus", "kafka", "openshift", "ml"]
    },
    "content_creator": {
      "showroom_integration": true,
      "technology_focus": "openshift",
      "customization_level": "extensive",
      "content_style": "technical"
    },
    "research_validation": {
      "search_enabled": true,
      "validation_sources": [
        "https://quarkus.io/guides/",
        "https://docs.openshift.com/",
        "https://kafka.apache.org/documentation/"
      ]
    },
    "documentation_pipeline": {
      "monitoring_frequency": "daily",
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
        "change_threshold": 0.1,
        "rag_integration": true,
        "notification_webhook": "https://workshop-system.com/webhook/healthcare-ml"
      }
    },
    "source_manager": {
      "deployment_targets": ["rhpds", "showroom"],
      "repository_settings": {
        "create_new_repo": true,
        "repo_name": "healthcare-ml-workshop",
        "visibility": "public",
        "template_source": "showroom_template_default"
      }
    },
    "workshop_chat": {
      "rag_enabled": true,
      "response_style": "technical",
      "vector_db_config": {
        "index_name": "healthcare-ml-workshop",
        "embedding_model": "multilingual-e5-large"
      }
    }
  },
  "deployment": {
    "staging": {
      "platform": "openshift",
      "namespace": "healthcare-ml-staging",
      "resources": {
        "cpu": "2",
        "memory": "4Gi"
      }
    },
    "production": {
      "platform": "rhpds",
      "catalog_entry": "healthcare-ml-genetic-prediction",
      "showroom_url": "https://showroom.redhat.com/healthcare-ml"
    }
  }
}
```

## 🔒 Example 2: Cloud-Native Security Workshop (Original Content)

**Scenario**: Creating a workshop from scratch based on learning objectives

```json
{
  "workshop": {
    "name": "cloud-native-security-fundamentals",
    "title": "Cloud-Native Security Fundamentals",
    "description": "Comprehensive workshop on securing cloud-native applications and infrastructure",
    "version": "1.0.0",
    "type": "original-content",
    "difficulty": "intermediate",
    "duration": "4.5 hours",
    "target_audience": "DevOps engineers, security professionals, and developers",
    "prerequisites": [
      "Basic Kubernetes knowledge",
      "Understanding of containerization",
      "Familiarity with security concepts"
    ],
    "learning_objectives": [
      "Understand cloud-native security principles and threat models",
      "Implement container security best practices and scanning",
      "Configure network policies and micro-segmentation in Kubernetes",
      "Set up monitoring and alerting for security events",
      "Apply zero-trust architecture concepts to cloud deployments"
    ],
    "technologies": ["Kubernetes", "Container Security", "Network Policies", "Monitoring"],
    "source_repository": null
  },
  "agents": {
    "content_creator": {
      "showroom_integration": true,
      "technology_focus": "kubernetes",
      "customization_level": "standard",
      "content_style": "formal",
      "workshop_structure": {
        "modules": [
          {
            "name": "Introduction & Security Landscape",
            "duration": "20 minutes",
            "type": "presentation"
          },
          {
            "name": "Container Security Fundamentals", 
            "duration": "45 minutes",
            "type": "hybrid"
          },
          {
            "name": "Kubernetes Network Policies",
            "duration": "60 minutes", 
            "type": "hands-on"
          },
          {
            "name": "Security Monitoring & Alerting",
            "duration": "45 minutes",
            "type": "hands-on"
          },
          {
            "name": "Zero-Trust Implementation",
            "duration": "60 minutes",
            "type": "hands-on"
          },
          {
            "name": "Integration & Best Practices",
            "duration": "30 minutes",
            "type": "discussion"
          }
        ]
      }
    },
    "research_validation": {
      "search_enabled": true,
      "validation_sources": [
        "https://kubernetes.io/docs/concepts/security/",
        "https://owasp.org/www-project-container-security/",
        "https://www.cisecurity.org/benchmark/kubernetes"
      ],
      "fact_checking": true
    },
    "documentation_pipeline": {
      "monitoring_frequency": "weekly",
      "external_sources": {
        "documentation_sites": [
          "https://kubernetes.io/docs/concepts/security/",
          "https://owasp.org/www-project-container-security/",
          "https://www.cisecurity.org/benchmark/kubernetes"
        ],
        "pdf_documents": [
          "https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-190.pdf",
          "https://media.defense.gov/2022/Aug/29/2003066362/-1/-1/0/CTR_KUBERNETES_HARDENING_GUIDANCE_1.2_20220829.PDF"
        ]
      },
      "monitoring_settings": {
        "change_threshold": 0.15,
        "rag_integration": true
      }
    },
    "source_manager": {
      "deployment_targets": ["showroom", "openshift"],
      "repository_settings": {
        "create_new_repo": true,
        "repo_name": "cloud-security-workshop",
        "visibility": "public"
      }
    },
    "workshop_chat": {
      "rag_enabled": true,
      "response_style": "helpful",
      "vector_db_config": {
        "index_name": "cloud-security-workshop",
        "embedding_model": "multilingual-e5-large"
      }
    }
  },
  "assessment": {
    "enabled": true,
    "methods": ["hands-on-validation", "knowledge-check", "scenario-based"],
    "passing_criteria": {
      "hands_on_completion": 80,
      "knowledge_check_score": 75
    }
  }
}
```

## 🔧 Example 3: OpenShift Bare Metal Workshop Enhancement

**Scenario**: Enhancing an existing high-quality workshop with modern features

```json
{
  "workshop": {
    "name": "openshift-bare-metal-deployment-enhanced",
    "title": "OpenShift Bare Metal Deployment - Enhanced Edition",
    "description": "Enhanced version of the OpenShift bare metal deployment workshop with modern features and updated content",
    "version": "2.0.0",
    "type": "repository-based",
    "difficulty": "advanced",
    "duration": "8 hours",
    "target_audience": "Infrastructure engineers and OpenShift administrators",
    "source_repository": "https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git",
    "enhancement_type": "modernization"
  },
  "agents": {
    "template_converter": {
      "analysis_depth": "standard",
      "workshop_detection": true,
      "enhancement_focus": ["content-refresh", "technology-updates", "exercise-enhancement"]
    },
    "research_validation": {
      "search_enabled": true,
      "validation_focus": ["version-currency", "best-practices", "deprecated-features"],
      "validation_sources": [
        "https://docs.openshift.com/container-platform/latest/",
        "https://access.redhat.com/documentation/en-us/openshift_container_platform/"
      ]
    },
    "documentation_pipeline": {
      "monitoring_frequency": "daily",
      "external_sources": {
        "documentation_sites": [
          "https://docs.openshift.com/container-platform/latest/",
          "https://docs.openshift.com/container-platform/latest/installing/installing_bare_metal/"
        ],
        "api_endpoints": [
          "https://docs.openshift.com/container-platform/4.16/rest_api/"
        ]
      },
      "monitoring_settings": {
        "high_priority_sources": [
          "https://docs.openshift.com/container-platform/latest/installing/"
        ],
        "change_threshold": 0.05,
        "immediate_notification": true
      }
    },
    "content_creator": {
      "enhancement_mode": true,
      "modernization_features": [
        "interactive-elements",
        "assessment-integration", 
        "troubleshooting-scenarios",
        "automation-examples"
      ]
    },
    "source_manager": {
      "deployment_targets": ["rhpds", "showroom"],
      "repository_settings": {
        "fork_original": true,
        "enhancement_branch": "enhanced-v2",
        "preserve_history": true
      }
    }
  },
  "enhancement_plan": {
    "content_updates": [
      "Update to OpenShift 4.16+ procedures",
      "Add automation examples with Ansible",
      "Include troubleshooting scenarios",
      "Add monitoring and observability section"
    ],
    "structure_improvements": [
      "Add assessment checkpoints",
      "Include interactive demos",
      "Add advanced configuration options",
      "Create modular learning paths"
    ]
  }
}
```

## 🌐 Example 4: Multi-Technology Integration Workshop

**Scenario**: Workshop covering multiple technologies with complex dependencies

```json
{
  "workshop": {
    "name": "cloud-native-full-stack",
    "title": "Cloud-Native Full-Stack Development",
    "description": "End-to-end workshop covering frontend, backend, database, and deployment in cloud-native environments",
    "version": "1.0.0",
    "type": "original-content",
    "difficulty": "intermediate",
    "duration": "2 days",
    "target_audience": "Full-stack developers transitioning to cloud-native",
    "technologies": ["React", "Quarkus", "PostgreSQL", "OpenShift", "Kafka", "Redis"]
  },
  "agents": {
    "content_creator": {
      "multi_technology_mode": true,
      "technology_integration": {
        "frontend": {
          "primary": "React",
          "focus": ["hooks", "state-management", "api-integration"]
        },
        "backend": {
          "primary": "Quarkus",
          "focus": ["rest-apis", "reactive-programming", "microservices"]
        },
        "data": {
          "primary": "PostgreSQL",
          "secondary": "Redis",
          "focus": ["data-modeling", "caching", "transactions"]
        },
        "messaging": {
          "primary": "Kafka",
          "focus": ["event-streaming", "microservice-communication"]
        },
        "platform": {
          "primary": "OpenShift",
          "focus": ["deployment", "scaling", "monitoring"]
        }
      }
    },
    "research_validation": {
      "multi_source_validation": true,
      "technology_sources": {
        "react": ["https://react.dev/", "https://reactjs.org/docs/"],
        "quarkus": ["https://quarkus.io/guides/"],
        "postgresql": ["https://www.postgresql.org/docs/"],
        "kafka": ["https://kafka.apache.org/documentation/"],
        "openshift": ["https://docs.openshift.com/"]
      }
    },
    "documentation_pipeline": {
      "monitoring_frequency": "daily",
      "technology_specific_monitoring": {
        "react": {
          "sources": ["https://react.dev/blog"],
          "frequency": "weekly"
        },
        "quarkus": {
          "sources": ["https://quarkus.io/blog/"],
          "frequency": "daily"
        },
        "openshift": {
          "sources": ["https://docs.openshift.com/"],
          "frequency": "daily"
        }
      }
    }
  },
  "workshop_structure": {
    "day_1": {
      "morning": ["Environment Setup", "Frontend Development with React"],
      "afternoon": ["Backend APIs with Quarkus", "Database Integration"]
    },
    "day_2": {
      "morning": ["Event Streaming with Kafka", "Caching with Redis"],
      "afternoon": ["OpenShift Deployment", "Monitoring and Scaling"]
    }
  }
}
```

## ⚙️ Environment-Specific Configurations

### Development Environment
```json
{
  "environment": "development",
  "agents": {
    "all": {
      "log_level": "DEBUG",
      "mock_external_services": true,
      "fast_mode": true
    }
  },
  "external_sources": {
    "cache_enabled": true,
    "cache_duration": "1 hour"
  }
}
```

### Production Environment
```json
{
  "environment": "production",
  "agents": {
    "all": {
      "log_level": "INFO",
      "health_checks": true,
      "metrics_enabled": true
    }
  },
  "security": {
    "api_authentication": true,
    "rate_limiting": true,
    "input_validation": "strict"
  },
  "monitoring": {
    "prometheus_metrics": true,
    "alerting": true
  }
}
```

## 📊 Configuration Validation

### Required Fields Validation
```json
{
  "validation_rules": {
    "workshop.name": "required|string|max:100",
    "workshop.type": "required|in:repository-based,original-content",
    "workshop.learning_objectives": "required|array|min:3",
    "agents.*.port": "required|integer|min:1024|max:65535"
  }
}
```

### Configuration Templates
```bash
# Generate configuration from template
./workshop-system generate-config \
  --template healthcare-ml \
  --repository https://github.com/user/repo.git \
  --output config.json

# Validate configuration
./workshop-system validate-config config.json

# Apply configuration
./workshop-system apply-config config.json
```

## 🔗 Related Documentation

- [Agent API Reference](agent-api.md)
- [Deployment Guide](deployment.md)
- [Troubleshooting Guide](troubleshooting.md)
- [How-To Configuration Guides](../how-to/index.md)

---

*These configuration examples provide complete, real-world setups for various workshop scenarios and can be adapted for specific requirements.*
