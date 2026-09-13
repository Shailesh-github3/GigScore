package com.org.gigscore.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.org.gigscore.config.JwtUtil;
import com.org.gigscore.entity.User;
import com.org.gigscore.entity.UserRole;
import com.org.gigscore.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LenderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void worker_cannotAccess_lenderEndpoints() throws Exception {
        String token = tokenFor(createRoleUser(UserRole.WORKER));

        mockMvc.perform(get("/api/v1/lender/workers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void lender_canAccess_workerList() throws Exception {
        User worker = createRoleUser(UserRole.WORKER);
        String token = tokenFor(createRoleUser(UserRole.LENDER));

        mockMvc.perform(get("/api/v1/lender/workers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userId == " + worker.getUserId() + ")].band").value(hasItem("EARLY")))
                .andExpect(jsonPath("$[?(@.userId == " + worker.getUserId() + ")].score").value(hasItem(0.0)));
    }

    @Test
    void lender_canAccess_singleWorker() throws Exception {
        User worker = createRoleUser(UserRole.WORKER);
        String token = tokenFor(createRoleUser(UserRole.LENDER));

        mockMvc.perform(get("/api/v1/lender/workers/" + worker.getUserId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(worker.getUserId()))
                .andExpect(jsonPath("$.score").value(0.0))
                .andExpect(jsonPath("$.scoreVersion").value("v1"))
                .andExpect(jsonPath("$.band").value("EARLY"))
                .andExpect(jsonPath("$.components.earnings").value(0.0))
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.dataQuality.confidence").value("LOW"))
                .andExpect(jsonPath("$.history").isArray());
    }

    private User createRoleUser(UserRole role) {
        User user = new User();
        user.setName(role == UserRole.LENDER ? "Lender" : "Worker");
        user.setEmail(role.name().toLowerCase() + "_" + System.nanoTime() + "@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        return userRepository.save(user);
    }

    private String tokenFor(User user) {
        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }
}