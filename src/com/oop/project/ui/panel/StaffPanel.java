package com.oop.project.ui.panel;

import com.oop.project.model.User;
// import com.oop.project.model.UserRole;
import com.oop.project.service.interfaces.IUserService;
import com.oop.project.ui.dialogs.AddStaffDialog;
import com.oop.project.ui.frames.MainFrame;
import com.oop.project.ui.utils.TableRenderer;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Staff Management panel — Admin only (FR-Admin).
 * Displays all user accounts and allows adding new staff/admin.
 * The admin cannot delete their own account.
 */
public class StaffPanel extends JPanel {

    private final MainFrame mf;
    private final IUserService svc;

    private DefaultTableModel model;
    private JTable table;
    private JTextField searchField;

    private static final String[] COLS = { "ID", "Username", "Role", "Created Date", "Last Login" };

    public StaffPanel(MainFrame mf, IUserService svc) {
        this.mf = mf;
        this.svc = svc;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
        refresh();
    }

    // ── Top: title + search ───────────────────────────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));

        // Left: icon + title
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleWrap.setOpaque(false);
        JLabel iconLabel = new JLabel("👤");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel titleLabel = UITheme.title("Staff Management");
        JLabel adminBadge = new JLabel("  ADMIN ONLY");
        adminBadge.setFont(UITheme.FONT_BADGE);
        adminBadge.setForeground(UITheme.WARNING);
        adminBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.WARNING, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        titleWrap.add(iconLabel);
        titleWrap.add(titleLabel);
        titleWrap.add(adminBadge);
        p.add(titleWrap, BorderLayout.WEST);

        // Right: search
        searchField = UITheme.styledTextField();
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.addActionListener(e -> doSearch());

        JButton searchBtn = UITheme.primaryButton("Search");
        searchBtn.addActionListener(e -> doSearch());
        JButton allBtn = UITheme.ghostButton("Show All");
        allBtn.addActionListener(e -> refresh());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UITheme.label("Search:"));
        right.add(searchField);
        right.add(searchBtn);
        right.add(allBtn);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        model = TableRenderer.model(COLS);
        table = new JTable(model);
        TableRenderer.applyAll(table);
        TableRenderer.widths(table, 50, 200, 120, 180, 180);

        // Color-code role column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                String role = value != null ? value.toString() : "";
                lbl.setForeground(sel ? Color.WHITE
                        : "ADMIN".equals(role) ? UITheme.ACCENT : UITheme.SUCCESS);
                lbl.setFont(UITheme.FONT_BADGE);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(sel ? new Color(50, 70, 110) : UITheme.BG_CARD);
                lbl.setOpaque(true);
                return lbl;
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG_DARK);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel hint = UITheme.label("Select a row then use the buttons below to manage staff accounts.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setBorder(BorderFactory.createEmptyBorder(2, 0, 6, 0));
        wrap.add(hint, BorderLayout.NORTH);
        wrap.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return wrap;
    }

    // ── Bottom action bar ─────────────────────────────────────────────────────
    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton addBtn = UITheme.primaryButton("+ Add Account");
        JButton deleteBtn = UITheme.dangerButton("Delete Account");
        JButton refreshBtn = UITheme.ghostButton("Refresh");

        addBtn.addActionListener(e -> openAddDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> refresh());

        // Summary label (right-aligned)
        JLabel summaryLabel = UITheme.label("");
        summaryLabel.setFont(UITheme.FONT_SMALL);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        right.setOpaque(false);
        right.add(summaryLabel);

        p.add(addBtn);
        p.add(deleteBtn);
        p.add(refreshBtn);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_DARK);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        bar.add(p, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void openAddDialog() {
        AddStaffDialog dlg = new AddStaffDialog(mf);
        dlg.setVisible(true);
        User newUser = dlg.getResult();
        if (newUser == null)
            return;
        try {
            svc.addStaff(newUser);
            refresh();
            UITheme.showSuccess(this,
                    "Account \"" + newUser.getUserName() + "\" created successfully.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UITheme.showError(this, "Select a user account to delete.");
            return;
        }
        int targetId = (int) model.getValueAt(row, 0);
        String targetName = (String) model.getValueAt(row, 1);
        String targetRole = (String) model.getValueAt(row, 2);

        if (targetId == mf.getCurrentUser().getUserId()) {
            UITheme.showError(this, "You cannot delete your own account.");
            return;
        }
        if ("ADMIN".equals(targetRole)) {
            if (!UITheme.confirm(this,
                    "⚠  You are about to delete an ADMIN account: \"" + targetName
                            + "\".\nThis action is irreversible. Continue?",
                    "Delete Admin Account"))
                return;
        } else {
            if (!UITheme.confirm(this,
                    "Delete staff account \"" + targetName + "\"?\nThis action cannot be undone.",
                    "Confirm Delete"))
                return;
        }

        try {
            svc.deleteUser(targetId, mf.getCurrentUser().getUserId());
            refresh();
            UITheme.showSuccess(this, "Account \"" + targetName + "\" deleted.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void doSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            refresh();
            return;
        }
        List<User> all = svc.getAllUsers();
        model.setRowCount(0);
        for (User u : all) {
            if (u.getUserName().toLowerCase().contains(kw)
                    || u.getUserRole().name().toLowerCase().contains(kw)) {
                addRow(u);
            }
        }
    }

    // ── Table data ────────────────────────────────────────────────────────────
    public void refresh() {
        try {
            List<User> users = svc.getAllUsers();
            model.setRowCount(0);
            for (User u : users) {
                addRow(u);
            }
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to load users: " + ex.getMessage());
        }
    }

    private void addRow(User u) {
        model.addRow(new Object[] {
                u.getUserId(),
                u.getUserName(),
                u.getUserRole() != null ? u.getUserRole().name() : "",
                u.getCreatedDate() != null ? u.getCreatedDate().toString() : "—",
                u.getLastLogin() != null ? u.getLastLogin().toString() : "—"
        });
    }
}
