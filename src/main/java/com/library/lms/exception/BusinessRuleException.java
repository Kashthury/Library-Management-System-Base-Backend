package com.library.lms.exception;

/** Thrown when a library rule is broken (limit exceeded, no stock, ...) -> HTTP 409. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
