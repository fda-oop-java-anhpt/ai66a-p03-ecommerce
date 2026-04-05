package com.oop.project.repository;

import java.sql.Timestamp;
import java.util.List;

import com.oop.project.model.User;

public interface UserRepository {
    User findByUsername(String username);
    List<User> findAll();
    boolean insert(User user);
    boolean updateLastLogin(int userId, Timestamp timestamp);
}