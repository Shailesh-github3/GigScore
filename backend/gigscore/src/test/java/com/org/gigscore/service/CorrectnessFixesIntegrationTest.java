package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.org.gigscore.controller.GigScoreController;
import com.org.gigscore.dto.LenderWorkerDetail;
import com.org.gigscore.dto.LenderWorkerSummary;
import com.org.gigscore.dto.ScoreHistoryEntry;
import com.org.gigscore.entity.Activity;
import com.org.gigscore.entity.ScoreHistory;
import com.org.gigscore.entity.User;
import com.org.gigscore.entity.UserRole;
import com.org.gigscore.repository.ActivityRepository;
import com.org.gigscore.repository.ScoreHistoryRepository;
import com.org.gigscore.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
class CorrectnessFixesIntegrationTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserMetricsService userMetricsService;

    @Autowired
    private GigScoreService gigScoreService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void lenderWorkerSummaryAndDetail_mustUseBigDecimalForTotalEarnings() throws Exception {
        Field summaryField = LenderWorkerSummary.class.getDeclaredField("totalEarnings");
        Field detailField = LenderWorkerDetail.class.getDeclaredField("totalEarnings");

        assertThat(summaryField.getType())
                .as("LenderWorkerSummary.totalEarnings must be BigDecimal to avoid floating point inaccuracies")
                .isEqualTo(BigDecimal.class);

        assertThat(detailField.getType())
                .as("LenderWorkerDetail.totalEarnings must be BigDecimal to avoid floating point inaccuracies")
                .isEqualTo(BigDecimal.class);
    }

    @Test
    void gigScoreController_mustNotDirectlyDependOnScoreHistoryRepository() {
        Field[] fields = GigScoreController.class.getDeclaredFields();
        boolean hasRepoField = Arrays.stream(fields)
                .anyMatch(f -> f.getType().equals(ScoreHistoryRepository.class));

        assertThat(hasRepoField)
                .as("GigScoreController must not inject ScoreHistoryRepository directly; use GigScoreService instead")
                .isFalse();
    }

    @Test
    @Transactional
    void databaseCountDistinctActiveDays_returnsAccurateDistinctDayCount() {
        User user = new User();
        user.setName("Days Worker");
        user.setEmail("days-worker@example.com");
        user.setPassword("hash");
        user.setRole(UserRole.WORKER);
        userRepository.save(user);

        // Day 1: 3 activities on the same day
        LocalDateTime day1 = LocalDateTime.now().minusDays(3);
        createActivity(user, day1.withHour(9));
        createActivity(user, day1.withHour(12));
        createActivity(user, day1.withHour(18));

        // Day 2: 1 activity
        LocalDateTime day2 = LocalDateTime.now().minusDays(1);
        createActivity(user, day2);

        entityManager.flush();

        long distinctDays = activityRepository.countDistinctActiveDaysByUser(user);
        assertThat(distinctDays)
                .as("Database query must count distinct calendar dates correctly")
                .isEqualTo(2);

        var metrics = userMetricsService.calculateAggregates(user);
        assertThat(metrics.activeDays())
                .as("UserMetricsService must use database-computed active days")
                .isEqualTo(2);
    }

    @Test
    @Transactional
    void gigScoreService_getScoreHistory_encapsulatesRepositoryAccess() {
        User user = new User();
        user.setName("History Worker");
        user.setEmail("history-worker@example.com");
        user.setPassword("hash");
        userRepository.save(user);

        ScoreHistory h1 = new ScoreHistory();
        h1.setUser(user);
        h1.setScore(60.0);
        h1.setScoreVersion(1);
        entityManager.persist(h1);

        ScoreHistory h2 = new ScoreHistory();
        h2.setUser(user);
        h2.setScore(75.0);
        h2.setScoreVersion(2);
        entityManager.persist(h2);

        entityManager.flush();

        List<ScoreHistoryEntry> history = gigScoreService.getScoreHistory(user);
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getScore()).isEqualTo(60.0);
        assertThat(history.get(1).getScore()).isEqualTo(75.0);
    }

    private void createActivity(User user, LocalDateTime timestamp) {
        Activity act = new Activity();
        act.setUser(user);
        act.setPlatform("Uber");
        act.setAction("TRIP");
        act.setAmount(new BigDecimal("15.00"));
        act.setRating(5.0);
        act.setTimestamp(timestamp);
        entityManager.persist(act);
    }
}
