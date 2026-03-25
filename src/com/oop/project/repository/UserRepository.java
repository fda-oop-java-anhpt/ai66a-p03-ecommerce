package com.oop.project.repository;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.User;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(int id);
    boolean save(User user);
    boolean update(User user);
    boolean deleteById(int id);
}