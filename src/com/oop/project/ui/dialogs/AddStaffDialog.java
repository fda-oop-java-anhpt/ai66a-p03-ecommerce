package com.oop.project.ui.dialogs;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Modal dialog for adding a new Staff account — Admin only.
 * Returns the new User object on success, or null if cancelled.
 */
public class AddStaffDialog extends JDialog {

    private User result = null;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JComboBox<String> roleCombo;

    public AddStaffDialog(Window owner) {
        super(owner, "Add New Staff Account", ModalityType.APPLICATION_MODAL);
        buildUI();
    }

    private void buildUI() {
        setResizable(false);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(28, 34, 24, 34));
        setContentPane(root);

        // Title
        JLabel titleLabel = UITheme.heading("New Staff Account");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        root.add(titleLabel, BorderLayout.NORTH);

        // Sub-label: role note
        JLabel note = UITheme.label("Fields marked * are required. Password must be at least 6 characters.");
        note.setFont(UITheme.FONT_SMALL);

        // Fields
        usernameField = UITheme.styledTextField();
        passwordField = UITheme.styledPasswordField();
        confirmField  = UITheme.styledPasswordField();
        roleCombo     = UITheme.styledComboBox(new String[]{"STAFF", "ADMIN"});

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 12));
        fields.setBackground(UITheme.BG_CARD);
        fields.add(UITheme.labeledField("Username *", usernameField));
        fields.add(UITheme.labeledField("Password * (min 6 chars)", passwordField));
        fields.add(UITheme.labeledField("Confirm Password *", confirmField));
        fields.add(UITheme.labeledField("Role *", roleCombo));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(UITheme.BG_CARD);
        center.add(note, BorderLayout.NORTH);
        center.add(fields, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // Buttons
        JButton saveBtn   = UITheme.primaryButton("Create Account");
        JButton cancelBtn = UITheme.ghostButton("Cancel");
        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(UITheme.BG_CARD);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        root.add(btnPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(420, 0));
        setLocationRelativeTo(getOwner());
    }

    private void onSave() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());
        String roleStr  = (String) roleCombo.getSelectedItem();

        if (username.isEmpty()) {
            UITheme.showError(this, "Username is required.");
            usernameField.requestFocus();
            return;
        }
        if (username.length() < 3 || username.length() > 50) {
            UITheme.showError(this, "Username must be 3–50 characters.");
            usernameField.requestFocus();
            return;
        }
        if (password.length() < 6) {
            UITheme.showError(this, "Password must be at least 6 characters.");
            passwordField.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            UITheme.showError(this, "Passwords do not match.");
            confirmField.requestFocus();
            return;
        }

        // Build User object (password will be hashed in repository layer)
        result = new User();
        result.setUserName(username);
        result.setUserPassword(password);
        result.setUserRole(UserRole.valueOf(roleStr));
        dispose();
    }

    /** @return the created User, or null if the dialog was cancelled. */
    public User getResult() {
        return result;
    }
}
