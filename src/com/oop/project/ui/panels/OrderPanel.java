package com.oop.project.ui.panels;

import com.oop.project.model.*;
import com.oop.project.model.OrderStatus;
import com.oop.project.model.UserRole;
import com.oop.project.service.interfaces.*;
import com.oop.project.ui.components.SearchBar;
import com.oop.project.ui.utils.DialogUtils;
import com.oop.project.ui.utils.TableUtils;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Order Management tab.
 * FR-3: CRUD for orders, multi-item lines, BillingService calls.
 * FR-4: Coupon codes, order status, audit log.
 */
public class OrderPanel extends JPanel {

    private final OrderService    orderService;
    private final CustomerService customerService;
    private final ItemService     itemService;
    private final BillingService  billingService;
    private final CouponService   couponService;
    private final User            currentUser;

    // Order list table
    private DefaultTableModel orderTableModel;
    private JTable            orderTable;

    // Order items table (inside create order form)
    private DefaultTableModel lineTableModel;
    private JTable            lineTable;

    // Form fields
    private JComboBox<String> customerCombo, itemCombo, statusCombo;
    private JSpinner          quantitySpinner;
    private JTextField        couponField;
    private JLabel            subtotalLbl, discountLbl, taxLbl, totalLbl;
    private JButton           addLineBtn, createOrderBtn, deleteOrderBtn,
                               updateStatusBtn, viewInvoiceBtn;

    // Tracks the currently-selected order ID
    private Integer selectedOrderId = null;

    private static final String[] ORDER_COLS  =
            {"Order ID", "Customer", "Date", "Status", "Total"};
    private static final String[] LINE_COLS   =
            {"SKU", "Item Name", "Qty", "Unit Price", "Line Total"};

    public OrderPanel(OrderService orderService, CustomerService customerService,
                      ItemService itemService, BillingService billingService,
                      CouponService couponService, User currentUser) {
        this.orderService    = orderService;
        this.customerService = customerService;
        this.itemService     = itemService;
        this.billingService  = billingService;
        this.couponService   = couponService;
        this.currentUser     = currentUser;
        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildSplitPane(), BorderLayout.CENTER);
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));

        p.add(UITheme.title("Orders"), BorderLayout.WEST);

        SearchBar search = new SearchBar("Search by customer or order ID…", keyword -> {
            try { populateOrderTable(orderService.searchOrders(keyword)); }
            catch (Exception ex) { DialogUtils.showError(this, ex.getMessage()); }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UITheme.label("Status:"));

        JComboBox<String> statusFilter = UITheme.styledComboBox(
                new String[]{"All", "PENDING", "PAID", "CANCELLED"});
        statusFilter.addActionListener(e -> {
            String sel = (String) statusFilter.getSelectedItem();
            try {
                if ("All".equals(sel)) refreshTable();
                else populateOrderTable(orderService.getOrdersByStatus(sel));
            } catch (Exception ex) { DialogUtils.showError(this, ex.getMessage()); }
        });
        right.add(statusFilter);
        right.add(search);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Split pane: left=order list, right=create/detail ─────────────────────
    private JSplitPane buildSplitPane() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildOrderListPanel(), buildOrderDetailPanel());
        split.setDividerLocation(620);
        split.setDividerSize(4);
        split.setBackground(UITheme.BORDER_COLOR);
        split.setBorder(null);
        return split;
    }

    // ── Left: order list ──────────────────────────────────────────────────────
    private JPanel buildOrderListPanel() {
        orderTableModel = TableUtils.nonEditableModel(ORDER_COLS);
        orderTable = new JTable(orderTableModel);
        TableUtils.applyDefaultRenderers(orderTable);

        // Status badge on column 3, currency on column 4
        orderTable.getColumnModel().getColumn(3)
                  .setCellRenderer(TableUtils.statusBadgeRenderer());
        orderTable.getColumnModel().getColumn(4)
                  .setCellRenderer(TableUtils.currencyRenderer());
        TableUtils.setColumnWidths(orderTable, 90, 170, 120, 100, 110);

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedOrder();
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 8));
        p.add(UITheme.styledScrollPane(orderTable), BorderLayout.CENTER);

        // Action buttons below list
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBackground(UITheme.BG_DARK);
        deleteOrderBtn    = UITheme.dangerButton("Delete Order");
        viewInvoiceBtn    = UITheme.ghostButton("View Invoice");
        deleteOrderBtn.addActionListener(e -> deleteOrder());
        viewInvoiceBtn.addActionListener(e -> viewInvoice());
        actions.add(deleteOrderBtn);
        actions.add(viewInvoiceBtn);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    // ── Right: create / update order form ─────────────────────────────────────
    private JPanel buildOrderDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setPreferredSize(new Dimension(460, 0));

        // ── Header ────────────────────────────────────────────────────────────
        JLabel heading = UITheme.heading("New Order");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        // ── Customer selector ─────────────────────────────────────────────────
        customerCombo = UITheme.styledComboBox(new String[]{"Loading…"});
        loadCustomerCombo();
        JPanel custRow = labeledComponent("Customer", customerCombo);

        // ── Item selector + qty ───────────────────────────────────────────────
        itemCombo = UITheme.styledComboBox(new String[]{"Loading…"});
        loadItemCombo();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        quantitySpinner.setFont(UITheme.FONT_BODY);
        ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField()
                .setBackground(UITheme.BG_INPUT);
        ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField()
                .setForeground(UITheme.TEXT_PRIMARY);

        JPanel itemRow = new JPanel(new BorderLayout(8, 0));
        itemRow.setBackground(UITheme.BG_CARD);
        itemRow.add(itemCombo, BorderLayout.CENTER);
        itemRow.add(quantitySpinner, BorderLayout.EAST);
        quantitySpinner.setPreferredSize(new Dimension(70, 34));

        addLineBtn = UITheme.primaryButton("+ Add Item");
        addLineBtn.addActionListener(e -> addLineItem());

        // ── Line items table ──────────────────────────────────────────────────
        lineTableModel = TableUtils.nonEditableModel(LINE_COLS);
        lineTable = new JTable(lineTableModel);
        TableUtils.applyDefaultRenderers(lineTable);
        lineTable.getColumnModel().getColumn(3).setCellRenderer(TableUtils.currencyRenderer());
        lineTable.getColumnModel().getColumn(4).setCellRenderer(TableUtils.currencyRenderer());
        TableUtils.setColumnWidths(lineTable, 90, 160, 50, 90, 90);
        lineTable.setPreferredScrollableViewportSize(new Dimension(0, 160));

        JScrollPane lineScroll = UITheme.styledScrollPane(lineTable);
        lineScroll.setPreferredSize(new Dimension(0, 170));

        JButton removeLineBtn = UITheme.dangerButton("Remove Line");
        removeLineBtn.addActionListener(e -> removeLineItem());

        // ── Coupon ────────────────────────────────────────────────────────────
        couponField = UITheme.styledTextField();
        JButton applyCouponBtn = UITheme.ghostButton("Apply");
        applyCouponBtn.addActionListener(e -> applyCoupon());
        JPanel couponRow = new JPanel(new BorderLayout(8, 0));
        couponRow.setBackground(UITheme.BG_CARD);
        couponRow.add(couponField, BorderLayout.CENTER);
        couponRow.add(applyCouponBtn, BorderLayout.EAST);

        // ── Status selector ───────────────────────────────────────────────────
        statusCombo = UITheme.styledComboBox(
                new String[]{"PENDING", "PAID", "CANCELLED"});
        updateStatusBtn = UITheme.ghostButton("Update Status");
        updateStatusBtn.addActionListener(e -> updateOrderStatus());
        JPanel statusRow = new JPanel(new BorderLayout(8, 0));
        statusRow.setBackground(UITheme.BG_CARD);
        statusRow.add(statusCombo, BorderLayout.CENTER);
        statusRow.add(updateStatusBtn, BorderLayout.EAST);

        // ── Billing summary ───────────────────────────────────────────────────
        JPanel summary = buildBillingSummary();

        // ── Create button ─────────────────────────────────────────────────────
        createOrderBtn = UITheme.successButton("✓ Create Order");
        createOrderBtn.setPreferredSize(new Dimension(0, 42));
        createOrderBtn.addActionListener(e -> createOrder());

        // ── Assemble ──────────────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.BG_CARD);

        form.add(custRow);
        form.add(vgap(8));
        form.add(labeledComponent("Item + Qty", itemRow));
        form.add(vgap(4));
        form.add(rightAlign(addLineBtn));
        form.add(vgap(8));
        form.add(lineScroll);
        form.add(vgap(2));
        form.add(rightAlign(removeLineBtn));
        form.add(vgap(8));
        form.add(hline());
        form.add(vgap(6));
        form.add(labeledComponent("Coupon Code", couponRow));
        form.add(vgap(6));
        form.add(labeledComponent("Order Status", statusRow));
        form.add(vgap(10));
        form.add(summary);
        form.add(vgap(14));
        createOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(createOrderBtn);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(form) {{
            setBorder(null);
            getViewport().setBackground(UITheme.BG_CARD);
            setBackground(UITheme.BG_CARD);
        }}, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildBillingSummary() {
        JPanel p = new JPanel(new GridLayout(0, 2, 0, 4));
        p.setBackground(new Color(20, 25, 40));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        subtotalLbl = moneyLabel("$0.00");
        discountLbl = moneyLabel("$0.00");
        taxLbl      = moneyLabel("$0.00");
        totalLbl    = moneyLabel("$0.00");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLbl.setForeground(UITheme.ACCENT);

        p.add(summaryLabel("Subtotal:")); p.add(subtotalLbl);
        p.add(summaryLabel("Discount:")); p.add(discountLbl);
        p.add(summaryLabel("Tax (8%):"));  p.add(taxLbl);
        p.add(hlineFull());               p.add(hlineFull());
        p.add(boldLabel("TOTAL:"));       p.add(totalLbl);
        return p;
    }

    // ── Add/remove line items ─────────────────────────────────────────────────
    private void addLineItem() {
        String selected = (String) itemCombo.getSelectedItem();
        if (selected == null || selected.startsWith("Loading")) return;

        int qty = (int) quantitySpinner.getValue();
        try {
            // Parse "SKU - Name ($price)" from combo text
            String sku = selected.split(" - ")[0].trim();
            Item item = itemService.getItemBySku(sku);
            double lineTotal = item.getUnitPrice() * qty;
            lineTableModel.addRow(new Object[]{
                item.getSku(), item.getName(), qty, item.getUnitPrice(), lineTotal
            });
            recalculateBill();
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void removeLineItem() {
        int row = lineTable.getSelectedRow();
        if (row < 0) { DialogUtils.showError(this, "Select a line to remove."); return; }
        lineTableModel.removeRow(row);
        recalculateBill();
    }

    private void applyCoupon() {
        recalculateBill();
    }

    // ── Billing calculation (FR-3.3, FR-3.4, FR-6.4) ─────────────────────────
    private double currentDiscount = 0;

    private void recalculateBill() {
        double subtotal = 0;
        for (int i = 0; i < lineTableModel.getRowCount(); i++) {
            subtotal += (double) lineTableModel.getValueAt(i, 4);
        }

        String coupon = couponField.getText().trim();
        currentDiscount = 0;
        if (!coupon.isEmpty()) {
            try {
                currentDiscount = couponService.getDiscountAmount(coupon, subtotal);
            } catch (Exception ex) {
                // invalid coupon — ignore silently; error shown on create
            }
        }

        double afterDiscount = Math.max(0, subtotal - currentDiscount);
        double tax   = afterDiscount * 0.08;
        double total = afterDiscount + tax;

        subtotalLbl.setText(String.format("$%.2f", subtotal));
        discountLbl.setText(String.format("-$%.2f", currentDiscount));
        taxLbl     .setText(String.format("+$%.2f", tax));
        totalLbl   .setText(String.format("$%.2f", total));
    }

    // ── Create order ──────────────────────────────────────────────────────────
    private void createOrder() {
        if (lineTableModel.getRowCount() == 0) {
            DialogUtils.showError(this, "Add at least one item to the order.");
            return;
        }
        String custSelected = (String) customerCombo.getSelectedItem();
        if (custSelected == null) { DialogUtils.showError(this, "Select a customer."); return; }

        try {
            int customerId = Integer.parseInt(custSelected.split(" - ")[0].trim());
            String coupon  = couponField.getText().trim().isEmpty() ? null : couponField.getText().trim();

            // Validate coupon if present
            if (coupon != null && !couponService.validateCoupon(coupon)) {
                DialogUtils.showError(this, "Invalid or expired coupon code.");
                return;
            }

            Order order = new Order();
            order.setCustomerId(customerId);
            order.setStatus(OrderStatus.valueOf(
                    (String) statusCombo.getSelectedItem()));
            order.setCouponCode(coupon);

            // Build line items from table
            for (int i = 0; i < lineTableModel.getRowCount(); i++) {
                OrderItem oi = new OrderItem();
                oi.setItemSku((String) lineTableModel.getValueAt(i, 0));
                oi.setQuantity((int) lineTableModel.getValueAt(i, 2));
                oi.setUnitPrice((double) lineTableModel.getValueAt(i, 3));
                order.addItem(oi);
            }

            orderService.createOrder(order);
            refreshTable();
            resetForm();
            DialogUtils.showSuccess(this, "Order created successfully.");
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void loadSelectedOrder() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { selectedOrderId = null; return; }
        selectedOrderId = (int) orderTableModel.getValueAt(row, 0);
        String status = (String) orderTableModel.getValueAt(row, 3);
        statusCombo.setSelectedItem(status);
    }

    private void updateOrderStatus() {
        if (selectedOrderId == null) {
            DialogUtils.showError(this, "Select an order to update its status.");
            return;
        }
        String newStatus = (String) statusCombo.getSelectedItem();
        try {
            orderService.updateOrderStatus(selectedOrderId,
                    OrderStatus.valueOf(newStatus));
            refreshTable();
            DialogUtils.showSuccess(this, "Status updated to " + newStatus + ".");
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void deleteOrder() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { DialogUtils.showError(this, "Select an order to delete."); return; }
        int id = (int) orderTableModel.getValueAt(row, 0);
        if (!DialogUtils.confirm(this, "Delete order #" + id + "?", "Confirm Delete")) return;
        try {
            orderService.deleteOrder(id);
            refreshTable();
            resetForm();
            DialogUtils.showSuccess(this, "Order deleted.");
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    // ── Invoice (FR-3.5) ──────────────────────────────────────────────────────
    private void viewInvoice() {
        if (selectedOrderId == null) {
            DialogUtils.showError(this, "Select an order to view its invoice.");
            return;
        }
        try {
            String invoice = orderService.generateInvoice(selectedOrderId);
            DialogUtils.showScrollableText(this, invoice,
                    "Invoice — Order #" + selectedOrderId);
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    // ── Table refresh ─────────────────────────────────────────────────────────
    public void refreshTable() {
        try {
            populateOrderTable(orderService.getAllOrders());
            loadCustomerCombo();
            loadItemCombo();
        } catch (Exception ex) {
            DialogUtils.showError(this, "Failed to load orders: " + ex.getMessage());
        }
    }

    private void populateOrderTable(List<Order> list) {
        orderTableModel.setRowCount(0);
        for (Order o : list) {
            orderTableModel.addRow(new Object[]{
                o.getOrderId(), o.getCustomerName(), o.getOrderDate(),
                o.getStatus().name(), o.getTotalAmount()
            });
        }
    }

    private void loadCustomerCombo() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerCombo.removeAllItems();
            for (Customer c : customers) {
                customerCombo.addItem(c.getId() + " - " + c.getName());
            }
        } catch (Exception ex) { /* ignore */ }
    }

    private void loadItemCombo() {
        try {
            List<Item> items = itemService.getAllItems();
            itemCombo.removeAllItems();
            for (Item i : items) {
                itemCombo.addItem(i.getSku() + " - " + i.getName()
                        + " ($" + String.format("%.2f", i.getUnitPrice()) + ")");
            }
        } catch (Exception ex) { /* ignore */ }
    }

    private void resetForm() {
        lineTableModel.setRowCount(0);
        couponField.setText("");
        currentDiscount = 0;
        recalculateBill();
        selectedOrderId = null;
        orderTable.clearSelection();
    }

    // ── Layout helpers ────────────────────────────────────────────────────────
    private JPanel labeledComponent(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UITheme.BG_CARD);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        p.add(UITheme.label(label), BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private Component vgap(int h) {
        JPanel g = new JPanel(); g.setBackground(UITheme.BG_CARD);
        g.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        g.setPreferredSize(new Dimension(0, h));
        return g;
    }

    private Component hline() {
        JSeparator s = UITheme.separator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    private JLabel hlineFull() {
        JLabel l = new JLabel(); l.setOpaque(true); l.setBackground(UITheme.BORDER_COLOR);
        l.setPreferredSize(new Dimension(0, 1));
        return l;
    }

    private JPanel rightAlign(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setBackground(UITheme.BG_CARD);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(c); return p;
    }

    private JLabel moneyLabel(String v) {
        JLabel l = new JLabel(v);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private JLabel summaryLabel(String v) {
        JLabel l = new JLabel(v);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_MUTED);
        return l;
    }

    private JLabel boldLabel(String v) {
        JLabel l = new JLabel(v);
        l.setFont(UITheme.FONT_HEADING);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }
}