package com.org.gigscore.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LazyFetchingIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    private Long userId;
    private Long activityId;
    private Long gigDataId;
    private Long gigScoreId;
    private Long scoreHistoryId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("Lazy Test User");
        user.setEmail("lazy-test@example.com");
        user.setPassword("hashedpassword");
        entityManager.persist(user);
        userId = user.getUserId();

        Activity activity = new Activity();
        activity.setUser(user);
        activity.setPlatform("Uber");
        activity.setAction("TRIP_COMPLETED");
        activity.setAmount(new BigDecimal("25.00"));
        activity.setRating(5.0);
        activity.setTimestamp(LocalDateTime.now());
        entityManager.persist(activity);
        activityId = activity.getId();

        GigData gigData = new GigData();
        gigData.setUser(user);
        gigData.setPlatform("Uber");
        gigData.setTotalEarnings(new BigDecimal("100.00"));
        gigData.setJobsCompleted(4);
        gigData.setAvgRating(4.9);
        entityManager.persist(gigData);
        gigDataId = gigData.getGigId();

        GigScore gigScore = new GigScore();
        gigScore.setUser(user);
        gigScore.setScore(750.0);
        entityManager.persist(gigScore);
        gigScoreId = gigScore.getGigScoreId();

        ScoreHistory history = new ScoreHistory();
        history.setUser(user);
        history.setScore(750.0);
        history.setScoreVersion(1);
        entityManager.persist(history);
        scoreHistoryId = history.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void activityUserAssociation_isLazyLoaded() {
        Activity loaded = entityManager.find(Activity.class, activityId);
        assertThat(loaded).isNotNull();
        assertThat(Hibernate.isInitialized(loaded.getUser()))
                .as("Activity.user must be lazily fetched and not initialized upon entity retrieval")
                .isFalse();
    }

    @Test
    void gigDataUserAssociation_isLazyLoaded() {
        GigData loaded = entityManager.find(GigData.class, gigDataId);
        assertThat(loaded).isNotNull();
        assertThat(Hibernate.isInitialized(loaded.getUser()))
                .as("GigData.user must be lazily fetched and not initialized upon entity retrieval")
                .isFalse();
    }

    @Test
    void gigScoreUserAssociation_isLazyLoaded() {
        GigScore loaded = entityManager.find(GigScore.class, gigScoreId);
        assertThat(loaded).isNotNull();
        assertThat(Hibernate.isInitialized(loaded.getUser()))
                .as("GigScore.user must be lazily fetched and not initialized upon entity retrieval")
                .isFalse();
    }

    @Test
    void scoreHistoryUserAssociation_isLazyLoaded() {
        ScoreHistory loaded = entityManager.find(ScoreHistory.class, scoreHistoryId);
        assertThat(loaded).isNotNull();
        assertThat(Hibernate.isInitialized(loaded.getUser()))
                .as("ScoreHistory.user must be lazily fetched and not initialized upon entity retrieval")
                .isFalse();
    }
}
