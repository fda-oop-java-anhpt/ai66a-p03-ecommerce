package com.oop.project.ui.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;

public class TableRenderer {
    public static DefaultTableModel model(String[] cols) {
        return new DefaultTableModel (cols,0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }
    public static DefaultTableCellRenderer rows() {
        return new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent (JTable t, Object v, boolean sel, boolean foc, int r, int c){
                JLabel l = (JLabel)super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    l.setBackground(r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT);
                    l.setForeground(UITheme.TEXT_PRIMARY);
                }
                l.setBorder(new EmptyBorder(0, 12, 0, 12)); 
                return l;
            }
        };
    }
    public static DefaultTableCellRenderer status() {
        return new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l=(JLabel)super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setFont(UITheme.FONT_BADGE); 
                l.setHorizontalAlignment(CENTER);
                if (!sel) {
                    String s=v==null ? "" : v.toString();
                    switch(s){
                        case "PAID":
                            l.setForeground(UITheme.STATUS_PAID);
                            break;
                        case "CANCELLED":
                            l.setForeground(UITheme.STATUS_CANCELLED);
                            break;
                        default:
                            l.setForeground(UITheme.STATUS_PENDING);
                    }
                    l.setBackground(r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT);
                }
                l.setBorder(new EmptyBorder(0,12,0,12)); 
                return l;
            }
        };
    }
    public static DefaultTableCellRenderer currency() {
        return new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                if (v instanceof java.math.BigDecimal) v = String.format("%,.0f VNĐ", ((java.math.BigDecimal)v).doubleValue());
                else if(v instanceof Number) v = String.format("%,.0f VNĐ", ((Number)v).doubleValue());
                JLabel l = (JLabel)super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setHorizontalAlignment(RIGHT); 
                l.setForeground(sel ? Color.WHITE : UITheme.SUCCESS);
                l.setBackground(sel ? t.getSelectionBackground() : (r%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT));
                l.setBorder(new EmptyBorder(0,12,0,16)); 
                return l;
            }
        };
    }
    public static void applyAll(JTable t) {
        UITheme.styleTable(t); 
        DefaultTableCellRenderer r = rows();
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(r);
    }
    public static void widths(JTable t,int... ws){
        for(int i=0;i<ws.length&&i<t.getColumnCount();i++) t.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);
    }
}