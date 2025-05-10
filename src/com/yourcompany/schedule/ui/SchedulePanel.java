package com.yourcompany.schedule.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SchedulePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public SchedulePanel() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new Object[]{"Course", "Room", "Day", "Start", "End"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        // TODO: Load data from backend
    }

    public void addScheduleRow(Object[] row) {
        tableModel.addRow(row);
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }
} 