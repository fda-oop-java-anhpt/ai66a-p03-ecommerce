package com.oop.project.ui.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class UITheme {
    public static final Color BG_DARK = new Color(15, 18, 28);
    public static final Color BG_CARD = new Color(24, 29, 45);
    public static final Color BG_INPUT = new Color(32, 38, 58);
    public static final Color BG_ROW_ALT = new Color(28, 34, 52);
    public static final Color ACCENT = new Color(99, 179, 237);
    public static final Color ACCENT_DARK = new Color(66, 133, 188);
    public static final Color SUCCESS = new Color(72, 199, 142);
    public static final Color WARNING = new Color(255, 193, 69);
    public static final Color DANGER = new Color(252, 100, 100);
    public static final Color UPDATE = new Color(200, 150, 255);
    public static final Color TEXT_PRIMARY = new Color(200, 200, 200);
    public static final Color TEXT_MUTED = new Color(113, 128, 150);
    public static final Color BORDER_COLOR = new Color(45, 55, 80);
    public static final Color STATUS_PENDING = new Color(255, 193, 69);
    public static final Color STATUS_PAID = new Color(72, 199, 142);
    public static final Color STATUS_CANCELLED = new Color(252, 100, 100);
    public static final Color TEXT_DARK = new Color(15, 18, 28);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    public static javax.swing.border.Border inputBorder() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 10, 6, 10));
    }

    public static javax.swing.border.Border inputFocusBorder() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT, 1),
                new EmptyBorder(6, 10, 6, 10));
    }

    public static JButton primaryButton(String t) {
        return btn(t, ACCENT, Color.WHITE);
    }

    public static JButton successButton(String t) {
        return btn(t, SUCCESS, Color.WHITE);
    }

    public static JButton dangerButton(String t) {
        return btn(t, DANGER, Color.WHITE);
    }

    public static JButton ghostButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (isContentAreaFilled()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(ACCENT);
        btn.setBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 30));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setContentAreaFilled(true);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });
        return btn;
    }

    private static JButton btn(String t, Color bg, Color fg) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_BODY);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false); // tắt để dùng paintComponent thay thế
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JTextField styledTextField() {
        JTextField f = new JTextField();
        applyInput(f);
        return f;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        applyInput(f);
        return f;
    }

    public static JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_MUTED);
        // make dropdown list items dark-themed so text is readable
        cb.setRenderer(new javax.swing.DefaultListCellRenderer() {
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
                l.setFont(FONT_BODY);
                l.setBackground(sel ? ACCENT_DARK : BG_INPUT);
                l.setForeground(TEXT_PRIMARY);
                l.setBorder(new javax.swing.border.EmptyBorder(5, 10, 5, 10));
                return l;
            }
        });
        cb.setMaximumRowCount(10);
        return cb;
    }

    public static JSpinner styledSpinner(int min, int max, int val) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(val, min, max, 1));
        sp.setFont(FONT_BODY);
        JTextField e = ((JSpinner.DefaultEditor) sp.getEditor()).getTextField();
        e.setBackground(BG_INPUT);
        e.setForeground(TEXT_PRIMARY);
        return sp;
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

    private static void applyInput(JTextField f) {
        f.setFont(FONT_BODY);
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setBorder(inputBorder());
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(inputFocusBorder());
            }

            public void focusLost(FocusEvent e) {
                f.setBorder(inputBorder());
            }
        });
    }

    public static JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static JLabel heading(String t) {
        JLabel l = new JLabel(t);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel title(String t) {
        JLabel l = new JLabel(t);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JScrollPane scrollPane(Component v) {
        JScrollPane sp = new JScrollPane(v);
        sp.setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getViewport().setBackground(BG_CARD);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                thumbColor = BORDER_COLOR;
                trackColor = BG_DARK;
            }

            protected JButton createDecreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            protected JButton createIncreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        return sp;
    }

    public static void styleTable(JTable t) {
        t.setBackground(BG_CARD);
        t.setForeground(TEXT_PRIMARY);
        t.setFont(FONT_BODY);
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(50, 70, 110));
        t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setBackground(BG_DARK);
        t.getTableHeader().setForeground(TEXT_MUTED);
        t.getTableHeader().setFont(FONT_BADGE);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        t.getTableHeader().setReorderingAllowed(false);
    }

    public static void showError(Component p, String m) {
        JOptionPane.showMessageDialog(p, m, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component p, String m) {
        JOptionPane.showMessageDialog(p, m, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component p, String m, String t) {
        return JOptionPane.showConfirmDialog(p, m, t, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void showScrollable(Component p, String text, String title) {
        JTextArea ta = styledTextArea();
        ta.setFont(FONT_MONO);
        ta.setText(text);
        ta.setEditable(false);
        ta.setCaretPosition(0);
        JScrollPane sp = scrollPane(ta);
        sp.setPreferredSize(new Dimension(560, 480));
        JOptionPane.showMessageDialog(p, sp, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void installGlobalDefaults() {
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Button.background", BG_INPUT);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("PasswordField.background", BG_INPUT);
        UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", ACCENT_DARK);
        UIManager.put("TabbedPane.background", BG_DARK);
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
    }

    public static JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER_COLOR);
        return s;
    }

    public static JPanel labeledField(String lbl, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(BG_CARD);
        JLabel l = label(lbl);
        l.setFont(FONT_SMALL);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}