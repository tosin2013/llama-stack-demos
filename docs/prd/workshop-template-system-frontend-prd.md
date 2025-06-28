# Workshop Template System Frontend - Product Requirements Document

## Executive Summary

Based on comprehensive analysis of the Workshop Template System codebase and modern frontend architecture patterns, this document provides detailed requirements for a Node.js/React frontend that will orchestrate the existing 6-agent system through an intuitive user interface. The frontend will transform complex multi-agent workflows into a simple URL-driven experience while providing real-time monitoring and OpenShift integration.

## 1. Current System Analysis

### 1.1 Agent Architecture Overview

The Workshop Template System consists of 6 specialized agents with well-defined APIs:

| Agent | Port | Primary Function | Key Capabilities |
|-------|------|------------------|------------------|
| **Workshop Chat Agent** | 10040 | RAG-based participant assistance | Workshop content queries, navigation assistance |
| **Template Converter Agent** | 10041 | Repository analysis & conversion | GitHub repository analysis, workshop detection |
| **Content Creator Agent** | 10042 | Workshop content generation | Showroom template integration, content creation |
| **Source Manager Agent** | 10051 | Repository & deployment management | Gitea integration, OpenShift deployment |
| **Research Validation Agent** | 10052 | Technical accuracy validation | External source validation, content verification |
| **Documentation Pipeline Agent** | 10050 | Content monitoring & updates | Repository change detection, update orchestration |

### 1.2 Integration Points

#### **Gitea Integration**
- **API Endpoints**: Full REST API for repository management
- **Authentication**: Service account with admin privileges (`opentlc-mgr`)
- **Operations**: Repository creation, content commits, webhook management
- **BuildConfig Triggers**: Automatic OpenShift builds via Git webhooks
- **Organization**: `workshop-system` org for all workshop repositories

#### **OpenShift Integration**
- **API Access**: Kubernetes client with RBAC permissions
- **Resources**: BuildConfigs, Deployments, Routes, Services, Pods
- **Monitoring**: Real-time status of builds, deployments, and pod health
- **Authentication**: Service account with cluster-admin or edit permissions
- **Namespace**: `workshop-system` for all workshop components

#### **Workflow Patterns**
- **Leader-Collaborator**: Primary agent leads workflow phase with supporting agents
- **Validation Chain**: Content generation → validation → iteration → approval
- **Monitoring & Feedback**: Continuous surveillance with human-in-the-loop decisions

## 2. Frontend Requirements Specification

### 2.1 Core User Experience

#### **Primary User Flow**
1. **Repository URL Input**: User enters GitHub repository URL
2. **Automatic Detection**: System analyzes URL and determines workflow type
3. **Workflow Orchestration**: Frontend coordinates appropriate agent sequence
4. **Real-time Progress**: Live updates on agent activities and progress
5. **Workshop Deployment**: Automatic deployment to OpenShift with live URLs

#### **Supported Repository Types**
- **Application Repositories**: Convert to workshops (Workflow 1)
  - Example: `https://github.com/tosin2013/healthcare-ml-genetic-predictor.git`
- **Existing Workshops**: Enhance and modernize (Workflow 3)
  - Example: `https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git`
- **Documentation Repositories**: Transform to interactive content

### 2.2 Technical Architecture

#### **Frontend Stack**
- **Framework**: React 18+ with TypeScript
- **State Management**: XState for complex workflow orchestration
- **Real-time Communication**: Socket.IO for WebSocket connections
- **UI Framework**: Material-UI or Chakra UI for consistent design
- **Build Tool**: Vite for fast development and building
- **Routing**: React Router for navigation

#### **Backend Stack**
- **Runtime**: Node.js 18+ with Express.js
- **API Integration**: Axios for HTTP requests to agents and APIs
- **WebSocket Server**: Socket.IO server for real-time updates
- **Authentication**: JWT tokens for session management
- **Process Management**: Bull Queue for background job processing
- **Database**: Redis for session and workflow state caching

### 2.3 State Management Architecture

#### **XState Workflow Machines**

The frontend will use XState to manage complex workflow state transitions:

**Workflow 1: Repository-Based Workshop Creation**
```
analyzing → validated → generating → deploying → completed
    ↓           ↓           ↓           ↓
  failed     failed     failed     failed
```

**Workflow 3: Workshop Enhancement**
```
analyzing → researching → enhancing → deploying → completed
    ↓           ↓           ↓           ↓
  failed     failed     failed     failed
```

#### **Agent Coordination States**
- **Template Converter**: Repository analysis and workflow detection
- **Research Validation**: Technical accuracy and external source validation
- **Content Creator**: Workshop content generation using Showroom template
- **Source Manager**: Gitea repository management and OpenShift deployment
- **Documentation Pipeline**: Content monitoring and update orchestration
- **Workshop Chat**: RAG system preparation and participant assistance setup

## 3. API Integration Layer

### 3.1 Agent Communication Interface

```typescript
interface AgentClient {
  sendTask(agentName: string, task: AgentTask): Promise<AgentResponse>;
  getAgentCard(agentName: string): Promise<AgentCard>;
  subscribeToUpdates(agentName: string, callback: (update: AgentUpdate) => void): void;
}

interface AgentTask {
  id: string;
  sessionId: string;
  message: {
    role: 'user';
    parts: Array<{ type: 'text'; text: string }>;
  };
  acceptedOutputModes: string[];
}

interface AgentResponse {
  id: string;
  status: 'completed' | 'failed' | 'in_progress';
  result?: any;
  error?: string;
}
```

### 3.2 OpenShift Integration

```typescript
interface OpenShiftClient {
  // Build Management
  getBuildStatus(buildName: string): Promise<BuildStatus>;
  triggerBuild(buildConfigName: string): Promise<Build>;
  getBuildLogs(buildName: string): Promise<string>;
  
  // Deployment Management
  getDeploymentStatus(deploymentName: string): Promise<DeploymentStatus>;
  getPodLogs(podName: string): Promise<string>;
  getPodStatus(namespace: string): Promise<Pod[]>;
  
  // Route Management
  getRoutes(namespace: string): Promise<Route[]>;
  createRoute(route: RouteSpec): Promise<Route>;
  
  // Health Monitoring
  getNamespaceHealth(namespace: string): Promise<NamespaceHealth>;
}
```

### 3.3 Gitea Integration

```typescript
interface GiteaClient {
  // Repository Management
  createRepository(org: string, repo: RepositorySpec): Promise<Repository>;
  getRepository(org: string, repo: string): Promise<Repository>;
  commitContent(repo: string, content: CommitContent): Promise<Commit>;
  
  // Webhook Management
  createWebhook(repo: string, webhook: WebhookSpec): Promise<Webhook>;
  getRepositoryStatus(repo: string): Promise<RepositoryStatus>;
  
  // Organization Management
  getOrganization(orgName: string): Promise<Organization>;
  listRepositories(orgName: string): Promise<Repository[]>;
}
```

## 4. Real-time Monitoring System

### 4.1 WebSocket Event Types

```typescript
interface WorkflowEvents {
  'workflow:started': { workflowId: string; type: WorkflowType; repositoryUrl: string };
  'workflow:progress': { workflowId: string; stage: string; progress: number };
  'agent:activity': { agentName: string; activity: string; status: 'started' | 'completed' | 'failed' };
  'build:status': { buildName: string; status: BuildStatus; progress?: number };
  'deployment:status': { deploymentName: string; status: DeploymentStatus };
  'workshop:ready': { workshopName: string; url: string; chatUrl?: string };
  'error:occurred': { workflowId: string; error: string; recoverable: boolean };
}
```

### 4.2 Progress Tracking Components

- **Workflow Progress Bar**: Overall workflow completion percentage
- **Agent Activity Feed**: Real-time log of agent activities with timestamps
- **Build Status Monitor**: OpenShift build progress and logs
- **Deployment Health**: Pod status and resource utilization
- **Error Notifications**: User-friendly error messages with recovery options

## 5. User Interface Design

### 5.1 Main Dashboard Layout

```
┌─────────────────────────────────────────────────────────────┐
│ Workshop Template System                            [User]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🚀 Create New Workshop                                     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Repository URL: [________________________] [Create] │   │
│  │                                                     │   │
│  │ Detected: ✅ Application Repository                 │   │
│  │ Workflow: Repository-Based Creation                 │   │
│  │ Estimated Time: 15-20 minutes                       │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  📊 Active Workshops                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Healthcare ML Workshop        [████████░░] 80%      │   │
│  │ Status: Content Generation    Agent: Content Creator│   │
│  │ [View Progress] [Open Workshop] [View Logs]         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  🏗️ System Status                                          │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Agents: 6/6 Healthy    Builds: 2 Running           │   │
│  │ OpenShift: Connected   Gitea: Connected             │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 Workflow Progress View

```
┌─────────────────────────────────────────────────────────────┐
│ Healthcare ML Workshop Creation                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📋 Workflow Progress                                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ ✅ Repository Analysis      Template Converter      │   │
│  │ ✅ Requirements Validation  Research Validation     │   │
│  │ 🔄 Content Generation       Content Creator         │   │
│  │ ⏳ Workshop Deployment      Source Manager          │   │
│  │ ⏳ Chat Integration         Workshop Chat           │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  🤖 Agent Activity Feed                                     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 14:32 Content Creator: Generating module 3 of 5    │   │
│  │ 14:31 Content Creator: Created lab exercise        │   │
│  │ 14:30 Research Validation: Verified Quarkus docs   │   │
│  │ 14:29 Template Converter: Analysis complete        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  🏗️ Build & Deployment Status                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Build: healthcare-ml-build-3    [████████░░] 80%   │   │
│  │ Status: Pushing image to registry                   │   │
│  │ [View Build Logs] [View Pod Status]                 │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 5.3 Workshop Management View

```
┌─────────────────────────────────────────────────────────────┐
│ Workshop Management Dashboard                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📚 Workshop Library                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Healthcare ML Workshop                              │   │
│  │ Status: ✅ Live    Participants: 23    Rating: 4.8 │   │
│  │ URL: https://healthcare-ml.apps.cluster.local      │   │
│  │ [Open] [Edit] [Analytics] [Chat Logs]              │   │
│  │                                                     │   │
│  │ OpenShift Bare Metal Workshop                       │   │
│  │ Status: 🔄 Updating    Progress: [██████░░░░] 60%  │   │
│  │ Agent: Documentation Pipeline                       │   │
│  │ [View Progress] [Pause] [Rollback]                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  🔧 System Administration                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Agent Health: 6/6 ✅    OpenShift: Connected ✅    │   │
│  │ Gitea: Connected ✅     Active Builds: 1           │   │
│  │ Memory Usage: 68%       CPU Usage: 45%             │   │
│  │ [View Metrics] [Agent Logs] [System Settings]      │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 6. Implementation Roadmap

### 6.1 Phase 1: Core Infrastructure (Weeks 1-2)

**Backend Setup**
- Express.js server with TypeScript
- Socket.IO server for real-time communication
- Basic authentication and session management
- Health check endpoints

**Agent Integration**
- HTTP client for all 6 agents
- Agent discovery and health monitoring
- Basic task submission and response handling
- Error handling and retry mechanisms

**Basic UI**
- React app with TypeScript setup
- Repository URL input form
- Basic workflow detection
- Simple progress display

**State Management**
- XState setup and basic workflow machines
- Redux Toolkit for UI state management
- WebSocket integration for real-time updates

### 6.2 Phase 2: OpenShift Integration (Weeks 3-4)

**Kubernetes Client**
- OpenShift API integration using `@kubernetes/client-node`
- Service account authentication
- RBAC permission setup
- Namespace management

**Build Monitoring**
- Real-time build status tracking
- Build log streaming
- BuildConfig management
- Image registry integration

**Deployment Management**
- Pod status monitoring
- Service and route management
- Health check integration
- Resource utilization tracking

**UI Enhancements**
- Build progress visualization
- Deployment status dashboard
- Log viewer component
- Error notification system

### 6.3 Phase 3: Gitea Integration (Weeks 5-6)

**Repository Management**
- Gitea API client implementation
- Repository creation and management
- Organization and user management
- Permission and access control

**Content Commits**
- Automated content deployment to Gitea
- Git workflow integration
- Commit history tracking
- Branch management

**Webhook Setup**
- Build trigger configuration
- Event handling and processing
- Webhook security and validation
- Integration with OpenShift BuildConfigs

**UI Features**
- Repository browser interface
- Content preview and editing
- Commit history visualization
- Webhook management dashboard

### 6.4 Phase 4: Advanced Features (Weeks 7-8)

**Multi-workshop Support**
- Concurrent workflow management
- Resource allocation and scheduling
- Workshop isolation and security
- Performance optimization

**User Management**
- Authentication and authorization
- Role-based access control
- User profiles and preferences
- Activity logging and auditing

**Analytics Dashboard**
- Usage metrics and reporting
- Performance monitoring
- Workshop analytics
- User engagement tracking

**Error Recovery**
- Workflow failure detection
- Automatic retry mechanisms
- Manual intervention options
- Rollback and recovery procedures

## 7. Technical Considerations

### 7.1 Security Requirements

**Authentication & Authorization**
- JWT-based session management
- Role-based access control (RBAC)
- OpenShift service account integration
- Gitea admin token management

**API Security**
- Rate limiting and throttling
- Input validation and sanitization
- CORS configuration
- HTTPS enforcement

**Data Protection**
- Sensitive data encryption
- Secure credential storage
- Audit logging
- Privacy compliance

### 7.2 Performance Optimization

**Caching Strategy**
- Redis for session and workflow state
- Agent response caching
- Static asset optimization
- Database query optimization

**Connection Management**
- HTTP connection pooling
- WebSocket connection management
- Database connection pooling
- Resource cleanup and garbage collection

**UI Performance**
- Lazy loading and code splitting
- Virtual scrolling for large lists
- Debounced user inputs
- Progressive web app features

### 7.3 Monitoring & Observability

**Application Metrics**
- Prometheus metrics collection
- Custom business metrics
- Performance monitoring
- Resource utilization tracking

**Logging & Tracing**
- Structured logging with correlation IDs
- Distributed tracing
- Error aggregation and alerting
- Log retention and archival

**Health Monitoring**
- Service health checks
- Dependency monitoring
- Alerting and notification
- SLA monitoring and reporting

## 8. Deployment Architecture

### 8.1 Container Strategy

**Frontend Container**
- React production build
- Nginx for static file serving
- Environment-based configuration
- Health check endpoints

**Backend Container**
- Node.js application
- Express.js API server
- Socket.IO WebSocket server
- Process management with PM2

### 8.2 OpenShift Deployment

**Deployment Configuration**
- Kubernetes manifests
- ConfigMaps for environment variables
- Secrets for sensitive data
- Service and route definitions

**Scaling Strategy**
- Horizontal pod autoscaling
- Resource requests and limits
- Load balancing configuration
- Session affinity management

### 8.3 CI/CD Pipeline

**Build Process**
- Automated testing and linting
- Container image building
- Security scanning
- Artifact management

**Deployment Process**
- Environment promotion
- Blue-green deployment
- Rollback capabilities
- Monitoring and validation

## 9. Success Metrics

### 9.1 User Experience Metrics

- **Time to Workshop**: Average time from URL input to live workshop
- **Success Rate**: Percentage of successful workshop creations
- **User Satisfaction**: User feedback and rating scores
- **Error Recovery**: Time to resolve workflow failures

### 9.2 Technical Metrics

- **System Uptime**: Availability of the frontend system
- **Response Time**: API response times and UI responsiveness
- **Resource Utilization**: CPU, memory, and storage usage
- **Scalability**: Concurrent workflow handling capacity

### 9.3 Business Metrics

- **Workshop Creation Volume**: Number of workshops created per period
- **User Adoption**: Number of active users and usage patterns
- **Workshop Quality**: Participant feedback and completion rates
- **Cost Efficiency**: Resource costs per workshop created

## 10. Conclusion

The proposed Workshop Template System frontend transforms complex multi-agent workflows into an intuitive, URL-driven platform. By leveraging modern web technologies including React, XState, and Socket.IO, combined with comprehensive OpenShift and Gitea integration, the frontend provides users with a seamless experience for creating and managing AI-generated workshops.

The architecture supports the core vision of simply entering a repository URL and having the system automatically determine and execute the appropriate workflow, while providing real-time feedback, professional workshop deployment capabilities, and comprehensive monitoring and management features.

This solution bridges the gap between the sophisticated backend agent system and end-user needs, making the Workshop Template System accessible to a broader audience while maintaining the full power and flexibility of the underlying multi-agent architecture.
