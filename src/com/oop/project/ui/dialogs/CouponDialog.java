package com.oop.project.ui.dialogs;

import com.oop.project.model.Coupon;
import com.oop.project.model.DiscountType;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Modal dialog for Add/Edit Coupon — FR-4.1, FR-4.2.
 * Validates expiry date is in the future.
 * Live preview of discount effect.
 */
public class CouponDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Coupon result = null;
    private final boolean isEdit;

    private JTextField codeField, valueField, minOrderField, expiryField;
    private JComboBox<String> typeCombo;
    private JCheckBox activeCheck;
    private JLabel previewLbl;

    public CouponDialog(Window owner, Coupon toEdit) {
        super(owner, toEdit == null ? "Add Coupon" : "Edit Coupon", ModalityType.APPLICATION_MODAL);
        this.isEdit = toEdit != null;
        buildUI(toEdit);
    }

    private void buildUI(Coupon c) {
        setResizable(false);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(26, 32, 22, 32));
        setContentPane(root);

        JLabel title = UITheme.heading(isEdit ? "Edit Coupon" : "New Coupon");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        root.add(title, BorderLayout.NORTH);

        codeField = UITheme.styledTextField();
        valueField = UITheme.styledTextField();
        minOrderField = UITheme.styledTextField();
        expiryField = UITheme.styledTextField();
        expiryField.setText(LocalDate.now().plusMonths(1).format(FMT));

        typeCombo = UITheme.styledComboBox(new String[] { "Percent", "Fixed" });
        activeCheck = new JCheckBox("Active");
        activeCheck.setSelected(true);
        activeCheck.setFont(UITheme.FONT_BODY);
        activeCheck.setForeground(UITheme.TEXT_PRIMARY);
        activeCheck.setBackground(UITheme.BG_CARD);
        activeCheck.setOpaque(false);

        previewLbl = UITheme.label("Preview: —");
        previewLbl.setForeground(UITheme.ACCENT);
        previewLbl.setFont(UITheme.FONT_SMALL);

        if (isEdit && c != null) {
            codeField.setText(c.getCouponCode() != null ? c.getCouponCode() : "");
            codeField.setEditable(false);
            codeField.setForeground(UITheme.TEXT_MUTED);
            valueField.setText(c.getDiscountValue() != null ? c.getDiscountValue().toPlainString() : "");
            minOrderField.setText(c.getMinOrderValue() != null ? c.getMinOrderValue().toPlainString() : "0");
            expiryField.setText(c.getExpiryDate() != null ? c.getExpiryDate().toString() : "");
            typeCombo.setSelectedItem(c.getDiscountType() != null ? c.getDiscountType().name() : "Percent");
            activeCheck.setSelected(c.isActive());
            // if coupon is expired, disable active checkbox
            if (c.getExpiryDate() != null && c.getExpiryDate().before(new java.sql.Date(System.currentTimeMillis()))) {
                activeCheck.setEnabled(false);
                activeCheck.setToolTipText("Cannot activate an expired coupon. Update expiry date first.");
            }
        }

        // Live preview on key release
        KeyAdapter kl = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updatePreview();
            }
        };
        valueField.addKeyListener(kl);
        typeCombo.addActionListener(e -> updatePreview());

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 10));
        fields.setBackground(UITheme.BG_CARD);
        fields.add(UITheme.labeledField("Coupon Code *" + (isEdit ? "  (locked)" : ""), codeField));
        fields.add(UITheme.labeledField("Discount Type", typeCombo));
        fields.add(UITheme.labeledField("Discount Value *", valueField));
        fields.add(UITheme.labeledField("Min Order Value (VNĐ)", minOrderField));
        fields.add(UITheme.labeledField("Expiry Date *  (yyyy-MM-dd)", expiryField));
        fields.add(activeCheck);
        fields.add(previewLbl);
        root.add(fields, BorderLayout.CENTER);

        JButton save = UITheme.primaryButton(isEdit ? "Update Coupon" : "Add Coupon");
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

    private void updatePreview() {
        try {
            double val = Double.parseDouble(valueField.getText().trim());
            String type = (String) typeCombo.getSelectedItem();
            previewLbl.setText("Percent".equals(type)
                    ? String.format("Preview: %.0f%% off the order", val)
                    : String.format("Preview: %,.0f VNĐ off the order", val));
            previewLbl.setForeground(UITheme.ACCENT);
        } catch (NumberFormatException e) {
            previewLbl.setText("Preview: enter a valid value");
            previewLbl.setForeground(UITheme.TEXT_MUTED);
        }
    }

    private void onSave() {
        String code = codeField.getText().trim().toUpperCase();
        String valStr = valueField.getText().trim();
        String minStr = minOrderField.getText().trim();
        String expStr = expiryField.getText().trim();
        String typeStr = (String) typeCombo.getSelectedItem();

        if (!isEdit && code.isEmpty()) {
            UITheme.showError(this, "Coupon code is required.");
            codeField.requestFocus();
            return;
        }

        BigDecimal value;
        try {
            value = new BigDecimal(valStr);
            if (value.compareTo(BigDecimal.ZERO) <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            UITheme.showError(this, "Discount value must be a positive number.");
            valueField.requestFocus();
            return;
        }

        BigDecimal minOrder = BigDecimal.ZERO;
        if (!minStr.isEmpty()) {
            try {
                minOrder = new BigDecimal(minStr);
                if (minOrder.compareTo(BigDecimal.ZERO) < 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                UITheme.showError(this, "Min order value must be 0 or positive.");
                minOrderField.requestFocus();
                return;
            }
        }

        Date expiry;
        try {
            LocalDate ld = LocalDate.parse(expStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (ld.isBefore(LocalDate.now())) {
                UITheme.showError(this, "Expiry date must be in the future.");
                expiryField.requestFocus();
                return;
            }
            expiry = Date.valueOf(ld);
        } catch (DateTimeParseException ex) {
            UITheme.showError(this, "Expiry date must be in yyyy-MM-dd format.");
            expiryField.requestFocus();
            return;
        }

        result = new Coupon();
        result.setCouponCode(code);
        result.setDiscountValue(value);
        result.setDiscountType(DiscountType.valueOf(typeStr));
        result.setMinOrderValue(minOrder);
        result.setExpiryDate(expiry);
        result.setActive(activeCheck.isSelected());
        dispose();
    }

    public Coupon getResult() {
        return result;
    }
}