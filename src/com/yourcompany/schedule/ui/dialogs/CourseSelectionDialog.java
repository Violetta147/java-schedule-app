package com.yourcompany.schedule.ui.dialogs;

import com.yourcompany.schedule.model.CourseOffering;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CourseSelectionDialog extends JDialog {
    private JList<CourseOffering> courseList;
    private JButton okButton;
    private JButton cancelButton;
    private boolean confirmed = false;
    private CourseOffering selectedCourseOffering;

    public CourseSelectionDialog(JFrame parent, List<CourseOffering> courses) {
        super(parent, "Select Course", true);
        initComponents(courses);
        initListeners();
    }

    private void initComponents(List<CourseOffering> courses) {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        DefaultListModel<CourseOffering> listModel = new DefaultListModel<>();
        for (CourseOffering course : courses) {
            listModel.addElement(course);
        }
        courseList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(courseList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initListeners() {
        okButton.addActionListener(e -> {
            selectedCourseOffering = courseList.getSelectedValue();
            confirmed = true;
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public CourseOffering getSelectedCourseOffering() {
        return selectedCourseOffering;
    }
}