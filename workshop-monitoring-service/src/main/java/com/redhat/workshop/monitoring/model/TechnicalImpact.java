package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * Model representing technical impact analysis.
 */
@RegisterForReflection
public class TechnicalImpact {

    @JsonProperty("technology_stack_changes")
    private List<String> technologyStackChanges;

    @JsonProperty("dependency_updates")
    private List<String> dependencyUpdates;

    @JsonProperty("breaking_api_changes")
    private Boolean breakingApiChanges;

    @JsonProperty("performance_impact")
    private String performanceImpact; // improved, degraded, unchanged

    @JsonProperty("security_implications")
    private List<String> securityImplications;

    @JsonProperty("compatibility_score")
    private Double compatibilityScore; // 0.0 to 10.0

    @JsonProperty("deployment_complexity")
    private String deploymentComplexity; // simple, moderate, complex

    @JsonProperty("infrastructure_changes")
    private Boolean infrastructureChanges;

    @JsonProperty("testing_complexity")
    private String testingComplexity; // simple, moderate, complex

    @JsonProperty("rollback_feasibility")
    private String rollbackFeasibility; // easy, moderate, difficult, risky

    // Default constructor
    public TechnicalImpact() {
        this.breakingApiChanges = false;
        this.infrastructureChanges = false;
        this.compatibilityScore = 10.0;
    }

    // Getters and setters
    public List<String> getTechnologyStackChanges() {
        return technologyStackChanges;
    }

    public void setTechnologyStackChanges(List<String> technologyStackChanges) {
        this.technologyStackChanges = technologyStackChanges;
    }

    public List<String> getDependencyUpdates() {
        return dependencyUpdates;
    }

    public void setDependencyUpdates(List<String> dependencyUpdates) {
        this.dependencyUpdates = dependencyUpdates;
    }

    public Boolean getBreakingApiChanges() {
        return breakingApiChanges;
    }

    public void setBreakingApiChanges(Boolean breakingApiChanges) {
        this.breakingApiChanges = breakingApiChanges;
    }

    public String getPerformanceImpact() {
        return performanceImpact;
    }

    public void setPerformanceImpact(String performanceImpact) {
        this.performanceImpact = performanceImpact;
    }

    public List<String> getSecurityImplications() {
        return securityImplications;
    }

    public void setSecurityImplications(List<String> securityImplications) {
        this.securityImplications = securityImplications;
    }

    public Double getCompatibilityScore() {
        return compatibilityScore;
    }

    public void setCompatibilityScore(Double compatibilityScore) {
        this.compatibilityScore = compatibilityScore;
    }

    public String getDeploymentComplexity() {
        return deploymentComplexity;
    }

    public void setDeploymentComplexity(String deploymentComplexity) {
        this.deploymentComplexity = deploymentComplexity;
    }

    public Boolean getInfrastructureChanges() {
        return infrastructureChanges;
    }

    public void setInfrastructureChanges(Boolean infrastructureChanges) {
        this.infrastructureChanges = infrastructureChanges;
    }

    public String getTestingComplexity() {
        return testingComplexity;
    }

    public void setTestingComplexity(String testingComplexity) {
        this.testingComplexity = testingComplexity;
    }

    public String getRollbackFeasibility() {
        return rollbackFeasibility;
    }

    public void setRollbackFeasibility(String rollbackFeasibility) {
        this.rollbackFeasibility = rollbackFeasibility;
    }
}
