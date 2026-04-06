package com.oop.project.service.interfaces;

import com.oop.project.model.Order;
import com.oop.project.model.User;
import java.math.BigDecimal;

public interface IBillingService {
    BigDecimal computeBill(BigDecimal price);
    BigDecimal computeBill(BigDecimal price, int quantity);
    BigDecimal computeBill(BigDecimal price, int quantity, BigDecimal couponDiscount);
    Order createOrder(Order order, User currentUser);
    Order updateOrder(Order order, User currentUser);
    boolean cancelOrder(int orderId, User currentUser);
    String generateInvoice(Order order);
}