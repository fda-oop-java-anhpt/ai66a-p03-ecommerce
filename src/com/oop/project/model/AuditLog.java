package com.oop.project.model;

import java.sql.Timestamp;

public class AuditLog {
    private int logId;
    private User user;
    private String actions;
    private String targetType;
    private String targetId;
    private Timestamp createdDate;

    public AuditLog(){

    }
    public AuditLog(int logId, User user, String actions, String targetType, String targetId, Timestamp createdDate){
        this.logId = logId;
        setUser(user);
        setActions(actions);
        setTargetType(targetType);
        setTargetId(targetId); 
        this.createdDate = createdDate;
    }
    public int getLogId(){ return logId;}
    public void setLogId(int logId){this.logId = logId;}
    public User getUser(){ return user;}
    public void setUser(User user){
        if (user == null){
            throw new IllegalArgumentException(" Audit Log must be associated with a User");
        }
        this.user = user;
    }
    public String getActions() {
        return actions; 
    }
    public void setActions(String actions) { 
        if (actions == null || actions.trim().isEmpty()){
            throw new IllegalArgumentException("Actions cannot be null or empty");
        }
        this.actions = actions; 
    }


    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { 
        if (targetType == null || targetType.trim().isEmpty()){
            throw new IllegalArgumentException("Target type cannot be null or empty");
        }
        this.targetType = targetType; 
    }

    public String getTargetId() {
        return targetId; 
    }
    public void setTargetId(String targetId) { 
        if (targetId == null || targetId.trim().isEmpty()){
            throw new IllegalArgumentException("Target ID cannot be null or empty");
        }
        this.targetId = targetId; 
    }

    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp createdDate) { this.createdDate = createdDate; }

    @Override
    public String toString() {
        // Tối ưu hiển thị: Lấy tên người dùng ra in luôn
        String username = (user != null) ? user.getUserName() : "Unknown";
        return "AuditLog{id=" + logId + ", user=" + username + ", action='" + actions + "', target='" + targetType + "-" + targetId + "'}";
    }
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return logId == auditLog.logId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(logId);
    }
}