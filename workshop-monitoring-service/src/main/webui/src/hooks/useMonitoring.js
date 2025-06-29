import { useState, useEffect, useCallback } from 'react';
import { MonitoringApiService } from '../services/monitoringApi';

/**
 * Custom hook for managing monitoring data and state
 * Provides real-time updates and error handling for the monitoring dashboard
 */
export const useMonitoring = (refreshInterval = 30000) => {
  const [systemHealth, setSystemHealth] = useState(null);
  const [agentStatuses, setAgentStatuses] = useState([]);
  const [systemSummary, setSystemSummary] = useState(null);
  const [serviceInfo, setServiceInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);

  /**
   * Fetch all monitoring data
   */
  const fetchMonitoringData = useCallback(async () => {
    try {
      setError(null);
      
      // Fetch all data in parallel
      const [healthData, agentsData, summaryData, infoData] = await Promise.all([
        MonitoringApiService.getSystemHealth(),
        MonitoringApiService.getAllAgentStatus(),
        MonitoringApiService.getSystemSummary(),
        MonitoringApiService.getServiceInfo()
      ]);

      setSystemHealth(healthData);
      setAgentStatuses(agentsData);
      setSystemSummary(summaryData);
      setServiceInfo(infoData);
      setLastUpdated(new Date());
      
    } catch (err) {
      console.error('Failed to fetch monitoring data:', err);
      setError(err.message || 'Failed to fetch monitoring data');
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Trigger manual health check
   */
  const triggerHealthCheck = useCallback(async () => {
    try {
      setError(null);
      await MonitoringApiService.triggerHealthCheck();
      
      // Refresh data after triggering health check
      setTimeout(() => {
        fetchMonitoringData();
      }, 2000); // Wait 2 seconds for health check to complete
      
      return { success: true, message: 'Health check triggered successfully' };
    } catch (err) {
      console.error('Failed to trigger health check:', err);
      setError(err.message || 'Failed to trigger health check');
      return { success: false, message: err.message };
    }
  }, [fetchMonitoringData]);

  /**
   * Get status of a specific agent
   */
  const getAgentStatus = useCallback(async (agentName) => {
    try {
      const agentData = await MonitoringApiService.getAgentStatus(agentName);
      return agentData;
    } catch (err) {
      console.error(`Failed to fetch status for agent ${agentName}:`, err);
      throw err;
    }
  }, []);

  /**
   * Refresh monitoring data manually
   */
  const refreshData = useCallback(() => {
    setLoading(true);
    fetchMonitoringData();
  }, [fetchMonitoringData]);

  // Initial data fetch
  useEffect(() => {
    fetchMonitoringData();
  }, [fetchMonitoringData]);

  // Set up automatic refresh interval
  useEffect(() => {
    if (refreshInterval > 0) {
      const interval = setInterval(() => {
        fetchMonitoringData();
      }, refreshInterval);

      return () => clearInterval(interval);
    }
  }, [fetchMonitoringData, refreshInterval]);

  // Derived state for dashboard metrics
  const dashboardMetrics = {
    totalAgents: systemSummary?.total_agents || 0,
    healthyAgents: systemSummary?.healthy_agents || 0,
    degradedAgents: systemSummary?.degraded_agents || 0,
    unhealthyAgents: systemSummary?.unhealthy_agents || 0,
    unknownAgents: systemSummary?.unknown_agents || 0,
    overallStatus: systemSummary?.overall_status || 'UNKNOWN',
    healthPercentage: systemSummary?.total_agents > 0 
      ? Math.round((systemSummary.healthy_agents / systemSummary.total_agents) * 100)
      : 0
  };

  // Agent status grouped by health status
  const agentsByStatus = {
    healthy: agentStatuses.filter(agent => agent.health === 'HEALTHY'),
    degraded: agentStatuses.filter(agent => agent.health === 'DEGRADED'),
    unhealthy: agentStatuses.filter(agent => agent.health === 'UNHEALTHY'),
    unknown: agentStatuses.filter(agent => agent.health === 'UNKNOWN')
  };

  // Response time statistics
  const responseTimeStats = {
    average: agentStatuses.length > 0 
      ? Math.round(agentStatuses.reduce((sum, agent) => sum + (agent.responseTimeMs || 0), 0) / agentStatuses.length)
      : 0,
    min: agentStatuses.length > 0 
      ? Math.min(...agentStatuses.map(agent => agent.responseTimeMs || 0))
      : 0,
    max: agentStatuses.length > 0 
      ? Math.max(...agentStatuses.map(agent => agent.responseTimeMs || 0))
      : 0
  };

  return {
    // Raw data
    systemHealth,
    agentStatuses,
    systemSummary,
    serviceInfo,
    
    // State management
    loading,
    error,
    lastUpdated,
    
    // Actions
    refreshData,
    triggerHealthCheck,
    getAgentStatus,
    
    // Derived data for dashboard
    dashboardMetrics,
    agentsByStatus,
    responseTimeStats
  };
};

export default useMonitoring;
