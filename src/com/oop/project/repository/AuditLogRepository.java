package com.oop.project.repository;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.AuditLog;

public interface AuditLogRepository {
    List<AuditLog> findAll();
    Optional<AuditLog> findById(int logId);
    List<AuditLog> findByUserId(int userId);
    boolean save(AuditLog log);
    boolean deleteById(int logId);
}