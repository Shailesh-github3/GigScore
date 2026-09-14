package com.org.gigscore.controller;

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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final RequestPostProcessor fakeIp = request -> {
        request.setRemoteAddr("10.0.0.99");
        return request;
    };

    private String loginBody(String email, String password) throws Exception {
        var dto = new com.org.gigscore.dto.LoginDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dto);
    }

    @Test
    void loginRateLimit_returns429AfterExceeding() throws Exception {
        String body = loginBody("nonexistent@example.com", "wrong");

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/v1/users/login")
                            .with(fakeIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").exists());
        }

        mockMvc.perform(post("/api/v1/users/login")
                        .with(fakeIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.message").value("Too many requests. Please try again later."));
    }
}