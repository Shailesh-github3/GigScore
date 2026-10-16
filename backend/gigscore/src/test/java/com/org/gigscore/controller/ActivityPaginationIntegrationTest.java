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
class ActivityPaginationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private record RegisteredUser(String token, long userId) {}

    private RegisteredUser register(String email) throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Pager");
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

    private void addGigs(RegisteredUser user, int count) throws Exception {
        for (int i = 0; i < count; i++) {
            mockMvc.perform(post("/api/v1/gigs")
                            .header("Authorization", "Bearer " + user.token())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"platform\":\"Uber\",\"amount\":" + (i + 1) + ".50,\"rating\":4.5}"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void pagedActivities_respectsPageSizeAndMax() throws Exception {
        RegisteredUser user = register("pager@example.com");
        addGigs(user, 6);

        mockMvc.perform(get("/api/v1/activity/" + user.userId() + "?page=0&size=5")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5));

        mockMvc.perform(get("/api/v1/activity/" + user.userId() + "?page=1&size=5")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/v1/activity/" + user.userId() + "?page=0&size=100")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50));
    }
}