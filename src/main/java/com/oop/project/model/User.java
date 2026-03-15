package com.oop.project.model;
import java.sql.Timestamp;
public class User{
    private int userId;
    private String userName;
    private String userPassword;
    private UserRole userRole;
    private Timestamp createdDate;
    private Timestamp lastLogin;

    public User(int userId, String userName, String userPassword, UserRole userRole, Timestamp createdDate, Timestamp lastLogin){
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userRole = userRole;
        this.createdDate = createdDate;
        this.lastLogin = lastLogin;
    }
    public User(int userId, String userName, UserRole userRole, Timestamp createdDate, Timestamp lastLogin) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.createdDate = createdDate;
        this.lastLogin = lastLogin;
    }
    public int getUserId(){ 
        return userId;
    }
    public String getUserName(){
        return userName;
    }
    public void setUserName(String userName){
        if (userName != null && !userName.trim().isEmpty() && userName.length()<=200){
            this.userName = userName;
        } else{
            throw new IllegalArgumentException("Invalid UserName");
        }
    }
    public String getUserPassword(){
        return userPassword;
    }
    public void setUserPassword(String userPassword){
        if (userPassword != null && userPassword.length() >=6){
            this.userPassword = userPassword;
        }
    }

    public UserRole getUserRole(){
        return userRole;
    }
    public void setUserRole(UserRole userRole){
        this.userRole = userRole;
    }


    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }
    public Timestamp getlastlogin(Timestamp lastLogin){
        return lastLogin;
    }

    // --- CÁC HÀM BỔ TRỢ TUẦN 3 [cite: 61] ---

    @Override
    public String toString() {
        return "User{" + "id=" + userId + ", name='" + userName + '\'' + ", role=" + userRole + '}';
    }
}