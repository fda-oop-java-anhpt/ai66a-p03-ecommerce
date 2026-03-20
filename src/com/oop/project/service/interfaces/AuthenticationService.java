package com.oop.project.service.interfaces;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.service.exception.AuthenticationException;

/**
 * Service interface for user authentication and authorization.
 * Handles login, logout, and role-based permission checks.
 * 
 * @author Service Team - Member 3
 * @version 1.0
 */
public interface AuthenticationService {
    
    /**
     * Authenticates a user with username and password.
     * Updates lastLogin timestamp on successful authentication.
     * 
     * @param username the username to authenticate
     * @param password the password to verify
     * @return authenticated User object if credentials are valid
     * @throws AuthenticationException if credentials are invalid or user not found
     */
    User login(String username, String password) throws AuthenticationException;
    
    /**
     * Logs out the current user and records logout event.
     * 
     * @param userId the ID of the user logging out
     * @return true if logout successful, false otherwise
     */
    boolean logout(int userId);
    
    /**
     * Checks if a user with given role has permission to perform an action.
     * 
     * @param role the user's role (ADMIN or STAFF)
     * @param action the action to check permission for (e.g., "UPDATE_PRICE", "DELETE_ORDER")
     * @return true if user has permission, false otherwise
     */
    boolean checkPermission(UserRole role, String action);
    
    /**
     * Changes user password with old password verification.
     * 
     * @param userId the user ID
     * @param oldPassword the current password for verification
     * @param newPassword the new password to set
     * @return true if password changed successfully
     * @throws AuthenticationException if old password is incorrect
     */
    boolean changePassword(int userId, String oldPassword, String newPassword) throws AuthenticationException;
    
    /**
     * Validates if a user is currently logged in.
     * 
     * @param userId the user ID to check
     * @return true if user is logged in, false otherwise
     */
    boolean isLoggedIn(int userId);
}
