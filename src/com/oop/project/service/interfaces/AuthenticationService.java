package com.oop.project.service.interfaces;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;

import java.util.Optional;

/**
 * FR-0: Login and Authentication
 *
 * Handles user authentication, session management, and role-based access control.
 * - login()         → FR-0.1, FR-0.2: validate username + password
 * - logout()        → FR-0.5: record logout event in audit log
 * - checkPermission() → FR-0.3, FR-0.4: enforce role-based access
 *
 * @author Lan - Service Layer
 */
public interface AuthenticationService {

    /**
     * Authenticate a user with username and password.
     *
     * FR-0.1: The system shall require a username and password at login.
     * FR-0.2: Credentials are validated against stored user data.
     *
     * @param username  the username entered by the user
     * @param password  the plain-text password entered by the user
     * @return Optional<User> containing the authenticated User if successful,
     *         or Optional.empty() if credentials are invalid
     */
    Optional<User> login(String username, String password);

    /**
     * Log out the current user and record the logout event.
     *
     * FR-0.5: The system shall record each login and logout event.
     *
     * @param user  the currently logged-in User object
     */
    void logout(User user);

    /**
     * Check whether a user has permission to perform a specific action.
     *
     * FR-0.3: The system shall support multiple roles (ADMIN, STAFF).
     * FR-0.4: Admin users shall be allowed to modify product prices and tax rules.
     *
     * Valid action strings (defined in ValidationRules.ADMIN_ONLY_ACTIONS):
     *   "UPDATE_PRICE", "DELETE_ORDER", "DELETE_CUSTOMER",
     *   "CREATE_USER", "DELETE_COUPON"
     *
     * @param user    the currently logged-in User
     * @param action  the action string to check
     * @return true if the user is allowed to perform the action, false otherwise
     */
    boolean checkPermission(User user, String action);

    /**
     * Check whether a user has the required role.
     *
     * @param user      the user to check
     * @param required  the minimum required role (e.g., UserRole.ADMIN)
     * @return true if the user's role matches or exceeds the required role
     */
    boolean hasRole(User user, UserRole required);
}
