package com.oop.project.ui;

import com.oop.project.model.*;
import com.oop.project.repository.OrderRepository;
import com.oop.project.service.BillingService;
import com.oop.project.service.CouponService;
import com.oop.project.service.CustomerService;
import com.oop.project.service.ItemService;

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

    private final MainFrame       mf;
    private final BillingService  billSvc;
    private final CustomerService custSvc;
    private final ItemService     itemSvc;
    private final CouponService   couponSvc;
    private final OrderRepository orderRepo = new OrderRepository(); // for view/delete

    // Order list (left side)
    private DefaultTableModel orderModel;
    private JTable            orderTable;
    private Integer           selectedOrderId = null;

    // Line items in create form (right side)
    private DefaultTableModel lineModel;
    private JTable            lineTable;

    // Form fields
    private JComboBox<CustomerItem>  custCombo;
    private JComboBox<ItemComboItem> itemCombo;
    private JSpinner                 qtySpinner;
    private JTextField               couponField;
    private JComboBox<String>        statusCombo;

    // Billing display labels
    private JLabel subtotalLbl, discountLbl, taxLbl, totalLbl;

    // Coupon applied to current form
    private Coupon appliedCoupon = null;

    private static final String[] ORDER_COLS =
        {"ID", "Customer", "Date", "Status", "Total"};
    private static final String[] LINE_COLS  =
        {"SKU", "Item Name", "Qty", "Unit Price", "Line Total"};

    public OrderPanel(MainFrame mf) {
        this.mf        = mf;
        this.billSvc   = mf.billingService;
        this.custSvc   = mf.customerService;
        this.itemSvc   = mf.itemService;
        this.couponSvc = mf.couponService;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildTop(),       BorderLayout.NORTH);
        add(buildSplitPane(), BorderLayout.CENTER);
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
            new String[]{"All Statuses", "PENDING", "PAID", "CANCELLED"});
        statusFilter.addActionListener(e -> {
            String sel = (String) statusFilter.getSelectedItem();
            applyFilter(sel, null, null, searchField.getText().trim());
        });
        searchField.addActionListener(e ->
            applyFilter((String) statusFilter.getSelectedItem(),
                        null, null, searchField.getText().trim()));

        JButton searchBtn = UITheme.primaryButton("Search");
        searchBtn.addActionListener(e ->
            applyFilter((String) statusFilter.getSelectedItem(),
                        null, null, searchField.getText().trim()));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UITheme.label("Status:"));
        right.add(statusFilter);
        right.add(UITheme.label("Search:"));
        right.add(searchField);
        right.add(searchBtn);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Split: left = list, right = create form ────────────────────────────────
    private JSplitPane buildSplitPane() {
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildOrderList(), buildCreateForm());
        sp.setDividerLocation(600);
        sp.setDividerSize(4);
        sp.setBorder(null);
        sp.setBackground(UITheme.BORDER_COLOR);
        return sp;
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
            if (!e.getValueIsAdjusting()) onOrderSelected();
        });
        // Double-click → show invoice
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showInvoice();
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
        JButton invoiceBtn = UITheme.ghostButton("View Invoice");
        JButton cancelBtn  = UITheme.dangerButton("Cancel Order");
        JButton deleteBtn  = UITheme.dangerButton("Delete");
        JButton updateSBtn = UITheme.ghostButton("Update Status →");
        invoiceBtn.addActionListener(e -> showInvoice());
        cancelBtn .addActionListener(e -> cancelOrder());
        deleteBtn .addActionListener(e -> deleteOrder());
        updateSBtn.addActionListener(e -> updateStatus());
        acts.add(invoiceBtn); acts.add(updateSBtn);
        acts.add(Box.createHorizontalStrut(8));
        acts.add(cancelBtn); acts.add(deleteBtn);
        p.add(acts, BorderLayout.SOUTH);
        return p;
    }

    // ── Right: create order form ───────────────────────────────────────────────
    private JPanel buildCreateForm() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel heading = UITheme.heading("Create New Order");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        // Customer combo
        custCombo = new JComboBox<>();
        custCombo.setFont(UITheme.FONT_BODY);
        custCombo.setBackground(UITheme.BG_INPUT);
        custCombo.setForeground(UITheme.TEXT_PRIMARY);

        // Item combo + qty
        itemCombo = new JComboBox<>();
        itemCombo.setFont(UITheme.FONT_BODY);
        itemCombo.setBackground(UITheme.BG_INPUT);
        itemCombo.setForeground(UITheme.TEXT_PRIMARY);
        qtySpinner = UITheme.styledSpinner(1, 9999, 1);

        JPanel itemRow = new JPanel(new BorderLayout(6, 0));
        itemRow.setBackground(UITheme.BG_CARD);
        itemRow.add(itemCombo, BorderLayout.CENTER);
        qtySpinner.setPreferredSize(new Dimension(70, 34));
        itemRow.add(qtySpinner, BorderLayout.EAST);

        JButton addLineBtn = UITheme.primaryButton("+ Add to Order");
        addLineBtn.addActionListener(e -> addLine());
        JPanel addLineRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        addLineRow.setBackground(UITheme.BG_CARD);
        addLineRow.add(addLineBtn);

        // Line items table
        lineModel = TableRenderer.model(LINE_COLS);
        lineTable = new JTable(lineModel);
        TableRenderer.applyAll(lineTable);
        lineTable.getColumnModel().getColumn(3).setCellRenderer(TableRenderer.currency());
        lineTable.getColumnModel().getColumn(4).setCellRenderer(TableRenderer.currency());
        TableRenderer.widths(lineTable, 90, 160, 50, 90, 90);
        JScrollPane lineScroll = UITheme.scrollPane(lineTable);
        lineScroll.setPreferredSize(new Dimension(0, 160));

        JButton removeLineBtn = UITheme.dangerButton("Remove Line");
        removeLineBtn.addActionListener(e -> removeLine());
        JPanel removeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        removeRow.setBackground(UITheme.BG_CARD);
        removeRow.add(removeLineBtn);

        // Coupon
        couponField = UITheme.styledTextField();
        JButton applyCouponBtn = UITheme.ghostButton("Apply");
        applyCouponBtn.addActionListener(e -> applyCoupon());
        JPanel couponRow = new JPanel(new BorderLayout(6, 0));
        couponRow.setBackground(UITheme.BG_CARD);
        couponRow.add(couponField, BorderLayout.CENTER);
        couponRow.add(applyCouponBtn, BorderLayout.EAST);

        // Status selector (for initial status — default PENDING)
        statusCombo = UITheme.styledComboBox(new String[]{"PENDING", "PAID", "CANCELLED"});

        // Billing summary
        JPanel summary = buildBillSummary();

        // Create button
        JButton createBtn = UITheme.successButton("✓  Create Order");
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        createBtn.addActionListener(e -> createOrder());

        JButton clearBtn = UITheme.ghostButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());

        // Assemble form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.BG_CARD);

        form.add(row("Customer *",     custCombo));
        form.add(vgap(8));
        form.add(row("Item  +  Qty", itemRow));
        form.add(addLineRow);
        form.add(vgap(4));
        form.add(lineScroll);
        form.add(removeRow);
        form.add(vgap(8));
        form.add(sep());
        form.add(vgap(6));
        form.add(row("Coupon Code",  couponRow));
        form.add(vgap(6));
        form.add(row("Initial Status", statusCombo));
        form.add(vgap(10));
        form.add(summary);
        form.add(vgap(12));
        createBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(createBtn);
        form.add(vgap(4));
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(clearBtn);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(UITheme.BG_CARD);
        formScroll.setBackground(UITheme.BG_CARD);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(formScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBillSummary() {
        JPanel p = new JPanel(new GridLayout(0, 2, 0, 6));
        p.setBackground(new Color(20, 25, 40));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        subtotalLbl = moneyLbl("$0.00");
        discountLbl = moneyLbl("$0.00"); discountLbl.setForeground(UITheme.DANGER);
        taxLbl      = moneyLbl("$0.00"); taxLbl.setForeground(UITheme.WARNING);
        totalLbl    = moneyLbl("$0.00");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLbl.setForeground(UITheme.ACCENT);

        p.add(UITheme.label("Subtotal:"));   p.add(subtotalLbl);
        p.add(UITheme.label("Discount:"));   p.add(discountLbl);
        p.add(UITheme.label("Tax (8%):"));   p.add(taxLbl);
        p.add(UITheme.heading("TOTAL:"));    p.add(totalLbl);
        return p;
    }

    // ── Form actions ──────────────────────────────────────────────────────────
    private void addLine() {
        ItemComboItem sel = (ItemComboItem) itemCombo.getSelectedItem();
        if (sel == null) return;
        int qty = (int) qtySpinner.getValue();
        BigDecimal price     = sel.item.getUnitPrice();
        BigDecimal lineTotal = billSvc.computeBill(price, qty);
        lineModel.addRow(new Object[]{
            sel.item.getItemSku(), sel.item.getItemName(), qty, price, lineTotal
        });
        recalc();
    }

    private void removeLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0) { UITheme.showError(this, "Select a line to remove."); return; }
        lineModel.removeRow(row);
        recalc();
    }

    private void applyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty()) { appliedCoupon = null; recalc(); return; }
        BigDecimal subtotal = currentSubtotal();
        try {
            appliedCoupon = couponSvc.validateCoupon(code, subtotal);
            UITheme.showSuccess(this, "Coupon \"" + code + "\" applied!");
            recalc();
        } catch (Exception ex) {
            appliedCoupon = null;
            UITheme.showError(this, ex.getMessage());
            recalc();
        }
    }

    /**
     * Real-time billing calculation — FR-6.4.
     * Uses the three overloaded computeBill() from BillingService.
     */
    private void recalc() {
        BigDecimal subtotal = currentSubtotal();
        BigDecimal discount = BigDecimal.ZERO;

        if (appliedCoupon != null) {
            if (appliedCoupon.getDiscountType() == DiscountType.Percent) {
                discount = subtotal
                    .multiply(appliedCoupon.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            } else {
                discount = appliedCoupon.getDiscountValue();
            }
            discount = discount.min(subtotal); // cannot exceed subtotal
        }

        // computeBill(price=subtotal-discount, qty=1, couponDiscount=0) to get after-discount subtotal
        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal tax           = afterDiscount.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total         = afterDiscount.add(tax);

        subtotalLbl.setText(String.format("$%.2f", subtotal));
        discountLbl.setText(String.format("-$%.2f", discount));
        taxLbl     .setText(String.format("+$%.2f", tax));
        totalLbl   .setText(String.format("$%.2f", total));
    }

    private BigDecimal currentSubtotal() {
        BigDecimal sub = BigDecimal.ZERO;
        for (int i = 0; i < lineModel.getRowCount(); i++) {
            Object v = lineModel.getValueAt(i, 4);
            if (v instanceof BigDecimal) sub = sub.add((BigDecimal) v);
        }
        return sub;
    }

    private void createOrder() {
        if (lineModel.getRowCount() == 0) {
            UITheme.showError(this, "Add at least one item to the order."); return;
        }
        CustomerItem custSel = (CustomerItem) custCombo.getSelectedItem();
        if (custSel == null) { UITheme.showError(this, "Select a customer."); return; }

        Order order = new Order();
        order.setCustomer(custSel.customer);
        order.setCoupon(appliedCoupon);
        order.setStatus(OrderStatus.valueOf((String) statusCombo.getSelectedItem()));

        List<OrderDetail> details = new ArrayList<>();
        for (int i = 0; i < lineModel.getRowCount(); i++) {
            String  sku   = (String)  lineModel.getValueAt(i, 0);
            int     qty   = (int)     lineModel.getValueAt(i, 2);
            BigDecimal p  = (BigDecimal) lineModel.getValueAt(i, 3);
            OrderDetail od = new OrderDetail();
            Item item = new Item(); item.setItemSku(sku);
            od.setItem(item); od.setQuantity(qty); od.setPriceAtTime(p);
            details.add(od);
        }
        order.setOrderItems(details);

        try {
            Order saved = billSvc.createOrder(order, mf.getCurrentUser());
            refresh();
            clearForm();
            UITheme.showSuccess(this, "Order #" + saved.getOrderId() + " created successfully.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void onOrderSelected() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { selectedOrderId = null; return; }
        selectedOrderId = (int) orderModel.getValueAt(row, 0);
        String status = (String) orderModel.getValueAt(row, 3);
        statusCombo.setSelectedItem(status);
    }

    private void updateStatus() {
        if (selectedOrderId == null) { UITheme.showError(this,"Select an order first."); return; }
        String newStatus = (String) statusCombo.getSelectedItem();
        try {
            orderRepo.updateStatus(selectedOrderId, newStatus);
            refresh();
            UITheme.showSuccess(this, "Status updated to " + newStatus + ".");
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    private void cancelOrder() {
        if (selectedOrderId == null) { UITheme.showError(this,"Select an order to cancel."); return; }
        if (!UITheme.confirm(this, "Cancel order #" + selectedOrderId + "? Stock will be restored.", "Confirm Cancel")) return;
        try {
            billSvc.cancelOrder(selectedOrderId, mf.getCurrentUser());
            refresh();
            UITheme.showSuccess(this, "Order #" + selectedOrderId + " cancelled.");
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    private void deleteOrder() {
        if (selectedOrderId == null) { UITheme.showError(this,"Select an order to delete."); return; }
        if (!UITheme.confirm(this, "Permanently delete order #" + selectedOrderId + "?", "Confirm Delete")) return;
        try {
            orderRepo.delete(selectedOrderId);
            refresh();
            UITheme.showSuccess(this, "Order deleted.");
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    private void showInvoice() {
        if (selectedOrderId == null) { UITheme.showError(this,"Select an order first."); return; }
        try {
            Order order = orderRepo.findById(selectedOrderId);
            if (order == null) { UITheme.showError(this, "Order not found."); return; }
            String invoice = billSvc.generateInvoice(order);
            UITheme.showScrollable(this, invoice, "Invoice — Order #" + selectedOrderId);
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    private void clearForm() {
        lineModel.setRowCount(0);
        couponField.setText("");
        appliedCoupon = null;
        statusCombo.setSelectedIndex(0);
        recalc();
        orderTable.clearSelection();
        selectedOrderId = null;
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    public void refresh() {
        try {
            populateOrderTable(orderRepo.findAll());
            loadCombos();
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
        } catch (Exception ex) { UITheme.showError(this, ex.getMessage()); }
    }

    private void populateOrderTable(List<Order> list) {
        orderModel.setRowCount(0);
        for (Order o : list) {
            String cname = o.getCustomer() != null ? o.getCustomer().getCustomerName() : "—";
            orderModel.addRow(new Object[]{
                o.getOrderId(), cname, o.getOrderDate(),
                o.getStatus() != null ? o.getStatus().name() : "—",
                o.getFinalTotal()
            });
        }
    }

    private void loadCombos() {
        custCombo.removeAllItems();
        try {
            for (Customer c : custSvc.getAllCustomers())
                custCombo.addItem(new CustomerItem(c));
        } catch (Exception ignored) {}

        itemCombo.removeAllItems();
        try {
            for (Item i : itemSvc.getAllItems())
                itemCombo.addItem(new ItemComboItem(i));
        } catch (Exception ignored) {}
    }

    // ── Layout helpers ────────────────────────────────────────────────────────
    private JPanel row(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UITheme.BG_CARD);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel l = UITheme.label(label); l.setFont(UITheme.FONT_SMALL);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }
    private Component vgap(int h) {
        JPanel g = new JPanel(); g.setBackground(UITheme.BG_CARD);
        g.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        g.setPreferredSize(new Dimension(0, h));
        g.setAlignmentX(Component.LEFT_ALIGNMENT);
        return g;
    }
    private Component sep() {
        JSeparator s = UITheme.separator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        return s;
    }
    private JLabel moneyLbl(String v) {
        JLabel l = new JLabel(v);
        l.setFont(UITheme.FONT_BODY); l.setForeground(UITheme.TEXT_PRIMARY);
        l.setHorizontalAlignment(SwingConstants.RIGHT); return l;
    }

    // ── Inner wrapper classes for combo boxes ─────────────────────────────────
    static class CustomerItem {
        final Customer customer;
        CustomerItem(Customer c) { this.customer = c; }
        public String toString() {
            return "[" + customer.getCustomerId() + "]  " + customer.getCustomerName();
        }
    }
    static class ItemComboItem {
        final Item item;
        ItemComboItem(Item i) { this.item = i; }
        public String toString() {
            String price = item.getUnitPrice() != null
                ? String.format("$%.2f", item.getUnitPrice().doubleValue()) : "$0.00";
            return item.getItemSku() + "  –  " + item.getItemName() + "  (" + price + ")";
        }
    }
}