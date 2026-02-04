package com.oop.project.ui.panel;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * OrderPanel - Panel quản lý đơn hàng
 * Tuần 6 - Order Management Implementation
 * 
 * Features:
 * - View all orders in JTable
 * - Create new order with multiple items
 * - Add/Remove items to order
 * - Real-time subtotal calculation
 * - Order status management (Pending, Paid, Cancelled)
 * - Customer selection via JComboBox
 * - View order details
 * - Status filter
 * - Date range filter
 * 
 * @author Thành viên 4 (UI Layer)
 */
public class OrderPanel extends JPanel {
    
    // UI Components - Order List
    private JTable orderTable;
    private DefaultTableModel orderTableModel;
    private JComboBox<String> cmbStatusFilter;
    private JTextField txtSearchOrder;
    private JButton btnCreateOrder;
    private JButton btnViewDetails;
    private JButton btnUpdateStatus;
    private JButton btnCancelOrder;
    private JButton btnRefresh;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color PENDING_COLOR = new Color(255, 193, 7);
    
    // Order statuses
    private static final String[] ORDER_STATUSES = {
        "All Statuses", "Pending", "Paid", "Cancelled"
    };
    
    /**
     * Constructor
     */
    public OrderPanel() {
        initComponents();
        setupLayout();
        loadOrderData();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Order table setup
        String[] columns = {"Order ID", "Customer", "Date", "Items", "Subtotal", "Tax", "Total", "Status"};
        orderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column >= 4 && column <= 6) return Double.class;
                if (column == 3) return Integer.class;
                return String.class;
            }
        };
        
        orderTable = new JTable(orderTableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setRowHeight(30);
        orderTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        orderTable.getTableHeader().setBackground(PRIMARY_COLOR);
        orderTable.getTableHeader().setForeground(Color.WHITE);
        orderTable.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Custom renderer for money columns
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    setText(String.format("$%.2f", value));
                    setHorizontalAlignment(JLabel.RIGHT);
                    setFont(new Font("Arial", Font.BOLD, 12));
                }
                return c;
            }
        };
        
        orderTable.getColumnModel().getColumn(4).setCellRenderer(moneyRenderer);
        orderTable.getColumnModel().getColumn(5).setCellRenderer(moneyRenderer);
        orderTable.getColumnModel().getColumn(6).setCellRenderer(moneyRenderer);
        
        // Custom renderer for status column
        orderTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Arial", Font.BOLD, 11));
                
                if (!isSelected) {
                    switch ((String) value) {
                        case "Pending":
                            setBackground(new Color(255, 248, 225));
                            setForeground(PENDING_COLOR.darker());
                            setText("⏳ Pending");
                            break;
                        case "Paid":
                            setBackground(new Color(232, 245, 233));
                            setForeground(SUCCESS_COLOR.darker());
                            setText("✓ Paid");
                            break;
                        case "Cancelled":
                            setBackground(new Color(255, 235, 238));
                            setForeground(ERROR_COLOR);
                            setText("✗ Cancelled");
                            break;
                    }
                } else {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                }
                
                return c;
            }
        });
        
        // Column widths
        orderTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // Order ID
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Customer
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Date
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(60);   // Items
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(90);   // Subtotal
        orderTable.getColumnModel().getColumn(5).setPreferredWidth(70);   // Tax
        orderTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // Total
        orderTable.getColumnModel().getColumn(7).setPreferredWidth(100);  // Status
        
        // Selection listener
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Double-click to view details
        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewOrderDetails();
                }
            }
        });
        
        // Search field
        txtSearchOrder = new JTextField(15);
        txtSearchOrder.setFont(new Font("Arial", Font.PLAIN, 12));
        txtSearchOrder.addActionListener(e -> searchOrders());
        
        // Status filter
        cmbStatusFilter = new JComboBox<>(ORDER_STATUSES);
        cmbStatusFilter.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbStatusFilter.addActionListener(e -> filterByStatus());
        
        // Buttons
        btnCreateOrder = createButton("➕ Create Order", PRIMARY_COLOR);
        btnViewDetails = createButton("👁 View Details", new Color(96, 125, 139));
        btnUpdateStatus = createButton("📝 Update Status", WARNING_COLOR);
        btnCancelOrder = createButton("✗ Cancel Order", ERROR_COLOR);
        btnRefresh = createButton("🔄 Refresh", new Color(158, 158, 158));
        
        // Button actions
        btnCreateOrder.addActionListener(e -> createOrder());
        btnViewDetails.addActionListener(e -> viewOrderDetails());
        btnUpdateStatus.addActionListener(e -> updateOrderStatus());
        btnCancelOrder.addActionListener(e -> cancelOrder());
        btnRefresh.addActionListener(e -> loadOrderData());
        
        updateButtonStates();
    }
    
    /**
     * Create styled button
     */
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 35));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    /**
     * Setup layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Create top panel
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("🛒 Order Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(Color.WHITE);
        
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cmbStatusFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(new JLabel("🔍 Search:"));
        filterPanel.add(txtSearchOrder);
        
        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 11));
        btnSearch.addActionListener(e -> searchOrders());
        filterPanel.add(btnSearch);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(filterPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create center panel
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JLabel infoLabel = new JLabel("Double-click to view order details");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create bottom panel
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        
        panel.add(btnCreateOrder);
        panel.add(btnViewDetails);
        panel.add(btnUpdateStatus);
        panel.add(btnCancelOrder);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnRefresh);
        
        return panel;
    }
    
    /**
     * Load order data
     */
    private void loadOrderData() {
        orderTableModel.setRowCount(0);
        
        // TODO Week 6: Replace with actual service call
        // List<Order> orders = orderService.getAllOrders();
        
        // Sample data
        Object[][] sampleData = {
            {"ORD-001", "John Doe", "2026-01-20", 3, 250.00, 20.00, 270.00, "Paid"},
            {"ORD-002", "Jane Smith", "2026-01-22", 2, 150.00, 12.00, 162.00, "Pending"},
            {"ORD-003", "Bob Johnson", "2026-01-23", 5, 500.00, 40.00, 540.00, "Paid"},
            {"ORD-004", "Alice Williams", "2026-01-25", 1, 89.99, 7.20, 97.19, "Pending"},
            {"ORD-005", "Charlie Brown", "2026-01-26", 4, 320.00, 25.60, 345.60, "Cancelled"},
            {"ORD-006", "David Lee", "2026-01-27", 2, 180.50, 14.44, 194.94, "Paid"},
            {"ORD-007", "Emma Davis", "2026-01-28", 3, 275.00, 22.00, 297.00, "Pending"}
        };
        
        for (Object[] row : sampleData) {
            orderTableModel.addRow(row);
        }
        
        updateButtonStates();
    }
    
    /**
     * Filter by status
     */
    private void filterByStatus() {
        String status = (String) cmbStatusFilter.getSelectedItem();
        
        if ("All Statuses".equals(status)) {
            loadOrderData();
            return;
        }
        
        // TODO Week 6: Call orderService.getOrdersByStatus(status)
        
        orderTableModel.setRowCount(0);
        
        Object[][] allData = {
            {"ORD-001", "John Doe", "2026-01-20", 3, 250.00, 20.00, 270.00, "Paid"},
            {"ORD-002", "Jane Smith", "2026-01-22", 2, 150.00, 12.00, 162.00, "Pending"},
            {"ORD-005", "Charlie Brown", "2026-01-26", 4, 320.00, 25.60, 345.60, "Cancelled"}
        };
        
        for (Object[] row : allData) {
            if (status.equals(row[7])) {
                orderTableModel.addRow(row);
            }
        }
    }
    
    /**
     * Search orders
     */
    private void searchOrders() {
        String keyword = txtSearchOrder.getText().trim();
        
        if (keyword.isEmpty()) {
            loadOrderData();
            return;
        }
        
        // TODO Week 6: Call orderService.searchOrders(keyword)
        
        JOptionPane.showMessageDialog(this,
            "Searching for: " + keyword + "\n\n" +
            "Search by Order ID or Customer Name",
            "Search",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Create new order
     */
    private void createOrder() {
        CreateOrderDialog dialog = new CreateOrderDialog(
            (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        
        if (dialog.isOrderCreated()) {
            loadOrderData();
        }
    }
    
    /**
     * View order details
     */
    private void viewOrderDetails() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an order to view details",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String orderId = (String) orderTableModel.getValueAt(selectedRow, 0);
        String customer = (String) orderTableModel.getValueAt(selectedRow, 1);
        String date = (String) orderTableModel.getValueAt(selectedRow, 2);
        Double subtotal = (Double) orderTableModel.getValueAt(selectedRow, 4);
        Double tax = (Double) orderTableModel.getValueAt(selectedRow, 5);
        Double total = (Double) orderTableModel.getValueAt(selectedRow, 6);
        String status = (String) orderTableModel.getValueAt(selectedRow, 7);
        
        // TODO Week 6: Get actual order items from service
        
        String details = String.format(
            "========== ORDER DETAILS ==========\n\n" +
            "Order ID: %s\n" +
            "Customer: %s\n" +
            "Date: %s\n" +
            "Status: %s\n\n" +
            "ITEMS:\n" +
            "─────────────────────────────\n" +
            "1. Laptop Dell XPS 13    x1  $1,299.99\n" +
            "2. Wireless Mouse        x2  $   25.99\n" +
            "3. USB-C Cable           x1  $   15.00\n" +
            "─────────────────────────────\n\n" +
            "Subtotal:  $%.2f\n" +
            "Tax (8%%):  $%.2f\n" +
            "─────────────────────────────\n" +
            "TOTAL:     $%.2f\n" +
            "=================================",
            orderId, customer, date, status, subtotal, tax, total
        );
        
        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 400));
        
        JOptionPane.showMessageDialog(this,
            scrollPane,
            "Order Details - " + orderId,
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Update order status
     */
    private void updateOrderStatus() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an order to update status",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String orderId = (String) orderTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) orderTableModel.getValueAt(selectedRow, 7);
        
        String[] statuses = {"Pending", "Paid", "Cancelled"};
        String newStatus = (String) JOptionPane.showInputDialog(
            this,
            "Select new status for Order " + orderId + ":",
            "Update Order Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statuses,
            currentStatus
        );
        
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            // TODO Week 6: Call orderService.updateOrderStatus(orderId, newStatus)
            
            orderTableModel.setValueAt(newStatus, selectedRow, 7);
            
            JOptionPane.showMessageDialog(this,
                "Order status updated successfully!\n\n" +
                "Order: " + orderId + "\n" +
                "New Status: " + newStatus,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Cancel order
     */
    private void cancelOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an order to cancel",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String orderId = (String) orderTableModel.getValueAt(selectedRow, 0);
        String status = (String) orderTableModel.getValueAt(selectedRow, 7);
        
        if ("Cancelled".equals(status)) {
            JOptionPane.showMessageDialog(this,
                "This order is already cancelled!",
                "Already Cancelled",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel order:\n\n" +
            "Order ID: " + orderId + "\n\n" +
            "This action will be logged.",
            "Confirm Cancel Order",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // TODO Week 6: Call orderService.cancelOrder(orderId)
            
            orderTableModel.setValueAt("Cancelled", selectedRow, 7);
            
            JOptionPane.showMessageDialog(this,
                "Order cancelled successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Update button states
     */
    private void updateButtonStates() {
        boolean hasSelection = orderTable.getSelectedRow() >= 0;
        btnViewDetails.setEnabled(hasSelection);
        btnUpdateStatus.setEnabled(hasSelection);
        btnCancelOrder.setEnabled(hasSelection);
    }
    
    /**
     * Test method
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Order Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new OrderPanel());
            frame.setSize(1100, 650);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

/**
 * CreateOrderDialog - Dialog for creating new orders
 */
class CreateOrderDialog extends JDialog {
    
    private boolean orderCreated = false;
    
    private JComboBox<String> cmbCustomer;
    private JTable itemTable;
    private DefaultTableModel itemTableModel;
    private JComboBox<String> cmbItem;
    private JSpinner spnQuantity;
    private JLabel lblSubtotal;
    private JLabel lblTax;
    private JLabel lblTotal;
    
    private double subtotal = 0.0;
    private static final double TAX_RATE = 0.08;
    
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    
    public CreateOrderDialog(Frame owner) {
        super(owner, "Create New Order", true);
        initComponents();
        setupLayout();
        setSize(700, 550);
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        // Customer selection
        String[] customers = {"Select Customer...", "John Doe", "Jane Smith", "Bob Johnson", "Alice Williams"};
        cmbCustomer = new JComboBox<>(customers);
        cmbCustomer.setFont(new Font("Arial", Font.PLAIN, 13));
        
        // Item selection
        String[] items = {
            "Select Item...",
            "Laptop Dell XPS 13 - $1,299.99",
            "iPhone 15 Pro - $999.99",
            "Wireless Mouse - $25.99",
            "USB-C Cable - $15.00"
        };
        cmbItem = new JComboBox<>(items);
        cmbItem.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Quantity spinner
        SpinnerNumberModel qtyModel = new SpinnerNumberModel(1, 1, 99, 1);
        spnQuantity = new JSpinner(qtyModel);
        spnQuantity.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Item table
        String[] columns = {"Item", "Quantity", "Unit Price", "Subtotal"};
        itemTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        itemTable = new JTable(itemTableModel);
        itemTable.setRowHeight(25);
        itemTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        
        // Labels
        lblSubtotal = new JLabel("$0.00");
        lblTax = new JLabel("$0.00");
        lblTotal = new JLabel("$0.00");
        
        lblSubtotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTax.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(PRIMARY_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        
        // Top - Customer selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Customer: "));
        topPanel.add(cmbCustomer);
        
        // Center - Items
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(Color.WHITE);
        
        JPanel addItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addItemPanel.setBackground(Color.WHITE);
        addItemPanel.add(new JLabel("Item:"));
        addItemPanel.add(cmbItem);
        addItemPanel.add(new JLabel("Qty:"));
        addItemPanel.add(spnQuantity);
        
        JButton btnAddItem = new JButton("➕ Add to Order");
        btnAddItem.setBackground(PRIMARY_COLOR);
        btnAddItem.setForeground(Color.WHITE);
        btnAddItem.setFocusPainted(false);
        btnAddItem.addActionListener(e -> addItemToOrder());
        addItemPanel.add(btnAddItem);
        
        JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(e -> removeSelectedItem());
        addItemPanel.add(btnRemove);
        
        JScrollPane scrollPane = new JScrollPane(itemTable);
        
        centerPanel.add(addItemPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom - Total
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(Color.WHITE);
        
        JPanel totalPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setBorder(new TitledBorder("Order Summary"));
        
        totalPanel.add(new JLabel("Subtotal:"));
        totalPanel.add(lblSubtotal);
        totalPanel.add(new JLabel("Tax (8%):"));
        totalPanel.add(lblTax);
        totalPanel.add(new JLabel("TOTAL:"));
        totalPanel.add(lblTotal);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnCreate = new JButton("💾 Create Order");
        btnCreate.setFont(new Font("Arial", Font.BOLD, 13));
        btnCreate.setBackground(SUCCESS_COLOR);
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFocusPainted(false);
        btnCreate.setPreferredSize(new Dimension(150, 35));
        btnCreate.addActionListener(e -> createOrder());
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnCreate);
        buttonPanel.add(btnCancel);
        
        bottomPanel.add(totalPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void addItemToOrder() {
        if (cmbItem.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an item!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String itemStr = (String) cmbItem.getSelectedItem();
        String[] parts = itemStr.split(" - \\$");
        String itemName = parts[0];
        double price = Double.parseDouble(parts[1].replace(",", ""));
        int qty = (Integer) spnQuantity.getValue();
        double itemSubtotal = price * qty;
        
        itemTableModel.addRow(new Object[]{
            itemName,
            qty,
            String.format("$%.2f", price),
            String.format("$%.2f", itemSubtotal)
        });
        
        subtotal += itemSubtotal;
        updateTotals();
        
        // Reset
        cmbItem.setSelectedIndex(0);
        spnQuantity.setValue(1);
    }
    
    private void removeSelectedItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow >= 0) {
            String subtotalStr = (String) itemTableModel.getValueAt(selectedRow, 3);
            double amount = Double.parseDouble(subtotalStr.replace("$", "").replace(",", ""));
            subtotal -= amount;
            itemTableModel.removeRow(selectedRow);
            updateTotals();
        }
    }
    
    private void updateTotals() {
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;
        
        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblTax.setText(String.format("$%.2f", tax));
        lblTotal.setText(String.format("$%.2f", total));
    }
    
    private void createOrder() {
        if (cmbCustomer.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a customer!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (itemTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Please add at least one item!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // TODO Week 6: Call orderService.createOrder(...)
        
        JOptionPane.showMessageDialog(this,
            "Order created successfully!\n\n" +
            "Customer: " + cmbCustomer.getSelectedItem() + "\n" +
            "Items: " + itemTableModel.getRowCount() + "\n" +
            "Total: " + lblTotal.getText(),
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        
        orderCreated = true;
        dispose();
    }
    
    public boolean isOrderCreated() {
        return orderCreated;
    }
}