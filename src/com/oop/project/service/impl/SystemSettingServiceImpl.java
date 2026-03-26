package com.oop.project.service.impl;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.SystemSetting;
import com.oop.project.repository.SystemSettingRepository;
import com.oop.project.service.SystemSettingService;

public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    public SystemSettingServiceImpl(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    @Override
    public List<SystemSetting> getAllSettings() {
        return systemSettingRepository.findAll();
    }

    @Override
    public Optional<SystemSetting> getSettingByKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            System.out.println("Setting key cannot be empty.");
            return Optional.empty();
        }
        return systemSettingRepository.findByKey(key);
    }

    @Override
    public boolean createSetting(SystemSetting setting) {
        if (!validateSetting(setting)) return false;
        if (systemSettingRepository.findByKey(setting.getSettingKey()).isPresent()) {
            System.out.println("Setting already exists.");
            return false;
        }
        return systemSettingRepository.save(setting);
    }

    @Override
    public boolean updateSetting(SystemSetting setting) {
        if (!validateSetting(setting)) return false;
        if (systemSettingRepository.findByKey(setting.getSettingKey()).isEmpty()) {
            System.out.println("Setting not found.");
            return false;
        }
        return systemSettingRepository.update(setting);
    }

    @Override
    public boolean deleteSetting(String key) {
        if (key == null || key.trim().isEmpty()) {
            System.out.println("Setting key cannot be empty.");
            return false;
        }
        if (systemSettingRepository.findByKey(key).isEmpty()) {
            System.out.println("Setting not found.");
            return false;
        }
        return systemSettingRepository.deleteByKey(key);
    }

    private boolean validateSetting(SystemSetting setting) {
        if (setting == null) {
            System.out.println("System setting cannot be null.");
            return false;
        }
        if (setting.getSettingKey() == null || setting.getSettingKey().trim().isEmpty()) {
            System.out.println("Setting key cannot be empty.");
            return false;
        }
        if (setting.getSettingValue() == null || setting.getSettingValue().trim().isEmpty()) {
            System.out.println("Setting value cannot be empty.");
            return false;
        }
        return true;
    }
}