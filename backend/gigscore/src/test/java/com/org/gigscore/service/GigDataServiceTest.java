package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.org.gigscore.dto.GigEventRequest;
import com.org.gigscore.dto.ScoreResponse;
import com.org.gigscore.entity.GigData;
import com.org.gigscore.entity.User;
import com.org.gigscore.repository.GigDataRepository;
import com.org.gigscore.repository.UserRepository;
import com.org.gigscore.service.UserMetricsService.UserAggregateMetrics;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GigDataServiceTest {

    @Mock
    private GigDataRepository gigDataRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GigScoreService scoreService;
    @Mock
    private UserMetricsService userMetricsService;
    @Mock
    private ActivityService activityService;

    @InjectMocks
    private GigDataService gigDataService;

    private User createUser(Long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");
        return user;
    }

    private void stubDashboard(User user, List<GigData> gigDataList, double totalEarnings, int jobs, double avgRating, int activeDays) {
        when(userMetricsService.calculateAggregates(user))
                .thenReturn(new UserAggregateMetrics(totalEarnings, jobs, avgRating, activeDays));
        when(scoreService.getScoreForUser(user))
                .thenReturn(ScoreResponse.builder().userId(user.getUserId()).score(50.0).build());
        when(gigDataRepository.findByUser(user)).thenReturn(gigDataList);
    }

    @Test
    void addGig_duplicatePlatform_mergeRows() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        GigData row1 = new GigData();
        row1.setUser(user);
        row1.setPlatform("Uber");
        row1.setTotalEarnings(1000.0);
        row1.setJobsCompleted(10);
        row1.setAvgRating(4.0);
        row1.setActiveDays(5);

        GigData row2 = new GigData();
        row2.setUser(user);
        row2.setPlatform("Uber");
        row2.setTotalEarnings(500.0);
        row2.setJobsCompleted(5);
        row2.setAvgRating(3.0);
        row2.setActiveDays(3);

        when(gigDataRepository.findAllByUserAndPlatform(user, "Uber")).thenReturn(List.of(row1, row2));
        when(gigDataRepository.save(any(GigData.class))).thenAnswer(inv -> inv.getArgument(0));
        stubDashboard(user, List.of(row1), 1500.0, 15, 3.6667, 8);

        GigEventRequest request = new GigEventRequest();
        request.setUserId(1L);
        request.setPlatform("Uber");
        request.setAmount(200.0);
        request.setRating(4.5);

        gigDataService.addGig(request);

        verify(gigDataRepository).delete(row2);

        ArgumentCaptor<GigData> captor = ArgumentCaptor.forClass(GigData.class);
        verify(gigDataRepository).save(captor.capture());
        GigData saved = captor.getValue();
        assertThat(saved.getTotalEarnings()).isEqualTo(1700.0);
        assertThat(saved.getJobsCompleted()).isEqualTo(16);
        assertThat(saved.getActiveDays()).isEqualTo(9);
    }

    @Test
    void addGig_incrementalAverageRating() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        GigData existing = new GigData();
        existing.setUser(user);
        existing.setPlatform("Lyft");
        existing.setTotalEarnings(2000.0);
        existing.setJobsCompleted(2);
        existing.setAvgRating(4.0);
        existing.setActiveDays(2);

        when(gigDataRepository.findAllByUserAndPlatform(user, "Lyft")).thenReturn(List.of(existing));
        when(gigDataRepository.save(any(GigData.class))).thenAnswer(inv -> inv.getArgument(0));
        stubDashboard(user, List.of(existing), 2000.0, 2, 4.0, 2);

        GigEventRequest request = new GigEventRequest();
        request.setUserId(1L);
        request.setPlatform("Lyft");
        request.setAmount(100.0);
        request.setRating(5.0);

        gigDataService.addGig(request);

        ArgumentCaptor<GigData> captor = ArgumentCaptor.forClass(GigData.class);
        verify(gigDataRepository).save(captor.capture());
        GigData saved = captor.getValue();
        assertThat(saved.getAvgRating()).isCloseTo(4.333, org.assertj.core.data.Offset.offset(0.01));
        assertThat(saved.getJobsCompleted()).isEqualTo(3);
        assertThat(saved.getActiveDays()).isEqualTo(3);
        assertThat(saved.getTotalEarnings()).isEqualTo(2100.0);
    }
}
