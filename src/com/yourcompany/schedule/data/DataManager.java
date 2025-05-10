package com.yourcompany.schedule.data;

import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;
import com.yourcompany.schedule.model.Schedule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class DataManager {
    private DatabaseConnector connector;

    public DataManager() {
        connector = new DatabaseConnector();
    }

    // CRUD for Course
    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT * FROM courses";
        try (Connection conn = connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getInt("course_id"));
                course.setCourseCode(rs.getString("course_code"));
                course.setCourseName(rs.getString("course_name"));
                course.setInstructor(rs.getString("instructor"));
                course.setCredits(rs.getInt("credits"));
                courses.add(course);
            }
        }
        return courses;
    }

    public void addCourse(Course course) throws SQLException {
        String query = "INSERT INTO courses (course_code, course_name, instructor, credits) VALUES (?, ?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseName());
            pstmt.setString(3, course.getInstructor());
            pstmt.setInt(4, course.getCredits());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    course.setCourseId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateCourse(Course course) throws SQLException {
        String query = "UPDATE courses SET course_code=?, course_name=?, instructor=?, credits=? WHERE course_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseName());
            pstmt.setString(3, course.getInstructor());
            pstmt.setInt(4, course.getCredits());
            pstmt.setInt(5, course.getCourseId());
            pstmt.executeUpdate();
        }
    }

    public void deleteCourse(int courseId) throws SQLException {
        String query = "DELETE FROM courses WHERE course_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
        }
    }

    // CRUD for Room
    public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";
        try (Connection conn = connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getInt("room_id"));
                room.setRoomName(rs.getString("room_name"));
                room.setCapacity(rs.getInt("capacity"));
                room.setDescription(rs.getString("description"));
                rooms.add(room);
            }
        }
        return rooms;
    }

    public void addRoom(Room room) throws SQLException {
        String query = "INSERT INTO rooms (room_name, capacity, description) VALUES (?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, room.getRoomName());
            pstmt.setInt(2, room.getCapacity());
            pstmt.setString(3, room.getDescription());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    room.setRoomId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateRoom(Room room) throws SQLException {
        String query = "UPDATE rooms SET room_name=?, capacity=?, description=? WHERE room_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, room.getRoomName());
            pstmt.setInt(2, room.getCapacity());
            pstmt.setString(3, room.getDescription());
            pstmt.setInt(4, room.getRoomId());
            pstmt.executeUpdate();
        }
    }

    public void deleteRoom(int roomId) throws SQLException {
        String query = "DELETE FROM rooms WHERE room_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, roomId);
            pstmt.executeUpdate();
        }
    }

    // CRUD for ScheduleEntry
    public List<ScheduleEntry> getAllScheduleEntries(List<Course> courses, List<Room> rooms) throws SQLException {
        List<ScheduleEntry> entries = new ArrayList<>();
        String query = "SELECT * FROM schedule_entries";
        try (Connection conn = connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                int roomId = rs.getInt("room_id");
                Course course = courses.stream().filter(c -> c.getCourseId() == courseId).findFirst().orElse(null);
                Room room = rooms.stream().filter(r -> r.getRoomId() == roomId).findFirst().orElse(null);
                ScheduleEntry entry = new ScheduleEntry();
                entry.setEntryId(rs.getInt("entry_id"));
                entry.setCourse(course);
                entry.setRoom(room);
                entry.setDayOfWeek(DayOfWeek.valueOf(rs.getString("day_of_week")));
                entry.setStartTime(rs.getTime("start_time").toLocalTime());
                entry.setEndTime(rs.getTime("end_time").toLocalTime());
                entries.add(entry);
            }
        }
        return entries;
    }

    public void addScheduleEntry(ScheduleEntry entry) throws SQLException {
        String query = "INSERT INTO schedule_entries (course_id, room_id, day_of_week, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, entry.getCourse().getCourseId());
            pstmt.setInt(2, entry.getRoom().getRoomId());
            pstmt.setString(3, entry.getDayOfWeek().name());
            pstmt.setTime(4, java.sql.Time.valueOf(entry.getStartTime()));
            pstmt.setTime(5, java.sql.Time.valueOf(entry.getEndTime()));
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entry.setEntryId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateScheduleEntry(ScheduleEntry entry) throws SQLException {
        String query = "UPDATE schedule_entries SET course_id=?, room_id=?, day_of_week=?, start_time=?, end_time=? WHERE entry_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, entry.getCourse().getCourseId());
            pstmt.setInt(2, entry.getRoom().getRoomId());
            pstmt.setString(3, entry.getDayOfWeek().name());
            pstmt.setTime(4, java.sql.Time.valueOf(entry.getStartTime()));
            pstmt.setTime(5, java.sql.Time.valueOf(entry.getEndTime()));
            pstmt.setInt(6, entry.getEntryId());
            pstmt.executeUpdate();
        }
    }

    public void deleteScheduleEntry(int entryId) throws SQLException {
        String query = "DELETE FROM schedule_entries WHERE entry_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, entryId);
            pstmt.executeUpdate();
        }
    }
} 