package com.org.gigscore.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.org.gigscore.entity.GigData;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.ActivityRepository;
import com.org.gigscore.repository.GigDataRepository;

@Service
public class UserMetricsService {

    private final GigDataRepository gigDataRepository;
    private final ActivityRepository activityRepository;

    public UserMetricsService(GigDataRepository gigDataRepository, ActivityRepository activityRepository) {
        this.gigDataRepository = gigDataRepository;
        this.activityRepository = activityRepository;
    }

    public UserAggregateMetrics calculateAggregates(User user) {
        List<GigData> gigDataList = gigDataRepository.findByUser(user);

        double totalEarnings = 0;
        int totalJobs = 0;
        double totalRating = 0;

        for (GigData data : gigDataList) {
            totalEarnings += data.getTotalEarnings();
            totalJobs += data.getJobsCompleted();
            totalRating += data.getAvgRating() * data.getJobsCompleted();
        }

        double avgRating = totalJobs > 0 ? totalRating / totalJobs : 0;
        int activeDays = countDistinctActiveDays(user);

        return new UserAggregateMetrics(totalEarnings, totalJobs, avgRating, activeDays);
    }

    private int countDistinctActiveDays(User user) {
        Set<LocalDate> distinctDays = activityRepository.findByUser(user).stream()
                .map(activity -> activity.getTimestamp().toLocalDate())
                .collect(Collectors.toSet());
        return distinctDays.size();
    }

    public record UserAggregateMetrics(double totalEarnings, int jobsCompleted, double avgRating, int activeDays) {
    }
}
