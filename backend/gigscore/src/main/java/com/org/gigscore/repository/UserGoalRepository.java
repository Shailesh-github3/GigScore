package com.org.gigscore.repository;

import com.org.gigscore.entity.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
    UserGoal findFirstByUser_UserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
