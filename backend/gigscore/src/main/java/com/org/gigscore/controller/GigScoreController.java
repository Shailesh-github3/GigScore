package com.org.gigscore.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.dto.ScoreConfig;
import com.org.gigscore.dto.ScoreHistoryEntry;
import com.org.gigscore.dto.ScoreResponse;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.UserRepository;
import com.org.gigscore.service.GigScoreService;
import com.org.gigscore.exception.ResourceNotFoundException;
import com.org.gigscore.exception.UnauthorizedAccessException;
import com.org.gigscore.security.CurrentUserResolver;

@RestController
@RequestMapping("/api/v1/score")
@Tag(name = "Score", description = "Gig performance score")
public class GigScoreController {
    final GigScoreService gigScoreService;
    final UserRepository userRepository;
    final CurrentUserResolver currentUserResolver;

    public GigScoreController(GigScoreService gigScoreService, UserRepository userRepository, CurrentUserResolver currentUserResolver) {
        this.gigScoreService = gigScoreService;
        this.userRepository = userRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Operation(summary = "Calculate and return the user's weighted gig score (0-100)")
    @GetMapping("/{userId}")
    public ScoreResponse getScore(@PathVariable Long userId) {
        if (!currentUserResolver.getCurrentUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return gigScoreService.getScoreForUser(user);
    }

    @Operation(summary = "Get the score formula configuration")
    @GetMapping("/config")
    public ScoreConfig getScoreConfig() {
        return gigScoreService.getConfig();
    }

    @Operation(summary = "Get the user's score history")
    @GetMapping("/{userId}/history")
    public List<ScoreHistoryEntry> getScoreHistory(@PathVariable Long userId) {
        if (!currentUserResolver.getCurrentUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return gigScoreService.getScoreHistory(user);
    }
}
