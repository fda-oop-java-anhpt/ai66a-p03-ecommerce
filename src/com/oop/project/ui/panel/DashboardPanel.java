package com.oop.project.ui.panel;

import com.oop.project.model.Order;
import com.oop.project.service.interfaces.IDashboardService;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.ui.frames.MainFrame;
import com.oop.project.ui.utils.TableRenderer;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Dashboard tab — FR-5.
 * FR-5.1 sortable order list (DashboardService.getAllOrders with sort).
 * FR-5.2 filter by status / date range.
 * FR-5.3 search by customer name or order ID.
 * FR-5.4 summary statistics.
 * Also shows Audit Log section (FR-4.4).
 */
public class DashboardPanel extends JPanel {

    private final IDashboardService dashSvc;

    // KPI labels
    private JLabel totalOrdersVal, revenueVal, cancelledVal, pendingVal;

    // Orders table
    private DefaultTableModel orderModel;
    private JTable orderTable;

    // Filter controls
    private JComboBox<String> statusFilter, sortByCombo, sortDirCombo;
    private JTextField searchField;

    private static final String[] ORDER_COLS = { "ID", "Customer", "Date", "Status", "Total (VNĐ)" };

    public DashboardPanel(MainFrame mf) {
        this.dashSvc = mf.dashboardService;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildTop(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        refresh();
    }

    // ── Top: title + refresh ──────────────────────────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        p.add(UITheme.title("Dashboard"), BorderLayout.WEST);

        JButton refreshBtn = UITheme.primaryButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(refreshBtn);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Content: KPI + filter bar + table + audit log ────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BG_DARK);
        p.add(buildKpiRow(), BorderLayout.NORTH);
        p.add(buildMain(), BorderLayout.CENTER);
        return p;
    }

    // ── KPI row — FR-5.4 ─────────────────────────────────────────────────────
    private JPanel buildKpiRow() {
        JPanel p = new JPanel(new GridLayout(1, 4, 14, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 14, 20));

        totalOrdersVal = kpiCard("Total Orders", "0", UITheme.ACCENT, p);
        revenueVal = kpiCard("Total Revenue", "0 VNĐ", UITheme.SUCCESS, p);
        pendingVal = kpiCard("Pending", "0", UITheme.WARNING, p);
        cancelledVal = kpiCard("Cancelled", "0", UITheme.DANGER, p);
        return p;
    }

    /** Creates a KPI card, adds it to parent, returns the value label. */
    private JLabel kpiCard(String title, String initVal, Color accent, JPanel parent) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accent);
                g.fillRect(0, 0, 4, getHeight());
            }
        };
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent.darker().darker(), 1),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(UITheme.FONT_BADGE);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel valLbl = new JLabel(initVal);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(accent);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        parent.add(card);
        return valLbl;
    }

    // ── Main area: filter bar + split (orders | audit log) ───────────────────
    private JPanel buildMain() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BG_DARK);
        p.add(buildFilterBar(), BorderLayout.NORTH);
        p.add(buildOrderTable(), BorderLayout.CENTER);
        return p;
    }

    // ── Filter bar — FR-5.1, FR-5.2, FR-5.3 ──────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        // Status filter (FR-5.2)
        p.add(UITheme.label("Status:"));
        statusFilter = UITheme.styledComboBox(
                new String[] { "All", "PENDING", "PAID", "CANCELLED" });
        statusFilter.addActionListener(e -> applyFilters());
        p.add(statusFilter);

        // Sort (FR-5.1)
        p.add(UITheme.label("Sort by:"));
        sortByCombo = UITheme.styledComboBox(
                new String[] { "date", "customer", "amount", "status" });
        sortByCombo.addActionListener(e -> applyFilters());
        p.add(sortByCombo);

        sortDirCombo = UITheme.styledComboBox(new String[] { "Desc", "Asc" });
        sortDirCombo.addActionListener(e -> applyFilters());
        p.add(sortDirCombo);

        // Search (FR-5.3)
        p.add(UITheme.label("Search:"));
        searchField = UITheme.styledTextField();
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.addActionListener(e -> applyFilters());
        p.add(searchField);

        JButton searchBtn = UITheme.primaryButton("Search");
        searchBtn.addActionListener(e -> applyFilters());
        p.add(searchBtn);

        JButton clearBtn = UITheme.ghostButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            sortByCombo.setSelectedIndex(0);
            sortDirCombo.setSelectedIndex(0);
            applyFilters();
        });
        p.add(clearBtn);
        return p;
    }

    // ── Orders table ──────────────────────────────────────────────────────────
    private JPanel buildOrderTable() {
        orderModel = TableRenderer.model(ORDER_COLS);
        orderTable = new JTable(orderModel);
        TableRenderer.applyAll(orderTable);
        orderTable.getColumnModel().getColumn(3).setCellRenderer(TableRenderer.status());
        orderTable.getColumnModel().getColumn(4).setCellRenderer(TableRenderer.currency());
        TableRenderer.widths(orderTable, 70, 200, 140, 110, 120);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        JLabel lbl = UITheme.heading("All Orders");
        JLabel hint = UITheme.label("Sorted by: date ↓  |  Click column header to sort");
        hint.setFont(UITheme.FONT_SMALL);
        header.add(lbl, BorderLayout.WEST);
        header.add(hint, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));

        p.add(header, BorderLayout.NORTH);
        p.add(UITheme.scrollPane(orderTable), BorderLayout.CENTER);
        return p;
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    public void refresh() {
        applyFilters();
        refreshKpis();
    }

    private void applyFilters() {
        try {
            String keyword = searchField != null ? searchField.getText().trim() : "";
            String status = statusFilter != null ? (String) statusFilter.getSelectedItem() : "All";
            String sortBy = sortByCombo != null ? (String) sortByCombo.getSelectedItem() : "date";
            boolean asc = sortDirCombo != null && "Asc".equals(sortDirCombo.getSelectedItem());

            List<Order> list;
            if (!keyword.isEmpty()) {
                // FR-5.3: search overrides other filters
                list = dashSvc.searchOrders(keyword);
            } else if (!"All".equals(status)) {
                // FR-5.2: status filter
                list = dashSvc.filterOrders(status, null, null);
            } else {
                // FR-5.1: sorted list
                list = dashSvc.getAllOrders(sortBy, asc);
            }
            populateOrderTable(list);
        } catch (Exception ex) {
            UITheme.showError(this, "Filter error: " + ex.getMessage());
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

    /** FR-5.4: summary statistics */
    private void refreshKpis() {
        try {
            Map<String, Object> stats = dashSvc.getSummaryStatistics();

            Object total = stats.get("totalOrders");
            totalOrdersVal.setText(total != null ? total.toString() : "0");

            Object rev = stats.get("totalRevenue");
            if (rev instanceof BigDecimal) {
                revenueVal.setText(String.format("%,.0f VNĐ", ((BigDecimal) rev).doubleValue()));
            } else {
                revenueVal.setText("0 VNĐ");
            }

            Object cancelled = stats.get("cancelledOrders");
            cancelledVal.setText(cancelled != null ? cancelled.toString() : "0");

            // Pending = total - paid - cancelled (no direct stat, compute inline)
            try {
                List<Order> pending = dashSvc.filterOrders("PENDING", null, null);
                pendingVal.setText(String.valueOf(pending.size()));
            } catch (Exception ignored) {
            }

        } catch (Exception ex) {
            UITheme.showError(this, "Failed to load statistics: " + ex.getMessage());
        }
    }
}