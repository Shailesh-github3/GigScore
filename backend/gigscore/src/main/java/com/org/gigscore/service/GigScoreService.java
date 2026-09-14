package com.org.gigscore.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.gigscore.dto.ScoreConfig;
import com.org.gigscore.dto.ScoreResponse;
import com.org.gigscore.entity.GigScore;
import com.org.gigscore.entity.ScoreHistory;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.GigScoreRepository;
import com.org.gigscore.repository.ScoreHistoryRepository;
import com.org.gigscore.service.UserMetricsService.UserAggregateMetrics;

@Service
public class GigScoreService {
    static final double MAX_SCORE = 100.0;
    static final double EARNINGS_TARGET = 5000.0;
    static final double JOBS_TARGET = 100.0;
    static final double ACTIVE_DAYS_TARGET = 30.0;
    static final double RATING_MAX = 5.0;

    static final double WEIGHT_EARNINGS = 0.35;
    static final double WEIGHT_JOBS = 0.25;
    static final double WEIGHT_RATING = 0.30;
    static final double WEIGHT_ACTIVE_DAYS = 0.10;

    static final double EXP_EARNINGS = 0.70;
    static final double EXP_JOBS = 0.65;
    static final double EXP_RATING = 1.40;
    static final double EXP_ACTIVE_DAYS = 1.00;

    final GigScoreRepository gigScoreRepository;
    final ScoreHistoryRepository scoreHistoryRepository;
    final UserMetricsService userMetricsService;

    public GigScoreService(GigScoreRepository gigScoreRepository, ScoreHistoryRepository scoreHistoryRepository, UserMetricsService userMetricsService) {
        this.gigScoreRepository = gigScoreRepository;
        this.scoreHistoryRepository = scoreHistoryRepository;
        this.userMetricsService = userMetricsService;
    }

    @Transactional
    public ScoreResponse calculateAndPersistScore(User user) {
        UserAggregateMetrics metrics = userMetricsService.calculateAggregates(user);
        Map<String, Double> components = computeComponents(metrics);
        double score = roundToTwoDecimals(components.values().stream().mapToDouble(Double::doubleValue).sum());

        GigScore gigScore = resolveOrCreateUserGigScore(user);
        gigScore.setUser(user);
        gigScore.setScore(score);
        gigScoreRepository.save(gigScore);

        ScoreHistory history = new ScoreHistory();
        history.setUser(user);
        history.setScore(score);
        scoreHistoryRepository.save(history);

        return ScoreResponse.builder()
                .userId(user.getUserId())
                .score(score)
                .components(components)
                .band(bandFor(score))
                .suggestions(suggestionsFor(components))
                .build();
    }

    public Map<String, Double> computeComponents(UserAggregateMetrics metrics) {
        double earnings = Math.pow(normalizedComponent(metrics.totalEarnings(), EARNINGS_TARGET), EXP_EARNINGS) * WEIGHT_EARNINGS * MAX_SCORE;
        double jobs = Math.pow(normalizedComponent(metrics.jobsCompleted(), JOBS_TARGET), EXP_JOBS) * WEIGHT_JOBS * MAX_SCORE;
        double rating = Math.pow(normalizedComponent(clamp(metrics.avgRating(), 0.0, RATING_MAX), RATING_MAX), EXP_RATING) * WEIGHT_RATING * MAX_SCORE;
        double activeDays = Math.pow(normalizedComponent(metrics.activeDays(), ACTIVE_DAYS_TARGET), EXP_ACTIVE_DAYS) * WEIGHT_ACTIVE_DAYS * MAX_SCORE;

        Map<String, Double> map = new LinkedHashMap<>();
        map.put("earnings", roundToTwoDecimals(earnings));
        map.put("jobs", roundToTwoDecimals(jobs));
        map.put("rating", roundToTwoDecimals(rating));
        map.put("activeDays", roundToTwoDecimals(activeDays));
        return map;
    }

    public double calculateScoreValue(UserAggregateMetrics metrics) {
        return roundToTwoDecimals(computeComponents(metrics).values().stream()
                .mapToDouble(Double::doubleValue).sum());
    }

    @Cacheable(value = "scores", key = "#user.userId")
    public ScoreResponse getScoreForUser(User user) {
        return calculateAndPersistScore(user);
    }

    public ScoreConfig getConfig() {
        return ScoreConfig.builder()
                .maxScore(MAX_SCORE)
                .earningsTarget(EARNINGS_TARGET)
                .jobsTarget(JOBS_TARGET)
                .ratingMax(RATING_MAX)
                .activeDaysTarget(ACTIVE_DAYS_TARGET)
                .weights(ScoreConfig.WeightSet.builder()
                        .earnings(WEIGHT_EARNINGS)
                        .jobs(WEIGHT_JOBS)
                        .rating(WEIGHT_RATING)
                        .activeDays(WEIGHT_ACTIVE_DAYS)
                        .build())
                .exponents(ScoreConfig.ExponentSet.builder()
                        .earnings(EXP_EARNINGS)
                        .jobs(EXP_JOBS)
                        .rating(EXP_RATING)
                        .activeDays(EXP_ACTIVE_DAYS)
                        .build())
                .build();
    }

    private String bandFor(double score) {
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Strong";
        if (score >= 50) return "Fair";
        return "Needs Improvement";
    }

    private List<String> suggestionsFor(Map<String, Double> components) {
        List<String> suggestions = new ArrayList<>();
        if (components.getOrDefault("earnings", 0.0) < 10.0) {
            suggestions.add("Increase total earnings to boost your earnings component.");
        }
        if (components.getOrDefault("jobs", 0.0) < 7.0) {
            suggestions.add("Complete more gigs to strengthen your job completion metric.");
        }
        if (components.getOrDefault("rating", 0.0) < 9.0) {
            suggestions.add("Improve your average rating by delivering higher-quality work.");
        }
        if (components.getOrDefault("activeDays", 0.0) < 3.0) {
            suggestions.add("Work on more active days to demonstrate consistency.");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Your score is well-balanced. Keep it up!");
        }
        return suggestions;
    }

    private double normalizedComponent(double value, double target) {
        if (target <= 0) return 0;
        return clamp(value / target, 0.0, 1.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private GigScore resolveOrCreateUserGigScore(User user) {
        return gigScoreRepository.findByUser(user).orElseGet(GigScore::new);
    }
}
