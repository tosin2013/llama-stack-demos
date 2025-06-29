import React, { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { 
  TrendingUp, 
  TrendingDown, 
  Clock, 
  CheckCircle, 
  XCircle,
  BarChart3,
  PieChart,
  Activity,
  Target
} from 'lucide-react';

/**
 * Evolution Metrics Dashboard Component - Displays comprehensive evolution analytics
 * Provides success rates, performance metrics, and trend analysis
 */
const EvolutionMetrics = ({ timeRange = '30d' }) => {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch evolution metrics
  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        setLoading(true);
        const response = await fetch('/api/evolution/statistics');
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        setMetrics(data);
        setError(null);
      } catch (err) {
        console.error('Error fetching evolution metrics:', err);
        setError('Failed to load evolution metrics');
      } finally {
        setLoading(false);
      }
    };

    fetchMetrics();
    
    // Set up polling for real-time updates
    const interval = setInterval(fetchMetrics, 60000); // Poll every minute
    
    return () => clearInterval(interval);
  }, [timeRange]);

  // Calculate trend indicators
  const getTrendIndicator = (current, previous) => {
    if (!previous || previous === 0) return { trend: 'neutral', percentage: 0 };
    
    const change = ((current - previous) / previous) * 100;
    const trend = change > 0 ? 'up' : change < 0 ? 'down' : 'neutral';
    
    return { trend, percentage: Math.abs(change) };
  };

  // Format percentage
  const formatPercentage = (value) => {
    return `${Math.round(value * 10) / 10}%`;
  };

  // Format duration
  const formatDuration = (minutes) => {
    if (minutes < 60) return `${minutes}m`;
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return `${hours}h ${remainingMinutes}m`;
  };

  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {[...Array(4)].map((_, i) => (
          <Card key={i}>
            <CardContent className="p-6">
              <div className="animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-8 bg-gray-200 rounded w-1/2 mb-2"></div>
                <div className="h-3 bg-gray-200 rounded w-full"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center">
            <XCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
            <p className="text-red-600">{error}</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  const {
    total_evolutions = 0,
    active_evolutions = 0,
    success_rate = 0,
    average_duration_minutes = 0,
    recent_activity_7_days = 0,
    by_phase = {},
    by_type = {}
  } = metrics || {};

  // Calculate completed evolutions
  const completedEvolutions = (by_phase.completed || 0) + (by_phase.deployed || 0);
  const failedEvolutions = (by_phase.failed || 0) + (by_phase.rolled_back || 0);

  return (
    <div className="space-y-6">
      {/* Key Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Total Evolutions */}
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Total Evolutions</p>
                <p className="text-2xl font-bold text-gray-900">{total_evolutions}</p>
              </div>
              <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <BarChart3 className="h-6 w-6 text-blue-600" />
              </div>
            </div>
            <div className="mt-4 flex items-center">
              <Badge variant="secondary" className="text-xs">
                All Time
              </Badge>
            </div>
          </CardContent>
        </Card>

        {/* Active Evolutions */}
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Active Evolutions</p>
                <p className="text-2xl font-bold text-gray-900">{active_evolutions}</p>
              </div>
              <div className="h-12 w-12 bg-orange-100 rounded-lg flex items-center justify-center">
                <Activity className="h-6 w-6 text-orange-600" />
              </div>
            </div>
            <div className="mt-4 flex items-center">
              <Badge variant="warning" className="text-xs">
                In Progress
              </Badge>
            </div>
          </CardContent>
        </Card>

        {/* Success Rate */}
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Success Rate</p>
                <p className="text-2xl font-bold text-gray-900">{formatPercentage(success_rate)}</p>
              </div>
              <div className="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
                <Target className="h-6 w-6 text-green-600" />
              </div>
            </div>
            <div className="mt-4 flex items-center">
              {success_rate >= 90 ? (
                <div className="flex items-center text-green-600">
                  <TrendingUp className="h-4 w-4 mr-1" />
                  <span className="text-xs">Excellent</span>
                </div>
              ) : success_rate >= 75 ? (
                <div className="flex items-center text-yellow-600">
                  <TrendingUp className="h-4 w-4 mr-1" />
                  <span className="text-xs">Good</span>
                </div>
              ) : (
                <div className="flex items-center text-red-600">
                  <TrendingDown className="h-4 w-4 mr-1" />
                  <span className="text-xs">Needs Improvement</span>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Average Duration */}
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Avg Duration</p>
                <p className="text-2xl font-bold text-gray-900">
                  {formatDuration(average_duration_minutes)}
                </p>
              </div>
              <div className="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <Clock className="h-6 w-6 text-purple-600" />
              </div>
            </div>
            <div className="mt-4 flex items-center">
              <Badge variant="outline" className="text-xs">
                Per Evolution
              </Badge>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Charts and Detailed Metrics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Evolution Status Distribution */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <PieChart className="h-5 w-5" />
              <span>Status Distribution</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {Object.entries(by_phase).map(([phase, count]) => {
                const percentage = total_evolutions > 0 ? (count / total_evolutions) * 100 : 0;
                const phaseColors = {
                  'completed': 'bg-green-500',
                  'deployed': 'bg-green-600',
                  'implementing': 'bg-blue-500',
                  'validating': 'bg-yellow-500',
                  'approved': 'bg-purple-500',
                  'under_review': 'bg-orange-500',
                  'requested': 'bg-gray-500',
                  'failed': 'bg-red-500',
                  'rolled_back': 'bg-red-600'
                };
                
                return (
                  <div key={phase} className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className={`w-3 h-3 rounded-full ${phaseColors[phase] || 'bg-gray-400'}`}></div>
                      <span className="text-sm font-medium capitalize">
                        {phase.replace(/_/g, ' ')}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <span className="text-sm text-gray-600">{count}</span>
                      <span className="text-xs text-gray-500">
                        ({formatPercentage(percentage)})
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

        {/* Evolution Type Distribution */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <BarChart3 className="h-5 w-5" />
              <span>Evolution Types</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {Object.entries(by_type).map(([type, count]) => {
                const percentage = total_evolutions > 0 ? (count / total_evolutions) * 100 : 0;
                const maxCount = Math.max(...Object.values(by_type));
                const barWidth = maxCount > 0 ? (count / maxCount) * 100 : 0;
                
                return (
                  <div key={type} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium capitalize">
                        {type.replace(/_/g, ' ')}
                      </span>
                      <div className="flex items-center space-x-2">
                        <span className="text-sm text-gray-600">{count}</span>
                        <span className="text-xs text-gray-500">
                          ({formatPercentage(percentage)})
                        </span>
                      </div>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div 
                        className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                        style={{ width: `${barWidth}%` }}
                      ></div>
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recent Activity Summary */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Activity className="h-5 w-5" />
            <span>Recent Activity (7 days)</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">{recent_activity_7_days}</div>
              <div className="text-sm text-gray-600">New Evolutions</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">{completedEvolutions}</div>
              <div className="text-sm text-gray-600">Completed</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-red-600">{failedEvolutions}</div>
              <div className="text-sm text-gray-600">Failed/Rolled Back</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default EvolutionMetrics;
