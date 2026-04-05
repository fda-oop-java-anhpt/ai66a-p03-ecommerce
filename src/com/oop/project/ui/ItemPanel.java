package com.oop.project.ui;

import com.oop.project.exception.DuplicateException;
import com.oop.project.repository.SystemSettingRepository;
import com.oop.project.model.Item;
import com.oop.project.service.ItemService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Item Catalog tab — FR-2.
 * Uses ItemService: getAllItems(), addItem(item, user),
 * updateItem(item, user), deleteItem(sku).
 * Price editing restricted to ADMIN (FR-2.4).
 */
public class ItemPanel extends JPanel {

    private final MainFrame  mf;
    private final ItemService svc;

    private DefaultTableModel model;
    private JTable            table;
    private JTextField        searchField;

    private int lowStockLimit = 5;

    private static final String[] COLS = {"SKU", "Name", "Category", "Price ($)", "Stock"};

    public ItemPanel(MainFrame mf) {
        this.mf  = mf;
        this.svc = mf.itemService;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildTop(),    BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
        refresh();
    }

    // ── Top ───────────────────────────────────────────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        p.add(UITheme.title("Item Catalog"), BorderLayout.WEST);

        searchField = UITheme.styledTextField();
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.addActionListener(e -> doSearch());
        JButton sBtn  = UITheme.primaryButton("Search");
        JButton allBtn = UITheme.ghostButton("Show All");
        sBtn  .addActionListener(e -> doSearch());
        allBtn.addActionListener(e -> refresh());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UITheme.label("Search:"));
        right.add(searchField);
        right.add(sBtn);
        right.add(allBtn);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        model = TableRenderer.model(COLS);
        table = new JTable(model);
        TableRenderer.applyAll(table);
        table.getColumnModel().getColumn(3).setCellRenderer(TableRenderer.currency());
        // Fix 6: Red stock renderer for items at or below LOW_STOCK_LIMIT
        table.getColumnModel().getColumn(4).setCellRenderer(stockRenderer());
        TableRenderer.widths(table, 120, 240, 140, 110, 80);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openEditDialog();
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG_DARK);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel hint = UITheme.label("Double-click a row to edit  |  Price editing: Admin only");
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

        JButton addBtn     = UITheme.primaryButton("+ Add Item");
        JButton editBtn    = UITheme.ghostButton("Edit");
        JButton deleteBtn  = UITheme.dangerButton("Delete");
        JButton stockBtn   = UITheme.ghostButton("Add Stock");
        JButton refreshBtn = UITheme.ghostButton("Refresh");

        addBtn    .addActionListener(e -> openAddDialog());
        editBtn   .addActionListener(e -> openEditDialog());
        deleteBtn .addActionListener(e -> deleteSelected());
        stockBtn  .addActionListener(e -> addStock());
        refreshBtn.addActionListener(e -> refresh());

        p.add(addBtn); p.add(editBtn); p.add(deleteBtn);
        p.add(Box.createHorizontalStrut(12));
        p.add(stockBtn); p.add(refreshBtn);

        if (!mf.isAdmin()) {
            JLabel note = UITheme.label("  ⚠  Price editing restricted to Admin.");
            note.setForeground(UITheme.WARNING);
            note.setFont(UITheme.FONT_SMALL);
            p.add(note);
        }
        return p;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void openAddDialog() {
        ItemDialog dlg = new ItemDialog(mf, null, mf.isAdmin());
        dlg.setVisible(true);
        Item result = dlg.getResult();
        if (result == null) return;
        try {
            svc.addItem(result, mf.getCurrentUser());
            refresh();
            UITheme.showSuccess(this, "Item \"" + result.getItemName() + "\" added.");
        } catch (DuplicateException ex) {
            UITheme.showError(this, "Duplicate SKU: " + ex.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void openEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { UITheme.showError(this, "Select an item first."); return; }
        String sku = (String) model.getValueAt(row, 0);

        // Build item from table row
        Item existing = new Item();
        existing.setItemSku(sku);
        existing.setItemName((String) model.getValueAt(row, 1));
        existing.setCategory((String) model.getValueAt(row, 2));
        Object priceObj = model.getValueAt(row, 3);
        if (priceObj instanceof java.math.BigDecimal)
            existing.setUnitPrice((java.math.BigDecimal) priceObj);
        int stock = model.getValueAt(row, 4) instanceof Number
                ? ((Number) model.getValueAt(row, 4)).intValue() : 0;
        existing.setStockQuantity(stock);

        ItemDialog dlg = new ItemDialog(mf, existing, mf.isAdmin());
        dlg.setVisible(true);
        Item result = dlg.getResult();
        if (result == null) return;
        try {
            svc.updateItem(result, mf.getCurrentUser());
            refresh();
            UITheme.showSuccess(this, "Item updated.");
        } catch (SecurityException ex) {
            UITheme.showError(this, "Permission denied: " + ex.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { UITheme.showError(this, "Select an item to delete."); return; }
        String sku  = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        if (!UITheme.confirm(this, "Delete \"" + name + "\" (" + sku + ")?", "Confirm Delete")) return;
        try {
            svc.deleteItem(sku);
            refresh();
            UITheme.showSuccess(this, "Item deleted.");
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    private void addStock() {
        int row = table.getSelectedRow();
        if (row < 0) { UITheme.showError(this, "Select an item first."); return; }
        String sku  = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        String input = JOptionPane.showInputDialog(this,
                "Add stock quantity for \"" + name + "\":", "Add Stock", JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            int qty = Integer.parseInt(input.trim());
            svc.addStock(sku, qty, mf.getCurrentUser());
            refresh();
            UITheme.showSuccess(this, "Stock updated.");
        } catch (NumberFormatException ex) {
            UITheme.showError(this, "Please enter a valid integer.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    // Stock cell renderer: red if qty <= LOW_STOCK_LIMIT
    private javax.swing.table.TableCellRenderer stockRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                javax.swing.JLabel l = (javax.swing.JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setBorder(new javax.swing.border.EmptyBorder(0, 12, 0, 12));
                if (!sel) {
                    int qty = v instanceof Number ? ((Number) v).intValue() : 0;
                    int LOW = loadLowStockLimit(); // ← gọi ở đây thay vì bên ngoài
                    boolean low = qty <= LOW;
                    l.setForeground(low ? UITheme.DANGER : UITheme.ACCENT);
                    l.setBackground(r % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT);
                    l.setFont(low ? UITheme.FONT_BADGE : UITheme.FONT_BODY);
                    l.setToolTipText(low ? "⚠ Low stock!" : null);
                }
                return l;
            }
        };
    }

    private int loadLowStockLimit() {
        try {
            SystemSettingRepository r = new SystemSettingRepository();
            com.oop.project.model.SystemSetting s = r.findByKey("LOW_STOCK_LIMIT");
            if (s != null) return Integer.parseInt(s.getSettingValue());
        } catch (Exception ignored) {}
        return 5; // default
    }

    private void doSearch() {
        // ItemService has no search — filter table in memory
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) { refresh(); return; }
        model.setRowCount(0);
        try {
            for (Item i : svc.getAllItems()) {
                if (i.getItemName().toLowerCase().contains(kw)
                        || i.getItemSku().toLowerCase().contains(kw)
                        || (i.getCategory() != null && i.getCategory().toLowerCase().contains(kw))) {
                    model.addRow(new Object[]{
                        i.getItemSku(), i.getItemName(), i.getCategory(),
                        i.getUnitPrice(), i.getStockQuantity()});
                }
            }
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    public void refresh() {
        lowStockLimit = loadLowStockLimit(); // reload từ DB mỗi lần refresh
        try { 
            populate(svc.getAllItems());
        }
        catch (Exception ex) { 
            UITheme.showError(this, "Failed to load items: " + ex.getMessage()); 
        }
    }

    private void populate(List<Item> list) {
        model.setRowCount(0);
        for (Item i : list) {
            model.addRow(new Object[]{
                i.getItemSku(), i.getItemName(), i.getCategory(),
                i.getUnitPrice(), i.getStockQuantity()
            });
        }
    }
}