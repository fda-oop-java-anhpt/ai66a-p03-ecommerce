package com.oop.project.exception;

<<<<<<< HEAD
/**
 * Thrown when an order's requested quantity exceeds the available stock for an item.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
=======
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
>>>>>>> 118f56a (Add files in exception)
}
