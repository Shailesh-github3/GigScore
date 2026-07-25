package com.org.gigscore.controller;

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
public class UserController {

    final UserService userService;
    final CurrentUserResolver currentUserResolver;
    public UserController(UserService userService, CurrentUserResolver currentUserResolver) {
        this.userService = userService;
        this.currentUserResolver = currentUserResolver;
    }

    @PostMapping
    public ResponseEntity<LoginResponseDTO> createUser(@Valid @RequestBody CreateUserRequest request){
        return userService.createUser(request);
    }

    @GetMapping("/{userId}")
    public UserDashboardResponse getUserDetails(@PathVariable Long userId) {
        if (!currentUserResolver.getCurrentUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied.");
        }
        return userService.getUserDashboard(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO request) {
        return userService.login(request);
    }

}