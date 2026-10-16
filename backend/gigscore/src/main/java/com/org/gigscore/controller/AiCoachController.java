package com.org.gigscore.controller;

import com.org.gigscore.dto.AiCoachRequest;
import com.org.gigscore.dto.AiCoachResponse;
import com.org.gigscore.dto.AiGoalRequest;
import com.org.gigscore.dto.AiGoalResponse;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.UserRepository;
import com.org.gigscore.service.AiCoachService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coach")
@CrossOrigin(origins = "*") // Assuming React is running on localhost:5173
public class AiCoachController {

    private final AiCoachService aiCoachService;
    private final UserRepository userRepository;

    public AiCoachController(AiCoachService aiCoachService, UserRepository userRepository) {
        this.aiCoachService = aiCoachService;
        this.userRepository = userRepository;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AiCoachResponse> analyzeUser(@RequestParam Long userId, @RequestBody AiCoachRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        AiCoachResponse response = aiCoachService.analyzeUser(user, request.getCategory());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/set-goal")
    public ResponseEntity<AiGoalResponse> setGoal(@RequestParam Long userId, @RequestBody AiGoalRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        AiGoalResponse response = aiCoachService.setGoal(user, request.getTargetScore());
        return ResponseEntity.ok(response);
    }
}
