package com.oop.project.service.impl;

import com.oop.project.exception.AuthenticationException;
import com.oop.project.model.AuditLog;
import com.oop.project.model.User;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.repository.interfaces.UserRepository;
import com.oop.project.repository.impl.AuditLogRepositoryImpl;
import com.oop.project.repository.impl.UserRepositoryImpl;
import com.oop.project.service.interfaces.IAuthService;
import com.oop.project.util.Validator;

import java.sql.Timestamp;

public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepo;
    private final AuditLogRepository auditLogRepo;

    public AuthServiceImpl() {
        this.userRepo = new UserRepositoryImpl();
        this.auditLogRepo = new AuditLogRepositoryImpl();
    }

    // For dependency injection (testing)
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

        // Audit log
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setActions("LOGIN");
        log.setTargetType("USER");
        log.setTargetId(String.valueOf(user.getUserId()));
        log.setCreatedDate(now);
        auditLogRepo.insert(log);

        return user;
    }

    @Override
    public void logout(User user) {
        if (user != null) {
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setActions("LOGOUT");
            log.setTargetType("USER");
            log.setTargetId(String.valueOf(user.getUserId()));
            log.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            auditLogRepo.insert(log);
        }
    }
}