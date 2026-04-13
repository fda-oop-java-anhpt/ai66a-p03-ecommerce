package com.oop.project.ui.frames;

import com.oop.project.model.*;
import com.oop.project.repository.interfaces.SystemSettingRepository;
import com.oop.project.repository.impl.SystemSettingRepositoryImpl;
import com.oop.project.service.interfaces.IBillingService;
import com.oop.project.service.interfaces.ICouponService;
import com.oop.project.service.interfaces.ICustomerService;
import com.oop.project.service.interfaces.IItemService;
import com.oop.project.ui.utils.TableRenderer;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Main frame for creating a new order.
 * Layout: GridBagLayout for even alignment; pack() is called AFTER data loads.
 * Default initial status: PENDING.
 * Category combo filters item list.
 */
public class OrderFrame extends JFrame {

    private final IBillingService billSvc;
    private final ICustomerService custSvc;
    private final IItemService itemSvc;
    private final ICouponService couponSvc;
    private final SystemSettingRepository settingRepo = new SystemSettingRepositoryImpl();
    private final User currentUser;

    public interface OrderCreatedListener {
        void onOrderCreated(Order order);
    }
    private final OrderCreatedListener listener;

    // Result
    private Order createdOrder = null;

    // All loaded data (for category filtering)
    private List<Item> allItems = new ArrayList<>();

    // Form fields
    private JComboBox<CustomerItem> custCombo;
    private JComboBox<String> categoryCombo;
    private JComboBox<ItemComboItem> itemCombo;
    private JSpinner qtySpinner;
    private JTextField couponField;

    // Line items table
    private DefaultTableModel lineModel;
    private JTable lineTable;

    // Billing labels
    private JLabel subtotalLbl, discountLbl, taxLbl, totalLbl, taxNameLbl;

    // Applied coupon
    private Coupon appliedCoupon = null;

    private static final String ALL_CAT = "All Categories";
    private static final String[] LINE_COLS = { "SKU", "Item Name", "Qty", "Unit Price", "Line Total" };

    // ─────────────────────────────────────────────────────────────────────────
    public OrderFrame(Window owner,
            IBillingService billSvc,
            ICustomerService custSvc,
            IItemService itemSvc,
            ICouponService couponSvc,
            User currentUser,
            OrderCreatedListener listener) {
        super("Create New Order");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.billSvc = billSvc;
        this.custSvc = custSvc;
        this.itemSvc = itemSvc;
        this.couponSvc = couponSvc;
        this.currentUser = currentUser;
        this.listener = listener;

        buildUI(); // build skeleton (no pack yet)
        loadCombos(); // fill combos with data
        recalc(); // initial billing display
        pack(); // size AFTER content is loaded → correct fit
        setLocationRelativeTo(owner);
    }

    // ── Build UI ──────────────────────────────────────────────────────────────
    private void buildUI() {
        setResizable(true);

        // ── Root ──────────────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        setContentPane(root);

        // ── Header ────────────────────────────────────────────────────────────
        JLabel heading = UITheme.heading("Create New Order");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        root.add(heading, BorderLayout.NORTH);

        // ── Form (GridBagLayout → uniform left-edge, fills width) ─────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 4, 0);

        // ── Customer ──────────────────────────────────────────────────────────
        custCombo = newCombo();
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 2, 0);
        form.add(smallLabel("Customer *"), gc);
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 10, 0);
        form.add(custCombo, gc);

        // ── Category filter ───────────────────────────────────────────────────
        categoryCombo = UITheme.styledComboBox(new String[] { ALL_CAT });
        categoryCombo.addActionListener(e -> applyItemFilter());

        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 2, 0);
        form.add(smallLabel("Filter by Category"), gc);
        gc.gridy = 3;
        gc.insets = new Insets(0, 0, 6, 0);
        form.add(categoryCombo, gc);

        // ── Item + Qty ────────────────────────────────────────────────────────
        itemCombo = newItemCombo();
        qtySpinner = UITheme.styledSpinner(1, 9999, 1);

        JPanel itemQtyRow = new JPanel(new BorderLayout(6, 0));
        itemQtyRow.setBackground(UITheme.BG_CARD);
        itemQtyRow.add(itemCombo, BorderLayout.CENTER);
        itemQtyRow.add(qtySpinner, BorderLayout.EAST);
        qtySpinner.setPreferredSize(new Dimension(72, itemCombo.getPreferredSize().height));

        JButton addLineBtn = UITheme.primaryButton("+ Add to Order");
        addLineBtn.addActionListener(e -> addLine());
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        addRow.setBackground(UITheme.BG_CARD);
        addRow.add(addLineBtn);

        gc.gridy = 4;
        gc.insets = new Insets(0, 0, 2, 0);
        form.add(smallLabel("Item  +  Qty"), gc);
        gc.gridy = 5;
        gc.insets = new Insets(0, 0, 2, 0);
        form.add(itemQtyRow, gc);
        gc.gridy = 6;
        gc.insets = new Insets(0, 0, 6, 0);
        form.add(addRow, gc);

        // ── Line items table ──────────────────────────────────────────────────
        lineModel = TableRenderer.model(LINE_COLS);
        lineTable = new JTable(lineModel);
        TableRenderer.applyAll(lineTable);
        lineTable.getColumnModel().getColumn(3).setCellRenderer(TableRenderer.currency());
        lineTable.getColumnModel().getColumn(4).setCellRenderer(TableRenderer.currency());
        TableRenderer.widths(lineTable, 85, 155, 48, 88, 88);

        JScrollPane lineScroll = UITheme.scrollPane(lineTable);
        lineScroll.setPreferredSize(new Dimension(0, 155));

        JButton removeLineBtn = UITheme.dangerButton("Remove Line");
        removeLineBtn.addActionListener(e -> removeLine());
        JPanel removeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        removeRow.setBackground(UITheme.BG_CARD);
        removeRow.add(removeLineBtn);

        gc.gridy = 7;
        gc.insets = new Insets(0, 0, 0, 0);
        form.add(lineScroll, gc);
        gc.gridy = 8;
        gc.insets = new Insets(2, 0, 8, 0);
        form.add(removeRow, gc);

        // ── Separator ─────────────────────────────────────────────────────────
        gc.gridy = 9;
        gc.insets = new Insets(0, 0, 8, 0);
        form.add(UITheme.separator(), gc);

        // ── Coupon ────────────────────────────────────────────────────────────
        couponField = UITheme.styledTextField();
        JButton applyBtn = UITheme.ghostButton("Apply");
        applyBtn.addActionListener(e -> applyCoupon());
        JPanel couponRow = new JPanel(new BorderLayout(6, 0));
        couponRow.setBackground(UITheme.BG_CARD);
        couponRow.add(couponField, BorderLayout.CENTER);
        couponRow.add(applyBtn, BorderLayout.EAST);

        gc.gridy = 10;
        gc.insets = new Insets(0, 0, 2, 0);
        form.add(smallLabel("Coupon Code"), gc);
        gc.gridy = 11;
        gc.insets = new Insets(0, 0, 10, 0);
        form.add(couponRow, gc);

        // ── Initial Status: fixed PENDING ─────────────────────────────────────
        JLabel statusLabel = new JLabel("PENDING");
        statusLabel.setFont(UITheme.FONT_BADGE);
        statusLabel.setForeground(UITheme.WARNING);
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.WARNING, 1),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));

        gc.gridy = 12;
        gc.insets = new Insets(0, 0, 2, 0);
        form.add(smallLabel("Initial Status"), gc);
        gc.gridy = 13;
        gc.insets = new Insets(0, 0, 10, 0);
        form.add(statusLabel, gc);

        // ── Billing summary ───────────────────────────────────────────────────
        gc.gridy = 14;
        gc.insets = new Insets(0, 0, 0, 0);
        form.add(buildBillSummary(), gc);

        // Wrap form in scroll pane (safe for small screens)
        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(UITheme.BG_CARD);
        formScroll.setBackground(UITheme.BG_CARD);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(formScroll, BorderLayout.CENTER);

        // ── Bottom button bar ─────────────────────────────────────────────────
        JButton createBtn = UITheme.successButton("Create Order");
        JButton clearBtn = UITheme.ghostButton("Clear Form");
        JButton cancelBtn = UITheme.dangerButton("Cancel");
        createBtn.addActionListener(e -> onCreateOrder());
        clearBtn.addActionListener(e -> clearForm());
        cancelBtn.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(createBtn);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setBackground(UITheme.BG_CARD);
        btnBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)));
        btnBar.add(cancelBtn);
        btnBar.add(clearBtn);
        btnBar.add(createBtn);
        root.add(btnBar, BorderLayout.SOUTH);

        // Fixed dialog width; height determined by pack() after data loads
        setMinimumSize(new Dimension(600, 500));
    }

    // ── Bill summary ──────────────────────────────────────────────────────────
    private JPanel buildBillSummary() {
        JPanel p = new JPanel(new GridLayout(0, 2, 0, 6));
        p.setBackground(new Color(20, 25, 40));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

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

    // ── Data loading ──────────────────────────────────────────────────────────
    private void loadCombos() {
        // Customers
        custCombo.removeAllItems();
        try {
            for (Customer c : custSvc.getAllCustomers())
                custCombo.addItem(new CustomerItem(c));
        } catch (Exception ignored) {
        }

        // Items + build category list
        allItems = new ArrayList<>();
        try {
            allItems = itemSvc.getAllItems();
        } catch (Exception ignored) {
        }

        Set<String> cats = new LinkedHashSet<>();
        cats.add(ALL_CAT);
        for (Item i : allItems) {
            if (i.getCategory() != null && !i.getCategory().isBlank())
                cats.add(i.getCategory());
        }
        categoryCombo.setModel(new DefaultComboBoxModel<>(cats.toArray(new String[0])));
        categoryCombo.setSelectedItem(ALL_CAT);

        // Populate item combo (all categories)
        rebuildItemCombo(ALL_CAT);
    }

    /** Refresh item combo based on selected category. */
    private void applyItemFilter() {
        String cat = (String) categoryCombo.getSelectedItem();
        rebuildItemCombo(cat == null ? ALL_CAT : cat);
    }

    private void rebuildItemCombo(String category) {
        itemCombo.removeAllItems();
        for (Item i : allItems) {
            if (ALL_CAT.equals(category) || category.equals(i.getCategory()))
                itemCombo.addItem(new ItemComboItem(i));
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void addLine() {
        ItemComboItem sel = (ItemComboItem) itemCombo.getSelectedItem();
        if (sel == null) {
            UITheme.showError(this, "Select an item.");
            return;
        }
        int qty = (int) qtySpinner.getValue();
        BigDecimal price = sel.item.getUnitPrice();
        String sku = sel.item.getItemSku();
        for (int i = 0; i < lineModel.getRowCount(); i++) {
            if (sku.equals(lineModel.getValueAt(i, 0))) {
                int newQty = (int) lineModel.getValueAt(i, 2) + qty;
                lineModel.setValueAt(newQty, i, 2);
                lineModel.setValueAt(billSvc.computeBill(price, newQty), i, 4);
                recalc();
                return;
            }
        }
        lineModel.addRow(new Object[] { sku, sel.item.getItemName(), qty, price,
                billSvc.computeBill(price, qty) });
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
        try {
            appliedCoupon = couponSvc.validateCoupon(code, currentSubtotal());
            UITheme.showSuccess(this, "Coupon \"" + code + "\" applied!");
            recalc();
        } catch (Exception ex) {
            appliedCoupon = null;
            UITheme.showError(this, ex.getMessage());
            recalc();
        }
    }

    private void onCreateOrder() {
        if (lineModel.getRowCount() == 0) {
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
        order.setStatus(OrderStatus.PENDING);

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
            createdOrder = billSvc.createOrder(order, currentUser);
            if (listener != null) {
                listener.onOrderCreated(createdOrder);
            }
            dispose();
        } catch (Exception ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void clearForm() {
        lineModel.setRowCount(0);
        couponField.setText("");
        appliedCoupon = null;
        // status is always PENDING — no reset needed
        categoryCombo.setSelectedItem(ALL_CAT);
        rebuildItemCombo(ALL_CAT);
        recalc();
    }

    // ── Billing calculation ───────────────────────────────────────────────────
    private void recalc() {
        BigDecimal subtotal = currentSubtotal();
        BigDecimal discount = BigDecimal.ZERO;
        if (appliedCoupon != null) {
            if (appliedCoupon.getDiscountType() == DiscountType.Percent) {
                discount = subtotal.multiply(appliedCoupon.getDiscountValue())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            } else {
                discount = appliedCoupon.getDiscountValue();
            }
            discount = discount.min(subtotal);
        }
        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal taxRate = getTaxRate();
        BigDecimal tax = afterDiscount.multiply(taxRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(tax);

        subtotalLbl.setText(String.format("%,.0f VNĐ", subtotal));
        discountLbl.setText(String.format("-%,.0f VNĐ", discount));
        taxNameLbl.setText(String.format("Tax (%s%%):", taxRate.stripTrailingZeros().toPlainString()));
        taxLbl.setText(String.format("+%,.0f VNĐ", tax));
        totalLbl.setText(String.format("%,.0f VNĐ", total));
    }

    private BigDecimal getTaxRate() {
        SystemSetting s = settingRepo.findByKey("TAX_RATE");
        if (s != null && s.getSettingValue() != null) {
            try {
                return new BigDecimal(s.getSettingValue());
            } catch (NumberFormatException ignored) {
            }
        }
        return new BigDecimal("8.00");
    }

    private BigDecimal currentSubtotal() {
        BigDecimal sub = BigDecimal.ZERO;
        for (int i = 0; i < lineModel.getRowCount(); i++) {
            Object v = lineModel.getValueAt(i, 4);
            if (v instanceof BigDecimal)
                sub = sub.add((BigDecimal) v);
        }
        return sub;
    }

    // ── Combo factory helpers ─────────────────────────────────────────────────
    private static <T> JComboBox<T> newCombo() {
        JComboBox<T> cb = new JComboBox<>();
        cb.setFont(UITheme.FONT_BODY);
        cb.setBackground(UITheme.BG_INPUT);
        cb.setForeground(UITheme.TEXT_DARK);
        styleComboRenderer(cb);
        return cb;
    }

    private static JComboBox<ItemComboItem> newItemCombo() {
        JComboBox<ItemComboItem> cb = new JComboBox<>();
        cb.setFont(UITheme.FONT_BODY);
        cb.setBackground(UITheme.BG_INPUT);
        cb.setForeground(UITheme.TEXT_DARK);
        styleComboRenderer(cb);
        return cb;
    }

    private static void styleComboRenderer(JComboBox<?> cb) {
        cb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
                l.setFont(UITheme.FONT_BODY);
                l.setBackground(sel ? UITheme.ACCENT_DARK : UITheme.BG_INPUT);
                l.setForeground(UITheme.TEXT_PRIMARY);
                l.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return l;
            }
        });
        cb.setMaximumRowCount(10);
    }

    // ── Layout helpers ────────────────────────────────────────────────────────
    private static JLabel smallLabel(String text) {
        JLabel l = UITheme.label(text);
        l.setFont(UITheme.FONT_SMALL);
        return l;
    }

    private JLabel moneyLbl(String v) {
        JLabel l = new JLabel(v);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    public Order getCreatedOrder() {
        return createdOrder;
    }

    // ── Inner combo wrapper classes ───────────────────────────────────────────
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
            return item.getItemSku() + "  –  " + item.getItemName() + " | Stock: " + item.getStockQuantity();
        }
    }
}