package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.org.gigscore.dto.ScoreResponse;
import com.org.gigscore.entity.GigScore;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.GigScoreRepository;
import com.org.gigscore.service.UserMetricsService.UserAggregateMetrics;

@ExtendWith(MockitoExtension.class)
class GigScoreServiceTest {

    @Mock
    private GigScoreRepository gigScoreRepository;

    @Mock
    private UserMetricsService userMetricsService;

    @InjectMocks
    private GigScoreService gigScoreService;

    private User createUser(Long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");
        return user;
    }

    @Test
    void calculateAndPersistScore_normalValues() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = new UserAggregateMetrics(2500.0, 50, 4.0, 15);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());

        ScoreResponse response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getScore()).isGreaterThan(0.0);
        verify(gigScoreRepository).save(any(GigScore.class));
    }

    @Test
    void calculateAndPersistScore_zeroJobs() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = new UserAggregateMetrics(0.0, 0, 0.0, 0);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());

        ScoreResponse response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getScore()).isEqualTo(0.0);
    }

    @Test
    void calculateAndPersistScore_maxRating() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = new UserAggregateMetrics(2500.0, 50, 5.0, 15);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());

        ScoreResponse response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getScore()).isGreaterThan(0.0);
    }

    @Test
    void calculateAndPersistScore_zeroEarnings() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = new UserAggregateMetrics(0.0, 50, 4.0, 15);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());

        ScoreResponse response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getScore()).isGreaterThan(0.0);
    }
}
