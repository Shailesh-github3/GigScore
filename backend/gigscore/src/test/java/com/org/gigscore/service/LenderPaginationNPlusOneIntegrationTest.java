package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import com.org.gigscore.entity.Activity;
import com.org.gigscore.entity.GigData;
import com.org.gigscore.entity.GigScore;
import com.org.gigscore.entity.User;
import com.org.gigscore.entity.UserRole;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@SpringBootTest
@ActiveProfiles("test")
class LenderPaginationNPlusOneIntegrationTest {

    @Autowired
    private LenderService lenderService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        transactionTemplate.execute(status -> {
            // Seed 5 workers with data
            for (int i = 1; i <= 5; i++) {
                User user = new User();
                user.setName("Worker " + i);
                user.setEmail("worker" + i + "@example.com");
                user.setPassword("pass");
                user.setRole(UserRole.WORKER);
                entityManager.persist(user);

                GigData gig1 = new GigData();
                gig1.setUser(user);
                gig1.setPlatform("Uber");
                gig1.setTotalEarnings(new BigDecimal("150.00"));
                gig1.setJobsCompleted(5);
                gig1.setAvgRating(4.8);
                entityManager.persist(gig1);

                GigData gig2 = new GigData();
                gig2.setUser(user);
                gig2.setPlatform("Lyft");
                gig2.setTotalEarnings(new BigDecimal("100.00"));
                gig2.setJobsCompleted(3);
                gig2.setAvgRating(4.9);
                entityManager.persist(gig2);

                GigScore gigScore = new GigScore();
                gigScore.setUser(user);
                gigScore.setScore(70.0 + i * 4);
                entityManager.persist(gigScore);

                Activity act1 = new Activity();
                act1.setUser(user);
                act1.setPlatform("Uber");
                act1.setAction("TRIP");
                act1.setAmount(new BigDecimal("30.00"));
                act1.setRating(5.0);
                act1.setTimestamp(LocalDateTime.now().minusDays(1));
                entityManager.persist(act1);

                Activity act2 = new Activity();
                act2.setUser(user);
                act2.setPlatform("Uber");
                act2.setAction("TRIP");
                act2.setAmount(new BigDecimal("40.00"));
                act2.setRating(5.0);
                act2.setTimestamp(LocalDateTime.now().minusDays(2));
                entityManager.persist(act2);
            }
            entityManager.flush();
            return null;
        });
    }

    @Test
    void getAllWorkers_mustNotExecuteNPlusOneQueries() {
        statistics.clear();

        var workers = lenderService.getAllWorkers();
        assertThat(workers).isNotEmpty();

        long statementCount = statistics.getPrepareStatementCount();

        // For 5 workers, the unoptimized code fires 1 (users) + 5*(gig_data) + 5*(activity) + 5*(gig_score) = 16 queries.
        // The hardened endpoint must execute <= 3 statements regardless of worker count.
        assertThat(statementCount)
                .as("Statement count must be bounded <= 3 to prevent N+1 query storm (was %d)", statementCount)
                .isLessThanOrEqualTo(3);
    }
}
