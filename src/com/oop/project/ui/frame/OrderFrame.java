package com.oop.project.ui.frame;

import javax.swing.*;
import java.awt.*;
import com.oop.project.ui.component.UIColors;
import com.oop.project.ui.component.UIFonts;
import com.oop.project.ui.component.UIButton;

public class OrderFrame extends BaseFrame {

    public OrderFrame() {
        super("Order Management");

        initUI();   // setup giao diện
        setVisible(true);
    }

    private void initUI() {
        add(createHeader(), BorderLayout.NORTH);
        add(createMain(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setBackground(UIColors.PRIMARY);

        JLabel title = new JLabel("Order Management System");
        title.setFont(UIFonts.TITLE);
        title.setForeground(Color.WHITE);

        panel.add(title);
        return panel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel();

        UIButton addBtn = new UIButton("Add");
        UIButton editBtn = new UIButton("Edit");
        UIButton deleteBtn = new UIButton("Delete");

        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);

        return panel;
    }

    private JPanel createMain() {
        JPanel panel = new JPanel();
        panel.setBackground(UIColors.BACKGROUND);

        JLabel label = new JLabel("Order list will be here");
        label.setFont(UIFonts.NORMAL);

        panel.add(label);
        return panel;
    }
}