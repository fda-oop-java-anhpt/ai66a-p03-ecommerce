package com.oop.project.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class UIPaginator<T> extends JPanel {
    private List<T> allData;
    private int itemsPerPage = 20;
    private int currentPage = 1;
    private Consumer<List<T>> onPageChange;
    
    private JLabel pageLabel;
    private JButton prevBtn;
    private JButton nextBtn;

    public UIPaginator(Consumer<List<T>> onPageChange) {
        this.onPageChange = onPageChange;
        
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        setOpaque(false);
        
        prevBtn = UITheme.ghostButton("<< Prev");
        nextBtn = UITheme.ghostButton("Next >>");
        pageLabel = UITheme.label("Page 1 of 1");
        
        prevBtn.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                render();
            }
        });
        
        nextBtn.addActionListener(e -> {
            int max = getMaxPage();
            if (currentPage < max) {
                currentPage++;
                render();
            }
        });
        
        add(prevBtn);
        add(pageLabel);
        add(nextBtn);
    }
    
    public void setData(List<T> data) {
        this.allData = data;
        this.currentPage = 1;
        render();
    }
    
    private int getMaxPage() {
        if (allData == null || allData.isEmpty()) return 1;
        return (int) Math.ceil((double) allData.size() / itemsPerPage);
    }
    
    private void render() {
        if (allData == null || allData.isEmpty()) {
            pageLabel.setText("Page 1 of 1");
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            onPageChange.accept(java.util.Collections.emptyList());
            return;
        }
        
        int maxPage = getMaxPage();
        if (currentPage > maxPage) currentPage = maxPage;
        if (currentPage < 1) currentPage = 1;

        pageLabel.setText("Page " + currentPage + " of " + maxPage);
        prevBtn.setEnabled(currentPage > 1);
        nextBtn.setEnabled(currentPage < maxPage);
        
        int fromNode = (currentPage - 1) * itemsPerPage;
        int toNode = Math.min(fromNode + itemsPerPage, allData.size());
        onPageChange.accept(allData.subList(fromNode, toNode));
    }
}
