package com.oop.project.service;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.User;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(int id);
    boolean createUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(int id);
}