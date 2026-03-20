package com.oop.project.exception;

/**
 * Exception thrown when data validation fails.
 * Used for invalid email, phone, duplicate SKU, etc.
 * 
 * @author Service Team
 * @version 1.0
 */
public class ValidationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private String fieldName;
    
    /**
     * Constructs a new ValidationException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ValidationException with field name and message.
     * 
     * @param fieldName the name of the invalid field
     * @param message the detail message
     */
    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }
    
    /**
     * Gets the name of the field that failed validation.
     * 
     * @return the field name, or null if not specified
     */
    public String getFieldName() {
        return fieldName;
    }
}