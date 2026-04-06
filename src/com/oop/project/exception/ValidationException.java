package com.oop.project.exception;
/**
 * Thrown when user input fails validation — invalid phone, email, SKU format, empty fields, etc.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
