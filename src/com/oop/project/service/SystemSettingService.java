package com.oop.project.service;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.SystemSetting;

public interface SystemSettingService {
    List<SystemSetting> getAllSettings();
    Optional<SystemSetting> getSettingByKey(String key);
    boolean createSetting(SystemSetting setting);
    boolean updateSetting(SystemSetting setting);
    boolean deleteSetting(String key);
}