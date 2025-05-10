package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;
import com.github.lgooddatepicker.components.DateTimePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.swing.SpinnerDateModel;

public class AddEditEntryDialog extends JDialog {
    private JComboBox<Course> courseCombo;
    private JComboBox<Room> roomCombo;
    private JComboBox<DayOfWeek> dayCombo;
    private DateTimePicker startDateTimePicker;
    private DateTimePicker endDateTimePicker;
    private boolean confirmed = false;
    private Course selectedCourse;
    private Room selectedRoom;
    private JTextField courseField;
    private JTextField roomField;

    public AddEditEntryDialog(JFrame parent, List<Course> courses, List<Room> rooms, ScheduleEntry entry) {
        super(parent, true);
        setTitle(entry == null ? "Add Schedule Entry" : "Edit Schedule Entry");
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(6, 2, 5, 5));

        add(new JLabel("Course:"));
        courseField = new JTextField();
        courseField.setEditable(false);
        JButton selectCourseButton = new JButton("Select...");
        JPanel coursePanel = new JPanel(new BorderLayout());
        coursePanel.add(courseField, BorderLayout.CENTER);
        coursePanel.add(selectCourseButton, BorderLayout.EAST);
        add(coursePanel);

        add(new JLabel("Room:"));
        roomField = new JTextField();
        roomField.setEditable(false);
        JButton selectRoomButton = new JButton("Select...");
        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.add(roomField, BorderLayout.CENTER);
        roomPanel.add(selectRoomButton, BorderLayout.EAST);
        add(roomPanel);

        add(new JLabel("Day of Week:"));
        dayCombo = new JComboBox<>(DayOfWeek.values());
        add(dayCombo);

        add(new JLabel("Start Date & Time:"));
        startDateTimePicker = new DateTimePicker();
        add(startDateTimePicker);

        add(new JLabel("End Date & Time:"));
        endDateTimePicker = new DateTimePicker();
        add(endDateTimePicker);

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        add(okButton);
        add(cancelButton);

        if (entry != null) {
            selectedCourse = entry.getCourse();
            selectedRoom = entry.getRoom();
            courseField.setText(selectedCourse != null ? selectedCourse.toString() : "");
            roomField.setText(selectedRoom != null ? selectedRoom.toString() : "");
            // Set pickers using LocalDateTime
            try {
                startDateTimePicker.setDateTimePermissive(entry.getStartDateTime());
                endDateTimePicker.setDateTimePermissive(entry.getEndDateTime());
            } catch (Exception ex) {
                // fallback: do nothing
            }
        }

        selectCourseButton.addActionListener(e -> {
            CourseSelectionDialog dialog = new CourseSelectionDialog((JFrame) getParent(), courses);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                selectedCourse = dialog.getSelectedCourse();
                courseField.setText(selectedCourse != null ? selectedCourse.toString() : "");
            }
        });
        selectRoomButton.addActionListener(e -> {
            RoomSelectionDialog dialog = new RoomSelectionDialog((JFrame) getParent(), rooms);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                selectedRoom = dialog.getSelectedRoom();
                roomField.setText(selectedRoom != null ? selectedRoom.toString() : "");
            }
        });

        okButton.addActionListener((ActionEvent e) -> {
            confirmed = true;
            setVisible(false);
        });
        cancelButton.addActionListener((ActionEvent e) -> {
            confirmed = false;
            setVisible(false);
        });
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ScheduleEntry getEntry() {
        try {
            Course course = selectedCourse;
            Room room = selectedRoom;
            java.time.LocalDateTime start = startDateTimePicker.getDateTimePermissive();
            java.time.LocalDateTime end = endDateTimePicker.getDateTimePermissive();
            return new ScheduleEntry(0, course, room, start, end);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
} 