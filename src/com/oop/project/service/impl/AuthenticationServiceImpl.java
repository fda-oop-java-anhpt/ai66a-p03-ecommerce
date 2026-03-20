package com.oop.project.service.impl;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.service.interfaces.AuthenticationService;
import com.oop.project.exception.AuthenticationException;
import com.oop.project.util.Validator;
import com.oop.project.util.ValidationRules;
// import com.oop.project.repository.UserRepository; // Uncomment when Repository layer ready
import java.util.*;

/**
 * Implementation of AuthenticationService interface.
 * Handles user authentication, authorization, and session management.
 * 
 * NOTE: This implementation uses stub data for Week 1-3.
 * In Week 4, integrate with actual UserRepository.
 * 
 * @author Service Team
 * @version 1.0
 */
public class AuthenticationServiceImpl implements AuthenticationService {
    
    // TODO Week 4: Inject UserRepository via constructor
    // private final UserRepository userRepository;
    
    // Temporary in-memory session tracking (Week 1-3 only)
    private final Set<Integer> loggedInUsers = new HashSet<>();
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username the username to authenticate
     * @param password the password to verify
     * @return authenticated User object if credentials are valid
     * @throws AuthenticationException if credentials are invalid or user not found
     */
    @Override
    public User login(String username, String password) throws AuthenticationException {
        try {
            // Validate inputs
            Validator.validateRequired(username, "Username");
            Validator.validateRequired(password, "Password");
            
            // TODO Week 4: Replace with actual repository call
            // User user = userRepository.findByUsername(username);
            
            // STUB: Simulate database lookup (Week 1-3 only)
            User user = findUserByUsername(username);
            
            if (user == null) {
                throw new AuthenticationException("Invalid username or password");
            }
            
            // Verify password
            if (!verifyPassword(password, user.getUserPassword())) {
                throw new AuthenticationException("Invalid username or password");
            }
            
            // Update last login timestamp
            // TODO Week 4: userRepository.updateLastLogin(user.getUserId());
            
            // Track session
            loggedInUsers.add(user.getUserId());
            
            return user;
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Login failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Logs out the current user.
     * 
     * @param userId the ID of the user logging out
     * @return true if logout successful
     */
    @Override
    public boolean logout(int userId) {
        try {
            // Remove from active sessions
            loggedInUsers.remove(userId);
            
            // TODO Week 4: Record logout event in audit_log
            // auditLogRepository.recordLogout(userId);
            
            return true;
        } catch (Exception e) {
            System.err.println("Logout failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a user with given role has permission to perform an action.
     * 
     * @param role the user's role (ADMIN or STAFF)
     * @param action the action to check permission for
     * @return true if user has permission
     */
    @Override
    public boolean checkPermission(UserRole role, String action) {
        if (role == null || action == null) {
            return false;
        }
        
        // ADMIN has all permissions
        if (role == UserRole.ADMIN) {
            return true;
        }
        
        // Check if action is admin-only
        for (String adminAction : ValidationRules.ADMIN_ONLY_ACTIONS) {
            if (adminAction.equalsIgnoreCase(action)) {
                return false; // STAFF cannot perform admin-only actions
            }
        }
        
        // Check if action is in user actions
        for (String userAction : ValidationRules.USER_ACTIONS) {
            if (userAction.equalsIgnoreCase(action)) {
                return true; // STAFF can perform user actions
            }
        }
        
        // Default deny
        return false;
    }
    
    /**
     * Changes user password with old password verification.
     * 
     * @param userId the user ID
     * @param oldPassword the current password for verification
     * @param newPassword the new password to set
     * @return true if password changed successfully
     * @throws AuthenticationException if old password is incorrect
     */
    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) 
            throws AuthenticationException {
        try {
            // Validate inputs
            Validator.validatePositiveId(userId, "User ID");
            Validator.validateRequired(oldPassword, "Old Password");
            Validator.validatePassword(newPassword);
            
            // TODO Week 4: Get user from repository
            // User user = userRepository.findById(userId);
            
            // STUB: Simulate user lookup
            User user = findUserById(userId);
            
            if (user == null) {
                throw new AuthenticationException("User not found");
            }
            
            // Verify old password
            if (!verifyPassword(oldPassword, user.getUserPassword())) {
                throw new AuthenticationException("Current password is incorrect");
            }
            
            // Ensure new password is different
            if (oldPassword.equals(newPassword)) {
                throw new AuthenticationException("New password must be different from current password");
            }
            
            // TODO Week 4: Hash and update password
            // String hashedPassword = passwordHasher.hash(newPassword);
            // userRepository.updatePassword(userId, hashedPassword);
            
            return true;
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Password change failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates if a user is currently logged in.
     * 
     * @param userId the user ID to check
     * @return true if user is logged in
     */
    @Override
    public boolean isLoggedIn(int userId) {
        return loggedInUsers.contains(userId);
    }
    
    // ========== HELPER METHODS (PRIVATE) ==========
    
    /**
     * Verifies if provided password matches stored password.
     * TODO Week 4: Implement proper password hashing with BCrypt or similar
     * 
     * @param plainPassword the password to verify
     * @param storedPassword the stored password hash
     * @return true if passwords match
     */
    private boolean verifyPassword(String plainPassword, String storedPassword) {
        // STUB: Simple equality check (Week 1-3 only)
        // TODO Week 4: Use BCrypt or similar: passwordHasher.verify(plainPassword, storedPassword)
        return plainPassword.equals(storedPassword);
    }
    
    /**
     * STUB METHOD - Simulates database lookup by username.
     * TODO Week 4: Remove this and use userRepository.findByUsername()
     */
    private User findUserByUsername(String username) {
        // Hardcoded test users for Week 1-3
        if ("admin".equals(username)) {
            return new User(1, "admin", "admin123", UserRole.ADMIN, 
                          new java.sql.Timestamp(System.currentTimeMillis()), 
                          new java.sql.Timestamp(System.currentTimeMillis()));
        } else if ("staff".equals(username)) {
            return new User(2, "staff", "staff123", UserRole.STAFF, 
                          new java.sql.Timestamp(System.currentTimeMillis()), 
                          new java.sql.Timestamp(System.currentTimeMillis()));
        }
        return null;
    }
    
    /**
     * STUB METHOD - Simulates database lookup by user ID.
     * TODO Week 4: Remove this and use userRepository.findById()
     */
    private User findUserById(int userId) {
        // Hardcoded test users for Week 1-3
        if (userId == 1) {
            return new User(1, "admin", "admin123", UserRole.ADMIN, 
                          new java.sql.Timestamp(System.currentTimeMillis()), 
                          new java.sql.Timestamp(System.currentTimeMillis()));
        } else if (userId == 2) {
            return new User(2, "staff", "staff123", UserRole.STAFF, 
                          new java.sql.Timestamp(System.currentTimeMillis()), 
                          new java.sql.Timestamp(System.currentTimeMillis()));
        }
        return null;
    }
}
