package com.oop.project.service.interfaces;

import com.oop.project.model.User;

public interface IAuthService {
    User login(String username, String password);
    void logout(User user);
}