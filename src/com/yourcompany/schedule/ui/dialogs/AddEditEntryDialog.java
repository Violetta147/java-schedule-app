package com.yourcompany.schedule.ui.dialogs;

import com.yourcompany.schedule.controller.AddEditEntryController;
import com.yourcompany.schedule.model.CourseOffering;
import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;
import com.yourcompany.schedule.model.Teacher;
import com.yourcompany.schedule.model.SchoolClass;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.TimePickerSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class AddEditEntryDialog extends JDialog {
    private DateTimePicker startDateTimePicker;
    private DateTimePicker endDateTimePicker;
    private JTextField courseField;
    private JTextField roomField;

    private final AddEditEntryController controller;

    public AddEditEntryDialog(JFrame parent, List<CourseOffering> offerings, List<Room> rooms, ScheduleEntry entry) {
        super(parent, true);
        this.controller = new AddEditEntryController(parent, offerings, rooms, entry);
        initializeUI();
    }

    private void initializeUI() {
        setTitle(controller.getDialogTitle());
        setSize(450, 250);
        setLocationRelativeTo(getParent());
        setLayout(new GridLayout(5, 2, 5, 5));

        add(new JLabel("Course:"));
        courseField = new JTextField();
        courseField.setEditable(false);
        JButton selectCourseButton = new JButton("Select...");
        JPanel coursePanel = new JPanel(new BorderLayout());
        coursePanel.add(courseField, BorderLayout.CENTER);
        coursePanel.add(selectCourseButton, BorderLayout.EAST);
        add(coursePanel);
        courseField.setText(controller.getSelectedCourseOffering() != null ? controller.getSelectedCourseOffering().toString() : "");

        add(new JLabel("Room:"));
        roomField = new JTextField();
        roomField.setEditable(false);
        JButton selectRoomButton = new JButton("Select...");
        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.add(roomField, BorderLayout.CENTER);
        roomPanel.add(selectRoomButton, BorderLayout.EAST);
        add(roomPanel);
        roomField.setText(controller.getSelectedRoom() != null ? controller.getSelectedRoom().toString() : "");

        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setAllowEmptyDates(false);
        TimePickerSettings timeSettings = new TimePickerSettings();
        timeSettings.setAllowEmptyTimes(false);

        add(new JLabel("Start Date & Time:"));
        startDateTimePicker = new DateTimePicker(dateSettings, timeSettings);
        startDateTimePicker.setDateTimePermissive(controller.getStartDateTime());
        add(startDateTimePicker);

        add(new JLabel("End Date & Time:"));
        TimePickerSettings endTimeSettings = new TimePickerSettings();
        endTimeSettings.setAllowEmptyTimes(false);
        endDateTimePicker = new DateTimePicker(dateSettings.copySettings(), endTimeSettings);
        endDateTimePicker.setDateTimePermissive(controller.getEndDateTime());
        add(endDateTimePicker);

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        add(okButton);
        add(cancelButton);

        initializeActionListeners(selectCourseButton, selectRoomButton, okButton, cancelButton);
    }

    private void initializeActionListeners(JButton selectCourseButton, JButton selectRoomButton, JButton okButton, JButton cancelButton) {
        selectCourseButton.addActionListener(e -> {
            controller.handleCourseSelection();
            courseField.setText(controller.getSelectedCourseOffering() != null ? controller.getSelectedCourseOffering().toString() : "");
        });

        selectRoomButton.addActionListener(e -> {
            controller.handleRoomSelection();
            roomField.setText(controller.getSelectedRoom() != null ? controller.getSelectedRoom().toString() : "");
        });

        okButton.addActionListener((ActionEvent e) -> {
            controller.setStartDateTime(startDateTimePicker.getDateTimePermissive());
            controller.setEndDateTime(endDateTimePicker.getDateTimePermissive());
            if (controller.validateInputs(this)) {
                controller.setConfirmed(true);
                setVisible(false);
            }
        });

        cancelButton.addActionListener((ActionEvent e) -> {
            controller.setConfirmed(false);
            setVisible(false);
        });
    }

    public boolean isConfirmed() {
        return controller.isConfirmed();
    }

    public ScheduleEntry getScheduleEntry() {
        return controller.getScheduleEntry();
    }
}