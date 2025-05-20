package com.yourcompany.schedule.ui.dialogs;

import com.yourcompany.schedule.model.CourseOffering;
import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Teacher;
import com.yourcompany.schedule.model.SchoolClass;
import com.yourcompany.schedule.data.DataManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.sql.SQLException;

public class ManageCoursesDialog extends JDialog {
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton;
    private DataManager dataManager;
    private List<Teacher> teachers;
    private List<SchoolClass> classes;
    private boolean dataChanged = false;

    public ManageCoursesDialog(JFrame parent, List<Teacher> teachers, List<SchoolClass> classes) {
        super(parent, "Manage Courses", true);
        this.teachers = teachers;
        this.classes = classes;
        this.dataManager = new DataManager();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        String[] columns = {"Code", "Name", "Teacher", "Class"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(courseTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        initListeners();
    }

    private void initListeners() {
        addButton.addActionListener(e -> handleAdd());
        editButton.addActionListener(e -> handleEdit());
        deleteButton.addActionListener(e -> handleDelete());
    }

    private void loadData() {
        try {
            tableModel.setRowCount(0);
            List<CourseOffering> offerings = dataManager.getAllCourseOfferings();
            for (CourseOffering offering : offerings) {
                Course course = offering.getCourse();
                Object[] row = {
                    course.getCourseCode(),
                    course.getCourseName(),
                    offering.getTeacher() != null ? offering.getTeacher().getName() : "",
                    offering.getSchoolClass() != null ? offering.getSchoolClass().getName() : ""
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAdd() {
        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Course Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Course Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            if (!code.isEmpty() && !name.isEmpty()) {
                try {
                    Course course = new Course();
                    course.setCourseCode(code);
                    course.setCourseName(name);
                    dataManager.addCourse(course);
                    dataChanged = true;
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error adding course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter both code and name!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void handleEdit() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String code = (String) tableModel.getValueAt(selectedRow, 0);
            List<CourseOffering> offerings = dataManager.getAllCourseOfferings();
            CourseOffering offering = offerings.stream()
                .filter(o -> o.getCourse().getCourseCode().equals(code))
                .findFirst()
                .orElse(null);

            if (offering != null) {
                CourseFormDialog dialog = new CourseFormDialog(this, offering.getCourse());
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    Course updatedCourse = dialog.getCourse();
                    dataManager.updateCourse(updatedCourse);
                    dataChanged = true;
                    loadData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String code = (String) tableModel.getValueAt(selectedRow, 0);
            List<CourseOffering> offerings = dataManager.getAllCourseOfferings();
            CourseOffering offering = offerings.stream()
                .filter(o -> o.getCourse().getCourseCode().equals(code))
                .findFirst()
                .orElse(null);

            if (offering != null) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete this course?", 
                    "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    dataManager.deleteCourseOffering(offering.getOfferingId());
                    dataChanged = true;
                    loadData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDataChanged() {
        return dataChanged;
    }
}