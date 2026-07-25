package com.org.gigscore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.dto.CreateUserRequest;
import com.org.gigscore.dto.LoginDTO;
import com.org.gigscore.dto.LoginResponseDTO;
import com.org.gigscore.dto.UserDashboardResponse;
import com.org.gigscore.service.UserService;
import com.org.gigscore.exception.UnauthorizedAccessException;
import com.org.gigscore.security.CurrentUserResolver;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Registration, login and user dashboard")
public class UserController {

    final UserService userService;
    final CurrentUserResolver currentUserResolver;
    public UserController(UserService userService, CurrentUserResolver currentUserResolver) {
        this.userService = userService;
        this.currentUserResolver = currentUserResolver;
    }

    @Operation(summary = "Register a new user and receive a JWT token")
    @PostMapping
    public ResponseEntity<LoginResponseDTO> createUser(@Valid @RequestBody CreateUserRequest request){
        return userService.createUser(request);
    }

    @Operation(summary = "Get user dashboard with score breakdown and gig summaries")
    @GetMapping("/{userId}")
    public UserDashboardResponse getUserDetails(@PathVariable Long userId) {
        if (!currentUserResolver.getCurrentUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied.");
        }
        return userService.getUserDashboard(userId);
    }

    @Operation(summary = "Login with email and password to receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO request) {
        return userService.login(request);
    }

}