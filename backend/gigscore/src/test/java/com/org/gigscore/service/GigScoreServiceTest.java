package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.org.gigscore.entity.GigScore;
import com.org.gigscore.entity.ScoreHistory;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.GigScoreRepository;
import com.org.gigscore.repository.ScoreHistoryRepository;
import com.org.gigscore.service.UserMetricsService.UserAggregateMetrics;

@ExtendWith(MockitoExtension.class)
class GigScoreServiceTest {

    @Mock
    private GigScoreRepository gigScoreRepository;
    @Mock
    private ScoreHistoryRepository scoreHistoryRepository;
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

    private UserAggregateMetrics metricsFor(double earnings, int jobs, double rating, int activeDays) {
        return new UserAggregateMetrics(new BigDecimal(String.valueOf(earnings)), jobs, rating, activeDays);
    }

    @Test
    void calculateScoreValue_zeroMetrics_returnsZero() {
        double score = gigScoreService.calculateScoreValue(metricsFor(0, 0, 0, 0));
        assertThat(score).isCloseTo(0.0, within(0.01));
    }

    @Test
    void calculateScoreValue_maxMetrics_returns100() {
        double score = gigScoreService.calculateScoreValue(metricsFor(5000, 100, 5.0, 30));
        assertThat(score).isCloseTo(100.0, within(0.01));
    }

    @Test
    void calculateScoreValue_halfEarnings_contributesWeighted() {
        UserAggregateMetrics metrics = metricsFor(2500, 0, 0, 0);
        double score = gigScoreService.calculateScoreValue(metrics);

        double expectedComponent = Math.pow(2500.0 / 5000.0, 0.70) * 0.35 * 100.0;
        assertThat(score).isCloseTo(expectedComponent, within(0.01));
    }

    @Test
    void calculateScoreValue_halfJobs_contributesWeighted() {
        UserAggregateMetrics metrics = metricsFor(0, 50, 0, 0);
        double score = gigScoreService.calculateScoreValue(metrics);

        double expectedComponent = Math.pow(50.0 / 100.0, 0.65) * 0.25 * 100.0;
        assertThat(score).isCloseTo(expectedComponent, within(0.01));
    }

    @Test
    void calculateScoreValue_halfRating_contributesWeighted() {
        UserAggregateMetrics metrics = metricsFor(0, 0, 2.5, 0);
        double score = gigScoreService.calculateScoreValue(metrics);

        double expectedComponent = Math.pow(2.5 / 5.0, 1.40) * 0.30 * 100.0;
        assertThat(score).isCloseTo(expectedComponent, within(0.01));
    }

    @Test
    void calculateScoreValue_halfActiveDays_contributesWeighted() {
        UserAggregateMetrics metrics = metricsFor(0, 0, 0, 15);
        double score = gigScoreService.calculateScoreValue(metrics);

        double expectedComponent = Math.pow(15.0 / 30.0, 1.0) * 0.10 * 100.0;
        assertThat(score).isCloseTo(expectedComponent, within(0.01));
    }

    @Test
    void computeComponents_populatesAllFourKeys() {
        Map<String, Double> components = gigScoreService.computeComponents(metricsFor(5000, 100, 5.0, 30));

        assertThat(components).containsKeys("earnings", "jobs", "rating", "activeDays");
        assertThat(components.values()).allMatch(v -> v >= 0.0);
    }

    @Test
    void getConfig_returnsMatchingConstants() {
        var config = gigScoreService.getConfig();

        assertThat(config.getMaxScore()).isEqualTo(100.0);
        assertThat(config.getWeights().getEarnings()).isEqualTo(0.35);
        assertThat(config.getExponents().getEarnings()).isEqualTo(0.70);
        assertThat(config.getEarningsTarget()).isEqualTo(5000.0);
        assertThat(config.getJobsTarget()).isEqualTo(100.0);
        assertThat(config.getActiveDaysTarget()).isEqualTo(30.0);
    }

    @Test
    void calculateAndPersistScore_persistsAndReturnsResponse() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = metricsFor(5000, 100, 5.0, 30);

        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());

        var response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getScore()).isCloseTo(100.0, within(0.01));
        assertThat(response.getBand()).isEqualTo("Excellent");
        assertThat(response.getComponents()).isNotEmpty();
        assertThat(response.getSuggestions()).isNotEmpty();
        assertThat(response.getDataQuality()).isNotNull();
        assertThat(response.getDataQuality().getConfidence()).isNotNull();
        verify(gigScoreRepository).save(any(GigScore.class));
        verify(scoreHistoryRepository).save(any(ScoreHistory.class));
    }

    @Test
    void calculateAndPersistScore_recordsScoreVersionOnHistory() {
        User user = createUser(1L);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metricsFor(0, 0, 0, 0));
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());

        gigScoreService.calculateAndPersistScore(user);

        ArgumentCaptor<ScoreHistory> captor = ArgumentCaptor.forClass(ScoreHistory.class);
        verify(scoreHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getScoreVersion()).isEqualTo(1);
    }

    @Test
    void getConfig_includesScoreVersion() {
        var config = gigScoreService.getConfig();
        assertThat(config.getVersion()).isEqualTo(1);
    }

    @Test
    void computeDataQuality_lowVolume_returnsLowConfidence() {
        var profile = gigScoreService.computeDataQuality(metricsFor(0, 1, 4.5, 1));
        assertThat(profile.getConfidence()).isEqualTo(com.org.gigscore.dto.ConfidenceLevel.LOW);
        assertThat(profile.getFlags()).isNotEmpty();
        assertThat(profile.getNote()).isNotBlank();
    }

    @Test
    void computeDataQuality_highVolume_returnsHighConfidence() {
        var profile = gigScoreService.computeDataQuality(metricsFor(5000, 25, 4.8, 15));
        assertThat(profile.getConfidence()).isEqualTo(com.org.gigscore.dto.ConfidenceLevel.HIGH);
        assertThat(profile.getFlags()).isEmpty();
        assertThat(profile.getNumberOfGigs()).isEqualTo(25);
    }
}
