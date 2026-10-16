package com.library.lms.dto.response;

public record BookResponse(
        Long bookId,
        String bookTitle,
        String author,
        String category,
        Integer availableQuantity
) {
}
