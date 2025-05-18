package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.TimePickerSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.util.List;

public class AddEditEntryDialog extends JDialog {
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
        setSize(450, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(5, 2, 5, 5));

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

        // Configure date-time pickers with current date
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setAllowEmptyDates(false);
        TimePickerSettings timeSettings = new TimePickerSettings();
        timeSettings.setAllowEmptyTimes(false);
        
        add(new JLabel("Start Date & Time:"));
        startDateTimePicker = new DateTimePicker(dateSettings, timeSettings);
        startDateTimePicker.setDateTimePermissive(LocalDateTime.now());
        add(startDateTimePicker);

        add(new JLabel("End Date & Time:"));
        TimePickerSettings endTimeSettings = new TimePickerSettings();
        endTimeSettings.setAllowEmptyTimes(false);
        endDateTimePicker = new DateTimePicker(dateSettings.copySettings(), endTimeSettings);
        endDateTimePicker.setDateTimePermissive(LocalDateTime.now().plusHours(1));
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
            if (validateInputs()) {
                confirmed = true;
                setVisible(false);
            }
        });
        cancelButton.addActionListener((ActionEvent e) -> {
            confirmed = false;
            setVisible(false);
        });
    }
    
    private boolean validateInputs() {
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Please select a room", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        LocalDateTime start = startDateTimePicker.getDateTimePermissive();
        LocalDateTime end = endDateTimePicker.getDateTimePermissive();
        
        if (start == null) {
            JOptionPane.showMessageDialog(this, "Please select a start date and time", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (end == null) {
            JOptionPane.showMessageDialog(this, "Please select an end date and time", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (end.isBefore(start) || end.equals(start)) {
            JOptionPane.showMessageDialog(this, "End time must be after start time", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
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