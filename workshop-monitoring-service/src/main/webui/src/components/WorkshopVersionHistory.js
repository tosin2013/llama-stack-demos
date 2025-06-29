import React, { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { 
  GitBranch, 
  Clock, 
  User, 
  Tag, 
  ArrowRight, 
  RotateCcw, 
  FileText,
  TrendingUp,
  AlertCircle
} from 'lucide-react';

/**
 * Workshop Version History Component - Displays evolution history and version tracking
 * Provides version comparison, rollback capabilities, and evolution timeline
 */
const WorkshopVersionHistory = ({ workshopName, selectedEvolution }) => {
  const [versionHistory, setVersionHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedVersions, setSelectedVersions] = useState([]);
  const [showComparison, setShowComparison] = useState(false);

  // Fetch version history for workshop
  useEffect(() => {
    if (!workshopName) return;

    const fetchVersionHistory = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/evolution/workshops/${workshopName}/history`);
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        setVersionHistory(data.evolutions || []);
        setError(null);
      } catch (err) {
        console.error('Error fetching version history:', err);
        setError('Failed to load version history');
      } finally {
        setLoading(false);
      }
    };

    fetchVersionHistory();
  }, [workshopName]);

  // Handle version selection for comparison
  const handleVersionSelect = (evolution) => {
    if (selectedVersions.includes(evolution.evolution_id)) {
      setSelectedVersions(selectedVersions.filter(id => id !== evolution.evolution_id));
    } else if (selectedVersions.length < 2) {
      setSelectedVersions([...selectedVersions, evolution.evolution_id]);
    }
  };

  // Get evolution type badge color
  const getEvolutionTypeBadge = (type) => {
    const typeMap = {
      'research_update': 'bg-blue-100 text-blue-800',
      'technology_refresh': 'bg-purple-100 text-purple-800',
      'feedback_integration': 'bg-green-100 text-green-800',
      'content_expansion': 'bg-orange-100 text-orange-800',
      'content_update': 'bg-gray-100 text-gray-800',
      'bug_fix': 'bg-red-100 text-red-800',
      'security_update': 'bg-red-100 text-red-800',
      'performance_optimization': 'bg-yellow-100 text-yellow-800'
    };
    
    return typeMap[type] || 'bg-gray-100 text-gray-800';
  };

  // Format evolution type for display
  const formatEvolutionType = (type) => {
    return type.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  };

  // Get status icon and color
  const getStatusIcon = (status) => {
    const statusMap = {
      'completed': { icon: TrendingUp, color: 'text-green-600' },
      'deployed': { icon: TrendingUp, color: 'text-green-600' },
      'failed': { icon: AlertCircle, color: 'text-red-600' },
      'rolled_back': { icon: RotateCcw, color: 'text-orange-600' },
      'implementing': { icon: Clock, color: 'text-blue-600' },
      'validating': { icon: Clock, color: 'text-yellow-600' }
    };
    
    return statusMap[status] || { icon: FileText, color: 'text-gray-600' };
  };

  // Calculate duration
  const calculateDuration = (createdAt, completedAt) => {
    if (!completedAt) return 'In progress';
    
    const start = new Date(createdAt);
    const end = new Date(completedAt);
    const diffMs = end - start;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    
    if (diffHours > 0) {
      return `${diffHours}h ${diffMinutes}m`;
    }
    return `${diffMinutes}m`;
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Version History</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <span className="ml-2">Loading version history...</span>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Version History</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8">
            <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
            <p className="text-red-600 mb-4">{error}</p>
            <Button onClick={() => window.location.reload()}>
              Retry
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center space-x-2">
            <GitBranch className="h-5 w-5" />
            <span>Version History</span>
            {workshopName && (
              <Badge variant="outline">{workshopName}</Badge>
            )}
          </CardTitle>
          
          {selectedVersions.length === 2 && (
            <Button
              onClick={() => setShowComparison(!showComparison)}
              variant="outline"
              size="sm"
            >
              {showComparison ? 'Hide' : 'Show'} Comparison
            </Button>
          )}
        </div>
      </CardHeader>
      
      <CardContent>
        {versionHistory.length === 0 ? (
          <div className="text-center py-8">
            <GitBranch className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500">No version history available</p>
          </div>
        ) : (
          <div className="space-y-4">
            {/* Version comparison panel */}
            {showComparison && selectedVersions.length === 2 && (
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
                <h4 className="font-medium text-blue-900 mb-2">Version Comparison</h4>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  {selectedVersions.map((versionId, index) => {
                    const evolution = versionHistory.find(e => e.evolution_id === versionId);
                    return (
                      <div key={versionId} className="bg-white rounded p-3">
                        <div className="font-medium">{evolution?.target_version || 'Unknown'}</div>
                        <div className="text-gray-600">{formatEvolutionType(evolution?.evolution_type || '')}</div>
                        <div className="text-xs text-gray-500 mt-1">
                          {new Date(evolution?.created_at).toLocaleDateString()}
                        </div>
                      </div>
                    );
                  })}
                </div>
                <Button 
                  className="mt-3" 
                  size="sm" 
                  onClick={() => setSelectedVersions([])}
                >
                  Clear Selection
                </Button>
              </div>
            )}

            {/* Version timeline */}
            <div className="relative">
              {versionHistory.map((evolution, index) => {
                const statusInfo = getStatusIcon(evolution.status);
                const StatusIcon = statusInfo.icon;
                const isSelected = selectedVersions.includes(evolution.evolution_id);
                const isHighlighted = selectedEvolution?.evolution_id === evolution.evolution_id;
                
                return (
                  <div
                    key={evolution.evolution_id}
                    className={`relative flex items-start space-x-4 pb-6 ${
                      isHighlighted ? 'bg-blue-50 -mx-4 px-4 rounded-lg' : ''
                    }`}
                  >
                    {/* Timeline line */}
                    {index < versionHistory.length - 1 && (
                      <div className="absolute left-6 top-8 w-0.5 h-full bg-gray-200"></div>
                    )}
                    
                    {/* Status icon */}
                    <div className={`flex-shrink-0 w-12 h-12 rounded-full border-2 border-white shadow-sm flex items-center justify-center ${
                      evolution.status === 'completed' || evolution.status === 'deployed' 
                        ? 'bg-green-100' 
                        : evolution.status === 'failed' || evolution.status === 'rolled_back'
                        ? 'bg-red-100'
                        : 'bg-gray-100'
                    }`}>
                      <StatusIcon className={`h-5 w-5 ${statusInfo.color}`} />
                    </div>
                    
                    {/* Evolution details */}
                    <div 
                      className={`flex-1 cursor-pointer ${
                        selectedVersions.length < 2 ? 'hover:bg-gray-50' : ''
                      } rounded-lg p-3 transition-colors ${
                        isSelected ? 'bg-blue-50 border border-blue-200' : ''
                      }`}
                      onClick={() => selectedVersions.length < 2 && handleVersionSelect(evolution)}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center space-x-2">
                          <Tag className="h-4 w-4 text-gray-500" />
                          <span className="font-medium">
                            {evolution.target_version || evolution.current_version || 'Unknown Version'}
                          </span>
                          <Badge 
                            className={getEvolutionTypeBadge(evolution.evolution_type)}
                          >
                            {formatEvolutionType(evolution.evolution_type)}
                          </Badge>
                        </div>
                        
                        <div className="text-sm text-gray-500">
                          {calculateDuration(evolution.created_at, evolution.completed_at)}
                        </div>
                      </div>
                      
                      {evolution.evolution_description && (
                        <p className="text-sm text-gray-700 mb-2 line-clamp-2">
                          {evolution.evolution_description}
                        </p>
                      )}
                      
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-4 text-xs text-gray-500">
                          <span>
                            <User className="h-3 w-3 inline mr-1" />
                            {evolution.requested_by || 'System'}
                          </span>
                          <span>
                            <Clock className="h-3 w-3 inline mr-1" />
                            {new Date(evolution.created_at).toLocaleDateString()}
                          </span>
                          {evolution.files_modified && (
                            <span>
                              <FileText className="h-3 w-3 inline mr-1" />
                              {evolution.files_modified} files
                            </span>
                          )}
                        </div>
                        
                        <div className="flex items-center space-x-2">
                          {evolution.rollback_available && (
                            <Badge variant="outline" className="text-xs">
                              <RotateCcw className="h-3 w-3 mr-1" />
                              Rollback Available
                            </Badge>
                          )}
                          
                          <Badge 
                            variant={
                              evolution.status === 'completed' || evolution.status === 'deployed' 
                                ? 'success' 
                                : evolution.status === 'failed' || evolution.status === 'rolled_back'
                                ? 'destructive'
                                : 'secondary'
                            }
                          >
                            {evolution.status.replace(/_/g, ' ')}
                          </Badge>
                        </div>
                      </div>
                      
                      {/* Version transition indicator */}
                      {evolution.current_version && evolution.target_version && 
                       evolution.current_version !== evolution.target_version && (
                        <div className="mt-2 flex items-center text-xs text-gray-500">
                          <span>{evolution.current_version}</span>
                          <ArrowRight className="h-3 w-3 mx-2" />
                          <span>{evolution.target_version}</span>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
            
            {/* Selection instructions */}
            {selectedVersions.length < 2 && versionHistory.length > 1 && (
              <div className="text-center py-4 text-sm text-gray-500 border-t">
                Click on versions to select them for comparison (max 2)
              </div>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default WorkshopVersionHistory;
