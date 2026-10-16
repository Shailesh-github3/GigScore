package com.org.gigscore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.gigscore.dto.AiCoachResponse;
import com.org.gigscore.dto.AiGoalResponse;
import com.org.gigscore.entity.AiInteraction;
import com.org.gigscore.entity.GigScore;
import com.org.gigscore.entity.User;
import com.org.gigscore.entity.UserGoal;
import com.org.gigscore.repository.AiInteractionRepository;
import com.org.gigscore.repository.GigScoreRepository;
import com.org.gigscore.repository.UserGoalRepository;
import org.springframework.stereotype.Service;

@Service
public class AiCoachService {

    private final UserMetricsService userMetricsService;
    private final GigScoreRepository gigScoreRepository;
    private final AiInteractionRepository aiInteractionRepository;
    private final UserGoalRepository userGoalRepository;
    private final GeminiClientService geminiClientService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiCoachService(UserMetricsService userMetricsService,
                          GigScoreRepository gigScoreRepository,
                          AiInteractionRepository aiInteractionRepository,
                          UserGoalRepository userGoalRepository,
                          GeminiClientService geminiClientService) {
        this.userMetricsService = userMetricsService;
        this.gigScoreRepository = gigScoreRepository;
        this.aiInteractionRepository = aiInteractionRepository;
        this.userGoalRepository = userGoalRepository;
        this.geminiClientService = geminiClientService;
    }

    public AiCoachResponse analyzeUser(User user, String categoryPath) {
        UserMetricsService.UserAggregateMetrics metrics = userMetricsService.calculateAggregates(user);
        GigScore scoreOpt = gigScoreRepository.findByUser(user).orElse(null);
        double currentScore = scoreOpt != null ? scoreOpt.getScore() : 0.0;

        // Simplify data depth calculation
        int monthsActive = Math.max(1, metrics.activeDays() / 30);
        int platformCount = metrics.platformsUsed();
        double avgDaysPerWeek = metrics.activeDays() > 0 ? (metrics.activeDays() / (double)(monthsActive * 4)) : 0;

        String systemInstruction = "You are the GigScore AI Financial Coach. Your tone is encouraging, professional, and empathetic.\n" +
                "You must return ONLY a JSON object matching this schema exactly:\n" +
                "{\n" +
                "  \"behavioralObservation\": \"string linking two metrics to form a narrative\",\n" +
                "  \"dataInsight\": \"string citing user data vs baseline\",\n" +
                "  \"topActions\": [\"action1\", \"action2\"],\n" +
                "  \"estimatedImpactRange\": \"string e.g. +30 to +50 points\",\n" +
                "  \"confidence\": { \"level\": \"High/Medium/Low\", \"reasons\": [\"reason1\", \"reason2\", \"reason3\"] }\n" +
                "}";

        String userPrompt = String.format("""
            User Context:
            - Current Score: %.0f
            - Work Consistency: %.1f days/week (Impacts 30%%. Low-risk average is 5-6 days)
            - Ratings: %.2f (Impacts 20%%)
            - Connected Platforms: %d (Impacts 10%%)
            - Data Depth: %d months of history, %d connected apps.
            
            User Path: "%s"
            
            Instructions:
            1. Generate a "Behavioral Observation" that links two metrics together.
            2. Cite exact user data versus the baseline (e.g. low-risk average is 5-6 days).
            3. Provide 2 actionable improvement steps.
            4. Estimate the potential score impact using a RANGE (e.g., "+30 to +50 points"). NEVER give an exact number.
            5. Determine your "Confidence Level" (High/Medium/Low) based on the Data Depth provided, and list 3 bullet points justifying this confidence.
            """,
            currentScore, avgDaysPerWeek, metrics.avgRating(), platformCount, monthsActive, platformCount, categoryPath
        );

        String jsonResponseStr = geminiClientService.generateContent(userPrompt, systemInstruction);

        AiCoachResponse responseObj;
        try {
            responseObj = objectMapper.readValue(jsonResponseStr, AiCoachResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback
            responseObj = new AiCoachResponse();
            responseObj.setBehavioralObservation("Could not load observation.");
            responseObj.setDataInsight("Please try again later.");
        }

        // Save Interaction
        AiInteraction interaction = new AiInteraction();
        interaction.setUser(user);
        interaction.setCategoryPath(categoryPath);
        interaction.setResponse(jsonResponseStr);
        aiInteractionRepository.save(interaction);

        return responseObj;
    }

    public AiGoalResponse setGoal(User user, Integer targetScore) {
        UserGoal goal = new UserGoal();
        goal.setUser(user);
        goal.setTargetScore(targetScore);
        userGoalRepository.save(goal);

        AiGoalResponse response = new AiGoalResponse();
        response.setMessage("Goal set successfully!");
        response.setTargetScore(targetScore);
        response.setEstimatedTimeline("6-8 weeks"); // Placeholder for now

        return response;
    }
}
