import React from 'react';
import { 
  CheckCircle, 
  AlertTriangle, 
  XCircle, 
  HelpCircle,
  TrendingUp,
  Clock
} from 'lucide-react';
import { getHealthStatusColor } from '../services/monitoringApi';

/**
 * System Health Card Component
 * Displays overall system health status and key metrics
 */
const SystemHealthCard = ({ systemHealth, dashboardMetrics, lastUpdated }) => {
  if (!systemHealth) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="animate-pulse">
          <div className="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
          <div className="h-8 bg-gray-200 rounded w-1/2 mb-2"></div>
          <div className="h-4 bg-gray-200 rounded w-3/4"></div>
        </div>
      </div>
    );
  }

  const getStatusIcon = (status, size = 24) => {
    const color = getHealthStatusColor(status);
    const iconProps = { size, color };
    
    switch (status) {
      case 'HEALTHY':
        return <CheckCircle {...iconProps} />;
      case 'DEGRADED':
        return <AlertTriangle {...iconProps} />;
      case 'UNHEALTHY':
        return <XCircle {...iconProps} />;
      case 'UNKNOWN':
      default:
        return <HelpCircle {...iconProps} />;
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'HEALTHY':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'DEGRADED':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'UNHEALTHY':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'UNKNOWN':
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusMessage = (status, metrics) => {
    switch (status) {
      case 'HEALTHY':
        return `All ${metrics.totalAgents} agents are operating normally`;
      case 'DEGRADED':
        return `${metrics.degradedAgents + metrics.unknownAgents} agent(s) experiencing issues`;
      case 'UNHEALTHY':
        return `${metrics.unhealthyAgents} agent(s) are unhealthy`;
      case 'UNKNOWN':
      default:
        return 'System status is being determined';
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-lg border border-gray-200">
      <div className="p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-3">
            {getStatusIcon(dashboardMetrics.overallStatus, 32)}
            <div>
              <h2 className="text-2xl font-bold text-gray-900">
                System Health
              </h2>
              <p className="text-gray-600">
                Workshop Template System Status
              </p>
            </div>
          </div>
          
          <div className="text-right">
            <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${getStatusBadgeClass(dashboardMetrics.overallStatus)}`}>
              {dashboardMetrics.overallStatus}
            </span>
            {lastUpdated && (
              <p className="text-xs text-gray-500 mt-1 flex items-center">
                <Clock size={12} className="mr-1" />
                {lastUpdated.toLocaleTimeString()}
              </p>
            )}
          </div>
        </div>

        {/* Status Message */}
        <div className="mb-6">
          <p className="text-lg text-gray-700">
            {getStatusMessage(dashboardMetrics.overallStatus, dashboardMetrics)}
          </p>
        </div>

        {/* Health Metrics Grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <div className="text-center p-4 bg-green-50 rounded-lg border border-green-200">
            <div className="flex items-center justify-center mb-2">
              <CheckCircle size={20} className="text-green-600" />
            </div>
            <p className="text-2xl font-bold text-green-700">
              {dashboardMetrics.healthyAgents}
            </p>
            <p className="text-sm text-green-600">Healthy</p>
          </div>

          <div className="text-center p-4 bg-yellow-50 rounded-lg border border-yellow-200">
            <div className="flex items-center justify-center mb-2">
              <AlertTriangle size={20} className="text-yellow-600" />
            </div>
            <p className="text-2xl font-bold text-yellow-700">
              {dashboardMetrics.degradedAgents}
            </p>
            <p className="text-sm text-yellow-600">Degraded</p>
          </div>

          <div className="text-center p-4 bg-red-50 rounded-lg border border-red-200">
            <div className="flex items-center justify-center mb-2">
              <XCircle size={20} className="text-red-600" />
            </div>
            <p className="text-2xl font-bold text-red-700">
              {dashboardMetrics.unhealthyAgents}
            </p>
            <p className="text-sm text-red-600">Unhealthy</p>
          </div>

          <div className="text-center p-4 bg-gray-50 rounded-lg border border-gray-200">
            <div className="flex items-center justify-center mb-2">
              <HelpCircle size={20} className="text-gray-600" />
            </div>
            <p className="text-2xl font-bold text-gray-700">
              {dashboardMetrics.unknownAgents}
            </p>
            <p className="text-sm text-gray-600">Unknown</p>
          </div>
        </div>

        {/* Health Score Progress Bar */}
        <div className="mb-4">
          <div className="flex items-center justify-between mb-2">
            <span className="text-sm font-medium text-gray-700">
              Overall Health Score
            </span>
            <span className="text-sm font-bold text-gray-900">
              {dashboardMetrics.healthPercentage}%
            </span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-3">
            <div 
              className={`h-3 rounded-full transition-all duration-500 ${
                dashboardMetrics.healthPercentage >= 80 
                  ? 'bg-green-500' 
                  : dashboardMetrics.healthPercentage >= 60 
                    ? 'bg-yellow-500' 
                    : 'bg-red-500'
              }`}
              style={{ width: `${dashboardMetrics.healthPercentage}%` }}
            ></div>
          </div>
        </div>

        {/* Additional System Info */}
        {systemHealth.activeIssues && systemHealth.activeIssues.length > 0 && (
          <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg">
            <h4 className="text-sm font-medium text-red-800 mb-2">Active Issues:</h4>
            <ul className="text-sm text-red-700 space-y-1">
              {systemHealth.activeIssues.map((issue, index) => (
                <li key={index} className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>{issue}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        {/* Performance Indicator */}
        <div className="mt-4 flex items-center justify-between text-sm text-gray-600">
          <div className="flex items-center">
            <TrendingUp size={16} className="mr-1" />
            <span>System Performance</span>
          </div>
          <span className={`font-medium ${
            dashboardMetrics.healthPercentage >= 90 
              ? 'text-green-600' 
              : dashboardMetrics.healthPercentage >= 70 
                ? 'text-yellow-600' 
                : 'text-red-600'
          }`}>
            {dashboardMetrics.healthPercentage >= 90 
              ? 'Excellent' 
              : dashboardMetrics.healthPercentage >= 70 
                ? 'Good' 
                : 'Needs Attention'}
          </span>
        </div>
      </div>
    </div>
  );
};

export default SystemHealthCard;
