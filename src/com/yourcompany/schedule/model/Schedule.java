package com.yourcompany.schedule.model;

import java.util.ArrayList;
import java.util.List;

public class Schedule {
    private List<ScheduleEntry> entries;

    public Schedule() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(ScheduleEntry entry) {
        // Potentially add validation here before adding, e.g., check for conflicts
        // if (!hasConflict(entry)) {
        //     entries.add(entry);
        // } else {
        //     System.err.println("Conflict detected. Cannot add entry: " + entry);
        // }
        entries.add(entry);
    }

    public List<ScheduleEntry> getEntries() {
        return new ArrayList<>(entries); // Return a copy to prevent external modification
    }

    public void setEntries(List<ScheduleEntry> entries) {
        this.entries = (entries != null) ? new ArrayList<>(entries) : new ArrayList<>();
    }

    // Optional: Add a method to check for conflicts within this schedule
    public boolean hasConflict(ScheduleEntry newEntry) {
        for (ScheduleEntry existingEntry : entries) {
            // Check conflict only if they are for the same resource types that cannot overlap
            // E.g., same room, or same teacher, or same class
            boolean sameRoom = existingEntry.getRoom() != null && newEntry.getRoom() != null &&
                               existingEntry.getRoom().getRoomId() == newEntry.getRoom().getRoomId();
            
            boolean sameTeacher = existingEntry.getCourseOffering() != null && newEntry.getCourseOffering() != null &&
                                  existingEntry.getCourseOffering().getTeacher() != null && newEntry.getCourseOffering().getTeacher() != null &&
                                  existingEntry.getCourseOffering().getTeacher().getTeacherId() == newEntry.getCourseOffering().getTeacher().getTeacherId();

            boolean sameClass = existingEntry.getSchoolClass() != null && newEntry.getSchoolClass() != null &&
                                existingEntry.getSchoolClass().getClassId() == newEntry.getSchoolClass().getClassId();

            if (existingEntry.conflictsWith(newEntry)) { // Time conflict
                if (sameRoom) return true; // Room conflict
                if (sameTeacher) return true; // Teacher conflict
                if (sameClass) return true; // Class conflict
            }
        }
        return false;
    }

    public void clearEntries() {
        this.entries.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Schedule:\n");
        if (entries.isEmpty()) {
            sb.append("  No entries.\n");
        } else {
            for (ScheduleEntry entry : entries) {
                sb.append("  - ").append(entry.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}