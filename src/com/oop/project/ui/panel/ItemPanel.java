package panel; 

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * ItemPanel - Panel quản lý danh mục sản phẩm
 * Tuần 5-6 - Item Catalog Management
 * 
 * Features:
 * - JTable hiển thị item catalog
 * - CRUD operations (Create, Read, Update, Delete)
 * - SKU code validation (no duplicates)
 * - Category filtering
 * - Admin-only price modification (FR-2.4)
 * - Stock quantity tracking
 * - Search by name or SKU
 * - Real-time price updates
 * 
 * @author Thành viên 4 (UI Layer)
 */
public class ItemPanel extends JPanel {
    
    // User role for permission check
    private String currentUserRole = "ADMIN"; // TODO: Get from session
    
    // UI Components
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbCategoryFilter;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnUpdatePrice;
    private JButton btnRefresh;
    private JCheckBox chkShowInactive;
    
    // Form components
    private JTextField txtSKU;
    private JTextField txtName;
    private JTextField txtPrice;
    private JComboBox<String> cmbCategory;
    private JSpinner spnStockQuantity;
    private JCheckBox chkActive;
    
    // Current selected item
    private String selectedSKU = null;
    
    // Categories
    private static final String[] CATEGORIES = {
        "All Categories",
        "Electronics",
        "Clothing",
        "Food & Beverage",
        "Home & Garden",
        "Sports & Outdoors",
        "Books & Media",
        "Toys & Games",
        "Health & Beauty",
        "Other"
    };
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color ADMIN_COLOR = new Color(156, 39, 176);
    
    /**
     * Constructor
     */
    public ItemPanel() {
        initComponents();
        setupLayout();
        loadItemData();
    }
    
    /**
     * Constructor with role
     */
    public ItemPanel(String userRole) {
        this.currentUserRole = userRole;
        initComponents();
        setupLayout();
        loadItemData();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Table setup
        String[] columns = {"SKU", "Name", "Price", "Category", "Stock", "Status", "Last Updated"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) return Double.class; // Price column
                if (column == 4) return Integer.class; // Stock column
                return String.class;
            }
        };
        
        itemTable = new JTable(tableModel);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setRowHeight(28);
        itemTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        itemTable.getTableHeader().setBackground(PRIMARY_COLOR);
        itemTable.getTableHeader().setForeground(Color.WHITE);
        itemTable.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Custom renderer for price column
        itemTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    setText(String.format("$%.2f", value));
                    setHorizontalAlignment(JLabel.RIGHT);
                }
                return c;
            }
        });
        
        // Custom renderer for stock column
        itemTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Integer) {
                    int stock = (Integer) value;
                    setHorizontalAlignment(JLabel.CENTER);
                    if (!isSelected) {
                        if (stock == 0) {
                            setBackground(new Color(255, 235, 235));
                            setForeground(ERROR_COLOR);
                        } else if (stock < 10) {
                            setBackground(new Color(255, 245, 235));
                            setForeground(WARNING_COLOR);
                        } else {
                            setBackground(Color.WHITE);
                            setForeground(Color.BLACK);
                        }
                    }
                }
                return c;
            }
        });
        
        // Custom renderer for status column
        itemTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if ("Active".equals(value)) {
                    setText("✓ Active");
                    if (!isSelected) setForeground(SUCCESS_COLOR);
                } else {
                    setText("✗ Inactive");
                    if (!isSelected) setForeground(Color.GRAY);
                }
                return c;
            }
        });
        
        // Column widths
        itemTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // SKU
        itemTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // Name
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // Price
        itemTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Category
        itemTable.getColumnModel().getColumn(4).setPreferredWidth(60);   // Stock
        itemTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // Status
        itemTable.getColumnModel().getColumn(6).setPreferredWidth(120);  // Date
        
        // Selection listener
        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Double-click to edit
        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editItem();
                }
            }
        });
        
        // Search field
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        txtSearch.addActionListener(e -> performSearch());
        
        // Category filter
        cmbCategoryFilter = new JComboBox<>(CATEGORIES);
        cmbCategoryFilter.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbCategoryFilter.addActionListener(e -> filterByCategory());
        
        // Show inactive checkbox
        chkShowInactive = new JCheckBox("Show Inactive");
        chkShowInactive.setFont(new Font("Arial", Font.PLAIN, 11));
        chkShowInactive.addActionListener(e -> loadItemData());
        
        // Buttons
        btnAdd = createButton("➕ Add Item", PRIMARY_COLOR);
        btnEdit = createButton("✏️ Edit", WARNING_COLOR);
        btnDelete = createButton("🗑️ Delete", ERROR_COLOR);
        btnUpdatePrice = createButton("💲 Update Price", ADMIN_COLOR);
        btnRefresh = createButton("🔄 Refresh", new Color(96, 125, 139));
        
        // Button actions
        btnAdd.addActionListener(e -> addItem());
        btnEdit.addActionListener(e -> editItem());
        btnDelete.addActionListener(e -> deleteItem());
        btnUpdatePrice.addActionListener(e -> updatePrice());
        btnRefresh.addActionListener(e -> loadItemData());
        
        // Admin-only button visibility
        btnUpdatePrice.setVisible(isAdmin());
        if (!isAdmin()) {
            btnUpdatePrice.setToolTipText("Admin only");
        }
        
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
        
        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create top panel
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        
        // Title with role indicator
        String roleIndicator = isAdmin() ? " [Admin Mode]" : " [View Mode]";
        JLabel titleLabel = new JLabel("📦 Item Catalog Management" + roleIndicator);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        // Filter and search panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(Color.WHITE);
        
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 11));
        btnSearch.addActionListener(e -> performSearch());
        
        JButton btnClear = new JButton("Clear");
        btnClear.setFont(new Font("Arial", Font.PLAIN, 11));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategoryFilter.setSelectedIndex(0);
            loadItemData();
        });
        
        filterPanel.add(lblCategory);
        filterPanel.add(cmbCategoryFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(chkShowInactive);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(btnSearch);
        filterPanel.add(btnClear);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(filterPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create center panel with table
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel infoLabel = new JLabel("Double-click to edit item");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        
        JLabel legendLabel = new JLabel("  |  Legend: ");
        legendLabel.setFont(new Font("Arial", Font.BOLD, 11));
        
        JLabel redLabel = new JLabel("■ Out of Stock");
        redLabel.setForeground(ERROR_COLOR);
        redLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        
        JLabel orangeLabel = new JLabel("■ Low Stock (<10)");
        orangeLabel.setForeground(WARNING_COLOR);
        orangeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        
        infoPanel.add(infoLabel);
        infoPanel.add(legendLabel);
        infoPanel.add(redLabel);
        infoPanel.add(new JLabel("  "));
        infoPanel.add(orangeLabel);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        
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
        
        if (isAdmin()) {
            panel.add(btnUpdatePrice);
        }
        
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnRefresh);
        
        return panel;
    }
    
    /**
     * Load item data into table
     */
    private void loadItemData() {
        tableModel.setRowCount(0);
        
        // TODO Week 6: Replace with actual service call
        // List<Item> items = itemService.getAllItems(chkShowInactive.isSelected());
        
        // Sample data
        Object[][] sampleData = {
            {"ELEC-001", "Laptop Dell XPS 13", 1299.99, "Electronics", 15, "Active", "2026-01-15"},
            {"ELEC-002", "iPhone 15 Pro", 999.99, "Electronics", 8, "Active", "2026-01-18"},
            {"CLOT-001", "T-Shirt Nike", 29.99, "Clothing", 50, "Active", "2026-01-20"},
            {"FOOD-001", "Organic Coffee Beans", 15.50, "Food & Beverage", 0, "Active", "2026-01-22"},
            {"HOME-001", "LED Desk Lamp", 45.00, "Home & Garden", 25, "Active", "2026-01-25"},
            {"SPRT-001", "Yoga Mat Premium", 35.00, "Sports & Outdoors", 5, "Active", "2026-01-26"},
            {"BOOK-001", "The Great Gatsby", 12.99, "Books & Media", 30, "Active", "2026-01-27"},
            {"ELEC-003", "Wireless Mouse", 25.99, "Electronics", 100, "Active", "2026-01-28"},
            {"CLOT-002", "Winter Jacket", 89.99, "Clothing", 2, "Active", "2026-01-29"},
            {"FOOD-002", "Green Tea Set", 22.50, "Food & Beverage", 20, "Inactive", "2026-01-30"}
        };
        
        boolean showInactive = chkShowInactive.isSelected();
        
        for (Object[] row : sampleData) {
            String status = (String) row[5];
            if (showInactive || "Active".equals(status)) {
                tableModel.addRow(row);
            }
        }
        
        updateButtonStates();
    }
    
    /**
     * Perform search
     */
    private void performSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        
        if (keyword.isEmpty()) {
            loadItemData();
            return;
        }
        
        // TODO Week 6: Call itemService.searchItems(keyword)
        
        // Simulate search by filtering current data
        tableModel.setRowCount(0);
        
        // Re-load and filter
        Object[][] allData = {
            {"ELEC-001", "Laptop Dell XPS 13", 1299.99, "Electronics", 15, "Active", "2026-01-15"},
            {"ELEC-002", "iPhone 15 Pro", 999.99, "Electronics", 8, "Active", "2026-01-18"},
            {"CLOT-001", "T-Shirt Nike", 29.99, "Clothing", 50, "Active", "2026-01-20"}
        };
        
        for (Object[] row : allData) {
            String sku = ((String) row[0]).toLowerCase();
            String name = ((String) row[1]).toLowerCase();
            
            if (sku.contains(keyword) || name.contains(keyword)) {
                tableModel.addRow(row);
            }
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No items found matching: " + keyword,
                "Search Results",
                JOptionPane.INFORMATION_MESSAGE);
            loadItemData();
        }
    }
    
    /**
     * Filter by category
     */
    private void filterByCategory() {
        String category = (String) cmbCategoryFilter.getSelectedItem();
        
        if ("All Categories".equals(category)) {
            loadItemData();
            return;
        }
        
        // TODO Week 6: Call itemService.getItemsByCategory(category)
        
        tableModel.setRowCount(0);
        
        // Filter sample data
        Object[][] allData = {
            {"ELEC-001", "Laptop Dell XPS 13", 1299.99, "Electronics", 15, "Active", "2026-01-15"},
            {"ELEC-002", "iPhone 15 Pro", 999.99, "Electronics", 8, "Active", "2026-01-18"},
            {"CLOT-001", "T-Shirt Nike", 29.99, "Clothing", 50, "Active", "2026-01-20"}
        };
        
        for (Object[] row : allData) {
            if (category.equals(row[3])) {
                tableModel.addRow(row);
            }
        }
    }
    
    /**
     * Add new item
     */
    private void addItem() {
        JDialog dialog = createItemFormDialog("Add New Item", null);
        dialog.setVisible(true);
    }
    
    /**
     * Edit selected item
     */
    private void editItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an item to edit",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get item data
        String sku = (String) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        Double price = (Double) tableModel.getValueAt(selectedRow, 2);
        String category = (String) tableModel.getValueAt(selectedRow, 3);
        Integer stock = (Integer) tableModel.getValueAt(selectedRow, 4);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("sku", sku);
        itemData.put("name", name);
        itemData.put("price", price);
        itemData.put("category", category);
        itemData.put("stock", stock);
        itemData.put("status", status);
        
        JDialog dialog = createItemFormDialog("Edit Item", itemData);
        dialog.setVisible(true);
    }
    
    /**
     * Delete selected item
     */
    private void deleteItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an item to delete",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String sku = (String) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete item:\n\n" +
            "SKU: " + sku + "\n" +
            "Name: " + name + "\n\n" +
            "This action cannot be undone!",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // TODO Week 6: Call itemService.deleteItem(sku)
            
            tableModel.removeRow(selectedRow);
            
            JOptionPane.showMessageDialog(this,
                "Item deleted successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Update price (Admin only)
     */
    private void updatePrice() {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(this,
                "Only Admin users can modify prices!",
                "Permission Denied",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an item to update price",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String sku = (String) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        Double currentPrice = (Double) tableModel.getValueAt(selectedRow, 2);
        
        // Price update dialog
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panel.add(new JLabel("SKU:"));
        panel.add(new JLabel(sku));
        
        panel.add(new JLabel("Item:"));
        panel.add(new JLabel(name));
        
        panel.add(new JLabel("New Price: $"));
        JTextField txtNewPrice = new JTextField(String.valueOf(currentPrice));
        txtNewPrice.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(txtNewPrice);
        
        int result = JOptionPane.showConfirmDialog(this,
            panel,
            "Update Price (Admin Only)",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                double newPrice = Double.parseDouble(txtNewPrice.getText());
                
                if (newPrice <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Price must be greater than 0!",
                        "Invalid Price",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // TODO Week 6: Call itemService.updatePrice(sku, newPrice, currentUsername)
                
                tableModel.setValueAt(newPrice, selectedRow, 2);
                
                JOptionPane.showMessageDialog(this,
                    String.format("Price updated successfully!\n\n" +
                        "Old Price: $%.2f\n" +
                        "New Price: $%.2f", currentPrice, newPrice),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Invalid price format!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Create item form dialog
     */
    private JDialog createItemFormDialog(String title, Map<String, Object> itemData) {
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
        
        boolean isEditing = (itemData != null);
        
        // SKU field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("SKU Code: *"), gbc);
        gbc.gridx = 1;
        txtSKU = new JTextField(20);
        txtSKU.setFont(new Font("Courier New", Font.BOLD, 12));
        if (isEditing) {
            txtSKU.setText((String) itemData.get("sku"));
            txtSKU.setEditable(false); // Cannot change SKU when editing
            txtSKU.setBackground(new Color(240, 240, 240));
        }
        formPanel.add(txtSKU, gbc);
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Item Name: *"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(20);
        formPanel.add(txtName, gbc);
        
        // Price field
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Price ($): *"), gbc);
        gbc.gridx = 1;
        txtPrice = new JTextField(20);
        if (!isAdmin() && isEditing) {
            txtPrice.setEditable(false);
            txtPrice.setBackground(new Color(240, 240, 240));
            txtPrice.setToolTipText("Admin only");
        }
        formPanel.add(txtPrice, gbc);
        
        // Category field
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Category: *"), gbc);
        gbc.gridx = 1;
        String[] categories = Arrays.copyOfRange(CATEGORIES, 1, CATEGORIES.length);
        cmbCategory = new JComboBox<>(categories);
        formPanel.add(cmbCategory, gbc);
        
        // Stock quantity
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1;
        SpinnerNumberModel stockModel = new SpinnerNumberModel(0, 0, 9999, 1);
        spnStockQuantity = new JSpinner(stockModel);
        formPanel.add(spnStockQuantity, gbc);
        
        // Active checkbox
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Active:"), gbc);
        gbc.gridx = 1;
        chkActive = new JCheckBox("Item is active");
        chkActive.setSelected(true);
        formPanel.add(chkActive, gbc);
        
        // Populate data if editing
        if (itemData != null) {
            selectedSKU = (String) itemData.get("sku");
            txtName.setText((String) itemData.get("name"));
            txtPrice.setText(String.valueOf(itemData.get("price")));
            cmbCategory.setSelectedItem(itemData.get("category"));
            spnStockQuantity.setValue(itemData.get("stock"));
            chkActive.setSelected("Active".equals(itemData.get("status")));
        } else {
            selectedSKU = null;
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
            if (validateItemForm(isEditing)) {
                saveItem(dialog, isEditing);
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
     * Validate item form
     */
    private boolean validateItemForm(boolean isEditing) {
        // SKU validation
        String sku = txtSKU.getText().trim();
        if (sku.isEmpty()) {
            showError("SKU is required!");
            txtSKU.requestFocus();
            return false;
        }
        
        // SKU format validation (XXX-NNNNN)
        if (!sku.matches("^[A-Z]{4}-\\d{3}$")) {
            showError("Invalid SKU format!\n\n" +
                     "Format: XXXX-NNN\n" +
                     "Example: ELEC-001");
            txtSKU.requestFocus();
            return false;
        }
        
        // Check duplicate SKU (only for new items)
        if (!isEditing && isDuplicateSKU(sku)) {
            showError("SKU already exists!\n\nPlease use a different SKU code.");
            txtSKU.requestFocus();
            return false;
        }
        
        // Name validation
        if (txtName.getText().trim().isEmpty()) {
            showError("Item name is required!");
            txtName.requestFocus();
            return false;
        }
        
        // Price validation
        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            if (price <= 0) {
                showError("Price must be greater than 0!");
                txtPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid price format!");
            txtPrice.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if SKU is duplicate
     */
    private boolean isDuplicateSKU(String sku) {
        // TODO Week 6: Call itemService.checkSkuDuplicate(sku)
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingSKU = (String) tableModel.getValueAt(i, 0);
            if (sku.equals(existingSKU)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Save item (Add or Update)
     */
    private void saveItem(JDialog dialog, boolean isEditing) {
        String sku = txtSKU.getText().trim();
        String name = txtName.getText().trim();
        double price = Double.parseDouble(txtPrice.getText().trim());
        String category = (String) cmbCategory.getSelectedItem();
        int stock = (Integer) spnStockQuantity.getValue();
        String status = chkActive.isSelected() ? "Active" : "Inactive";
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        
        if (!isEditing) {
            // ADD NEW ITEM
            // TODO Week 6: Call itemService.addItem(...)
            
            tableModel.addRow(new Object[]{
                sku, name, price, category, stock, status, date
            });
            
            JOptionPane.showMessageDialog(this,
                "Item added successfully!\n\n" +
                "SKU: " + sku + "\n" +
                "Name: " + name,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
        } else {
            // UPDATE EXISTING ITEM
            // TODO Week 6: Call itemService.updateItem(...)
            
            int selectedRow = itemTable.getSelectedRow();
            tableModel.setValueAt(name, selectedRow, 1);
            
            // Only update price if admin
            if (isAdmin()) {
                tableModel.setValueAt(price, selectedRow, 2);
            }
            
            tableModel.setValueAt(category, selectedRow, 3);
            tableModel.setValueAt(stock, selectedRow, 4);
            tableModel.setValueAt(status, selectedRow, 5);
            
            JOptionPane.showMessageDialog(this,
                "Item updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        dialog.dispose();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Validation Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Update button states
     */
    private void updateButtonStates() {
        boolean hasSelection = itemTable.getSelectedRow() >= 0;
        btnEdit.setEnabled(hasSelection);
        btnDelete.setEnabled(hasSelection);
        btnUpdatePrice.setEnabled(hasSelection && isAdmin());
    }
    
    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(currentUserRole);
    }
    
    /**
     * Test method
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Item Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Test as Admin
            frame.add(new ItemPanel("ADMIN"));
            
            // Test as Staff
            // frame.add(new ItemPanel("STAFF"));
            
            frame.setSize(1100, 650);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("=== ItemPanel Test ===");
            System.out.println("Try adding items with SKU format: ELEC-001");
            System.out.println("Admin can update prices, Staff cannot");
        });
    }
}