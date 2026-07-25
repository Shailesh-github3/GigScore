package com.org.gigscore.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.org.gigscore.DTO.ActivityResponse;
import com.org.gigscore.Entity.Activity;
import com.org.gigscore.Entity.User;
import com.org.gigscore.Repository.ActivityRepository;
import com.org.gigscore.Repository.UserRepository;
import com.org.gigscore.exception.ResourceNotFoundException;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public void recordGigAdded(User user, String platform, Double amount, Double rating) {
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setPlatform(platform);
        activity.setAction("Added gig");
        activity.setAmount(amount);
        activity.setRating(rating);
        activity.setTimestamp(LocalDateTime.now());
        activityRepository.save(activity);
    }

    public List<ActivityResponse> getLatestActivities(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return activityRepository.findTop5ByUserOrderByTimestampDesc(user)
                .stream()
                .map(this::toActivityResponse)
                .toList();
    }

    private ActivityResponse toActivityResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .userId(activity.getUser().getUserId())
                .platform(activity.getPlatform())
                .action(activity.getAction())
                .amount(activity.getAmount())
                .rating(activity.getRating())
                .timestamp(activity.getTimestamp())
                .build();
    }
}
