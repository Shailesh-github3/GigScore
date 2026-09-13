package com.org.gigscore.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    public static final int COLUMN_PRECISION = 12;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE, ROUNDING);

    private Money() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(SCALE, ROUNDING);
    }

    public static BigDecimal add(BigDecimal left, BigDecimal right) {
        return normalize(normalize(left).add(normalize(right)));
    }
}