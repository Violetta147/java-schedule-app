package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Teacher;
import com.yourcompany.schedule.model.SchoolClass;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CourseFormDialog extends JDialog {
    private JTextField codeField, nameField, creditsField;
    private JComboBox<Teacher> teacherComboBox;
    private JComboBox<SchoolClass> classComboBox;
    private boolean confirmed = false;
    private Course course;
    private List<Teacher> teachers;
    private List<SchoolClass> classes;

    public CourseFormDialog(JFrame parent, Course course, List<Teacher> teachers, List<SchoolClass> classes) {
        super(parent, course == null ? "Add Course" : "Edit Course", true);
        this.course = course;
        this.teachers = teachers;
        this.classes = classes;
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(6, 2, 5, 5));

        add(new JLabel("Course Code:"));
        codeField = new JTextField();
        add(codeField);

        add(new JLabel("Course Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Teacher:"));
        teacherComboBox = new JComboBox<>();
        teacherComboBox.addItem(null); // Add empty option
        for (Teacher teacher : teachers) {
            teacherComboBox.addItem(teacher);
        }
        add(teacherComboBox);

        add(new JLabel("Class:"));
        classComboBox = new JComboBox<>();
        classComboBox.addItem(null); // Add empty option
        for (SchoolClass schoolClass : classes) {
            classComboBox.addItem(schoolClass);
        }
        add(classComboBox);

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
            creditsField.setText(String.valueOf(course.getCredits()));
            
            // Set selected teacher if exists
            if (course.getTeacher() != null) {
                for (int i = 0; i < teacherComboBox.getItemCount(); i++) {
                    Teacher item = teacherComboBox.getItemAt(i);
                    if (item != null && item.getTeacherId() == course.getTeacher().getTeacherId()) {
                        teacherComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            // Set selected class if exists
            if (course.getSchoolClass() != null) {
                for (int i = 0; i < classComboBox.getItemCount(); i++) {
                    SchoolClass item = classComboBox.getItemAt(i);
                    if (item != null && item.getClassId() == course.getSchoolClass().getClassId()) {
                        classComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
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
        course.setTeacher((Teacher) teacherComboBox.getSelectedItem());
        course.setSchoolClass((SchoolClass) classComboBox.getSelectedItem());
        course.setCredits(Integer.parseInt(creditsField.getText().trim()));
        return course;
    }
} 