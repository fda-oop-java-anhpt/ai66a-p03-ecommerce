package com.oop.project.service.impl;

import com.oop.project.exception.AuthenticationException;
import com.oop.project.model.AuditLog;
import com.oop.project.model.User;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.repository.interfaces.UserRepository;
import com.oop.project.service.interfaces.IAuthService;
import com.oop.project.util.Validator;

import java.sql.Timestamp;

public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepo;
    private final AuditLogRepository auditLogRepo;

    public AuthServiceImpl(UserRepository userRepo, AuditLogRepository auditLogRepo) {
        this.userRepo = userRepo;
        this.auditLogRepo = auditLogRepo;
    }

    @Override
    public User login(String username, String password) {
        if (Validator.checkEmpty(username) || Validator.checkEmpty(password)) {
            throw new AuthenticationException("Username and password must not be empty.");
        }

        User user = userRepo.findByUsername(username.trim());
        if (user == null) {
            throw new AuthenticationException("Username not found.");
        }

        if (!user.getUserPassword().equals(password)) {
            throw new AuthenticationException("Incorrect password.");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        userRepo.updateLastLogin(user.getUserId(), now);
        user.setLastLogin(now);

        logAudit(user, "LOGIN", "USER", String.valueOf(user.getUserId()));
        return user;
    }

    @Override
    public void logout(User user) {
        if (user != null) {
            logAudit(user, "LOGOUT", "USER", String.valueOf(user.getUserId()));
        }
    }

    private void logAudit(User user, String action, String targetType, String targetId) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setActions(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        auditLogRepo.insert(log);
    }
}