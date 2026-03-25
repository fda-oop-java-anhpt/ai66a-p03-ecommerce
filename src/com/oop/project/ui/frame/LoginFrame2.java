package com.oop.project.ui.frame;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrameAdvanced - Phiên bản nâng cao với nhiều tính năng hơn
 * 
 * Additional Features:
 * - Forgot Password link
 * - Loading animation
 * - Input field icons
 * - Smooth animations
 * - Better error handling
 * - Keyboard shortcuts
 * - Failed login attempt counter
 * 
 * @author Thành viên 4 (UI Layer)
 */
public class LoginFrame2 extends JFrame {
    
    // UI Components
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnCancel;
    private JCheckBox chkShowPassword;
    private JLabel lblStatus;
    private JLabel lblForgotPassword;
    private JProgressBar progressBar;
    
    // State tracking
    private int failedLoginAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    
    public LoginFrame2() {
        initComponents();
        setupUI();
        addEventListeners();
        addKeyboardShortcuts();
    }
    
    private void initComponents() {
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        btnLogin = new JButton("Login");
        btnCancel = new JButton("Cancel");
        chkShowPassword = new JCheckBox("Show Password");
        lblStatus = new JLabel(" ");
        lblForgotPassword = new JLabel("<html><u>Forgot Password?</u></html>");
        progressBar = new JProgressBar();
        
        // Styling
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 11));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblForgotPassword.setFont(new Font("Arial", Font.PLAIN, 11));
        lblForgotPassword.setForeground(WARNING_COLOR);
        lblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(300, 8));
    }
    
    private void setupUI() {
        setTitle("E-Commerce System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Main container
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);
        
        // Center card panel
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(CARD_COLOR);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(30, 40, 30, 40),
            BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(20, 30, 20, 30)
            )
        ));
        
        // Add components to card
        cardPanel.add(createHeaderPanel());
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(createFormPanel());
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(createButtonPanel());
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(createFooterPanel());
        
        // Wrapper for centering
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BACKGROUND_COLOR);
        wrapper.add(cardPanel);
        
        container.add(wrapper, BorderLayout.CENTER);
        add(container);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Logo
        JLabel logoLabel = new JLabel("🛒");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title with gradient effect (simulated)
        JLabel titleLabel = new JLabel("E-Commerce System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sales & Billing Management");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Username field with icon
        JPanel usernamePanel = createInputPanel("Username", txtUsername);
        
        // Password field with icon
        JPanel passwordPanel = createInputPanel("Password", txtPassword);
        
        // Options panel
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setBackground(CARD_COLOR);
        
        chkShowPassword.setBackground(CARD_COLOR);
        chkShowPassword.setFont(new Font("Arial", Font.PLAIN, 11));
        
        optionsPanel.add(chkShowPassword, BorderLayout.WEST);
        optionsPanel.add(lblForgotPassword, BorderLayout.EAST);
        
        // Status and progress
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(CARD_COLOR);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.add(lblStatus);
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(progressBar);
        
        panel.add(usernamePanel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(passwordPanel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(optionsPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(statusPanel);
        
        return panel;
    }
    
    private JPanel createInputPanel(String labelText, JTextField textField) {
        // Horizontal layout: label on the left, field on the right
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        // Fixed width so labels align
        label.setPreferredSize(new Dimension(110, 25));
        label.setVerticalAlignment(SwingConstants.CENTER);
        
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(250, 35));
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Focus effect
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2, true),
                    new EmptyBorder(5, 10, 5, 10)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200), 1, true),
                    new EmptyBorder(5, 10, 5, 10)
                ));
            }
        });
        
        panel.add(label, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setLayout(new GridLayout(1, 2, 10, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // Login button
        btnLogin.setBackground(PRIMARY_COLOR);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Cancel button
        btnCancel.setBackground(new Color(240, 240, 240));
        btnCancel.setForeground(Color.DARK_GRAY);
        btnCancel.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(btnLogin);
        panel.add(btnCancel);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        versionLabel.setForeground(Color.LIGHT_GRAY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel copyrightLabel = new JLabel("© 2026 E-Commerce Team");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        copyrightLabel.setForeground(Color.LIGHT_GRAY);
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(Box.createVerticalStrut(10));
        panel.add(versionLabel);
        panel.add(copyrightLabel);
        
        return panel;
    }
    
    private void addEventListeners() {
        // Login button
        btnLogin.addActionListener(e -> handleLogin());
        
        // Cancel button
        btnCancel.addActionListener(e -> handleCancel());
        
        // Show/Hide password
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        
        // Forgot password link
        lblForgotPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleForgotPassword();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                lblForgotPassword.setForeground(PRIMARY_COLOR.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                lblForgotPassword.setForeground(PRIMARY_COLOR);
            }
        });
        
        // Enter key listener
        txtPassword.addActionListener(e -> handleLogin());
        
        // Button hover effects
        addButtonHoverEffect(btnLogin, PRIMARY_COLOR, PRIMARY_COLOR.darker());
        addButtonHoverEffect(btnCancel, new Color(240, 240, 240), Color.LIGHT_GRAY);
        
        // Clear status on typing
        KeyAdapter clearStatusListener = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                clearStatus();
            }
        };
        txtUsername.addKeyListener(clearStatusListener);
        txtPassword.addKeyListener(clearStatusListener);
    }
    
    private void addKeyboardShortcuts() {
        // ESC to close
        getRootPane().registerKeyboardAction(
            e -> handleCancel(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Ctrl+L to focus username
        getRootPane().registerKeyboardAction(
            e -> txtUsername.requestFocus(),
            KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    private void addButtonHoverEffect(JButton button, Color normal, Color hover) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normal);
            }
        });
    }
    
    private void handleLogin() {
        clearStatus();
        
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Validation
        if (username.isEmpty()) {
            shakeComponent(txtUsername);
            showError("Username is required");
            txtUsername.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            shakeComponent(txtPassword);
            showError("Password is required");
            txtPassword.requestFocus();
            return;
        }
        
        // Check max attempts
        if (failedLoginAttempts >= MAX_ATTEMPTS) {
            showError("Too many failed attempts. Please try again later.");
            setButtonsEnabled(false);
            return;
        }
        
        // Show loading
        setButtonsEnabled(false);
        progressBar.setVisible(true);
        showStatus("Authenticating...", Color.BLUE);
        
        // Simulate login (replace with real service call in Week 4)
        simulateLogin(username, password);
    }
    
    private void simulateLogin(String username, String password) {
        // Simulate network delay
        Timer timer = new Timer(1500, e -> {
            progressBar.setVisible(false);
            
            // Test credentials
            boolean success = false;
            String role = "";
            
            if (username.equals("admin") && password.equals("admin123")) {
                success = true;
                role = "ADMIN";
            } else if (username.equals("staff") && password.equals("staff123")) {
                success = true;
                role = "STAFF";
            }
            
            if (success) {
                failedLoginAttempts = 0;
                showSuccess("✓ Login successful! Welcome " + username);
                
                // Fade out effect (use final copy of role to satisfy lambda capture rules)
                final String finalRole = role;
                Timer fadeTimer = new Timer(800, evt -> {
                    openMainFrame(username, finalRole);
                });
                fadeTimer.setRepeats(false);
                fadeTimer.start();
                
            } else {
                failedLoginAttempts++;
                int remaining = MAX_ATTEMPTS - failedLoginAttempts;
                
                shakeComponent(this);
                showError("✗ Invalid credentials. " + remaining + " attempts remaining.");
                setButtonsEnabled(true);
                txtPassword.setText("");
                txtPassword.requestFocus();
                
                if (failedLoginAttempts >= MAX_ATTEMPTS) {
                    showError("Account locked. Contact administrator.");
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void handleForgotPassword() {
        String email = JOptionPane.showInputDialog(
            this,
            "Enter your registered email address:",
            "Forgot Password",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (email != null && !email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Password reset link has been sent to:\n" + email +
                "\n\nPlease check your email inbox.",
                "Reset Link Sent",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    private void handleCancel() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    private void openMainFrame(String username, String role) {
        // TODO: Replace with actual MainFrame in Week 5
        JOptionPane.showMessageDialog(
            this,
            "Login Successful!\n\n" +
            "User: " + username + "\n" +
            "Role: " + role + "\n\n" +
            "MainFrame will be implemented in Week 5",
            "Welcome",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Reset for testing
        resetForm();
        setButtonsEnabled(true);
    }
    
    private void shakeComponent(Component component) {
        Point original = component.getLocation();
        Timer timer = new Timer(50, null);
        final int[] count = {0};
        
        timer.addActionListener(e -> {
            if (count[0] < 10) {
                int offset = (count[0] % 2 == 0) ? 5 : -5;
                component.setLocation(original.x + offset, original.y);
                count[0]++;
            } else {
                component.setLocation(original);
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }
    
    private void showError(String message) {
        lblStatus.setText(message);
        lblStatus.setForeground(ERROR_COLOR);
    }
    
    private void showSuccess(String message) {
        lblStatus.setText(message);
        lblStatus.setForeground(SUCCESS_COLOR);
    }
    
    private void showStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
    }
    
    private void clearStatus() {
        lblStatus.setText(" ");
    }
    
    private void setButtonsEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
    }
    
    private void resetForm() {
        txtUsername.setText("");
        txtPassword.setText("");
        chkShowPassword.setSelected(false);
        clearStatus();
        progressBar.setVisible(false);
        failedLoginAttempts = 0;
        txtUsername.requestFocus();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame2 frame = new LoginFrame2();
            frame.setVisible(true);
            
            System.out.println("=== LoginFrame2 (Advanced) ===");
            System.out.println("Test Credentials:");
            System.out.println("- Admin: username='admin', password='admin123'");
            System.out.println("- Staff: username='staff', password='staff123'");
            System.out.println("\nFeatures:");
            System.out.println("- Show/Hide password");
            System.out.println("- Forgot password link");
            System.out.println("- Failed login attempt tracking (max 5)");
            System.out.println("- Shake animation on error");
            System.out.println("- Loading indicator");
            System.out.println("- Keyboard shortcuts (ESC, Ctrl+L)");
        });
    }
}