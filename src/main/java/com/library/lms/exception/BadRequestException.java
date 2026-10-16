package com.library.lms.exception;

/** Thrown for invalid input that bean validation cannot catch -> HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
