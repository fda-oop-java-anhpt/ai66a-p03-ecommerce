package com.oop.project.repository;

import com.oop.project.model.SystemSetting;

public interface SystemSettingRepository {
    SystemSetting findByKey(String key);
    boolean update(String key, String value);
}