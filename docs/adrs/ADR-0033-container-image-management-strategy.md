# ADR-0033: Container Image Build and Registry Management Strategy

## Status
Proposed

## Context

The Workshop Template System is experiencing container image-related issues in OpenShift:

- ImagePullBackOff errors in workshop-monitoring-service pods
- Build failures in recent OpenShift builds
- No documented strategy for container image lifecycle management
- Using OpenShift's internal image registry without formal policies

Current evidence from the cluster:
```
workshop-monitoring-service-59bf8f8c6f-rfdf8: ImagePullBackOff
workshop-monitoring-service-855ccfddc4-d9t89: ImagePullBackOff
build.build.openshift.io/workshop-monitoring-service-build-15: Failed (DockerBuildFailed)
```

This impacts system reliability and deployment success rates.

## Decision

Implement a comprehensive container image build and registry management strategy:

### 1. Standardized Image Tagging and Versioning
- Semantic versioning for all images: `major.minor.patch`
- Environment-specific tags: `latest`, `dev`, `staging`, `prod`
- Git commit SHA tags for traceability
- Immutable tags for production deployments

### 2. Automated Build Pipeline with Error Handling
- Multi-stage builds for optimization
- Build cache management for faster builds
- Automated vulnerability scanning
- Build failure notification and retry logic

### 3. Registry Management and Policies
- Image retention policies (keep last 10 versions)
- Automated cleanup of unused images
- Registry health monitoring
- Backup and disaster recovery procedures

### 4. Image Security and Compliance
- Vulnerability scanning with Clair or Trivy
- Base image update automation
- Security policy enforcement
- Compliance reporting

### 5. Deployment Safety Mechanisms
- Image availability verification before deployment
- Rollback mechanisms for failed deployments
- Health checks for image integrity
- Canary deployment support

### 6. Monitoring and Observability
- Image pull metrics and alerting
- Build success/failure tracking
- Registry storage utilization monitoring
- Performance metrics for image operations

## Consequences

### Positive
- Eliminated ImagePullBackOff errors through proper image management
- Faster and more reliable builds with caching and optimization
- Improved security through automated vulnerability scanning
- Optimized storage usage with retention policies
- Automated image lifecycle management reducing manual overhead
- Better traceability and rollback capabilities

### Negative
- Additional complexity in build pipelines and CI/CD processes
- Increased storage requirements for build cache and multiple image versions
- Potential longer initial setup time for implementing all policies
- Additional monitoring and maintenance overhead

## Implementation Plan

### Phase 1: Immediate Fixes (Week 1)
- Fix current ImagePullBackOff issues
- Implement basic image tagging strategy
- Add build failure retry logic

### Phase 2: Build Optimization (Week 2)
- Implement multi-stage builds
- Add build cache management
- Create automated vulnerability scanning

### Phase 3: Registry Management (Week 3-4)
- Implement retention policies
- Add registry monitoring
- Create backup procedures

## Technical Implementation

### Image Tagging Strategy
```yaml
# Production images
workshop-monitoring-service:1.2.3
workshop-monitoring-service:1.2.3-abc123f

# Environment tags
workshop-monitoring-service:latest
workshop-monitoring-service:dev
workshop-monitoring-service:prod
```

### Build Configuration
```yaml
# Multi-stage Dockerfile example
FROM registry.access.redhat.com/ubi8/openjdk-17:latest AS builder
# Build stage

FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:latest AS runtime
# Runtime stage
```

### Registry Cleanup Policy
```yaml
# Keep last 10 versions per image
# Remove images older than 30 days
# Preserve production tags indefinitely
```

## Alternatives Considered

1. **Manual image management**: Not scalable for production
2. **External registry only**: Loses OpenShift integration benefits
3. **Simple tagging without lifecycle management**: Leads to storage bloat

## Related ADRs
- ADR-0023: OpenShift Deployment Strategy
- ADR-0025: Kubernetes Deployment Architecture
- ADR-0032: Pipeline Failure Recovery Strategy

## Success Metrics
- Zero ImagePullBackOff errors
- Build success rate: >95%
- Image pull time: <30 seconds
- Registry storage growth: <10% monthly

---

**Date**: 2025-01-04
**Participants**: Workshop Template System Development Team
**Review Date**: 2025-04-04 (3 months)
