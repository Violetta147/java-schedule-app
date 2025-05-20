package com.yourcompany.schedule.controller;


import com.yourcompany.schedule.core.ScheduleEntryManager;
import com.yourcompany.schedule.model.CourseOffering;
import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;
import com.yourcompany.schedule.ui.dialogs.CourseSelectionDialog;
import com.yourcompany.schedule.ui.dialogs.RoomSelectionDialog;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;

public class AddEditEntryController {
    private final ScheduleEntryManager entryManager = new ScheduleEntryManager();
    private CourseOffering selectedCourseOffering;
    private Room selectedRoom;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean confirmed = false;

    private final ScheduleEntry originalEntry;
    private final JFrame parentFrame;
    private final List<CourseOffering> availableCourseOfferings;
    private final List<Room> availableRooms;

    public AddEditEntryController(JFrame parent, List<CourseOffering> courseOfferings, List<Room> rooms, ScheduleEntry entry) {
        this.parentFrame = parent;
        this.availableCourseOfferings = courseOfferings;
        this.availableRooms = rooms;
        this.originalEntry = entry;

        if (entry != null) {
            this.selectedCourseOffering = entry.getOffering();
            this.selectedRoom = entry.getRoom();
            this.startDateTime = entry.getStartDateTime();
            this.endDateTime = entry.getEndDateTime();
        } else {
            this.startDateTime = LocalDateTime.now();
            this.endDateTime = LocalDateTime.now().plusHours(1);
        }
    }

    public String getDialogTitle() {
        return originalEntry == null ? "Add Schedule Entry" : "Edit Schedule Entry";
    }

    public CourseOffering getSelectedCourseOffering() {
        return selectedCourseOffering;
    }

    public Room getSelectedRoom() {
        return selectedRoom;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void handleCourseSelection() {
        CourseSelectionDialog dialog = new CourseSelectionDialog(parentFrame, availableCourseOfferings);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            this.selectedCourseOffering = dialog.getSelectedCourseOffering();
        }
    }

    public void handleRoomSelection() {
        RoomSelectionDialog dialog = new RoomSelectionDialog(parentFrame, availableRooms);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            this.selectedRoom = dialog.getSelectedRoom();
        }
    }

    public boolean validateInputs(JDialog parentDialog) {
        ScheduleEntry tempEntry = new ScheduleEntry(
            originalEntry != null ? originalEntry.getEntryId() : 0,
            selectedCourseOffering, selectedRoom, startDateTime, endDateTime
        );
        return entryManager.validateEntry(tempEntry, parentDialog);
    }

    public void saveEntry(List<ScheduleEntry> allEntries) {
        ScheduleEntry tempEntry = new ScheduleEntry(
            originalEntry != null ? originalEntry.getEntryId() : 0,
            selectedCourseOffering, selectedRoom, startDateTime, endDateTime
        );
        entryManager.saveEntry(tempEntry, allEntries);
    }

    public ScheduleEntry getScheduleEntry() {
        if (!confirmed) {
            return null;
        }
        try {
            int id = (originalEntry != null) ? originalEntry.getEntryId() : 0;
            return new ScheduleEntry(id, selectedCourseOffering, selectedRoom, startDateTime, endDateTime);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame, "Invalid input for creating entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
