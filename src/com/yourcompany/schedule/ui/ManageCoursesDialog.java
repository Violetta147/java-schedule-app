package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.data.DataManager;
import com.yourcompany.schedule.model.Course;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ManageCoursesDialog extends JDialog {
    private DataManager dataManager;
    private JTable table;
    private DefaultTableModel tableModel;

    public ManageCoursesDialog(JFrame parent, DataManager dataManager) {
        super(parent, "Manage Courses", true);
        this.dataManager = dataManager;
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"ID", "Code", "Name", "Instructor", "Credits"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addCourse());
        editButton.addActionListener(e -> editCourse());
        deleteButton.addActionListener(e -> deleteCourse());

        loadCourses();
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        try {
            List<Course> courses = dataManager.getAllCourses();
            for (Course c : courses) {
                tableModel.addRow(new Object[]{c.getCourseId(), c.getCourseCode(), c.getCourseName(), c.getInstructor(), c.getCredits()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCourse() {
        CourseFormDialog dialog = new CourseFormDialog((JFrame) getParent(), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                dataManager.addCourse(dialog.getCourse());
                loadCourses();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a course to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Course course = new Course(
            (int) tableModel.getValueAt(row, 0),
            (String) tableModel.getValueAt(row, 1),
            (String) tableModel.getValueAt(row, 2),
            (String) tableModel.getValueAt(row, 3),
            (int) tableModel.getValueAt(row, 4)
        );
        CourseFormDialog dialog = new CourseFormDialog((JFrame) getParent(), course);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                dataManager.updateCourse(dialog.getCourse());
                loadCourses();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a course to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this course?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int courseId = (int) tableModel.getValueAt(row, 0);
                dataManager.deleteCourse(courseId);
                loadCourses();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 