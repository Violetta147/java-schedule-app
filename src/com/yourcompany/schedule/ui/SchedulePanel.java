package com.yourcompany.schedule.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import com.yourcompany.schedule.model.ScheduleEntry;

public class SchedulePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SchedulePanel() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new Object[]{"Course", "Room", "Start Date/Time", "End Date/Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        // Set preferred column widths
        int[] widths = {150, 120, 150, 150};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // TODO: Load data from backend
    }

    public void addScheduleRow(ScheduleEntry entry) {
        tableModel.addRow(new Object[] {
            entry.getCourse() != null ? entry.getCourse().getCourseName() : "",
            entry.getRoom() != null ? entry.getRoom().getRoomName() : "",
            entry.getStartDateTime() != null ? dtf.format(entry.getStartDateTime()) : "",
            entry.getEndDateTime() != null ? dtf.format(entry.getEndDateTime()) : ""
        });
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }
} 