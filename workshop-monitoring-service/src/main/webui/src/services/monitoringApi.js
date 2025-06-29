import axios from 'axios';

// Base API configuration
const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? '/api/monitoring' 
  : 'http://localhost:8086/api/monitoring';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for logging
api.interceptors.request.use(
  (config) => {
    console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('API Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => {
    console.log(`API Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('API Response Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

/**
 * Workshop Monitoring API Service
 * Provides methods to interact with the monitoring REST endpoints
 */
export class MonitoringApiService {
  
  /**
   * Get overall system health status
   * @returns {Promise<Object>} System health data
   */
  static async getSystemHealth() {
    try {
      const response = await api.get('/health');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch system health:', error);
      throw new Error('Unable to fetch system health status');
    }
  }

  /**
   * Get status of all agents
   * @returns {Promise<Array>} Array of agent status objects
   */
  static async getAllAgentStatus() {
    try {
      const response = await api.get('/agents');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch agent statuses:', error);
      throw new Error('Unable to fetch agent statuses');
    }
  }

  /**
   * Get status of a specific agent
   * @param {string} agentName - Name of the agent
   * @returns {Promise<Object>} Agent status data
   */
  static async getAgentStatus(agentName) {
    try {
      const response = await api.get(`/agents/${agentName}`);
      return response.data;
    } catch (error) {
      if (error.response?.status === 404) {
        throw new Error(`Agent '${agentName}' not found`);
      }
      console.error(`Failed to fetch status for agent ${agentName}:`, error);
      throw new Error(`Unable to fetch status for agent '${agentName}'`);
    }
  }

  /**
   * Get system summary for dashboard display
   * @returns {Promise<Object>} System summary data
   */
  static async getSystemSummary() {
    try {
      const response = await api.get('/summary');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch system summary:', error);
      throw new Error('Unable to fetch system summary');
    }
  }

  /**
   * Trigger manual health check for all agents
   * @returns {Promise<Object>} Health check trigger response
   */
  static async triggerHealthCheck() {
    try {
      const response = await api.post('/health-check');
      return response.data;
    } catch (error) {
      console.error('Failed to trigger health check:', error);
      throw new Error('Unable to trigger health check');
    }
  }

  /**
   * Get service information and metadata
   * @returns {Promise<Object>} Service info data
   */
  static async getServiceInfo() {
    try {
      const response = await api.get('/info');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch service info:', error);
      throw new Error('Unable to fetch service information');
    }
  }
}

/**
 * Health status constants matching the backend enum
 */
export const HealthStatus = {
  HEALTHY: 'HEALTHY',
  DEGRADED: 'DEGRADED',
  UNHEALTHY: 'UNHEALTHY',
  UNKNOWN: 'UNKNOWN'
};

/**
 * Get health status color for UI display
 * @param {string} status - Health status
 * @returns {string} CSS color class or hex color
 */
export const getHealthStatusColor = (status) => {
  switch (status) {
    case HealthStatus.HEALTHY:
      return '#10b981'; // green-500
    case HealthStatus.DEGRADED:
      return '#f59e0b'; // amber-500
    case HealthStatus.UNHEALTHY:
      return '#ef4444'; // red-500
    case HealthStatus.UNKNOWN:
    default:
      return '#6b7280'; // gray-500
  }
};

/**
 * Get health status icon for UI display
 * @param {string} status - Health status
 * @returns {string} Icon name (for lucide-react)
 */
export const getHealthStatusIcon = (status) => {
  switch (status) {
    case HealthStatus.HEALTHY:
      return 'CheckCircle';
    case HealthStatus.DEGRADED:
      return 'AlertTriangle';
    case HealthStatus.UNHEALTHY:
      return 'XCircle';
    case HealthStatus.UNKNOWN:
    default:
      return 'HelpCircle';
  }
};

export default MonitoringApiService;
