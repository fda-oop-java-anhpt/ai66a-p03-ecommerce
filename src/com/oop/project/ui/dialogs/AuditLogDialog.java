package com.oop.project.ui.dialogs;

import com.oop.project.repository.impl.AuditLogRepositoryImpl;
import com.oop.project.ui.utils.TableRenderer;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Audit Log — standalone JDialog, FR-4.4 + FR-0.5.
 * Opened from the avatar dropdown menu.
 * Shows all order and authentication events with colour-coded actions.
 */
public class AuditLogDialog extends JDialog {

    private final AuditLogRepositoryImpl auditRepo = new AuditLogRepositoryImpl();

    private DefaultTableModel model;
    private JTable            table;
    private JComboBox<String> actionFilter;
    private JTextField        searchField;

    private static final String[] COLS =
        {"Timestamp", "User", "Action", "Target Type", "Target ID"};

    public AuditLogDialog(Window owner) {
        super(owner, "Audit Log", Dialog.ModalityType.APPLICATION_MODAL);
        buildUI(owner);
        refresh();
    }

    private void buildUI(Window owner) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 580);
        setMinimumSize(new Dimension(700, 400));
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        setContentPane(root);

        root.add(buildTop(),    BorderLayout.NORTH);
        root.add(buildTable(),  BorderLayout.CENTER);

        // ESC closes
        getRootPane().registerKeyboardAction(e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ── Top: title + filter bar ───────────────────────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));

        JPanel titleBlock = new JPanel(new BorderLayout(0, 3));
        titleBlock.setBackground(UITheme.BG_DARK);
        titleBlock.add(UITheme.title("Audit Log"), BorderLayout.NORTH);
        JLabel sub = UITheme.label("All order and authentication events");
        sub.setFont(UITheme.FONT_SMALL);
        titleBlock.add(sub, BorderLayout.SOUTH);

        // Filter bar
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        filters.setOpaque(false);

        filters.add(UITheme.label("Action:"));
        actionFilter = UITheme.styledComboBox(new String[] {
                "All", "LOGIN", "LOGOUT",
                "CREATE_ORDER", "UPDATE_ORDER", "CANCEL_ORDER",
                "CREATE_ITEM", "UPDATE_ITEM", "DELETE_ITEM",
                "CREATE_CUSTOMER", "UPDATE_CUSTOMER", "DELETE_CUSTOMER",
                "ADD_STAFF", "DELETE_STAFF",
                "UPDATE_SETTING"
        });
        actionFilter.addActionListener(e -> applyFilter());
        filters.add(actionFilter);

        filters.add(UITheme.label("Search user:"));
        searchField = UITheme.styledTextField();
        searchField.setPreferredSize(new Dimension(160, 32));
        searchField.addActionListener(e -> applyFilter());
        filters.add(searchField);

        JButton searchBtn  = UITheme.primaryButton("Search");
        JButton clearBtn   = UITheme.ghostButton("Clear");
        // JButton refreshBtn = UITheme.ghostButton("⟳");
        searchBtn .addActionListener(e -> applyFilter());
        clearBtn  .addActionListener(e -> { searchField.setText(""); actionFilter.setSelectedIndex(0); refresh(); });
        // refreshBtn.addActionListener(e -> refresh());
        filters.add(searchBtn);
        filters.add(clearBtn);
        // filters.add(refreshBtn);

        p.add(titleBlock, BorderLayout.WEST);
        p.add(filters,    BorderLayout.EAST);
        return p;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JPanel buildTable() {
        model = TableRenderer.model(COLS);
        table = new JTable(model);
        TableRenderer.applyAll(table);
        TableRenderer.widths(table, 175, 130, 160, 130, 140);

        // Colour-coded Action column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setFont(UITheme.FONT_BADGE);
                if (!sel) {
                    String s = v == null ? "" : v.toString();
                    Color col = switch (s) {
                        case "CREATE_ORDER", "CREATE_ITEM", "CREATE_CUSTOMER", "ADD_STAFF" -> UITheme.SUCCESS;
                        case "CANCEL_ORDER", "DELETE_ORDER", "DELETE_ITEM", "DELETE_CUSTOMER", "DELETE_STAFF" -> UITheme.DANGER;
                        case "LOGIN", "LOGOUT" -> UITheme.ACCENT;
                        case "UPDATE_SETTING", "UPDATE_ORDER", "UPDATE_ITEM", "UPDATE_CUSTOMER" -> UITheme.UPDATE;
                        default -> UITheme.TEXT_MUTED;
                    };
                    l.setForeground(col);
                    l.setBackground(r % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT);
                }
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                return l;
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 20));

        // Row count label at bottom
        JLabel countLbl = UITheme.label("0 records");
        countLbl.setFont(UITheme.FONT_SMALL);
        countLbl.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        // Update count when model changes
        model.addTableModelListener(e -> countLbl.setText(model.getRowCount() + " records"));

        p.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        p.add(countLbl, BorderLayout.SOUTH);
        return p;
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    public void refresh() {
        try {
            model.setRowCount(0);
            auditRepo.findAll().forEach(log -> model.addRow(new Object[]{
                log.getCreatedDate(),
                log.getUser() != null ? log.getUser().getUserName() : "—",
                log.getActions(),
                log.getTargetType(),
                log.getTargetId()
            }));
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to load audit log: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String action  = (String) actionFilter.getSelectedItem();
        String userKw  = searchField.getText().trim().toLowerCase();

        try {
            model.setRowCount(0);
            auditRepo.findAll().stream()
                .filter(log -> {
                    boolean matchAction = "All".equals(action)
                            || action.equals(log.getActions());
                    boolean matchUser = userKw.isEmpty()
                            || (log.getUser() != null
                                && log.getUser().getUserName().toLowerCase().contains(userKw));
                    return matchAction && matchUser;
                })
                .forEach(log -> model.addRow(new Object[]{
                    log.getCreatedDate(),
                    log.getUser() != null ? log.getUser().getUserName() : "—",
                    log.getActions(),
                    log.getTargetType(),
                    log.getTargetId()
                }));
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }
}
