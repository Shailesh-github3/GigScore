package com.org.gigscore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.gigscore.config.JwtUtil;
import com.org.gigscore.dto.CreateUserRequest;
import com.org.gigscore.dto.LoginDTO;
import com.org.gigscore.dto.LoginResponseDTO;
import com.org.gigscore.dto.UserDashboardResponse;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.UserRepository;
import com.org.gigscore.exception.DuplicateResourceException;
import com.org.gigscore.exception.UnauthorizedException;



@Service
public class UserService {

    final UserRepository userRepository;
    final GigDataService gigDataService;
    private final JwtUtil jwtUtil;
        
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, GigDataService gigDataService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.gigDataService = gigDataService;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public ResponseEntity<LoginResponseDTO> createUser(CreateUserRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateResourceException("Email already exists.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return ResponseEntity.ok(
                LoginResponseDTO.builder()
                        .userId(savedUser.getUserId())
                        .name(savedUser.getName())
                        .email(savedUser.getEmail())
                        .token(token)
                        .build());
    }

    public UserDashboardResponse getUserDashboard(Long userId) {
        return gigDataService.getUserDashboard(userId);
    }

    public ResponseEntity<LoginResponseDTO> login(LoginDTO request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        String rawPassword = request.getPassword();
        String storedPassword = user.getPassword();

        boolean authenticated = false;

        if (!isBlank(storedPassword)) {
            try {
                authenticated = passwordEncoder.matches(rawPassword, storedPassword);
            } catch (IllegalArgumentException exception) {
                authenticated = false;
            }

            // Backward compatibility for legacy plaintext passwords.
            // On successful legacy auth, upgrade storage to BCrypt.
            if (!authenticated && rawPassword.equals(storedPassword)) {
                authenticated = true;
                user.setPassword(passwordEncoder.encode(rawPassword));
                userRepository.save(user);
            }
        }

        if (!authenticated) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(
                LoginResponseDTO.builder()
                        .userId(user.getUserId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .token(token)
                        .build());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
}
