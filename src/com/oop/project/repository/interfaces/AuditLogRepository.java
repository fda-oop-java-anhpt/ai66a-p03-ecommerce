package com.oop.project.repository.interfaces;

import com.oop.project.model.AuditLog;

import java.util.List;

public interface AuditLogRepository {
    boolean insert(AuditLog log);

    List<AuditLog> findAll();

    boolean deleteByUserId(int userId);
}