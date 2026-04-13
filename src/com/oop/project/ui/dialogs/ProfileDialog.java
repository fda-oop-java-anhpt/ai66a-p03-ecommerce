package com.oop.project.ui.dialogs;

import com.oop.project.model.User;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Profile Dialog — shows current user's info (read-only).
 * Password is masked with asterisks.
 */
public class ProfileDialog extends JDialog {

    public ProfileDialog(Window owner, User user) {
        super(owner, "My Profile", Dialog.ModalityType.APPLICATION_MODAL);
        buildUI(owner, user);
    }

    private void buildUI(Window owner, User user) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_CARD);
        setContentPane(root);

        // ── Avatar header ──────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(10, 14, 24));
        header.setBorder(BorderFactory.createEmptyBorder(28, 0, 22, 0));

        // Big avatar circle
        JLabel avatar = new JLabel(avatarLetter(user.getUserName()), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean admin = user.getUserRole() != null && user.getUserRole().name().equals("ADMIN");
                g2.setColor(admin ? UITheme.ACCENT : UITheme.SUCCESS);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 36));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(80, 80));
        avatar.setMaximumSize(new Dimension(80, 80));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel(user.getUserName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        boolean admin = user.getUserRole() != null && user.getUserRole().name().equals("ADMIN");
        JLabel roleLbl = new JLabel(admin ? "● ADMIN" : "● STAFF");
        roleLbl.setFont(UITheme.FONT_BADGE);
        roleLbl.setForeground(admin ? UITheme.ACCENT : UITheme.SUCCESS);
        roleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(avatar);
        header.add(Box.createRigidArea(new Dimension(0, 10)));
        header.add(nameLbl);
        header.add(Box.createRigidArea(new Dimension(0, 4)));
        header.add(roleLbl);
        root.add(header, BorderLayout.NORTH);

        // ── Info rows ──────────────────────────────────────────────────────────
        JPanel info = new JPanel(new GridLayout(0, 1, 0, 0));
        info.setBackground(UITheme.BG_CARD);
        info.setBorder(BorderFactory.createEmptyBorder(4, 32, 4, 32));

        info.add(infoRow("Username",     user.getUserName()));
        info.add(sep());
        info.add(infoRow("Password",     maskPassword(user.getUserPassword())));
        info.add(sep());
        info.add(infoRow("Role",         user.getUserRole() != null ? user.getUserRole().name() : "—"));
        info.add(sep());
        info.add(infoRow("Created Date", user.getCreatedDate() != null ? user.getCreatedDate().toString() : "—"));
        info.add(sep());
        info.add(infoRow("Last Login",   user.getLastLogin()   != null ? user.getLastLogin()  .toString() : "Never"));

        root.add(info, BorderLayout.CENTER);

        // ── Close button ───────────────────────────────────────────────────────
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 14));
        bottom.setBackground(UITheme.BG_CARD);
        JButton closeBtn = UITheme.primaryButton("Close");
        closeBtn.setPreferredSize(new Dimension(120, 36));
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);
        root.add(bottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(closeBtn);
        getRootPane().registerKeyboardAction(e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setMinimumSize(new Dimension(380, 0));
        setLocationRelativeTo(owner);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JPanel infoRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JLabel lbl = UITheme.label(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setPreferredSize(new Dimension(110, 20));

        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BODY);
        val.setForeground(UITheme.TEXT_PRIMARY);

        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setForeground(UITheme.BORDER_COLOR);
        s.setBackground(UITheme.BORDER_COLOR);
        return s;
    }

    /** Returns first letter of username, uppercased. */
    private String avatarLetter(String username) {
        if (username == null || username.isEmpty()) return "?";
        return String.valueOf(username.charAt(0)).toUpperCase();
    }

    /** Masks all characters with '*' */
    private String maskPassword(String pwd) {
        if (pwd == null || pwd.isEmpty()) return "—";
        return "*".repeat(pwd.length());
    }
}
