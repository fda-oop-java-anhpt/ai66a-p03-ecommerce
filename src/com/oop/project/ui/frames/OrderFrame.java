package com.oop.project.ui.frames;

import com.oop.project.model.*;
import com.oop.project.repository.interfaces.SystemSettingRepository;
import com.oop.project.repository.impl.SystemSettingRepositoryImpl;
import com.oop.project.service.interfaces.*;
import com.oop.project.ui.panel.OrderPanel;
import com.oop.project.ui.utils.TableRenderer;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class OrderFrame extends JDialog {

    private final MainFrame mf;
    private final OrderPanel parentPanel;
    private final IBillingService billSvc;
    private final ICustomerService custSvc;
    private final IItemService itemSvc;
    private final ICouponService couponSvc;
    private final SystemSettingRepository settingRepo = new SystemSettingRepositoryImpl();

    private DefaultTableModel lineModel;
    private JTable lineTable;
    private JComboBox<CustomerItem> custCombo;
    private JComboBox<ItemComboItem> itemCombo;
    private JSpinner qtySpinner;
    private JTextField couponField;
    private JComboBox<String> statusCombo;
    private JLabel subtotalLbl, discountLbl, taxLbl, totalLbl, taxNameLbl;
    private Coupon appliedCoupon = null;

    private static final String[] LINE_COLS = { "SKU", "Item Name", "Qty", "Unit Price", "Line Total" };

    public OrderFrame(MainFrame mf, OrderPanel parentPanel) {
        super(mf, "Create New Order", true);
        this.mf = mf;
        this.parentPanel = parentPanel;
        this.billSvc = mf.billingService;
        this.custSvc = mf.customerService;
        this.itemSvc = mf.itemService;
        this.couponSvc = mf.couponService;

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        getContentPane().add(buildCreateForm());
        pack();
        setSize(new Dimension(500, 750));
        setLocationRelativeTo(parentPanel);
    }

    public void showForm() {
        loadCombos();
        clearForm();
        setVisible(true);
    }

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
        custCombo.setForeground(UITheme.TEXT_DARK);

        // Item combo + qty
        itemCombo = new JComboBox<>();
        itemCombo.setFont(UITheme.FONT_BODY);
        itemCombo.setBackground(UITheme.BG_INPUT);
        itemCombo.setForeground(UITheme.TEXT_DARK);
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
        statusCombo = UITheme.styledComboBox(new String[] { "PENDING", "PAID" });

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

        form.add(row("Customer *", custCombo));
        form.add(vgap(8));
        form.add(row("Item  +  Qty", itemRow));
        form.add(addLineRow);
        form.add(vgap(4));
        form.add(lineScroll);
        form.add(removeRow);
        form.add(vgap(8));
        form.add(sep());
        form.add(vgap(6));
        form.add(row("Coupon Code", couponRow));
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

        subtotalLbl = moneyLbl("0 VNĐ");
        discountLbl = moneyLbl("0 VNĐ");
        discountLbl.setForeground(UITheme.DANGER);
        taxLbl = moneyLbl("0 VNĐ");
        taxLbl.setForeground(UITheme.WARNING);
        totalLbl = moneyLbl("0 VNĐ");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLbl.setForeground(UITheme.ACCENT);

        taxNameLbl = UITheme.label("Tax:");

        p.add(UITheme.label("Subtotal:"));
        p.add(subtotalLbl);
        p.add(UITheme.label("Discount:"));
        p.add(discountLbl);
        p.add(taxNameLbl);
        p.add(taxLbl);
        p.add(UITheme.heading("TOTAL:"));
        p.add(totalLbl);
        return p;
    }

    private void addLine() {
        ItemComboItem sel = (ItemComboItem) itemCombo.getSelectedItem();
        if (sel == null)
            return;
        int qty = (int) qtySpinner.getValue();
        BigDecimal price = sel.item.getUnitPrice();
        // fix duplicate item in order
        String sku = sel.item.getItemSku();
        for (int i = 0; i < lineModel.getRowCount(); i++) {
            if (sku.equals(lineModel.getValueAt(i, 0))) {
                int existingQty = (int) lineModel.getValueAt(i, 2);
                int newQty = existingQty + qty;
                BigDecimal newLineTotal = billSvc.computeBill(price, newQty);
                lineModel.setValueAt(newQty, i, 2);
                lineModel.setValueAt(newLineTotal, i, 4);
                recalc();
                return;
            }
        }

        BigDecimal lineTotal = billSvc.computeBill(price, qty);
        lineModel.addRow(new Object[] {
                sku, sel.item.getItemName(), qty, price, lineTotal
        });
        recalc();
    }

    private void removeLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0) {
            UITheme.showError(this, "Select a line to remove.");
            return;
        }
        lineModel.removeRow(row);
        recalc();
    }

    private void applyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty()) {
            appliedCoupon = null;
            recalc();
            return;
        }
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

    private void recalc() {
        if (subtotalLbl == null) return;
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

        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal taxRate = getTaxRate();
        BigDecimal tax = afterDiscount.multiply(taxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(tax);

        subtotalLbl.setText(String.format("%,.0f VNĐ", subtotal));
        discountLbl.setText(String.format("-%,.0f VNĐ", discount));
        taxNameLbl.setText(String.format("Tax (%s%%):", taxRate.stripTrailingZeros().toPlainString()));
        taxLbl.setText(String.format("+%,.0f VNĐ", tax));
        totalLbl.setText(String.format("%,.0f VNĐ", total));
    }

    private BigDecimal getTaxRate() {
        SystemSetting setting = settingRepo.findByKey("TAX_RATE");
        if (setting != null && setting.getSettingValue() != null) {
            try {
                return new BigDecimal(setting.getSettingValue());
            } catch (NumberFormatException ignored) {
            }
        }
        return new BigDecimal("8.00");
    }

    private BigDecimal currentSubtotal() {
        if (lineModel == null) return BigDecimal.ZERO;
        BigDecimal sub = BigDecimal.ZERO;
        for (int i = 0; i < lineModel.getRowCount(); i++) {
            Object v = lineModel.getValueAt(i, 4);
            if (v instanceof BigDecimal)
                sub = sub.add((BigDecimal) v);
        }
        return sub;
    }

    private void createOrder() {
        if (lineModel == null || lineModel.getRowCount() == 0) {
            UITheme.showError(this, "Add at least one item to the order.");
            return;
        }
        CustomerItem custSel = (CustomerItem) custCombo.getSelectedItem();
        if (custSel == null) {
            UITheme.showError(this, "Select a customer.");
            return;
        }

        Order order = new Order();
        order.setCustomer(custSel.customer);
        order.setCoupon(appliedCoupon);
        order.setStatus(OrderStatus.valueOf((String) statusCombo.getSelectedItem()));

        List<OrderDetail> details = new ArrayList<>();
        for (int i = 0; i < lineModel.getRowCount(); i++) {
            String sku = (String) lineModel.getValueAt(i, 0);
            int qty = (int) lineModel.getValueAt(i, 2);
            BigDecimal p = (BigDecimal) lineModel.getValueAt(i, 3);
            OrderDetail od = new OrderDetail();
            Item item = new Item();
            item.setItemSku(sku);
            od.setItem(item);
            od.setQuantity(qty);
            od.setPriceAtTime(p);
            details.add(od);
        }
        order.setOrderItems(details);

        try {
            Order saved = billSvc.createOrder(order, mf.getCurrentUser());
            setVisible(false);
            parentPanel.onOrderCreated(); // Note: we need to add this to OrderPanel
            clearForm();
            UITheme.showSuccess(parentPanel, "Order #" + saved.getOrderId() + " created successfully.");
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void clearForm() {
        if (lineModel != null) lineModel.setRowCount(0);
        if (couponField != null) couponField.setText("");
        appliedCoupon = null;
        if (statusCombo != null) {
            statusCombo.setSelectedIndex(0);
            statusCombo.setEnabled(true);
        }
        recalc();
    }

    private void loadCombos() {
        if (custCombo == null || itemCombo == null) return;
        custCombo.removeAllItems();
        try {
            for (Customer c : custSvc.getAllCustomers())
                custCombo.addItem(new CustomerItem(c));
        } catch (Exception ignored) {
        }

        itemCombo.removeAllItems();
        try {
            for (Item i : itemSvc.getAllItems())
                itemCombo.addItem(new ItemComboItem(i));
        } catch (Exception ignored) {
        }
    }

    private JPanel row(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UITheme.BG_CARD);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel l = UITheme.label(label);
        l.setFont(UITheme.FONT_SMALL);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private Component vgap(int h) {
        JPanel g = new JPanel();
        g.setBackground(UITheme.BG_CARD);
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
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    static class CustomerItem {
        final Customer customer;

        CustomerItem(Customer c) {
            this.customer = c;
        }

        public String toString() {
            return "[" + customer.getCustomerId() + "]  " + customer.getCustomerName();
        }
    }

    static class ItemComboItem {
        final Item item;

        ItemComboItem(Item i) {
            this.item = i;
        }

        public String toString() {
            String price = item.getUnitPrice() != null
                    ? String.format("%,.0f VNĐ", item.getUnitPrice().doubleValue())
                    : "0 VNĐ";
            return item.getItemName() + "  (" + price + ")  | Tồn: " + item.getStockQuantity();
        }
    }
}
