package com.org.gigscore.controller;

import com.org.gigscore.dto.AiCoachRequest;
import com.org.gigscore.dto.AiCoachResponse;
import com.org.gigscore.dto.AiGoalRequest;
import com.org.gigscore.dto.AiGoalResponse;
import com.org.gigscore.entity.User;
import com.org.gigscore.exception.BadRequestException;
import com.org.gigscore.exception.ResourceNotFoundException;
import com.org.gigscore.exception.UnauthorizedAccessException;
import com.org.gigscore.repository.UserRepository;
import com.org.gigscore.security.CurrentUserResolver;
import com.org.gigscore.service.AiCoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coach")
@Tag(name = "AI Coach", description = "AI financial coach insights and goal setting")
public class AiCoachController {

    private final AiCoachService aiCoachService;
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;

    public AiCoachController(AiCoachService aiCoachService, UserRepository userRepository, CurrentUserResolver currentUserResolver) {
        this.aiCoachService = aiCoachService;
        this.userRepository = userRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Operation(summary = "Analyze user metrics and provide AI coach insights")
    @PostMapping("/analyze")
    public ResponseEntity<AiCoachResponse> analyzeUser(@RequestParam(required = false) Long userId, @RequestBody(required = false) AiCoachRequest request) {
        User currentUser = currentUserResolver.getCurrentUser();
        Long targetUserId = (userId != null) ? userId : currentUser.getUserId();

        if (!targetUserId.equals(currentUser.getUserId())) {
            throw new UnauthorizedAccessException("You are not authorized to view coach insights for this user.");
        }

        String category = (request != null && request.getCategory() != null) ? request.getCategory() : "general";
        AiCoachResponse response = aiCoachService.analyzeUser(currentUser, category);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set a target credit score goal")
    @PostMapping("/set-goal")
    public ResponseEntity<AiGoalResponse> setGoal(@RequestParam(required = false) Long userId, @RequestBody AiGoalRequest request) {
        if (request == null || request.getTargetScore() == null) {
            throw new BadRequestException("Target score is required.");
        }

        User currentUser = currentUserResolver.getCurrentUser();
        Long targetUserId = (userId != null) ? userId : currentUser.getUserId();

        if (!targetUserId.equals(currentUser.getUserId())) {
            throw new UnauthorizedAccessException("You are not authorized to set goals for this user.");
        }

        AiGoalResponse response = aiCoachService.setGoal(currentUser, request.getTargetScore());
        return ResponseEntity.ok(response);
    }
}
