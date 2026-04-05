package com.oop.project.ui;

import com.oop.project.model.Customer;
import com.oop.project.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Modal dialog for add/edit Customer — FR-1.1, FR-1.2. */
public class CustomerDialog extends JDialog {

    private Customer result = null;
    private final boolean isEdit;

    private JTextField nameField, phoneField, emailField, addressField;

    public CustomerDialog(Window owner, Customer toEdit) {
        super(owner, toEdit==null?"Add Customer":"Edit Customer", ModalityType.APPLICATION_MODAL);
        this.isEdit = toEdit != null;
        buildUI(toEdit);
    }

    private void buildUI(Customer c) {
        setResizable(false);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(26,32,22,32));
        setContentPane(root);

        JLabel title = UITheme.heading(isEdit ? "Edit Customer" : "New Customer");
        title.setBorder(BorderFactory.createEmptyBorder(0,0,18,0));
        root.add(title, BorderLayout.NORTH);

        nameField    = UITheme.styledTextField();
        phoneField   = UITheme.styledTextField();
        emailField   = UITheme.styledTextField();
        addressField = UITheme.styledTextField();

        if (isEdit && c != null) {
            nameField   .setText(c.getCustomerName() != null ? c.getCustomerName() : "");
            phoneField  .setText(c.getPhone()        != null ? c.getPhone()        : "");
            emailField  .setText(c.getEmail()        != null ? c.getEmail()        : "");
            addressField.setText(c.getAddress()      != null ? c.getAddress()      : "");
        }

        JPanel fields = new JPanel(new GridLayout(0,1,0,10));
        fields.setBackground(UITheme.BG_CARD);
        fields.add(UITheme.labeledField("Full Name *",     nameField));
        fields.add(UITheme.labeledField("Phone * (0xxxxxxxxx)", phoneField));
        fields.add(UITheme.labeledField("Email *",         emailField));
        fields.add(UITheme.labeledField("Address",         addressField));
        root.add(fields, BorderLayout.CENTER);

        JButton save   = UITheme.primaryButton(isEdit ? "Update" : "Add Customer");
        JButton cancel = UITheme.ghostButton("Cancel");
        save  .addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        btn.setBackground(UITheme.BG_CARD);
        btn.setBorder(BorderFactory.createEmptyBorder(18,0,0,0));
        btn.add(cancel); btn.add(save);
        root.add(btn, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(400, 0));
        setLocationRelativeTo(getOwner());
    }

    private void onSave() {
        String name  = nameField   .getText().trim();
        String phone = phoneField  .getText().trim();
        String email = emailField  .getText().trim();
        String addr  = addressField.getText().trim();

        if (Validator.checkEmpty(name))  { UITheme.showError(this,"Name is required."); nameField.requestFocus(); return; }
        if (!Validator.isValidPhone(phone)) { UITheme.showError(this,"Invalid phone number (e.g. 0912345678)."); phoneField.requestFocus(); return; }
        if (!Validator.isValidEmail(email)) { UITheme.showError(this,"Invalid email format."); emailField.requestFocus(); return; }

        result = new Customer();
        result.setCustomerName(name);
        result.setPhone(phone);
        result.setEmail(email);
        result.setAddress(addr);
        dispose();
    }

    public Customer getResult() { return result; }
}