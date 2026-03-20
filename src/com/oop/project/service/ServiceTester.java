package com.oop.project.service;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.service.impl.*;
import com.oop.project.service.interfaces.*;
import com.oop.project.exception.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * Service Layer Test Class
 * Demonstrates all services working with stub data.
 * 
 * This class can be run to verify that all Service implementations work correctly.
 * 
 * @author Service Team - Member 3
 * @version 1.0
 */
public class ServiceTester {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   SERVICE LAYER TEST - WEEK 1");
        System.out.println("========================================\n");
        
        // Test 1: Authentication Service
        testAuthenticationService();
        
        // Test 2: Billing Service (MOST IMPORTANT)
        testBillingService();
        
        // Test 3: Permission Checking
        testPermissionChecking();
        
        System.out.println("\n========================================");
        System.out.println("   ALL TESTS COMPLETED ✅");
        System.out.println("========================================");
    }
    
    /**
     * Test 1: Authentication Service
     */
    private static void testAuthenticationService() {
        System.out.println("📝 TEST 1: AUTHENTICATION SERVICE");
        System.out.println("----------------------------------");
        
        try {
            AuthenticationService authService = new AuthenticationServiceImpl();
            
            // Test login with admin
            System.out.println("Testing login with admin credentials...");
            User admin = authService.login("admin", "admin123");
            System.out.println("✅ Login successful!");
            System.out.println("   User: " + admin.getUserName());
            System.out.println("   Role: " + admin.getUserRole());
            
            // Test login with staff
            System.out.println("\nTesting login with staff credentials...");
            User staff = authService.login("staff", "staff123");
            System.out.println("✅ Login successful!");
            System.out.println("   User: " + staff.getUserName());
            System.out.println("   Role: " + staff.getUserRole());
            
            // Test invalid login
            System.out.println("\nTesting invalid login...");
            try {
                authService.login("invalid", "wrong");
                System.out.println("❌ Should have thrown exception!");
            } catch (AuthenticationException e) {
                System.out.println("✅ Correctly rejected: " + e.getMessage());
            }
            
            // Test logout
            System.out.println("\nTesting logout...");
            boolean loggedOut = authService.logout(admin.getUserId());
            System.out.println("✅ Logout successful: " + loggedOut);
            
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test 2: Billing Service (MOST IMPORTANT)
     * Demonstrates 3 overloaded computeBill methods
     */
    private static void testBillingService() {
        System.out.println("⭐ TEST 2: BILLING SERVICE (CỰC KỲ QUAN TRỌNG)");
        System.out.println("------------------------------------------------");
        
        try {
            BillingService billingService = new BillingServiceImpl();
            
            // Test Method #1: Price only
            System.out.println("Method #1: computeBill(price)");
            BigDecimal price1 = new BigDecimal("100.00");
            BigDecimal total1 = billingService.computeBill(price1);
            System.out.println("   Input: $" + price1);
            System.out.println("   Formula: 100.00 × 1.08");
            System.out.println("   Output: $" + total1);
            System.out.println("   Expected: $108.00");
            System.out.println("   ✅ " + (total1.compareTo(new BigDecimal("108.00")) == 0 ? "PASS" : "FAIL"));
            
            // Test Method #2: Price + Quantity
            System.out.println("\nMethod #2: computeBill(price, quantity)");
            BigDecimal price2 = new BigDecimal("50.00");
            int quantity2 = 2;
            BigDecimal total2 = billingService.computeBill(price2, quantity2);
            System.out.println("   Input: $" + price2 + " × " + quantity2);
            System.out.println("   Formula: (50.00 × 2) × 1.08");
            System.out.println("   Output: $" + total2);
            System.out.println("   Expected: $108.00");
            System.out.println("   ✅ " + (total2.compareTo(new BigDecimal("108.00")) == 0 ? "PASS" : "FAIL"));
            
            // Test Method #3: Full calculation with discount
            System.out.println("\nMethod #3: computeBill(price, quantity, discount)");
            BigDecimal price3 = new BigDecimal("50.00");
            int quantity3 = 2;
            BigDecimal discount3 = new BigDecimal("10.00");
            BigDecimal total3 = billingService.computeBill(price3, quantity3, discount3);
            System.out.println("   Input: $" + price3 + " × " + quantity3 + " - $" + discount3 + " discount");
            System.out.println("   Formula: ((50.00 × 2) - 10.00) × 1.08");
            System.out.println("   Breakdown:");
            System.out.println("     Subtotal: $100.00");
            System.out.println("     Discount: -$10.00");
            System.out.println("     After discount: $90.00");
            System.out.println("     Tax (8%): +$7.20");
            System.out.println("   Output: $" + total3);
            System.out.println("   Expected: $97.20");
            System.out.println("   ✅ " + (total3.compareTo(new BigDecimal("97.20")) == 0 ? "PASS" : "FAIL"));
            
            // Test billing breakdown
            System.out.println("\nTesting generateBillingBreakdown()...");
            Map<String, BigDecimal> breakdown = billingService.generateBillingBreakdown(
                new BigDecimal("50.00"), 2, new BigDecimal("10.00")
            );
            System.out.println("   Subtotal: $" + breakdown.get("subtotal"));
            System.out.println("   Discount: $" + breakdown.get("discount"));
            System.out.println("   Tax Amount: $" + breakdown.get("taxAmount"));
            System.out.println("   Final Total: $" + breakdown.get("finalTotal"));
            System.out.println("   ✅ Breakdown generated successfully");
            
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test 3: Permission Checking
     */
    private static void testPermissionChecking() {
        System.out.println("🔒 TEST 3: PERMISSION CHECKING");
        System.out.println("-------------------------------");
        
        try {
            AuthenticationService authService = new AuthenticationServiceImpl();
            
            // Test ADMIN permissions
            System.out.println("Testing ADMIN permissions:");
            boolean adminUpdatePrice = authService.checkPermission(UserRole.ADMIN, "UPDATE_PRICE");
            boolean adminDeleteOrder = authService.checkPermission(UserRole.ADMIN, "DELETE_ORDER");
            boolean adminCreateOrder = authService.checkPermission(UserRole.ADMIN, "CREATE_ORDER");
            
            System.out.println("   UPDATE_PRICE: " + (adminUpdatePrice ? "✅ ALLOWED" : "❌ DENIED"));
            System.out.println("   DELETE_ORDER: " + (adminDeleteOrder ? "✅ ALLOWED" : "❌ DENIED"));
            System.out.println("   CREATE_ORDER: " + (adminCreateOrder ? "✅ ALLOWED" : "❌ DENIED"));
            
            // Test STAFF permissions
            System.out.println("\nTesting STAFF permissions:");
            boolean staffUpdatePrice = authService.checkPermission(UserRole.STAFF, "UPDATE_PRICE");
            boolean staffDeleteOrder = authService.checkPermission(UserRole.STAFF, "DELETE_ORDER");
            boolean staffCreateOrder = authService.checkPermission(UserRole.STAFF, "CREATE_ORDER");
            
            System.out.println("   UPDATE_PRICE: " + (staffUpdatePrice ? "✅ ALLOWED" : "❌ DENIED (correct!)"));
            System.out.println("   DELETE_ORDER: " + (staffDeleteOrder ? "✅ ALLOWED" : "❌ DENIED (correct!)"));
            System.out.println("   CREATE_ORDER: " + (staffCreateOrder ? "✅ ALLOWED" : "❌ DENIED"));
            
            // Verify STAFF cannot do admin actions
            if (!staffUpdatePrice && !staffDeleteOrder) {
                System.out.println("\n✅ Permission system working correctly!");
                System.out.println("   STAFF correctly blocked from admin actions.");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
}
