import { useState, useEffect, useCallback } from 'react';

/**
 * Custom hook for managing evolution data and operations
 * Provides centralized state management for evolution-related components
 * Follows existing useMonitoring.js patterns for consistency
 */
const useEvolution = () => {
  const [evolutions, setEvolutions] = useState([]);
  const [selectedEvolution, setSelectedEvolution] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  // Fetch active evolutions
  const fetchActiveEvolutions = useCallback(async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/evolution/active');
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      setEvolutions(data.active_evolutions || []);
      setError(null);
    } catch (err) {
      console.error('Error fetching active evolutions:', err);
      setError('Failed to load active evolutions');
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch evolution by ID
  const fetchEvolutionById = useCallback(async (evolutionId) => {
    try {
      setLoading(true);
      const response = await fetch(`/api/evolution/${evolutionId}`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const evolution = await response.json();
      setSelectedEvolution(evolution);
      setError(null);
      return evolution;
    } catch (err) {
      console.error('Error fetching evolution:', err);
      setError('Failed to load evolution details');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch workshop evolution history
  const fetchWorkshopHistory = useCallback(async (workshopName) => {
    try {
      setLoading(true);
      const response = await fetch(`/api/evolution/workshops/${workshopName}/history`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      setError(null);
      return data.evolutions || [];
    } catch (err) {
      console.error('Error fetching workshop history:', err);
      setError('Failed to load workshop history');
      return [];
    } finally {
      setLoading(false);
    }
  }, []);

  // Create new evolution analysis
  const createEvolutionAnalysis = useCallback(async (analysisRequest) => {
    try {
      setLoading(true);
      const response = await fetch('/api/impact-assessment/analyze', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(analysisRequest),
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const analysis = await response.json();
      setError(null);
      
      // Trigger refresh of evolutions
      setRefreshTrigger(prev => prev + 1);
      
      return analysis;
    } catch (err) {
      console.error('Error creating evolution analysis:', err);
      setError('Failed to create evolution analysis');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  // Update evolution status
  const updateEvolutionStatus = useCallback(async (evolutionId, statusUpdate) => {
    try {
      setLoading(true);
      const response = await fetch(`/api/evolution/${evolutionId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(statusUpdate),
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const result = await response.json();
      setError(null);
      
      // Update selected evolution if it matches
      if (selectedEvolution?.evolution_id === evolutionId) {
        const updatedEvolution = await fetchEvolutionById(evolutionId);
        setSelectedEvolution(updatedEvolution);
      }
      
      // Trigger refresh of evolutions
      setRefreshTrigger(prev => prev + 1);
      
      return result;
    } catch (err) {
      console.error('Error updating evolution status:', err);
      setError('Failed to update evolution status');
      return null;
    } finally {
      setLoading(false);
    }
  }, [selectedEvolution, fetchEvolutionById]);

  // Get evolution statistics
  const fetchEvolutionStatistics = useCallback(async () => {
    try {
      const response = await fetch('/api/evolution/statistics');
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const statistics = await response.json();
      return statistics;
    } catch (err) {
      console.error('Error fetching evolution statistics:', err);
      return null;
    }
  }, []);

  // Get impact analysis for evolution
  const fetchImpactAnalysis = useCallback(async (evolutionId) => {
    try {
      const response = await fetch(`/api/impact-assessment/evolution/${evolutionId}`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      return data.analyses || [];
    } catch (err) {
      console.error('Error fetching impact analysis:', err);
      return [];
    }
  }, []);

  // Refresh data
  const refresh = useCallback(() => {
    setRefreshTrigger(prev => prev + 1);
  }, []);

  // Clear error
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  // Clear selected evolution
  const clearSelection = useCallback(() => {
    setSelectedEvolution(null);
  }, []);

  // Auto-refresh active evolutions
  useEffect(() => {
    fetchActiveEvolutions();
  }, [fetchActiveEvolutions, refreshTrigger]);

  // Set up polling for real-time updates
  useEffect(() => {
    const interval = setInterval(() => {
      fetchActiveEvolutions();
    }, 30000); // Poll every 30 seconds

    return () => clearInterval(interval);
  }, [fetchActiveEvolutions]);

  // Filter evolutions by status
  const getEvolutionsByStatus = useCallback((status) => {
    return evolutions.filter(evolution => evolution.status === status);
  }, [evolutions]);

  // Filter evolutions by type
  const getEvolutionsByType = useCallback((type) => {
    return evolutions.filter(evolution => evolution.evolution_type === type);
  }, [evolutions]);

  // Get evolution counts by status
  const getStatusCounts = useCallback(() => {
    const counts = {};
    evolutions.forEach(evolution => {
      counts[evolution.status] = (counts[evolution.status] || 0) + 1;
    });
    return counts;
  }, [evolutions]);

  // Get evolution counts by type
  const getTypeCounts = useCallback(() => {
    const counts = {};
    evolutions.forEach(evolution => {
      counts[evolution.evolution_type] = (counts[evolution.evolution_type] || 0) + 1;
    });
    return counts;
  }, [evolutions]);

  // Check if evolution is in progress
  const isEvolutionInProgress = useCallback((evolution) => {
    const inProgressStatuses = ['approved', 'implementing', 'validating'];
    return inProgressStatuses.includes(evolution.status);
  }, []);

  // Check if evolution is completed
  const isEvolutionCompleted = useCallback((evolution) => {
    const completedStatuses = ['completed', 'deployed'];
    return completedStatuses.includes(evolution.status);
  }, []);

  // Check if evolution has failed
  const isEvolutionFailed = useCallback((evolution) => {
    const failedStatuses = ['failed', 'rolled_back', 'rejected', 'cancelled'];
    return failedStatuses.includes(evolution.status);
  }, []);

  return {
    // State
    evolutions,
    selectedEvolution,
    loading,
    error,
    refreshTrigger,

    // Actions
    fetchActiveEvolutions,
    fetchEvolutionById,
    fetchWorkshopHistory,
    createEvolutionAnalysis,
    updateEvolutionStatus,
    fetchEvolutionStatistics,
    fetchImpactAnalysis,
    refresh,
    clearError,
    clearSelection,
    setSelectedEvolution,

    // Computed values
    getEvolutionsByStatus,
    getEvolutionsByType,
    getStatusCounts,
    getTypeCounts,
    isEvolutionInProgress,
    isEvolutionCompleted,
    isEvolutionFailed,

    // Derived state
    activeEvolutionsCount: evolutions.length,
    inProgressEvolutions: evolutions.filter(e => isEvolutionInProgress(e)),
    completedEvolutions: evolutions.filter(e => isEvolutionCompleted(e)),
    failedEvolutions: evolutions.filter(e => isEvolutionFailed(e)),
  };
};

export default useEvolution;
