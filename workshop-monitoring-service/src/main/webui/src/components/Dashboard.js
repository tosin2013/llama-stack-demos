import React, { useState } from 'react';
import { 
  RefreshCw, 
  Activity, 
  Server, 
  Clock, 
  AlertTriangle,
  CheckCircle,
  XCircle,
  HelpCircle,
  Settings,
  Info
} from 'lucide-react';
import { useMonitoring } from '../hooks/useMonitoring';
import { getHealthStatusColor, getHealthStatusIcon } from '../services/monitoringApi';
import SystemHealthCard from './SystemHealthCard';
import AgentStatusGrid from './AgentStatusGrid';
import ResponseTimeChart from './ResponseTimeChart';
import ServiceInfoPanel from './ServiceInfoPanel';

/**
 * Main Dashboard Component
 * Displays comprehensive monitoring information for the Workshop Template System
 */
const Dashboard = () => {
  const {
    systemHealth,
    agentStatuses,
    systemSummary,
    serviceInfo,
    loading,
    error,
    lastUpdated,
    refreshData,
    triggerHealthCheck,
    dashboardMetrics,
    agentsByStatus,
    responseTimeStats
  } = useMonitoring(30000); // Refresh every 30 seconds

  const [isRefreshing, setIsRefreshing] = useState(false);
  const [showServiceInfo, setShowServiceInfo] = useState(false);

  /**
   * Handle manual refresh
   */
  const handleRefresh = async () => {
    setIsRefreshing(true);
    refreshData();
    setTimeout(() => setIsRefreshing(false), 1000);
  };

  /**
   * Handle manual health check trigger
   */
  const handleHealthCheck = async () => {
    const result = await triggerHealthCheck();
    if (result.success) {
      // Show success notification (could be replaced with toast notification)
      console.log('Health check triggered successfully');
    }
  };

  /**
   * Get status icon component
   */
  const getStatusIcon = (status, size = 20) => {
    const color = getHealthStatusColor(status);
    const iconName = getHealthStatusIcon(status);
    
    const iconProps = { size, color };
    
    switch (iconName) {
      case 'CheckCircle':
        return <CheckCircle {...iconProps} />;
      case 'AlertTriangle':
        return <AlertTriangle {...iconProps} />;
      case 'XCircle':
        return <XCircle {...iconProps} />;
      case 'HelpCircle':
      default:
        return <HelpCircle {...iconProps} />;
    }
  };

  if (loading && !systemHealth) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading monitoring dashboard...</p>
        </div>
      </div>
    );
  }

  if (error && !systemHealth) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <XCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Connection Error</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={handleRefresh}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            Retry Connection
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Activity className="h-8 w-8 text-blue-600 mr-3" />
              <div>
                <h1 className="text-xl font-semibold text-gray-900">
                  Workshop Monitoring Dashboard
                </h1>
                <p className="text-sm text-gray-500">
                  Template System Health & Status
                </p>
              </div>
            </div>
            
            <div className="flex items-center space-x-4">
              {/* Overall Status Indicator */}
              <div className="flex items-center space-x-2">
                {getStatusIcon(dashboardMetrics.overallStatus)}
                <span className="text-sm font-medium text-gray-700">
                  {dashboardMetrics.overallStatus}
                </span>
              </div>
              
              {/* Action Buttons */}
              <button
                onClick={() => setShowServiceInfo(!showServiceInfo)}
                className="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                title="Service Information"
              >
                <Info size={20} />
              </button>
              
              <button
                onClick={handleHealthCheck}
                className="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                title="Trigger Health Check"
              >
                <Settings size={20} />
              </button>
              
              <button
                onClick={handleRefresh}
                disabled={isRefreshing}
                className="p-2 text-gray-400 hover:text-gray-600 transition-colors disabled:opacity-50"
                title="Refresh Data"
              >
                <RefreshCw size={20} className={isRefreshing ? 'animate-spin' : ''} />
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Service Info Panel */}
        {showServiceInfo && (
          <ServiceInfoPanel 
            serviceInfo={serviceInfo}
            onClose={() => setShowServiceInfo(false)}
          />
        )}

        {/* Error Banner */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
            <div className="flex items-center">
              <AlertTriangle className="h-5 w-5 text-red-500 mr-2" />
              <p className="text-red-700">{error}</p>
            </div>
          </div>
        )}

        {/* System Health Overview */}
        <div className="mb-8">
          <SystemHealthCard 
            systemHealth={systemHealth}
            dashboardMetrics={dashboardMetrics}
            lastUpdated={lastUpdated}
          />
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <Server className="h-8 w-8 text-blue-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Total Agents</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {dashboardMetrics.totalAgents}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <CheckCircle className="h-8 w-8 text-green-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Healthy</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {dashboardMetrics.healthyAgents}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <Clock className="h-8 w-8 text-purple-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Avg Response</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {responseTimeStats.average}ms
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <Activity className="h-8 w-8 text-indigo-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Health Score</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {dashboardMetrics.healthPercentage}%
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Agent Status Grid */}
        <div className="mb-8">
          <AgentStatusGrid 
            agentStatuses={agentStatuses}
            agentsByStatus={agentsByStatus}
          />
        </div>

        {/* Response Time Chart */}
        <div className="mb-8">
          <ResponseTimeChart 
            agentStatuses={agentStatuses}
            responseTimeStats={responseTimeStats}
          />
        </div>

        {/* Footer */}
        <footer className="text-center text-sm text-gray-500 mt-12">
          <p>
            Last updated: {lastUpdated ? lastUpdated.toLocaleString() : 'Never'}
            {' • '}
            Auto-refresh: 30 seconds
          </p>
        </footer>
      </main>
    </div>
  );
};

export default Dashboard;
