package com.yourcompany.schedule.core;

import com.yourcompany.schedule.model.ScheduleEntry;
import com.yourcompany.schedule.model.Room;
import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleEntryManager {
    public boolean validateEntry(ScheduleEntry entry, JDialog parentDialog) {
        if (entry.getOffering() == null) {
            JOptionPane.showMessageDialog(parentDialog, "Please select a course", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (entry.getRoom() == null) {
            JOptionPane.showMessageDialog(parentDialog, "Please select a room", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (entry.getStartDateTime() == null) {
            JOptionPane.showMessageDialog(parentDialog, "Please select a start date and time", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (entry.getEndDateTime() == null) {
            JOptionPane.showMessageDialog(parentDialog, "Please select an end date and time", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (entry.getEndDateTime().isBefore(entry.getStartDateTime()) || entry.getEndDateTime().equals(entry.getStartDateTime())) {
            JOptionPane.showMessageDialog(parentDialog, "End time must be after start time", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean hasConflicts(ScheduleEntry entry, List<ScheduleEntry> allEntries) {
        for (ScheduleEntry existingEntry : allEntries) {
            if (existingEntry.getRoom().equals(entry.getRoom()) &&
                existingEntry.getStartDateTime().isBefore(entry.getEndDateTime()) &&
                existingEntry.getEndDateTime().isAfter(entry.getStartDateTime())) {
                return true;
            }
        }
        return false;
    }

    public void saveEntry(ScheduleEntry entry, List<ScheduleEntry> allEntries) {
        if (entry.getEntryId() == 0) {
            // Add new entry
            allEntries.add(entry);
        } else {
            // Update existing entry
            for (int i = 0; i < allEntries.size(); i++) {
                if (allEntries.get(i).getEntryId() == entry.getEntryId()) {
                    allEntries.set(i, entry);
                    break;
                }
            }
        }
    }
}
