package com.library.lms.util;

import java.math.BigDecimal;

/**
 * Fine table:
 *   0-3 days   -> Rs. 0
 *   4-7 days   -> Rs. 100
 *   8-14 days  -> Rs. 250
 *   15-30 days -> Rs. 500
 *   30+ days   -> Rs. 1000
 */
public final class FineCalculator {

    private FineCalculator() {
    }

    public static BigDecimal calculate(int lateDays) {
        if (lateDays < 0) {
            throw new IllegalArgumentException("Late days must not be negative");
        }
        if (lateDays <= 3) return BigDecimal.ZERO;
        if (lateDays <= 7) return BigDecimal.valueOf(100);
        if (lateDays <= 14) return BigDecimal.valueOf(250);
        if (lateDays <= 30) return BigDecimal.valueOf(500);
        return BigDecimal.valueOf(1000);
    }
}
