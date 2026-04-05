package com.oop.project.model;

import java.sql.Timestamp;
import java.util.Objects;

public class SystemSetting {
    private String settingKey; // Ví dụ: "TAX_RATE"
    private String settingValue; // Ví dụ: "8.0"
    private String description;
    private Timestamp createdDate;

    public SystemSetting() {}

    public SystemSetting(String settingKey, String settingValue, String description, Timestamp createdDate) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.createdDate = createdDate;
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp createdDate) { this.createdDate = createdDate; }

    @Override
    public String toString() {
        return "SystemSetting{" + "key='" + settingKey + '\'' + ", value='" + settingValue + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemSetting that = (SystemSetting) o;
        return Objects.equals(settingKey, that.settingKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingKey);
    }
}