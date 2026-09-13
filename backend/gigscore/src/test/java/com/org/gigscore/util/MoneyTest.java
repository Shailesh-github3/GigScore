package com.org.gigscore.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void normalize_nullReturnsZero() {
        assertThat(Money.normalize(null)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void normalize_roundsHalfUpToTwoDecimals() {
        assertThat(Money.normalize(new BigDecimal("10.005"))).isEqualTo(new BigDecimal("10.01"));
        assertThat(Money.normalize(new BigDecimal("10.004"))).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    void add_sumsAndNormalizes() {
        assertThat(Money.add(new BigDecimal("1000.005"), new BigDecimal("700.004")))
                .isEqualTo(new BigDecimal("1700.01"));
    }

    @Test
    void zero_hasCanonicalScale() {
        assertThat(Money.ZERO.scale()).isEqualTo(2);
    }
}