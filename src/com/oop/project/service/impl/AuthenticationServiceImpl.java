package com.oop.project.service.impl;

import com.oop.project.model.AuditLog;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.AuditLogRepository;
import com.oop.project.repository.UserRepository;
import com.oop.project.repository.impl.AuditLogRepositoryImpl;
import com.oop.project.repository.impl.UserRepositoryImpl;
import com.oop.project.service.interfaces.AuthenticationService;
import com.oop.project.util.ValidationRules;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AuthenticationService.
 *
 * FR-0: Login and Authentication
 *
 * Responsibilities:
 * - Validate username + password against the database (FR-0.1, FR-0.2)
 * - Support ADMIN and STAFF roles (FR-0.3)
 * - Enforce Admin-only permissions (FR-0.4)
 * - Record every login and logout event in AuditLog (FR-0.5)
 *
 * @author Lan - Service Layer
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    // ── Dependencies (Repository layer) ──────────────────────────
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    // ── Constructor injection ─────────────────────────────────────
    public AuthenticationServiceImpl() {
        this.userRepository     = new UserRepositoryImpl();
        this.auditLogRepository = new AuditLogRepositoryImpl();
    }

    // Allow injection for testing purposes
    public AuthenticationServiceImpl(UserRepository userRepository,
                                     AuditLogRepository auditLogRepository) {
        this.userRepository     = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // FR-0.1 + FR-0.2: LOGIN
    // ─────────────────────────────────────────────────────────────

    /**
     * Authenticate a user with username and password.
     *
     * Steps:
     *  1. Validate that username and password are not blank
     *  2. Find user by username in the database
     *  3. Compare the stored password with the input password
     *  4. If success → update lastLogin + record "LOGIN" in AuditLog (FR-0.5)
     *  5. Return Optional<User> with the authenticated user, or Optional.empty()
     */
    @Override
    public Optional<User> login(String username, String password) {
        // Step 1: Basic null/empty check
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        if (password == null || password.trim().isEmpty()) return Optional.empty();

        // Step 2: Find user by username
        List<User> allUsers = userRepository.findAll();
        Optional<User> found = allUsers.stream()
            .filter(u -> u.getUserName().equalsIgnoreCase(username.trim()))
            .findFirst();

        if (found.isEmpty()) return Optional.empty();

        User user = found.get();

        // Step 3: Validate password
        if (!user.getUserPassword().equals(password)) return Optional.empty();

        // Step 4: Update lastLogin timestamp
        user.setLastLogin(Timestamp.from(Instant.now()));
        userRepository.update(user);

        // Step 4 (cont.): Record LOGIN event in AuditLog (FR-0.5)
        recordAuditLog(user, "LOGIN", "USER", String.valueOf(user.getUserId()));

        // Step 5: Return the authenticated user
        return Optional.of(user);
    }

    // ─────────────────────────────────────────────────────────────
    // FR-0.5: LOGOUT
    // ─────────────────────────────────────────────────────────────

    /**
     * Log out the current user and record the event in AuditLog.
     */
    @Override
    public void logout(User user) {
        if (user == null) return;
        // Record LOGOUT event (FR-0.5)
        recordAuditLog(user, "LOGOUT", "USER", String.valueOf(user.getUserId()));
    }

    // ─────────────────────────────────────────────────────────────
    // FR-0.3 + FR-0.4: PERMISSION CHECK
    // ─────────────────────────────────────────────────────────────

    /**
     * Check if a user has permission to perform a specific action.
     *
     * Admin-only actions (from ValidationRules.ADMIN_ONLY_ACTIONS):
     *   "UPDATE_PRICE", "DELETE_ORDER", "DELETE_CUSTOMER",
     *   "CREATE_USER", "DELETE_COUPON"
     *
     * Regular user actions (from ValidationRules.USER_ACTIONS):
     *   "CREATE_ORDER", "VIEW_ORDERS", "UPDATE_CUSTOMER", "SEARCH_ITEMS"
     */
    @Override
    public boolean checkPermission(User user, String action) {
        if (user == null || action == null) return false;

        // ADMIN can do everything
        if (user.getUserRole() == UserRole.ADMIN) return true;

        // STAFF can only do non-admin actions
        boolean isAdminOnlyAction = Arrays.asList(ValidationRules.ADMIN_ONLY_ACTIONS)
                                          .contains(action.toUpperCase());
        return !isAdminOnlyAction;
    }

    /**
     * Check if a user has the required role.
     */
    @Override
    public boolean hasRole(User user, UserRole required) {
        if (user == null || required == null) return false;
        return user.getUserRole() == required;
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPER
    // ─────────────────────────────────────────────────────────────

    /**
     * Save an audit log entry for any action performed by a user.
     */
    private void recordAuditLog(User user, String action, String targetType, String targetId) {
        AuditLog log = new AuditLog(
            0,                              // logId auto-generated by DB
            user,
            action,
            targetType,
            targetId,
            Timestamp.from(Instant.now())
        );
        auditLogRepository.save(log);
    }
}