package com.oop.project.ui;

import java.sql.Timestamp;

import com.oop.project.model.SystemSetting;
import com.oop.project.repository.SystemSettingRepository;
import com.oop.project.repository.impl.SystemSettingRepositoryImpl;

public class TestSystemSettingRepository {
    public static void main(String[] args) {

        SystemSettingRepository repo = new SystemSettingRepositoryImpl();

        System.out.println("=== SYSTEM SETTING: FIND ALL ===");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n=== SYSTEM SETTING: SAVE ===");
        String key = "test.key." + System.currentTimeMillis();
        SystemSetting s = new SystemSetting(
                key,
                "value",
                "test setting",
                new Timestamp(System.currentTimeMillis())
        );

        System.out.println("Saved: " + repo.save(s));

        System.out.println("\n=== SYSTEM SETTING: FIND BY KEY ===");
        System.out.println(repo.findByKey(key).orElse(null));
    }
}