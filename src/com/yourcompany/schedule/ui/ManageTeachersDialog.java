package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.data.DataManager;
import com.yourcompany.schedule.model.Teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

public class ManageTeachersDialog extends JDialog {
    private JTable teacherTable;
    private DefaultTableModel tableModel;
    private DataManager dataManager;
    private List<Teacher> teachers;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton closeButton;
    private int selectedRow = -1;

    public ManageTeachersDialog(JFrame parent, DataManager dataManager) {
        super(parent, "Manage Teachers", true);
        this.dataManager = dataManager;
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Create table model
        String[] columns = {"ID", "Name", "Email", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        teacherTable = new JTable(tableModel);
        teacherTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(teacherTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);
        add(formPanel, BorderLayout.NORTH);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        closeButton = new JButton("Close");
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load data
        loadData();

        // Add event listeners
        teacherTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRow = teacherTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < teachers.size()) {
                    Teacher teacher = teachers.get(selectedRow);
                    nameField.setText(teacher.getName());
                    emailField.setText(teacher.getEmail());
                    phoneField.setText(teacher.getPhoneNumber());
                }
            }
        });

        addButton.addActionListener((ActionEvent e) -> {
            if (validateInputs()) {
                try {
                    Teacher teacher = new Teacher();
                    teacher.setName(nameField.getText().trim());
                    teacher.setEmail(emailField.getText().trim());
                    teacher.setPhoneNumber(phoneField.getText().trim());
                    dataManager.addTeacher(teacher);
                    clearFields();
                    loadData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error adding teacher: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        updateButton.addActionListener((ActionEvent e) -> {
            if (selectedRow >= 0 && selectedRow < teachers.size() && validateInputs()) {
                try {
                    Teacher teacher = teachers.get(selectedRow);
                    teacher.setName(nameField.getText().trim());
                    teacher.setEmail(emailField.getText().trim());
                    teacher.setPhoneNumber(phoneField.getText().trim());
                    dataManager.updateTeacher(teacher);
                    clearFields();
                    loadData();
                    selectedRow = -1;
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error updating teacher: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a teacher to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener((ActionEvent e) -> {
            if (selectedRow >= 0 && selectedRow < teachers.size()) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this teacher?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        dataManager.deleteTeacher(teachers.get(selectedRow).getTeacherId());
                        clearFields();
                        loadData();
                        selectedRow = -1;
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error deleting teacher: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a teacher to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        closeButton.addActionListener((ActionEvent e) -> {
            dispose();
        });
    }

    private void loadData() {
        try {
            teachers = dataManager.getAllTeachers();
            tableModel.setRowCount(0);
            for (Teacher teacher : teachers) {
                Object[] row = {
                    teacher.getTeacherId(),
                    teacher.getName(),
                    teacher.getEmail(),
                    teacher.getPhoneNumber()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading teachers: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Teacher name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
} 