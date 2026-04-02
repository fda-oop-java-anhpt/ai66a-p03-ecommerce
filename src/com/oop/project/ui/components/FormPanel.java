package com.oop.project.ui.components;

import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple form builder that stacks label + field pairs vertically.
 * Usage:
 *   FormPanel form = new FormPanel();
 *   JTextField nameField = form.addTextField("Name");
 *   JTextField phoneField = form.addTextField("Phone");
 *   panel.add(form);
 */
public class FormPanel extends JPanel {

    private final Map<String, JComponent> fields = new LinkedHashMap<>();
    private static final int LABEL_W = 120;

    public FormPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UITheme.BG_CARD);
    }

    // ── Field adders ──────────────────────────────────────────────────────────

    public JTextField addTextField(String label) {
        JTextField f = UITheme.styledTextField();
        addRow(label, f);
        fields.put(label, f);
        return f;
    }

    public JPasswordField addPasswordField(String label) {
        JPasswordField f = UITheme.styledPasswordField();
        addRow(label, f);
        fields.put(label, f);
        return f;
    }

    public JComboBox<String> addComboBox(String label, String[] items) {
        JComboBox<String> cb = UITheme.styledComboBox(items);
        addRow(label, cb);
        fields.put(label, cb);
        return cb;
    }

    public JSpinner addSpinner(String label, int min, int max, int initial) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(initial, min, max, 1));
        sp.setFont(UITheme.FONT_BODY);
        sp.setBackground(UITheme.BG_INPUT);
        sp.setForeground(UITheme.TEXT_PRIMARY);
        sp.setBorder(UITheme.inputBorder());
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField()
                .setBackground(UITheme.BG_INPUT);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField()
                .setForeground(UITheme.TEXT_PRIMARY);
        addRow(label, sp);
        fields.put(label, sp);
        return sp;
    }

    public JTextArea addTextArea(String label, int rows) {
        JTextArea ta = UITheme.styledTextArea();
        ta.setRows(rows);
        JScrollPane sp = UITheme.styledScrollPane(ta);
        addRow(label, sp);
        fields.put(label, ta);
        return ta;
    }

    // ── Separator ─────────────────────────────────────────────────────────────

    public void addSeparator() {
        add(Box.createRigidArea(new Dimension(0, 8)));
        JSeparator sep = UITheme.separator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        add(sep);
        add(Box.createRigidArea(new Dimension(0, 8)));
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void addRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(UITheme.BG_CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel lbl = UITheme.label(labelText);
        lbl.setPreferredSize(new Dimension(LABEL_W, 30));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        add(row);
    }

    public void clearAll() {
        fields.forEach((key, comp) -> {
            if (comp instanceof JTextField) ((JTextField) comp).setText("");
            else if (comp instanceof JComboBox) ((JComboBox<?>) comp).setSelectedIndex(0);
            else if (comp instanceof JSpinner)  ((JSpinner) comp).setValue(1);
            else if (comp instanceof JTextArea)  ((JTextArea) comp).setText("");
        });
    }
}