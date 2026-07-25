package com.org.gigscore.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.org.gigscore.entity.GigData;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.GigDataRepository;

@Service
public class UserMetricsService {

    private final GigDataRepository gigDataRepository;

    public UserMetricsService(GigDataRepository gigDataRepository) {
        this.gigDataRepository = gigDataRepository;
    }

    public UserAggregateMetrics calculateAggregates(User user) {
        List<GigData> gigDataList = gigDataRepository.findByUser(user);

        double totalEarnings = 0;
        int totalJobs = 0;
        double totalRating = 0;
        int totalActiveDays = 0;

        for (GigData data : gigDataList) {
            totalEarnings += data.getTotalEarnings();
            totalJobs += data.getJobsCompleted();
            totalRating += data.getAvgRating() * data.getJobsCompleted();
            totalActiveDays += data.getActiveDays();
        }

        double avgRating = totalJobs > 0 ? totalRating / totalJobs : 0;

        return new UserAggregateMetrics(totalEarnings, totalJobs, avgRating, totalActiveDays);
    }

    public record UserAggregateMetrics(double totalEarnings, int jobsCompleted, double avgRating, int activeDays) {
    }
}
