package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Request model for analyzing repository via Template Converter Agent
 */
public class AnalyzeRepositoryRequest {

    
    @JsonProperty("repository_url")
    private String repositoryUrl;

    @JsonProperty("analysis_depth")
    private String analysisDepth = "comprehensive";

    @JsonProperty("target_format")
    private String targetFormat = "rhpds_showroom";

    // Default constructor
    public AnalyzeRepositoryRequest() {}

    public AnalyzeRepositoryRequest(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    // Getters and Setters
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getAnalysisDepth() {
        return analysisDepth;
    }

    public void setAnalysisDepth(String analysisDepth) {
        this.analysisDepth = analysisDepth;
    }

    public String getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }
}
