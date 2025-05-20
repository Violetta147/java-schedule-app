package com.yourcompany.schedule.ui.dialogs;

import com.yourcompany.schedule.data.DataManager;
import com.yourcompany.schedule.model.SchoolClass;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

public class ManageClassesDialog extends JDialog {
    private JTable classTable;
    private DefaultTableModel tableModel;
    private DataManager dataManager;
    private List<SchoolClass> classes;
    private JTextField nameField;
    private JSpinner gradeSpinner;
    private JTextField sectionField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton closeButton;
    private int selectedRow = -1;

    public ManageClassesDialog(JFrame parent, DataManager dataManager) {
        super(parent, "Manage Classes", true);
        this.dataManager = dataManager;
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Create table model
        String[] columns = {"ID", "Name", "Grade", "Section"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        classTable = new JTable(tableModel);
        classTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(classTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        formPanel.add(new JLabel("Grade:"));
        gradeSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 12, 1));
        formPanel.add(gradeSpinner);
        formPanel.add(new JLabel("Section:"));
        sectionField = new JTextField();
        formPanel.add(sectionField);
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
        classTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRow = classTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < classes.size()) {
                    SchoolClass schoolClass = classes.get(selectedRow);
                    nameField.setText(schoolClass.getName());
                    gradeSpinner.setValue(schoolClass.getGrade());
                    sectionField.setText(schoolClass.getSection());
                }
            }
        });

        addButton.addActionListener((ActionEvent e) -> {
            if (validateInputs()) {
                try {
                    SchoolClass schoolClass = new SchoolClass();
                    schoolClass.setName(nameField.getText().trim());
                    schoolClass.setGrade((Integer) gradeSpinner.getValue());
                    schoolClass.setSection(sectionField.getText().trim());
                    dataManager.addSchoolClass(schoolClass);
                    clearFields();
                    loadData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error adding class: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        updateButton.addActionListener((ActionEvent e) -> {
            if (selectedRow >= 0 && selectedRow < classes.size() && validateInputs()) {
                try {
                    SchoolClass schoolClass = classes.get(selectedRow);
                    schoolClass.setName(nameField.getText().trim());
                    schoolClass.setGrade((Integer) gradeSpinner.getValue());
                    schoolClass.setSection(sectionField.getText().trim());
                    dataManager.updateSchoolClass(schoolClass);
                    clearFields();
                    loadData();
                    selectedRow = -1;
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error updating class: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a class to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener((ActionEvent e) -> {
            if (selectedRow >= 0 && selectedRow < classes.size()) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this class?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        dataManager.deleteSchoolClass(classes.get(selectedRow).getClassId());
                        clearFields();
                        loadData();
                        selectedRow = -1;
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error deleting class: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a class to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        closeButton.addActionListener((ActionEvent e) -> {
            dispose();
        });
    }

    private void loadData() {
        try {
            classes = dataManager.getAllSchoolClasses();
            tableModel.setRowCount(0);
            for (SchoolClass schoolClass : classes) {
                Object[] row = {
                    schoolClass.getClassId(),
                    schoolClass.getName(),
                    schoolClass.getGrade(),
                    schoolClass.getSection()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading classes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        nameField.setText("");
        gradeSpinner.setValue(10);
        sectionField.setText("");
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Class name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (sectionField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Section is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
} 