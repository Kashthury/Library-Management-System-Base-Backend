package com.library.lms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank(message = "Book title must not be empty")
        @Size(max = 200, message = "Book title must be at most 200 characters")
        String bookTitle,

        @NotBlank(message = "Author must not be empty")
        @Size(max = 150, message = "Author must be at most 150 characters")
        String author,

        @NotBlank(message = "Category must not be empty")
        @Size(max = 100, message = "Category must be at most 100 characters")
        String category,

        @NotNull(message = "Available quantity is required")
        @Min(value = 0, message = "Available quantity must not be negative")
        Integer availableQuantity
) {
}
