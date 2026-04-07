package com.oop.project;

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
        } catch (Exception ignored) {}

        UITheme.installGlobalDefaults();

        SwingUtilities.invokeLater(() -> {
            AuthServiceImpl authService = new AuthServiceImpl();
            LoginFrame login = new LoginFrame(authService);
            login.setVisible(true);
        });
    }
}