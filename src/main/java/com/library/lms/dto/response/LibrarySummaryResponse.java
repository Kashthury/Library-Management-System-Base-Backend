package com.library.lms.dto.response;

import lombok.Builder;

/** Field names match the frontend's LibrarySummary type exactly. */
@Builder
public record LibrarySummaryResponse(
        long totalStudents,
        long totalMaleStudents,
        long totalFemaleStudents,
        long totalBooksCurrentlyBorrowed,
        long studentsBorrowing0Books,
        long studentsBorrowing1Book,
        long studentsBorrowing2Books,
        long studentsBorrowing3Books,
        long studentsBorrowing4Books,
        long studentsBorrowing5Books,
        long totalRegisteredBooks,
        long totalAvailableBooks,
        long totalBorrowedBooks
) {
}
