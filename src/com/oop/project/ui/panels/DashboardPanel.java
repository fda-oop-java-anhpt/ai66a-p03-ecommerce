package com.oop.project.ui.panels;

import com.oop.project.model.Order;
import com.oop.project.model.OrderStatus;
import com.oop.project.service.interfaces.CustomerService;
import com.oop.project.service.interfaces.ItemService;
import com.oop.project.service.interfaces.OrderService;
import com.oop.project.ui.components.SearchBar;
import com.oop.project.ui.components.StatCard;
import com.oop.project.ui.utils.DialogUtils;
import com.oop.project.ui.utils.TableUtils;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Dashboard tab — FR-5.
 * FR-5.1 sortable order list, FR-5.2 filter by status, FR-5.3 search,
 * FR-5.4 summary statistics.
 */
public class DashboardPanel extends JPanel {

    private final OrderService    orderService;
    private final CustomerService customerService;
    private final ItemService     itemService;

    // KPI cards
    private StatCard totalOrdersCard, revenueCard, cancelledCard, paidCard;

    // Orders table
    private DefaultTableModel tableModel;
    private JTable            table;
    private TableRowSorter<DefaultTableModel> sorter;

    private static final String[] COLUMNS =
            {"Order ID", "Customer", "Date", "Status", "Total ($)"};

    public DashboardPanel(OrderService orderService, CustomerService customerService,
                          ItemService itemService) {
        this.orderService    = orderService;
        this.customerService = customerService;
        this.itemService     = itemService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);

        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildContent(),  BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        p.add(UITheme.title("Dashboard"), BorderLayout.WEST);

        JButton refreshBtn = UITheme.primaryButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(refreshBtn);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BG_DARK);
        p.add(buildKpiRow(),      BorderLayout.NORTH);
        p.add(buildTableSection(), BorderLayout.CENTER);
        return p;
    }

    // ── KPI row (FR-5.4) ──────────────────────────────────────────────────────
    private JPanel buildKpiRow() {
        JPanel p = new JPanel(new GridLayout(1, 4, 16, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 20));

        totalOrdersCard = new StatCard("Total Orders",   "0",     UITheme.ACCENT);
        revenueCard     = new StatCard("Total Revenue",  "$0.00", UITheme.SUCCESS);
        paidCard        = new StatCard("Paid Orders",    "0",     UITheme.STATUS_PAID);
        cancelledCard   = new StatCard("Cancelled",      "0",     UITheme.DANGER);

        p.add(totalOrdersCard);
        p.add(revenueCard);
        p.add(paidCard);
        p.add(cancelledCard);
        return p;
    }

    // ── Table section (FR-5.1, FR-5.2, FR-5.3) ───────────────────────────────
    private JPanel buildTableSection() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        // Filter toolbar
        JPanel filterBar = buildFilterBar();

        tableModel = TableUtils.nonEditableModel(COLUMNS);
        table = new JTable(tableModel);
        TableUtils.applyDefaultRenderers(table);
        table.getColumnModel().getColumn(3).setCellRenderer(TableUtils.statusBadgeRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(TableUtils.currencyRenderer());
        TableUtils.setColumnWidths(table, 90, 200, 130, 110, 120);

        // Enable sorting (FR-5.1)
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(UITheme.BG_CARD);
        tableCard.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(UITheme.BG_CARD);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel tableTitle = UITheme.heading("All Orders");
        JLabel hint = UITheme.label("Click column headers to sort");
        hint.setFont(UITheme.FONT_SMALL);
        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(hint, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);

        p.add(filterBar, BorderLayout.NORTH);
        p.add(tableCard, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFilterBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        p.setBackground(UITheme.BG_DARK);

        // Status filter (FR-5.2)
        p.add(UITheme.label("Filter:"));
        JComboBox<String> statusFilter = UITheme.styledComboBox(
                new String[]{"All Statuses", "PENDING", "PAID", "CANCELLED"});
        statusFilter.addActionListener(e -> applyFilter(
                (String) statusFilter.getSelectedItem()));
        p.add(statusFilter);

        // Date range quick filters
        p.add(UITheme.label("Period:"));
        JComboBox<String> periodFilter = UITheme.styledComboBox(
                new String[]{"All Time", "Today", "This Week", "This Month"});
        periodFilter.addActionListener(e -> applyPeriodFilter(
                (String) periodFilter.getSelectedItem()));
        p.add(periodFilter);

        // Search (FR-5.3)
        SearchBar search = new SearchBar("Search customer or order ID…", keyword -> {
            try { populateTable(orderService.searchOrders(keyword)); }
            catch (Exception ex) { DialogUtils.showError(this, ex.getMessage()); }
        });
        p.add(search);

        return p;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    public void refresh() {
        try {
            List<Order> orders = orderService.getAllOrders();
            populateTable(orders);
            updateKpis(orders);
        } catch (Exception ex) {
            DialogUtils.showError(this, "Failed to load dashboard: " + ex.getMessage());
        }
    }

    private void applyFilter(String status) {
        try {
            List<Order> orders = "All Statuses".equals(status)
                    ? orderService.getAllOrders()
                    : orderService.filterByStatus(OrderStatus.valueOf(status));
            populateTable(orders);
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void applyPeriodFilter(String period) {
        try {
            List<Order> orders;
            LocalDateTime now = LocalDateTime.now();
            switch (period) {
                case "Today" -> {
                    Timestamp start = Timestamp.valueOf(now.with(LocalTime.MIN));
                    Timestamp end = Timestamp.valueOf(now.with(LocalTime.MAX));
                    orders = orderService.filterByDateRange(start, end);
                }
                case "This Week" -> {
                    Timestamp start = Timestamp.valueOf(now.with(java.time.DayOfWeek.MONDAY).with(LocalTime.MIN));
                    Timestamp end = Timestamp.valueOf(now.with(java.time.DayOfWeek.SUNDAY).with(LocalTime.MAX));
                    orders = orderService.filterByDateRange(start, end);
                }
                case "This Month" -> {
                    Timestamp start = Timestamp.valueOf(now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN));
                    Timestamp end = Timestamp.valueOf(now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX));
                    orders = orderService.filterByDateRange(start, end);
                }
                default -> orders = orderService.getAllOrders();
            }
            populateTable(orders);
        } catch (Exception ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private void populateTable(List<Order> orders) {
        tableModel.setRowCount(0);
        for (Order o : orders) {
            tableModel.addRow(new Object[]{
                o.getOrderId(), 
                o.getCustomer() != null ? o.getCustomer().getCustomerName() : "N/A",
                o.getOrderDate(), o.getStatus().name(),
                o.getFinalTotal()
            });
        }
    }

    private void updateKpis(List<Order> orders) {
        int total     = orders.size();
        int paid      = (int) orders.stream().filter(o -> OrderStatus.PAID == o.getStatus()).count();
        int cancelled = (int) orders.stream().filter(o -> OrderStatus.CANCELLED == o.getStatus()).count();
        double revenue = orders.stream()
                .filter(o -> OrderStatus.PAID == o.getStatus())
                .mapToDouble(o -> o.getFinalTotal().doubleValue()).sum();

        totalOrdersCard.setValue(String.valueOf(total));
        revenueCard    .setValue(String.format("$%.2f", revenue));
        paidCard       .setValue(String.valueOf(paid));
        cancelledCard  .setValue(String.valueOf(cancelled));
    }
}
