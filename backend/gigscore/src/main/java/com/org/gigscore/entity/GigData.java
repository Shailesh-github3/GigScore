package com.org.gigscore.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Data;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_gig_data_user_platform", columnNames = {"user_id", "platform"}))
@Data
public class GigData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gigId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String platform;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalEarnings;
    private Integer jobsCompleted;
    private Double avgRating;

    @Version
    private Long version;
}