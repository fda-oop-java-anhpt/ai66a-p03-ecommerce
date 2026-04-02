package com.oop.project.ui.panels;

import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.model.User;
import com.oop.project.service.interfaces.CustomerService;
import com.oop.project.ui.components.SearchBar;
import com.oop.project.ui.utils.DialogUtils;
import com.oop.project.ui.utils.TableUtils;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
// import javax.swing.event.ListSelectionEvent;
// import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Customer Management tab.
 * FR-1.1 CRUD, FR-1.2 validation, FR-1.3 search, FR-1.4 order history.
 */
public class CustomerPanel extends JPanel {

    private final CustomerService customerService;
    private final User currentUser;

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        nameField, phoneField, emailField;
    private JButton           addBtn, updateBtn, deleteBtn, viewOrdersBtn;

    private static final String[] COLUMNS =
            {"ID", "Name", "Phone", "Email", "Created Date"};

    public CustomerPanel(CustomerService customerService, User currentUser) {
        this.customerService = customerService;
        this.currentUser = currentUser;
        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_DARK);

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildFormSide(), BorderLayout.EAST);
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));

        JLabel title = UITheme.title("Customers");
        title.setForeground(UITheme.TEXT_PRIMARY);

        SearchBar search = new SearchBar("Search by name or phone…", this::doSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(search);

        p.add(title, BorderLayout.WEST);
        p.add(right,  BorderLayout.EAST);
        return p;
    }

    // ── Table section ─────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        tableModel = TableUtils.nonEditableModel(COLUMNS);
        table = new JTable(tableModel);
        TableUtils.applyDefaultRenderers(table);
        TableUtils.setColumnWidths(table, 60, 180, 130, 220, 140);

        // Select row -> populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG_DARK);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 8));
        wrap.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        return wrap;
    }

    // ── Right-side form ───────────────────────────────────────────────────────
    private JPanel buildFormSide() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(UITheme.BG_CARD);
        side.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        side.setPreferredSize(new Dimension(300, 0));

        JLabel heading = UITheme.heading("Customer Details");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        nameField  = UITheme.styledTextField();
        phoneField = UITheme.styledTextField();
        emailField = UITheme.styledTextField();

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 8));
        fields.setBackground(UITheme.BG_CARD);
        fields.add(labeledField("Full Name",    nameField));
        fields.add(labeledField("Phone Number", phoneField));
        fields.add(labeledField("Email",        emailField));

        addBtn        = UITheme.ghostButton("Add Customer");
        updateBtn     = UITheme.ghostButton("Update");
        deleteBtn     = UITheme.ghostButton("Delete");
        viewOrdersBtn = UITheme.ghostButton("View Orders");

        addBtn.addActionListener(e -> addCustomer());
        updateBtn.addActionListener(e -> updateCustomer());
        deleteBtn.addActionListener(e -> deleteCustomer());
        viewOrdersBtn.addActionListener(e -> viewOrders());

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setBackground(UITheme.BG_CARD);
        buttons.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(viewOrdersBtn);
        buttons.add(deleteBtn);

        JButton clearBtn = UITheme.dangerButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UITheme.BG_CARD);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        bottom.add(clearBtn, BorderLayout.CENTER);

        side.add(heading, BorderLayout.NORTH);
        side.add(fields,  BorderLayout.CENTER);
        side.add(buttons, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG_CARD);
        wrapper.add(side, BorderLayout.CENTER);
        wrapper.add(bottom, BorderLayout.SOUTH);
        return wrapper;
    }

    // ── CRUD actions ──────────────────────────────────────────────────────────
    private void addCustomer() {
        String name  = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty()) { DialogUtils.showError(this, "Name is required."); return; }

        try {
            Customer c = new Customer();
            c.setCustomerName(name);
            c.setPhone(phone);
            c.setEmail(email);
            customerService.addCustomer(c);
            refreshTable();
            clearForm();
            DialogUtils.showSuccess(this, "Customer added successfully.");
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void updateCustomer() {
        int row = table.getSelectedRow();
        if (row < 0) { DialogUtils.showError(this, "Select a customer first."); return; }

        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Optional<Customer> opt = customerService.getCustomerById(id);
            if (opt.isPresent()) {
                Customer c = opt.get();
                c.setCustomerName(nameField.getText().trim());
                c.setPhone(phoneField.getText().trim());
                c.setEmail(emailField.getText().trim());
                customerService.updateCustomer(c);
                refreshTable();
                DialogUtils.showSuccess(this, "Customer updated.");
            } else {
                DialogUtils.showError(this, "Customer not found.");
            }
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void deleteCustomer() {
        int row = table.getSelectedRow();
        if (row < 0) { DialogUtils.showError(this, "Select a customer to delete."); return; }

        int id   = (int) tableModel.getValueAt(row, 0);
        String n = (String) tableModel.getValueAt(row, 1);
        if (!DialogUtils.confirm(this,
                "Delete customer \"" + n + "\"? This cannot be undone.", "Confirm Delete")) return;

        try {
            customerService.deleteCustomer(id, currentUser);
            refreshTable();
            clearForm();
            DialogUtils.showSuccess(this, "Customer deleted.");
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void viewOrders() {
        int row = table.getSelectedRow();
        if (row < 0) { DialogUtils.showError(this, "Select a customer first."); return; }

        int    id   = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        try {
            List<Order> orders = customerService.getCustomerOrderHistory(id);
            if (orders.isEmpty()) {
                DialogUtils.showInfo(this, name + " has no past orders.", "Order History");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Order history for: ").append(name).append("\n\n");
            orders.forEach(o -> sb.append(o.toString()).append("\n"));
            DialogUtils.showScrollableText(this, sb.toString(), "Order History — " + name);
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void doSearch(String keyword) {
        try {
            List<Customer> results = customerService.searchCustomer(keyword);
            populateTable(results);
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    // ── Table refresh ─────────────────────────────────────────────────────────
    public void refreshTable() {
        try {
            populateTable(customerService.getAllCustomers());
        } catch (Exception ex) {
            DialogUtils.showError(this, "Failed to load customers: " + ex.getMessage());
        }
    }

    private void populateTable(List<Customer> list) {
        tableModel.setRowCount(0);
        for (Customer c : list) {
            tableModel.addRow(new Object[]{
                c.getCustomerId(), c.getCustomerName(), c.getPhone(), c.getEmail(), c.getCreatedDate()
            });
        }
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        nameField .setText((String) tableModel.getValueAt(row, 1));
        phoneField.setText((String) tableModel.getValueAt(row, 2));
        emailField.setText((String) tableModel.getValueAt(row, 3));
    }

    private void clearForm() {
        table.clearSelection();
        nameField.setText(""); phoneField.setText(""); emailField.setText("");
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private JPanel labeledField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UITheme.BG_CARD);
        p.add(UITheme.label(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}
