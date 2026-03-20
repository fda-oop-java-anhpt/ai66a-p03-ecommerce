package com.oop.project.service.interfaces;

import com.oop.project.model.Order;
import com.oop.project.service.exception.ServiceException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Service interface for order management.
 * Handles order creation, status updates, cancellation with audit logging.
 * 
 * @author Service Team - Member 3
 * @version 1.0
 */
public interface OrderService {
    
    /**
     * Creates a new order with items and optional coupon.
     * Validates customer, items, stock availability, and coupon.
     * Calculates subtotal, applies discount, and computes final total with tax.
     * 
     * @param customerId the customer placing the order
     * @param items Map of item SKU to quantity
     * @param couponCode optional coupon code (can be null)
     * @return the created Order object with all calculated values
     * @throws ServiceException if validation fails or stock insufficient
     */
    Order createOrder(int customerId, Map<String, Integer> items, String couponCode) throws ServiceException;
    
    /**
     * Updates the status of an order.
     * Valid statuses: PENDING, PAID, CANCELLED
     * Logs status change in audit_log table.
     * 
     * @param orderId the order ID
     * @param newStatus the new status to set
     * @return true if update successful
     * @throws ServiceException if order not found or invalid status
     */
    boolean updateOrderStatus(int orderId, String newStatus) throws ServiceException;
    
    /**
     * Cancels an order and restores item stock.
     * Records cancellation in audit log.
     * Only PENDING orders can be cancelled.
     * 
     * @param orderId the order ID to cancel
     * @return true if cancellation successful
     * @throws ServiceException if order already paid/cancelled or not found
     */
    boolean cancelOrder(int orderId) throws ServiceException;
    
    /**
     * Retrieves an order by ID with complete details.
     * 
     * @param orderId the order ID
     * @return Order object if found, null otherwise
     */
    Order getOrderById(int orderId);
    
    /**
     * Retrieves all orders in the system.
     * 
     * @return List of all orders
     */
    List<Order> getAllOrders();
    
    /**
     * Retrieves orders for a specific customer.
     * 
     * @param customerId the customer ID
     * @return List of orders for this customer
     */
    List<Order> getOrdersByCustomer(int customerId);
    
    /**
     * Retrieves orders by status.
     * 
     * @param status the order status (PENDING, PAID, CANCELLED)
     * @return List of orders with matching status
     */
    List<Order> getOrdersByStatus(String status);
    
    /**
     * Retrieves orders within a date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return List of orders in date range
     */
    List<Order> getOrdersByDateRange(Timestamp startDate, Timestamp endDate);
    
    /**
     * Searches orders by customer name or order ID.
     * 
     * @param keyword the search keyword
     * @return List of matching orders
     */
    List<Order> searchOrders(String keyword);
    
    /**
     * Calculates total revenue from all PAID orders.
     * 
     * @return total revenue as BigDecimal
     */
    BigDecimal calculateTotalRevenue();
    
    /**
     * Counts orders by status.
     * 
     * @param status the order status
     * @return count of orders with given status
     */
    int countOrdersByStatus(String status);
    
    /**
     * Retrieves order details including items, quantities, and prices.
     * Used for invoice generation.
     * 
     * @param orderId the order ID
     * @return Map with order details and items
     */
    Map<String, Object> getOrderDetailsForInvoice(int orderId);
}
