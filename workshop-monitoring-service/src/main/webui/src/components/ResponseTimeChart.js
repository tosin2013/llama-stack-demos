import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Clock, TrendingUp } from 'lucide-react';

/**
 * Response Time Chart Component
 * Displays response time metrics for all agents in a bar chart
 */
const ResponseTimeChart = ({ agentStatuses, responseTimeStats }) => {
  if (!agentStatuses || agentStatuses.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Response Time Analysis</h3>
        <div className="text-center py-8">
          <Clock className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-500">No response time data available</p>
        </div>
      </div>
    );
  }

  // Prepare chart data
  const chartData = agentStatuses.map(agent => ({
    name: agent.name.replace('-', ' ').replace(/\b\w/g, l => l.toUpperCase()),
    responseTime: agent.responseTimeMs || 0,
    status: agent.health,
    fullName: agent.name
  })).sort((a, b) => a.responseTime - b.responseTime);

  // Custom tooltip
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
          <p className="font-medium text-gray-900">{label}</p>
          <p className="text-sm text-gray-600">
            Response Time: <span className="font-medium">{data.responseTime}ms</span>
          </p>
          <p className="text-sm text-gray-600">
            Status: <span className={`font-medium ${
              data.status === 'HEALTHY' ? 'text-green-600' :
              data.status === 'DEGRADED' ? 'text-yellow-600' :
              data.status === 'UNHEALTHY' ? 'text-red-600' : 'text-gray-600'
            }`}>
              {data.status}
            </span>
          </p>
        </div>
      );
    }
    return null;
  };

  // Get bar color based on response time and status
  const getBarColor = (entry) => {
    if (entry.status === 'UNHEALTHY') return '#ef4444'; // red-500
    if (entry.status === 'DEGRADED') return '#f59e0b'; // amber-500
    if (entry.responseTime > 1000) return '#f59e0b'; // amber-500 for slow responses
    if (entry.responseTime > 500) return '#eab308'; // yellow-500 for moderate responses
    return '#10b981'; // green-500 for fast responses
  };

  // Performance categories
  const getPerformanceCategory = (responseTime) => {
    if (responseTime < 100) return 'Excellent';
    if (responseTime < 300) return 'Good';
    if (responseTime < 500) return 'Fair';
    if (responseTime < 1000) return 'Slow';
    return 'Very Slow';
  };

  const performanceDistribution = {
    excellent: chartData.filter(d => d.responseTime < 100).length,
    good: chartData.filter(d => d.responseTime >= 100 && d.responseTime < 300).length,
    fair: chartData.filter(d => d.responseTime >= 300 && d.responseTime < 500).length,
    slow: chartData.filter(d => d.responseTime >= 500 && d.responseTime < 1000).length,
    verySlow: chartData.filter(d => d.responseTime >= 1000).length
  };

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">Response Time Analysis</h3>
            <p className="text-sm text-gray-600 mt-1">
              Performance metrics for all workshop agents
            </p>
          </div>
          <div className="flex items-center text-sm text-gray-500">
            <TrendingUp size={16} className="mr-1" />
            <span>Performance Tracking</span>
          </div>
        </div>
      </div>

      <div className="p-6">
        {/* Statistics Summary */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="text-center p-4 bg-blue-50 rounded-lg">
            <p className="text-2xl font-bold text-blue-700">
              {responseTimeStats.average}ms
            </p>
            <p className="text-sm text-blue-600">Average Response</p>
          </div>
          <div className="text-center p-4 bg-green-50 rounded-lg">
            <p className="text-2xl font-bold text-green-700">
              {responseTimeStats.min}ms
            </p>
            <p className="text-sm text-green-600">Fastest Response</p>
          </div>
          <div className="text-center p-4 bg-orange-50 rounded-lg">
            <p className="text-2xl font-bold text-orange-700">
              {responseTimeStats.max}ms
            </p>
            <p className="text-sm text-orange-600">Slowest Response</p>
          </div>
        </div>

        {/* Response Time Chart */}
        <div className="h-64 mb-6">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
              <XAxis 
                dataKey="name" 
                tick={{ fontSize: 12 }}
                angle={-45}
                textAnchor="end"
                height={80}
              />
              <YAxis 
                tick={{ fontSize: 12 }}
                label={{ value: 'Response Time (ms)', angle: -90, position: 'insideLeft' }}
              />
              <Tooltip content={<CustomTooltip />} />
              <Bar 
                dataKey="responseTime" 
                fill="#3b82f6"
                radius={[4, 4, 0, 0]}
              />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Performance Distribution */}
        <div className="border-t border-gray-200 pt-6">
          <h4 className="text-md font-medium text-gray-900 mb-4">Performance Distribution</h4>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
            <div className="text-center">
              <div className="w-8 h-8 bg-green-500 rounded-full mx-auto mb-2"></div>
              <p className="text-sm font-medium text-gray-900">{performanceDistribution.excellent}</p>
              <p className="text-xs text-gray-600">Excellent (&lt;100ms)</p>
            </div>
            <div className="text-center">
              <div className="w-8 h-8 bg-green-400 rounded-full mx-auto mb-2"></div>
              <p className="text-sm font-medium text-gray-900">{performanceDistribution.good}</p>
              <p className="text-xs text-gray-600">Good (100-300ms)</p>
            </div>
            <div className="text-center">
              <div className="w-8 h-8 bg-yellow-400 rounded-full mx-auto mb-2"></div>
              <p className="text-sm font-medium text-gray-900">{performanceDistribution.fair}</p>
              <p className="text-xs text-gray-600">Fair (300-500ms)</p>
            </div>
            <div className="text-center">
              <div className="w-8 h-8 bg-orange-400 rounded-full mx-auto mb-2"></div>
              <p className="text-sm font-medium text-gray-900">{performanceDistribution.slow}</p>
              <p className="text-xs text-gray-600">Slow (500ms-1s)</p>
            </div>
            <div className="text-center">
              <div className="w-8 h-8 bg-red-400 rounded-full mx-auto mb-2"></div>
              <p className="text-sm font-medium text-gray-900">{performanceDistribution.verySlow}</p>
              <p className="text-xs text-gray-600">Very Slow (&gt;1s)</p>
            </div>
          </div>
        </div>

        {/* Agent Performance Details */}
        <div className="border-t border-gray-200 pt-6 mt-6">
          <h4 className="text-md font-medium text-gray-900 mb-4">Agent Performance Details</h4>
          <div className="space-y-2">
            {chartData.map((agent, index) => (
              <div key={agent.fullName} className="flex items-center justify-between py-2 px-3 bg-gray-50 rounded-lg">
                <div className="flex items-center space-x-3">
                  <span className="text-sm font-medium text-gray-900 w-4">
                    #{index + 1}
                  </span>
                  <span className="text-sm text-gray-700">{agent.name}</span>
                </div>
                <div className="flex items-center space-x-3">
                  <span className="text-sm font-medium text-gray-900">
                    {agent.responseTime}ms
                  </span>
                  <span className={`text-xs px-2 py-1 rounded-full ${
                    agent.responseTime < 300 ? 'bg-green-100 text-green-800' :
                    agent.responseTime < 500 ? 'bg-yellow-100 text-yellow-800' :
                    'bg-red-100 text-red-800'
                  }`}>
                    {getPerformanceCategory(agent.responseTime)}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ResponseTimeChart;
