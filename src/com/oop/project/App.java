package com.oop.project;

import com.oop.project.repository.impl.AuditLogRepositoryImpl;
import com.oop.project.repository.impl.UserRepositoryImpl;
import com.oop.project.service.impl.AuthServiceImpl;
import com.oop.project.ui.frames.LoginFrame;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;

/**
 * Application entry point.
 * Sets up global L&F and launches the LoginFrame.
 */
public class App {

    public static void main(String[] args) {
        // Apply system L&F first, then override with our dark theme
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        UITheme.installGlobalDefaults();

        SwingUtilities.invokeLater(() -> {
            new AuthServiceImpl(
                    new UserRepositoryImpl(),
                    new AuditLogRepositoryImpl());
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}