package com.org.gigscore.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.org.gigscore.entity.GigData;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.ActivityRepository;
import com.org.gigscore.repository.GigDataRepository;
import com.org.gigscore.util.Money;

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

        BigDecimal totalEarnings = Money.ZERO;
        int totalJobs = 0;
        double totalRating = 0;

        for (GigData data : gigDataList) {
            int jobs = jobsOf(data);
            totalEarnings = Money.add(totalEarnings, earningsOf(data));
            totalJobs += jobs;
            totalRating += ratingOf(data) * jobs;
        }

        double avgRating = totalJobs > 0 ? totalRating / totalJobs : 0;
        int activeDays = countDistinctActiveDays(user);

        return new UserAggregateMetrics(totalEarnings, totalJobs, avgRating, activeDays);
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

    private int countDistinctActiveDays(User user) {
        Set<LocalDate> distinctDays = activityRepository.findByUser(user).stream()
                .map(activity -> activity.getTimestamp().toLocalDate())
                .collect(Collectors.toSet());
        return distinctDays.size();
    }

    public record UserAggregateMetrics(BigDecimal totalEarnings, int jobsCompleted, double avgRating, int activeDays) {
    }
}
