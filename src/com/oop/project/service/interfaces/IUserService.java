package com.oop.project.service.interfaces;

import com.oop.project.model.User;

import java.util.List;

/**
 * Service interface for User / Staff management — Admin only.
 */
public interface IUserService {

    /** Return all users in the system. */
    List<User> getAllUsers();

    /**
     * Add a new staff user.
     * Validates username uniqueness and password length.
     *
     * @param user User object (plain-text password — will be hashed by repo)
     * @throws IllegalArgumentException on validation failure
     */
    void addStaff(User user);

    /**
     * Delete a user account.
     *
     * @param userId     ID of the user to delete
     * @param adminId    ID of the admin performing the delete (cannot self-delete)
     * @throws IllegalArgumentException if trying to delete own account
     */
    void deleteUser(int userId, int adminId);
}
