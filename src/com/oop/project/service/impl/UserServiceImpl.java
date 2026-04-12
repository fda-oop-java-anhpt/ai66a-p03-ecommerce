package com.oop.project.service.impl;

import com.oop.project.model.User;
import com.oop.project.repository.interfaces.UserRepository;
import com.oop.project.service.interfaces.IUserService;

import java.util.List;

/**
 * Implementation of IUserService — Staff / User management for Admin.
 */
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepo;

    public UserServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public void addStaff(User user) {
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (user.getUserPassword() == null || user.getUserPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        // Check username uniqueness
        if (userRepo.findByUsername(user.getUserName().trim()) != null) {
            throw new IllegalArgumentException("Username \"" + user.getUserName() + "\" already exists.");
        }
        if (!userRepo.insert(user)) {
            throw new RuntimeException("Failed to insert user into database.");
        }
    }

    @Override
    public void deleteUser(int userId, int adminId) {
        if (userId == adminId) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }
        if (!userRepo.delete(userId)) {
            throw new RuntimeException("Failed to delete user (user may not exist).");
        }
    }
}
