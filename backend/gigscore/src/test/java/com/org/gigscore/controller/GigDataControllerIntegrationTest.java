package com.org.gigscore.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    private String registerAndToken(String name, String email, String password) throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setName(name);
        req.setEmail(email);
        req.setPassword(password);

        String body = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void userA_cannotAccess_userBData() throws Exception {
        String tokenA = registerAndToken("Alice", "alice2@example.com", "pass123");
        String tokenB = registerAndToken("Bob", "bob2@example.com", "pass123");

        Long userIdB = getUserIdFromToken(tokenB);

        mockMvc.perform(get("/api/users/" + userIdB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());
    }

    private Long getUserIdFromToken(String token) throws Exception {
        CreateUserRequest dummy = new CreateUserRequest();
        dummy.setName("tmp");
        dummy.setEmail("tmp_" + System.nanoTime() + "@example.com");
        dummy.setPassword("pass");

        String body = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummy)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("userId").asLong();
    }
}
