package com.oop.project.ui.components;

import com.oop.project.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * A styled search bar with a search icon and clear button.
 * Usage:
 *   SearchBar sb = new SearchBar("Search by name...", keyword -> doSearch(keyword));
 */
public class SearchBar extends JPanel {

    private final JTextField input;

    public SearchBar(String placeholder, Consumer<String> onSearch) {
        setLayout(new BorderLayout(8, 0));
        setBackground(UITheme.BG_DARK);
        setOpaque(false);

        // icon label
        JLabel icon = new JLabel("⌕");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        icon.setForeground(UITheme.TEXT_MUTED);
        icon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        input = UITheme.styledTextField();
        input.setPreferredSize(new Dimension(240, 34));

        // placeholder effect
        input.setForeground(UITheme.TEXT_MUTED);
        input.setText(placeholder);
        input.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (input.getText().equals(placeholder)) {
                    input.setText("");
                    input.setForeground(UITheme.TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (input.getText().isEmpty()) {
                    input.setForeground(UITheme.TEXT_MUTED);
                    input.setText(placeholder);
                }
            }
        });

        input.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = input.getText().equals(placeholder) ? "" : input.getText();
                if (e.getKeyCode() == KeyEvent.VK_ENTER || text.isEmpty()) {
                    onSearch.accept(text.trim());
                }
            }
        });

        JButton searchBtn = UITheme.primaryButton("Search");
        searchBtn.setPreferredSize(new Dimension(90, 34));
        searchBtn.addActionListener(e -> {
            String text = input.getText().equals(placeholder) ? "" : input.getText();
            onSearch.accept(text.trim());
        });

        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setBackground(UITheme.BG_INPUT);
        wrap.setBorder(UITheme.inputBorder());
        wrap.add(icon, BorderLayout.WEST);
        wrap.add(input, BorderLayout.CENTER);

        add(wrap, BorderLayout.CENTER);
        add(searchBtn, BorderLayout.EAST);
    }

    public String getText() { return input.getText(); }
    public void clear()     { input.setText(""); }
}