package com.yourcompany.schedule.core;

import com.yourcompany.schedule.model.ScheduleEntry;
import java.util.List;

public class Scheduler {
    private final ConflictChecker conflictChecker;

    public Scheduler() {
        this.conflictChecker = new ConflictChecker();
    }

    public Scheduler(ConflictChecker conflictChecker) {
        this.conflictChecker = conflictChecker;
    }

    public boolean canAddEntry(ScheduleEntry entry, List<ScheduleEntry> allExistingEntries) {
        // Khi thêm mới, entryIdToExclude là null
        return conflictChecker.findConflicts(entry, allExistingEntries, null).isEmpty();
    }

    public boolean canUpdateEntry(ScheduleEntry entryWithNewState, List<ScheduleEntry> allExistingEntries) {
        if (entryWithNewState.getEntryId() == 0) {
            System.err.println("Scheduler: Entry to update must have a valid ID.");
            return false; 
        }
        return conflictChecker.findConflicts(entryWithNewState, allExistingEntries, entryWithNewState.getEntryId()).isEmpty();
    }
}