package com.org.gigscore.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.gigscore.config.JwtUtil;
import com.org.gigscore.dto.CreateUserRequest;
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
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private record RegisteredUser(String token, long userId) {}

    private RegisteredUser registerWorker(String email) throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Worker");
        req.setEmail(email);
        req.setPassword("pass123");
        String body = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var node = objectMapper.readTree(body);
        return new RegisteredUser(node.get("token").asText(), node.get("userId").asLong());
    }

    private String createLenderToken(String email) {
        User lender = new User();
        lender.setName("Lender");
        lender.setEmail(email);
        lender.setPassword("unused-hash");
        lender.setRole(UserRole.LENDER);
        userRepository.save(lender);
        return jwtUtil.generateToken(email, "LENDER");
    }

    @Test
    void workerRole_cannotAccessLenderEndpoint() throws Exception {
        String workerToken = registerWorker("worker403@example.com").token();

        mockMvc.perform(get("/api/v1/lender/workers")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void lenderRole_canListAndViewWorkers() throws Exception {
        RegisteredUser worker = registerWorker("worker-ok@example.com");
        String lenderToken = createLenderToken("lender-ok@example.com");

        mockMvc.perform(get("/api/v1/lender/workers")
                        .header("Authorization", "Bearer " + lenderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").isNumber())
                .andExpect(jsonPath("$.content[0].name").isString())
                .andExpect(jsonPath("$.content[0].dataQuality.confidence").isString());

        mockMvc.perform(get("/api/v1/lender/workers/" + worker.userId())
                        .header("Authorization", "Bearer " + lenderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.band").isString())
                .andExpect(jsonPath("$.recentActivities").isArray());
    }
}