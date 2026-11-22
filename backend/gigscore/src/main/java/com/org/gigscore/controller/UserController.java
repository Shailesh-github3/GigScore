package com.org.gigscore.controller;

import java.util.Date;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.gigscore.config.JwtUtil;
import com.org.gigscore.dto.CreateUserRequest;
import com.org.gigscore.dto.LoginDTO;
import com.org.gigscore.dto.LoginResponseDTO;
import com.org.gigscore.dto.UserDashboardResponse;
import com.org.gigscore.exception.UnauthorizedAccessException;
import com.org.gigscore.security.CurrentUserResolver;
import com.org.gigscore.security.TokenBlacklistService;
import com.org.gigscore.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Registration, login, logout and user dashboard")
public class UserController {

    private static final String COOKIE_NAME = "gigscoreToken";

    private final UserService userService;
    private final CurrentUserResolver currentUserResolver;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService,
                          CurrentUserResolver currentUserResolver,
                          TokenBlacklistService tokenBlacklistService,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.currentUserResolver = currentUserResolver;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Register a new user, receive a JWT token, and set HttpOnly auth cookie")
    @PostMapping
    public ResponseEntity<LoginResponseDTO> createUser(@Valid @RequestBody CreateUserRequest request,
                                                       HttpServletResponse response) {
        ResponseEntity<LoginResponseDTO> result = userService.createUser(request);
        if (result.getBody() != null && result.getBody().getToken() != null) {
            response.addHeader(HttpHeaders.SET_COOKIE, createAuthCookie(result.getBody().getToken(), 3600).toString());
        }
        return result;
    }

    @Operation(summary = "Get user dashboard with score breakdown and gig summaries")
    @GetMapping("/{userId}")
    public UserDashboardResponse getUserDetails(@PathVariable Long userId) {
        if (!currentUserResolver.getCurrentUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied.");
        }
        return userService.getUserDashboard(userId);
    }

    @Operation(summary = "Login with email and password to receive a JWT token and set HttpOnly auth cookie")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO request,
                                                  HttpServletResponse response) {
        ResponseEntity<LoginResponseDTO> result = userService.login(request);
        if (result.getBody() != null && result.getBody().getToken() != null) {
            response.addHeader(HttpHeaders.SET_COOKIE, createAuthCookie(result.getBody().getToken(), 3600).toString());
        }
        return result;
    }

    @Operation(summary = "Logout user, revoke JWT token in blacklist, and clear auth cookie")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = resolveToken(request);
        if (token != null && !token.isBlank()) {
            Date expiration = jwtUtil.extractExpiration(token);
            tokenBlacklistService.blacklist(token, expiration);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, createAuthCookie("", 0).toString());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    private ResponseCookie createAuthCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(COOKIE_NAME, token != null ? token : "")
                .httpOnly(true)
                .secure(false) // Set to true for HTTPS-only; false allows local dev over HTTP
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
