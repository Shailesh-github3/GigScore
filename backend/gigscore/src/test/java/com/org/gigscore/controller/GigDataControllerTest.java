package com.org.gigscore.controller;

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
class GigDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"GigUser","email":"giguser@example.com","password":"password1"}
                        """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        userId = json.get("userId").asLong();
    }

    @Test
    void addGig_validRequest_returns200WithDashboard() throws Exception {
        mockMvc.perform(post("/api/gigs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {"userId":%d,"platform":"Upwork","amount":500.0,"rating":4.8}
                        """, userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalEarnings").value(500.0))
                .andExpect(jsonPath("$.jobsCompleted").value(1))
                .andExpect(jsonPath("$.score").isNumber());
    }

    @Test
    void addGig_unknownUser_returns500() throws Exception {
        mockMvc.perform(post("/api/gigs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"userId":999999,"platform":"Upwork","amount":100.0,"rating":4.0}
                        """))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void addGig_multipleGigs_accumulatesEarnings() throws Exception {
        String body = String.format("""
                {"userId":%d,"platform":"Swiggy","amount":200.0,"rating":4.0}
                """, userId);

        // Add two gigs on same platform
        mockMvc.perform(post("/api/gigs").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        MvcResult result = mockMvc.perform(post("/api/gigs").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        // Total should be 400
        org.assertj.core.api.Assertions.assertThat(json.get("totalEarnings").asDouble()).isEqualTo(400.0);
    }
}