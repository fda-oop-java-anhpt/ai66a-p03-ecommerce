package com.oop.project.service;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.AuditLog;

public interface AuditLogService {
    List<AuditLog> getAllLogs();
    Optional<AuditLog> getLogById(int logId);
    List<AuditLog> getLogsByUserId(int userId);
    boolean createLog(AuditLog log);
    boolean deleteLog(int logId);
}