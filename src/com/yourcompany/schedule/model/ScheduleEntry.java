package com.yourcompany.schedule.model;

import java.time.LocalDateTime;

public class ScheduleEntry {
    private int entryId;
    private CourseOffering offering;
    private Room room;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public ScheduleEntry() {}

    public ScheduleEntry(int entryId, CourseOffering offering, Room room, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.entryId = entryId;
        this.offering = offering;
        this.room = room;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public boolean conflictsWith(ScheduleEntry other) {
        return !startDateTime.isAfter(other.endDateTime) &&
               !endDateTime.isBefore(other.startDateTime);
    }

    public int getEntryId() { return entryId; }
    public void setEntryId(int entryId) { this.entryId = entryId; }
    public CourseOffering getOffering() { return offering; }
    public void setOffering(CourseOffering offering) { this.offering = offering; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
}