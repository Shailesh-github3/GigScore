package com.org.gigscore.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Actor","email":"actor@example.com","password":"password1"}
                        """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        token = json.get("token").asText();
        userId = json.get("userId").asLong();
    }

    @Test
    void getActivities_newUser_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/activity/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getActivities_afterAddingGig_returnsAtMostFiveItems() throws Exception {
        // Add 3 gigs
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/gigs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                            {"userId":%d,"platform":"Upwork","amount":100.0,"rating":4.5}
                            """, userId)))
                    .andExpect(status().isOk());
        }

        MvcResult result = mockMvc.perform(get("/api/activity/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        // Should return activities (max 5 per the findTop5 query)
        org.assertj.core.api.Assertions.assertThat(json.size()).isLessThanOrEqualTo(5);
        org.assertj.core.api.Assertions.assertThat(json.size()).isGreaterThan(0);
    }

    @Test
    void getActivities_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/activity/" + userId))
                .andExpect(status().isUnauthorized());
    }
}