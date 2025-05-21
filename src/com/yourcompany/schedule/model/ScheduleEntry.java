package com.yourcompany.schedule.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ScheduleEntry {
    private int entryId;
    private CourseOffering courseOffering;
    private SchoolClass schoolClass;
    private Room room;
    private AcaYear acaYear;
    private LocalDate date;
    private int startPeriod;
    private int endPeriod;

    private static final int ACTUAL_LESSON_DURATION_MINUTES = 45;

    // Mảng PERIOD_START_TIMES (giữ nguyên như bạn đã cung cấp)
    private static final LocalTime[] PERIOD_START_TIMES = {
        null, // 0th index unused
        LocalTime.of(7, 0),   // Period 1
        LocalTime.of(7, 50),  // Period 2
        LocalTime.of(8, 55),  // Period 3
        LocalTime.of(9, 40),  // Period 4
        LocalTime.of(10, 30), // Period 5
        LocalTime.of(12, 45), // Period 6
        LocalTime.of(13, 35), // Period 7
        LocalTime.of(14, 25), // Period 8
        LocalTime.of(15, 25), // Period 9
        LocalTime.of(16, 15)  // Period 10
    };

    // Enum để định nghĩa các buổi học
    public enum Session {
        MORNING,
        AFTERNOON,
        // Có thể có EVENING nếu cần
        UNKNOWN // Dành cho các tiết không xác định hoặc lỗi
    }

    // Mảng ánh xạ tiết học sang buổi học
    // QUAN TRỌNG: Cần điều chỉnh mảng này cho chính xác với lịch của bạn
    private static final Session[] PERIOD_SESSIONS = {
        Session.UNKNOWN,   // 0th index
        Session.MORNING,   // Period 1
        Session.MORNING,   // Period 2
        Session.MORNING,   // Period 3
        Session.MORNING,   // Period 4
        Session.MORNING,   // Period 5
        Session.AFTERNOON, // Period 6 (Sau nghỉ trưa)
        Session.AFTERNOON, // Period 7
        Session.AFTERNOON, // Period 8
        Session.AFTERNOON, // Period 9
        Session.AFTERNOON  // Period 10
        // Thêm nếu có nhiều tiết hơn
    };

    public ScheduleEntry() {}

    public ScheduleEntry(int entryId, CourseOffering courseOffering, SchoolClass schoolClass, Room room, AcaYear acaYear, LocalDate date, int startPeriod, int endPeriod) {
        // Kiểm tra ràng buộc buổi học trước khi gán giá trị
        if (!arePeriodsInSameSession(startPeriod, endPeriod)) {
            throw new IllegalArgumentException("Start period and end period must be in the same session (morning/afternoon). Cannot span across lunch break.");
        }
        this.entryId = entryId;
        this.courseOffering = courseOffering;
        this.schoolClass = schoolClass;
        this.room = room;
        this.acaYear = acaYear;
        this.date = date;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
    }

    // --- Getters ---
    public int getEntryId() { return entryId; }
    public CourseOffering getCourseOffering() { return courseOffering; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public Room getRoom() { return room; }
    public AcaYear getAcaYear() { return acaYear; }
    public LocalDate getDate() { return date; }
    public int getStartPeriod() { return startPeriod; }
    public int getEndPeriod() { return endPeriod; }

    // --- Setters (cũng cần kiểm tra ràng buộc) ---
    public void setEntryId(int entryId) { this.entryId = entryId; }
    public void setCourseOffering(CourseOffering courseOffering) { this.courseOffering = courseOffering; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public void setRoom(Room room) { this.room = room; }
    public void setAcaYear(AcaYear acaYear) { this.acaYear = acaYear; }
    public void setDate(LocalDate date) { this.date = date; }

    public void setStartPeriod(int startPeriod) {
        if (!arePeriodsInSameSession(startPeriod, this.endPeriod)) {
            throw new IllegalArgumentException("Start period and end period must be in the same session. Cannot span across lunch break.");
        }
        this.startPeriod = startPeriod;
    }

    public void setEndPeriod(int endPeriod) {
        if (!arePeriodsInSameSession(this.startPeriod, endPeriod)) {
            throw new IllegalArgumentException("Start period and end period must be in the same session. Cannot span across lunch break.");
        }
        this.endPeriod = endPeriod;
    }
    
    /**
     * Sets both start and end period, ensuring they are in the same session.
     */
    public void setPeriods(int startPeriod, int endPeriod) {
        if (startPeriod > endPeriod) {
            throw new IllegalArgumentException("Start period cannot be after end period.");
        }
        if (!arePeriodsInSameSession(startPeriod, endPeriod)) {
            throw new IllegalArgumentException("Start period and end period must be in the same session. Cannot span across lunch break.");
        }
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
    }

    // --- Logic kiểm tra buổi học ---
    public static boolean arePeriodsInSameSession(int p1, int p2) {
        if (p1 <= 0 || p1 >= PERIOD_SESSIONS.length ||
            p2 <= 0 || p2 >= PERIOD_SESSIONS.length ||
            PERIOD_SESSIONS[p1] == Session.UNKNOWN ||
            PERIOD_SESSIONS[p2] == Session.UNKNOWN) {
            // Nếu một trong các tiết không hợp lệ hoặc không có buổi xác định,
            // coi như không hợp lệ để tránh lỗi.
            // Hoặc bạn có thể ném lỗi cụ thể hơn ở đây.
            // System.err.println("Invalid period or unknown session for period " + p1 + " or " + p2);
            return false; // Hoặc throw new IllegalArgumentException("Invalid period or unknown session.");
        }
        return PERIOD_SESSIONS[p1] == PERIOD_SESSIONS[p2];
    }

    public Session getSessionForPeriod(int period) {
        if (period > 0 && period < PERIOD_SESSIONS.length) {
            return PERIOD_SESSIONS[period];
        }
        return Session.UNKNOWN;
    }


    // --- Các phương thức còn lại giữ nguyên ---
    public LocalDateTime getStartDateTime() {
        if (date == null || startPeriod <= 0 || startPeriod >= PERIOD_START_TIMES.length || PERIOD_START_TIMES[startPeriod] == null) {
            return null;
        }
        return LocalDateTime.of(date, PERIOD_START_TIMES[startPeriod]);
    }

    public LocalDateTime getEndDateTime() {
        if (date == null || startPeriod <= 0 || endPeriod < startPeriod ||
            startPeriod >= PERIOD_START_TIMES.length || PERIOD_START_TIMES[startPeriod] == null ||
            endPeriod >= PERIOD_START_TIMES.length || PERIOD_START_TIMES[endPeriod] == null) {
            return null;
        }

        int nextPeriodIndex = endPeriod + 1;

        if (nextPeriodIndex < PERIOD_START_TIMES.length && PERIOD_START_TIMES[nextPeriodIndex] != null &&
            arePeriodsInSameSession(endPeriod, nextPeriodIndex)) { // Thêm kiểm tra cùng buổi cho nextPeriod
            return LocalDateTime.of(date, PERIOD_START_TIMES[nextPeriodIndex]);
        } else {
            LocalTime lastPeriodActualStartTime = PERIOD_START_TIMES[endPeriod];
            return LocalDateTime.of(date, lastPeriodActualStartTime).plusMinutes(ACTUAL_LESSON_DURATION_MINUTES);
        }
    }

    public boolean conflictsWith(ScheduleEntry other) {
        LocalDateTime thisStart = getStartDateTime();
        LocalDateTime thisEnd = getEndDateTime();
        LocalDateTime otherStart = other.getStartDateTime();
        LocalDateTime otherEnd = other.getEndDateTime();

        if (thisStart == null || thisEnd == null || otherStart == null || otherEnd == null) {
            return false;
        }
        return thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd);
    }
    
    public Course getCourse() {
        return (this.courseOffering != null) ? this.courseOffering.getCourse() : null;
    }

    @Override
    public String toString() {
        String courseName = (getCourse() != null) ? getCourse().getCourseCode() : "N/A";
        String roomName = (room != null) ? room.getRoomName() : "N/A";
        String className = (schoolClass != null) ? schoolClass.getName() : "N/A";
        String startTimeStr = (getStartDateTime() != null) ? getStartDateTime().toLocalTime().toString() : "N/A";
        String endTimeStr = (getEndDateTime() != null) ? getEndDateTime().toLocalTime().toString() : "N/A";
        Session session = getSessionForPeriod(startPeriod); // Lấy buổi của tiết bắt đầu

        return "Entry ID: " + entryId +
               ", Date: " + (date != null ? date.toString() : "N/A") +
               ", Periods: " + startPeriod + "-" + endPeriod +
               " (" + startTimeStr + " - " + endTimeStr + ") Session: " + session +
               ", Course: " + courseName +
               ", Room: " + roomName +
               ", Class: " + className;
    }
}