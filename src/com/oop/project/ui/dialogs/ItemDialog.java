package com.oop.project.ui.dialogs;

import com.oop.project.model.Item;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

/**
 * Modal dialog for Add/Edit Item.
 * FR-2.1, FR-2.3 (SKU duplicate checked by service), FR-2.4 (price locked for
 * Staff).
 */
public class ItemDialog extends JDialog {

    private Item result = null;
    private final boolean isEdit;
    private final boolean isAdmin;

    private JTextField skuField, nameField, priceField, stockField;
    private JComboBox<String> categoryCombo;

    public ItemDialog(Window owner, Item toEdit, boolean isAdmin) {
        super(owner, toEdit == null ? "Add Item" : "Edit Item", ModalityType.APPLICATION_MODAL);
        this.isEdit = toEdit != null;
        this.isAdmin = isAdmin;
        buildUI(toEdit);
    }

    private void buildUI(Item item) {
        setResizable(false);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(26, 32, 22, 32));
        setContentPane(root);

        JLabel title = UITheme.heading(isEdit ? "Edit Item" : "New Item");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        root.add(title, BorderLayout.NORTH);

        skuField = UITheme.styledTextField();
        nameField = UITheme.styledTextField();
        categoryCombo = UITheme.styledComboBox(new String[] {
                "Điện thoại", "Tai nghe", "Màn hình", "Laptop", "Phụ kiện"
        });
        priceField = UITheme.styledTextField();
        stockField = UITheme.styledTextField();

        // SKU locked in edit mode (it is the PK)
        if (isEdit) {
            skuField.setEditable(false);
            skuField.setForeground(UITheme.TEXT_MUTED);
        }
        // Price & Fields locked for Staff (FR-2.4)
        if (!isAdmin) {
            nameField.setEditable(false);
            nameField.setForeground(UITheme.TEXT_MUTED);
            categoryCombo.setEnabled(false);
            
            priceField.setEditable(false);
            priceField.setForeground(UITheme.TEXT_MUTED);
            priceField.setToolTipText("Only Admin can modify prices and item details.");
        }

        if (isEdit && item != null) {
            skuField.setText(item.getItemSku() != null ? item.getItemSku() : "");
            nameField.setText(item.getItemName() != null ? item.getItemName() : "");

            if (item.getCategory() != null) {
                categoryCombo.setSelectedItem(item.getCategory());
            } else {
                categoryCombo.setSelectedIndex(0);
            }

            priceField.setText(item.getUnitPrice() != null ? item.getUnitPrice().toPlainString() : "");
            stockField.setText(String.valueOf(item.getStockQuantity()));
        }

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 10));
        fields.setBackground(UITheme.BG_CARD);
        fields.add(UITheme.labeledField("SKU Code *" + (isEdit ? "  (locked)" : ""), skuField));
        fields.add(UITheme.labeledField("Item Name *", nameField));
        fields.add(UITheme.labeledField("Category", categoryCombo));
        fields.add(UITheme.labeledField(isAdmin ? "Unit Price (VNĐ) *" : "Unit Price (Admin only)", priceField));
        fields.add(UITheme.labeledField("Stock Quantity", stockField));

        if (isEdit && !isAdmin) {
            // Staff editing: show what they CAN and CANNOT change
            JLabel warn = UITheme.label("\u26A0  Staff can only edit: Name, Category, Stock Quantity.");
            warn.setForeground(UITheme.WARNING);
            warn.setFont(UITheme.FONT_SMALL);
            fields.add(warn);
        } else if (!isAdmin) {
            // Staff adding new item (admin-only action normally, but just in case)
            JLabel warn = UITheme.label("\u26A0  Price editing is restricted to Admin users.");
            warn.setForeground(UITheme.WARNING);
            warn.setFont(UITheme.FONT_SMALL);
            fields.add(warn);
        }
        root.add(fields, BorderLayout.CENTER);

        JButton save = UITheme.primaryButton(isEdit ? "Update Item" : "Add Item");
        JButton cancel = UITheme.ghostButton("Cancel");
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btn.setBackground(UITheme.BG_CARD);
        btn.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        btn.add(cancel);
        btn.add(save);
        root.add(btn, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(420, 0));
        setLocationRelativeTo(getOwner());
    }

    private void onSave() {
        String sku = skuField.getText().trim();
        String name = nameField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String priceStr = priceField.getText().trim();
        String stockStr = stockField.getText().trim();

        if (!isEdit && sku.isEmpty()) {
            UITheme.showError(this, "SKU is required.");
            skuField.requestFocus();
            return;
        }
        if (name.isEmpty()) {
            UITheme.showError(this, "Item name is required.");
            nameField.requestFocus();
            return;
        }

        BigDecimal price = null;
        if (isAdmin) {
            // Admin must provide a valid positive price
            if (priceStr.isEmpty()) {
                UITheme.showError(this, "Unit price is required.");
                priceField.requestFocus();
                return;
            }
            try {
                price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) <= 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                UITheme.showError(this, "Unit price must be a positive number.");
                priceField.requestFocus();
                return;
            }
        } else {
            // Staff: price field is disabled but still contains the existing price — preserve it
            if (!priceStr.isEmpty()) {
                try {
                    price = new BigDecimal(priceStr);
                } catch (NumberFormatException ignored) {}
            }
        }

        int stock = 0;
        if (!stockStr.isEmpty()) {
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                UITheme.showError(this, "Stock quantity must be a non-negative integer.");
                stockField.requestFocus();
                return;
            }
        }

        result = new Item();
        result.setItemSku(sku);
        result.setItemName(name);
        result.setCategory(category.isEmpty() ? null : category);
        if (price != null)
            result.setUnitPrice(price);
        result.setStockQuantity(stock);
        dispose();
    }

    public Item getResult() {
        return result;
    }
}