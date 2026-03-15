package com.oop.project.ui;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.oop.project.model.Coupon;
import com.oop.project.repository.CouponRepository;
import com.oop.project.repository.impl.CouponRepositoryImpl;

public class TestCouponRepository {

    public static void main(String[] args) {

        CouponRepository repo = new CouponRepositoryImpl();

        System.out.println("=== FIND ALL ===");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n=== FIND BY ID ===");
        System.out.println(repo.findById("GREET2026").orElse(null));

        Coupon coupon = new Coupon(
                "TEST20",
                new BigDecimal("20.00"),
                "Percent",
                new BigDecimal("100.00"),
                new Timestamp(System.currentTimeMillis()),
                Date.valueOf("2026-12-31"),
                true
        );

        System.out.println("\n=== SAVE ===");
        System.out.println("Saved: " + repo.save(coupon));

        System.out.println("\n=== FIND ALL AFTER SAVE ===");
        repo.findAll().forEach(System.out::println);
    }
}