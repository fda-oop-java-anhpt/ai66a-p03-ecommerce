package com.oop.project.ui.frames;

import com.oop.project.exception.AuthenticationException;
import com.oop.project.model.User;
import com.oop.project.repository.AuditLogRepository;
import com.oop.project.repository.impl.UserRepositoryImpl;
import com.oop.project.service.interfaces.IAuthService;
import com.oop.project.service.impl.AuthServiceImpl;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * FR-0.1, FR-0.2: Login screen. Calls AuthService.login().
 */
public class LoginFrame extends JFrame {

    private final IAuthService authService;
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JButton        loginBtn;

    public LoginFrame() {
        this.authService = new AuthServiceImpl(new UserRepositoryImpl(), new AuditLogRepository());
        UITheme.installGlobalDefaults();
        buildUI();
    }

    private void buildUI() {
        setTitle("ShopFlow — Sign In");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        setContentPane(root);

        // Brand panel (left)
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(new Color(10, 14, 24));
        brand.setPreferredSize(new Dimension(280, 0));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.insets = new Insets(6, 0, 6, 0);

        JLabel ico  = new JLabel("🛒"); ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        JLabel name = new JLabel("ShopFlow"); name.setFont(new Font("Segoe UI", Font.BOLD, 24)); name.setForeground(UITheme.ACCENT);
        JLabel sub  = UITheme.label("Billing & Order System");

        g.gridy=0; brand.add(ico,g);
        g.gridy=1; brand.add(name,g);
        g.gridy=2; brand.add(sub,g);
        root.add(brand, BorderLayout.WEST);

        // Form panel (right)
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(46,46,46,46));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5,0,5,0);
        c.gridx = 0; c.weightx = 1.0;

        c.gridy=0; form.add(UITheme.title("Welcome back"), c);
        c.gridy=1; form.add(UITheme.label("Sign in to your account"), c);
        c.gridy=2; form.add(Box.createRigidArea(new Dimension(0,8)), c);

        c.gridy=3; form.add(UITheme.label("Username"), c);
        usernameField = UITheme.styledTextField();
        usernameField.setPreferredSize(new Dimension(280, 38));
        c.gridy=4; form.add(usernameField, c);

        c.gridy=5; form.add(UITheme.label("Password"), c);
        passwordField = UITheme.styledPasswordField();
        passwordField.setPreferredSize(new Dimension(280, 38));
        c.gridy=6; form.add(passwordField, c);

        c.gridy=7; form.add(Box.createRigidArea(new Dimension(0,6)), c);

        loginBtn = UITheme.primaryButton("Sign In");
        loginBtn.setPreferredSize(new Dimension(280, 42));
        c.gridy=8; form.add(loginBtn, c);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.DANGER);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy=9; form.add(errorLabel, c);

        root.add(form, BorderLayout.CENTER);

        ActionListener doLogin = e -> attemptLogin();
        loginBtn.addActionListener(doLogin);
        passwordField.addActionListener(doLogin);
        usernameField.addActionListener(doLogin);

        pack();
        setMinimumSize(new Dimension(560, 340));
        setLocationRelativeTo(null);
    }

    private void attemptLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Username and password are required."); return;
        }
        loginBtn.setEnabled(false); loginBtn.setText("Signing in…");

        SwingWorker<User, Void> w = new SwingWorker<>() {
            protected User doInBackground() { return authService.login(user, pass); }
            protected void done() {
                try {
                    User u = get();
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainFrame(u, authService).setVisible(true));
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    errorLabel.setText(cause instanceof AuthenticationException
                        ? cause.getMessage() : "Login error: " + cause.getMessage());
                    passwordField.setText("");
                    loginBtn.setEnabled(true); loginBtn.setText("Sign In  →");
                }
            }
        };
        w.execute();
    }
}