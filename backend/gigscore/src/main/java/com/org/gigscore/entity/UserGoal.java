package com.org.gigscore.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "user_goal", indexes = @Index(name = "idx_user_goal_user", columnList = "user_id, status"))
@Data
public class UserGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_score")
    private Integer targetScore;

    @Column(name = "status")
    private String status = "ACTIVE"; // ACTIVE, ACHIEVED, ABANDONED

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
