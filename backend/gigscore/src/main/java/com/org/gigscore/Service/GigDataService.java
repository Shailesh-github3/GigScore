package com.org.gigscore.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.gigscore.DTO.GigEventRequest;
import com.org.gigscore.DTO.GigSummaryResponse;
import com.org.gigscore.DTO.UserDashboardResponse;
import com.org.gigscore.Entity.GigData;
import com.org.gigscore.Entity.User;
import com.org.gigscore.Repository.GigDataRepository;
import com.org.gigscore.Repository.UserRepository;
import com.org.gigscore.Service.UserMetricsService.UserAggregateMetrics;
import com.org.gigscore.exception.BadRequestException;
import com.org.gigscore.exception.ResourceNotFoundException;

@Service
public class GigDataService {
    
    final GigDataRepository gigDataRepository;
    final UserRepository userRepository;
    final GigScoreService scoreService;
    final UserMetricsService userMetricsService;
        final ActivityService activityService;

    public GigDataService(
            GigDataRepository gigDataRepository,
            UserRepository userRepository,
            GigScoreService scoreService,
                        UserMetricsService userMetricsService,
                        ActivityService activityService) {
        this.gigDataRepository = gigDataRepository;
        this.userRepository = userRepository;
        this.scoreService = scoreService;
        this.userMetricsService = userMetricsService;
                this.activityService = activityService;
    }

        @Transactional
        public UserDashboardResponse addGig(GigEventRequest request){

        if (request.getUserId() == null) {
            throw new BadRequestException("userId is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                String normalizedPlatform = request.getPlatform().trim();

                GigData data = resolveOrCreatePlatformAggregate(user, normalizedPlatform);

        data.setTotalEarnings(data.getTotalEarnings() + request.getAmount());
        data.setJobsCompleted(data.getJobsCompleted() + 1);
        data.setActiveDays(data.getActiveDays() + 1);

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
                        g.setTotalEarnings(0.0);
                        g.setJobsCompleted(0);
                        g.setAvgRating(0.0);
                        g.setActiveDays(0);
                        return g;
                }

                GigData primary = rows.get(0);
                if (rows.size() == 1) {
                        return primary;
                }

                double mergedEarnings = 0.0;
                int mergedJobs = 0;
                int mergedActiveDays = 0;
                double weightedRatingTotal = 0.0;

                for (GigData row : rows) {
                        double earnings = row.getTotalEarnings() == null ? 0.0 : row.getTotalEarnings();
                        int jobs = row.getJobsCompleted() == null ? 0 : row.getJobsCompleted();
                        int activeDays = row.getActiveDays() == null ? 0 : row.getActiveDays();
                        double avgRating = row.getAvgRating() == null ? 0.0 : row.getAvgRating();

                        mergedEarnings += earnings;
                        mergedJobs += jobs;
                        mergedActiveDays += activeDays;
                        weightedRatingTotal += avgRating * jobs;
                }

                primary.setTotalEarnings(mergedEarnings);
                primary.setJobsCompleted(mergedJobs);
                primary.setActiveDays(mergedActiveDays);
                primary.setAvgRating(mergedJobs > 0 ? weightedRatingTotal / mergedJobs : 0.0);

                for (int i = 1; i < rows.size(); i++) {
                        gigDataRepository.delete(rows.get(i));
                }

                return primary;
        }

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
                .totalEarnings(gigData.getTotalEarnings())
                .jobsCompleted(gigData.getJobsCompleted())
                .avgRating(gigData.getAvgRating())
                .activeDays(gigData.getActiveDays())
                .build();
    }
    

}
