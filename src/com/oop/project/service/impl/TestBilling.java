// File: src/com/oop/project/service/impl/TestBilling.java
package com.oop.project.service.impl;

import java.math.BigDecimal;

public class TestBilling {
    public static void main(String[] args) {
        BillingServiceImpl billing = new BillingServiceImpl();

        // Test overload 1: computeBill(price)
        BigDecimal r1 = billing.computeBill(new BigDecimal("100"));
        System.out.println("Overload 1 - price=100 → " + r1);
        // Expected: 108.00

        // Test overload 2: computeBill(price, qty)
        BigDecimal r2 = billing.computeBill(new BigDecimal("50"), 2);
        System.out.println("Overload 2 - price=50, qty=2 → " + r2);
        // Expected: 108.00

        // Test overload 3: computeBill(price, qty, discount)
        BigDecimal r3 = billing.computeBill(new BigDecimal("50"), 2, new BigDecimal("10"));
        System.out.println("Overload 3 - price=50, qty=2, discount=10 → " + r3);
        // Expected: 97.20
    }
}