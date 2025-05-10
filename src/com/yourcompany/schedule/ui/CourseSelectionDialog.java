package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Course;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;

public class CourseSelectionDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Course> allCourses;
    private Course selectedCourse;
    private boolean confirmed = false;

    public CourseSelectionDialog(JFrame parent, List<Course> courses) {
        super(parent, "Select Course", true);
        this.allCourses = courses;
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        searchField = new JTextField();
        add(searchField, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Code", "Name", "Instructor", "Credits"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                selectedCourse = allCourses.stream().filter(c -> c.getCourseId() == id).findFirst().orElse(null);
                confirmed = true;
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterCourses();
            }
        });

        loadCourses("");
    }

    private void loadCourses(String filter) {
        tableModel.setRowCount(0);
        List<Course> filtered = allCourses;
        if (filter != null && !filter.trim().isEmpty()) {
            String f = filter.trim().toLowerCase();
            filtered = allCourses.stream().filter(c ->
                c.getCourseCode().toLowerCase().contains(f) ||
                c.getCourseName().toLowerCase().contains(f) ||
                c.getInstructor().toLowerCase().contains(f)
            ).collect(Collectors.toList());
        }
        for (Course c : filtered) {
            tableModel.addRow(new Object[]{c.getCourseId(), c.getCourseCode(), c.getCourseName(), c.getInstructor(), c.getCredits()});
        }
    }

    private void filterCourses() {
        loadCourses(searchField.getText());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Course getSelectedCourse() {
        return selectedCourse;
    }
} 