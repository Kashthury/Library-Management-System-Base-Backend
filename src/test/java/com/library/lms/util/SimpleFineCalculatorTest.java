package com.library.lms.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A beginner-friendly example: give the calculator late days and check the fine. */
class SimpleFineCalculatorTest {

    @Test
    void calculatesFineForFourLateDays() {
        // Arrange: decide how many days late the book is.
        int lateDays = 4;

        // Act: calculate the fine.
        BigDecimal actualFine = FineCalculator.calculate(lateDays);

        // Assert: 4 late days should cost Rs. 100.
        BigDecimal expectedFine = BigDecimal.valueOf(100);
        assertEquals(0, expectedFine.compareTo(actualFine));
    }
}
