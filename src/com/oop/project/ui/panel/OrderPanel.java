package com.oop.project.ui.panel;

import com.oop.project.model.*;
import com.oop.project.repository.interfaces.OrderRepository;
import com.oop.project.service.interfaces.*;
import com.oop.project.repository.impl.OrderRepositoryImpl;
import com.oop.project.repository.interfaces.SystemSettingRepository;
import com.oop.project.repository.impl.SystemSettingRepositoryImpl;
import com.oop.project.ui.frames.MainFrame;
import com.oop.project.ui.frames.OrderFrame;
import com.oop.project.ui.utils.TableRenderer;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Orders tab — FR-3, FR-4.
 * Left: sortable order list with status filter + search.
 * Right: create new order form (customer, items, coupon, real-time bill).
 * Uses BillingService.createOrder(), cancelOrder(), generateInvoice().
 */
public class OrderPanel extends JPanel {

    private final MainFrame mf;
    private final IBillingService billSvc;
    private final ICustomerService custSvc;
    private final IItemService itemSvc;
    private final ICouponService couponSvc;
    private final OrderRepository orderRepo = new OrderRepositoryImpl(); // for view/delete
    private final SystemSettingRepository settingRepo = new SystemSettingRepositoryImpl();

    // Order list (left side)
    private DefaultTableModel orderModel;
    private JTable orderTable;
    private Integer selectedOrderId = null;

    private JButton cancelBtn;
    private JButton updateSBtn;
    private OrderFrame orderFrame;
    private static final String[] ORDER_COLS = { "ID", "Customer", "Date", "Status", "Total" };
    public OrderPanel(MainFrame mf) {
        this.mf = mf;
        this.billSvc = mf.billingService;
        this.custSvc = mf.customerService;
        this.itemSvc = mf.itemService;
        this.couponSvc = mf.couponService;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildTop(), BorderLayout.NORTH);
        add(buildOrderList(), BorderLayout.CENTER);
        refresh();
    }

    // ── Top bar: title + filter + search ──────────────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        p.add(UITheme.title("Orders"), BorderLayout.WEST);

        JTextField searchField = UITheme.styledTextField();
        searchField.setPreferredSize(new Dimension(200, 34));

        JComboBox<String> statusFilter = UITheme.styledComboBox(
                new String[] { "All Statuses", "PENDING", "PAID", "CANCELLED" });
        statusFilter.addActionListener(e -> {
            String sel = (String) statusFilter.getSelectedItem();
            applyFilter(sel, null, null, searchField.getText().trim());
        });
        searchField.addActionListener(e -> applyFilter((String) statusFilter.getSelectedItem(),
                null, null, searchField.getText().trim()));

        JButton searchBtn = UITheme.primaryButton("Search");
        searchBtn.addActionListener(e -> applyFilter((String) statusFilter.getSelectedItem(),
                null, null, searchField.getText().trim()));

        JButton allBtn = UITheme.ghostButton("Show All");
        allBtn.addActionListener(e -> refresh());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UITheme.label("Status:"));
        right.add(statusFilter);
        right.add(UITheme.label("Search:"));
        right.add(searchField);
        right.add(searchBtn);
        right.add(allBtn);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private void openCreateOrderForm() {
        if (orderFrame == null) {
            orderFrame = new OrderFrame(mf, this);
        }
        orderFrame.showForm();
    }

    public void onOrderCreated() {
        refresh();
        if (orderTable != null) orderTable.clearSelection();
        selectedOrderId = null;
        if (updateSBtn != null) { updateSBtn.setEnabled(true); updateSBtn.setToolTipText(null); }
        if (cancelBtn != null)  { cancelBtn.setEnabled(true);  cancelBtn.setToolTipText(null); }
    }

    // ── Left: order list ──────────────────────────────────────────────────────
    private JPanel buildOrderList() {
        orderModel = TableRenderer.model(ORDER_COLS);
        orderTable = new JTable(orderModel);
        TableRenderer.applyAll(orderTable);
        orderTable.getColumnModel().getColumn(3).setCellRenderer(TableRenderer.status());
        orderTable.getColumnModel().getColumn(4).setCellRenderer(TableRenderer.currency());
        TableRenderer.widths(orderTable, 70, 180, 130, 100, 110);

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                onOrderSelected();
        });
        // Double-click → show invoice
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2)
                    showInvoice();
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 8));

        JLabel hint = UITheme.label("Double-click an order to view invoice");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setBorder(BorderFactory.createEmptyBorder(2, 0, 6, 0));
        p.add(hint, BorderLayout.NORTH);
        p.add(UITheme.scrollPane(orderTable), BorderLayout.CENTER);

        // Action buttons below list
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        acts.setBackground(UITheme.BG_DARK);

        JButton createBtn = UITheme.primaryButton("+ Create Order");
        JButton invoiceBtn = UITheme.primaryButton("View Invoice");
        cancelBtn = UITheme.dangerButton("Cancel Order");
        JButton deleteBtn = UITheme.dangerButton("Delete");
        updateSBtn = UITheme.primaryButton("Update Status →");        
        createBtn.addActionListener(e -> openCreateOrderForm());
        invoiceBtn.addActionListener(e -> showInvoice());
        cancelBtn.addActionListener(e -> cancelOrder());
        deleteBtn.addActionListener(e -> deleteOrder());
        updateSBtn.addActionListener(e -> updateStatus());
        acts.add(createBtn);
        acts.add(updateSBtn);
        acts.add(invoiceBtn);
        acts.add(Box.createHorizontalStrut(8));
        acts.add(cancelBtn);
        acts.add(deleteBtn);
        p.add(acts, BorderLayout.SOUTH);
        return p;
    }

    private void onOrderSelected() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            selectedOrderId = null;
            return;
        }
        selectedOrderId = (int) orderModel.getValueAt(row, 0);
        String status = (String) orderModel.getValueAt(row, 3);
        
        // FR-4.3 / business rule: CANCELLED orders are final — cannot be edited
        boolean isCancelled = "CANCELLED".equals(status);
        if (updateSBtn != null) updateSBtn.setEnabled(!isCancelled);
        if (cancelBtn != null) cancelBtn.setEnabled(!isCancelled);
        
        if (isCancelled) {
            if (updateSBtn != null) updateSBtn.setToolTipText("Cannot change status: order is already cancelled");
            if (cancelBtn != null) cancelBtn.setToolTipText("Order is already cancelled");
        } else {
            if (updateSBtn != null) updateSBtn.setToolTipText(null);
            if (cancelBtn != null) cancelBtn.setToolTipText(null);
        }
    }

    private void updateStatus() {
        if (selectedOrderId == null) {
            UITheme.showError(this, "Select an order first.");
            return;
        }
        Order current = orderRepo.findById(selectedOrderId);
        if (current != null && current.getStatus() == OrderStatus.CANCELLED) {
            UITheme.showError(this, "Order #" + selectedOrderId + " is already cancelled.");
            return;
        }

        String[] choices = { "PENDING", "PAID", "CANCELLED" };
        String currentStatus = current != null && current.getStatus() != null ? current.getStatus().name() : "PENDING";
        String newStatus = (String) JOptionPane.showInputDialog(
                this, "Select new status for Order #" + selectedOrderId + ":",
                "Update Status", JOptionPane.QUESTION_MESSAGE, null, choices, currentStatus);

        if (newStatus == null || newStatus.equals(currentStatus)) return;

        try {
            if ("CANCELLED".equals(newStatus)) {
                billSvc.cancelOrder(selectedOrderId, mf.getCurrentUser());
            } else {
                orderRepo.updateStatus(selectedOrderId, newStatus);
            }
            refresh();
            UITheme.showSuccess(this, "Status updated to " + newStatus + ".");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void cancelOrder() {
        if (selectedOrderId == null) {
            UITheme.showError(this, "Select an order to cancel.");
            return;
        }
        // Guard: prevent cancelling an already-cancelled order
        Order current = orderRepo.findById(selectedOrderId);
        if (current != null && current.getStatus() == OrderStatus.CANCELLED) {
            UITheme.showError(this, "Order #" + selectedOrderId + " is already cancelled.");
            return;
        }
        if (!UITheme.confirm(this, "Cancel order #" + selectedOrderId + "? Stock will be restored.", "Confirm Cancel"))
            return;
        try {
            billSvc.cancelOrder(selectedOrderId, mf.getCurrentUser());
            refresh();
            UITheme.showSuccess(this, "Order #" + selectedOrderId + " cancelled.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void deleteOrder() {
        if (selectedOrderId == null) {
            UITheme.showError(this, "Select an order to delete.");
            return;
        }
        if (!UITheme.confirm(this, "Permanently delete order #" + selectedOrderId + "?", "Confirm Delete"))
            return;
        try {
            orderRepo.delete(selectedOrderId);
            refresh();
            UITheme.showSuccess(this, "Order deleted.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void showInvoice() {
        if (selectedOrderId == null) {
            UITheme.showError(this, "Select an order first.");
            return;
        }
        try {
            Order order = orderRepo.findById(selectedOrderId);
            if (order == null) {
                UITheme.showError(this, "Order not found.");
                return;
            }
            String invoice = billSvc.generateInvoice(order);
            UITheme.showScrollable(this, invoice, "Invoice — Order #" + selectedOrderId);
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }


    // ── Data ──────────────────────────────────────────────────────────────────
    public void refresh() {
        try {
            populateOrderTable(orderRepo.findAll());
        } catch (Exception ex) {
            UITheme.showError(this, "Failed to load orders: " + ex.getMessage());
        }
    }

    private void applyFilter(String status, java.sql.Timestamp from,
            java.sql.Timestamp to, String keyword) {
        try {
            List<Order> list;
            if (keyword != null && !keyword.isEmpty()) {
                list = orderRepo.searchByCustomerNameOrId(keyword);
            } else if (status != null && !status.equals("All Statuses")) {
                list = orderRepo.filterByStatusOrDateRange(status, from, to);
            } else {
                list = orderRepo.findAll();
            }
            populateOrderTable(list);
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void populateOrderTable(List<Order> list) {
        orderModel.setRowCount(0);
        for (Order o : list) {
            String cname = o.getCustomer() != null ? o.getCustomer().getCustomerName() : "—";
            orderModel.addRow(new Object[] {
                    o.getOrderId(), cname, o.getOrderDate(),
                    o.getStatus() != null ? o.getStatus().name() : "—",
                    o.getFinalTotal()
            });
        }
    }

}
