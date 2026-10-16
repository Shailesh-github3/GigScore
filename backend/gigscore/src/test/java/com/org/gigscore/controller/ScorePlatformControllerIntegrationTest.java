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
import com.org.gigscore.dto.CreateUserRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScorePlatformControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private record RegisteredUser(String token, long userId) {}

    private RegisteredUser register(String email, String password) throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Alice");
        req.setEmail(email);
        req.setPassword(password);

        String body = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var node = objectMapper.readTree(body);
        return new RegisteredUser(node.get("token").asText(), node.get("userId").asLong());
    }

    @Test
    void platformsEndpoint_returnsAllowedValues() throws Exception {
        RegisteredUser user = register("alice-platforms@example.com", "pass123");
        mockMvc.perform(get("/api/v1/platforms")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@ == 'Uber')]").exists())
                .andExpect(jsonPath("$[?(@ == 'Upwork')]").exists());
    }

    @Test
    void scoreConfigEndpoint_returnsFormulaConstants() throws Exception {
        RegisteredUser user = register("alice-config@example.com", "pass123");
        mockMvc.perform(get("/api/v1/score/config")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxScore").value(100.0))
                .andExpect(jsonPath("$.weights.earnings").value(0.35))
                .andExpect(jsonPath("$.weights.rating").value(0.30))
                .andExpect(jsonPath("$.exponents.activeDays").value(1.00))
                .andExpect(jsonPath("$.earningsTarget").value(5000.0));
    }

    @Test
    void scoreAndHistory_populatedAfterGigEvent() throws Exception {
        RegisteredUser user = register("alice-score@example.com", "pass123");

        mockMvc.perform(post("/api/v1/gigs")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\":\"Uber\",\"amount\":2500.0,\"rating\":4.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").isNumber());

        mockMvc.perform(get("/api/v1/score/" + user.userId())
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.band").isString())
                .andExpect(jsonPath("$.components.earnings").isNumber())
                .andExpect(jsonPath("$.components.jobs").isNumber())
                .andExpect(jsonPath("$.components.rating").isNumber())
                .andExpect(jsonPath("$.components.activeDays").isNumber())
                .andExpect(jsonPath("$.suggestions").isArray());

        mockMvc.perform(get("/api/v1/score/" + user.userId() + "/history")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].score").isNumber())
                .andExpect(jsonPath("$[0].recordedAt").exists());
    }

    @Test
    void addGig_unknownPlatform_returnsBadRequest() throws Exception {
        RegisteredUser user = register("alice-unknown@example.com", "pass123");

        mockMvc.perform(post("/api/v1/gigs")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\":\"MySpace\",\"amount\":12.5,\"rating\":4.5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}