package com.oop.project.model;
import java.sql.Timestamp;
import java.util.Objects;
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
    public Timestamp getLastLogin(){
        return lastLogin;
    }
    public void setLastLogin(){
        this.lastLogin = lastLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;       
        User user = (User) o;       
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        // In ra Role để dễ debug phân quyền
        return "User{id=" + userId + ", username='" + userName + "', role=" + userRole + "}";
    }
}
    
