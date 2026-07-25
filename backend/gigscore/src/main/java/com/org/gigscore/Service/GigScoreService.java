package com.org.gigscore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.gigscore.dto.ScoreResponse;
import com.org.gigscore.entity.GigScore;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.GigScoreRepository;
import com.org.gigscore.service.UserMetricsService.UserAggregateMetrics;

@Service
public class GigScoreService {
    private static final double MAX_SCORE = 100.0;
    private static final double EARNINGS_TARGET = 5000.0;
    private static final double JOBS_TARGET = 100.0;
    private static final double ACTIVE_DAYS_TARGET = 30.0;

    private static final double WEIGHT_EARNINGS = 0.35;
    private static final double WEIGHT_JOBS = 0.25;
    private static final double WEIGHT_RATING = 0.30;
    private static final double WEIGHT_ACTIVE_DAYS = 0.10;

    // Exponents used in score normalization.
    // a < 1: reward early earnings, b < 1: reward early jobs,
    // c > 1: stricter on ratings, d ~= 1: neutral for consistency.
    private static final double EXP_EARNINGS = 0.70;
    private static final double EXP_JOBS = 0.65;
    private static final double EXP_RATING = 1.40;
    private static final double EXP_ACTIVE_DAYS = 1.00;

    final GigScoreRepository gigScoreRepository;
    final UserMetricsService userMetricsService;

    public GigScoreService(GigScoreRepository gigScoreRepository, UserMetricsService userMetricsService) {
        this.gigScoreRepository = gigScoreRepository;
        this.userMetricsService = userMetricsService;
    }

    @Transactional
    public ScoreResponse calculateAndPersistScore(User user) {
        UserAggregateMetrics metrics = userMetricsService.calculateAggregates(user);
        double score = calculateScoreValue(metrics);

        GigScore gigScore = resolveOrCreateUserGigScore(user);
        gigScore.setUser(user);
        gigScore.setScore(score);
        gigScoreRepository.save(gigScore);

        return ScoreResponse.builder()
                .userId(user.getUserId())
                .score(score)
                .build();
    }

    public double calculateScoreValue(UserAggregateMetrics metrics) {
        double earningsComponent = Math.pow(normalizedComponent(metrics.totalEarnings(), EARNINGS_TARGET), EXP_EARNINGS)
            * WEIGHT_EARNINGS;
        double jobsComponent = Math.pow(normalizedComponent(metrics.jobsCompleted(), JOBS_TARGET), EXP_JOBS)
            * WEIGHT_JOBS;
        double ratingComponent = Math.pow(normalizedComponent(clamp(metrics.avgRating(), 0.0, 5.0), 5.0), EXP_RATING)
            * WEIGHT_RATING;
        double activeDaysComponent = Math.pow(normalizedComponent(metrics.activeDays(), ACTIVE_DAYS_TARGET),
            EXP_ACTIVE_DAYS) * WEIGHT_ACTIVE_DAYS;

        double score = (earningsComponent + jobsComponent + ratingComponent + activeDaysComponent) * MAX_SCORE;
        return roundToTwoDecimals(score);
    }

    public ScoreResponse getScoreForUser(User user) {
        return calculateAndPersistScore(user);

    }

    private double normalizedComponent(double value, double target) {
        if (target <= 0) {
            return 0;
        }
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
