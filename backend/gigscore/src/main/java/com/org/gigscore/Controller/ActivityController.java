package com.org.gigscore.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Activity", description = "Recent gig activity feed")
public class ActivityController {

    private final ActivityService activityService;
    private final CurrentUserResolver currentUserResolver;

    public ActivityController(ActivityService activityService, CurrentUserResolver currentUserResolver) {
        this.activityService = activityService;
        this.currentUserResolver = currentUserResolver;
    }

    @Operation(summary = "Get the 5 most recent activity entries for a user")
    @GetMapping("/{userId}")
    public List<ActivityResponse> getRecentActivities(@PathVariable Long userId) {
        if (!currentUserResolver.getCurrentUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied.");
        }
        return activityService.getLatestActivities(userId);
    }
}