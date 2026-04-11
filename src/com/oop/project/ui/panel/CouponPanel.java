package com.oop.project.ui.panel;

import com.oop.project.model.Coupon;
import com.oop.project.model.DiscountType;
import com.oop.project.repository.interfaces.CouponRepository;
import com.oop.project.service.interfaces.ICouponService;
import com.oop.project.repository.impl.CouponRepositoryImpl;
import com.oop.project.ui.dialogs.CouponDialog;
import com.oop.project.ui.frames.MainFrame;
import com.oop.project.ui.utils.TableRenderer;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * Coupon Management tab — FR-4.1, FR-4.2.
 * Admin: full CRUD. Staff: view only.
 * Uses CouponService for add/validate, CouponRepository for full CRUD.
 */
public class CouponPanel extends JPanel {

    private final MainFrame mf;
    private final ICouponService svc;
    private final CouponRepository repo = new CouponRepositoryImpl();

    private DefaultTableModel model;
    private JTable table;

    private static final String[] COLS = { "Code", "Type", "Value", "Min Order (VNĐ)", "Expiry", "Status" };

    public CouponPanel(MainFrame mf) {
        this.mf = mf;
        this.svc = mf.couponService;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
        refresh();
    }

    // ── Top ───────────────────────────────────────────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        p.add(UITheme.title("Coupons"), BorderLayout.WEST);

        if (!mf.isAdmin()) {
            JLabel note = UITheme.label("⚠  View only — Admin required to manage coupons.");
            note.setForeground(UITheme.WARNING);
            note.setFont(UITheme.FONT_SMALL);
            p.add(note, BorderLayout.EAST);
        }
        return p;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        model = TableRenderer.model(COLS);
        table = new JTable(model);
        TableRenderer.applyAll(table);
        TableRenderer.widths(table, 120, 100, 80, 110, 110, 100);

        // Status column — ACTIVE green / EXPIRED red
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setFont(UITheme.FONT_BADGE);
                l.setHorizontalAlignment(CENTER);
                if (!sel) {
                    boolean active = "ACTIVE".equals(v);
                    l.setForeground(active ? UITheme.STATUS_PAID : UITheme.STATUS_CANCELLED);
                    l.setBackground(r % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT);
                }
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                return l;
            }
        });

        // Double-click → edit (Admin only)
        if (mf.isAdmin()) {
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2)
                        editSelected();
                }
            });
        }

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG_DARK);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        if (mf.isAdmin()) {
            JLabel hint = UITheme.label("Double-click a row to edit");
            hint.setFont(UITheme.FONT_SMALL);
            hint.setBorder(BorderFactory.createEmptyBorder(2, 0, 6, 0));
            wrap.add(hint, BorderLayout.NORTH);
        }
        wrap.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return wrap;
    }

    // ── Bottom action bar ─────────────────────────────────────────────────────
    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton refreshBtn = UITheme.ghostButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        p.add(refreshBtn);

        if (mf.isAdmin()) {
            JButton addBtn = UITheme.primaryButton("+ New Coupon");
            JButton editBtn = UITheme.ghostButton("Edit");
            JButton toggleBtn = UITheme.ghostButton("Toggle Active");

            addBtn.addActionListener(e -> openAddDialog());
            editBtn.addActionListener(e -> editSelected());
            toggleBtn.addActionListener(e -> toggleActive());

            p.add(addBtn);
            p.add(editBtn);
            p.add(toggleBtn);
        }
        return p;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────
    private void openAddDialog() {
        CouponDialog dlg = new CouponDialog(mf, null);
        dlg.setVisible(true);
        Coupon result = dlg.getResult();
        if (result == null)
            return;
        try {
            svc.addCoupon(result);
            refresh();
            UITheme.showSuccess(this, "Coupon \"" + result.getCouponCode() + "\" added.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UITheme.showError(this, "Select a coupon first.");
            return;
        }
        String code = (String) model.getValueAt(row, 0);
        Coupon existing = repo.findByCode(code);
        if (existing == null) {
            UITheme.showError(this, "Coupon not found.");
            return;
        }

        CouponDialog dlg = new CouponDialog(mf, existing);
        dlg.setVisible(true);
        Coupon result = dlg.getResult();
        if (result == null)
            return;
        try {
            repo.update(result);
            refresh();
            UITheme.showSuccess(this, "Coupon updated.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void toggleActive() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UITheme.showError(this, "Select a coupon first.");
            return;
        }
        String code = (String) model.getValueAt(row, 0);
        Coupon c = repo.findByCode(code);
        if (c == null)
            return;

        if (!c.isActive() && c.getExpiryDate() != null
                && c.getExpiryDate().before(new Date(System.currentTimeMillis()))) {
            UITheme.showError(this, "Cannot activate an expired coupon.");
            return;
        }

        c.setActive(!c.isActive());
        repo.update(c);
        refresh();
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    public void refresh() {
        try {
            List<Coupon> list = repo.findAll();
            model.setRowCount(0);
            Date today = new Date(System.currentTimeMillis());
            for (Coupon c : list) {
                boolean expired = c.getExpiryDate() != null && c.getExpiryDate().before(today);
                String status = (!c.isActive() || expired) ? "EXPIRED" : "ACTIVE";
                String valStr = c.getDiscountType() == DiscountType.Percent
                        ? c.getDiscountValue().toPlainString() + "%"
                        : String.format("%,.0f VNĐ", c.getDiscountValue().doubleValue());
                model.addRow(new Object[] {
                        c.getCouponCode(), c.getDiscountType().name(),
                        valStr,
                        c.getMinOrderValue() != null ? c.getMinOrderValue() : BigDecimal.ZERO,
                        c.getExpiryDate(), status
                });
            }
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to load coupons: " + ex.getMessage());
        }
    }
}