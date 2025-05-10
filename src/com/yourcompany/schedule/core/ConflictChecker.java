package com.yourcompany.schedule.core;

import com.yourcompany.schedule.model.ScheduleEntry;
import java.util.ArrayList;
import java.util.List;

public class ConflictChecker {
    public List<ScheduleEntry> findConflicts(ScheduleEntry entry, List<ScheduleEntry> allEntries) {
        List<ScheduleEntry> conflicts = new ArrayList<>();
        for (ScheduleEntry other : allEntries) {
            if (entry != other && entry.conflictsWith(other)) {
                conflicts.add(other);
            }
        }
        return conflicts;
    }
} 