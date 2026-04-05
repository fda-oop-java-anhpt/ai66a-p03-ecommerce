package com.oop.project.ui;

import com.oop.project.model.SystemSetting;
import com.oop.project.repository.AuditLogRepository;
import com.oop.project.model.AuditLog;
import com.oop.project.repository.SystemSettingRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * System Settings panel — Admin only (FR-0.4).
 * Allows Admin to view and modify all system_settings rows.
 * TAX_RATE is highlighted and validated specially (must be 0–100).
 * Changes are persisted via SystemSettingRepository and logged via AuditLogRepository.
 */
public class SettingsPanel extends JPanel {

    private final MainFrame               mf;
    private final SystemSettingRepository repo    = new SystemSettingRepository();
    private final AuditLogRepository      auditRepo = new AuditLogRepository();

    // Known keys & descriptions (used for hints in the form)
    private static final String KEY_TAX        = "TAX_RATE";
    private static final String KEY_STORE      = "STORE_NAME";
    private static final String KEY_LOW_STOCK  = "LOW_STOCK_LIMIT";

    // Table
    private DefaultTableModel tableModel;
    private JTable            table;

    // Inline edit form (shown below table)
    private JLabel     editKeyLbl;
    private JLabel     editDescLbl;
    private JTextField editValueField;
    private JButton    saveBtn, cancelEditBtn;
    private JPanel     editPanel;

    private static final String[] COLS = {"Setting Key", "Current Value", "Description"};

    public SettingsPanel(MainFrame mf) {
        this.mf = mf;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildTop(),    BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildEdit(),   BorderLayout.SOUTH);
        refresh();
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));

        JPanel titleBlock = new JPanel(new BorderLayout(0, 4));
        titleBlock.setBackground(UITheme.BG_DARK);
        titleBlock.add(UITheme.title("System Settings"), BorderLayout.WEST);
        JLabel sub = UITheme.label("Admin-only — changes take effect immediately on the next order.");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.WARNING);
        titleBlock.add(sub, BorderLayout.SOUTH);
        p.add(titleBlock, BorderLayout.WEST);

        JButton refreshBtn = UITheme.ghostButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(refreshBtn);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Settings table ────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        tableModel = TableRenderer.model(COLS);
        table = new JTable(tableModel);
        TableRenderer.applyAll(table);
        TableRenderer.widths(table, 180, 200, 400);

        // Highlight TAX_RATE row with accent colour
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                l.setFont(UITheme.FONT_BADGE);
                if (!sel) {
                    boolean isTax = KEY_TAX.equals(v);
                    l.setForeground(isTax ? UITheme.ACCENT : UITheme.TEXT_PRIMARY);
                    l.setBackground(r % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT);
                }
                return l;
            }
        });
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!sel) {
                    // Check if this row is the TAX_RATE row
                    Object key = tableModel.getValueAt(r, 0);
                    l.setForeground(KEY_TAX.equals(key) ? UITheme.SUCCESS : UITheme.TEXT_PRIMARY);
                    l.setFont(KEY_TAX.equals(key) ? UITheme.FONT_HEADING : UITheme.FONT_BODY);
                    l.setBackground(r % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT);
                }
                return l;
            }
        });

        // Click → populate inline edit form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateEditForm();
        });
        // Double-click → focus the value field directly
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    populateEditForm();
                    editValueField.requestFocus();
                    editValueField.selectAll();
                }
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG_DARK);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel hint = UITheme.label("Click a row to edit  |  Double-click to edit immediately");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setBorder(BorderFactory.createEmptyBorder(2, 0, 6, 0));
        wrap.add(hint, BorderLayout.NORTH);
        wrap.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return wrap;
    }

    // ── Inline edit form ──────────────────────────────────────────────────────
    private JPanel buildEdit() {
        editPanel = new JPanel(new BorderLayout(16, 0));
        editPanel.setBackground(UITheme.BG_CARD);
        editPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        // Left: key name + description
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 4));
        info.setBackground(UITheme.BG_CARD);
        editKeyLbl  = new JLabel("Select a setting above to edit");
        editKeyLbl.setFont(UITheme.FONT_HEADING);
        editKeyLbl.setForeground(UITheme.TEXT_PRIMARY);
        editDescLbl = new JLabel(" ");
        editDescLbl.setFont(UITheme.FONT_SMALL);
        editDescLbl.setForeground(UITheme.TEXT_MUTED);
        info.add(editKeyLbl);
        info.add(editDescLbl);
        info.setPreferredSize(new Dimension(340, 0));

        // Centre: value field
        JPanel valueBlock = new JPanel(new BorderLayout(0, 4));
        valueBlock.setBackground(UITheme.BG_CARD);
        JLabel valueLbl = UITheme.label("New Value");
        valueLbl.setFont(UITheme.FONT_SMALL);
        editValueField = UITheme.styledTextField();
        editValueField.setEnabled(false);
        editValueField.addActionListener(e -> saveSetting()); // Enter key saves
        valueBlock.add(valueLbl,       BorderLayout.NORTH);
        valueBlock.add(editValueField, BorderLayout.CENTER);

        // Right: buttons
        saveBtn      = UITheme.primaryButton("Save Change");
        cancelEditBtn = UITheme.ghostButton("Cancel");
        saveBtn      .setEnabled(false);
        cancelEditBtn.setEnabled(false);
        saveBtn      .addActionListener(e -> saveSetting());
        cancelEditBtn.addActionListener(e -> clearEditForm());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(UITheme.BG_CARD);
        btnPanel.add(saveBtn);
        btnPanel.add(cancelEditBtn);

        JPanel rightSection = new JPanel(new BorderLayout(0, 4));
        rightSection.setBackground(UITheme.BG_CARD);
        rightSection.add(valueBlock, BorderLayout.CENTER);
        rightSection.add(btnPanel,   BorderLayout.SOUTH);

        editPanel.add(info,         BorderLayout.WEST);
        editPanel.add(rightSection, BorderLayout.CENTER);

        // Tax-rate specific hint (shown dynamically)
        return editPanel;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void populateEditForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String key   = (String) tableModel.getValueAt(row, 0);
        String value = (String) tableModel.getValueAt(row, 1);
        String desc  = (String) tableModel.getValueAt(row, 2);

        editKeyLbl .setText(key);
        editKeyLbl .setForeground(KEY_TAX.equals(key) ? UITheme.ACCENT : UITheme.TEXT_PRIMARY);
        editDescLbl.setText(desc != null ? desc : " ");

        editValueField.setText(value);
        editValueField.setEnabled(true);

        saveBtn      .setEnabled(true);
        cancelEditBtn.setEnabled(true);

        // Extra hint for TAX_RATE
        if (KEY_TAX.equals(key)) {
            editDescLbl.setText("Tax rate applied to all orders (%). Must be 0–100.  e.g. 8.00 = 8%");
            editDescLbl.setForeground(UITheme.WARNING);
        } else {
            editDescLbl.setForeground(UITheme.TEXT_MUTED);
        }
    }

    private void saveSetting() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String key      = (String) tableModel.getValueAt(row, 0);
        String newValue = editValueField.getText().trim();

        if (newValue.isEmpty()) {
            UITheme.showError(this, "Value cannot be empty."); return;
        }

        // ── Validate TAX_RATE specifically (FR-0.4) ────────────────────────
        if (KEY_TAX.equals(key)) {
            try {
                BigDecimal tax = new BigDecimal(newValue);
                if (tax.compareTo(BigDecimal.ZERO) < 0 || tax.compareTo(new BigDecimal("100")) > 0) {
                    UITheme.showError(this, "Tax rate must be between 0 and 100.\nExample: enter 8.00 for 8%."); return;
                }
                // Normalise to 2 decimal places for consistency with DB schema NUMERIC(4,2)
                newValue = tax.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
            } catch (NumberFormatException ex) {
                UITheme.showError(this, "Tax rate must be a valid number (e.g. 8.00)."); return;
            }
        }

        // ── Validate LOW_STOCK_LIMIT ────────────────────────────────────────
        if (KEY_LOW_STOCK.equals(key)) {
            try {
                int limit = Integer.parseInt(newValue);
                if (limit < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                UITheme.showError(this, "Low stock limit must be a non-negative integer."); return;
            }
        }

        // Confirm for TAX_RATE because it affects all future orders
        if (KEY_TAX.equals(key)) {
            if (!UITheme.confirm(this,
                    "Change tax rate to " + newValue + "%?\n\nThis will affect all future orders.",
                    "Confirm Tax Rate Change")) return;
        }

        boolean ok = repo.update(key, newValue);
        if (ok) {
            // Write audit log for the settings change
            AuditLog log = new AuditLog();
            log.setUser(mf.getCurrentUser());
            log.setActions("UPDATE_SETTING");
            log.setTargetType("SETTING");
            log.setTargetId(key + "=" + newValue);
            auditRepo.insert(log);

            refresh();
            clearEditForm();
            UITheme.showSuccess(this,
                KEY_TAX.equals(key)
                    ? "Tax rate updated to " + newValue + "%.  All new orders will use this rate."
                    : "Setting \"" + key + "\" updated to: " + newValue);
        } else {
            UITheme.showError(this, "Failed to save. Key \"" + key + "\" may not exist in the database.");
        }
    }

    private void clearEditForm() {
        table.clearSelection();
        editKeyLbl .setText("Select a setting above to edit");
        editKeyLbl .setForeground(UITheme.TEXT_PRIMARY);
        editDescLbl.setText(" ");
        editValueField.setText("");
        editValueField.setEnabled(false);
        saveBtn       .setEnabled(false);
        cancelEditBtn .setEnabled(false);
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    public void refresh() {
        tableModel.setRowCount(0);
        // Load all known keys
        for (String key : new String[]{KEY_TAX, KEY_STORE, KEY_LOW_STOCK}) {
            SystemSetting s = repo.findByKey(key);
            if (s != null) {
                tableModel.addRow(new Object[]{
                    s.getSettingKey(),
                    s.getSettingValue(),
                    s.getDescription() != null ? s.getDescription() : ""
                });
            }
        }
        clearEditForm();
    }
}