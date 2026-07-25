package com.org.gigscore.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.DTO.ScoreResponse;
import com.org.gigscore.Entity.User;
import com.org.gigscore.Repository.UserRepository;
import com.org.gigscore.Service.GigScoreService;
import com.org.gigscore.exception.ResourceNotFoundException;
import com.org.gigscore.exception.UnauthorizedAccessException;
import com.org.gigscore.security.CurrentUserResolver;

@RestController
public class GigScoreController {
    final GigScoreService gigScoreService;
    final UserRepository userRepository;
    final CurrentUserResolver currentUserResolver;
    public GigScoreController(GigScoreService gigScoreService, UserRepository userRepository, CurrentUserResolver currentUserResolver) {
        this.gigScoreService = gigScoreService;
        this.userRepository = userRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/score/{userId}")
    public ScoreResponse getScore(@PathVariable Long userId){
        if (!currentUserResolver.getCurrentUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return gigScoreService.getScoreForUser(user);
    }
}