package com.redhat.workshop.monitoring.service;

import com.redhat.workshop.monitoring.model.*;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for AI-powered impact assessment of workshop evolution requests.
 * Analyzes proposed changes and their effects on existing content, learners, and systems.
 */
@ApplicationScoped
public class ImpactAssessmentService {

    private static final Logger LOG = Logger.getLogger(ImpactAssessmentService.class);

    @Inject
    EvolutionService evolutionService;

    // In-memory storage for impact analyses (in production, this would be a database)
    private final Map<String, ImpactAnalysis> analyses = new ConcurrentHashMap<>();

    /**
     * Perform comprehensive impact analysis for an evolution request
     */
    public ImpactAnalysis analyzeEvolutionImpact(String evolutionId, String workshopName, 
                                               String evolutionType, String proposedChanges) {
        LOG.infof("Starting impact analysis: evolution=%s, workshop=%s, type=%s", 
                 evolutionId, workshopName, evolutionType);

        String analysisId = UUID.randomUUID().toString();
        
        ImpactAnalysis analysis = new ImpactAnalysis(workshopName, evolutionType, evolutionId);
        analysis.setAnalysisId(analysisId);
        analysis.setAnalysisStatus(AnalysisStatus.IN_PROGRESS);
        analysis.setAnalyzedBy("impact-assessment-service");

        // Store analysis
        analyses.put(analysisId, analysis);

        try {
            // Step 1: Analyze content impact
            ContentImpact contentImpact = analyzeContentImpact(workshopName, evolutionType, proposedChanges);
            analysis.setContentImpact(contentImpact);

            // Step 2: Analyze technical impact
            TechnicalImpact technicalImpact = analyzeTechnicalImpact(workshopName, evolutionType, proposedChanges);
            analysis.setTechnicalImpact(technicalImpact);

            // Step 3: Analyze learner impact
            LearnerImpact learnerImpact = analyzeLearnerImpact(workshopName, evolutionType, proposedChanges);
            analysis.setLearnerImpact(learnerImpact);

            // Step 4: Analyze dependencies
            List<DependencyImpact> dependencyAnalysis = analyzeDependencies(workshopName, evolutionType, proposedChanges);
            analysis.setDependencyAnalysis(dependencyAnalysis);

            // Step 5: Calculate overall impact score and risk level
            calculateOverallImpact(analysis);

            // Step 6: Generate recommendations and mitigation strategies
            generateRecommendations(analysis);

            // Step 7: Determine testing requirements
            generateTestingRequirements(analysis);

            // Step 8: Assess rollback complexity
            assessRollbackComplexity(analysis);

            // Step 9: Generate analysis summary
            generateAnalysisSummary(analysis);

            // Complete analysis
            analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
            analysis.setCompletedAt(Instant.now());
            analysis.setConfidenceScore(calculateConfidenceScore(analysis));

            LOG.infof("Impact analysis completed: %s - Risk: %s, Score: %.1f", 
                     analysisId, analysis.getRiskLevel(), analysis.getOverallImpactScore());

            return analysis;

        } catch (Exception e) {
            LOG.errorf("Error during impact analysis: %s", e.getMessage());
            analysis.setAnalysisStatus(AnalysisStatus.FAILED);
            analysis.setCompletedAt(Instant.now());
            return analysis;
        }
    }

    /**
     * Get impact analysis by ID
     */
    public ImpactAnalysis getAnalysisById(String analysisId) {
        return analyses.get(analysisId);
    }

    /**
     * Get all analyses for an evolution
     */
    public List<ImpactAnalysis> getAnalysesForEvolution(String evolutionId) {
        return analyses.values().stream()
                .filter(analysis -> evolutionId.equals(analysis.getEvolutionId()))
                .sorted(Comparator.comparing(ImpactAnalysis::getCreatedAt).reversed())
                .toList();
    }

    /**
     * Get recent analyses
     */
    public List<ImpactAnalysis> getRecentAnalyses(int limit) {
        return analyses.values().stream()
                .sorted(Comparator.comparing(ImpactAnalysis::getCreatedAt).reversed())
                .limit(limit)
                .toList();
    }

    /**
     * Analyze content impact using AI-powered analysis
     */
    private ContentImpact analyzeContentImpact(String workshopName, String evolutionType, String proposedChanges) {
        LOG.debugf("Analyzing content impact for workshop: %s", workshopName);

        ContentImpact impact = new ContentImpact();

        // Simulate AI analysis based on evolution type
        switch (evolutionType) {
            case "technology_refresh":
                impact.setSectionsAffected(6);
                impact.setExercisesModified(8);
                impact.setNewContentAdded(true);
                impact.setLearningObjectivesChanged(true);
                impact.setDifficultyLevelChange("increased");
                impact.setContentQualityScore(8.5);
                impact.setEducationalFlowImpact("improved");
                break;
            case "research_update":
                impact.setSectionsAffected(3);
                impact.setExercisesModified(2);
                impact.setNewContentAdded(true);
                impact.setContentQualityScore(9.0);
                impact.setEducationalFlowImpact("improved");
                break;
            case "feedback_integration":
                impact.setSectionsAffected(2);
                impact.setExercisesModified(3);
                impact.setContentQualityScore(8.0);
                impact.setEducationalFlowImpact("improved");
                impact.setLanguageClarityImpact("improved");
                break;
            default:
                impact.setSectionsAffected(1);
                impact.setExercisesModified(1);
                impact.setContentQualityScore(7.5);
                impact.setEducationalFlowImpact("unchanged");
        }

        return impact;
    }

    /**
     * Analyze technical impact
     */
    private TechnicalImpact analyzeTechnicalImpact(String workshopName, String evolutionType, String proposedChanges) {
        LOG.debugf("Analyzing technical impact for workshop: %s", workshopName);

        TechnicalImpact impact = new TechnicalImpact();

        // Simulate technical analysis
        if ("technology_refresh".equals(evolutionType)) {
            impact.setTechnologyStackChanges(Arrays.asList("Kubernetes 1.29", "Docker 24.0", "Helm 3.14"));
            impact.setBreakingApiChanges(true);
            impact.setPerformanceImpact("improved");
            impact.setCompatibilityScore(7.5);
            impact.setDeploymentComplexity("moderate");
            impact.setTestingComplexity("complex");
            impact.setRollbackFeasibility("moderate");
        } else {
            impact.setBreakingApiChanges(false);
            impact.setPerformanceImpact("unchanged");
            impact.setCompatibilityScore(9.0);
            impact.setDeploymentComplexity("simple");
            impact.setTestingComplexity("simple");
            impact.setRollbackFeasibility("easy");
        }

        return impact;
    }

    /**
     * Analyze learner impact
     */
    private LearnerImpact analyzeLearnerImpact(String workshopName, String evolutionType, String proposedChanges) {
        LOG.debugf("Analyzing learner impact for workshop: %s", workshopName);

        LearnerImpact impact = new LearnerImpact();

        // Simulate learner impact analysis
        impact.setActiveLearnersAffected(estimateActiveLearnersForWorkshop(workshopName));
        
        if ("technology_refresh".equals(evolutionType)) {
            impact.setLearningDisruptionLevel("moderate");
            impact.setSkillLevelRequirements("intermediate");
            impact.setTimeInvestmentChange("increased");
            impact.setCompletionRateImpact("unchanged");
            impact.setCommunicationNeeded(true);
        } else if ("feedback_integration".equals(evolutionType)) {
            impact.setLearningDisruptionLevel("minimal");
            impact.setCompletionRateImpact("improved");
            impact.setEngagementScoreChange(2.5);
            impact.setFeedbackIntegration(true);
        } else {
            impact.setLearningDisruptionLevel("minimal");
            impact.setCompletionRateImpact("unchanged");
        }

        return impact;
    }

    /**
     * Analyze dependencies
     */
    private List<DependencyImpact> analyzeDependencies(String workshopName, String evolutionType, String proposedChanges) {
        LOG.debugf("Analyzing dependencies for workshop: %s", workshopName);

        List<DependencyImpact> dependencies = new ArrayList<>();

        if ("technology_refresh".equals(evolutionType)) {
            DependencyImpact k8sDep = new DependencyImpact();
            k8sDep.setDependencyName("Kubernetes API");
            k8sDep.setDependencyType("external");
            k8sDep.setImpactLevel("high");
            k8sDep.setChangeDescription("API version updates require exercise modifications");
            k8sDep.setMitigationRequired(true);
            k8sDep.setTestingRequired(true);
            dependencies.add(k8sDep);

            DependencyImpact helmDep = new DependencyImpact();
            helmDep.setDependencyName("Helm Charts");
            helmDep.setDependencyType("external");
            helmDep.setImpactLevel("medium");
            helmDep.setChangeDescription("Chart syntax updates needed");
            helmDep.setTestingRequired(true);
            dependencies.add(helmDep);
        }

        return dependencies;
    }

    /**
     * Calculate overall impact score and risk level
     */
    private void calculateOverallImpact(ImpactAnalysis analysis) {
        double contentScore = analysis.getContentImpact().getContentQualityScore();
        double technicalScore = analysis.getTechnicalImpact().getCompatibilityScore();
        
        // Calculate weighted overall score
        double overallScore = (contentScore * 0.4) + (technicalScore * 0.4) + 
                             (analysis.getLearnerImpact().getEngagementScoreChange() + 5.0) * 0.2;
        
        analysis.setOverallImpactScore(Math.max(0.0, Math.min(10.0, overallScore)));

        // Determine risk level
        if (analysis.getTechnicalImpact().getBreakingApiChanges() || 
            analysis.getContentImpact().hasSignificantChanges()) {
            analysis.setRiskLevel(RiskLevel.HIGH);
        } else if (analysis.getContentImpact().getSectionsAffected() > 3) {
            analysis.setRiskLevel(RiskLevel.MEDIUM);
        } else {
            analysis.setRiskLevel(RiskLevel.LOW);
        }
    }

    /**
     * Generate recommendations and mitigation strategies
     */
    private void generateRecommendations(ImpactAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        List<String> mitigations = new ArrayList<>();

        if (analysis.getRiskLevel() == RiskLevel.HIGH) {
            recommendations.add("Conduct thorough testing before deployment");
            recommendations.add("Create comprehensive backup before implementation");
            recommendations.add("Plan staged rollout to minimize learner impact");
            mitigations.add("Implement feature flags for gradual rollout");
            mitigations.add("Prepare detailed rollback procedures");
        }

        if (analysis.getTechnicalImpact().getBreakingApiChanges()) {
            recommendations.add("Update all API references in exercises");
            recommendations.add("Validate compatibility with existing tools");
            mitigations.add("Provide API migration guide for learners");
        }

        if (analysis.getContentImpact().hasSignificantChanges()) {
            recommendations.add("Review learning objectives alignment");
            recommendations.add("Update assessment criteria if needed");
        }

        analysis.setRecommendations(recommendations);
        analysis.setMitigationStrategies(mitigations);
    }

    /**
     * Generate testing requirements
     */
    private void generateTestingRequirements(ImpactAnalysis analysis) {
        List<String> testingRequirements = new ArrayList<>();

        testingRequirements.add("Content accuracy validation");
        testingRequirements.add("Exercise functionality testing");

        if (analysis.getTechnicalImpact().getBreakingApiChanges()) {
            testingRequirements.add("API compatibility testing");
            testingRequirements.add("Integration testing with external systems");
        }

        if (analysis.getContentImpact().getExercisesModified() > 3) {
            testingRequirements.add("End-to-end workshop walkthrough");
            testingRequirements.add("User acceptance testing");
        }

        analysis.setTestingRequirements(testingRequirements);
    }

    /**
     * Assess rollback complexity
     */
    private void assessRollbackComplexity(ImpactAnalysis analysis) {
        if (analysis.getTechnicalImpact().getBreakingApiChanges()) {
            analysis.setRollbackComplexity("complex");
        } else if (analysis.getContentImpact().getSectionsAffected() > 3) {
            analysis.setRollbackComplexity("moderate");
        } else {
            analysis.setRollbackComplexity("simple");
        }
    }

    /**
     * Generate analysis summary
     */
    private void generateAnalysisSummary(ImpactAnalysis analysis) {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("Impact analysis for %s evolution of %s workshop. ", 
                      analysis.getEvolutionType().replace("_", " "), 
                      analysis.getWorkshopName()));
        
        summary.append(String.format("Overall impact score: %.1f/10, Risk level: %s. ", 
                      analysis.getOverallImpactScore(), 
                      analysis.getRiskLevel().getValue().toUpperCase()));
        
        if (analysis.getContentImpact().getSectionsAffected() > 0) {
            summary.append(String.format("Affects %d sections and %d exercises. ", 
                          analysis.getContentImpact().getSectionsAffected(),
                          analysis.getContentImpact().getExercisesModified()));
        }
        
        if (analysis.getTechnicalImpact().getBreakingApiChanges()) {
            summary.append("Contains breaking API changes requiring careful migration. ");
        }
        
        summary.append(String.format("Rollback complexity: %s.", analysis.getRollbackComplexity()));
        
        analysis.setAnalysisSummary(summary.toString());
    }

    /**
     * Calculate confidence score for the analysis
     */
    private double calculateConfidenceScore(ImpactAnalysis analysis) {
        // Simulate confidence calculation based on available data and analysis completeness
        double baseConfidence = 0.85;
        
        if (analysis.getDependencyAnalysis() != null && !analysis.getDependencyAnalysis().isEmpty()) {
            baseConfidence += 0.1;
        }
        
        if (analysis.getRecommendations() != null && analysis.getRecommendations().size() > 2) {
            baseConfidence += 0.05;
        }
        
        return Math.min(1.0, baseConfidence);
    }

    /**
     * Estimate active learners for a workshop
     */
    private int estimateActiveLearnersForWorkshop(String workshopName) {
        // Simulate learner count estimation
        Map<String, Integer> workshopLearners = Map.of(
            "kubernetes-fundamentals", 45,
            "advanced-ml-workshop", 28,
            "cloud-native-development", 62,
            "devops-pipeline", 38
        );
        
        return workshopLearners.getOrDefault(workshopName, 25);
    }
}
