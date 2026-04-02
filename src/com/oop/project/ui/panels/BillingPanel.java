package com.oop.project.ui.panels;

import com.oop.project.service.interfaces.BillingService;
import com.oop.project.service.interfaces.CouponService;
import com.oop.project.service.interfaces.OrderService;
import com.oop.project.ui.utils.DialogUtils;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

/**
 * Billing Calculator tab — FR-3.3 (3 overloaded methods), FR-3.4 (8% tax),
 * FR-6.4 (real-time update).
 */
public class BillingPanel extends JPanel {

    private final BillingService billingService;
    private final CouponService  couponService;
    private final OrderService   orderService;

    // Input fields
    private JTextField priceField, quantityField, couponField, orderIdField;

    // Output labels
    private JLabel subtotalLbl, discountLbl, taxLbl, totalLbl;
    private JLabel modeLabel;

    // Invoice area
    private JTextArea invoiceArea;

    public BillingPanel(BillingService billingService, CouponService couponService,
                        OrderService orderService) {
        this.billingService = billingService;
        this.couponService  = couponService;
        this.orderService   = orderService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_DARK);

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildContent(),   BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        p.add(UITheme.title("Billing Calculator"), BorderLayout.WEST);
        return p;
    }

    private JPanel buildContent() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 16);

        c.gridx = 0; c.gridy = 0; c.weightx = 0.4; c.weighty = 1;
        p.add(buildInputCard(), c);

        c.gridx = 1; c.weightx = 0.3;
        p.add(buildSummaryCard(), c);

        c.gridx = 2; c.weightx = 0.3; c.insets = new Insets(0, 0, 0, 0);
        p.add(buildInvoiceCard(), c);

        return p;
    }

    // ── Left card: inputs ────────────────────────────────────────────────────
    private JPanel buildInputCard() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = UITheme.heading("Compute Bill");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(heading);

        // Mode indicator
        modeLabel = new JLabel("Mode: computeBill(price)");
        modeLabel.setFont(UITheme.FONT_BADGE);
        modeLabel.setForeground(UITheme.ACCENT);
        modeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        card.add(modeLabel);

        priceField    = UITheme.styledTextField();
        quantityField = UITheme.styledTextField();
        couponField   = UITheme.styledTextField();
        orderIdField  = UITheme.styledTextField();

        // Real-time update on any key release (FR-6.4)
        KeyAdapter rt = new KeyAdapter() {
            public void keyReleased(KeyEvent e) { recalculate(); }
        };
        priceField   .addKeyListener(rt);
        quantityField.addKeyListener(rt);
        couponField  .addKeyListener(rt);

        card.add(labeledField("Unit Price ($)",   priceField));
        card.add(vgap(8));
        card.add(labeledField("Quantity",          quantityField));
        card.add(vgap(8));
        card.add(labeledField("Coupon Code",       couponField));
        card.add(vgap(16));

        // 3 overloaded method buttons (FR-3.3)
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        btnPanel.setBackground(UITheme.BG_CARD);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton b1 = UITheme.ghostButton("computeBill(price)");
        JButton b2 = UITheme.ghostButton("computeBill(price, qty)");
        JButton b3 = UITheme.primaryButton("computeBill(price, qty, coupon)");

        b1.addActionListener(e -> computeMode1());
        b2.addActionListener(e -> computeMode2());
        b3.addActionListener(e -> computeMode3());

        btnPanel.add(b1);
        btnPanel.add(b2);
        btnPanel.add(b3);
        card.add(btnPanel);

        card.add(vgap(16));
        card.add(UITheme.separator());
        card.add(vgap(12));

        // Invoice generator by order ID
        JLabel invTitle = UITheme.heading("Generate Invoice");
        invTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        card.add(invTitle);
        card.add(labeledField("Order ID", orderIdField));
        card.add(vgap(8));
        JButton genBtn = UITheme.successButton("Generate Invoice");
        genBtn.addActionListener(e -> generateInvoice());
        genBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(genBtn);

        return card;
    }

    // ── Middle card: billing summary ─────────────────────────────────────────
    private JPanel buildSummaryCard() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BorderLayout());

        JLabel heading = UITheme.heading("Bill Summary");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(heading, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(0, 2, 0, 16));
        rows.setBackground(UITheme.BG_CARD);

        subtotalLbl = bigMoneyLabel("$0.00", UITheme.TEXT_PRIMARY);
        discountLbl = bigMoneyLabel("-$0.00", UITheme.DANGER);
        taxLbl      = bigMoneyLabel("+$0.00", UITheme.WARNING);
        totalLbl    = bigMoneyLabel("$0.00", UITheme.ACCENT);
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));

        rows.add(summaryLabel("Subtotal"));     rows.add(subtotalLbl);
        rows.add(summaryLabel("Discount"));     rows.add(discountLbl);
        rows.add(summaryLabel("Tax (8%)"));     rows.add(taxLbl);

        JSeparator sep = UITheme.separator();
        rows.add(sep); rows.add(new JLabel());

        JLabel totalKey = new JLabel("TOTAL");
        totalKey.setFont(UITheme.FONT_HEADING);
        totalKey.setForeground(UITheme.TEXT_PRIMARY);
        rows.add(totalKey); rows.add(totalLbl);

        card.add(rows, BorderLayout.CENTER);

        // Tax note
        JLabel taxNote = UITheme.label("8% tax applied to all orders.");
        taxNote.setFont(UITheme.FONT_SMALL);
        taxNote.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        card.add(taxNote, BorderLayout.SOUTH);

        return card;
    }

    // ── Right card: invoice preview ──────────────────────────────────────────
    private JPanel buildInvoiceCard() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BorderLayout());

        JLabel heading = UITheme.heading("Invoice Preview");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(heading, BorderLayout.NORTH);

        invoiceArea = UITheme.styledTextArea();
        invoiceArea.setFont(UITheme.FONT_MONO);
        invoiceArea.setEditable(false);
        invoiceArea.setText("Invoice will appear here\nafter clicking Generate Invoice.");
        invoiceArea.setForeground(UITheme.TEXT_MUTED);

        JScrollPane sp = UITheme.styledScrollPane(invoiceArea);
        card.add(sp, BorderLayout.CENTER);

        JButton copyBtn = UITheme.ghostButton("Copy to Clipboard");
        copyBtn.addActionListener(e -> {
            java.awt.datatransfer.StringSelection sel =
                    new java.awt.datatransfer.StringSelection(invoiceArea.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            DialogUtils.showSuccess(this, "Invoice copied to clipboard.");
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        bottom.setBackground(UITheme.BG_CARD);
        bottom.add(copyBtn);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    // ── Compute modes (FR-3.3) ────────────────────────────────────────────────
    private void computeMode1() {
        double price = parseDouble(priceField.getText(), 0);
        BigDecimal total = billingService.computeBill(BigDecimal.valueOf(price));
        modeLabel.setText("Mode: computeBill(price)");
        updateSummary(price, 1, 0, total.doubleValue());
    }

    private void computeMode2() {
        double price = parseDouble(priceField.getText(), 0);
        int    qty   = parseInt(quantityField.getText(), 1);
        BigDecimal total = billingService.computeBill(BigDecimal.valueOf(price), qty);
        modeLabel.setText("Mode: computeBill(price, quantity)");
        updateSummary(price * qty, qty, 0, total.doubleValue());
    }

    private void computeMode3() {
        double price = parseDouble(priceField.getText(), 0);
        int    qty   = parseInt(quantityField.getText(), 1);
        String code  = couponField.getText().trim();
        double discount = 0;
        if (!code.isEmpty()) {
            try {
                if (!couponService.validateCoupon(code)) {
                    DialogUtils.showError(this, "Coupon is invalid or expired.");
                    return;
                }
                discount = couponService.getDiscountAmount(code, price * qty);
            } catch (Exception ex) {
                DialogUtils.showError(this, ex.getMessage());
                return;
            }
        }
        BigDecimal total = billingService.computeBill(BigDecimal.valueOf(price), qty, BigDecimal.valueOf(discount));
        modeLabel.setText("Mode: computeBill(price, qty, couponDiscount)");
        updateSummary(price * qty, qty, discount, total.doubleValue());
    }

    // Real-time calculation triggered by keystrokes (FR-6.4)
    private void recalculate() {
        if (!priceField.getText().trim().isEmpty()) {
            computeMode3();
        }
    }

    private void updateSummary(double subtotal, int qty, double discount, double total) {
        double tax = total - (subtotal - discount);
        subtotalLbl.setText(String.format("$%.2f", subtotal));
        discountLbl.setText(String.format("-$%.2f", discount));
        taxLbl     .setText(String.format("+$%.2f", tax));
        totalLbl   .setText(String.format("$%.2f", total));
    }

    private void generateInvoice() {
        String idStr = orderIdField.getText().trim();
        if (idStr.isEmpty()) {
            DialogUtils.showError(this, "Enter an Order ID.");
            return;
        }
        try {
            int orderId = Integer.parseInt(idStr);
            orderService.getOrderById(orderId).ifPresentOrElse(order -> {
                String invoice = billingService.generateInvoice(order);
                invoiceArea.setForeground(UITheme.TEXT_PRIMARY);
                invoiceArea.setText(invoice);
                invoiceArea.setCaretPosition(0);
            }, () -> {
                DialogUtils.showError(this, "Order not found: " + orderId);
            });
        } catch (NumberFormatException ex) {
            DialogUtils.showError(this, "Order ID must be a number.");
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    public void reset() {
        priceField.setText("");
        quantityField.setText("");
        couponField.setText("");
        orderIdField.setText("");
        subtotalLbl.setText("$0.00");
        discountLbl.setText("-$0.00");
        taxLbl.setText("+$0.00");
        totalLbl.setText("$0.00");
        modeLabel.setText("Mode: computeBill(price)");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JPanel labeledField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UITheme.BG_CARD);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        p.add(UITheme.label(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private Component vgap(int h) {
        JPanel g = new JPanel(); g.setBackground(UITheme.BG_CARD);
        g.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        g.setPreferredSize(new Dimension(0, h));
        g.setAlignmentX(Component.LEFT_ALIGNMENT);
        return g;
    }

    private JLabel bigMoneyLabel(String val, Color color) {
        JLabel l = new JLabel(val);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(color);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private JLabel summaryLabel(String text) {
        JLabel l = UITheme.label(text);
        l.setFont(UITheme.FONT_BODY);
        return l;
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }
    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
}