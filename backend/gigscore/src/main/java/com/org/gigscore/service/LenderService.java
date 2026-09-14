package com.org.gigscore.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.gigscore.dto.ActivityResponse;
import com.org.gigscore.dto.DataQualityProfile;
import com.org.gigscore.dto.LenderWorkerDetail;
import com.org.gigscore.dto.LenderWorkerSummary;
import com.org.gigscore.dto.ScoreResponse;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.UserRepository;
import com.org.gigscore.service.UserMetricsService.UserAggregateMetrics;
import com.org.gigscore.exception.ResourceNotFoundException;

@Service
public class LenderService {

    private final UserRepository userRepository;
    private final UserMetricsService userMetricsService;
    private final GigScoreService gigScoreService;
    private final ActivityService activityService;

    public LenderService(UserRepository userRepository, UserMetricsService userMetricsService,
                         GigScoreService gigScoreService, ActivityService activityService) {
        this.userRepository = userRepository;
        this.userMetricsService = userMetricsService;
        this.gigScoreService = gigScoreService;
        this.activityService = activityService;
    }

    @Transactional(readOnly = true)
    public List<LenderWorkerSummary> getAllWorkers() {
        return userRepository.findAll().stream()
                .map(this::toSummary)
                .sorted(Comparator.comparing(LenderWorkerSummary::getScore, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public LenderWorkerDetail getWorkerDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        UserAggregateMetrics metrics = userMetricsService.calculateAggregates(user);
        ScoreResponse scoreResponse = gigScoreService.getScoreForUser(user);
        DataQualityProfile dataQuality = gigScoreService.computeDataQuality(metrics);
        List<ActivityResponse> recentActivities = activityService.getLatestActivities(userId);

        return LenderWorkerDetail.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .score(scoreResponse.getScore())
                .band(scoreResponse.getBand())
                .components(scoreResponse.getComponents())
                .suggestions(scoreResponse.getSuggestions())
                .totalEarnings(metrics.totalEarnings().doubleValue())
                .jobsCompleted(metrics.jobsCompleted())
                .activeDays(metrics.activeDays())
                .dataQuality(dataQuality)
                .recentActivities(recentActivities)
                .build();
    }

    private LenderWorkerSummary toSummary(User user) {
        UserAggregateMetrics metrics = userMetricsService.calculateAggregates(user);
        ScoreResponse scoreResponse = gigScoreService.getScoreForUser(user);
        DataQualityProfile dataQuality = gigScoreService.computeDataQuality(metrics);

        return LenderWorkerSummary.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .score(scoreResponse.getScore())
                .band(scoreResponse.getBand())
                .totalEarnings(metrics.totalEarnings().doubleValue())
                .jobsCompleted(metrics.jobsCompleted())
                .activeDays(metrics.activeDays())
                .dataQuality(dataQuality)
                .build();
    }
}