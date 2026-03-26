package com.oop.project.service.impl;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.AuditLog;
import com.oop.project.repository.AuditLogRepository;
import com.oop.project.service.AuditLogService;

public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    @Override
    public Optional<AuditLog> getLogById(int logId) {
        if (logId <= 0) {
            System.out.println("Log ID must be greater than 0.");
            return Optional.empty();
        }
        return auditLogRepository.findById(logId);
    }

    @Override
    public List<AuditLog> getLogsByUserId(int userId) {
        if (userId <= 0) {
            System.out.println("User ID must be greater than 0.");
            return List.of();
        }
        return auditLogRepository.findByUserId(userId);
    }

    @Override
    public boolean createLog(AuditLog log) {
        if (!validateLog(log)) return false;
        return auditLogRepository.save(log);
    }

    @Override
    public boolean deleteLog(int logId) {
        if (logId <= 0) {
            System.out.println("Log ID must be greater than 0.");
            return false;
        }
        if (auditLogRepository.findById(logId).isEmpty()) {
            System.out.println("Audit log not found.");
            return false;
        }
        return auditLogRepository.deleteById(logId);
    }

    private boolean validateLog(AuditLog log) {
        if (log == null) {
            System.out.println("Audit log cannot be null.");
            return false;
        }
        if (log.getUser() == null) {
            System.out.println("Audit log user cannot be null.");
            return false;
        }
        if (log.getActions() == null || log.getActions().trim().isEmpty()) {
            System.out.println("Action cannot be empty.");
            return false;
        }
        if (log.getTargetType() == null || log.getTargetType().trim().isEmpty()) {
            System.out.println("Target type cannot be empty.");
            return false;
        }
        if (log.getTargetId() == null || log.getTargetId().trim().isEmpty()) {
            System.out.println("Target ID cannot be empty.");
            return false;
        }
        return true;
    }
}