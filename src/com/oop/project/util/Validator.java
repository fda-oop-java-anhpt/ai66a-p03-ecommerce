package com.oop.project.util;

import com.oop.project.exception.ValidationException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Utility class providing validation helper methods.
 * Used by service layer to validate data before processing.
 * 
 * @author Service Team
 * @version 1.0
 */
public class Validator {
    
    // ============ STRING VALIDATION ============
    
    /**
     * Validates if a string is not null and not empty.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error message
     * @throws ValidationException if string is null or empty
     */
    public static void validateRequired(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required and cannot be empty");
        }
    }
    
    /**
     * Validates string length.
     * 
     * @param value the string to validate
     * @param fieldName the field name
     * @param minLength minimum length (inclusive)
     * @param maxLength maximum length (inclusive)
     * @throws ValidationException if length is out of range
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) 
            throws ValidationException {
        if (value == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null");
        }
        if (value.length() < minLength || value.length() > maxLength) {
            throw new ValidationException(fieldName, 
                fieldName + " must be between " + minLength + " and " + maxLength + " characters");
        }
    }
    
    // ============ EMAIL VALIDATION ============
    
    /**
     * Validates email format.
     * 
     * @param email the email to validate
     * @throws ValidationException if email format is invalid
     */
    public static void validateEmail(String email) throws ValidationException {
        validateRequired(email, "Email");
        if (!ValidationRules.EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Email", "Invalid email format: " + email);
        }
    }
    
    // ============ PHONE VALIDATION ============
    
    /**
     * Validates phone number format (Vietnamese).
     * 
     * @param phone the phone number to validate
     * @throws ValidationException if phone format is invalid
     */
    public static void validatePhone(String phone) throws ValidationException {
        validateRequired(phone, "Phone");
        String cleanPhone = phone.replaceAll("\\s+", ""); // Remove whitespace
        if (!ValidationRules.PHONE_PATTERN.matcher(cleanPhone).matches()) {
            throw new ValidationException("Phone", 
                "Invalid phone format. Must be 10-11 digits starting with 0 or +84");
        }
    }
    
    // ============ PASSWORD VALIDATION ============
    
    /**
     * Validates password strength.
     * 
     * @param password the password to validate
     * @throws ValidationException if password doesn't meet requirements
     */
    public static void validatePassword(String password) throws ValidationException {
        validateRequired(password, "Password");
        if (password.length() < ValidationRules.MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password", 
                "Password must be at least " + ValidationRules.MIN_PASSWORD_LENGTH + " characters");
        }
        if (password.length() > ValidationRules.MAX_PASSWORD_LENGTH) {
            throw new ValidationException("Password", 
                "Password cannot exceed " + ValidationRules.MAX_PASSWORD_LENGTH + " characters");
        }
    }
    
    // ============ USERNAME VALIDATION ============
    
    /**
     * Validates username format.
     * 
     * @param username the username to validate
     * @throws ValidationException if username format is invalid
     */
    public static void validateUsername(String username) throws ValidationException {
        validateRequired(username, "Username");
        if (!ValidationRules.USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException("Username", 
                "Username must be 3-50 characters, alphanumeric and underscore only");
        }
    }
    
    // ============ SKU VALIDATION ============
    
    /**
     * Validates SKU format.
     * 
     * @param sku the SKU to validate
     * @throws ValidationException if SKU format is invalid
     */
    public static void validateSku(String sku) throws ValidationException {
        validateRequired(sku, "SKU");
        if (!ValidationRules.SKU_PATTERN.matcher(sku).matches()) {
            throw new ValidationException("SKU", 
                "SKU must be 3-20 characters, uppercase letters, numbers, and hyphens only");
        }
    }
    
    // ============ PRICE VALIDATION ============
    
    /**
     * Validates price is positive and within range.
     * 
     * @param price the price to validate
     * @param fieldName the field name
     * @throws ValidationException if price is invalid
     */
    public static void validatePrice(BigDecimal price, String fieldName) throws ValidationException {
        if (price == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be greater than 0");
        }
        if (price.compareTo(new BigDecimal(ValidationRules.MAX_PRICE)) > 0) {
            throw new ValidationException(fieldName, 
                fieldName + " cannot exceed " + ValidationRules.MAX_PRICE);
        }
    }
    
    // ============ QUANTITY VALIDATION ============
    
    /**
     * Validates quantity is positive and within range.
     * 
     * @param quantity the quantity to validate
     * @throws ValidationException if quantity is invalid
     */
    public static void validateQuantity(int quantity) throws ValidationException {
        if (quantity < ValidationRules.MIN_QUANTITY) {
            throw new ValidationException("Quantity", 
                "Quantity must be at least " + ValidationRules.MIN_QUANTITY);
        }
        if (quantity > ValidationRules.MAX_QUANTITY) {
            throw new ValidationException("Quantity", 
                "Quantity cannot exceed " + ValidationRules.MAX_QUANTITY);
        }
    }
    
    // ============ COUPON CODE VALIDATION ============
    
    /**
     * Validates coupon code format.
     * 
     * @param couponCode the coupon code to validate
     * @throws ValidationException if coupon code format is invalid
     */
    public static void validateCouponCode(String couponCode) throws ValidationException {
        validateRequired(couponCode, "Coupon Code");
        if (!ValidationRules.COUPON_CODE_PATTERN.matcher(couponCode).matches()) {
            throw new ValidationException("Coupon Code", 
                "Coupon code must be 4-20 characters, uppercase letters and numbers only");
        }
    }
    
    // ============ DATE VALIDATION ============
    
    /**
     * Validates if a date is not in the past.
     * 
     * @param date the date to validate
     * @param fieldName the field name
     * @throws ValidationException if date is in the past
     */
    public static void validateFutureDate(Date date, String fieldName) throws ValidationException {
        if (date == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null");
        }
        if (date.before(new Date())) {
            throw new ValidationException(fieldName, fieldName + " cannot be in the past");
        }
    }
    
    /**
     * Validates if a timestamp is not in the past.
     * 
     * @param timestamp the timestamp to validate
     * @param fieldName the field name
     * @throws ValidationException if timestamp is in the past
     */
    public static void validateFutureTimestamp(Timestamp timestamp, String fieldName) 
            throws ValidationException {
        if (timestamp == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null");
        }
        if (timestamp.before(new Timestamp(System.currentTimeMillis()))) {
            throw new ValidationException(fieldName, fieldName + " cannot be in the past");
        }
    }
    
    // ============ ORDER STATUS VALIDATION ============
    
    /**
     * Validates if order status is valid.
     * 
     * @param status the status to validate
     * @throws ValidationException if status is invalid
     */
    public static void validateOrderStatus(String status) throws ValidationException {
        validateRequired(status, "Order Status");
        for (String validStatus : ValidationRules.VALID_ORDER_STATUSES) {
            if (validStatus.equalsIgnoreCase(status)) {
                return; // Valid status found
            }
        }
        throw new ValidationException("Order Status", 
            "Invalid status: " + status + ". Must be PENDING, PAID, or CANCELLED");
    }
    
    // ============ DISCOUNT TYPE VALIDATION ============
    
    /**
     * Validates if discount type is valid.
     * 
     * @param discountType the discount type to validate
     * @throws ValidationException if discount type is invalid
     */
    public static void validateDiscountType(String discountType) throws ValidationException {
        validateRequired(discountType, "Discount Type");
        for (String validType : ValidationRules.VALID_DISCOUNT_TYPES) {
            if (validType.equalsIgnoreCase(discountType)) {
                return; // Valid type found
            }
        }
        throw new ValidationException("Discount Type", 
            "Invalid discount type: " + discountType + ". Must be Percent or Fixed");
    }
    
    // ============ ID VALIDATION ============
    
    /**
     * Validates if ID is positive.
     * 
     * @param id the ID to validate
     * @param fieldName the field name
     * @throws ValidationException if ID is not positive
     */
    public static void validatePositiveId(int id, String fieldName) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be a positive integer");
        }
    }
    
    // Private constructor to prevent instantiation
    private Validator() {
        throw new AssertionError("Validator is a utility class and should not be instantiated");
    }
}