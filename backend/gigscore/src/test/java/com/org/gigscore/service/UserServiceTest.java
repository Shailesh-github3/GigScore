package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.org.gigscore.config.JwtUtil;
import com.org.gigscore.dto.CreateUserRequest;
import com.org.gigscore.dto.LoginDTO;
import com.org.gigscore.dto.LoginResponseDTO;
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

    @Test
    void createUser_registersWorkerWithHashedPassword() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("  Alice  ");
        request.setEmail("Alice@Example.com");
        request.setPassword("pass123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserId(7L);
            return saved;
        });
        when(jwtUtil.generateToken("alice@example.com", "WORKER")).thenReturn("TOKEN");

        ResponseEntity<LoginResponseDTO> response = userService.createUser(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        LoginResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getUserId()).isEqualTo(7L);
        assertThat(body.getName()).isEqualTo("Alice");
        assertThat(body.getEmail()).isEqualTo("alice@example.com");
        assertThat(body.getRole()).isEqualTo("WORKER");
        assertThat(body.getToken()).isEqualTo("TOKEN");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.WORKER);
        assertThat(captor.getValue().getPassword()).isEqualTo("HASHED");
        assertThat(captor.getValue().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void createUser_duplicateEmail_throwsWithoutSaving() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("pass123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_successReturnsTokenAndRole() {
        User user = new User();
        user.setUserId(9L);
        user.setName("Lena");
        user.setEmail("lena@example.com");
        user.setPassword("HASHED");
        user.setRole(UserRole.LENDER);

        LoginDTO request = new LoginDTO();
        request.setEmail("lena@example.com");
        request.setPassword("raw-password");

        when(userRepository.findByEmail("lena@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "HASHED")).thenReturn(true);
        when(jwtUtil.generateToken("lena@example.com", "LENDER")).thenReturn("TOKEN");

        ResponseEntity<LoginResponseDTO> response = userService.login(request);

        LoginResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getUserId()).isEqualTo(9L);
        assertThat(body.getRole()).isEqualTo("LENDER");
        assertThat(body.getToken()).isEqualTo("TOKEN");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = new User();
        user.setEmail("bob@example.com");
        user.setPassword("HASHED");
        user.setRole(UserRole.WORKER);

        LoginDTO request = new LoginDTO();
        request.setEmail("bob@example.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASHED")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    @Test
    void login_legacyPlaintextPassword_upgradesToBcrypt() {
        User user = new User();
        user.setUserId(3L);
        user.setEmail("old@example.com");
        user.setPassword("pass123");
        user.setRole(UserRole.WORKER);

        LoginDTO request = new LoginDTO();
        request.setEmail("old@example.com");
        request.setPassword("pass123");

        when(userRepository.findByEmail("old@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "pass123")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("NEWHASH");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("old@example.com", "WORKER")).thenReturn("TOKEN");

        ResponseEntity<LoginResponseDTO> response = userService.login(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("TOKEN");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("NEWHASH");
    }

    @Test
    void login_unknownEmail_throwsUnauthorized() {
        LoginDTO request = new LoginDTO();
        request.setEmail("nobody@example.com");
        request.setPassword("whatever");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }
}