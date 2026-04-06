package com.oop.project.exception;

<<<<<<< HEAD
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
=======
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
>>>>>>> 118f56a (Add files in exception)
}
