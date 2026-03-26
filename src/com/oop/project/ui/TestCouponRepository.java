package com.oop.project.ui;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.oop.project.model.Coupon;
import com.oop.project.model.DiscountType;
import com.oop.project.repository.CouponRepository;
import com.oop.project.repository.impl.CouponRepositoryImpl;

public class TestCouponRepository {
    public static void main(String[] args) {

        CouponRepository repo = new CouponRepositoryImpl();

        System.out.println("=== COUPON: FIND ALL ===");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n=== COUPON: FIND BY CODE (GREET2026) ===");
        System.out.println(repo.findByCode("GREET2026").orElse(null));

        System.out.println("\n=== COUPON: SAVE ===");
        String code = "T" + System.currentTimeMillis();
        Coupon coupon = new Coupon(
                code,
                new BigDecimal("10.00"),
                DiscountType.Percent,   // enum của mày
                new BigDecimal("100000.00"),
                new Timestamp(System.currentTimeMillis()),
                Date.valueOf("2026-12-31"),
                true
        );

        System.out.println("Saved: " + repo.save(coupon));

        System.out.println("\n=== COUPON: FIND BY CODE (new) ===");
        System.out.println(repo.findByCode(code).orElse(null));
    }
}