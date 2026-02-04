

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * MainFrame - Cửa sổ chính của E-Commerce System
 * Tuần 5 - UI Advanced
 * 
 * Features:
 * - JTabbedPane với 5 tabs: Customers, Items, Orders, Billing, Dashboard
 * - Menu bar với File, Settings, Help, Logout
 * - Role-based tab visibility (Admin vs Staff)
 * - Status bar với thông tin user và thời gian
 * - Welcome message
 * 
 * @author Thành viên 4 (UI Layer)
 */
public class MainFrame extends JFrame {
    
    // User information
    private String currentUsername;
    private String currentRole;
    
    // UI Components
    private JTabbedPane tabbedPane;
    private JMenuBar menuBar;
    private JPanel statusBar;
    private JLabel lblUsername;
    private JLabel lblRole;
    private JLabel lblDateTime;
    
    // Tab panels (will be implemented separately)
    private JPanel customerPanel;
    private JPanel itemPanel;
    private JPanel orderPanel;
    private JPanel billingPanel;
    private JPanel dashboardPanel;
    
    // Timer for status bar
    private Timer clockTimer;
    
    // Color Scheme
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 250);
    private static final Color TAB_SELECTED = PRIMARY_COLOR;
    private static final Color TAB_BACKGROUND = Color.WHITE;
    
    /**
     * Constructor - Khởi tạo MainFrame
     * 
     * @param username Username của user đã login
     * @param role Role của user (ADMIN hoặc STAFF)
     */
    public MainFrame(String username, String role) {
        this.currentUsername = username;
        this.currentRole = role;
        
        initComponents();
        setupMenuBar();
        setupTabbedPane();
        setupStatusBar();
        setupUI();
        startClock();
        
        // Show welcome message
        showWelcomeMessage();
    }
    
    /**
     * Khởi tạo các components
     */
    private void initComponents() {
        // Initialize tab panels (placeholders for now)
        customerPanel = new CustomerPanel();
        itemPanel = new ItemPanel(currentRole);     // NEW!
        orderPanel = new OrderPanel(); 
        billingPanel = createPlaceholderPanel("Billing System", 
            "Calculate bills, apply discounts, and generate invoices");
        dashboardPanel = createPlaceholderPanel("Dashboard", 
            "View statistics, search orders, and analytics");
        
        // Status bar labels
        lblUsername = new JLabel();
        lblRole = new JLabel();
        lblDateTime = new JLabel();
    }
    
    /**
     * Setup menu bar
     */
    private void setupMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem refreshItem = createMenuItem("Refresh", "F5", KeyEvent.VK_F5, 
            e -> refreshCurrentTab());
        JMenuItem exportItem = createMenuItem("Export Data...", "Ctrl+E", 
            KeyEvent.VK_E, e -> handleExport());
        JMenuItem printItem = createMenuItem("Print...", "Ctrl+P", 
            KeyEvent.VK_P, e -> handlePrint());
        JMenuItem exitItem = createMenuItem("Exit", "Alt+F4", KeyEvent.VK_F4, 
            e -> handleExit());
        
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exportItem);
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Settings menu
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_S);
        
        JMenuItem preferencesItem = createMenuItem("Preferences...", "Ctrl+,", 
            KeyEvent.VK_COMMA, e -> showPreferences());
        JMenuItem themeItem = createMenuItem("Change Theme...", null, 0, 
            e -> showThemeSelector());
        
        // Admin-only settings
        if (isAdmin()) {
            JMenuItem taxSettingsItem = createMenuItem("Tax Settings...", null, 0, 
                e -> showTaxSettings());
            JMenuItem userManagementItem = createMenuItem("User Management...", null, 0, 
                e -> showUserManagement());
            
            settingsMenu.add(taxSettingsItem);
            settingsMenu.add(userManagementItem);
            settingsMenu.addSeparator();
        }
        
        settingsMenu.add(preferencesItem);
        settingsMenu.add(themeItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem userGuideItem = createMenuItem("User Guide", "F1", KeyEvent.VK_F1, 
            e -> showUserGuide());
        JMenuItem shortcutsItem = createMenuItem("Keyboard Shortcuts", null, 0, 
            e -> showKeyboardShortcuts());
        JMenuItem aboutItem = createMenuItem("About", null, 0, 
            e -> showAbout());
        
        helpMenu.add(userGuideItem);
        helpMenu.add(shortcutsItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        // Logout menu (special - right aligned)
        JMenu logoutMenu = new JMenu("Logout");
        logoutMenu.setForeground(WARNING_COLOR);
        logoutMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                handleLogout();
            }
            public void menuDeselected(MenuEvent e) {}
            public void menuCanceled(MenuEvent e) {}
        });
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        menuBar.add(Box.createHorizontalGlue()); // Push logout to right
        menuBar.add(logoutMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Helper method to create menu items with shortcuts
     */
    private JMenuItem createMenuItem(String text, String shortcut, int keyCode, 
                                     ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        
        if (shortcut != null && !shortcut.isEmpty()) {
            item.setAccelerator(KeyStroke.getKeyStroke(shortcut));
        }
        
        if (listener != null) {
            item.addActionListener(listener);
        }
        
        return item;
    }
    
    /**
     * Setup tabbed pane with role-based visibility
     */
    private void setupTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));
        tabbedPane.setBackground(TAB_BACKGROUND);
        
        // Add tabs with icons
        tabbedPane.addTab("👥 Customers", customerPanel);
        tabbedPane.addTab("📦 Items", itemPanel);
        tabbedPane.addTab("🛒 Orders", orderPanel);
        tabbedPane.addTab("💰 Billing", billingPanel);
        tabbedPane.addTab("📊 Dashboard", dashboardPanel);
        
        // Role-based tab restrictions
        if (!isAdmin()) {
            // Staff cannot modify prices, so disable item editing
            tabbedPane.setEnabledAt(1, true); // Can view items
            // Add tooltip
            tabbedPane.setToolTipTextAt(1, "View only - Admin required for price changes");
        }
        
        // Tab change listener
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            String tabName = tabbedPane.getTitleAt(index);
            updateStatusBar("Viewing: " + tabName);
        });
        
        // Custom tab colors
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                                             int tabIndex, int x, int y, int w, int h,
                                             boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                if (isSelected) {
                    g2d.setColor(TAB_SELECTED);
                } else {
                    g2d.setColor(TAB_BACKGROUND);
                }
                g2d.fillRect(x, y, w, h);
            }
            
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font,
                                   FontMetrics metrics, int tabIndex, String title,
                                   Rectangle textRect, boolean isSelected) {
                g.setColor(isSelected ? Color.WHITE : Color.BLACK);
                super.paintText(g, tabPlacement, font, metrics, tabIndex, title, 
                              textRect, isSelected);
            }
        });
    }
    
    /**
     * Setup status bar at bottom
     */
    private void setupStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(240, 240, 240));
        statusBar.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Left panel - User info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        
        lblUsername.setText(currentUsername);
        lblUsername.setFont(new Font("Arial", Font.BOLD, 11));
        
        lblRole.setText("[" + currentRole + "]");
        lblRole.setFont(new Font("Arial", Font.PLAIN, 11));
        lblRole.setForeground(isAdmin() ? SUCCESS_COLOR : PRIMARY_COLOR);
        
        leftPanel.add(userIcon);
        leftPanel.add(lblUsername);
        leftPanel.add(lblRole);
        
        // Right panel - Date/Time
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        JLabel clockIcon = new JLabel("🕐");
        clockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        
        lblDateTime.setFont(new Font("Arial", Font.PLAIN, 11));
        updateDateTime();
        
        rightPanel.add(clockIcon);
        rightPanel.add(lblDateTime);
        
        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(rightPanel, BorderLayout.EAST);
    }
    
    /**
     * Setup main UI
     */
    private void setupUI() {
        setTitle("E-Commerce System - Sales & Billing Management");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Handle window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
        
        // Main layout
        setLayout(new BorderLayout());
        
        // Add components
        add(tabbedPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        
        // Window settings
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        // Icon (optional)
        // setIconImage(new ImageIcon("icon.png").getImage());
    }
    
    /**
     * Create placeholder panel for tabs (to be replaced with actual panels)
     */
    private JPanel createPlaceholderPanel(String title, String description) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Icon
        JLabel iconLabel = new JLabel("🚧");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        panel.add(iconLabel, gbc);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, gbc);
        
        // Description
        JLabel descLabel = new JLabel("<html><center>" + description + 
                                      "<br><br><i>Panel will be implemented in Week 5-6</i></center></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descLabel.setForeground(Color.GRAY);
        panel.add(descLabel, gbc);
        
        // Test button
        JButton testBtn = new JButton("Test Feature");
        testBtn.setFont(new Font("Arial", Font.BOLD, 12));
        testBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "This feature will be implemented soon!\n\n" +
                "Panel: " + title + "\n" +
                "User: " + currentUsername + "\n" +
                "Role: " + currentRole,
                "Coming Soon",
                JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(testBtn, gbc);
        
        return panel;
    }
    
    /**
     * Start clock timer for status bar
     */
    private void startClock() {
        clockTimer = new Timer(1000, e -> updateDateTime());
        clockTimer.start();
    }
    
    /**
     * Update date/time in status bar
     */
    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy - HH:mm:ss");
        lblDateTime.setText(sdf.format(new Date()));
    }
    
    /**
     * Update status bar message (temporary, for notifications)
     */
    private void updateStatusBar(String message) {
        // Could add a message label to status bar
        System.out.println("Status: " + message);
    }
    
    /**
     * Show welcome message when first opening
     */
    private void showWelcomeMessage() {
        SwingUtilities.invokeLater(() -> {
            String welcomeMsg = String.format(
                "Welcome back, %s!\n\n" +
                "You are logged in as: %s\n" +
                "Current time: %s\n\n" +
                "Use the tabs above to navigate through different modules.",
                currentUsername,
                currentRole,
                new SimpleDateFormat("HH:mm:ss").format(new Date())
            );
            
            JOptionPane.showMessageDialog(
                this,
                welcomeMsg,
                "Welcome to E-Commerce System",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
    
    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(currentRole);
    }
    
    // ==================== Menu Handlers ====================
    
    private void refreshCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        String tabName = tabbedPane.getTitleAt(selectedIndex);
        
        JOptionPane.showMessageDialog(this,
            "Refreshing: " + tabName + "\n\n" +
            "TODO: Reload data from database",
            "Refresh",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void handleExport() {
        String[] options = {"Excel (XLSX)", "PDF", "CSV", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Select export format:",
            "Export Data",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice >= 0 && choice < 3) {
            JOptionPane.showMessageDialog(this,
                "Export to " + options[choice] + " will be implemented in Week 7",
                "Export",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void handlePrint() {
        JOptionPane.showMessageDialog(this,
            "Print functionality will be implemented in Week 7",
            "Print",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?\n\n" +
            "Any unsaved changes will be lost.",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // TODO: Call logout service
            if (clockTimer != null) {
                clockTimer.stop();
            }
            dispose();
            System.exit(0);
        }
    }
    
    private void showPreferences() {
        JOptionPane.showMessageDialog(this,
            "Preferences dialog will be implemented in Week 8",
            "Preferences",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showThemeSelector() {
        String[] themes = {"Light", "Dark", "Blue", "Green"};
        String theme = (String) JOptionPane.showInputDialog(
            this,
            "Select theme:",
            "Change Theme",
            JOptionPane.QUESTION_MESSAGE,
            null,
            themes,
            themes[0]
        );
        
        if (theme != null) {
            JOptionPane.showMessageDialog(this,
                "Theme '" + theme + "' will be applied in Week 8",
                "Theme",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showTaxSettings() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Tax Rate (%):"));
        JTextField txtTaxRate = new JTextField("8.0");
        panel.add(txtTaxRate);
        
        panel.add(new JLabel("Tax Enabled:"));
        JCheckBox chkEnabled = new JCheckBox("", true);
        panel.add(chkEnabled);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Tax Settings (Admin Only)",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(this,
                "Tax settings updated!\n" +
                "Rate: " + txtTaxRate.getText() + "%\n" +
                "Enabled: " + chkEnabled.isSelected(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showUserManagement() {
        JOptionPane.showMessageDialog(this,
            "User Management (Admin Only)\n\n" +
            "Features:\n" +
            "- Add/Remove users\n" +
            "- Change roles\n" +
            "- Reset passwords\n" +
            "\nWill be implemented in Week 8",
            "User Management",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showUserGuide() {
        String guide = 
            "E-Commerce System - User Guide\n\n" +
            "NAVIGATION:\n" +
            "- Use tabs to switch between modules\n" +
            "- Customers: Manage customer information\n" +
            "- Items: Manage product catalog\n" +
            "- Orders: Create and track orders\n" +
            "- Billing: Calculate bills and invoices\n" +
            "- Dashboard: View reports and statistics\n\n" +
            "SHORTCUTS:\n" +
            "- F5: Refresh current tab\n" +
            "- Ctrl+E: Export data\n" +
            "- Ctrl+P: Print\n" +
            "- F1: Show this help\n" +
            "- Alt+F4: Exit application";
        
        JTextArea textArea = new JTextArea(guide);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this,
            scrollPane,
            "User Guide",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showKeyboardShortcuts() {
        String shortcuts = 
            "Keyboard Shortcuts:\n\n" +
            "FILE MENU:\n" +
            "F5          - Refresh\n" +
            "Ctrl+E      - Export Data\n" +
            "Ctrl+P      - Print\n" +
            "Alt+F4      - Exit\n\n" +
            "HELP MENU:\n" +
            "F1          - User Guide\n\n" +
            "NAVIGATION:\n" +
            "Ctrl+1-5    - Switch tabs\n" +
            "Tab         - Next field\n" +
            "Shift+Tab   - Previous field\n" +
            "Enter       - Submit/Confirm\n" +
            "Esc         - Cancel";
        
        JTextArea textArea = new JTextArea(shortcuts);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 350));
        
        JOptionPane.showMessageDialog(this,
            scrollPane,
            "Keyboard Shortcuts",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAbout() {
        String about = 
            "E-Commerce System\n" +
            "Sales & Billing Management\n\n" +
            "Version: 1.0.0\n" +
            "Build Date: January 28, 2026\n\n" +
            "Developed by:\n" +
            "- Thành viên 1 (Model Layer)\n" +
            "- Thành viên 2 (Repository Layer)\n" +
            "- Thành viên 3 (Service Layer)\n" +
            "- Thành viên 4 (UI Layer)\n\n" +
            "© 2026 E-Commerce Team\n" +
            "All rights reserved.";
        
        JOptionPane.showMessageDialog(this,
            about,
            "About E-Commerce System",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // TODO: Call AuthenticationService.logout()
            if (clockTimer != null) {
                clockTimer.stop();
            }
            
            JOptionPane.showMessageDialog(this,
                "You have been logged out successfully.\n\n" +
                "Thank you for using E-Commerce System!",
                "Logged Out",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
            // Return to login screen
            SwingUtilities.invokeLater(() -> {
                // Assuming LoginFrame exists
                // new LoginFrame().setVisible(true);
                System.exit(0); // Temporary - replace with login screen
            });
        }
    }
    
    /**
     * Main method for testing MainFrame independently
     */
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Test as Admin
            MainFrame frame = new MainFrame("admin", "ADMIN");
            frame.setVisible(true);
            
            System.out.println("=== MainFrame Test Mode ===");
            System.out.println("User: admin");
            System.out.println("Role: ADMIN");
            System.out.println("\nAll tabs are placeholders.");
            System.out.println("Actual panels will be implemented in Week 5-6.");
        });
    }
}