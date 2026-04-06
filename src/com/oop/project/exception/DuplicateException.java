package com.oop.project.exception;

/**
 * Thrown when attempting to create a record that would violate a uniqueness constraint —
 * e.g. duplicate SKU or duplicate username.
 */
public class DuplicateException extends RuntimeException {

    public DuplicateException(String message) {
        super(message);
    }

    public DuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
}
