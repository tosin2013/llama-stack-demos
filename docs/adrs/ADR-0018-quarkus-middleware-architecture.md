# ADR-0018: Quarkus Middleware Architecture for Tekton Pipeline Integration

## Status
**ACCEPTED** ✅ (Implementation Required)

## Context
The current Tekton pipeline implementation attempts to call agent endpoints directly, leading to endpoint mismatch issues (HTTP 404 errors) due to differences between expected `/tools/` endpoints and actual A2A protocol `/invoke` endpoints. Analysis of the workshop-monitoring-service reveals it already has 80% of the infrastructure needed to serve as an effective middleware layer for agent interactions.

## Decision
Use the existing Quarkus workshop-monitoring-service as middleware/API gateway between Tekton pipelines and agents, rather than direct agent calls or creating separate testing containers.

## Architecture

### Current Architecture (Problematic)
```
Tekton Pipeline → curl/jq scripts → Agent A2A /invoke endpoints
```

### New Architecture (Proposed)
```
Tekton Pipeline → HTTP calls → Quarkus Middleware → Agent A2A /invoke endpoints
```

## Implementation Strategy

### 1. Extend AgentInteractionResource
Add pipeline-specific endpoints to the existing `AgentInteractionResource` class:

```java
@Path("/api/pipeline")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PipelineIntegrationResource {

    @POST
    @Path("/content-creator/create-workshop")
    public Response createWorkshopContent(CreateWorkshopRequest request) {
        // Handle A2A protocol call to content-creator agent
        // Transform request/response for pipeline compatibility
    }

    @POST
    @Path("/template-converter/analyze-repository")
    public Response analyzeRepository(AnalyzeRepositoryRequest request) {
        // Handle A2A protocol call to template-converter agent
    }
    
    // Additional pipeline-specific endpoints...
}
```

### 2. Service Layer Implementation
Create service classes for agent communication:

```java
@ApplicationScoped
public class AgentOrchestrationService {
    
    @Inject
    AgentHealthService agentHealthService;
    
    public AgentResponse invokeAgent(String agentName, String toolName, Map<String, Object> parameters) {
        // Centralized A2A protocol handling
        // Error handling, retry logic, circuit breakers
        // Request/response transformation
    }
}
```

### 3. Tekton Task Simplification
Transform complex Tekton tasks from:
```bash
# Complex curl/jq scripts with error handling
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$(params.agent-endpoint)/invoke" \
  -H "Content-Type: application/json" \
  -d "$REQUEST_PAYLOAD" \
  --connect-timeout 30 \
  --max-time $(params.timeout-seconds))
```

To simple HTTP calls:
```bash
# Simple HTTP call to Quarkus middleware
RESPONSE=$(curl -X POST "http://workshop-monitoring-service:8080/api/pipeline/content-creator/create-workshop" \
  -H "Content-Type: application/json" \
  -d '{"workshop_name": "$(params.workshop-name)", "repository_url": "$(params.repository-url)"}')
```

## Benefits

### 1. Immediate Problem Resolution
- ✅ Eliminates endpoint mismatch issues (no more `/tools/` vs `/invoke` confusion)
- ✅ Provides stable, well-tested interface for Tekton pipelines
- ✅ Centralizes error handling and retry logic

### 2. Enhanced Reliability
- **Circuit Breaker Patterns**: Quarkus built-in fault tolerance
- **Centralized Retry Logic**: Consistent across all agent interactions
- **Better Error Reporting**: Structured error responses to Tekton
- **Health Monitoring**: Integration with existing agent health checks

### 3. Improved Observability
- **Centralized Logging**: All agent interactions logged in one place
- **Metrics Collection**: Quarkus Micrometer integration
- **Distributed Tracing**: Request tracing across agent calls
- **Performance Monitoring**: Response time tracking and analysis

### 4. Simplified Development
- **Reduced Tekton Complexity**: Simple HTTP calls instead of complex scripts
- **Standardized API**: Consistent request/response format
- **Better Testing**: Mock endpoints for local development
- **Documentation**: OpenAPI/Swagger integration

### 5. Architectural Consistency
- **Reuses Existing Infrastructure**: 80% of middleware capabilities already present
- **Natural Evolution**: Extends current monitoring service capabilities
- **Maintains Compatibility**: Existing monitoring functionality preserved
- **Follows Patterns**: Consistent with current REST API structure

## Testing Strategy

### 1. Mock Endpoints for Development
Add development-mode mock endpoints to the Quarkus service:
```java
@Path("/api/pipeline/mock")
@ApplicationScoped
public class MockPipelineResource {
    
    @ConfigProperty(name = "quarkus.profile")
    String profile;
    
    @POST
    @Path("/content-creator/create-workshop")
    public Response mockCreateWorkshop(CreateWorkshopRequest request) {
        if ("dev".equals(profile)) {
            // Return mock response for testing
            return Response.ok(createMockWorkshopResponse()).build();
        }
        return Response.status(404).build();
    }
}
```

### 2. Local Testing Workflow
```bash
# 1. Start Quarkus service in dev mode with mocks
mvn quarkus:dev -Dquarkus.profile=dev

# 2. Test Tekton pipeline locally with mock endpoints
tkn pipeline start workflow-1-new-workshop \
  --param middleware-endpoint="http://localhost:8080/api/pipeline/mock" \
  --workspace name=shared-data,emptyDir=""

# 3. Validate pipeline logic without live agents
```

## Migration Plan

### Phase 1: Infrastructure Setup
1. Add pipeline integration endpoints to workshop-monitoring-service
2. Implement agent orchestration service layer
3. Add mock endpoints for testing
4. Update service configuration and documentation

### Phase 2: Tekton Task Updates
1. Update agent-task-content-creator.yaml to use middleware
2. Update agent-task-template-converter.yaml to use middleware
3. Update agent-task-source-manager.yaml to use middleware
4. Update remaining agent tasks

### Phase 3: Testing and Validation
1. Test with mock endpoints locally
2. Test with live agents in development environment
3. Validate complete pipeline execution
4. Performance testing and optimization

### Phase 4: Production Deployment
1. Deploy updated Quarkus service to OpenShift
2. Update Tekton pipeline definitions
3. Monitor and validate production execution
4. Documentation updates

## Consequences

### Positive
- **Eliminates Current Blocking Issues**: Resolves HTTP 404 errors immediately
- **Improves System Reliability**: Centralized error handling and monitoring
- **Simplifies Pipeline Development**: Reduces complexity in Tekton tasks
- **Enables Better Testing**: Mock endpoints for local development
- **Enhances Observability**: Centralized logging and metrics
- **Leverages Existing Infrastructure**: Reuses 80% of current capabilities

### Negative
- **Additional Network Hop**: Slight latency increase (minimal impact)
- **Single Point of Failure**: Mitigated by Quarkus reliability and scaling
- **Development Overhead**: Initial implementation effort required

### Neutral
- **Architectural Consistency**: Maintains existing patterns and conventions
- **Backward Compatibility**: Existing monitoring functionality preserved

## Related ADRs
- **ADR-0004**: DDD Frontend-Backend Integration Architecture (alignment maintained)
- **ADR-0007**: Shared Workspace Strategy (workspace coordination preserved)
- **ADR-0008**: Enhanced Workspace Coordination (coordination patterns maintained)
- **ADR-0009**: Agent Workspace Integration (agent access patterns unchanged)
- **ADR-0010**: Workspace Tool Implementation (superseded by middleware approach)
- **ADR-0017**: Content Creator Agent (agent functionality unchanged)

## Implementation Notes
- Maintain existing monitoring service functionality
- Use Quarkus fault tolerance extensions for reliability
- Implement comprehensive logging for debugging
- Add metrics for performance monitoring
- Ensure OpenAPI documentation for all new endpoints
- Follow existing code patterns and conventions

---
**Decision Date**: 2025-06-30  
**Status**: Accepted - Implementation Required  
**Next Review**: After Phase 1 completion
