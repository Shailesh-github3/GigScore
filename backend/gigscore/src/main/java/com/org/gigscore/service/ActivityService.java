package com.org.gigscore.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.gigscore.dto.ActivityResponse;
import com.org.gigscore.dto.PagedActivities;
import com.org.gigscore.entity.Activity;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.ActivityRepository;
import com.org.gigscore.repository.UserRepository;
import com.org.gigscore.exception.ResourceNotFoundException;
import com.org.gigscore.util.Money;

@Service
public class ActivityService {

    static final int MAX_PAGE_SIZE = 50;

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void recordGigAdded(User user, String platform, BigDecimal amount, Double rating) {
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setPlatform(platform);
        activity.setAction("Added gig");
        activity.setAmount(Money.of(amount));
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

    public PagedActivities getActivities(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Page<Activity> result = activityRepository.findByUserOrderByTimestampDesc(user, PageRequest.of(safePage, safeSize));
        List<ActivityResponse> content = result.getContent().stream()
                .map(this::toActivityResponse)
                .toList();

        return PagedActivities.builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(result.getTotalElements())
                .build();
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
