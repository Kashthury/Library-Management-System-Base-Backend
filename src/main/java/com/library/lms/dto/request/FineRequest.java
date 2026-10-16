package com.library.lms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FineRequest(
        @NotNull(message = "Late days is required")
        @Min(value = 0, message = "Late days must not be negative")
        Integer lateDays
) {
}
