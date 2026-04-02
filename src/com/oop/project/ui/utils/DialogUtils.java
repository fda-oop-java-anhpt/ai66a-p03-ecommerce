package com.oop.project.ui.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Wrapper around JOptionPane to enforce consistent styling (FR-6.5).
 */
public class DialogUtils {

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public static String prompt(Component parent, String message, String title) {
        return JOptionPane.showInputDialog(parent, message, title,
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Shows a large scrollable text dialog — used for invoice printing.
     */
    public static void showScrollableText(Component parent, String text, String title) {
        JTextArea ta = UITheme.styledTextArea();
        ta.setFont(UITheme.FONT_MONO);
        ta.setText(text);
        ta.setEditable(false);
        ta.setCaretPosition(0);
        JScrollPane sp = UITheme.styledScrollPane(ta);
        sp.setPreferredSize(new Dimension(520, 440));
        JOptionPane.showMessageDialog(parent, sp, title, JOptionPane.PLAIN_MESSAGE);
    }
}