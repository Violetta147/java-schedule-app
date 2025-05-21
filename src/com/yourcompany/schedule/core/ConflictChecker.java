package com.yourcompany.schedule.core;

import com.yourcompany.schedule.model.CourseOffering;
import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;
import com.yourcompany.schedule.model.SchoolClass;
import com.yourcompany.schedule.model.Teacher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConflictChecker {

    public List<ScheduleEntry> findConflicts(ScheduleEntry entryToCheck, List<ScheduleEntry> allPotentialConflicts, Integer entryIdToExclude) {
        List<ScheduleEntry> conflicts = new ArrayList<>();

        if (entryToCheck == null || entryToCheck.getCourseOffering() == null ||
            entryToCheck.getRoom() == null || entryToCheck.getSchoolClass() == null ||
            entryToCheck.getCourseOffering().getTeacher() == null ||
            entryToCheck.getCourseOffering().getCourse() == null) {
            System.err.println("ConflictChecker: entryToCheck or its critical components (CourseOffering, Room, SchoolClass, Teacher, Course) are null. Cannot perform conflict check.");
            return conflicts; 
        }


        for (ScheduleEntry existingEntry : allPotentialConflicts) {
            if (existingEntry == null || existingEntry.getCourseOffering() == null ||
                existingEntry.getRoom() == null || existingEntry.getSchoolClass() == null ||
                existingEntry.getCourseOffering().getTeacher() == null ||
                existingEntry.getCourseOffering().getCourse() == null) {
                continue;
            }

            // Bỏ qua nếu là chính entry đang được cập nhật
            if (entryIdToExclude != null && existingEntry.getEntryId() == entryIdToExclude) {
                continue;
            }
            // Bỏ qua nếu là cùng một đối tượng tham chiếu
            if (entryToCheck == existingEntry) {
                continue;
            }

            if (entryToCheck.getEntryId() != 0 && entryToCheck.getEntryId() == existingEntry.getEntryId() &&
                (entryIdToExclude == null || !Objects.equals(entryToCheck.getEntryId(), entryIdToExclude))) {
                continue;
            }


            // Chỉ kiểm tra xung đột nếu cùng ngày (logic này đã có trong ScheduleEntry.conflictsWith nếu nó so sánh cả date)
            // Hoặc có thể thêm kiểm tra ngày ở đây để tối ưu nếu ScheduleEntry.conflictsWith chỉ so sánh period
            if (entryToCheck.getDate() == null || existingEntry.getDate() == null || !entryToCheck.getDate().equals(existingEntry.getDate())) {
                 continue;
            }


            if (entryToCheck.conflictsWith(existingEntry)) { // Kiểm tra xung đột thời gian (period trên cùng ngày)
                boolean teacherConflict = false;
                if (entryToCheck.getCourseOffering().getTeacher() != null && existingEntry.getCourseOffering().getTeacher() != null &&
                    entryToCheck.getCourseOffering().getTeacher().getTeacherId() == existingEntry.getCourseOffering().getTeacher().getTeacherId()) {
                    teacherConflict = true;
                }

                boolean roomConflict = false;
                if (entryToCheck.getRoom() != null && existingEntry.getRoom() != null &&
                    entryToCheck.getRoom().getRoomId() == existingEntry.getRoom().getRoomId()) {
                    roomConflict = true;
                }

                boolean classConflict = false;
                if (entryToCheck.getSchoolClass() != null && existingEntry.getSchoolClass() != null &&
                    entryToCheck.getSchoolClass().getClassId() == existingEntry.getSchoolClass().getClassId()) {
                    classConflict = true;
                }

                // Nếu có xung đột thời gian VÀ một trong các tài nguyên bị trùng
                if (teacherConflict || roomConflict || classConflict) {
                    conflicts.add(existingEntry);
                }
            }
        }
        return conflicts;
    }
}