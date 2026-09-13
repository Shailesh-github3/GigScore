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
class GigDataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private record RegisteredUser(String token, long userId) {}

    private RegisteredUser register(String name, String email, String password) throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setName(name);
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
    void userA_cannotAccess_userBData() throws Exception {
        RegisteredUser alice = register("Alice", "alice2@example.com", "pass123");
        RegisteredUser bob = register("Bob", "bob2@example.com", "pass123");

        mockMvc.perform(get("/api/v1/users/" + bob.userId())
                        .header("Authorization", "Bearer " + alice.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void addGig_withInvalidAmountOrRating_returnsBadRequest() throws Exception {
        String token = register("Alice", "alice-invalid@example.com", "pass123").token();

        String invalidAmountBody = "{\"platform\":\"Upwork\",\"amount\":0.0,\"rating\":4.5}";
        mockMvc.perform(post("/api/v1/gigs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidAmountBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        String invalidRatingBody = "{\"platform\":\"Upwork\",\"amount\":12.5,\"rating\":6.0}";
        mockMvc.perform(post("/api/v1/gigs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRatingBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        String missingAmountBody = "{\"platform\":\"Upwork\",\"rating\":4.5}";
        mockMvc.perform(post("/api/v1/gigs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingAmountBody))
                .andExpect(status().isBadRequest());

        String unknownPlatformBody = "{\"platform\":\"MySpace\",\"amount\":12.5,\"rating\":4.5}";
        mockMvc.perform(post("/api/v1/gigs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unknownPlatformBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
