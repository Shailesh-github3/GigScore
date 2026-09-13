package com.org.gigscore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.org.gigscore.dto.PagedActivities;
import com.org.gigscore.entity.Activity;
import com.org.gigscore.entity.User;
import com.org.gigscore.exception.BadRequestException;
import com.org.gigscore.repository.ActivityRepository;
import com.org.gigscore.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void getActivities_returnsMappedPageWithMetadata() {
        User user = new User();
        user.setUserId(1L);

        Activity activity = new Activity();
        activity.setId(10L);
        activity.setUser(user);
        activity.setPlatform("Upwork");
        activity.setAction("Added gig");
        activity.setAmount(new BigDecimal("12.50"));
        activity.setRating(4.5);
        activity.setTimestamp(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityRepository.findByUserOrderByTimestampDesc(eq(user), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(activity), PageRequest.of(0, 5), 1));

        PagedActivities page = activityService.getActivities(1L, 0, 5);

        assertThat(page.page()).isEqualTo(0);
        assertThat(page.size()).isEqualTo(5);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).getPlatform()).isEqualTo("Upwork");
        verify(activityRepository).findByUserOrderByTimestampDesc(eq(user), any(PageRequest.class));
    }

    @Test
    void getActivities_rejectsNegativePage() {
        assertThrows(BadRequestException.class, () -> activityService.getActivities(1L, -1, 5));
    }

    @Test
    void getActivities_rejectsOutOfRangeSize() {
        assertThrows(BadRequestException.class, () -> activityService.getActivities(1L, 0, 0));
        assertThrows(BadRequestException.class, () -> activityService.getActivities(1L, 0, 51));
    }
}