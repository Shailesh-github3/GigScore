package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.org.gigscore.config.JwtUtil;
import com.org.gigscore.dto.CreateUserRequest;
import com.org.gigscore.dto.LoginDTO;
import com.org.gigscore.entity.User;
import com.org.gigscore.entity.UserRole;
import com.org.gigscore.exception.DuplicateResourceException;
import com.org.gigscore.exception.UnauthorizedException;
import com.org.gigscore.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GigDataService gigDataService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User userWith(String email, UserRole role) {
        User user = new User();
        user.setUserId(1L);
        user.setName("Test User");
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    @Test
    void createUser_duplicateEmail_throwsDuplicate() {
        when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new User()));

        CreateUserRequest request = new CreateUserRequest();
        request.setName("Test");
        request.setEmail("DUP@example.com");
        request.setPassword("password123");

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_assignsWorkerRoleAndReturnsToken() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setUserId(1L);
            return user;
        });
        when(jwtUtil.generateToken("new@example.com", "WORKER")).thenReturn("jwt-token");

        CreateUserRequest request = new CreateUserRequest();
        request.setName("New User");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        var response = userService.createUser(request).getBody();

        assertThat(response.getRole()).isEqualTo("WORKER");
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("new@example.com");

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.WORKER);
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
    }

    @Test
    void login_validBcryptPassword_returnsTokenWithRole() {
        User user = userWith("lender@example.com", UserRole.LENDER);
        user.setPassword("hashed");
        when(userRepository.findByEmail("lender@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("lender@example.com", "LENDER")).thenReturn("jwt-token");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("lender@example.com");
        loginDTO.setPassword("password123");

        var response = userService.login(loginDTO).getBody();

        assertThat(response.getRole()).isEqualTo("LENDER");
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = userWith("bad@example.com", UserRole.WORKER);
        user.setPassword("hashed");
        when(userRepository.findByEmail("bad@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("bad@example.com");
        loginDTO.setPassword("wrong");

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_legacyPlaintextPassword_upgradesToBcrypt() {
        User user = userWith("legacy@example.com", UserRole.WORKER);
        user.setPassword("plaintext");
        when(userRepository.findByEmail("legacy@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plaintext", "plaintext")).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("new-hash");
        when(jwtUtil.generateToken("legacy@example.com", "WORKER")).thenReturn("jwt-token");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("legacy@example.com");
        loginDTO.setPassword("plaintext");

        var response = userService.login(loginDTO).getBody();

        assertThat(response.getToken()).isEqualTo("jwt-token");
        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("new-hash");
    }
}