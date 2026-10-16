package com.library.lms.dto.response;

import java.math.BigDecimal;

public record StudentResponse(
        Long studentId,
        String studentName,
        Integer age,
        String gender,
        String course,
        BigDecimal fineAmount
) {
}
