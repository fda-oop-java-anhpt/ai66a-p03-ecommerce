import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * CustomerPanel - Panel quản lý khách hàng
 * Tuần 5-6 - Customer Management Implementation
 * 
 * Features:
 * - JTable hiển thị danh sách customers
 * - CRUD operations (Create, Read, Update, Delete)
 * - Search by name or phone
 * - View customer order history
 * - Input validation
 * - Real-time table updates
 */
public class CustomerPanel extends JPanel {
    
    // UI Components
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnViewOrders;
    private JButton btnRefresh;
    
    // Form components
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextArea txtAddress;
    
    // Current selected customer ID
    private Integer selectedCustomerId = null;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    
    /**
     * Constructor
     */
    public CustomerPanel() {
        initComponents();
        setupLayout();
        loadCustomerData();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Table setup
        String[] columns = {"ID", "Name", "Phone", "Email", "Address", "Created Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
        };
        
        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.setRowHeight(25);
        customerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        customerTable.getTableHeader().setBackground(PRIMARY_COLOR);
        customerTable.getTableHeader().setForeground(Color.WHITE);
        customerTable.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Column widths
        customerTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Phone
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Email
        customerTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Address
        customerTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Date
        
        // Selection listener
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Double-click to edit
        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editCustomer();
                }
            }
        });
        
        // Search field
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Search on Enter key
        txtSearch.addActionListener(e -> performSearch());
        
        // Buttons
        btnAdd = createButton("➕ Add Customer", PRIMARY_COLOR);
        btnEdit = createButton("✏️ Edit", WARNING_COLOR);
        btnDelete = createButton("🗑️ Delete", ERROR_COLOR);
        btnViewOrders = createButton("📋 View Orders", new Color(156, 39, 176));
        btnRefresh = createButton("🔄 Refresh", new Color(96, 125, 139));
        
        // Button actions
        btnAdd.addActionListener(e -> addCustomer());
        btnEdit.addActionListener(e -> editCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnViewOrders.addActionListener(e -> viewCustomerOrders());
        btnRefresh.addActionListener(e -> loadCustomerData());
        
        // Form fields
        txtName = new JTextField(20);
        txtPhone = new JTextField(20);
        txtEmail = new JTextField(20);
        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        
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
        
        // Hover effect
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
        
        // Top panel - Title and search
        JPanel topPanel = createTopPanel();
        
        // Center panel - Table
        JPanel centerPanel = createCenterPanel();
        
        // Bottom panel - Buttons
        JPanel bottomPanel = createBottomPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create top panel with title and search
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("👥 Customer Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 11));
        btnSearch.addActionListener(e -> performSearch());
        
        JButton btnClear = new JButton("Clear");
        btnClear.setFont(new Font("Arial", Font.PLAIN, 11));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            loadCustomerData();
        });
        
        searchPanel.add(searchLabel);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create center panel with table
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Table in scroll pane
        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Info label
        JLabel infoLabel = new JLabel("Double-click a row to edit customer");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create bottom panel with buttons
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        
        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnDelete);
        panel.add(btnViewOrders);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnRefresh);
        
        return panel;
    }
    
    /**
     * Load customer data into table
     * TODO: Replace with actual service call in Week 6
     */
    private void loadCustomerData() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // TODO Week 6: Replace with actual service call
        // List<Customer> customers = customerService.getAllCustomers();
        
        // Sample data for testing
        Object[][] sampleData = {
            {1, "John Doe", "0901234567", "john.doe@email.com", "123 Main St, Hanoi", "2026-01-15"},
            {2, "Jane Smith", "0987654321", "jane.smith@email.com", "456 Oak Ave, HCMC", "2026-01-18"},
            {3, "Bob Johnson", "0912345678", "bob.j@email.com", "789 Pine Rd, Da Nang", "2026-01-20"},
            {4, "Alice Williams", "0923456789", "alice.w@email.com", "321 Elm St, Hanoi", "2026-01-22"},
            {5, "Charlie Brown", "0934567890", "charlie.b@email.com", "654 Maple Dr, Hue", "2026-01-25"}
        };
        
        for (Object[] row : sampleData) {
            tableModel.addRow(row);
        }
        
        updateButtonStates();
    }
    
    /**
     * Perform search
     */
    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        
        if (keyword.isEmpty()) {
            loadCustomerData();
            return;
        }
        
        // TODO Week 6: Replace with actual service call
        // List<Customer> results = customerService.searchCustomers(keyword);
        
        // Simulate search
        tableModel.setRowCount(0);
        
        // Filter sample data
        if (keyword.toLowerCase().contains("john")) {
            tableModel.addRow(new Object[]{
                1, "John Doe", "0901234567", "john.doe@email.com", 
                "123 Main St, Hanoi", "2026-01-15"
            });
        }
        if (keyword.toLowerCase().contains("jane")) {
            tableModel.addRow(new Object[]{
                2, "Jane Smith", "0987654321", "jane.smith@email.com", 
                "456 Oak Ave, HCMC", "2026-01-18"
            });
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No customers found matching: " + keyword,
                "Search Results",
                JOptionPane.INFORMATION_MESSAGE);
            loadCustomerData();
        }
    }
    
    /**
     * Add new customer
     */
    private void addCustomer() {
        // Create form dialog
        JDialog dialog = createCustomerFormDialog("Add New Customer", null);
        dialog.setVisible(true);
    }
    
    /**
     * Edit selected customer
     */
    private void editCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a customer to edit",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get customer data from table
        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String phone = (String) tableModel.getValueAt(selectedRow, 2);
        String email = (String) tableModel.getValueAt(selectedRow, 3);
        String address = (String) tableModel.getValueAt(selectedRow, 4);
        
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("id", id);
        customerData.put("name", name);
        customerData.put("phone", phone);
        customerData.put("email", email);
        customerData.put("address", address);
        
        JDialog dialog = createCustomerFormDialog("Edit Customer", customerData);
        dialog.setVisible(true);
    }
    
    /**
     * Delete selected customer
     */
    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a customer to delete",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete customer:\n\n" +
            "ID: " + id + "\n" +
            "Name: " + name + "\n\n" +
            "This action cannot be undone!",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // TODO Week 6: Call customerService.deleteCustomer(id)
            
            tableModel.removeRow(selectedRow);
            
            JOptionPane.showMessageDialog(this,
                "Customer deleted successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * View customer orders
     */
    private void viewCustomerOrders() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a customer to view orders",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        
        // TODO Week 6: Get actual order history
        // List<Order> orders = customerService.getCustomerOrderHistory(id);
        
        String orderHistory = 
            "Customer: " + name + " (ID: " + id + ")\n\n" +
            "Order History:\n" +
            "─────────────────────────────\n" +
            "Order #001 - $150.00 - 2026-01-20 - PAID\n" +
            "Order #005 - $89.50 - 2026-01-25 - PENDING\n" +
            "Order #012 - $220.00 - 2026-01-28 - PAID\n" +
            "─────────────────────────────\n" +
            "Total Orders: 3\n" +
            "Total Spent: $459.50";
        
        JTextArea textArea = new JTextArea(orderHistory);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 250));
        
        JOptionPane.showMessageDialog(this,
            scrollPane,
            "Order History - " + name,
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Create customer form dialog
     */
    private JDialog createCustomerFormDialog(String title, Map<String, Object> customerData) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                     title, true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name: *"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(20);
        formPanel.add(txtName, gbc);
        
        // Phone field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Phone: *"), gbc);
        gbc.gridx = 1;
        txtPhone = new JTextField(20);
        formPanel.add(txtPhone, gbc);
        
        // Email field
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);
        
        // Address field
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtAddress);
        formPanel.add(scrollPane, gbc);
        
        // Populate data if editing
        if (customerData != null) {
            selectedCustomerId = (Integer) customerData.get("id");
            txtName.setText((String) customerData.get("name"));
            txtPhone.setText((String) customerData.get("phone"));
            txtEmail.setText((String) customerData.get("email"));
            txtAddress.setText((String) customerData.get("address"));
        } else {
            selectedCustomerId = null;
        }
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnSave = new JButton("💾 Save");
        btnSave.setFont(new Font("Arial", Font.BOLD, 12));
        btnSave.setBackground(SUCCESS_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> {
            if (validateCustomerForm()) {
                saveCustomer(dialog);
            }
        });
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        return dialog;
    }
    
    /**
     * Validate customer form
     */
    private boolean validateCustomerForm() {
        // Name validation
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Name is required!",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return false;
        }
        
        // Phone validation
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Phone is required!",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return false;
        }
        
        // Phone format validation (Vietnamese format)
        if (!phone.matches("^0\\d{9,10}$")) {
            JOptionPane.showMessageDialog(this,
                "Invalid phone format!\n\n" +
                "Phone must start with 0 and have 10-11 digits.\n" +
                "Example: 0901234567",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return false;
        }
        
        // Email validation (if provided)
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this,
                "Invalid email format!",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Save customer (Add or Update)
     */
    private void saveCustomer(JDialog dialog) {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        
        if (selectedCustomerId == null) {
            // ADD NEW CUSTOMER
            // TODO Week 6: Call customerService.createCustomer(...)
            
            int newId = tableModel.getRowCount() + 1;
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
            
            tableModel.addRow(new Object[]{
                newId, name, phone, email, address, date
            });
            
            JOptionPane.showMessageDialog(this,
                "Customer added successfully!\n\n" +
                "ID: " + newId + "\n" +
                "Name: " + name,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
        } else {
            // UPDATE EXISTING CUSTOMER
            // TODO Week 6: Call customerService.updateCustomer(...)
            
            int selectedRow = customerTable.getSelectedRow();
            tableModel.setValueAt(name, selectedRow, 1);
            tableModel.setValueAt(phone, selectedRow, 2);
            tableModel.setValueAt(email, selectedRow, 3);
            tableModel.setValueAt(address, selectedRow, 4);
            
            JOptionPane.showMessageDialog(this,
                "Customer updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        dialog.dispose();
    }
    
    /**
     * Update button states based on selection
     */
    private void updateButtonStates() {
        boolean hasSelection = customerTable.getSelectedRow() >= 0;
        btnEdit.setEnabled(hasSelection);
        btnDelete.setEnabled(hasSelection);
        btnViewOrders.setEnabled(hasSelection);
    }
    
    /**
     * Test method
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Customer Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new CustomerPanel());
            frame.setSize(1000, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}