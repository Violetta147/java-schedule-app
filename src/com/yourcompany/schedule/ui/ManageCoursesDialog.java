package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.data.DataManager;
import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Teacher;
import com.yourcompany.schedule.model.SchoolClass;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ManageCoursesDialog extends JDialog {
    private DataManager dataManager;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Course> courses;

    public ManageCoursesDialog(JFrame parent, DataManager dataManager) {
        super(parent, "Manage Courses", true);
        this.dataManager = dataManager;
        setSize(800, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"ID", "Code", "Name", "Teacher", "Class", "Credits"}, 0) {
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
            courses = dataManager.getAllCourses();
            for (Course c : courses) {
                String teacherName = c.getTeacher() != null ? c.getTeacher().getName() : "Not Assigned";
                String className = c.getSchoolClass() != null ? c.getSchoolClass().getName() : "Not Assigned";
                tableModel.addRow(new Object[]{
                    c.getCourseId(), 
                    c.getCourseCode(), 
                    c.getCourseName(), 
                    teacherName, 
                    className, 
                    c.getCredits()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCourse() {
        try {
            List<Teacher> teachers = dataManager.getAllTeachers();
            List<SchoolClass> classes = dataManager.getAllSchoolClasses();
            
            CourseFormDialog dialog = new CourseFormDialog((JFrame) getParent(), null, teachers, classes);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                dataManager.addCourse(dialog.getCourse());
                loadCourses();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a course to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            List<Teacher> teachers = dataManager.getAllTeachers();
            List<SchoolClass> classes = dataManager.getAllSchoolClasses();
            Course course = courses.get(row);
            
            CourseFormDialog dialog = new CourseFormDialog((JFrame) getParent(), course, teachers, classes);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                dataManager.updateCourse(dialog.getCourse());
                loadCourses();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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