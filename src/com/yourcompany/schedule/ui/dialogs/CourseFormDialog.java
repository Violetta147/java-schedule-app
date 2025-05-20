package com.yourcompany.schedule.ui.dialogs;

import com.yourcompany.schedule.model.Course;

import javax.swing.*;
import java.awt.*;

public class CourseFormDialog extends JDialog {
    private JTextField codeField, nameField;
    private JButton okButton, cancelButton;
    private boolean confirmed = false;
    private Course course;

    public CourseFormDialog(Window parent, Course course) {
        super(parent, course == null ? "Add Course" : "Edit Course", ModalityType.APPLICATION_MODAL);
        this.course = course != null ? course : new Course();
        initComponents();
        if (course != null) populateFields();
        initListeners();
    }

    private boolean validateFields() {
        if (codeField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Course getCourse() {
        course.setCourseCode(codeField.getText().trim());
        course.setCourseName(nameField.getText().trim());
        return course;
    }

    private void initComponents() {
        setSize(350, 180);
        setLocationRelativeTo(getParent());
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("Course Code:"));
        codeField = new JTextField();
        add(codeField);

        add(new JLabel("Course Name:"));
        nameField = new JTextField();
        add(nameField);

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        add(okButton);
        add(cancelButton);
    }

    private void populateFields() {
        codeField.setText(course.getCourseCode());
        nameField.setText(course.getCourseName());
    }

    private void initListeners() {
        okButton.addActionListener(e -> {
            if (validateFields()) {
                confirmed = true;
                setVisible(false);
            }
        });
        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });
    }
}