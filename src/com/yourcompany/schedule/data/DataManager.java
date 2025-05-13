package com.yourcompany.schedule.data;

import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;
import com.yourcompany.schedule.model.Schedule;
import com.yourcompany.schedule.model.Teacher;
import com.yourcompany.schedule.model.SchoolClass;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class DataManager {
    private DatabaseConnector connector;

    public DataManager() {
        connector = new DatabaseConnector();
    }
    
    /**
     * Initialize the database schema and seed with initial data if needed
     */
    public void initializeDatabase() {
        try (Connection conn = connector.getConnection()) {
            
            // Create tables if they don't exist
            createTablesIfNotExist(conn);
            
            // Seed initial data if tables are empty
            seedInitialData(conn);
            
            System.out.println("Database initialization completed successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTablesIfNotExist(Connection conn) throws SQLException {
        createTeachersTable(conn);
        createSchoolClassesTable(conn);
        createCoursesTable(conn);
        createRoomsTable(conn);
        createScheduleEntriesTable(conn);
    }

    private void createTeachersTable(Connection conn) throws SQLException {
        String createTableSQL = 
            "CREATE TABLE IF NOT EXISTS teachers (" +
            "teacher_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "email VARCHAR(100), " +
            "phone_number VARCHAR(20)" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
        }
    }

    private void createSchoolClassesTable(Connection conn) throws SQLException {
        String createTableSQL = 
            "CREATE TABLE IF NOT EXISTS school_classes (" +
            "class_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(20) NOT NULL, " +
            "grade INT NOT NULL, " +
            "section VARCHAR(10) NOT NULL" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
        }
    }

    private void createCoursesTable(Connection conn) throws SQLException {
        String createTableSQL = 
            "CREATE TABLE IF NOT EXISTS courses (" +
            "course_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "course_code VARCHAR(20) NOT NULL, " +
            "course_name VARCHAR(100) NOT NULL, " +
            "teacher_id INT, " +
            "class_id INT, " +
            "credits INT DEFAULT 0, " +
            "FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL, " +
            "FOREIGN KEY (class_id) REFERENCES school_classes(class_id) ON DELETE SET NULL" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
        }
    }

    private void createRoomsTable(Connection conn) throws SQLException {
        String createTableSQL = 
            "CREATE TABLE IF NOT EXISTS rooms (" +
            "room_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "room_name VARCHAR(50) NOT NULL, " +
            "capacity INT DEFAULT 0, " +
            "description VARCHAR(255)" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
        }
    }

    private void createScheduleEntriesTable(Connection conn) throws SQLException {
        String createTableSQL = 
            "CREATE TABLE IF NOT EXISTS schedule_entries (" +
            "entry_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "course_id INT NOT NULL, " +
            "room_id INT NOT NULL, " +
            "start_datetime DATETIME NOT NULL, " +
            "end_datetime DATETIME NOT NULL, " +
            "FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE, " +
            "FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
        }
    }

    private void seedInitialData(Connection conn) throws SQLException {
        seedTeachersIfEmpty(conn);
        seedSchoolClassesIfEmpty(conn);
        seedCoursesIfEmpty(conn);
        seedRoomsIfEmpty(conn);
        seedScheduleEntriesIfEmpty(conn);
    }

    private void seedTeachersIfEmpty(Connection conn) throws SQLException {
        // Check if teachers table is empty
        String countQuery = "SELECT COUNT(*) FROM teachers";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert sample teachers
                String insertSQL = "INSERT INTO teachers (name, email, phone_number) VALUES " +
                                  "('Trinh Minh Dat', 'dat123@gmail.com', '1234432156'), " +
                                  "('Le Quang Anh Khoa', 'khoa321@gmail.com', '0796643288'), " +
                                  "('Ha Minh Khoa', 'khoahalo@gmail.com', '0796643288'), " +
                                  "('Ha Duc Kien', 'kiencntt@gmail.com', '0796643288'), " +
                                  "('Bien Cao Cuong', 'biencaocuong69@gmail.com', '0796643288')";
                
                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSQL);
                    System.out.println("Sample teachers added to database.");
                }
            }
        }
    }

    private void seedSchoolClassesIfEmpty(Connection conn) throws SQLException {
        // Check if school_classes table is empty
        String countQuery = "SELECT COUNT(*) FROM school_classes";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert sample school classes
                String insertSQL = "INSERT INTO school_classes (name, grade, section) VALUES " +
                                  "('10/1', 10, '1'), " +
                                  "('10/2', 10, '2'), " +
                                  "('11/1', 11, '1'), " +
                                  "('11/2', 11, '2'), " +
                                  "('12/1', 12, '1')";
                
                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSQL);
                    System.out.println("Sample school classes added to database.");
                }
            }
        }
    }

    private void seedCoursesIfEmpty(Connection conn) throws SQLException {
        // Check if courses table is empty
        String countQuery = "SELECT COUNT(*) FROM courses";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Get teacher and class IDs
                List<Integer> teacherIds = new ArrayList<>();
                List<Integer> classIds = new ArrayList<>();
                
                try (Statement teacherStmt = conn.createStatement();
                     ResultSet teacherRs = teacherStmt.executeQuery("SELECT teacher_id FROM teachers")) {
                    while (teacherRs.next()) {
                        teacherIds.add(teacherRs.getInt("teacher_id"));
                    }
                }
                
                try (Statement classStmt = conn.createStatement();
                     ResultSet classRs = classStmt.executeQuery("SELECT class_id FROM school_classes")) {
                    while (classRs.next()) {
                        classIds.add(classRs.getInt("class_id"));
                    }
                }
                
                // Only proceed if we have teachers and classes
                if (!teacherIds.isEmpty() && !classIds.isEmpty()) {
                    // Insert sample courses with teacher and class references
                    String insertSQL = "INSERT INTO courses (course_code, course_name, teacher_id, class_id, credits) VALUES " +
                                      "(?, ?, ?, ?, ?)";
                    
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                        // Course 1: CS101 - Introduction to Computer Science
                        pstmt.setString(1, "CS101");
                        pstmt.setString(2, "Introduction to Computer Science");
                        pstmt.setInt(3, teacherIds.get(0 % teacherIds.size()));
                        pstmt.setInt(4, classIds.get(0 % classIds.size()));
                        pstmt.setInt(5, 3);
                        pstmt.executeUpdate();
                        
                        // Course 2: MATH201 - Calculus II
                        pstmt.setString(1, "MATH201");
                        pstmt.setString(2, "Calculus II");
                        pstmt.setInt(3, teacherIds.get(1 % teacherIds.size()));
                        pstmt.setInt(4, classIds.get(1 % classIds.size()));
                        pstmt.setInt(5, 4);
                        pstmt.executeUpdate();
                        
                        // Course 3: ENG105 - Academic Writing
                        pstmt.setString(1, "ENG105");
                        pstmt.setString(2, "Academic Writing");
                        pstmt.setInt(3, teacherIds.get(2 % teacherIds.size()));
                        pstmt.setInt(4, classIds.get(2 % classIds.size()));
                        pstmt.setInt(5, 3);
                        pstmt.executeUpdate();
                        
                        // Course 4: PHYS101 - Physics I
                        pstmt.setString(1, "PHYS101");
                        pstmt.setString(2, "Physics I");
                        pstmt.setInt(3, teacherIds.get(3 % teacherIds.size()));
                        pstmt.setInt(4, classIds.get(3 % classIds.size()));
                        pstmt.setInt(5, 4);
                        pstmt.executeUpdate();
                        
                        // Course 5: BIO220 - Molecular Biology
                        pstmt.setString(1, "BIO220");
                        pstmt.setString(2, "Molecular Biology");
                        pstmt.setInt(3, teacherIds.get(4 % teacherIds.size()));
                        pstmt.setInt(4, classIds.get(4 % classIds.size()));
                        pstmt.setInt(5, 3);
                        pstmt.executeUpdate();
                        
                        System.out.println("Sample courses added to database.");
                    }
                }
            }
        }
    }

    private void seedRoomsIfEmpty(Connection conn) throws SQLException {
        // Check if rooms table is empty
        String countQuery = "SELECT COUNT(*) FROM rooms";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert sample rooms
                String insertSQL = "INSERT INTO rooms (room_name, capacity, description) VALUES " +
                                  "('A101', 30, 'Computer Lab'), " +
                                  "('B202', 50, 'Lecture Hall'), " +
                                  "('C303', 25, 'Seminar Room'), " +
                                  "('D404', 100, 'Auditorium'), " +
                                  "('E505', 20, 'Study Room')";
                
                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSQL);
                    System.out.println("Sample rooms added to database.");
                }
            }
        }
    }

    private void seedScheduleEntriesIfEmpty(Connection conn) throws SQLException {
        // Check if schedule_entries table is empty
        String countQuery = "SELECT COUNT(*) FROM schedule_entries";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Get course and room IDs
                List<Integer> courseIds = new ArrayList<>();
                List<Integer> roomIds = new ArrayList<>();
                
                try (Statement courseStmt = conn.createStatement();
                     ResultSet courseRs = courseStmt.executeQuery("SELECT course_id FROM courses")) {
                    while (courseRs.next()) {
                        courseIds.add(courseRs.getInt("course_id"));
                    }
                }
                
                try (Statement roomStmt = conn.createStatement();
                     ResultSet roomRs = roomStmt.executeQuery("SELECT room_id FROM rooms")) {
                    while (roomRs.next()) {
                        roomIds.add(roomRs.getInt("room_id"));
                    }
                }
                
                // Only proceed if we have courses and rooms
                if (!courseIds.isEmpty() && !roomIds.isEmpty()) {
                    // Create some sample schedule entries
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime tomorrow = now.plusDays(1);
                    
                    // Create prepared statement for inserting entries
                    String insertSQL = "INSERT INTO schedule_entries (course_id, room_id, start_datetime, end_datetime) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                        
                        // Entry 1: Course 1 in Room 1, today
                        pstmt.setInt(1, courseIds.get(0));
                        pstmt.setInt(2, roomIds.get(0));
                        pstmt.setTimestamp(3, Timestamp.valueOf(now.withHour(9).withMinute(0).withSecond(0)));
                        pstmt.setTimestamp(4, Timestamp.valueOf(now.withHour(10).withMinute(30).withSecond(0)));
                        pstmt.executeUpdate();
                        
                        // Entry 2: Course 2 in Room 2, today
                        pstmt.setInt(1, courseIds.get(1 % courseIds.size()));
                        pstmt.setInt(2, roomIds.get(1 % roomIds.size()));
                        pstmt.setTimestamp(3, Timestamp.valueOf(now.withHour(11).withMinute(0).withSecond(0)));
                        pstmt.setTimestamp(4, Timestamp.valueOf(now.withHour(12).withMinute(30).withSecond(0)));
                        pstmt.executeUpdate();
                        
                        // Entry 3: Course 3 in Room 3, tomorrow
                        pstmt.setInt(1, courseIds.get(2 % courseIds.size()));
                        pstmt.setInt(2, roomIds.get(2 % roomIds.size()));
                        pstmt.setTimestamp(3, Timestamp.valueOf(tomorrow.withHour(9).withMinute(0).withSecond(0)));
                        pstmt.setTimestamp(4, Timestamp.valueOf(tomorrow.withHour(10).withMinute(30).withSecond(0)));
                        pstmt.executeUpdate();
                        
                        // Entry 4: Course 4 in Room 4, tomorrow
                        pstmt.setInt(1, courseIds.get(3 % courseIds.size()));
                        pstmt.setInt(2, roomIds.get(3 % roomIds.size()));
                        pstmt.setTimestamp(3, Timestamp.valueOf(tomorrow.withHour(11).withMinute(0).withSecond(0)));
                        pstmt.setTimestamp(4, Timestamp.valueOf(tomorrow.withHour(12).withMinute(30).withSecond(0)));
                        pstmt.executeUpdate();
                        
                        System.out.println("Sample schedule entries added to database.");
                    }
                }
            }
        }
    }

    // CRUD for Course
    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        List<Teacher> teachers = getAllTeachers();
        List<SchoolClass> classes = getAllSchoolClasses();
        
        String query = "SELECT * FROM courses";
        try (Connection conn = connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getInt("course_id"));
                course.setCourseCode(rs.getString("course_code"));
                course.setCourseName(rs.getString("course_name"));
                course.setCredits(rs.getInt("credits"));
                
                // Find and set the teacher
                int teacherId = rs.getInt("teacher_id");
                if (!rs.wasNull()) {
                    for (Teacher teacher : teachers) {
                        if (teacher.getTeacherId() == teacherId) {
                            course.setTeacher(teacher);
                            break;
                        }
                    }
                }
                
                // Find and set the class
                int classId = rs.getInt("class_id");
                if (!rs.wasNull()) {
                    for (SchoolClass schoolClass : classes) {
                        if (schoolClass.getClassId() == classId) {
                            course.setSchoolClass(schoolClass);
                            break;
                        }
                    }
                }
                
                courses.add(course);
            }
        }
        return courses;
    }

    public void addCourse(Course course) throws SQLException {
        String query = "INSERT INTO courses (course_code, course_name, teacher_id, class_id, credits) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseName());
            
            // Set teacher_id
            if (course.getTeacher() != null) {
                pstmt.setInt(3, course.getTeacher().getTeacherId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            // Set class_id
            if (course.getSchoolClass() != null) {
                pstmt.setInt(4, course.getSchoolClass().getClassId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(5, course.getCredits());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    course.setCourseId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateCourse(Course course) throws SQLException {
        String query = "UPDATE courses SET course_code=?, course_name=?, teacher_id=?, class_id=?, credits=? WHERE course_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseName());
            
            // Set teacher_id
            if (course.getTeacher() != null) {
                pstmt.setInt(3, course.getTeacher().getTeacherId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            // Set class_id
            if (course.getSchoolClass() != null) {
                pstmt.setInt(4, course.getSchoolClass().getClassId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(5, course.getCredits());
            pstmt.setInt(6, course.getCourseId());
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
                entry.setStartDateTime(rs.getTimestamp("start_datetime").toLocalDateTime());
                entry.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
                entries.add(entry);
            }
        }
        return entries;
    }

    public void addScheduleEntry(ScheduleEntry entry) throws SQLException {
        String query = "INSERT INTO schedule_entries (course_id, room_id, start_datetime, end_datetime) VALUES (?, ?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, entry.getCourse().getCourseId());
            pstmt.setInt(2, entry.getRoom().getRoomId());
            pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(entry.getStartDateTime()));
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(entry.getEndDateTime()));
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entry.setEntryId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateScheduleEntry(ScheduleEntry entry) throws SQLException {
        String query = "UPDATE schedule_entries SET course_id=?, room_id=?, start_datetime=?, end_datetime=? WHERE entry_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, entry.getCourse().getCourseId());
            pstmt.setInt(2, entry.getRoom().getRoomId());
            pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(entry.getStartDateTime()));
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(entry.getEndDateTime()));
            pstmt.setInt(5, entry.getEntryId());
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

    // CRUD for Teacher
    public List<Teacher> getAllTeachers() throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        String query = "SELECT * FROM teachers";
        try (Connection conn = connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Teacher teacher = new Teacher();
                teacher.setTeacherId(rs.getInt("teacher_id"));
                teacher.setName(rs.getString("name"));
                teacher.setEmail(rs.getString("email"));
                teacher.setPhoneNumber(rs.getString("phone_number"));
                teachers.add(teacher);
            }
        }
        return teachers;
    }

    public void addTeacher(Teacher teacher) throws SQLException {
        String query = "INSERT INTO teachers (name, email, phone_number) VALUES (?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, teacher.getName());
            pstmt.setString(2, teacher.getEmail());
            pstmt.setString(3, teacher.getPhoneNumber());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    teacher.setTeacherId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateTeacher(Teacher teacher) throws SQLException {
        String query = "UPDATE teachers SET name=?, email=?, phone_number=? WHERE teacher_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, teacher.getName());
            pstmt.setString(2, teacher.getEmail());
            pstmt.setString(3, teacher.getPhoneNumber());
            pstmt.setInt(4, teacher.getTeacherId());
            pstmt.executeUpdate();
        }
    }

    public void deleteTeacher(int teacherId) throws SQLException {
        String query = "DELETE FROM teachers WHERE teacher_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, teacherId);
            pstmt.executeUpdate();
        }
    }

    // CRUD for SchoolClass
    public List<SchoolClass> getAllSchoolClasses() throws SQLException {
        List<SchoolClass> classes = new ArrayList<>();
        String query = "SELECT * FROM school_classes";
        try (Connection conn = connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setClassId(rs.getInt("class_id"));
                schoolClass.setName(rs.getString("name"));
                schoolClass.setGrade(rs.getInt("grade"));
                schoolClass.setSection(rs.getString("section"));
                classes.add(schoolClass);
            }
        }
        return classes;
    }

    public void addSchoolClass(SchoolClass schoolClass) throws SQLException {
        String query = "INSERT INTO school_classes (name, grade, section) VALUES (?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, schoolClass.getName());
            pstmt.setInt(2, schoolClass.getGrade());
            pstmt.setString(3, schoolClass.getSection());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    schoolClass.setClassId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateSchoolClass(SchoolClass schoolClass) throws SQLException {
        String query = "UPDATE school_classes SET name=?, grade=?, section=? WHERE class_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, schoolClass.getName());
            pstmt.setInt(2, schoolClass.getGrade());
            pstmt.setString(3, schoolClass.getSection());
            pstmt.setInt(4, schoolClass.getClassId());
            pstmt.executeUpdate();
        }
    }

    public void deleteSchoolClass(int classId) throws SQLException {
        String query = "DELETE FROM school_classes WHERE class_id=?";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, classId);
            pstmt.executeUpdate();
        }
    }
} 