package com.org.gigscore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.org.gigscore.dto.GigEventRequest;
import com.org.gigscore.dto.GigSummaryResponse;
import com.org.gigscore.dto.UserDashboardResponse;
import com.org.gigscore.entity.GigData;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.GigDataRepository;
import com.org.gigscore.repository.UserRepository;
import com.org.gigscore.service.UserMetricsService.UserAggregateMetrics;
import com.org.gigscore.exception.BadRequestException;
import com.org.gigscore.exception.ResourceNotFoundException;
import com.org.gigscore.util.Money;
import com.org.gigscore.util.Platform;

@Service
public class GigDataService {
    
    final GigDataRepository gigDataRepository;
    final UserRepository userRepository;
    final GigScoreService scoreService;
    final UserMetricsService userMetricsService;
    final ActivityService activityService;
    private final TransactionTemplate transactionTemplate;

    public GigDataService(
            GigDataRepository gigDataRepository,
            UserRepository userRepository,
            GigScoreService scoreService,
            UserMetricsService userMetricsService,
            ActivityService activityService,
            @Autowired(required = false) PlatformTransactionManager transactionManager) {
        this.gigDataRepository = gigDataRepository;
        this.userRepository = userRepository;
        this.scoreService = scoreService;
        this.userMetricsService = userMetricsService;
        this.activityService = activityService;
        this.transactionTemplate = transactionManager != null ? new TransactionTemplate(transactionManager) : null;
    }

    @CacheEvict(value = "scores", key = "#request.userId")
    public UserDashboardResponse addGig(GigEventRequest request) {
        if (request.getUserId() == null) {
            throw new BadRequestException("userId is required");
        }

        int maxAttempts = 5;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return executeInTransaction(() -> executeAddGig(request));
            } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(15L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Concurrent gig submission retry interrupted", ie);
                }
            }
        }
        throw new IllegalStateException("Exhausted retry attempts for gig submission");
    }

    private UserDashboardResponse executeInTransaction(java.util.function.Supplier<UserDashboardResponse> action) {
        if (transactionTemplate != null) {
            return transactionTemplate.execute(status -> action.get());
        }
        return action.get();
    }

    private UserDashboardResponse executeAddGig(GigEventRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String normalizedPlatform = Platform.normalize(request.getPlatform());

        GigData data = resolveOrCreatePlatformAggregate(user, normalizedPlatform);

        data.setTotalEarnings(Money.add(earningsOf(data), request.getAmount()));
        data.setJobsCompleted(data.getJobsCompleted() + 1);

        int oldCount = data.getJobsCompleted() - 1;

        double newAvg = ((data.getAvgRating() * oldCount) + request.getRating())
                / data.getJobsCompleted();

        data.setAvgRating(newAvg);

        gigDataRepository.save(data);
        activityService.recordGigAdded(user, normalizedPlatform, request.getAmount(), request.getRating());

        scoreService.calculateAndPersistScore(user);

        return getUserDashboard(user.getUserId());
    }

        private GigData resolveOrCreatePlatformAggregate(User user, String platform) {
                List<GigData> rows = new ArrayList<>(gigDataRepository.findAllByUserAndPlatform(user, platform));

                if (rows.isEmpty()) {
                        GigData g = new GigData();
                        g.setUser(user);
                        g.setPlatform(platform);
                        g.setTotalEarnings(Money.ZERO);
                        g.setJobsCompleted(0);
                        g.setAvgRating(0.0);
                        return g;
                }

                GigData primary = rows.get(0);
                if (rows.size() == 1) {
                        return primary;
                }

                BigDecimal mergedEarnings = Money.ZERO;
                int mergedJobs = 0;
                double weightedRatingTotal = 0.0;

                for (GigData row : rows) {
                        BigDecimal earnings = earningsOf(row);
                        int jobs = jobsOf(row);
                        double avgRating = ratingOf(row);

                        mergedEarnings = Money.add(mergedEarnings, earnings);
                        mergedJobs += jobs;
                        weightedRatingTotal += avgRating * jobs;
                }

                primary.setTotalEarnings(mergedEarnings);
                primary.setJobsCompleted(mergedJobs);
                primary.setAvgRating(mergedJobs > 0 ? weightedRatingTotal / mergedJobs : 0.0);

                for (int i = 1; i < rows.size(); i++) {
                        gigDataRepository.delete(rows.get(i));
                }

                return primary;
        }

    @Transactional(readOnly = true)
    public UserDashboardResponse getUserDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserAggregateMetrics metrics = userMetricsService.calculateAggregates(user);
        double score = scoreService.getScoreForUser(user).getScore();

        List<GigSummaryResponse> gigSummaries = gigDataRepository.findByUser(user).stream()
                .map(this::toGigSummaryResponse)
                .collect(Collectors.toList());

        return UserDashboardResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .totalEarnings(metrics.totalEarnings())
                .jobsCompleted(metrics.jobsCompleted())
                .avgRating(metrics.avgRating())
                .activeDays(metrics.activeDays())
                .score(score)
                .gigSummaries(gigSummaries)
                .build();
    }

    private GigSummaryResponse toGigSummaryResponse(GigData gigData) {
        return GigSummaryResponse.builder()
                .platform(gigData.getPlatform())
                .totalEarnings(earningsOf(gigData))
                .jobsCompleted(jobsOf(gigData))
                .avgRating(ratingOf(gigData))
                .build();
    }

    private BigDecimal earningsOf(GigData data) {
        return Money.of(data.getTotalEarnings());
    }

    private int jobsOf(GigData data) {
        return data.getJobsCompleted() == null ? 0 : data.getJobsCompleted();
    }

    private double ratingOf(GigData data) {
        return data.getAvgRating() == null ? 0.0 : data.getAvgRating();
    }
    

}
