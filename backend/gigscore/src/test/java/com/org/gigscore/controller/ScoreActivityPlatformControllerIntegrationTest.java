package com.org.gigscore.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
class ScoreActivityPlatformControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void score_returnsShapeForOwnUser() throws Exception {
        User worker = createWorker();
        String token = tokenFor(worker);

        mockMvc.perform(get("/api/v1/score/" + worker.getUserId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(worker.getUserId()))
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.scoreVersion").value("v1"))
                .andExpect(jsonPath("$.band").value("EARLY"))
                .andExpect(jsonPath("$.components").exists())
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.dataQuality.confidence").value("LOW"));
    }

    @Test
    void score_crossUser_returns403() throws Exception {
        User alice = createWorker();
        User bob = createWorker();

        mockMvc.perform(get("/api/v1/score/" + bob.getUserId())
                        .header("Authorization", "Bearer " + tokenFor(alice)))
                .andExpect(status().isForbidden());
    }

    @Test
    void scoreHistory_returnsArray() throws Exception {
        User worker = createWorker();

        mockMvc.perform(get("/api/v1/score/" + worker.getUserId() + "/history")
                        .header("Authorization", "Bearer " + tokenFor(worker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void activity_returnsPagedMetadata() throws Exception {
        User worker = createWorker();

        mockMvc.perform(get("/api/v1/activity/" + worker.getUserId() + "?page=0&size=5")
                        .header("Authorization", "Bearer " + tokenFor(worker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void activity_invalidPagination_returns400() throws Exception {
        User worker = createWorker();

        mockMvc.perform(get("/api/v1/activity/" + worker.getUserId() + "?page=-1&size=5")
                        .header("Authorization", "Bearer " + tokenFor(worker)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/activity/" + worker.getUserId() + "?page=0&size=51")
                        .header("Authorization", "Bearer " + tokenFor(worker)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void platforms_listsAllowedValues() throws Exception {
        User worker = createWorker();

        mockMvc.perform(get("/api/v1/platforms")
                        .header("Authorization", "Bearer " + tokenFor(worker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasItem("Upwork")))
                .andExpect(jsonPath("$").value(hasItem("Uber")));
    }

    @Test
    void chat_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/chat/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messages\":[]}"))
                .andExpect(status().isUnauthorized());
    }

    private User createWorker() {
        User user = new User();
        user.setName("Worker");
        user.setEmail("worker_" + System.nanoTime() + "@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(UserRole.WORKER);
        return userRepository.save(user);
    }

    private String tokenFor(User user) {
        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }
}
