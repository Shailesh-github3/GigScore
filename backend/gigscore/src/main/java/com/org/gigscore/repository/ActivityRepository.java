package com.org.gigscore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.gigscore.entity.Activity;
import com.org.gigscore.entity.User;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findTop5ByUserOrderByTimestampDesc(User user);
}
