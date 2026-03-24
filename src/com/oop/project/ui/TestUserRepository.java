package com.oop.project.ui;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.UserRepository;
import com.oop.project.repository.impl.UserRepositoryImpl;

import java.sql.Timestamp;

public class TestUserRepository {
    public static void main(String[] args) {

        UserRepository repo = new UserRepositoryImpl();

        System.out.println("=== USER: FIND ALL ===");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n=== USER: FIND BY ID (1) ===");
        System.out.println(repo.findById(1).orElse(null));

        System.out.println("\n=== USER: SAVE ===");
        User u = new User(
                0,
                "test_user_" + System.currentTimeMillis(),
                "123456",
                UserRole.STAFF,
                new Timestamp(System.currentTimeMillis()),
                null
        );
        System.out.println("Saved: " + repo.save(u));

        System.out.println("\n=== USER: FIND ALL AFTER SAVE ===");
        repo.findAll().forEach(System.out::println);
    }
}