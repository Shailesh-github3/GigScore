package com.org.gigscore.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.dto.ActivityResponse;
import com.org.gigscore.service.ActivityService;
import com.org.gigscore.exception.UnauthorizedAccessException;
import com.org.gigscore.security.CurrentUserResolver;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityService activityService;
    private final CurrentUserResolver currentUserResolver;

    public ActivityController(ActivityService activityService, CurrentUserResolver currentUserResolver) {
        this.activityService = activityService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/{userId}")
    public List<ActivityResponse> getRecentActivities(@PathVariable Long userId) {
        if (!currentUserResolver.getCurrentUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied.");
        }
        return activityService.getLatestActivities(userId);
    }
}