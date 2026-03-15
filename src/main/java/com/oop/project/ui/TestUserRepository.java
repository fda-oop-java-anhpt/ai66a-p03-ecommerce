package com.oop.project.ui;

import com.oop.project.repository.UserRepository;
import com.oop.project.repository.impl.UserRepositoryImpl;

public class TestUserRepository {
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepositoryImpl();

        System.out.println("Testing UserRepository...");
        userRepository.findAll().forEach(System.out::println);
    }
}