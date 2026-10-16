package com.org.gigscore.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.gigscore.dto.CreateUserRequest;
import com.org.gigscore.dto.LoginDTO;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void register_thenLogin_returnsToken() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Alice");
        createRequest.setEmail("alice@example.com");
        createRequest.setPassword("password123");

        String registerResponse = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("alice@example.com");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Short");
        createRequest.setEmail("shortpw@example.com");
        createRequest.setPassword("123");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthorizedRequests_return401() throws Exception {
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());

        mockMvc.perform(get("/api/v1/users/999")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void loginFailures_returnIdentical401StatusAndBody() throws Exception {
        // 1. Create a user
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Bob");
        createRequest.setEmail("bob@example.com");
        createRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        // 2. Login with unknown email
        LoginDTO unknownEmailLogin = new LoginDTO();
        unknownEmailLogin.setEmail("nonexistent@example.com");
        unknownEmailLogin.setPassword("somepassword");

        String unknownEmailBody = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownEmailLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andReturn().getResponse().getContentAsString();

        // 3. Login with wrong password for existing user
        LoginDTO wrongPasswordLogin = new LoginDTO();
        wrongPasswordLogin.setEmail("bob@example.com");
        wrongPasswordLogin.setPassword("wrongpassword");

        String wrongPasswordBody = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andReturn().getResponse().getContentAsString();

        // 4. Assert response status and message are identical
        com.fasterxml.jackson.databind.JsonNode unknownJson = objectMapper.readTree(unknownEmailBody);
        com.fasterxml.jackson.databind.JsonNode wrongPasswordJson = objectMapper.readTree(wrongPasswordBody);
        org.assertj.core.api.Assertions.assertThat(unknownJson.get("status").asInt())
                .isEqualTo(wrongPasswordJson.get("status").asInt());
        org.assertj.core.api.Assertions.assertThat(unknownJson.get("message").asText())
                .isEqualTo(wrongPasswordJson.get("message").asText());
    }

    @Test
    void actuatorHealth_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
