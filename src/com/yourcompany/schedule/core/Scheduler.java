package com.yourcompany.schedule.core;

import com.yourcompany.schedule.model.ScheduleEntry;
import java.util.List;

public class Scheduler {
    private ConflictChecker conflictChecker;

    public Scheduler() {
        this.conflictChecker = new ConflictChecker();
    }

    public boolean canAddEntry(ScheduleEntry entry, List<ScheduleEntry> allEntries) {
        return conflictChecker.findConflicts(entry, allEntries).isEmpty();
    }
} 