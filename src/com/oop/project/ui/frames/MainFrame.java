package com.oop.project.ui.frames;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.repository.interfaces.CouponRepository;
import com.oop.project.repository.interfaces.CustomerRepository;
import com.oop.project.repository.interfaces.ItemRepository;
import com.oop.project.repository.interfaces.OrderDetailRepository;
import com.oop.project.repository.interfaces.OrderRepository;
import com.oop.project.repository.interfaces.SystemSettingRepository;
import com.oop.project.repository.interfaces.UserRepository;
import com.oop.project.repository.impl.AuditLogRepositoryImpl;
import com.oop.project.repository.impl.CouponRepositoryImpl;
import com.oop.project.repository.impl.CustomerRepositoryImpl;
import com.oop.project.repository.impl.ItemRepositoryImpl;
import com.oop.project.repository.impl.OrderDetailRepositoryImpl;
import com.oop.project.repository.impl.OrderRepositoryImpl;
import com.oop.project.repository.impl.SystemSettingRepositoryImpl;
import com.oop.project.repository.impl.UserRepositoryImpl;
import com.oop.project.service.interfaces.*;
import com.oop.project.service.impl.*;
import com.oop.project.ui.dialogs.AuditLogDialog;
import com.oop.project.ui.dialogs.ProfileDialog;
import com.oop.project.ui.panel.CouponPanel;
import com.oop.project.ui.panel.CustomerPanel;
import com.oop.project.ui.panel.DashboardPanel;
import com.oop.project.ui.panel.ItemPanel;
import com.oop.project.ui.panel.OrderPanel;
import com.oop.project.ui.panel.SettingsPanel;
import com.oop.project.ui.panel.StaffPanel;
import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window — FR-6.3.
 * Avatar button in header → popup: Profile | Settings (Admin) | Logout
 * Settings → SettingsFrame (FR-0.4), Profile → ProfileDialog
 */
public class MainFrame extends JFrame {

    private final User currentUser;
    private final IAuthService authService;

    public final ICustomerService customerService;
    public final IItemService itemService;
    public final IBillingService billingService;
    public final ICouponService couponService;
    public final IDashboardService dashboardService;
    public final IUserService userService;

    private JTabbedPane tabs;
    private CustomerPanel customerPanel;
    private ItemPanel itemPanel;
    private OrderPanel orderPanel;
    private CouponPanel couponPanel;
    private DashboardPanel dashboardPanel;
    private SettingsPanel settingsPanel; // Admin only
    private StaffPanel staffPanel;       // Admin only

    public MainFrame(User user, IAuthService authService) {
        this.currentUser = user;
        this.authService = authService;

        // Initialize Repositories
        AuditLogRepository auditLogRepo = new AuditLogRepositoryImpl();
        CustomerRepository customerRepo = new CustomerRepositoryImpl();
        ItemRepository itemRepo = new ItemRepositoryImpl();
        OrderRepository orderRepo = new OrderRepositoryImpl();
        OrderDetailRepository orderDetailRepo = new OrderDetailRepositoryImpl();
        CouponRepository couponRepo = new CouponRepositoryImpl();
        SystemSettingRepository settingRepo = new SystemSettingRepositoryImpl();
        UserRepository userRepo = new UserRepositoryImpl();

        // Initialize Services
        this.customerService = new CustomerServiceImpl(customerRepo, orderRepo, auditLogRepo);
        this.itemService = new ItemServiceImpl(itemRepo, auditLogRepo);
        this.couponService = new CouponServiceImpl(couponRepo);
        this.dashboardService = new DashboardServiceImpl(orderRepo);
        this.billingService = new BillingServiceImpl(orderRepo, auditLogRepo, settingRepo, couponRepo, itemRepo,
                orderDetailRepo);
        this.userService = new UserServiceImpl(userRepo, auditLogRepo);

        buildUI();
    }

    private void buildUI() {
        setTitle("ShopFlow  —  " + currentUser.getUserName() + "  [" + currentUser.getUserRole() + "]");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                confirmLogout();
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        tabs.addChangeListener(e -> onTabChange(tabs.getSelectedIndex()));
        setSize(1280, 820);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(10, 14, 24));
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 22, 8, 22)));

        JLabel logo = new JLabel("ShopFlow");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        logo.setForeground(UITheme.ACCENT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        boolean admin = isAdmin();
        JLabel roleBadge = new JLabel(admin ? "● ADMIN" : "● STAFF");
        roleBadge.setFont(UITheme.FONT_BADGE);
        roleBadge.setForeground(admin ? UITheme.ACCENT : UITheme.SUCCESS);
        right.add(roleBadge);
        right.add(buildAvatarButton());

        h.add(logo, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    /** Circular avatar button with first letter of username. Popup on click. */
    private JButton buildAvatarButton() {
        String letter = String.valueOf(currentUser.getUserName().charAt(0)).toUpperCase();
        Color col = isAdmin() ? UITheme.ACCENT : UITheme.SUCCESS;

        JButton btn = new JButton() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? col.darker() : col);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(letter)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(letter, x, y);
                g2.dispose();
            }

            protected void paintBorder(Graphics g) {
            }
        };
        btn.setPreferredSize(new Dimension(38, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(currentUser.getUserName() + " - User navigation menu");

        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(UITheme.BG_CARD);
        popup.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));

        // Header row inside popup
        JMenuItem hdr = new JMenuItem(currentUser.getUserName());
        hdr.setFont(UITheme.FONT_BADGE);
        hdr.setForeground(UITheme.TEXT_MUTED);
        hdr.setBackground(UITheme.BG_CARD);
        hdr.setEnabled(false);
        popup.add(hdr);
        popup.addSeparator();

        JMenuItem profileMi = popupItem("Profile", UITheme.ACCENT);
        profileMi.addActionListener(e -> new ProfileDialog(this, currentUser).setVisible(true));
        popup.add(profileMi);

        if (isAdmin()) { // chi admin duoc xem audit log
            JMenuItem auditMi = popupItem("Audit Log", UITheme.SUCCESS);
            auditMi.addActionListener(e -> new AuditLogDialog(this).setVisible(true));
            popup.add(auditMi);
        }
        popup.addSeparator();

        JMenuItem logoutMi = popupItem("Logout", UITheme.DANGER);
        logoutMi.addActionListener(e -> confirmLogout());
        popup.add(logoutMi);

        btn.addActionListener(e -> popup.show(btn, 0, btn.getHeight() + 4));
        return btn;
    }

    private JMenuItem popupItem(String text, Color fg) {
        JMenuItem mi = new JMenuItem(text);
        mi.setFont(new Font("Segoe UI", Font.BOLD, 13));
        mi.setBackground(UITheme.BG_CARD);
        mi.setForeground(fg);
        mi.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 28));
        mi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return mi;
    }

    // ── Tabs — fix 5: custom dark UI so tab text is readable ─────────────────
    private JTabbedPane buildTabs() {
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(UITheme.BG_DARK);
        tabs.setForeground(UITheme.TEXT_MUTED);
        tabs.setFont(UITheme.FONT_BODY);

        // Override tab UI so selected tab = BG_CARD background + ACCENT text,
        // unselected = BG_DARK background + TEXT_MUTED text
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                shadow = UITheme.BORDER_COLOR;
                darkShadow = UITheme.BORDER_COLOR;
                focus = UITheme.ACCENT;
                lightHighlight = UITheme.BG_CARD;
            }

            @Override
            protected void paintTabBackground(Graphics g, int tp, int ti,
                    int x, int y, int w, int h, boolean sel) {
                g.setColor(sel ? UITheme.BG_CARD : UITheme.BG_DARK);
                g.fillRect(x, y, w, h);
                // Accent underline for selected tab
                if (sel) {
                    g.setColor(UITheme.ACCENT);
                    g.fillRect(x, y, w, 3);
                }
            }

            @Override
            protected void paintTabBorder(Graphics g, int tp, int ti,
                    int x, int y, int w, int h, boolean sel) {
                g.setColor(UITheme.BORDER_COLOR);
                g.drawRect(x, y, w - 1, h - 1);
            }

            @Override
            protected void paintText(Graphics g, int tp, Font f, FontMetrics fm,
                    int ti, String title, Rectangle tr, boolean sel) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(sel ? UITheme.FONT_HEADING : UITheme.FONT_BODY);
                g2.setColor(sel ? UITheme.ACCENT : UITheme.TEXT_MUTED);
                FontMetrics nm = g2.getFontMetrics();
                int x = tr.x + (tr.width - nm.stringWidth(title)) / 2;
                int y = tr.y + (tr.height + nm.getAscent() - nm.getDescent()) / 2;
                g2.drawString(title, x, y);
            }

            @Override
            protected int calculateTabHeight(int tp, int ti, int fh) {
                return 44;
            }

            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                int total = tabs.getTabCount();
                return total > 0 ? (tabs.getParent() != null ? tabs.getParent().getWidth() : tabs.getWidth()) / total
                        : super.calculateTabWidth(tabPlacement, tabIndex, metrics);
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tp, Rectangle[] r,
                    int ti, Rectangle ir, Rectangle tr, boolean sel) {
                /* none */ }
        });

        customerPanel = new CustomerPanel(this);
        itemPanel = new ItemPanel(this);
        orderPanel = new OrderPanel(this);
        couponPanel = new CouponPanel(this);
        dashboardPanel = new DashboardPanel(this);

        tabs.addTab("Customers", customerPanel);
        tabs.addTab("Items", itemPanel);
        tabs.addTab("Orders", orderPanel);
        tabs.addTab("Coupons", couponPanel);
        if (isAdmin()) {
            settingsPanel = new SettingsPanel(this);
            tabs.addTab("Settings", settingsPanel);
            staffPanel = new StaffPanel(this, userService);
            tabs.addTab("Staff", staffPanel);
        }
        tabs.addTab("Dashboard", dashboardPanel);

        return tabs;
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(10, 14, 24));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 16, 4, 16)));
        JLabel l = UITheme.label("ShopFlow E-Commerce Billing System  v1.0");
        l.setFont(UITheme.FONT_SMALL);
        String taxInfo = "";
        try {
            SystemSettingRepository sr = new SystemSettingRepositoryImpl();
            com.oop.project.model.SystemSetting s = sr.findByKey("TAX_RATE");
            if (s != null)
                taxInfo = "  |  Tax Rate: " + s.getSettingValue() + "%";
        } catch (Exception ignored) {
        }
        JLabel r = UITheme
                .label("User: " + currentUser.getUserName() + "  |  Role: " + currentUser.getUserRole() + taxInfo);
        r.setFont(UITheme.FONT_SMALL);
        bar.add(l, BorderLayout.WEST);
        bar.add(r, BorderLayout.EAST);
        return bar;
    }

    private void onTabChange(int i) {
        if (i == 0)
            customerPanel.refresh();
        else if (i == 1)
            itemPanel.refresh();
        else if (i == 2)
            orderPanel.refresh();
        else if (i == 3)
            couponPanel.refresh();
        else if (isAdmin() && i == 4)
            settingsPanel.revalidate(); // settings
        else if (isAdmin() && i == 5)
            staffPanel.refresh();
        else
            dashboardPanel.refresh(); // last tab = dashboard
    }

    private void confirmLogout() {
        if (UITheme.confirm(this, "Are you sure you want to logout?", "Confirm Logout")) {
            authService.logout(currentUser);
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser.getUserRole() == UserRole.ADMIN;
    }
}