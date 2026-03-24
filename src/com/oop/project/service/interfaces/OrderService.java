package com.oop.project.service.interfaces;

import com.oop.project.model.Order;
import com.oop.project.model.OrderDetail;
import com.oop.project.model.OrderStatus;
import com.oop.project.model.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * FR-3: Order and Billing Management
 * FR-5: Order Dashboard and Search
 *
 * Handles all business logic for creating, viewing, updating, and
 * cancelling customer orders. Also provides search and filter methods
 * for the Dashboard tab.
 *
 * Validation rules:
 * - Order must have at least 1 OrderDetail
 * - Customer must exist before creating an order
 * - Only ADMIN can delete a finalized order
 * - Cancellation is logged in AuditLog (FR-4.4)
 *
 * @author Lan - Service Layer
 */
public interface OrderService {

    /**
     * Retrieve all orders in the system.
     *
     * FR-5.1: The system shall display all orders in a sortable list.
     *
     * @return list of all Order objects (with Customer and OrderDetail loaded)
     */
    List<Order> getAllOrders();

    /**
     * Find a specific order by its ID.
     *
     * FR-5.3: The system shall allow searching orders by order ID.
     *
     * @param orderId  the order's unique ID
     * @return Optional<Order> with full details, empty if not found
     */
    Optional<Order> getOrderById(int orderId);

    /**
     * Create a new order for a customer.
     *
     * FR-3.1: The system shall allow creating orders.
     * FR-3.2: The system shall allow adding multiple items with quantity to an order.
     *
     * This method:
     * 1. Validates the customer exists
     * 2. Validates each OrderDetail (item exists, quantity > 0)
     * 3. Applies coupon if provided (via CouponService)
     * 4. Calculates billing via BillingService.computeBill()
     * 5. Saves Order + all OrderDetails to database
     * 6. Records creation in AuditLog (FR-4.4)
     *
     * @param order        the Order object (with customer set, coupon optional)
     * @param orderDetails list of OrderDetail items to include
     * @param actor        the currently logged-in User creating the order
     * @return the saved Order with orderId populated, or null if failed
     * @throws IllegalArgumentException if customer not found or orderDetails is empty
     */
    Order createOrder(Order order, List<OrderDetail> orderDetails, User actor);

    /**
     * Update the status of an existing order.
     *
     * FR-3.1: The system shall allow updating orders.
     * FR-4.3: Valid statuses: PENDING, PAID, CANCELLED.
     * FR-4.4: Updates are logged in AuditLog.
     *
     * @param orderId    the ID of the order to update
     * @param newStatus  the new OrderStatus (PENDING / PAID / CANCELLED)
     * @param actor      the currently logged-in User
     * @return true if updated successfully
     * @throws IllegalArgumentException if order not found
     */
    boolean updateOrderStatus(int orderId, OrderStatus newStatus, User actor);

    /**
     * Cancel an order and record the cancellation in the audit log.
     *
     * FR-3.1: The system shall allow deleting (cancelling) orders.
     * FR-4.4: The system shall maintain an audit log for order cancellations.
     *
     * @param orderId  the ID of the order to cancel
     * @param actor    the currently logged-in User
     * @return true if cancelled successfully
     * @throws IllegalArgumentException if order not found or already cancelled
     */
    boolean cancelOrder(int orderId, User actor);

    /**
     * Search orders by customer name or order ID.
     *
     * FR-5.3: The system shall allow searching orders by customer name or order ID.
     *
     * @param keyword  the search term (matched against customer name or order ID string)
     * @return list of matching Order objects
     */
    List<Order> searchOrders(String keyword);

    /**
     * Filter orders by status.
     *
     * FR-5.2: The system shall filter orders by status.
     *
     * @param status  the OrderStatus to filter by (PENDING / PAID / CANCELLED)
     * @return list of Order objects with the given status
     */
    List<Order> filterByStatus(OrderStatus status);

    /**
     * Filter orders by a date range.
     *
     * FR-5.2: The system shall filter orders by date range.
     *
     * @param from  the start of the date range (inclusive)
     * @param to    the end of the date range (inclusive)
     * @return list of Order objects within the date range
     */
    List<Order> filterByDateRange(Timestamp from, Timestamp to);

    /**
     * Get all orders belonging to a specific customer.
     *
     * Used by CustomerService.getCustomerOrderHistory().
     *
     * @param customerId  the customer's ID
     * @return list of Order objects for that customer
     */
    List<Order> getOrdersByCustomerId(int customerId);
}
