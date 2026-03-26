package com.oop.project.service.impl;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.User;
import com.oop.project.repository.UserRepository;
import com.oop.project.service.UserService;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(int id) {
        if (id <= 0) {
            System.out.println("User ID must be greater than 0.");
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    @Override
    public boolean createUser(User user) {
        if (!validateUser(user)) return false;
        if (userRepository.findById(user.getUserId()).isPresent()) {
            System.out.println("User already exists.");
            return false;
        }
        return userRepository.save(user);
    }

    @Override
    public boolean updateUser(User user) {
        if (!validateUser(user)) return false;
        if (userRepository.findById(user.getUserId()).isEmpty()) {
            System.out.println("User not found.");
            return false;
        }
        return userRepository.update(user);
    }

    @Override
    public boolean deleteUser(int id) {
        if (id <= 0) {
            System.out.println("User ID must be greater than 0.");
            return false;
        }
        if (userRepository.findById(id).isEmpty()) {
            System.out.println("User not found.");
            return false;
        }
        return userRepository.deleteById(id);
    }

    private boolean validateUser(User user) {
        if (user == null) {
            System.out.println("User cannot be null.");
            return false;
        }
        if (user.getUserId() <= 0) {
            System.out.println("User ID must be greater than 0.");
            return false;
        }
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            System.out.println("Username cannot be empty.");
            return false;
        }
        if (user.getUserName().length() > 200) {
            System.out.println("Username must be <= 200 characters.");
            return false;
        }
        if (user.getUserPassword() == null || user.getUserPassword().trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        if (user.getUserPassword().length() < 6) {
            System.out.println("Password must be at least 6 characters.");
            return false;
        }
        if (user.getUserRole() == null) {
            System.out.println("User role cannot be null.");
            return false;
        }
        return true;
    }
}