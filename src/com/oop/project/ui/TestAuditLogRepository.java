package com.oop.project.ui;

import java.sql.Timestamp;

import com.oop.project.model.AuditLog;
import com.oop.project.model.User;
import com.oop.project.repository.AuditLogRepository;
import com.oop.project.repository.UserRepository;
import com.oop.project.repository.impl.AuditLogRepositoryImpl;
import com.oop.project.repository.impl.UserRepositoryImpl;

public class TestAuditLogRepository {
    public static void main(String[] args) {

        AuditLogRepository logRepo = new AuditLogRepositoryImpl();
        UserRepository userRepo = new UserRepositoryImpl();

        System.out.println("=== AUDIT LOG: FIND ALL ===");
        logRepo.findAll().forEach(System.out::println);

        User u = userRepo.findById(1).orElse(null);
        if (u == null) {
            System.out.println("No user id=1 found. Please seed users first.");
            return;
        }

        System.out.println("\n=== AUDIT LOG: SAVE ===");
        AuditLog log = new AuditLog(
                0,
                u,
                "CREATE",
                "ITEM",
                "TEST-ID",
                new Timestamp(System.currentTimeMillis())
        );

        System.out.println("Saved: " + logRepo.save(log));

        System.out.println("\n=== AUDIT LOG: FIND BY USER ID (1) ===");
        logRepo.findByUserId(1).forEach(System.out::println);
    }
}