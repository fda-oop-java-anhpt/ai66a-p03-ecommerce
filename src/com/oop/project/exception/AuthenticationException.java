package com.oop.project.exception;

/**
 * Exception thrown when authentication or authorization fails.
 * Used for login failures, invalid credentials, and permission denials.
 * 
 * @author Service Team
 * @version 1.0
 */
public class AuthenticationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new AuthenticationException with the specified detail message.
     * 
     * @param message the detail message
     */
    public AuthenticationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new AuthenticationException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}