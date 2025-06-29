import React, { useState } from 'react';
import { 
  RefreshCw, 
  Activity, 
  TrendingUp, 
  Clock, 
  AlertTriangle,
  CheckCircle,
  XCircle,
  GitBranch,
  BarChart3,
  Settings,
  Info,
  Tabs
} from 'lucide-react';
import { useMonitoring } from '../hooks/useMonitoring';
import useEvolution from '../hooks/useEvolution';
import { getHealthStatusColor, getHealthStatusIcon } from '../services/monitoringApi';
import SystemHealthCard from './SystemHealthCard';
import EvolutionQueue from './EvolutionQueue';
import WorkshopVersionHistory from './WorkshopVersionHistory';
import EvolutionMetrics from './EvolutionMetrics';
import ServiceInfoPanel from './ServiceInfoPanel';

/**
 * Evolution Dashboard Component - Enhanced dashboard with evolution management
 * Extends the existing monitoring dashboard with evolution tracking capabilities
 */
const EvolutionDashboard = () => {
  const {
    systemHealth,
    serviceInfo,
    loading: monitoringLoading,
    error: monitoringError,
    lastUpdated,
    refreshData: refreshMonitoring,
    triggerHealthCheck,
    dashboardMetrics
  } = useMonitoring(30000); // Refresh every 30 seconds

  const {
    evolutions,
    selectedEvolution,
    loading: evolutionLoading,
    error: evolutionError,
    refresh: refreshEvolutions,
    setSelectedEvolution,
    activeEvolutionsCount,
    inProgressEvolutions,
    completedEvolutions,
    failedEvolutions
  } = useEvolution();

  const [isRefreshing, setIsRefreshing] = useState(false);
  const [showServiceInfo, setShowServiceInfo] = useState(false);
  const [activeTab, setActiveTab] = useState('overview'); // overview, queue, history, metrics
  const [selectedWorkshop, setSelectedWorkshop] = useState(null);

  const loading = monitoringLoading || evolutionLoading;
  const error = monitoringError || evolutionError;

  /**
   * Handle manual refresh
   */
  const handleRefresh = async () => {
    setIsRefreshing(true);
    refreshMonitoring();
    refreshEvolutions();
    setTimeout(() => setIsRefreshing(false), 1000);
  };

  /**
   * Handle evolution selection
   */
  const handleEvolutionSelect = (evolution) => {
    setSelectedEvolution(evolution);
    setSelectedWorkshop(evolution.workshop_name);
    setActiveTab('history');
  };

  /**
   * Handle tab change
   */
  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  if (loading && !systemHealth) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading evolution dashboard...</p>
        </div>
      </div>
    );
  }

  if (error && !systemHealth) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <XCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Dashboard Error</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={handleRefresh}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            Retry
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
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <GitBranch className="h-8 w-8 text-blue-600" />
                <h1 className="text-2xl font-bold text-gray-900">Evolution Dashboard</h1>
              </div>
              
              {systemHealth && (
                <div className="flex items-center space-x-2">
                  <div className={`w-3 h-3 rounded-full ${
                    systemHealth.status === 'healthy' ? 'bg-green-500' : 
                    systemHealth.status === 'degraded' ? 'bg-yellow-500' : 'bg-red-500'
                  }`}></div>
                  <span className="text-sm text-gray-600 capitalize">
                    {systemHealth.status}
                  </span>
                </div>
              )}
            </div>

            <div className="flex items-center space-x-2">
              <button
                onClick={() => setShowServiceInfo(true)}
                className="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                title="Service Information"
              >
                <Info size={20} />
              </button>
              
              <button
                onClick={triggerHealthCheck}
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

        {/* Evolution Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <Activity className="h-8 w-8 text-blue-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Active Evolutions</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {activeEvolutionsCount}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <Clock className="h-8 w-8 text-orange-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">In Progress</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {inProgressEvolutions.length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <CheckCircle className="h-8 w-8 text-green-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Completed</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {completedEvolutions.length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <XCircle className="h-8 w-8 text-red-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Failed</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {failedEvolutions.length}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="mb-8">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              {[
                { id: 'overview', label: 'Overview', icon: BarChart3 },
                { id: 'queue', label: 'Evolution Queue', icon: Activity },
                { id: 'history', label: 'Version History', icon: GitBranch },
                { id: 'metrics', label: 'Metrics', icon: TrendingUp }
              ].map((tab) => {
                const Icon = tab.icon;
                return (
                  <button
                    key={tab.id}
                    onClick={() => handleTabChange(tab.id)}
                    className={`flex items-center space-x-2 py-2 px-1 border-b-2 font-medium text-sm ${
                      activeTab === tab.id
                        ? 'border-blue-500 text-blue-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                    }`}
                  >
                    <Icon className="h-4 w-4" />
                    <span>{tab.label}</span>
                  </button>
                );
              })}
            </nav>
          </div>
        </div>

        {/* Tab Content */}
        <div className="space-y-8">
          {activeTab === 'overview' && (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <EvolutionQueue 
                onEvolutionSelect={handleEvolutionSelect}
                refreshTrigger={isRefreshing}
              />
              <EvolutionMetrics timeRange="7d" />
            </div>
          )}

          {activeTab === 'queue' && (
            <EvolutionQueue 
              onEvolutionSelect={handleEvolutionSelect}
              refreshTrigger={isRefreshing}
            />
          )}

          {activeTab === 'history' && (
            <WorkshopVersionHistory 
              workshopName={selectedWorkshop}
              selectedEvolution={selectedEvolution}
            />
          )}

          {activeTab === 'metrics' && (
            <EvolutionMetrics timeRange="30d" />
          )}
        </div>

        {/* Footer */}
        <footer className="text-center text-sm text-gray-500 mt-12">
          <p>
            Last updated: {lastUpdated ? lastUpdated.toLocaleString() : 'Never'}
            {' • '}
            Auto-refresh: 30 seconds
            {' • '}
            Evolution Dashboard v1.0
          </p>
        </footer>
      </main>
    </div>
  );
};

export default EvolutionDashboard;
