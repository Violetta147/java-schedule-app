package com.yourcompany.schedule.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleEntry {
    private int entryId;
    private Course course;
    private Room room;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public ScheduleEntry() {}

    public ScheduleEntry(int entryId, Course course, Room room, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.entryId = entryId;
        this.course = course;
        this.room = room;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean conflictsWith(ScheduleEntry other) {
        return dayOfWeek.equals(other.dayOfWeek) &&
               !startTime.isAfter(other.endTime) &&
               !endTime.isBefore(other.startTime);
    }

    public int getEntryId() { return entryId; }
    public void setEntryId(int entryId) { this.entryId = entryId; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
} 