package com.org.gigscore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.gigscore.dto.CreateUserRequest;
import com.org.gigscore.dto.ScoreResponse;
import com.org.gigscore.repository.GigScoreRepository;
import com.org.gigscore.repository.ScoreHistoryRepository;
import com.org.gigscore.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScoreReadOnlyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScoreHistoryRepository scoreHistoryRepository;

    @Autowired
    private GigScoreRepository gigScoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private record RegisteredUser(String token, long userId) {}

    private RegisteredUser register(String email) throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Test Worker");
        req.setEmail(email);
        req.setPassword("password123");

        String body = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var node = objectMapper.readTree(body);
        return new RegisteredUser(node.get("token").asText(), node.get("userId").asLong());
    }

    @Test
    void testA_callingGetScoreFiveTimesLeavesScoreHistoryRowCountUnchanged() throws Exception {
        RegisteredUser user = register("test-a@example.com");

        // First add a gig so that an initial score is persisted
        mockMvc.perform(post("/api/v1/gigs")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\":\"Uber\",\"amount\":500.0,\"rating\":4.8}"))
                .andExpect(status().isOk());

        long historyCountAfterGig = scoreHistoryRepository.count();

        // Evict cache between requests to test that even on cache misses, GET /score NEVER writes
        for (int i = 0; i < 5; i++) {
            if (cacheManager.getCache("scores") != null) {
                cacheManager.getCache("scores").evict(user.userId());
            }
            mockMvc.perform(get("/api/v1/score/" + user.userId())
                            .header("Authorization", "Bearer " + user.token()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.score").isNumber());
        }

        long historyCountAfterReads = scoreHistoryRepository.count();
        assertThat(historyCountAfterReads)
                .as("GET /score must not insert new score_history rows, even on cache misses")
                .isEqualTo(historyCountAfterGig);
    }

    @Test
    void testB_afterAddingGigGetReturnsUpdatedScoreWithoutStaleCache() throws Exception {
        RegisteredUser user = register("test-b@example.com");

        // Add first gig
        mockMvc.perform(post("/api/v1/gigs")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\":\"Uber\",\"amount\":500.0,\"rating\":4.0}"))
                .andExpect(status().isOk());

        String firstScoreJson = mockMvc.perform(get("/api/v1/score/" + user.userId())
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        double firstScore = objectMapper.readTree(firstScoreJson).get("score").asDouble();

        // Add second gig (triggers @CacheEvict and recalculation)
        mockMvc.perform(post("/api/v1/gigs")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\":\"Uber\",\"amount\":1500.0,\"rating\":5.0}"))
                .andExpect(status().isOk());

        String secondScoreJson = mockMvc.perform(get("/api/v1/score/" + user.userId())
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        double secondScore = objectMapper.readTree(secondScoreJson).get("score").asDouble();

        assertThat(secondScore)
                .as("Score should increase after adding a higher paying gig and cache must not be stale")
                .isGreaterThan(firstScore);
    }

    @Test
    void testC_userWithNoPersistedScoreGetsComputedScoreAndZeroWrites() throws Exception {
        RegisteredUser user = register("test-c@example.com");

        long historyCountBefore = scoreHistoryRepository.count();
        long gigScoreCountBefore = gigScoreRepository.count();

        // Call GET /score for brand new user with no gigs and no persisted score
        mockMvc.perform(get("/api/v1/score/" + user.userId())
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(0.0))
                .andExpect(jsonPath("$.band").value("Needs Improvement"))
                .andExpect(jsonPath("$.components").isMap());

        long historyCountAfter = scoreHistoryRepository.count();
        long gigScoreCountAfter = gigScoreRepository.count();

        assertThat(historyCountAfter)
                .as("User with no persisted score must not cause inserts in score_history on GET")
                .isEqualTo(historyCountBefore);
        assertThat(gigScoreCountAfter)
                .as("User with no persisted score must not cause inserts in gig_score on GET")
                .isEqualTo(gigScoreCountBefore);
    }
}
