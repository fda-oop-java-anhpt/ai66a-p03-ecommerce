package com.oop.project.ui.frames;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.service.interfaces.*;
import com.oop.project.service.impl.*;
import com.oop.project.ui.panels.*;
import com.oop.project.ui.utils.UITheme;
import com.oop.project.ui.utils.DialogUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window with JTabbedPane and menu bar.
 * FR-6.3: Tabs for Customers, Items, Orders, Billing, Dashboard.
 * Role-based tab visibility — Admin sees all, Staff cannot modify prices.
 */
public class MainFrame extends JFrame {

    private final AuthenticationService authService;
    private final User currentUser;

    // Services — injected/created here so panels share the same instances
    private CustomerService   customerService;
    private ItemService       itemService;
    private OrderService      orderService;
    private BillingService    billingService;
    private CouponService     couponService;

    private JTabbedPane tabbedPane;
    private CustomerPanel   customerPanel;
    private ItemPanel       itemPanel;
    private OrderPanel      orderPanel;
    private BillingPanel    billingPanel;
    private DashboardPanel  dashboardPanel;

    public MainFrame(AuthenticationService authService, User currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
        initServices();
        initUI();
    }

    // ── Service initialization ────────────────────────────────────────────────
    // Replace constructors with your actual service implementations as needed.
    private void initServices() {
        customerService = new CustomerServiceImpl();
        itemService     = new ItemServiceImpl();
        orderService    = new OrderServiceImpl();
        billingService  = new BillingServiceImpl();
        couponService   = new CouponServiceImpl();
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    private void initUI() {
        setTitle("ShopFlow — " + currentUser.getUserName()
                + " (" + currentUser.getUserRole() + ")");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmLogout(); }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        setContentPane(root);

        // ── Top header bar ────────────────────────────────────────────────────
        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        // ── Tabbed pane ───────────────────────────────────────────────────────
        tabbedPane = buildTabbedPane();
        root.add(tabbedPane, BorderLayout.CENTER);

        // ── Menu bar ──────────────────────────────────────────────────────────
        setJMenuBar(buildMenuBar());

        // Listen for tab changes — refresh data when switching tabs
        tabbedPane.addChangeListener(e -> onTabChanged(tabbedPane.getSelectedIndex()));

        setSize(1200, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(10, 14, 24));
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel logo = new JLabel("🛒 ShopFlow");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(UITheme.ACCENT);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightSide.setOpaque(false);

        boolean isAdmin = currentUser.getUserRole() == UserRole.ADMIN;
        JLabel roleBadge = new JLabel(isAdmin ? "● ADMIN" : "● STAFF");
        roleBadge.setFont(UITheme.FONT_BADGE);
        roleBadge.setForeground(isAdmin ? UITheme.ACCENT : UITheme.SUCCESS);

        JLabel userLbl = UITheme.label("Logged in as  " + currentUser.getUserName());

        JButton logoutBtn = UITheme.ghostButton("Logout");
        logoutBtn.addActionListener(e -> confirmLogout());

        rightSide.add(userLbl);
        rightSide.add(roleBadge);
        rightSide.add(logoutBtn);

        h.add(logo, BorderLayout.WEST);
        h.add(rightSide, BorderLayout.EAST);
        return h;
    }

    private JTabbedPane buildTabbedPane() {
        JTabbedPane tp = new JTabbedPane(JTabbedPane.TOP);
        tp.setBackground(UITheme.BG_DARK);
        tp.setForeground(UITheme.TEXT_PRIMARY);
        tp.setFont(UITheme.FONT_BODY);
        tp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        customerPanel  = new CustomerPanel(customerService, currentUser);
        itemPanel      = new ItemPanel(itemService, currentUser);
        orderPanel     = new OrderPanel(orderService, customerService, itemService,
                                        billingService, couponService, currentUser);
        billingPanel   = new BillingPanel(billingService, couponService, orderService);
        dashboardPanel = new DashboardPanel(orderService, customerService, itemService);

        tp.addTab("  👤 Customers  ", customerPanel);
        tp.addTab("  📦 Items  ",     itemPanel);
        tp.addTab("  🛍 Orders  ",    orderPanel);
        tp.addTab("  💳 Billing  ",   billingPanel);
        tp.addTab("  📊 Dashboard  ", dashboardPanel);

        return tp;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(UITheme.BG_DARK);
        mb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

        JMenu fileMenu = styledMenu("File");
        JMenuItem refreshItem = styledMenuItem("Refresh");
        JMenuItem logoutItem  = styledMenuItem("Logout");
        JMenuItem exitItem    = styledMenuItem("Exit");
        refreshItem.addActionListener(e -> refreshCurrentTab());
        logoutItem .addActionListener(e -> confirmLogout());
        exitItem   .addActionListener(e -> System.exit(0));
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.add(exitItem);

        JMenu helpMenu = styledMenu("Help");
        JMenuItem aboutItem = styledMenuItem("About");
        aboutItem.addActionListener(e -> DialogUtils.showInfo(this,
                "ShopFlow E-Commerce Billing System\nVersion 1.0", "About"));
        helpMenu.add(aboutItem);

        mb.add(fileMenu);
        mb.add(helpMenu);
        return mb;
    }

    private JMenu styledMenu(String text) {
        JMenu m = new JMenu(text);
        m.setFont(UITheme.FONT_BODY);
        m.setForeground(UITheme.TEXT_PRIMARY);
        return m;
    }

    private JMenuItem styledMenuItem(String text) {
        JMenuItem mi = new JMenuItem(text);
        mi.setFont(UITheme.FONT_BODY);
        mi.setBackground(UITheme.BG_CARD);
        mi.setForeground(UITheme.TEXT_PRIMARY);
        return mi;
    }

    // ── Tab change handler ────────────────────────────────────────────────────
    private void onTabChanged(int index) {
        switch (index) {
            case 0 -> customerPanel .refreshTable();
            case 1 -> itemPanel     .refreshTable();
            case 2 -> orderPanel    .refreshTable();
            case 3 -> billingPanel  .reset();
            case 4 -> dashboardPanel.refresh();
        }
    }

    private void refreshCurrentTab() {
        onTabChanged(tabbedPane.getSelectedIndex());
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    private void confirmLogout() {
        if (DialogUtils.confirm(this, "Do you want to log out?", "Confirm Logout")) {
            authService.logout(currentUser);
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame(authService);
                login.setVisible(true);
            });
        }
    }
}