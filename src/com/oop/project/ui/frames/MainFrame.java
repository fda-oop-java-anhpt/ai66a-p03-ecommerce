package com.oop.project.ui.frames;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.service.interfaces.*;
import com.oop.project.ui.panel.CouponPanel;
import com.oop.project.ui.panel.CustomerPanel;
import com.oop.project.ui.panel.DashboardPanel;
import com.oop.project.ui.panel.ItemPanel;
import com.oop.project.ui.panel.OrderPanel;
import com.oop.project.ui.panel.SettingsPanel;
import com.oop.project.ui.utils.UITheme;
import com.oop.project.repository.interfaces.SystemSettingRepository;
import com.oop.project.repository.impl.SystemSettingRepositoryImpl;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window — FR-6.3.
 * Avatar button in header → popup: Profile | Settings (Admin) | Logout
 * Settings → SettingsFrame (FR-0.4), Profile → ProfileFrame
 */
public class MainFrame extends JFrame {

    private final User        currentUser;
    private final IAuthService authService;

    public ICustomerService  customerService;
    public IItemService      itemService;
    public IBillingService   billingService;   
    public ICouponService    couponService;    
    public IDashboardService dashboardService; 

    private JTabbedPane    tabs;
    private CustomerPanel  customerPanel;
    private ItemPanel      itemPanel;
    private OrderPanel     orderPanel;
    private CouponPanel    couponPanel;
    private DashboardPanel dashboardPanel;
    private SettingsPanel  settingsPanel;  // Admin only

    public MainFrame(User user, IAuthService authService) {
        this.currentUser = user;
        this.authService = authService;
        buildUI();
    }

    private void buildUI() {
        setTitle("ShopFlow  —  " + currentUser.getUserName() + "  [" + currentUser.getUserRole() + "]");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { confirmLogout(); }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        setContentPane(root);

        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildTabs(),      BorderLayout.CENTER);
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

        JLabel logo = new JLabel("🛒  ShopFlow");
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

        h.add(logo,  BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    /** Circular avatar button with first letter of username. Popup on click. */
    private JButton buildAvatarButton() {
        // String letter = currentUser.getUserName() == null || currentUser.getUserName().isEmpty()
        //         ? "?" : String.valueOf(currentUser.getUserName().charAt(0)).toUpperCase();
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
                int x = (getWidth()  - fm.stringWidth(letter)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(letter, x, y);
                g2.dispose();
            }
            protected void paintBorder(Graphics g) {}
        };
        btn.setPreferredSize(new Dimension(38, 38));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
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

        JMenuItem profileMi = popupItem("Profile", UITheme.TEXT_PRIMARY);
        profileMi.addActionListener(e -> new ProfileFrame(this, currentUser).setVisible(true));
        popup.add(profileMi);

        // if (isAdmin()) {
        //     JMenuItem settingsMi = popupItem("⚙  Settings", UITheme.WARNING);
        //     settingsMi.addActionListener(e -> new SettingsFrame(this).setVisible(true));
        //     popup.add(settingsMi);
        // }

        JMenuItem auditMi = popupItem("Audit Log", UITheme.TEXT_PRIMARY);
        auditMi.addActionListener(e -> new AuditLogFrame(this).setVisible(true));
        popup.add(auditMi);
        popup.addSeparator();
        
        JMenuItem logoutMi = popupItem("⏻  Logout", UITheme.DANGER);
        logoutMi.addActionListener(e -> confirmLogout());
        popup.add(logoutMi);

        btn.addActionListener(e -> popup.show(btn, 0, btn.getHeight() + 4));
        return btn;
    }

    private JMenuItem popupItem(String text, Color fg) {
        JMenuItem mi = new JMenuItem(text);
        mi.setFont(UITheme.FONT_BODY); mi.setBackground(UITheme.BG_CARD);
        mi.setForeground(fg); mi.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 20));
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
            @Override protected void installDefaults() {
                super.installDefaults();
                // tabAreaBackground = UITheme.BG_DARK;
                shadow            = UITheme.BORDER_COLOR;
                darkShadow        = UITheme.BORDER_COLOR;
                focus             = UITheme.ACCENT;
                lightHighlight    = UITheme.BG_CARD;
            }
            @Override protected void paintTabBackground(Graphics g, int tp, int ti,
                                                         int x, int y, int w, int h, boolean sel) {
                g.setColor(sel ? UITheme.BG_CARD : UITheme.BG_DARK);
                g.fillRect(x, y, w, h);
                // Accent underline for selected tab
                if (sel) {
                    g.setColor(UITheme.ACCENT);
                    g.fillRect(x, y, w, 3);
                }
            }
            @Override protected void paintTabBorder(Graphics g, int tp, int ti,
                                                     int x, int y, int w, int h, boolean sel) {
                g.setColor(UITheme.BORDER_COLOR);
                g.drawRect(x, y, w - 1, h - 1);
            }
            @Override protected void paintText(Graphics g, int tp, Font f, FontMetrics fm,
                                               int ti, String title, Rectangle tr, boolean sel) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(sel ? UITheme.FONT_HEADING : UITheme.FONT_BODY);
                g2.setColor(sel ? UITheme.ACCENT : UITheme.TEXT_MUTED);
                FontMetrics nm = g2.getFontMetrics();
                int x = tr.x + (tr.width  - nm.stringWidth(title)) / 2;
                int y = tr.y + (tr.height + nm.getAscent() - nm.getDescent()) / 2;
                g2.drawString(title, x, y);
            }
            @Override protected int calculateTabHeight(int tp, int ti, int fh) { return 44; }
            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                int total = tabs.getTabCount();
                return total > 0 ? (tabs.getParent() != null ? tabs.getParent().getWidth() : tabs.getWidth()) / total : super.calculateTabWidth(tabPlacement, tabIndex, metrics);
            }
            @Override protected void paintFocusIndicator(Graphics g, int tp, Rectangle[] r,
                    int ti, Rectangle ir, Rectangle tr, boolean sel) { /* none */ }
        });

        customerPanel  = new CustomerPanel(this);
        itemPanel      = new ItemPanel(this);
        orderPanel     = new OrderPanel(this);
        couponPanel    = new CouponPanel(this);
        dashboardPanel = new DashboardPanel(this);
        // Settings tab — Admin only (FR-0.4)
        if (isAdmin()) {            
            // settingsTabIndex = tabs.getTabCount();           // index 5
            // tabs.addTab("  ⚙ Settings  ", settingsPanel);
            // // Give the settings tab a yellow tint to make it stand out
            // tabs.setForegroundAt(settingsTabIndex, UITheme.WARNING);
        }


        tabs.addTab("Customers",  customerPanel);
        tabs.addTab("Items",      itemPanel);
        tabs.addTab("Orders",     orderPanel);
        tabs.addTab("Coupons",    couponPanel);
        if (isAdmin()) {
            settingsPanel = new SettingsPanel(this);
            tabs.addTab("Settings",   settingsPanel);
        }
        tabs.addTab("Dashboard",  dashboardPanel);

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
            if (s != null) taxInfo = "  |  Tax Rate: " + s.getSettingValue() + "%";
        } catch (Exception ignored) {}
        JLabel r = UITheme.label("User: " + currentUser.getUserName() + "  |  Role: " + currentUser.getUserRole() + taxInfo);
        r.setFont(UITheme.FONT_SMALL);
        bar.add(l, BorderLayout.WEST); bar.add(r, BorderLayout.EAST);
        return bar;
    }

    // ── Menu ──────────────────────────────────────────────────────────────────
    // private JMenuBar buildMenuBar() {
    //     JMenuBar mb = new JMenuBar();
    //     mb.setBackground(UITheme.BG_DARK);
    //     mb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

    //     JMenu file = menu("File");
    //     JMenuItem ri = mi("Refresh"); ri.addActionListener(e -> refreshCurrent());
    //     JMenuItem li = mi("Logout");  li.addActionListener(e -> confirmLogout());
    //     JMenuItem ei = mi("Exit");    ei.addActionListener(e -> System.exit(0));
    //     file.add(ri); file.addSeparator(); file.add(li); file.add(ei);

    //     JMenu view = menu("View");
    //     String[] names = {"Customers","Items","Orders","Coupons","Dashboard"};
    //     for (int i=0; i<names.length; i++) {
    //         final int idx=i; JMenuItem m=mi("Go to "+names[i]);
    //         m.addActionListener(e->tabs.setSelectedIndex(idx)); view.add(m);
    //     }

    //     JMenu account = menu("Account");
    //     JMenuItem pm = mi("Profile"); 
    //     pm.addActionListener(e -> new ProfileFrame(this, currentUser).setVisible(true));
    //     account.add(pm);
    //     // if (isAdmin()) {
    //     //     JMenuItem sm = mi("Settings (Admin)"); 
    //     //     sm.addActionListener(e -> new SettingsFrame(this).setVisible(true));
    //     //     account.add(sm);
    //     // }
    //     account.addSeparator();
    //     JMenuItem lm = mi("Logout"); 
    //     lm.addActionListener(e -> confirmLogout()); account.add(lm);

    //     JMenu help = menu("Help");
    //     JMenuItem ab = mi("About");
    //     ab.addActionListener(e -> UITheme.showScrollable(this,
    //         "ShopFlow E-Commerce Billing System\nVersion 1.0\n\nBuilt with Java Swing + PostgreSQL.\n\n"
    //         + "Default credentials:\n  Admin — admin_1 / admin@123\n  Staff — nv_hoang / staff@123",
    //         "About ShopFlow"));
    //     help.add(ab);

    //     mb.add(file); mb.add(view); mb.add(account); mb.add(help);
    //     return mb;
    // }

    // private JMenu     menu(String t) { JMenu     m = new JMenu(t);     m.setFont(UITheme.FONT_BODY); m.setForeground(UITheme.TEXT_PRIMARY); return m; }
    // private JMenuItem mi  (String t) { JMenuItem m = new JMenuItem(t); m.setFont(UITheme.FONT_BODY); m.setBackground(UITheme.BG_CARD); m.setForeground(UITheme.TEXT_PRIMARY); return m; }

    private void onTabChange(int i) {
        if      (i==0) customerPanel .refresh();
        else if (i==1) itemPanel     .refresh();
        else if (i==2) orderPanel    .refresh();
        else if (i==3) couponPanel   .refresh();
        else if (i==4) dashboardPanel.refresh();
    }
    // private void refreshCurrent() { onTabChange(tabs.getSelectedIndex()); }

    private void confirmLogout() {
        if (UITheme.confirm(this, "Log out of ShopFlow?", "Confirm Logout")) {
            authService.logout(currentUser);
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    public User    getCurrentUser() { return currentUser; }
    public boolean isAdmin()        { return currentUser.getUserRole() == UserRole.ADMIN; }
}