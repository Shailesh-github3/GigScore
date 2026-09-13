package com.org.gigscore.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

import com.org.gigscore.dto.ConfidenceLevel;
import com.org.gigscore.dto.DataQualityProfile;
import com.org.gigscore.entity.Activity;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.ActivityRepository;

@ExtendWith(MockitoExtension.class)
class DataQualityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    private DataQualityService dataQualityService;

    private User testUser;

    @BeforeEach
    void setUp() {
        dataQualityService = new DataQualityService(activityRepository);
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
    }

    @Test
    void testZeroGigs_ReturnsLowConfidence_WithNoDataFlag() {
        // Arrange: User with no activities
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(new ArrayList<>());

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        assertEquals(ConfidenceLevel.LOW, profile.confidence());
        assertEquals(0, profile.totalGigs());
        assertTrue(profile.flags().contains("short_history"));
    }

    @Test
    void testLessThanTenGigs_ReturnsLowConfidence_WithShortHistoryFlag() {
        // Arrange: User with 9 activities
        List<Activity> activities = createActivities(9, 0, 30); // 9 gigs, 0 days old, 30 day span
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        assertEquals(ConfidenceLevel.LOW, profile.confidence());
        assertEquals(9, profile.totalGigs());
        assertTrue(profile.flags().contains("short_history"));
        assertFalse(profile.flags().contains("stale_data"));
        assertFalse(profile.flags().contains("burst_pattern"));
    }

    @Test
    void testStaleData_ReturnsLowConfidence_WithStaleDataFlag() {
        // Arrange: User with >90 days since last gig
        List<Activity> activities = createActivities(50, 95, 100); // 50 gigs, 95 days old, 100 day span
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        assertEquals(ConfidenceLevel.LOW, profile.confidence());
        assertEquals(50, profile.totalGigs());
        assertTrue(profile.flags().contains("stale_data"));
        assertFalse(profile.flags().contains("short_history"));
        assertFalse(profile.flags().contains("burst_pattern"));
    }

    @Test
    void testBurstPattern_ReturnsLowConfidence_WithBurstPatternFlag() {
        // Arrange: User with <14 day span (many gigs in short window)
        List<Activity> activities = createActivities(50, 5, 10); // 50 gigs, 5 days old, 10 day span
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        assertEquals(ConfidenceLevel.LOW, profile.confidence());
        assertEquals(50, profile.totalGigs());
        assertTrue(profile.flags().contains("burst_pattern"));
        assertFalse(profile.flags().contains("short_history"));
        assertFalse(profile.flags().contains("stale_data"));
    }

    @Test
    void testMediumConfidence_LessThanFiftyGigs() {
        // Arrange: User with 20 gigs, recent (<30 days), good span (>14 days)
        List<Activity> activities = createActivities(20, 15, 50); // 20 gigs, 15 days old, 50 day span
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        assertEquals(ConfidenceLevel.MEDIUM, profile.confidence());
        assertEquals(20, profile.totalGigs());
        // short_history flag only applies if totalGigs < 10, so should not be present for 20 gigs
        assertFalse(profile.flags().contains("short_history"));
        assertFalse(profile.flags().contains("stale_data"));
        assertFalse(profile.flags().contains("burst_pattern"));
    }

    @Test
    void testMediumConfidence_StaleButEnoughGigs() {
        // Arrange: User with 60 gigs, moderately stale (30-90 days), good span
        // With 50 days since last gig and 60 total gigs: 
        // - Not LOW (60>=10, 50<=90, 150>=14)
        // - MEDIUM because daysSinceLastGig > 30 (50 > 30)
        List<Activity> activities = createActivities(60, 50, 150); // 60 gigs, 50 days old, 150 day span
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        assertEquals(ConfidenceLevel.MEDIUM, profile.confidence());
        assertEquals(60, profile.totalGigs());
        // stale_data flag only when daysSinceLastGig > 90, so should NOT be present for 50 days
        assertFalse(profile.flags().contains("short_history"));
        assertFalse(profile.flags().contains("stale_data"));
        assertFalse(profile.flags().contains("burst_pattern"));
    }

    @Test
    void testHighConfidence_AllGoodMetrics() {
        // Arrange: User with 60+ gigs, recent (<30 days), good span (>14 days)
        List<Activity> activities = createActivities(100, 10, 200);
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        assertEquals(ConfidenceLevel.HIGH, profile.confidence());
        assertEquals(100, profile.totalGigs());
        assertTrue(profile.flags().isEmpty());
    }

    @Test
    void testMultipleFlags_CanApplyTogether() {
        // Arrange: User with <10 gigs AND <14 day span (short history + burst pattern)
        List<Activity> activities = createActivities(5, 5, 10);
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        assertEquals(ConfidenceLevel.LOW, profile.confidence());
        assertEquals(5, profile.totalGigs());
        assertTrue(profile.flags().contains("short_history"));
        assertTrue(profile.flags().contains("burst_pattern"));
        assertFalse(profile.flags().contains("stale_data"));
    }

    @Test
    void testExactlyTenGigs_NotLow() {
        // Arrange: User with exactly 10 gigs (boundary)
        List<Activity> activities = createActivities(10, 5, 50);
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        // Should be MEDIUM (10 is not < 10)
        assertEquals(ConfidenceLevel.MEDIUM, profile.confidence());
        assertFalse(profile.flags().contains("short_history"));
    }

    @Test
    void testExactlyFiftyGigs_NotMedium() {
        // Arrange: User with exactly 50 gigs (boundary)
        List<Activity> activities = createActivities(50, 15, 100);
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        // Should be HIGH (50 is not < 50)
        assertEquals(ConfidenceLevel.HIGH, profile.confidence());
    }

    @Test
    void testExactly30DaysSinceLastGig_Boundary() {
        // Arrange: User with exactly 30 days since last gig (boundary)
        // With 60 gigs and 30 days (not > 30): 
        // - Not LOW (60>=10, 30 is NOT >90, 150>=14)
        // - Check MEDIUM: totalGigs < 50? No (60>=50). daysSinceLastGig > 30? No (30 is not > 30)
        // - So HIGH
        List<Activity> activities = createActivities(60, 30, 150);
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        // 30 days is not > 30, so should be HIGH
        assertEquals(ConfidenceLevel.HIGH, profile.confidence());
    }

    @Test
    void testExactly14DaySpan_NotBurst() {
        // Arrange: User with exactly 14 day span (boundary)
        // With 60 gigs and 14 day span (NOT < 14): should not trigger burst_pattern
        List<Activity> activities = createActivities(60, 5, 14);
        when(activityRepository.findByUserOrderByTimestampAsc(testUser))
            .thenReturn(activities);

        // Act
        DataQualityProfile profile = dataQualityService.evaluateDataQuality(testUser);

        // Assert
        // 14 is not < 14, so should NOT have burst_pattern flag
        assertTrue(profile.flags().isEmpty() || !profile.flags().contains("burst_pattern"));
    }

    /**
     * Helper method to create a list of mock activities.
     *
     * @param count                  number of activities to create
     * @param daysSinceLastActivity  days ago the most recent activity occurred
     * @param spanDays               days between first and most recent activity
     */
    private List<Activity> createActivities(int count, int daysSinceLastActivity, int spanDays) {
        List<Activity> activities = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime mostRecentTime = now.minusDays(daysSinceLastActivity);
        LocalDateTime firstTime = mostRecentTime.minusDays(spanDays);

        for (int i = 0; i < count; i++) {
            Activity activity = new Activity();
            activity.setId((long) (i + 1));
            activity.setUser(testUser);
            activity.setPlatform("TestPlatform");
            activity.setAction("TestAction");
            activity.setAmount(BigDecimal.valueOf(100.0));
            activity.setRating(4.5);

            // Spread activities evenly from firstTime (i=0) to mostRecentTime (i=count-1)
            if (count == 1) {
                activity.setTimestamp(mostRecentTime);
            } else {
                double fraction = (double) i / (count - 1);
                activity.setTimestamp(firstTime.plusNanos((long) (spanDays * 86_400_000_000_000.0 * fraction)));
            }

            activities.add(activity);
        }

        return activities;
    }
}
