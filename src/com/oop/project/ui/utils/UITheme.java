package com.oop.project.ui.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Central theme constants for the E-Commerce UI.
 * All panels should use these constants for consistent styling.
 */
public class UITheme {

    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color BG_DARK      = new Color(15, 18, 28);
    public static final Color BG_CARD      = new Color(24, 29, 45);
    public static final Color BG_INPUT     = new Color(32, 38, 58);
    public static final Color BG_ROW_ALT   = new Color(28, 34, 52);
    public static final Color ACCENT       = new Color(99, 179, 237);     // cyan-blue
    public static final Color ACCENT_DARK  = new Color(66, 133, 188);
    public static final Color SUCCESS      = new Color(72, 199, 142);
    public static final Color WARNING      = new Color(255, 193, 69);
    public static final Color DANGER       = new Color(252, 100, 100);
    public static final Color TEXT_PRIMARY = new Color(226, 232, 240);
    public static final Color TEXT_MUTED   = new Color(113, 128, 150);
    public static final Color BORDER_COLOR = new Color(45, 55, 80);

    // Status badge colors
    public static final Color STATUS_PENDING   = new Color(255, 193, 69);
    public static final Color STATUS_PAID      = new Color(72, 199, 142);
    public static final Color STATUS_CANCELLED = new Color(252, 100, 100);

    // ── Typography ────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD, 11);

    // ── Spacing ───────────────────────────────────────────────────────────────
    public static final int PAD_SM  = 6;
    public static final int PAD_MD  = 12;
    public static final int PAD_LG  = 20;
    public static final int PAD_XL  = 30;
    public static final int RADIUS  = 10;

    // ── Borders ───────────────────────────────────────────────────────────────
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(PAD_LG, PAD_LG, PAD_LG, PAD_LG)
        );
    }

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)
        );
    }

    public static Border inputFocusBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            new EmptyBorder(6, 10, 6, 10)
        );
    }

    // ── Button factory ────────────────────────────────────────────────────────
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, ACCENT, BG_DARK);
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, SUCCESS, BG_DARK);
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, DANGER, BG_DARK);
        return btn;
    }

    public static JButton ghostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(ACCENT);
        btn.setBackground(BG_CARD);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setOpaque(true);
                btn.setBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 30));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setOpaque(false);
            }
        });
        return btn;
    }

    private static void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(FONT_HEADING);
        btn.setForeground(fg); // Use dark text for better contrast on bright buttons
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            new EmptyBorder(8, 18, 8, 18)
        ));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        Color hover = bg.brighter();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(bg); }
        });
    }

    // ── Input factory ─────────────────────────────────────────────────────────
    public static JTextField styledTextField() {
        JTextField f = new JTextField();
        applyInputStyle(f);
        return f;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        applyInputStyle(f);
        return f;
    }

    public static JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBorder(inputBorder());
        return cb;
    }

    public static JTextArea styledTextArea() {
        JTextArea ta = new JTextArea();
        ta.setFont(FONT_BODY);
        ta.setBackground(BG_INPUT);
        ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(TEXT_PRIMARY);
        ta.setBorder(new EmptyBorder(8, 10, 8, 10));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    private static void applyInputStyle(JTextField f) {
        f.setFont(FONT_BODY);
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setBorder(inputBorder());
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { f.setBorder(inputFocusBorder()); }
            public void focusLost(java.awt.event.FocusEvent e)   { f.setBorder(inputBorder()); }
        });
    }

    // ── Label factory ─────────────────────────────────────────────────────────
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    // ── Panel factory ─────────────────────────────────────────────────────────
    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(cardBorder());
        return p;
    }

    // ── Table styling ─────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(50, 70, 110));
        table.setSelectionForeground(Color.WHITE);
        table.setFocusable(false);
        table.getTableHeader().setBackground(BG_DARK);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(FONT_BADGE);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        table.getTableHeader().setReorderingAllowed(false);
    }

    public static JScrollPane styledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getViewport().setBackground(BG_CARD);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                thumbColor = BORDER_COLOR;
                trackColor = BG_DARK;
            }
            protected JButton createDecreaseButton(int o) { return makeZeroButton(); }
            protected JButton createIncreaseButton(int o) { return makeZeroButton(); }
            private JButton makeZeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });
        return sp;
    }

    // ── Separator ─────────────────────────────────────────────────────────────
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        return sep;
    }

    // ── Setup global L&F ─────────────────────────────────────────────────────
    public static void installGlobalDefaults() {
        UIManager.put("OptionPane.background",           BG_CARD);
        UIManager.put("Panel.background",                BG_DARK);
        UIManager.put("OptionPane.messageForeground",    TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont",          FONT_BODY);
        UIManager.put("Button.background",               BG_CARD);
        UIManager.put("Button.foreground",               ACCENT);
        UIManager.put("Button.font",                     FONT_HEADING);
        UIManager.put("Button.border",                   BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        UIManager.put("TextField.background",            BG_INPUT);
        UIManager.put("TextField.foreground",            TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",       TEXT_PRIMARY);
        UIManager.put("PasswordField.background",        BG_INPUT);
        UIManager.put("PasswordField.foreground",        TEXT_PRIMARY);
        UIManager.put("Label.foreground",                TEXT_PRIMARY);
        UIManager.put("Label.font",                      FONT_BODY);
        UIManager.put("ComboBox.background",             BG_INPUT);
        UIManager.put("ComboBox.foreground",             TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",    ACCENT_DARK);
        UIManager.put("ComboBox.selectionForeground",    Color.WHITE);
        UIManager.put("ScrollPane.background",           BG_CARD);
        UIManager.put("Viewport.background",             BG_CARD);
        UIManager.put("Table.background",                BG_CARD);
        UIManager.put("Table.foreground",                TEXT_PRIMARY);
        UIManager.put("TableHeader.background",          BG_DARK);
        UIManager.put("TableHeader.foreground",          TEXT_MUTED);
        UIManager.put("TabbedPane.background",           BG_DARK);
        UIManager.put("TabbedPane.foreground",           TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",             BG_CARD);
        UIManager.put("TabbedPane.contentAreaColor",     BG_CARD);
        UIManager.put("TabbedPane.tabAreaBackground",    BG_DARK);
        UIManager.put("Separator.foreground",            BORDER_COLOR);
        UIManager.put("Spinner.background",              BG_INPUT);
        UIManager.put("Spinner.foreground",              TEXT_PRIMARY);
    }
}