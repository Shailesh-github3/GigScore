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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.org.gigscore.dto.ConfidenceLevel;
import com.org.gigscore.dto.DataQualityProfile;
import com.org.gigscore.dto.ScoreBand;
import com.org.gigscore.dto.ScoreResponse;
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
    private UserMetricsService userMetricsService;

    @Mock
    private DataQualityService dataQualityService;

    @Mock
    private ScoreHistoryRepository scoreHistoryRepository;

    @InjectMocks
    private GigScoreService gigScoreService;

    private DataQualityProfile validDataQuality() {
        return new DataQualityProfile(ConfidenceLevel.HIGH, 50, 10, 100, List.of());
    }

    private UserAggregateMetrics metrics(double earnings, int jobs, double rating, int activeDays) {
        return new UserAggregateMetrics(BigDecimal.valueOf(earnings), jobs, rating, activeDays);
    }

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
        UserAggregateMetrics metrics = metrics(2500.0, 50, 4.0, 15);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());
        when(dataQualityService.evaluateDataQuality(user)).thenReturn(validDataQuality());

        ScoreResponse response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getScore()).isGreaterThan(0.0);
        assertThat(response.getScoreVersion()).isEqualTo(GigScoreService.SCORE_VERSION);
        assertThat(response.getBand()).isEqualTo(ScoreBand.GROWING);
        assertThat(response.getComponents()).isNotNull();
        assertThat(response.getSuggestions()).hasSize(2);
        verify(gigScoreRepository).save(any(GigScore.class));
        verify(scoreHistoryRepository).save(any(ScoreHistory.class));
    }

    @Test
    void calculateAndPersistScore_doesNotRecordDuplicateHistoryWhenScoreUnchanged() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = metrics(2500.0, 50, 4.0, 15);
        GigScore existing = new GigScore();
        existing.setUser(user);
        existing.setScore(gigScoreService.calculateScoreValue(metrics));

        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.of(existing));
        when(dataQualityService.evaluateDataQuality(user)).thenReturn(validDataQuality());

        gigScoreService.calculateAndPersistScore(user);

        verify(scoreHistoryRepository, never()).save(any(ScoreHistory.class));
    }

    @Test
    void getScoreHistory_returnsMappedEntriesInOrder() {
        User user = createUser(1L);
        ScoreHistory first = new ScoreHistory();
        first.setScoreHistoryId(1L);
        first.setScore(20.0);
        first.setCreatedAt(LocalDateTime.now().minusDays(2));

        ScoreHistory second = new ScoreHistory();
        second.setScoreHistoryId(2L);
        second.setScore(30.0);
        second.setCreatedAt(LocalDateTime.now());

        when(scoreHistoryRepository.findByUserOrderByCreatedAtAsc(user)).thenReturn(List.of(first, second));

        var entries = gigScoreService.getScoreHistory(user);

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).score()).isEqualTo(20.0);
        assertThat(entries.get(0).recordedAt()).isNotNull();
        assertThat(entries.get(1).score()).isEqualTo(30.0);
    }

    @Test
    void calculateScoreValue_capsAt100() {
        assertThat(gigScoreService.calculateScoreValue(metrics(20000.0, 500, 5.0, 100))).isEqualTo(100.0);
        assertThat(gigScoreService.calculateScoreValue(metrics(1e12, 1000000, 5.0, 3650))).isEqualTo(100.0);
    }

    @Test
    void calculateScoreValue_ratingAboveMaxIsClamped() {
        assertThat(gigScoreService.calculateScoreValue(metrics(2500.0, 50, 7.0, 15)))
                .isEqualTo(gigScoreService.calculateScoreValue(metrics(2500.0, 50, 5.0, 15)));
    }

    @Test
    void calculateScoreValue_zeroRatingScoresBelowRated() {
        double zeroRating = gigScoreService.calculateScoreValue(metrics(2500.0, 50, 0.0, 15));
        double rated = gigScoreService.calculateScoreValue(metrics(2500.0, 50, 4.0, 15));
        assertThat(zeroRating).isGreaterThan(0.0);
        assertThat(zeroRating).isLessThan(rated);
    }

    @Test
    void calculateAndPersistScore_cappedMetrics_returnsStrongBandWithExactComponents() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = metrics(20000.0, 500, 5.0, 100);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());
        when(dataQualityService.evaluateDataQuality(user)).thenReturn(validDataQuality());

        ScoreResponse response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getScore()).isEqualTo(100.0);
        assertThat(response.getBand()).isEqualTo(ScoreBand.STRONG);
        assertThat(response.getComponents().earnings()).isEqualTo(35.0);
        assertThat(response.getComponents().jobs()).isEqualTo(25.0);
        assertThat(response.getComponents().rating()).isEqualTo(30.0);
        assertThat(response.getComponents().activeDays()).isEqualTo(10.0);
        assertThat(response.getSuggestions()).hasSize(1);
    }

    @Test
    void calculateAndPersistScore_zeroJobs() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = metrics(0.0, 0, 0.0, 0);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());
        when(dataQualityService.evaluateDataQuality(user)).thenReturn(validDataQuality());

        ScoreResponse response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getScore()).isEqualTo(0.0);
    }

    @Test
    void calculateAndPersistScore_maxRating() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = metrics(2500.0, 50, 5.0, 15);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());
        when(dataQualityService.evaluateDataQuality(user)).thenReturn(validDataQuality());

        ScoreResponse response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getScore()).isGreaterThan(0.0);
    }

    @Test
    void calculateAndPersistScore_zeroEarnings() {
        User user = createUser(1L);
        UserAggregateMetrics metrics = metrics(0.0, 50, 4.0, 15);
        when(userMetricsService.calculateAggregates(user)).thenReturn(metrics);
        when(gigScoreRepository.findByUser(user)).thenReturn(Optional.empty());
        when(dataQualityService.evaluateDataQuality(user)).thenReturn(validDataQuality());

        ScoreResponse response = gigScoreService.calculateAndPersistScore(user);

        assertThat(response.getScore()).isGreaterThan(0.0);
    }
}
