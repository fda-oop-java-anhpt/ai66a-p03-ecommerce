package com.oop.project.exception;
/**
 * Thrown when a coupon is expired, inactive, does not exist,
 * or the order total does not meet the minimum required value (FR-4.2).
 */
public class CouponExpiredException extends RuntimeException {

    public CouponExpiredException(String message) {
        super(message);
    }

    public CouponExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
