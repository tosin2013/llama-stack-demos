package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Model representing dependency impact analysis.
 */
@RegisterForReflection
public class DependencyImpact {

    @JsonProperty("dependency_name")
    private String dependencyName;

    @JsonProperty("dependency_type")
    private String dependencyType; // internal, external, system

    @JsonProperty("impact_level")
    private String impactLevel; // low, medium, high, critical

    @JsonProperty("change_description")
    private String changeDescription;

    @JsonProperty("mitigation_required")
    private Boolean mitigationRequired;

    @JsonProperty("testing_required")
    private Boolean testingRequired;

    // Default constructor
    public DependencyImpact() {
        this.mitigationRequired = false;
        this.testingRequired = false;
    }

    // Getters and setters
    public String getDependencyName() {
        return dependencyName;
    }

    public void setDependencyName(String dependencyName) {
        this.dependencyName = dependencyName;
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(String dependencyType) {
        this.dependencyType = dependencyType;
    }

    public String getImpactLevel() {
        return impactLevel;
    }

    public void setImpactLevel(String impactLevel) {
        this.impactLevel = impactLevel;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    public Boolean getMitigationRequired() {
        return mitigationRequired;
    }

    public void setMitigationRequired(Boolean mitigationRequired) {
        this.mitigationRequired = mitigationRequired;
    }

    public Boolean getTestingRequired() {
        return testingRequired;
    }

    public void setTestingRequired(Boolean testingRequired) {
        this.testingRequired = testingRequired;
    }
}
