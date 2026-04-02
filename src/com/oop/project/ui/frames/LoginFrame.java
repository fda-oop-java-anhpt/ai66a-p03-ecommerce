package com.oop.project.ui.frames;

import com.oop.project.service.interfaces.AuthenticationService;
import com.oop.project.model.User;
import com.oop.project.ui.utils.UITheme;
import com.oop.project.ui.utils.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Login window — FR-0.
 * Validates credentials via AuthenticationService and opens MainFrame on success.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService;

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JButton        loginButton;
    private JLabel         statusLabel;

    public LoginFrame(AuthenticationService authService) {
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setTitle("E-Commerce System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(false);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        setContentPane(root);

        // ── Left brand panel ─────────────────────────────────────────────────
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(new Color(10, 14, 24));
        brand.setPreferredSize(new Dimension(280, 0));

        JLabel logo = new JLabel("🛒");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        JLabel name = new JLabel("ShopFlow");
        name.setFont(new Font("Segoe UI", Font.BOLD, 26));
        name.setForeground(UITheme.ACCENT);
        JLabel tagline = new JLabel("Billing & Orders");
        tagline.setFont(UITheme.FONT_SMALL);
        tagline.setForeground(UITheme.TEXT_MUTED);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridy = 0; brand.add(logo, gbc);
        gbc.gridy = 1; brand.add(name, gbc);
        gbc.gridy = 2; brand.add(tagline, gbc);

        root.add(brand, BorderLayout.WEST);

        // ── Right form panel ──────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);

        JLabel title = UITheme.title("Welcome back");
        JLabel sub = UITheme.label("Sign in to your account");

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; form.add(title, c);
        c.gridy = 1; form.add(sub, c);
        c.gridy = 2; form.add(Box.createRigidArea(new Dimension(0, 12)), c);

        c.gridy = 3; c.gridwidth = 1; c.weightx = 0;
        form.add(UITheme.label("Username"), c);
        c.gridy = 4; c.weightx = 1.0;
        usernameField = UITheme.styledTextField();
        usernameField.setPreferredSize(new Dimension(280, 38));
        form.add(usernameField, c);

        c.gridy = 5; c.weightx = 0;
        form.add(UITheme.label("Password"), c);
        c.gridy = 6; c.weightx = 1.0;
        passwordField = UITheme.styledPasswordField();
        passwordField.setPreferredSize(new Dimension(280, 38));
        form.add(passwordField, c);

        c.gridy = 7; form.add(Box.createRigidArea(new Dimension(0, 10)), c);

        loginButton = UITheme.primaryButton("Sign In  →");
        loginButton.setPreferredSize(new Dimension(280, 42));
        c.gridy = 8; form.add(loginButton, c);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 9; form.add(statusLabel, c);

        root.add(form, BorderLayout.CENTER);

        // ── Events ────────────────────────────────────────────────────────────
        ActionListener doLogin = e -> attemptLogin();
        loginButton.addActionListener(doLogin);
        passwordField.addActionListener(doLogin);
        usernameField.addActionListener(doLogin);

        pack();
        setMinimumSize(new Dimension(600, 380));
        setLocationRelativeTo(null);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in…");

        // Run on background thread to avoid freezing the UI
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override protected User doInBackground() {
                return authService.login(username, password).orElse(null);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            MainFrame main = new MainFrame(authService, user);
                            main.setVisible(true);
                        });
                    } else {
                        statusLabel.setText("Invalid username or password.");
                        passwordField.setText("");
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In  →");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Login failed: " + ex.getMessage());
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In  →");
                }
            }
        };
        worker.execute();
    }
}