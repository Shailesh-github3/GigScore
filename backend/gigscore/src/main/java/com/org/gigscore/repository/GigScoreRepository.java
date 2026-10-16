package com.org.gigscore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.gigscore.entity.GigScore;
import com.org.gigscore.entity.User;

public interface  GigScoreRepository extends JpaRepository<GigScore, Long> {
    Optional<GigScore> findByUser(User user);
}
