package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Course;

import javax.swing.*;
import java.awt.*;

public class CourseFormDialog extends JDialog {
    private JTextField codeField, nameField, instructorField, creditsField;
    private boolean confirmed = false;
    private Course course;

    public CourseFormDialog(JFrame parent, Course course) {
        super(parent, course == null ? "Add Course" : "Edit Course", true);
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(5, 2, 5, 5));

        add(new JLabel("Course Code:"));
        codeField = new JTextField();
        add(codeField);

        add(new JLabel("Course Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Instructor:"));
        instructorField = new JTextField();
        add(instructorField);

        add(new JLabel("Credits:"));
        creditsField = new JTextField();
        add(creditsField);

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        add(okButton);
        add(cancelButton);

        if (course != null) {
            codeField.setText(course.getCourseCode());
            nameField.setText(course.getCourseName());
            instructorField.setText(course.getInstructor());
            creditsField.setText(String.valueOf(course.getCredits()));
            this.course = course;
        }

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

    private boolean validateFields() {
        if (codeField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() || creditsField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Integer.parseInt(creditsField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credits must be a number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Course getCourse() {
        if (course == null) {
            course = new Course();
        }
        course.setCourseCode(codeField.getText().trim());
        course.setCourseName(nameField.getText().trim());
        course.setInstructor(instructorField.getText().trim());
        course.setCredits(Integer.parseInt(creditsField.getText().trim()));
        return course;
    }
} 