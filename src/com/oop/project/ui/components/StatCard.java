package com.oop.project.ui.components;

import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * A KPI card for the Dashboard tab.
 * Shows an icon label, a large value, and a sub-label.
 */
public class StatCard extends JPanel {

    private final JLabel valueLbl;
    private final JLabel titleLbl;
    private final Color accentColor;

    public StatCard(String title, String value, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(0, 6));
        setBackground(UITheme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor.darker().darker(), 1),
            BorderFactory.createEmptyBorder(20, 22, 20, 22)
        ));

        valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLbl.setForeground(accentColor);

        titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(UITheme.FONT_BADGE);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        add(titleLbl, BorderLayout.NORTH);
        add(valueLbl, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLbl.setText(value);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // subtle left accent stripe
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(accentColor);
        g2.fillRect(0, 0, 4, getHeight());
    }
}