package com.yourcompany.schedule.service;

import com.yourcompany.schedule.core.Scheduler;
import com.yourcompany.schedule.data.DataManager;
import com.yourcompany.schedule.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScheduleService {
    private final DataManager dataManager;
    private final Scheduler scheduler;

    public ScheduleService(DataManager dataManager, Scheduler scheduler) {
        this.dataManager = dataManager;
        this.scheduler = scheduler;
    }

    public ScheduleService() {
        this.dataManager = new DataManager();
        this.scheduler = new Scheduler();
        
        // GỌI INITIALIZE DATABASE Ở ĐÂY
        try {
            this.dataManager.initializeDatabase(false);
            System.out.println("Service: Database initialized and seeded (if necessary).");
        } catch (Exception e) {
            System.err.println("Service: CRITICAL - Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //<editor-fold desc="ScheduleEntry Service Methods">
    public Optional<ScheduleEntry> createAndAddScheduleEntry(
            CourseOffering courseOffering, SchoolClass schoolClass, Room room, AcaYear acaYear,
            LocalDate date, int startPeriod, int endPeriod) {
        if (courseOffering == null || courseOffering.getOfferingId() == 0 ||
            schoolClass == null || schoolClass.getClassId() == 0 ||
            room == null || room.getRoomId() == 0 ||
            acaYear == null || acaYear.getYearId() == 0 ||
            date == null) {
            System.err.println("Service: Invalid input parameters for creating ScheduleEntry. Associated objects must be valid and have IDs.");
            return Optional.empty();
        }
        ScheduleEntry newEntry;
        try {
            newEntry = new ScheduleEntry(0, courseOffering, schoolClass, room, acaYear, date, startPeriod, endPeriod);
        } catch (IllegalArgumentException e) {
            System.err.println("Service: Error creating schedule entry object: " + e.getMessage());
            return Optional.empty();
        }
        try {
            List<ScheduleEntry> relevantEntries = dataManager.getPotentialConflictEntries(
                newEntry.getDate(), newEntry.getRoom().getRoomId(),
                newEntry.getCourseOffering().getTeacher().getTeacherId(), newEntry.getSchoolClass().getClassId()
            );
            if (scheduler.canAddEntry(newEntry, relevantEntries)) {
                ScheduleEntry addedEntry = dataManager.addScheduleEntry(newEntry);
                System.out.println("Service: ScheduleEntry added successfully with ID " + addedEntry.getEntryId());
                return Optional.of(addedEntry);
            } else {
                System.err.println("Service: Could not add ScheduleEntry due to conflicts. Proposed: " + newEntry);
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Service: DB error adding schedule entry: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean updateScheduleEntry(
            int entryIdToUpdate, CourseOffering newCourseOffering, SchoolClass newSchoolClass, 
            Room newRoom, AcaYear newAcaYear, LocalDate newDate, 
            int newStartPeriod, int newEndPeriod) {
        if (newCourseOffering == null || newCourseOffering.getOfferingId() == 0 ||
            newSchoolClass == null || newSchoolClass.getClassId() == 0 ||
            newRoom == null || newRoom.getRoomId() == 0 ||
            newAcaYear == null || newAcaYear.getYearId() == 0 ||
            newDate == null) {
            System.err.println("Service: Invalid input parameters for updating ScheduleEntry.");
            return false;
        }
        try {
            Optional<ScheduleEntry> existingEntryOpt = dataManager.getScheduleEntryById(entryIdToUpdate);
            if (!existingEntryOpt.isPresent()) {
                System.err.println("Service: Entry to update not found: " + entryIdToUpdate);
                return false;
            }
            ScheduleEntry proposedState;
            try {
                proposedState = new ScheduleEntry(entryIdToUpdate, newCourseOffering, newSchoolClass, newRoom, newAcaYear, newDate, newStartPeriod, newEndPeriod);
            } catch (IllegalArgumentException e) {
                System.err.println("Service: Error creating proposed state for update (entry " + entryIdToUpdate + "): " + e.getMessage());
                return false;
            }
            List<ScheduleEntry> relevantEntries = dataManager.getPotentialConflictEntries(
                proposedState.getDate(), proposedState.getRoom().getRoomId(),
                proposedState.getCourseOffering().getTeacher().getTeacherId(), proposedState.getSchoolClass().getClassId()
            );
            if (scheduler.canUpdateEntry(proposedState, relevantEntries)) {
                boolean success = dataManager.updateScheduleEntry(proposedState);
                if (success) System.out.println("Service: ScheduleEntry updated: " + entryIdToUpdate);
                else System.err.println("Service: Failed to update ScheduleEntry in DB: " + entryIdToUpdate);
                return success;
            } else {
                System.err.println("Service: Could not update ScheduleEntry " + entryIdToUpdate + " due to conflicts. Proposed: " + proposedState);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Service: DB error updating schedule entry " + entryIdToUpdate + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deleteScheduleEntry(int entryId) {
        try {
            boolean success = dataManager.deleteScheduleEntry(entryId);
            if (success) System.out.println("Service: Entry deleted: " + entryId);
            else System.err.println("Service: Entry to delete not found or not deleted: " + entryId);
            return success;
        } catch (SQLException e) {
            System.err.println("Service: DB error deleting entry " + entryId + ": " + e.getMessage());
            return false;
        }
    }
    public Optional<ScheduleEntry> findScheduleEntryById(int entryId) {
        try {
            return dataManager.getScheduleEntryById(entryId);
        } catch (SQLException e) {
            System.err.println("Service: DB error finding entry " + entryId + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    public List<ScheduleEntry> getAllScheduleEntries() {
        try {
            return dataManager.getAllScheduleEntries();
        } catch (SQLException e) {
            System.err.println("Service: DB error getting all entries: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public List<ScheduleEntry> getScheduleEntriesForTeacher(int teacherId, LocalDate date) {
        try {
            return dataManager.getAllScheduleEntries().stream()
                    .filter(e -> e.getDate().equals(date) &&
                                 e.getCourseOffering().getTeacher().getTeacherId() == teacherId)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
             System.err.println("Service: Error fetching schedule for teacher " + teacherId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public List<ScheduleEntry> getScheduleEntriesForRoom(int roomId, LocalDate date) {
        try {
            return dataManager.getAllScheduleEntries().stream()
                    .filter(e -> e.getDate().equals(date) &&
                                 e.getRoom().getRoomId() == roomId)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
             System.err.println("Service: Error fetching schedule for room " + roomId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public List<ScheduleEntry> getScheduleEntriesForClass(int classId, LocalDate date) {
         try {
            return dataManager.getAllScheduleEntries().stream()
                    .filter(e -> e.getDate().equals(date) &&
                                 e.getSchoolClass().getClassId() == classId)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
             System.err.println("Service: Error fetching schedule for class " + classId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    //</editor-fold>

    //<editor-fold desc="AcaYear Service Methods">
    public Optional<AcaYear> addAcaYear(AcaYear acaYear) {
        try {
            return Optional.ofNullable(dataManager.addAcaYear(acaYear));
        } catch (SQLException e) {
            System.err.println("Service: Error adding AcaYear: " + e.getMessage());
            return Optional.empty();
        }
    }
    public List<AcaYear> getAllAcaYears() {
        try {
            return dataManager.getAllAcaYears();
        } catch (SQLException e) {
            System.err.println("Service: Error getting all AcaYears: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Optional<AcaYear> findAcaYearById(int yearId) {
        try {
            return dataManager.getAcaYearById(yearId);
        } catch (SQLException e) {
            System.err.println("Service: Error finding AcaYear by ID " + yearId + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    public boolean updateAcaYear(AcaYear acaYear) {
        try {
            return dataManager.updateAcaYear(acaYear);
        } catch (SQLException e) {
            System.err.println("Service: Error updating AcaYear " + acaYear.getYearId() + ": " + e.getMessage());
            return false;
        }
    }
    public boolean deleteAcaYear(int yearId) {
        try {
            return dataManager.deleteAcaYear(yearId);
        } catch (SQLException e) {
            System.err.println("Service: Error deleting AcaYear " + yearId + ": " + e.getMessage());
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Teacher Service Methods">
    public Optional<Teacher> addTeacher(Teacher teacher) {
        try {
            return Optional.ofNullable(dataManager.addTeacher(teacher));
        } catch (SQLException e) {
            System.err.println("Service: Error adding teacher: " + e.getMessage());
            return Optional.empty();
        }
    }
    public List<Teacher> getAllTeachers() {
        try {
            return dataManager.getAllTeachers();
        } catch (SQLException e) {
            System.err.println("Service: Error getting all teachers: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Optional<Teacher> findTeacherById(int teacherId) {
        try {
            return dataManager.getTeacherById(teacherId);
        } catch (SQLException e) {
            System.err.println("Service: Error finding Teacher by ID " + teacherId + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    public boolean updateTeacher(Teacher teacher) {
        try {
            return dataManager.updateTeacher(teacher);
        } catch (SQLException e) {
            System.err.println("Service: Error updating Teacher " + teacher.getTeacherId() + ": " + e.getMessage());
            return false;
        }
    }
    public boolean deleteTeacher(int teacherId) {
        try {
            return dataManager.deleteTeacher(teacherId);
        } catch (SQLException e) {
            System.err.println("Service: Error deleting Teacher " + teacherId + ": " + e.getMessage());
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="SchoolClass Service Methods">
    public Optional<SchoolClass> addSchoolClass(SchoolClass schoolClass) {
        try {
            return Optional.ofNullable(dataManager.addSchoolClass(schoolClass));
        } catch (SQLException e) {
            System.err.println("Service: Error adding SchoolClass: " + e.getMessage());
            return Optional.empty();
        }
    }
    public List<SchoolClass> getAllSchoolClasses() {
        try {
            return dataManager.getAllSchoolClasses();
        } catch (SQLException e) {
            System.err.println("Service: Error getting all SchoolClasses: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Optional<SchoolClass> findSchoolClassById(int classId) {
        try {
            return dataManager.getSchoolClassById(classId);
        } catch (SQLException e) {
            System.err.println("Service: Error finding SchoolClass by ID " + classId + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    public boolean updateSchoolClass(SchoolClass schoolClass) {
        try {
            return dataManager.updateSchoolClass(schoolClass);
        } catch (SQLException e) {
            System.err.println("Service: Error updating SchoolClass " + schoolClass.getClassId() + ": " + e.getMessage());
            return false;
        }
    }
    public boolean deleteSchoolClass(int classId) {
        try {
            return dataManager.deleteSchoolClass(classId);
        } catch (SQLException e) {
            System.err.println("Service: Error deleting SchoolClass " + classId + ": " + e.getMessage());
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Course Service Methods">
    public Optional<Course> addCourse(Course course) {
        try {
            return Optional.ofNullable(dataManager.addCourse(course));
        } catch (SQLException e) {
            System.err.println("Service: Error adding Course: " + e.getMessage());
            return Optional.empty();
        }
    }
    public List<Course> getAllCourses() {
        try {
            return dataManager.getAllCourses();
        } catch (SQLException e) {
            System.err.println("Service: Error getting all Courses: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Optional<Course> findCourseById(int courseId) {
        try {
            return dataManager.getCourseById(courseId);
        } catch (SQLException e) {
            System.err.println("Service: Error finding Course by ID " + courseId + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    public boolean updateCourse(Course course) {
        try {
            return dataManager.updateCourse(course);
        } catch (SQLException e) {
            System.err.println("Service: Error updating Course " + course.getCourseId() + ": " + e.getMessage());
            return false;
        }
    }
    public boolean deleteCourse(int courseId) {
        try {
            return dataManager.deleteCourse(courseId);
        } catch (SQLException e) {
            System.err.println("Service: Error deleting Course " + courseId + ": " + e.getMessage());
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Room Service Methods">
    public Optional<Room> addRoom(Room room) {
        try {
            return Optional.ofNullable(dataManager.addRoom(room));
        } catch (SQLException e) {
            System.err.println("Service: Error adding Room: " + e.getMessage());
            return Optional.empty();
        }
    }
    public List<Room> getAllRooms() {
        try {
            return dataManager.getAllRooms();
        } catch (SQLException e) {
            System.err.println("Service: Error getting all Rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Optional<Room> findRoomById(int roomId) {
        try {
            return dataManager.getRoomById(roomId);
        } catch (SQLException e) {
            System.err.println("Service: Error finding Room by ID " + roomId + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    public boolean updateRoom(Room room) {
        try {
            return dataManager.updateRoom(room);
        } catch (SQLException e) {
            System.err.println("Service: Error updating Room " + room.getRoomId() + ": " + e.getMessage());
            return false;
        }
    }
    public boolean deleteRoom(int roomId) {
        try {
            return dataManager.deleteRoom(roomId);
        } catch (SQLException e) {
            System.err.println("Service: Error deleting Room " + roomId + ": " + e.getMessage());
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="CourseOffering Service Methods">
    public Optional<CourseOffering> addCourseOffering(CourseOffering offering) {
        if (offering.getCourse() == null || offering.getCourse().getCourseId() == 0 ||
            offering.getTeacher() == null || offering.getTeacher().getTeacherId() == 0) {
            System.err.println("Service: Cannot add CourseOffering with invalid Course or Teacher.");
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(dataManager.addCourseOffering(offering));
        } catch (SQLException e) {
            System.err.println("Service: Error adding CourseOffering: " + e.getMessage());
            return Optional.empty();
        }
    }
    public List<CourseOffering> getAllCourseOfferings() {
        try {
            return dataManager.getAllCourseOfferings();
        } catch (SQLException e) {
            System.err.println("Service: Error getting all CourseOfferings: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Optional<CourseOffering> findCourseOfferingById(int offeringId) {
        try {
            return dataManager.getCourseOfferingById(offeringId);
        } catch (SQLException e) {
            System.err.println("Service: Error finding CourseOffering by ID " + offeringId + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    public boolean updateCourseOffering(CourseOffering offering) {
        if (offering.getCourse() == null || offering.getCourse().getCourseId() == 0 ||
            offering.getTeacher() == null || offering.getTeacher().getTeacherId() == 0) {
            System.err.println("Service: Cannot update CourseOffering with invalid Course or Teacher.");
            return false;
        }
        try {
            return dataManager.updateCourseOffering(offering);
        } catch (SQLException e) {
            System.err.println("Service: Error updating CourseOffering " + offering.getOfferingId() + ": " + e.getMessage());
            return false;
        }
    }
    public boolean deleteCourseOffering(int offeringId) {
        try {
            return dataManager.deleteCourseOffering(offeringId);
        } catch (SQLException e) {
            System.err.println("Service: Error deleting CourseOffering " + offeringId + ": " + e.getMessage());
            return false;
        }
    }
    //</editor-fold>
}