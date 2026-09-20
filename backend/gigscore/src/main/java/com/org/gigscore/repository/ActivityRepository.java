package com.org.gigscore.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.org.gigscore.entity.Activity;
import com.org.gigscore.entity.User;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findTop5ByUserOrderByTimestampDesc(User user);
    List<Activity> findByUser(User user);
    Page<Activity> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT CAST(a.timestamp AS date)) FROM Activity a WHERE a.user = :user")
    long countDistinctActiveDaysByUser(@Param("user") User user);
}
