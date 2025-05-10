package com.yourcompany.schedule.model;

import java.util.ArrayList;
import java.util.List;

public class Schedule {
    private List<ScheduleEntry> entries;

    public Schedule() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(ScheduleEntry entry) {
        entries.add(entry);
    }

    public List<ScheduleEntry> getEntries() {
        return entries;
    }
} 