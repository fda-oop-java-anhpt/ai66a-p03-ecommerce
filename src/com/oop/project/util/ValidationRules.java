package com.oop.project.util;

import java.util.regex.Pattern;

/**
 * Contains validation rules and regex patterns used across the service layer.
 * Centralized validation constants for consistency.
 * 
 * @author Service Team
 * @version 1.0
 */
public class ValidationRules {
    
    // ============ EMAIL VALIDATION ============
    /**
     * Email regex pattern - RFC 5322 simplified.
     * Validates standard email formats: user@domain.com
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // ============ PHONE VALIDATION ============
    /**
     * Phone number pattern - Vietnamese format.
     * Supports: 0123456789, 84123456789, +84123456789
     * Length: 10-11 digits
     */
    public static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+?84|0)[0-9]{9,10}$"
    );
    
    // ============ PASSWORD VALIDATION ============
    /**
     * Minimum password length.
     */
    public static final int MIN_PASSWORD_LENGTH = 6;
    
    /**
     * Maximum password length.
     */
    public static final int MAX_PASSWORD_LENGTH = 50;
    
    // ============ USERNAME VALIDATION ============
    /**
     * Minimum username length.
     */
    public static final int MIN_USERNAME_LENGTH = 3;
    
    /**
     * Maximum username length.
     */
    public static final int MAX_USERNAME_LENGTH = 50;
    
    /**
     * Username pattern - alphanumeric and underscore only.
     */
    public static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,50}$"
    );
    
    // ============ CUSTOMER NAME VALIDATION ============
    /**
     * Minimum customer name length.
     */
    public static final int MIN_NAME_LENGTH = 2;
    
    /**
     * Maximum customer name length.
     */
    public static final int MAX_NAME_LENGTH = 100;
    
    // ============ SKU VALIDATION ============
    /**
     * SKU pattern - uppercase letters, numbers, and hyphens.
     * Example: ITEM-001, PROD-ABC-123
     */
    public static final Pattern SKU_PATTERN = Pattern.compile(
        "^[A-Z0-9-]{3,20}$"
    );
    
    /**
     * Minimum SKU length.
     */
    public static final int MIN_SKU_LENGTH = 3;
    
    /**
     * Maximum SKU length.
     */
    public static final int MAX_SKU_LENGTH = 20;
    
    // ============ PRICE VALIDATION ============
    /**
     * Minimum item price.
     */
    public static final double MIN_PRICE = 0.01;
    
    /**
     * Maximum item price.
     */
    public static final double MAX_PRICE = 999999.99;
    
    // ============ QUANTITY VALIDATION ============
    /**
     * Minimum order quantity.
     */
    public static final int MIN_QUANTITY = 1;
    
    /**
     * Maximum order quantity per item.
     */
    public static final int MAX_QUANTITY = 9999;
    
    // ============ COUPON CODE VALIDATION ============
    /**
     * Coupon code pattern - uppercase letters and numbers only.
     * Example: SAVE10, DISCOUNT2024, FREESHIP
     */
    public static final Pattern COUPON_CODE_PATTERN = Pattern.compile(
        "^[A-Z0-9]{4,20}$"
    );
    
    /**
     * Minimum coupon code length.
     */
    public static final int MIN_COUPON_LENGTH = 4;
    
    /**
     * Maximum coupon code length.
     */
    public static final int MAX_COUPON_LENGTH = 20;
    
    // ============ ORDER STATUS VALIDATION ============
    /**
     * Valid order statuses.
     */
    public static final String[] VALID_ORDER_STATUSES = {
        "PENDING", "PAID", "CANCELLED"
    };
    
    // ============ DISCOUNT TYPE VALIDATION ============
    /**
     * Valid discount types for coupons.
     */
    public static final String[] VALID_DISCOUNT_TYPES = {
        "Percent", "Fixed"
    };
    
    // ============ PERMISSION ACTIONS ============
    /**
     * Admin-only actions that require role verification.
     */
    public static final String[] ADMIN_ONLY_ACTIONS = {
        "UPDATE_PRICE", 
        "DELETE_ORDER", 
        "DELETE_CUSTOMER",
        "CREATE_USER",
        "DELETE_COUPON"
    };
    
    /**
     * Actions available to all authenticated users.
     */
    public static final String[] USER_ACTIONS = {
        "CREATE_ORDER",
        "VIEW_ORDERS",
        "UPDATE_CUSTOMER",
        "SEARCH_ITEMS"
    };
    
    // Private constructor to prevent instantiation
    private ValidationRules() {
        throw new AssertionError("ValidationRules is a utility class and should not be instantiated");
    }
}