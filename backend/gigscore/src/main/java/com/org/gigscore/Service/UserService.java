package com.org.gigscore.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.org.gigscore.Config.JWTutill;
import com.org.gigscore.DTO.CreateUserRequest;
import com.org.gigscore.DTO.LoginDTO;
import com.org.gigscore.DTO.LoginResponseDTO;
import com.org.gigscore.DTO.UserDashboardResponse;
import com.org.gigscore.Entity.User;
import com.org.gigscore.Repository.UserRepository;
import com.org.gigscore.exception.BadRequestException;
import com.org.gigscore.exception.DuplicateResourceException;
import com.org.gigscore.exception.UnauthorizedException;



@Service
public class UserService {

    final UserRepository userRepository;
    final GigDataService gigDataService;
    private final JWTutill jwtUtil;
        
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, GigDataService gigDataService, JWTutill jwtUtil) {
        this.userRepository = userRepository;
        this.gigDataService = gigDataService;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<LoginResponseDTO> createUser(CreateUserRequest request) {
        if (request == null || isBlank(request.getName()) || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw new BadRequestException("Name, email and password are required.");
        }

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

    public ResponseEntity<LoginResponseDTO> Login(LoginDTO request) {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw new BadRequestException("Email and password are required.");
        }

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
