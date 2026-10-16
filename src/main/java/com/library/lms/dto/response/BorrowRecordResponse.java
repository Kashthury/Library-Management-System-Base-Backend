package com.library.lms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * numberOfBooks    = books of this record still borrowed (0 once everything is returned)
 * originalQuantity = books that were borrowed in the first place
 * overdueDays      = live value for active records, stored value for returned ones
 * fineAmount       = fine charged so far for this record (null until a return happens)
 */
public record BorrowRecordResponse(
        Long borrowRecordId,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        Integer numberOfBooks,
        Integer originalQuantity,
        Long studentId,
        String studentName,
        Long bookId,
        String bookTitle,
        Integer overdueDays,
        BigDecimal fineAmount,
        String status
) {
}
