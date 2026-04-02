package com.oop.project.ui.panels;

import com.oop.project.model.Item;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.service.interfaces.ItemService;
import com.oop.project.ui.components.SearchBar;
import com.oop.project.ui.utils.DialogUtils;
import com.oop.project.ui.utils.TableUtils;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Item Catalog tab.
 * FR-2.1 CRUD, FR-2.2 fields, FR-2.3 duplicate SKU check, FR-2.4 admin-only price edit.
 */
public class ItemPanel extends JPanel {

    private final ItemService itemService;
    private final User        currentUser;
    private final boolean     isAdmin;

    private DefaultTableModel tableModel;
    private JTable            table;

    private JTextField skuField, nameField, priceField, categoryField;
    private JButton    addBtn, updateBtn, deleteBtn;

    private static final String[] COLUMNS = {"SKU", "Name", "Price", "Category"};

    public ItemPanel(ItemService itemService, User currentUser) {
        this.itemService  = itemService;
        this.currentUser  = currentUser;
        this.isAdmin      = currentUser.getUserRole() == UserRole.ADMIN;
        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildCenter(),   BorderLayout.CENTER);
        add(buildFormSide(), BorderLayout.EAST);
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        p.add(UITheme.title("Item Catalog"), BorderLayout.WEST);

        SearchBar search = new SearchBar("Search by name or SKU…", keyword -> {
            try {
                populateTable(itemService.searchItems(keyword));
            } catch (Exception ex) {
                DialogUtils.showError(this, ex.getMessage());
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(search);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Table section ─────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        tableModel = TableUtils.nonEditableModel(COLUMNS);
        table = new JTable(tableModel);
        TableUtils.applyDefaultRenderers(table);

        // Currency renderer for price column
        table.getColumnModel().getColumn(2)
             .setCellRenderer(TableUtils.currencyRenderer());
        TableUtils.setColumnWidths(table, 120, 220, 100, 160);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG_DARK);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 8));
        wrap.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        return wrap;
    }

    // ── Right form ────────────────────────────────────────────────────────────
    private JPanel buildFormSide() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(UITheme.BG_CARD);
        side.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        side.setPreferredSize(new Dimension(300, 0));

        JLabel heading = UITheme.heading("Item Details");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        skuField      = UITheme.styledTextField();
        nameField     = UITheme.styledTextField();
        priceField    = UITheme.styledTextField();
        categoryField = UITheme.styledTextField();

        // Staff cannot modify price (FR-2.4)
        if (!isAdmin) {
            priceField.setEditable(false);
            priceField.setForeground(UITheme.TEXT_MUTED);
            priceField.setToolTipText("Only Admins can modify prices.");
        }

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 8));
        fields.setBackground(UITheme.BG_CARD);
        fields.add(labeledField("SKU Code",  skuField));
        fields.add(labeledField("Item Name", nameField));
        fields.add(labeledField(isAdmin ? "Unit Price ($)" : "Unit Price (read-only)", priceField));
        fields.add(labeledField("Category",  categoryField));

        if (!isAdmin) {
            JLabel notice = UITheme.label("⚠ Price editing restricted to Admin.");
            notice.setForeground(UITheme.WARNING);
            notice.setFont(UITheme.FONT_SMALL);
            fields.add(notice);
        }

        addBtn    = UITheme.primaryButton("Add Item");
        updateBtn = UITheme.ghostButton("Update");
        deleteBtn = UITheme.dangerButton("Delete");

        addBtn.addActionListener(e -> addItem());
        updateBtn.addActionListener(e -> updateItem());
        deleteBtn.addActionListener(e -> deleteItem());

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.setBackground(UITheme.BG_CARD);
        buttons.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);

        JButton clearBtn = UITheme.ghostButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UITheme.BG_CARD);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        bottom.add(clearBtn);

        side.add(heading, BorderLayout.NORTH);
        side.add(fields,  BorderLayout.CENTER);
        side.add(buttons, BorderLayout.SOUTH);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UITheme.BG_CARD);
        outer.add(side, BorderLayout.CENTER);
        outer.add(bottom, BorderLayout.SOUTH);
        return outer;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────
    private void addItem() {
        try {
            Item item = buildItemFromForm(null);
            itemService.addItem(item);
            refreshTable();
            clearForm();
            DialogUtils.showSuccess(this, "Item added successfully.");
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void updateItem() {
        int row = table.getSelectedRow();
        if (row < 0) { DialogUtils.showError(this, "Select an item first."); return; }
        String sku = (String) tableModel.getValueAt(row, 0);
        try {
            Item item = buildItemFromForm(sku);
            itemService.updateItem(item);
            refreshTable();
            DialogUtils.showSuccess(this, "Item updated.");
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void deleteItem() {
        int row = table.getSelectedRow();
        if (row < 0) { DialogUtils.showError(this, "Select an item to delete."); return; }
        String sku  = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        if (!DialogUtils.confirm(this, "Delete item \"" + name + "\"?","Confirm Delete")) return;
        try {
            itemService.removeItem(sku, currentUser);
            refreshTable();
            clearForm();
            DialogUtils.showSuccess(this, "Item deleted.");
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private Item buildItemFromForm(String skuOverride) throws Exception {
        String sku      = skuOverride != null ? skuOverride : skuField.getText().trim();
        String name     = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String category = categoryField.getText().trim();

        if (sku.isEmpty())   throw new Exception("SKU is required.");
        if (name.isEmpty())  throw new Exception("Name is required.");

        BigDecimal price = BigDecimal.ZERO;
        if (!priceStr.isEmpty()) {
            try { price = new BigDecimal(priceStr); }
            catch (NumberFormatException e) { throw new Exception("Invalid price format."); }
        }

        Item item = new Item();
        item.setItemSku(sku);
        item.setItemName(name);
        item.setUnitPrice(price);
        item.setCategory(category);
        return item;
    }

    // ── Table ops ─────────────────────────────────────────────────────────────
    public void refreshTable() {
        try {
            populateTable(itemService.getAllItems());
        } catch (Exception ex) {
            DialogUtils.showError(this, "Failed to load items: " + ex.getMessage());
        }
    }

    private void populateTable(List<Item> list) {
        tableModel.setRowCount(0);
        for (Item i : list) {
            tableModel.addRow(new Object[]{i.getItemSku(), i.getItemName(), i.getUnitPrice(), i.getCategory()});
        }
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        skuField     .setText((String) tableModel.getValueAt(row, 0));
        nameField    .setText((String) tableModel.getValueAt(row, 1));
        priceField   .setText(String.valueOf(tableModel.getValueAt(row, 2)));
        categoryField.setText((String) tableModel.getValueAt(row, 3));
    }

    private void clearForm() {
        table.clearSelection();
        skuField.setText(""); nameField.setText("");
        priceField.setText(""); categoryField.setText("");
    }

    private JPanel labeledField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UITheme.BG_CARD);
        p.add(UITheme.label(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}
