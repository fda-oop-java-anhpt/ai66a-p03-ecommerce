package com.oop.project.repository;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.SystemSetting;

public interface SystemSettingRepository {
    List<SystemSetting> findAll();
    Optional<SystemSetting> findByKey(String key);
    boolean save(SystemSetting setting);
    boolean update(SystemSetting setting);
    boolean deleteByKey(String key);
}