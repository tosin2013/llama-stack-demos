package com.redhat.workshop.monitoring.model;

/**
 * Enumeration representing the health status of an agent or the overall system.
 * 
 * @author Workshop Monitoring Service
 */
public enum HealthStatus {
    /**
     * Agent is fully operational and responding normally
     */
    HEALTHY,
    
    /**
     * Agent is operational but experiencing some issues or reduced performance
     */
    DEGRADED,
    
    /**
     * Agent is not responding or experiencing critical issues
     */
    UNHEALTHY,
    
    /**
     * Agent status cannot be determined (e.g., network issues, timeout)
     */
    UNKNOWN
}
