package com.org.gigscore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.gigscore.entity.ScoreHistory;
import com.org.gigscore.entity.User;

public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {
    List<ScoreHistory> findByUserOrderByRecordedAtAsc(User user);
}
