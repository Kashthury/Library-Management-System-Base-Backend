package com.library.lms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** returnDate defaults to today. */
public record ReturnRequest(
        @NotNull(message = "Student ID is required")
        Long studentId,

        @NotNull(message = "Book ID is required")
        Long bookId,

        @NotNull(message = "Number of books is required")
        @Min(value = 1, message = "Number of books must be greater than 0")
        Integer numberOfBooks,

        LocalDate returnDate
) {
}
