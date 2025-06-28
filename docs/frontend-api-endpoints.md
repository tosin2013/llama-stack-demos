# Workshop Template System - API Endpoints for Frontend Development

## 🌐 Agent API Endpoints (HTTPS)

All agents are deployed in the `workshop-system` namespace and accessible via HTTPS routes:

### **Core Workshop Template System Agents**

| Agent | Endpoint | Purpose |
|-------|----------|---------|
| **Template Converter Agent** | `https://template-converter-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com` | Repository analysis & workflow detection |
| **Content Creator Agent** | `https://content-creator-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com` | Workshop content generation |
| **Workshop Chat Agent** | `https://workshop-chat-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com` | RAG-based participant assistance |
| **Source Manager Agent** | `https://source-manager-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com` | Repository & deployment management |
| **Research Validation Agent** | `https://research-validation-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com` | Technical accuracy validation |
| **Documentation Pipeline Agent** | `https://documentation-pipeline-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com` | Content monitoring & updates |

### **Agent API Endpoints**

Each agent supports the following endpoints:

#### **Agent Card (GET)**
```
GET https://{agent-url}/agent-card
Accept: application/json
```

**Response Schema:**
```typescript
interface AgentCard {
  name: string;
  description: string;
  version: string;
  capabilities: {
    streaming: boolean;
    pushNotifications: boolean;
    stateTransitionHistory: boolean;
  };
  skills: Array<{
    id: string;
    name: string;
    description: string;
    tags: string[];
    examples: string[];
    inputModes: string[];
    outputModes: string[];
  }>;
  defaultInputModes: string[];
  defaultOutputModes: string[];
}
```

**Example Response:**
```json
{
  "name": "Template Converter Agent",
  "description": "Transforms GitHub repositories into interactive workshop content",
  "version": "0.1.0",
  "capabilities": {
    "streaming": true,
    "pushNotifications": false,
    "stateTransitionHistory": true
  },
  "skills": [
    {
      "id": "analyze_repository_tool",
      "name": "Repository Analysis",
      "description": "Analyze GitHub repository structure and content",
      "tags": ["github", "analysis", "repository"],
      "examples": [
        "Analyze https://github.com/user/repo.git for workshop potential",
        "Examine repository structure and identify key technologies"
      ],
      "inputModes": ["text/plain"],
      "outputModes": ["text/plain"]
    }
  ],
  "defaultInputModes": ["text/plain"],
  "defaultOutputModes": ["text/plain", "text/markdown"]
}
```

**Error Responses:**
- `500 Internal Server Error`: Agent is unhealthy or not responding
- `404 Not Found`: Agent endpoint not available

#### **Send Task (POST)**
```
POST https://{agent-url}/send-task
Content-Type: application/json
Accept: application/json
```

**Request Schema:**
```typescript
interface SendTaskRequest {
  id: string;                    // Unique task identifier
  params: {
    sessionId: string;           // Session identifier for tracking
    message: {
      role: "user";
      parts: Array<{
        type: "text";
        text: string;
      }>;
    };
    acceptedOutputModes?: string[];  // Optional: defaults to agent's default
  };
}
```

**Response Schema:**
```typescript
interface SendTaskResponse {
  id: string;                    // Task ID from request
  status: "completed" | "failed" | "in_progress";
  result?: {
    content: string;             // Main response content
    metadata?: {                 // Optional metadata
      processingTime?: number;
      confidence?: number;
      sources?: string[];
    };
  };
  error?: {
    code: string;
    message: string;
    details?: any;
  };
  sessionId: string;
}
```

**Example Request:**
```json
{
  "id": "task-12345-67890",
  "params": {
    "sessionId": "workflow-session-abc123",
    "message": {
      "role": "user",
      "parts": [
        {
          "type": "text",
          "text": "Analyze https://github.com/tosin2013/healthcare-ml-genetic-predictor.git for workshop conversion using Showroom template"
        }
      ]
    },
    "acceptedOutputModes": ["text/plain", "text/markdown"]
  }
}
```

**Example Success Response:**
```json
{
  "id": "task-12345-67890",
  "status": "completed",
  "result": {
    "content": "# Repository Analysis Report\n\n**Repository**: healthcare-ml-genetic-predictor\n**Type**: Application Repository\n**Technologies**: Quarkus, Kafka, Machine Learning\n**Workshop Potential**: High\n\n## Recommended Workflow\nWorkflow 1: Repository-Based Workshop Creation\n\n## Key Components Identified\n- Quarkus REST API\n- Kafka messaging\n- ML model inference\n- Docker containerization\n\n## Workshop Structure Recommendation\n1. Introduction to Healthcare ML\n2. Quarkus Application Setup\n3. Kafka Integration\n4. ML Model Deployment\n5. Testing and Validation",
    "metadata": {
      "processingTime": 15.2,
      "confidence": 0.92,
      "sources": [
        "https://github.com/tosin2013/healthcare-ml-genetic-predictor.git",
        "https://quarkus.io/guides/",
        "https://kafka.apache.org/documentation/"
      ]
    }
  },
  "sessionId": "workflow-session-abc123"
}
```

**Example Error Response:**
```json
{
  "id": "task-12345-67890",
  "status": "failed",
  "error": {
    "code": "REPOSITORY_ACCESS_ERROR",
    "message": "Unable to access repository: https://github.com/invalid/repo.git",
    "details": {
      "httpStatus": 404,
      "gitError": "Repository not found"
    }
  },
  "sessionId": "workflow-session-abc123"
}
```

**HTTP Status Codes:**
- `200 OK`: Task completed successfully
- `202 Accepted`: Task accepted and processing (for async operations)
- `400 Bad Request`: Invalid request format or parameters
- `401 Unauthorized`: Authentication required
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Agent processing error
- `503 Service Unavailable`: Agent temporarily unavailable

## 🔧 Infrastructure API Endpoints

### **Gitea Git Server**
- **URL**: `https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`
- **API Base**: `https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/api/v1`
- **Admin User**: `opentlc-mgr`
- **Organization**: `workshop-system`

#### **Gitea API Endpoints**

**Base URL**: `https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/api/v1`

##### **1. List Organization Repositories**
```
GET /orgs/workshop-system/repos
Authorization: Basic {base64(username:password)}
```

**Response Schema:**
```typescript
interface Repository {
  id: number;
  name: string;
  full_name: string;
  description: string;
  private: boolean;
  fork: boolean;
  html_url: string;
  clone_url: string;
  ssh_url: string;
  default_branch: string;
  created_at: string;
  updated_at: string;
  size: number;
  language: string;
  archived: boolean;
  empty: boolean;
}

type RepositoryList = Repository[];
```

##### **2. Create Repository**
```
POST /orgs/workshop-system/repos
Authorization: Basic {base64(username:password)}
Content-Type: application/json

{
  "name": "workshop-name",
  "description": "Workshop description",
  "private": false,
  "auto_init": true,
  "default_branch": "main",
  "readme": "Default"
}
```

**Request Schema:**
```typescript
interface CreateRepositoryRequest {
  name: string;
  description?: string;
  private?: boolean;
  auto_init?: boolean;
  default_branch?: string;
  readme?: "Default" | "None";
  gitignores?: string;
  license?: string;
  issue_labels?: string;
}
```

##### **3. Get Repository**
```
GET /repos/workshop-system/{repo-name}
Authorization: Basic {base64(username:password)}
```

**Response**: Same as Repository schema above

##### **4. Create File**
```
POST /repos/workshop-system/{repo-name}/contents/{file-path}
Authorization: Basic {base64(username:password)}
Content-Type: application/json

{
  "message": "Commit message",
  "content": "{base64-encoded-content}",
  "branch": "main"
}
```

**Request Schema:**
```typescript
interface CreateFileRequest {
  message: string;
  content: string;        // Base64 encoded file content
  branch?: string;        // Default: repository default branch
  author?: {
    name: string;
    email: string;
  };
  committer?: {
    name: string;
    email: string;
  };
}
```

**Response Schema:**
```typescript
interface FileResponse {
  content: {
    name: string;
    path: string;
    sha: string;
    size: number;
    url: string;
    html_url: string;
    git_url: string;
    download_url: string;
    type: "file";
    encoding: "base64";
    content: string;
  };
  commit: {
    sha: string;
    url: string;
    author: {
      name: string;
      email: string;
      date: string;
    };
    committer: {
      name: string;
      email: string;
      date: string;
    };
    message: string;
  };
}
```

##### **5. Update File**
```
PUT /repos/workshop-system/{repo-name}/contents/{file-path}
Authorization: Basic {base64(username:password)}
Content-Type: application/json

{
  "message": "Update commit message",
  "content": "{base64-encoded-content}",
  "sha": "{current-file-sha}",
  "branch": "main"
}
```

**Request Schema:**
```typescript
interface UpdateFileRequest extends CreateFileRequest {
  sha: string;           // SHA of the file being updated
}
```

##### **6. Create Webhook**
```
POST /repos/workshop-system/{repo-name}/hooks
Authorization: Basic {base64(username:password)}
Content-Type: application/json

{
  "type": "gitea",
  "config": {
    "url": "https://webhook-endpoint.com/webhook",
    "content_type": "json",
    "secret": "webhook-secret"
  },
  "events": ["push", "pull_request"],
  "active": true
}
```

**Request Schema:**
```typescript
interface CreateWebhookRequest {
  type: "gitea" | "gogs" | "slack" | "discord";
  config: {
    url: string;
    content_type: "json" | "form";
    secret?: string;
    username?: string;
    password?: string;
  };
  events: string[];      // e.g., ["push", "pull_request", "issues"]
  active: boolean;
}
```

##### **7. Get Commits**
```
GET /repos/workshop-system/{repo-name}/commits
Authorization: Basic {base64(username:password)}
Query Parameters:
  - sha: {branch-name} (optional)
  - path: {file-path} (optional)
  - page: {page-number} (optional)
  - limit: {per-page} (optional, max 100)
```

**Response Schema:**
```typescript
interface Commit {
  sha: string;
  commit: {
    author: {
      name: string;
      email: string;
      date: string;
    };
    committer: {
      name: string;
      email: string;
      date: string;
    };
    message: string;
  };
  author: {
    id: number;
    login: string;
    full_name: string;
    email: string;
    avatar_url: string;
  };
  committer: {
    id: number;
    login: string;
    full_name: string;
    email: string;
    avatar_url: string;
  };
  parents: Array<{
    sha: string;
    url: string;
  }>;
}

type CommitList = Commit[];
```

### **OpenShift Kubernetes API**
- **API Server**: `https://api.cluster-9cfzr.9cfzr.sandbox180.opentlc.com:6443`
- **Namespace**: `workshop-system`
- **Authentication**: Service Account with RBAC permissions

#### **OpenShift API Endpoints**

**Base URL**: `https://api.cluster-9cfzr.9cfzr.sandbox180.opentlc.com:6443`

##### **1. Get Builds**
```
GET /apis/build.openshift.io/v1/namespaces/workshop-system/builds
Authorization: Bearer {service-account-token}
```

**Response Schema:**
```typescript
interface BuildList {
  kind: "BuildList";
  items: Array<{
    metadata: {
      name: string;
      namespace: string;
      creationTimestamp: string;
      labels?: Record<string, string>;
    };
    spec: {
      source: {
        type: "Git";
        git: {
          uri: string;
          ref: string;
        };
      };
      strategy: {
        type: "Source" | "Docker";
      };
    };
    status: {
      phase: "New" | "Pending" | "Running" | "Complete" | "Failed" | "Error" | "Cancelled";
      startTimestamp?: string;
      completionTimestamp?: string;
      duration?: number;
      outputDockerImageReference?: string;
    };
  }>;
}
```

##### **2. Get Build Logs**
```
GET /apis/build.openshift.io/v1/namespaces/workshop-system/builds/{build-name}/log
Authorization: Bearer {service-account-token}
```

**Response**: Plain text build logs

##### **3. Trigger Build**
```
POST /apis/build.openshift.io/v1/namespaces/workshop-system/buildconfigs/{buildconfig-name}/instantiate
Authorization: Bearer {service-account-token}
Content-Type: application/json

{
  "kind": "BuildRequest",
  "apiVersion": "build.openshift.io/v1",
  "metadata": {
    "name": "{buildconfig-name}"
  }
}
```

##### **4. Get Deployments**
```
GET /apis/apps/v1/namespaces/workshop-system/deployments
Authorization: Bearer {service-account-token}
```

**Response Schema:**
```typescript
interface DeploymentList {
  kind: "DeploymentList";
  items: Array<{
    metadata: {
      name: string;
      namespace: string;
      labels?: Record<string, string>;
    };
    spec: {
      replicas: number;
      selector: {
        matchLabels: Record<string, string>;
      };
    };
    status: {
      replicas?: number;
      readyReplicas?: number;
      availableReplicas?: number;
      conditions?: Array<{
        type: string;
        status: "True" | "False" | "Unknown";
        reason?: string;
        message?: string;
      }>;
    };
  }>;
}
```

##### **5. Get Pods**
```
GET /api/v1/namespaces/workshop-system/pods
Authorization: Bearer {service-account-token}
```

**Response Schema:**
```typescript
interface PodList {
  kind: "PodList";
  items: Array<{
    metadata: {
      name: string;
      namespace: string;
      labels?: Record<string, string>;
    };
    spec: {
      containers: Array<{
        name: string;
        image: string;
        ports?: Array<{
          containerPort: number;
          name?: string;
        }>;
      }>;
    };
    status: {
      phase: "Pending" | "Running" | "Succeeded" | "Failed" | "Unknown";
      conditions?: Array<{
        type: string;
        status: "True" | "False" | "Unknown";
      }>;
      containerStatuses?: Array<{
        name: string;
        ready: boolean;
        restartCount: number;
        state?: {
          running?: { startedAt: string };
          waiting?: { reason: string; message?: string };
          terminated?: { reason: string; exitCode: number };
        };
      }>;
    };
  }>;
}
```

##### **6. Get Pod Logs**
```
GET /api/v1/namespaces/workshop-system/pods/{pod-name}/log
Authorization: Bearer {service-account-token}
Query Parameters:
  - container: {container-name} (optional)
  - follow: true (for streaming logs)
  - tailLines: {number} (limit log lines)
```

##### **7. Get Routes**
```
GET /apis/route.openshift.io/v1/namespaces/workshop-system/routes
Authorization: Bearer {service-account-token}
```

**Response Schema:**
```typescript
interface RouteList {
  kind: "RouteList";
  items: Array<{
    metadata: {
      name: string;
      namespace: string;
    };
    spec: {
      host: string;
      to: {
        kind: "Service";
        name: string;
      };
      port?: {
        targetPort: string | number;
      };
      tls?: {
        termination: "edge" | "passthrough" | "reencrypt";
      };
    };
    status: {
      ingress?: Array<{
        host: string;
        conditions?: Array<{
          type: string;
          status: "True" | "False";
        }>;
      }>;
    };
  }>;
}
```

### **Workshop Content Routes**
- **Healthcare ML Workshop**: `https://healthcare-ml.apps.cluster.local` ⚠️ *Placeholder domain - needs cluster domain update*
- **OpenShift Bare Metal Workshop**: `https://openshift-baremetal.apps.cluster.local` ⚠️ *Placeholder domain - needs cluster domain update*

**Note**: These workshop routes are currently configured with placeholder domains. They should be updated to use the actual cluster domain:
- Expected: `https://healthcare-ml-workshop-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`
- Expected: `https://openshift-baremetal-workshop-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`

## 🔑 Authentication & Authorization

### **Gitea Authentication**
```javascript
// Retrieve Gitea admin password from OpenShift
const giteaPassword = await k8sApi.readNamespacedSecret(
  'gitea-admin-password', 
  'gitea'
);

// Use for Gitea API calls
const giteaAuth = {
  username: 'opentlc-mgr',
  password: giteaPassword.data.password
};
```

### **OpenShift Authentication**
```javascript
// Use service account token for OpenShift API
const serviceAccountToken = process.env.OPENSHIFT_TOKEN;
const k8sConfig = {
  url: 'https://api.cluster-9cfzr.9cfzr.sandbox180.opentlc.com:6443',
  auth: {
    bearer: serviceAccountToken
  }
};
```

## 📋 Example API Calls

### **1. Get Agent Capabilities**
```javascript
// Get Template Converter Agent capabilities
const response = await fetch(
  'https://template-converter-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/agent-card'
);
const agentCard = await response.json();
```

### **2. Start Repository Analysis**
```javascript
// Send repository analysis task to Template Converter Agent
const analysisTask = {
  id: 'repo-analysis-' + Date.now(),
  params: {
    sessionId: 'workflow-session-1',
    message: {
      role: 'user',
      parts: [{
        type: 'text',
        text: 'Analyze https://github.com/tosin2013/healthcare-ml-genetic-predictor.git for workshop conversion'
      }]
    },
    acceptedOutputModes: ['text/plain']
  }
};

const response = await fetch(
  'https://template-converter-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/send-task',
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(analysisTask)
  }
);
```

### **3. Monitor OpenShift Builds**
```javascript
// Get build status from OpenShift API
const builds = await k8sApi.listNamespacedBuild('workshop-system');
const buildStatus = builds.body.items.map(build => ({
  name: build.metadata.name,
  status: build.status.phase,
  startTime: build.status.startTimestamp
}));
```

### **4. Create Gitea Repository**
```javascript
// Create workshop repository in Gitea
const repoData = {
  name: 'healthcare-ml-workshop',
  description: 'Healthcare ML Workshop - Generated by Workshop Template System',
  private: false,
  auto_init: true
};

const response = await fetch(
  'https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/api/v1/orgs/workshop-system/repos',
  {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Basic ${btoa('opentlc-mgr:' + giteaPassword)}`
    },
    body: JSON.stringify(repoData)
  }
);
```

## 🚀 Workflow Integration Examples

### **Workflow 1: Repository-Based Workshop Creation**
```javascript
// 1. Analyze repository
const analysis = await sendTaskToAgent('template-converter', {
  text: 'Analyze https://github.com/tosin2013/healthcare-ml-genetic-predictor.git'
});

// 2. Generate content
const content = await sendTaskToAgent('content-creator', {
  text: 'Create workshop content based on analysis results'
});

// 3. Deploy to OpenShift
const deployment = await sendTaskToAgent('source-manager', {
  text: 'Deploy healthcare-ml-workshop to OpenShift'
});
```

### **Workflow 3: Workshop Enhancement**
```javascript
// 1. Analyze existing workshop
const analysis = await sendTaskToAgent('template-converter', {
  text: 'Analyze existing workshop from Gitea repository'
});

// 2. Research improvements
const research = await sendTaskToAgent('research-validation', {
  text: 'Research latest best practices for workshop enhancement'
});

// 3. Generate enhancements
const enhancements = await sendTaskToAgent('content-creator', {
  text: 'Generate workshop enhancements based on research'
});
```

## 🔍 Health Check Endpoints

All agents support health checks via their `/agent-card` endpoint. A successful response indicates the agent is healthy and ready to receive tasks.

```javascript
// Health check function
async function checkAgentHealth(agentUrl) {
  try {
    const response = await fetch(`${agentUrl}/agent-card`);
    return response.ok;
  } catch (error) {
    return false;
  }
}
```

## 📊 Real-time Monitoring

For real-time updates, implement WebSocket connections to monitor:
- Agent task progress
- OpenShift build status
- Deployment health
- Workshop availability

## 🛠️ Development Notes

1. **HTTPS Only**: All endpoints use HTTPS with self-signed certificates in development
2. **CORS**: Configure CORS settings for cross-origin requests
3. **Rate Limiting**: Implement appropriate rate limiting for API calls
4. **Error Handling**: All APIs may return errors - implement proper error handling
5. **Timeouts**: Set appropriate timeouts for long-running operations

## ✅ Configuration Management

### **Gitea Integration Configuration**
The Source Manager Agent now uses proper ConfigMaps and Secrets for Gitea integration:

**ConfigMap: `gitea-config`**
```yaml
GITEA_URL: "https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
GITEA_API_URL: "https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/api/v1"
GITEA_ORGANIZATION: "workshop-system"
GITEA_USERNAME: "opentlc-mgr"
```

**Secret: `gitea-credentials`**
```yaml
GITEA_TOKEN: "5064d47a5fdb598395a4eb57d3253c394467ca6c" (base64 encoded)
GITEA_PASSWORD: "[Retrieved dynamically from OpenShift]"
```

### **Credential Management**
Use the provided script to update credentials:

```bash
# Update Gitea credentials automatically
./scripts/update-gitea-credentials.sh
```

This script:
1. Retrieves current Gitea admin password from OpenShift
2. Updates the `gitea-credentials` Secret
3. Restarts Source Manager Agent
4. Tests API connectivity

### **Repository Conflict Resolution**
The "repository already exists" error is now handled by the Source Manager Agent through proper configuration. The agent will:

1. **Check if repository exists** before attempting creation
2. **Use existing repository** if it already exists
3. **Update content** rather than failing on conflicts

### **Dynamic Domain Resolution**
For production deployments, the system now supports dynamic cluster domain resolution:

```javascript
// Get cluster domain from OpenShift API
const clusterDomain = await getClusterDomain();
const workshopUrl = `https://healthcare-ml-workshop-workshop-system.${clusterDomain}`;
```

---

**Note**: Replace cluster domain `apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com` with your actual OpenShift cluster domain if different.
