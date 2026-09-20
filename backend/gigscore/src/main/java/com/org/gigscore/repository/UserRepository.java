package com.org.gigscore.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.org.gigscore.dto.WorkerSummaryProjection;
import com.org.gigscore.entity.User;
import com.org.gigscore.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT 
            u.userId AS userId,
            u.name AS name,
            u.email AS email,
            s.score AS score,
            (SELECT COALESCE(SUM(g.totalEarnings), 0) FROM GigData g WHERE g.user = u) AS totalEarnings,
            (SELECT COALESCE(SUM(g.jobsCompleted), 0) FROM GigData g WHERE g.user = u) AS jobsCompleted,
            (SELECT COUNT(DISTINCT CAST(a.timestamp AS date)) FROM Activity a WHERE a.user = u) AS activeDays
        FROM User u
        LEFT JOIN GigScore s ON s.user = u
        WHERE u.role = :role
        ORDER BY s.score DESC NULLS LAST, u.userId ASC
    """)
    Page<WorkerSummaryProjection> findWorkerSummaries(@Param("role") UserRole role, Pageable pageable);
}
