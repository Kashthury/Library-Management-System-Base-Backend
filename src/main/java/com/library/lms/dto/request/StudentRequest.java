package com.library.lms.dto.request;

import jakarta.validation.constraints.*;

public record StudentRequest(
        @NotBlank(message = "Student name must not be empty")
        @Size(max = 100, message = "Student name must be at most 100 characters")
        String studentName,

        @NotNull(message = "Age is required")
        @Min(value = 1, message = "Age must be a positive number")
        @Max(value = 120, message = "Age must be 120 or less")
        Integer age,

        @NotBlank(message = "Gender is required (Male or Female)")
        @Pattern(regexp = "^(?i)(M|F|Male|Female)$", message = "Gender must be Male or Female")
        String gender,

        @NotBlank(message = "Course must not be empty")
        @Size(max = 100, message = "Course must be at most 100 characters")
        String course
) {
}
