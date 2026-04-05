package com.oop.project.ui;

import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Customer Management tab — FR-1.
 * Uses CustomerService: getAllCustomers(), search(), addCustomer(),
 * updateCustomer(), deleteCustomer(), getOrderHistory().
 */
public class CustomerPanel extends JPanel {

    private final MainFrame        mf;
    private final CustomerService  svc;

    private DefaultTableModel model;
    private JTable            table;
    private JTextField        searchField;

    private static final String[] COLS =
        {"ID", "Name", "Phone", "Email", "Address", "Created"};

    public CustomerPanel(MainFrame mf) {
        this.mf  = mf;
        this.svc = mf.customerService;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildTop(),    BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
        refresh();
    }

    // ── Top: title + search ───────────────────────────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(12,0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16,20,12,20));
        p.add(UITheme.title("Customers"), BorderLayout.WEST);

        searchField = UITheme.styledTextField();
        searchField.setPreferredSize(new Dimension(240,34));
        searchField.addActionListener(e -> doSearch());
        JButton sBtn = UITheme.primaryButton("Search");
        sBtn.addActionListener(e -> doSearch());
        JButton allBtn = UITheme.ghostButton("Show All");
        allBtn.addActionListener(e -> refresh());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
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
        TableRenderer.widths(table, 50,180,120,200,160,130);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount()==2) openEditDialog();
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG_DARK);
        wrap.setBorder(BorderFactory.createEmptyBorder(0,20,0,20));
        JLabel hint = UITheme.label("Double-click a row to edit");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setBorder(BorderFactory.createEmptyBorder(2,0,6,0));
        wrap.add(hint, BorderLayout.NORTH);
        wrap.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return wrap;
    }

    // ── Bottom action bar ─────────────────────────────────────────────────────
    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER_COLOR));

        JButton addBtn     = UITheme.primaryButton("+ Add Customer");
        JButton editBtn    = UITheme.primaryButton("Edit");
        JButton deleteBtn  = UITheme.dangerButton("Delete");
        JButton ordersBtn  = UITheme.ghostButton("View Orders");
        JButton refreshBtn = UITheme.ghostButton("Refresh");

        addBtn    .addActionListener(e -> openAddDialog());
        editBtn   .addActionListener(e -> openEditDialog());
        deleteBtn .addActionListener(e -> deleteSelected());
        ordersBtn .addActionListener(e -> viewOrders());
        refreshBtn.addActionListener(e -> refresh());

        p.add(addBtn); p.add(editBtn); p.add(deleteBtn);
        p.add(Box.createHorizontalStrut(12));
        p.add(ordersBtn); p.add(refreshBtn);
        return p;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void openAddDialog() {
        CustomerDialog dlg = new CustomerDialog(mf, null);
        dlg.setVisible(true);
        Customer c = dlg.getResult();
        if (c == null) return;
        try {
            svc.addCustomer(c);
            refresh();
            UITheme.showSuccess(this, "Customer added successfully.");
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    private void openEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { UITheme.showError(this,"Select a customer first."); return; }
        int id = (int) model.getValueAt(row,0);
        try {
            // Build customer from table row (avoid extra DB call)
            Customer c = new Customer();
            c.setCustomerId(id);
            c.setCustomerName((String)model.getValueAt(row,1));
            c.setPhone((String)model.getValueAt(row,2));
            c.setEmail((String)model.getValueAt(row,3));
            c.setAddress((String)model.getValueAt(row,4));

            CustomerDialog dlg = new CustomerDialog(mf, c);
            dlg.setVisible(true);
            Customer result = dlg.getResult();
            if (result == null) return;
            result.setCustomerId(id);
            svc.updateCustomer(result);
            refresh();
            UITheme.showSuccess(this,"Customer updated.");
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { UITheme.showError(this,"Select a customer to delete."); return; }
        int    id   = (int)    model.getValueAt(row,0);
        String name = (String) model.getValueAt(row,1);
        if (!UITheme.confirm(this,"Delete \""+name+"\"?","Confirm Delete")) return;
        try {
            svc.deleteCustomer(id);
            refresh();
            UITheme.showSuccess(this,"Customer deleted.");
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    private void viewOrders() {
        int row = table.getSelectedRow();
        if (row < 0) { UITheme.showError(this,"Select a customer first."); return; }
        int    id   = (int)    model.getValueAt(row,0);
        String name = (String) model.getValueAt(row,1);
        List<Order> orders = svc.getOrderHistory(id);
        if (orders.isEmpty()) {
            UITheme.showScrollable(this, name+" has no past orders.", "Order History"); return;
        }
        StringBuilder sb = new StringBuilder("Order history for: "+name+"\n\n");
        orders.forEach(o -> sb.append(String.format(
            "#%d  |  %s  |  $%.2f  |  %s\n",
            o.getOrderId(),
            o.getOrderDate()!=null?o.getOrderDate().toString():"—",
            o.getFinalTotal()!=null?o.getFinalTotal().doubleValue():0,
            o.getStatus())));
        UITheme.showScrollable(this, sb.toString(), "Order History — "+name);
    }

    private void doSearch() {
        String kw = searchField.getText().trim();
        populate(svc.search(kw));
    }

    // ── Table data ────────────────────────────────────────────────────────────
    public void refresh() {
        try { populate(svc.getAllCustomers()); }
        catch (Exception ex) { UITheme.showError(this,"Failed to load customers: "+ex.getMessage()); }
    }

    private void populate(List<Customer> list) {
        model.setRowCount(0);
        for (Customer c : list) {
            model.addRow(new Object[]{
                c.getCustomerId(), c.getCustomerName(), c.getPhone(),
                c.getEmail(), c.getAddress(), c.getCreatedDate()
            });
        }
    }
}