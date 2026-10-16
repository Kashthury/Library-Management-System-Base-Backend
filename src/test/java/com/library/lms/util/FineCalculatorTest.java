package com.library.lms.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FineCalculatorTest {

    @ParameterizedTest
    @CsvSource({
            "0,0", "3,0",
            "4,100", "7,100",
            "8,250", "14,250",
            "15,500", "30,500",
            "31,1000", "365,1000"
    })
    void returnsFineFromTable(int lateDays, int expected) {
        BigDecimal fine = FineCalculator.calculate(lateDays);
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(fine),
                "late days " + lateDays + " should cost " + expected + " but was " + fine);
    }

    @Test
    void rejectsNegativeLateDays() {
        assertThrows(IllegalArgumentException.class, () -> FineCalculator.calculate(-1));
    }
}
