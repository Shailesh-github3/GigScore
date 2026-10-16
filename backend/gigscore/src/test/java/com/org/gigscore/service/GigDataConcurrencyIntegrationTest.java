package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.org.gigscore.dto.CreateUserRequest;
import com.org.gigscore.dto.GigEventRequest;
import com.org.gigscore.entity.GigData;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.GigDataRepository;
import com.org.gigscore.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class GigDataConcurrencyIntegrationTest {

    @Autowired
    private GigDataService gigDataService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GigDataRepository gigDataRepository;

    @Test
    void concurrentSubmissions_resultInSingleRowAndExactSum() throws Exception {
        // Register a user
        CreateUserRequest userReq = new CreateUserRequest();
        userReq.setName("Concurrency Tester");
        userReq.setEmail("concurrency-test@example.com");
        userReq.setPassword("password123");
        var authResponse = userService.createUser(userReq).getBody();
        Long userId = authResponse.getUserId();

        User user = userRepository.findById(userId).orElseThrow();

        int threadCount = 5;
        BigDecimal amountPerGig = new BigDecimal("100.00");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    GigEventRequest req = new GigEventRequest();
                    req.setUserId(userId);
                    req.setPlatform("Uber");
                    req.setAmount(amountPerGig);
                    req.setRating(5.0);
                    gigDataService.addGig(req);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // Fire all threads simultaneously

        for (Future<?> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        List<GigData> rows = gigDataRepository.findAllByUserAndPlatform(user, "Uber");

        // In absence of unique constraint and optimistic locking with retries,
        // this fails either due to multiple rows created or lost update in earnings.
        assertThat(rows)
                .as("There must be exactly one gig_data row for (user, platform)")
                .hasSize(1);

        BigDecimal expectedTotal = amountPerGig.multiply(BigDecimal.valueOf(threadCount));
        assertThat(rows.get(0).getTotalEarnings())
                .as("Total earnings must equal the sum of all concurrent submissions without lost updates")
                .isEqualByComparingTo(expectedTotal);
        assertThat(rows.get(0).getJobsCompleted())
                .as("Jobs completed must match the number of concurrent submissions")
                .isEqualTo(threadCount);
    }
}
