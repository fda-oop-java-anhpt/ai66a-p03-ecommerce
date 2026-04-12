package com.oop.project.ui.panel;

import com.oop.project.model.*;
import com.oop.project.repository.interfaces.OrderRepository;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.service.interfaces.*;
import com.oop.project.repository.impl.OrderRepositoryImpl;
import com.oop.project.repository.impl.AuditLogRepositoryImpl;
import com.oop.project.ui.dialogs.CreateOrderDialog;
import com.oop.project.ui.frames.MainFrame;
import com.oop.project.ui.utils.TableRenderer;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Orders tab — FR-3, FR-4.
 * Full-width sortable order list; "Create New Order" opens a modal dialog.
 * Uses BillingService.cancelOrder(), generateInvoice(), OrderRepository for
 * list / delete / status-update.
 */
public class OrderPanel extends JPanel {

    private final MainFrame mf;
    private final IBillingService billSvc;
    private final ICustomerService custSvc;
    private final IItemService itemSvc;
    private final ICouponService couponSvc;
    private final OrderRepository orderRepo = new OrderRepositoryImpl();
    private final AuditLogRepository auditRepo = new AuditLogRepositoryImpl();

    // Order list table
    private DefaultTableModel orderModel;
    private JTable orderTable;
    private Integer selectedOrderId = null;

    // Action buttons that react to row selection
    private JButton cancelBtn;
    private JButton updateSBtn;
    private JComboBox<String> statusCombo;

    private static final String[] ORDER_COLS = { "ID", "Customer", "Date", "Status", "Total" };

    public OrderPanel(MainFrame mf) {
        this.mf       = mf;
        this.billSvc  = mf.billingService;
        this.custSvc  = mf.customerService;
        this.itemSvc  = mf.itemService;
        this.couponSvc = mf.couponService;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildTop(),    BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
        refresh();
    }

    // ── Top bar: title + status filter + search ───────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        p.add(UITheme.title("Orders"), BorderLayout.WEST);

        JTextField searchField = UITheme.styledTextField();
        searchField.setPreferredSize(new Dimension(220, 34));

        JComboBox<String> statusFilter = UITheme.styledComboBox(
                new String[] { "All Statuses", "PENDING", "PAID", "CANCELLED" });
        statusFilter.addActionListener(e -> {
            String sel = (String) statusFilter.getSelectedItem();
            applyFilter(sel, null, null, searchField.getText().trim());
        });
        searchField.addActionListener(e -> applyFilter(
                (String) statusFilter.getSelectedItem(), null, null, searchField.getText().trim()));

        JButton searchBtn = UITheme.primaryButton("Search");
        searchBtn.addActionListener(e -> applyFilter(
                (String) statusFilter.getSelectedItem(), null, null, searchField.getText().trim()));

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

    // ── Center: full-width order table ────────────────────────────────────────
    private JPanel buildCenter() {
        orderModel = TableRenderer.model(ORDER_COLS);
        orderTable = new JTable(orderModel);
        TableRenderer.applyAll(orderTable);
        orderTable.getColumnModel().getColumn(3).setCellRenderer(TableRenderer.status());
        orderTable.getColumnModel().getColumn(4).setCellRenderer(TableRenderer.currency());
        TableRenderer.widths(orderTable, 70, 220, 160, 120, 140);

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onOrderSelected();
        });
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showInvoice();
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG_DARK);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel hint = UITheme.label("Double-click an order to view invoice");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setBorder(BorderFactory.createEmptyBorder(2, 0, 6, 0));
        wrap.add(hint, BorderLayout.NORTH);
        wrap.add(UITheme.scrollPane(orderTable), BorderLayout.CENTER);
        return wrap;
    }

    // ── Bottom action bar ─────────────────────────────────────────────────────
    private JPanel buildBottom() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_DARK);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        // Left: primary actions on selected order
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        left.setOpaque(false);

        JButton newOrderBtn = UITheme.primaryButton("+ Create New Order");
        newOrderBtn.addActionListener(e -> openCreateOrderDialog());

        JButton invoiceBtn = UITheme.primaryButton("View Invoice");
        invoiceBtn.addActionListener(e -> showInvoice());

        cancelBtn  = UITheme.dangerButton("Cancel Order");
        cancelBtn.addActionListener(e -> cancelOrder());

        JButton deleteBtn = UITheme.dangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteOrder());

        left.add(newOrderBtn);
        left.add(Box.createHorizontalStrut(12));
        left.add(invoiceBtn);
        left.add(cancelBtn);
        left.add(deleteBtn);

        // Right: status update combo + button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        right.setOpaque(false);

        statusCombo = UITheme.styledComboBox(new String[] { "PENDING", "PAID", "CANCELLED" });
        statusCombo.setPreferredSize(new Dimension(140, 34));

        updateSBtn = UITheme.ghostButton("Update Status →");
        updateSBtn.addActionListener(e -> updateStatus());

        right.add(UITheme.label("Status:"));
        right.add(statusCombo);
        right.add(updateSBtn);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Open Create Order dialog ──────────────────────────────────────────────
    private void openCreateOrderDialog() {
        CreateOrderDialog dlg = new CreateOrderDialog(
                mf, billSvc, custSvc, itemSvc, couponSvc, mf.getCurrentUser());
        dlg.setVisible(true);
        Order created = dlg.getCreatedOrder();
        if (created != null) {
            refresh();
            UITheme.showSuccess(this, "Order #" + created.getOrderId() + " created successfully.");
        }
    }

    // ── Row selection ─────────────────────────────────────────────────────────
    private void onOrderSelected() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { selectedOrderId = null; return; }

        selectedOrderId = (int) orderModel.getValueAt(row, 0);
        String status   = (String) orderModel.getValueAt(row, 3);

        // Sync status combo to selected order
        statusCombo.setSelectedItem(status);

        boolean isCancelled = "CANCELLED".equals(status);
        if (updateSBtn != null) updateSBtn.setEnabled(!isCancelled);
        if (cancelBtn  != null) cancelBtn.setEnabled(!isCancelled);
        if (statusCombo != null) statusCombo.setEnabled(!isCancelled);

        if (isCancelled) {
            if (updateSBtn != null) updateSBtn.setToolTipText("Cannot change: order is already cancelled");
            if (cancelBtn  != null) cancelBtn.setToolTipText("Order is already cancelled");
        } else {
            if (updateSBtn != null) updateSBtn.setToolTipText(null);
            if (cancelBtn  != null) cancelBtn.setToolTipText(null);
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void updateStatus() {
        if (selectedOrderId == null) { UITheme.showError(this, "Select an order first."); return; }
        Order current = orderRepo.findById(selectedOrderId);
        if (current != null && current.getStatus() == OrderStatus.CANCELLED) {
            UITheme.showError(this, "Order #" + selectedOrderId + " is already cancelled.");
            return;
        }
        String newStatus = (String) statusCombo.getSelectedItem();
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
        if (selectedOrderId == null) { UITheme.showError(this, "Select an order to cancel."); return; }
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
        if (selectedOrderId == null) { UITheme.showError(this, "Select an order to delete."); return; }
        
        Order current = orderRepo.findById(selectedOrderId);
        if (current != null && current.getStatus() != OrderStatus.CANCELLED) {
            UITheme.showError(this, "Only CANCELLED orders can be deleted.");
            return;
        }
        
        if (!UITheme.confirm(this, "Permanently delete order #" + selectedOrderId + "?", "Confirm Delete"))
            return;
        try {
            orderRepo.delete(selectedOrderId);
            auditRepo.insert(new AuditLog(0, mf.getCurrentUser(), "DELETE", "Order", String.valueOf(selectedOrderId), null));
            refresh();
            UITheme.showSuccess(this, "Order deleted.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void showInvoice() {
        if (selectedOrderId == null) { UITheme.showError(this, "Select an order first."); return; }
        try {
            Order order = orderRepo.findById(selectedOrderId);
            if (order == null) { UITheme.showError(this, "Order not found."); return; }
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
        // Reset selection state
        selectedOrderId = null;
        if (updateSBtn != null) { updateSBtn.setEnabled(true); updateSBtn.setToolTipText(null); }
        if (cancelBtn  != null) { cancelBtn.setEnabled(true);  cancelBtn.setToolTipText(null); }
        if (statusCombo != null) statusCombo.setEnabled(true);
    }
}