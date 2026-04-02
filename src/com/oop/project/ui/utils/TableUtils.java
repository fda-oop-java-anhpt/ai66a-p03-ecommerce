package com.oop.project.ui.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for JTable operations.
 */
public class TableUtils {

    public static DefaultTableModel nonEditableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void applyDefaultRenderers(JTable table) {
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(UITheme.BG_DARK);
        table.getTableHeader().setForeground(UITheme.TEXT_MUTED);
        table.getTableHeader().setFont(UITheme.FONT_SMALL);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));
        table.setBackground(UITheme.BG_CARD);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setSelectionBackground(new Color(40, 45, 60));
        table.setSelectionForeground(UITheme.ACCENT);
        table.setBorder(null);
    }

    public static void setColumnWidths(JTable table, int... widths) {
        TableColumnModel model = table.getColumnModel();
        for (int i = 0; i < widths.length && i < model.getColumnCount(); i++) {
            model.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public static TableCellRenderer currencyRenderer() {
        return new DefaultTableCellRenderer() {
            private final NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                if (value instanceof Number) {
                    value = fmt.format(value);
                }
                setHorizontalAlignment(JLabel.RIGHT);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
    }

    public static TableCellRenderer statusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(JLabel.CENTER);
                String status = String.valueOf(value);
                
                if ("PAID".equals(status) || "COMPLETED".equals(status)) {
                    l.setForeground(UITheme.SUCCESS);
                } else if ("PENDING".equals(status)) {
                    l.setForeground(UITheme.WARNING);
                } else if ("CANCELLED".equals(status)) {
                    l.setForeground(UITheme.DANGER);
                } else {
                    l.setForeground(UITheme.TEXT_PRIMARY);
                }
                return l;
            }
        };
    }
}
