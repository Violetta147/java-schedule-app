package com.yourcompany.schedule.data;

import com.yourcompany.schedule.core.Scheduler;
import com.yourcompany.schedule.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


public class DataManager {
    private final DatabaseConnector connector;
    private static final String DATABASE_NAME = "schedule_db";
    private final Scheduler schedulerForSeeding;
    
    public DataManager(DatabaseConnector connector) {
        this.connector = connector;
        this.schedulerForSeeding = new Scheduler();
    }

    public DataManager() {
        this(new DatabaseConnector());
    }

    public void initializeDatabase(boolean forceClearAndSeed) {
        try (Connection serverConn = connector.getServerConnection()) {
            createDatabaseIfNotExists(serverConn);
        } catch (SQLException e) {
            System.err.println("FATAL: Error creating database schema: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        try (Connection dbConn = connector.getConnection(DATABASE_NAME)) {
            createTablesIfNotExist(dbConn);
            seedInitialData(dbConn, forceClearAndSeed);
            System.out.println("Database initialization completed successfully for '" + DATABASE_NAME + "'.");
        } catch (SQLException e) {
            System.err.println("Error initializing tables or seeding data in '" + DATABASE_NAME + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void initializeDatabase() {
        initializeDatabase(false);
    }


    private void createDatabaseIfNotExists(Connection serverConn) throws SQLException {
        try (Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("Database '" + DATABASE_NAME + "' ensured to exist.");
        }
    }

    private void createTablesIfNotExist(Connection conn) throws SQLException {
        createAcaYearsTable(conn);
        createClassTable(conn);
        createTeachersTable(conn);
        createCoursesTable(conn);
        createRoomsTable(conn);
        createCourseOfferingsTable(conn);
        createScheduleEntriesTable(conn);
    }

    // --- Table Creation Methods (Giữ nguyên như bạn đã có) ---
    private void createAcaYearsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS aca_year (" +
                     "year_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "year_name VARCHAR(50) NOT NULL UNIQUE, " +
                     "start_date DATE NOT NULL, " +
                     "weeks INT NOT NULL)"; // ERD của bạn là 'weeks'
        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate(sql); }
        System.out.println("Table 'aca_year' ensured to exist.");
    }

    private void createClassTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS class (" +
                     "class_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "grade INT NOT NULL, " +
                     "section VARCHAR(50) NOT NULL, " +
                     "UNIQUE KEY uq_grade_section (grade, section)" +
                     ")";
        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate(sql); }
        System.out.println("Table 'class' ensured to exist.");
    }

    private void createTeachersTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS teacher (" +
                     "teacher_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "name VARCHAR(150) NOT NULL, " +
                     "email VARCHAR(100) UNIQUE, " +
                     "phone_number VARCHAR(20), " +
                     "class_id INT NULL, " +
                     "FOREIGN KEY (class_id) REFERENCES class(class_id) ON DELETE SET NULL ON UPDATE CASCADE" +
                     ")";
        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate(sql); }
        System.out.println("Table 'teacher' ensured to exist.");
    }

    private void createCoursesTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS course (" +
                     "course_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "course_code VARCHAR(20) NOT NULL UNIQUE, " +
                     "course_name VARCHAR(150) NOT NULL)";
        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate(sql); }
        System.out.println("Table 'course' ensured to exist.");
    }

    private void createRoomsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS room (" +
                     "room_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "room_name VARCHAR(100) NOT NULL UNIQUE, " +
                     "description TEXT)";
        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate(sql); }
        System.out.println("Table 'room' ensured to exist.");
    }

    private void createCourseOfferingsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS course_offering (" +
                     "offering_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "course_id INT NOT NULL, " +
                     "teacher_id INT NOT NULL, " +
                     "FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                     "FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                     "UNIQUE KEY uq_course_teacher (course_id, teacher_id)" +
                     ")";
        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate(sql); }
        System.out.println("Table 'course_offering' ensured to exist.");
    }

    private void createScheduleEntriesTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS schedule_entry (" +
                     "entry_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "offering_id INT NOT NULL, " +
                     "class_id INT NOT NULL, " +
                     "room_id INT NOT NULL, " +
                     "year_id INT NOT NULL, " +
                     "date DATE NOT NULL, " +
                     "start_period INT NOT NULL, " +
                     "end_period INT NOT NULL, " +
                     "FOREIGN KEY (offering_id) REFERENCES course_offering(offering_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                     "FOREIGN KEY (class_id) REFERENCES class(class_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                     "FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                     "FOREIGN KEY (year_id) REFERENCES aca_year(year_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                     ")";
        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate(sql); }
        System.out.println("Table 'schedule_entry' ensured to exist.");
    }


    // --- BIẾN THÀNH VIÊN DÙNG CHUNG CHO CÁC PHƯƠNG THỨC SEED ---
    private AcaYear currentAcaYearFkForSchedule;
    private List<Room> seededRoomsList;
    private List<SchoolClass> seededSchoolClassesList;
    private List<Course> seededCoursesList;
    private List<Teacher> seededTeachersList;
    private Map<String, List<Teacher>> teachersByCourseSpecialtyMap;
    private List<CourseOffering> seededCourseOfferingsList;
    private List<ScheduleEntry> successfullySeededEntriesThisSession;

    // Cấu trúc để lưu trữ số tiết/tuần cho mỗi môn, theo khối
    private Map<Integer, Map<Course, Integer>> weeklyPeriodsPerCoursePerGrade;

    // Cấu trúc để lưu trữ TKB tuần mẫu cho mỗi lớp (sau khi được tạo)
    // Key: classId, Value: Danh sách các slot đã được phân bổ môn học trong tuần đó
    private Map<Integer, List<ScheduleEntryTemplate>> weeklyTimetableTemplatePerClass;

    // Định nghĩa số tiết sáng/chiều
    private final int MORNING_SESSIONS_COUNT = 5; // Tiết 1-5
    private final int AFTERNOON_START_PERIOD = 6; // Tiết chiều bắt đầu từ tiết 6
    private final int AFTERNOON_SESSIONS_COUNT = 5; // Tiết 6-10
    private final int MAX_PERIODS_PER_DAY = AFTERNOON_START_PERIOD + AFTERNOON_SESSIONS_COUNT - 1; // Tiết cuối cùng trong ngày (10)
    private final int DAYS_IN_WEEK = 5; // Thứ 2 đến Thứ 6
    
    // --- CLEAR DATA METHOD ---
    private void clearAllData(Connection conn) throws SQLException {
        System.out.println("Clearing existing data...");
        String[] tablesToClearInOrder = {
            "schedule_entry", "course_offering", "teacher",
            "class", "room", "course", "aca_year"
        };
        try (Statement stmt = conn.createStatement()) {
            for (String tableName : tablesToClearInOrder) {
                System.out.println("Clearing table: " + tableName);
                stmt.executeUpdate("DELETE FROM " + tableName);
                // Reset auto-increment (ví dụ cho MySQL)
                // stmt.executeUpdate("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1;");
            }
            System.out.println("Data clearing process completed.");
        }
    }

    // --- Seeding Data Methods ---
    private void seedInitialData(Connection conn, boolean forceClearAndSeed) throws SQLException {
        if (forceClearAndSeed) {
            clearAllData(conn);
        }

        if (isTableEmpty(conn, "aca_year") || forceClearAndSeed) {
            System.out.println("Starting data seeding process (focusing on 2024-2025 with conflict checking)...");
            successfullySeededEntriesThisSession = new ArrayList<>(); 

            _seedAcaYears(conn);
            _seedRooms(conn);
            _seedSchoolClasses(conn);
            _seedCoursesAndTeachers(conn);
            
            _initializeCurriculum();
            
            _seedCourseOfferings(conn);
            
            _generateWeeklyTimetableTemplates();
            
            _seedScheduleEntries(conn);
            System.out.println("Data seeding process completed.");
        } else {
            System.out.println("Database already contains data. Skipping seeding process.");
        }
    }


    private void _seedAcaYears(Connection conn) throws SQLException {
        System.out.println("Seeding aca_year table...");
        AcaYear year2023_2024 = this.addAcaYear(new AcaYear(0, "2023-2024", LocalDate.of(2023, 9, 5), 35));
        currentAcaYearFkForSchedule = this.addAcaYear(new AcaYear(0, "2024-2025", LocalDate.of(2024, 9, 3), 35));
        AcaYear year2025_2026 = this.addAcaYear(new AcaYear(0, "2025-2026", LocalDate.of(2025, 9, 2), 35));

        System.out.println("Current academic year for detailed seeding: " + currentAcaYearFkForSchedule.getYearName());
    }

    private void _seedRooms(Connection conn) throws SQLException {
        System.out.println("Seeding room table...");
        seededRoomsList = new ArrayList<>();
        seededRoomsList.add(this.addRoom(new Room(0, "A101", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A102", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A103", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A104", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A105", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A106", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A201", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A202", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A203", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A204", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A301", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A302", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A303", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "A304", "Phòng học lý thuyết")));
        seededRoomsList.add(this.addRoom(new Room(0, "B201", "Phòng Tin học 1")));
        seededRoomsList.add(this.addRoom(new Room(0, "B202", "Phòng Tin học 2")));
        seededRoomsList.add(this.addRoom(new Room(0, "C301", "Phòng Thí nghiệm Lý")));
        seededRoomsList.add(this.addRoom(new Room(0, "C302", "Phòng Thí nghiệm Hóa")));
        seededRoomsList.add(this.addRoom(new Room(0, "D401", "Phòng Ngoại ngữ")));
        seededRoomsList.add(this.addRoom(new Room(0, "Hội trường", "Hội trường lớn")));
        seededRoomsList.add(this.addRoom(new Room(0, "Nhà Thi Đấu", "Nhà thi đấu thể thao")));
        seededRoomsList.add(this.addRoom(new Room(0, "Sân Bãi", "Sân thể dục ngoài trời")));
    }

    private void _seedSchoolClasses(Connection conn) throws SQLException {
        System.out.println("Seeding class table...");
        seededSchoolClassesList = new ArrayList<>();
        String[] classSuffixes = {"A1", "A2", "A3", "A4", "A5"};
        int[] grades = {10, 11, 12};
        for (int grade : grades) {
            for (String suffix : classSuffixes) {
                seededSchoolClassesList.add(this.addSchoolClass(new SchoolClass(0, grade, grade + suffix)));
            }
        }
    }

    private void _seedCoursesAndTeachers(Connection conn) throws SQLException {
        System.out.println("Seeding course and teacher tables...");
        seededCoursesList = new ArrayList<>();
        seededTeachersList = new ArrayList<>();
        teachersByCourseSpecialtyMap = new HashMap<>();

        Map<String, String[]> coursesData = new HashMap<>();
        coursesData.put("Toán", new String[]{"TOAN", "Nguyễn Văn Toán", "Trần Thị Đại Số", "Lê Văn Hình Học", "Phạm Phương Trình"});
        coursesData.put("Vật Lý", new String[]{"LY", "Hoàng Trọng Lý", "Mai Thị Quang Học", "Đỗ Văn Điện"});
        coursesData.put("Hóa Học", new String[]{"HOA", "Dương Thị Hóa", "Bùi Văn Chất", "Vũ Thị Phản Ứng"});
        coursesData.put("Sinh Học", new String[]{"SINH", "Lý Thị Sinh", "Trịnh Văn Gen", "Chu Thị Tế Bào"});
        coursesData.put("Ngữ Văn", new String[]{"VAN", "Văn Thị Thơ", "Đặng Văn Nghị Luận", "Kiều Thị Truyện"});
        coursesData.put("Lịch Sử", new String[]{"SU", "Sử Văn Đại", "Triệu Thị Cổ Đại", "Ngô Văn Cận Hiện"});
        coursesData.put("Địa Lý", new String[]{"DIA", "Địa Văn Đồ", "Thạch Thị Sông Ngòi", "Kim Thị Khí Hậu"});
        coursesData.put("Tiếng Anh", new String[]{"ANH", "John Smith", "Emily White", "David Brown", "Sarah Taylor"});
        coursesData.put("Tin Học", new String[]{"TIN", "Trịnh Minh Đạt", "Lê Quang Anh Khoa", "Biên Cao Cường"});
        coursesData.put("GDCD", new String[]{"GDCD", "Giáo Văn Dục", "Công Thị Dân", "Nguyễn Thị Pháp Luật"});
        coursesData.put("Công Nghệ", new String[]{"CN", "Kỹ Văn Thuật", "Thái Thị Nông Nghiệp", "Mạc Văn Công Nghiệp"});
        coursesData.put("Thể Dục", new String[]{"TD", "Khỏe Văn Mạnh", "Nhanh Thị Nhẹn", "Bật Văn Cao"});
        coursesData.put("GDQP-AN", new String[]{"QP", "Quốc Văn Phòng", "An Thị Ninh", "Vệ Văn Quốc"});


        int teacherPhoneCounter = 900000000;
        for (Map.Entry<String, String[]> entry : coursesData.entrySet()) {
            String courseName = entry.getKey();
            String courseCode = entry.getValue()[0];
            Course course = this.addCourse(new Course(0, courseCode, courseName));
            seededCoursesList.add(course);

            List<Teacher> courseTeachers = new ArrayList<>();
            for (int i = 1; i < entry.getValue().length; i++) {
                String teacherName = entry.getValue()[i];
                String email = teacherName.toLowerCase().replaceAll("\\s+", ".") + (i-1) +"@example.com";
                Teacher teacher = this.addTeacher(new Teacher(0, teacherName, email, "0" + teacherPhoneCounter++, null));
                seededTeachersList.add(teacher);
                courseTeachers.add(teacher);
            }
            teachersByCourseSpecialtyMap.put(courseCode, courseTeachers);
        }

        // Gán GVCN
        List<SchoolClass> unassignedClasses = new ArrayList<>(seededSchoolClassesList);
        Collections.shuffle(unassignedClasses);

        List<Teacher> teachersFromDbForFormAssignment = this.getAllTeachers();
        Collections.shuffle(teachersFromDbForFormAssignment);

        int classIdx = 0;
        for (Teacher teacher : teachersFromDbForFormAssignment) {
            if (classIdx < unassignedClasses.size()) {
                if (teacher.getClassId() == null) {
                    SchoolClass classToAssign = unassignedClasses.get(classIdx);
                    final int currentClassToAssignId = classToAssign.getClassId();
                    boolean classAlreadyHasFormTeacher = teachersFromDbForFormAssignment.stream()
                            .filter(t -> t.getClassId() != null)
                            .anyMatch(t -> t.getClassId().equals(currentClassToAssignId));

                    if (!classAlreadyHasFormTeacher) {
                        teacher.setClassId(classToAssign.getClassId());
                        this.updateTeacher(teacher);
                        classIdx++;
                    }
                }
            } else {
                break;
            }
        }
    }
    private void _seedCourseOfferings(Connection conn) throws SQLException {
        System.out.println("Seeding course_offering table...");
        seededCourseOfferingsList = new ArrayList<>();

        for (Course course : seededCoursesList) { // Sử dụng list đã có ID
            List<Teacher> availableTeachersFromMap = teachersByCourseSpecialtyMap.get(course.getCourseCode());
            if (availableTeachersFromMap != null && !availableTeachersFromMap.isEmpty()) {
                for(Teacher teacherRefFromMap : availableTeachersFromMap) {
                    CourseOffering newOffering = new CourseOffering(0, course, teacherRefFromMap);
                    seededCourseOfferingsList.add(this.addCourseOffering(newOffering));
                }
            }
        }
    }


    /**
     * Seed các ScheduleEntry dựa trên template tuần đã tạo.
     */
    private void _seedScheduleEntries(Connection conn) throws SQLException {
        System.out.println("Seeding schedule_entries based on weekly templates for year: " +
                           (currentAcaYearFkForSchedule != null ? currentAcaYearFkForSchedule.getYearName() : "N/A"));

        if (currentAcaYearFkForSchedule == null || seededCourseOfferingsList.isEmpty() ||
            seededSchoolClassesList.isEmpty() || seededRoomsList.isEmpty() ||
            weeklyTimetableTemplatePerClass == null || weeklyTimetableTemplatePerClass.isEmpty()) {
            System.err.println("Cannot seed schedule entries. Prerequisites missing (year, offerings, classes, rooms, or weekly templates).");
            return;
        }

        LocalDate firstDayOfYear = currentAcaYearFkForSchedule.getStartDate();
        int totalWeeksInYear = currentAcaYearFkForSchedule.getWeeks();
        int seededEntryCount = 0;
        int conflictSkippedCount = 0;
        int noTeacherRoomSkippedCount = 0;

        System.out.println("Total weeks to seed for actual schedule entries: " + totalWeeksInYear);

        for (int weekNum = 0; weekNum < totalWeeksInYear; weekNum++) {
            if (weekNum % 4 == 0) { // Log mỗi 4 tuần
                System.out.println("Seeding week " + (weekNum + 1) + "/" + totalWeeksInYear +
                                   " -> Entries Added: " + seededEntryCount +
                                   ", Scheduler Conflicts: " + conflictSkippedCount +
                                   ", No Teacher/Room: " + noTeacherRoomSkippedCount);
            }

            for (SchoolClass schoolClass : seededSchoolClassesList) {
                List<ScheduleEntryTemplate> classWeeklyTemplate = weeklyTimetableTemplatePerClass.get(schoolClass.getClassId());
                if (classWeeklyTemplate == null || classWeeklyTemplate.isEmpty()) {
                    continue;
                }

                for (ScheduleEntryTemplate templateEntry : classWeeklyTemplate) {
                    LocalDate currentDate = firstDayOfYear.plusWeeks(weekNum).plusDays(templateEntry.dayOfWeekOffset);

                    CourseOffering selectedOffering = findAvailableOfferingForCourse(templateEntry.course, currentDate, templateEntry.startPeriod, templateEntry.endPeriod);
                    if (selectedOffering == null) {
                        noTeacherRoomSkippedCount++;
                        continue;
                    }

                    Room selectedRoom = findAvailableRoom(templateEntry.course, currentDate, templateEntry.startPeriod, templateEntry.endPeriod, schoolClass);
                    if (selectedRoom == null) {
                        noTeacherRoomSkippedCount++;
                        continue;
                    }

                    ScheduleEntry potentialEntry = new ScheduleEntry(0, selectedOffering, schoolClass, selectedRoom,
                            currentAcaYearFkForSchedule, currentDate, templateEntry.startPeriod, templateEntry.endPeriod);

                    if (schedulerForSeeding.canAddEntry(potentialEntry, successfullySeededEntriesThisSession)) {
                        try {
                            ScheduleEntry addedEntry = this.addScheduleEntry(potentialEntry);
                            successfullySeededEntriesThisSession.add(addedEntry);
                            seededEntryCount++;
                        } catch (SQLException e) {
                            // DB level conflict (e.g. UNIQUE constraint not caught by in-memory check)
                            conflictSkippedCount++;
                        }
                    } else {
                        // Conflict detected by in-memory scheduler
                        conflictSkippedCount++;
                    }
                }
            }
        }
        System.out.println("Completed template-based seeding. Total entries added: " + seededEntryCount +
                           ", Scheduler/DB Conflicts Skipped: " + conflictSkippedCount +
                           ", Skipped due to no Teacher/Room: " + noTeacherRoomSkippedCount);
    }


    /**
     * Tìm một CourseOffering (Giáo viên + Môn) phù hợp và còn trống.
     */
    private CourseOffering findAvailableOfferingForCourse(Course course, LocalDate date, int startPeriod, int endPeriod) {
        List<CourseOffering> offeringsForCourse = seededCourseOfferingsList.stream()
                .filter(co -> co.getCourse().getCourseId() == course.getCourseId())
                .collect(Collectors.toList());
        Collections.shuffle(offeringsForCourse);

        for (CourseOffering offering : offeringsForCourse) {
            boolean teacherBusy = successfullySeededEntriesThisSession.stream()
                .filter(e -> e.getDate().equals(date)) // Chỉ xét các entry cùng ngày
                .filter(e -> e.getCourseOffering().getTeacher().getTeacherId() == offering.getTeacher().getTeacherId())
                .anyMatch(e -> periodsOverlap(startPeriod, endPeriod, e.getStartPeriod(), e.getEndPeriod()));
            if (!teacherBusy) {
                return offering;
            }
        }
        return null;
    }

    /**
     * Tìm một Phòng học phù hợp và còn trống.
     * Thêm tham số schoolClass để tránh một lớp dùng 2 phòng cùng lúc (dù ConflictChecker cũng sẽ bắt)
     */
    private Room findAvailableRoom(Course course, LocalDate date, int startPeriod, int endPeriod, SchoolClass currentClass) {
        List<Room> suitableRooms = new ArrayList<>(seededRoomsList);
        Collections.shuffle(suitableRooms);

        for (Room room : suitableRooms) {
            // Kiểm tra phòng có bận không
            boolean roomBusy = successfullySeededEntriesThisSession.stream()
                .filter(e -> e.getDate().equals(date))
                .filter(e -> e.getRoom().getRoomId() == room.getRoomId())
                .anyMatch(e -> periodsOverlap(startPeriod, endPeriod, e.getStartPeriod(), e.getEndPeriod()));

            if (roomBusy) continue;

            // Kiểm tra xem lớp này đã dùng phòng khác cùng lúc chưa
            // (ConflictChecker cũng sẽ kiểm tra điều này khi check lớp, nhưng ở đây là để chọn phòng)
            // boolean classUsingAnotherRoom = successfullySeededEntriesThisSession.stream()
            //     .filter(e -> e.getDate().equals(date) && e.getSchoolClass().getClassId() == currentClass.getClassId())
            //     .anyMatch(e -> periodsOverlap(startPeriod, endPeriod, e.getStartPeriod(), e.getEndPeriod()));
            // if (classUsingAnotherRoom) continue;


            // Logic phòng chuyên dụng (đơn giản)
            if (course.getCourseCode().equals("TIN") && !room.getRoomName().toLowerCase().contains("tin")) {
                continue;
            }
            if ((course.getCourseCode().equals("LY") || course.getCourseCode().equals("HOA") || course.getCourseCode().equals("SINH")) &&
                !room.getRoomName().toLowerCase().contains("thí nghiệm") && !room.getRoomName().toLowerCase().contains("lab")) {
                 // Có thể có nhiều phòng thí nghiệm, nên không loại trừ hoàn toàn nếu không tìm thấy chữ "lab"
                 // Ưu tiên phòng có chữ lab/thí nghiệm hơn. Nếu không có, thì có thể dùng phòng thường.
                 // Để đơn giản, nếu là môn TN mà không phải phòng TN, ta bỏ qua.
                if (suitableRooms.stream().anyMatch(r -> r.getRoomName().toLowerCase().contains("thí nghiệm") || r.getRoomName().toLowerCase().contains("lab"))) {
                     continue;
                }
            }
            if (course.getCourseCode().equals("TD") && (!room.getRoomName().toLowerCase().contains("thi đấu") && !room.getRoomName().toLowerCase().contains("sân"))) {
                 if (suitableRooms.stream().anyMatch(r -> r.getRoomName().toLowerCase().contains("thi đấu") || r.getRoomName().toLowerCase().contains("sân"))) {
                    continue;
                }
            }

            return room;
        }
        return null;
    }
   
    
    /**
     * Kiểm tra xem hai khoảng tiết có chồng chéo không.
     */
    private boolean periodsOverlap(int start1, int end1, int start2, int end2) {
        return Math.max(start1, start2) <= Math.min(end1, end2);
    }


    // --- Các phương thức CRUD hiện có của bạn (AcaYear, Teacher, SchoolClass, ...) ---
    //<editor-fold desc="AcaYear CRUD">
    public AcaYear addAcaYear(AcaYear acaYear) throws SQLException {
        String sql = "INSERT INTO aca_year (year_name, start_date, weeks) VALUES (?, ?, ?)";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, acaYear.getYearName());
            pstmt.setDate(2, Date.valueOf(acaYear.getStartDate()));
            pstmt.setInt(3, acaYear.getWeeks()); // Đã sửa ở model hoặc getter/setter
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    setGeneratedId(rs, acaYear);
                }
            }
        }
        return acaYear;
    }

    public List<AcaYear> getAllAcaYears() throws SQLException {
        List<AcaYear> acaYears = new ArrayList<>();
        String sql = "SELECT year_id, year_name, start_date, weeks FROM aca_year";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AcaYear acaYear = new AcaYear(
                        rs.getInt("year_id"),
                        rs.getString("year_name"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getInt("weeks")
                );
                acaYears.add(acaYear);
            }
        }
        return acaYears;
    }

     public Optional<AcaYear> getAcaYearById(int yearId) throws SQLException {
        String sql = "SELECT year_id, year_name, start_date, weeks FROM aca_year WHERE year_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    AcaYear acaYear = new AcaYear(
                            rs.getInt("year_id"),
                            rs.getString("year_name"),
                            rs.getDate("start_date").toLocalDate(),
                            rs.getInt("weeks")
                    );
                    return Optional.of(acaYear);
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateAcaYear(AcaYear acaYear) throws SQLException {
        String sql = "UPDATE aca_year SET year_name = ?, start_date = ?, weeks = ? WHERE year_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, acaYear.getYearName());
            pstmt.setDate(2, Date.valueOf(acaYear.getStartDate()));
            pstmt.setInt(3, acaYear.getWeeks());
            pstmt.setInt(4, acaYear.getYearId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteAcaYear(int yearId) throws SQLException {
        String sql = "DELETE FROM aca_year WHERE year_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            return pstmt.executeUpdate() > 0;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Teacher CRUD">
    public Teacher addTeacher(Teacher teacher) throws SQLException {
        String sql = "INSERT INTO teacher (name, email, phone_number, class_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, teacher.getName());
            pstmt.setString(2, teacher.getEmail());
            pstmt.setString(3, teacher.getPhoneNumber());
            if (teacher.getClassId() != null) {
                pstmt.setInt(4, teacher.getClassId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    setGeneratedId(rs, teacher);
                }
            }
        }
        return teacher;
    }

    public List<Teacher> getAllTeachers() throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT teacher_id, name, email, phone_number, class_id FROM teacher";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Teacher teacher = new Teacher(
                        rs.getInt("teacher_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone_number")
                );
                int classId = rs.getInt("class_id");
                if (!rs.wasNull()) {
                    teacher.setClassId(classId);
                }
                teachers.add(teacher);
            }
        }
        return teachers;
    }

    public Optional<Teacher> getTeacherById(int teacherId) throws SQLException {
        String sql = "SELECT teacher_id, name, email, phone_number, class_id FROM teacher WHERE teacher_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Teacher teacher = new Teacher(
                            rs.getInt("teacher_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone_number")
                    );
                    int classId = rs.getInt("class_id");
                    if (!rs.wasNull()) {
                        teacher.setClassId(classId);
                    }
                    return Optional.of(teacher);
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateTeacher(Teacher teacher) throws SQLException {
        String sql = "UPDATE teacher SET name = ?, email = ?, phone_number = ?, class_id = ? WHERE teacher_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacher.getName());
            pstmt.setString(2, teacher.getEmail());
            pstmt.setString(3, teacher.getPhoneNumber());
            if (teacher.getClassId() != null) {
                pstmt.setInt(4, teacher.getClassId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, teacher.getTeacherId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteTeacher(int teacherId) throws SQLException {
        String sql = "DELETE FROM teacher WHERE teacher_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            return pstmt.executeUpdate() > 0;
        }
    }
    //</editor-fold>

    //<editor-fold desc="SchoolClass CRUD">
    public SchoolClass addSchoolClass(SchoolClass schoolClass) throws SQLException {
        String sql = "INSERT INTO class (grade, section) VALUES (?, ?)";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, schoolClass.getGrade());
            pstmt.setString(2, schoolClass.getSection());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    setGeneratedId(rs, schoolClass);
                }
            }
        }
        return schoolClass;
    }

    public List<SchoolClass> getAllSchoolClasses() throws SQLException {
        List<SchoolClass> classes = new ArrayList<>();
        String sql = "SELECT class_id, grade, section FROM class";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SchoolClass schoolClass = new SchoolClass(
                        rs.getInt("class_id"),
                        rs.getInt("grade"),
                        rs.getString("section")
                );
                classes.add(schoolClass);
            }
        }
        return classes;
    }

    public Optional<SchoolClass> getSchoolClassById(int classId) throws SQLException {
        String sql = "SELECT class_id, grade, section FROM class WHERE class_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    SchoolClass schoolClass = new SchoolClass(
                            rs.getInt("class_id"),
                            rs.getInt("grade"),
                            rs.getString("section")
                    );
                    return Optional.of(schoolClass);
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateSchoolClass(SchoolClass schoolClass) throws SQLException {
        String sql = "UPDATE class SET grade = ?, section = ? WHERE class_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, schoolClass.getGrade());
            pstmt.setString(2, schoolClass.getSection());
            pstmt.setInt(3, schoolClass.getClassId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteSchoolClass(int classId) throws SQLException {
        String sql = "DELETE FROM class WHERE class_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            return pstmt.executeUpdate() > 0;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Course CRUD">
    public Course addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO course (course_code, course_name) VALUES (?, ?)";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseName());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    setGeneratedId(rs, course);
                }
            }
        }
        return course;
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, course_code, course_name FROM course";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Course course = new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name")
                );
                courses.add(course);
            }
        }
        return courses;
    }

    public Optional<Course> getCourseById(int courseId) throws SQLException {
        String sql = "SELECT course_id, course_code, course_name FROM course WHERE course_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Course course = new Course(
                            rs.getInt("course_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name")
                    );
                    return Optional.of(course);
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateCourse(Course course) throws SQLException {
        String sql = "UPDATE course SET course_code = ?, course_name = ? WHERE course_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseName());
            pstmt.setInt(3, course.getCourseId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCourse(int courseId) throws SQLException {
        String sql = "DELETE FROM course WHERE course_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            return pstmt.executeUpdate() > 0;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Room CRUD">
    public Room addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO room (room_name, description) VALUES (?, ?)";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, room.getRoomName());
            pstmt.setString(2, room.getDescription());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    setGeneratedId(rs, room);
                }
            }
        }
        return room;
    }

     public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT room_id, room_name, description FROM room";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_name"),
                        rs.getString("description")
                );
                rooms.add(room);
            }
        }
        return rooms;
    }

    public Optional<Room> getRoomById(int roomId) throws SQLException {
        String sql = "SELECT room_id, room_name, description FROM room WHERE room_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Room room = new Room(
                            rs.getInt("room_id"),
                            rs.getString("room_name"),
                            rs.getString("description")
                    );
                    return Optional.of(room);
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateRoom(Room room) throws SQLException {
        String sql = "UPDATE room SET room_name = ?, description = ? WHERE room_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomName());
            pstmt.setString(2, room.getDescription());
            pstmt.setInt(3, room.getRoomId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM room WHERE room_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            return pstmt.executeUpdate() > 0;
        }
    }
    //</editor-fold>

    //<editor-fold desc="CourseOffering CRUD">
    public CourseOffering addCourseOffering(CourseOffering offering) throws SQLException {
        if (offering.getCourse() == null || offering.getCourse().getCourseId() == 0 ||
            offering.getTeacher() == null || offering.getTeacher().getTeacherId() == 0) {
            throw new SQLException("Course and Teacher must be valid and have IDs to create a CourseOffering.");
        }
        String sql = "INSERT INTO course_offering (course_id, teacher_id) VALUES (?, ?)";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, offering.getCourse().getCourseId());
            pstmt.setInt(2, offering.getTeacher().getTeacherId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    setGeneratedId(rs, offering);
                }
            }
        }
        return offering;
    }
    
    // Helper method để hydrate CourseOffering từ ResultSet
    private CourseOffering mapRowToCourseOffering(ResultSet rs) throws SQLException {
        Course course = new Course(
                rs.getInt("co_course_id"),
                rs.getString("course_code"),
                rs.getString("course_name")
        );
        Teacher teacher = new Teacher(
                rs.getInt("co_teacher_id"),
                rs.getString("teacher_name"),
                rs.getString("teacher_email"),
                rs.getString("teacher_phone")
        );
        int teacherClassId = rs.getInt("teacher_class_id");
        if (!rs.wasNull()) {
            teacher.setClassId(teacherClassId);
        }
        return new CourseOffering(
                rs.getInt("offering_id"),
                course,
                teacher
        );
    }

    private String getBaseCourseOfferingQuery() {
        return "SELECT co.offering_id, co.course_id AS co_course_id, co.teacher_id AS co_teacher_id, " +
               "c.course_code, c.course_name, " +
               "t.name AS teacher_name, t.email AS teacher_email, t.phone_number AS teacher_phone, t.class_id AS teacher_class_id " +
               "FROM course_offering co " +
               "JOIN course c ON co.course_id = c.course_id " +
               "JOIN teacher t ON co.teacher_id = t.teacher_id ";
    }

    public List<CourseOffering> getAllCourseOfferings() throws SQLException {
        List<CourseOffering> offerings = new ArrayList<>();
        String sql = getBaseCourseOfferingQuery();
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                offerings.add(mapRowToCourseOffering(rs));
            }
        }
        return offerings;
    }

    public Optional<CourseOffering> getCourseOfferingById(int offeringId) throws SQLException {
        String sql = getBaseCourseOfferingQuery() + " WHERE co.offering_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, offeringId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCourseOffering(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateCourseOffering(CourseOffering offering) throws SQLException {
        if (offering.getCourse() == null || offering.getCourse().getCourseId() == 0 ||
            offering.getTeacher() == null || offering.getTeacher().getTeacherId() == 0) {
            throw new SQLException("Course and Teacher must be valid and have IDs to update a CourseOffering.");
        }
        String sql = "UPDATE course_offering SET course_id = ?, teacher_id = ? WHERE offering_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, offering.getCourse().getCourseId());
            pstmt.setInt(2, offering.getTeacher().getTeacherId());
            pstmt.setInt(3, offering.getOfferingId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCourseOffering(int offeringId) throws SQLException {
        String sql = "DELETE FROM course_offering WHERE offering_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, offeringId);
            return pstmt.executeUpdate() > 0;
        }
    }
    //</editor-fold>

    //<editor-fold desc="ScheduleEntry CRUD & Specific Queries">
    public ScheduleEntry addScheduleEntry(ScheduleEntry entry) throws SQLException {
        String sql = "INSERT INTO schedule_entry (offering_id, class_id, room_id, year_id, date, start_period, end_period) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, entry.getCourseOffering().getOfferingId());
            pstmt.setInt(2, entry.getSchoolClass().getClassId());
            pstmt.setInt(3, entry.getRoom().getRoomId());
            pstmt.setInt(4, entry.getAcaYear().getYearId());
            pstmt.setDate(5, Date.valueOf(entry.getDate()));
            pstmt.setInt(6, entry.getStartPeriod());
            pstmt.setInt(7, entry.getEndPeriod());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    setGeneratedId(rs, entry);
                }
            }
        }
        return entry;
    }

        private ScheduleEntry mapRowToScheduleEntry(ResultSet rs) throws SQLException {
        Course course = new Course(rs.getInt("co_course_id"), rs.getString("course_code"), rs.getString("course_name"));
        Teacher teacher = new Teacher(rs.getInt("co_teacher_id"), rs.getString("teacher_name"), rs.getString("teacher_email"), rs.getString("teacher_phone"));
        if (rs.getObject("teacher_class_id") != null) {
            teacher.setClassId(rs.getInt("teacher_class_id"));
        }
        // CourseOffering không chứa AcaYear theo ERD
        CourseOffering offering = new CourseOffering(rs.getInt("offering_id"), course, teacher);
        SchoolClass schoolClass = new SchoolClass(rs.getInt("sc_class_id"), rs.getInt("class_grade"), rs.getString("class_section"));
        Room room = new Room(rs.getInt("r_room_id"), rs.getString("room_name"), rs.getString("room_description"));
        AcaYear acaYear = new AcaYear(rs.getInt("ay_year_id"), rs.getString("aca_year_name"),
                                      rs.getDate("aca_start_date").toLocalDate(), rs.getInt("aca_weeks"));

        return new ScheduleEntry(
            rs.getInt("entry_id"), offering, schoolClass, room, acaYear,
            rs.getDate("date").toLocalDate(), rs.getInt("start_period"), rs.getInt("end_period")
        );
    }

    private String getBaseScheduleEntryQuery() {
        return "SELECT se.entry_id, se.date, se.start_period, se.end_period, " +
               "co.offering_id, co.course_id AS co_course_id, co.teacher_id AS co_teacher_id, " +
               "c.course_code, c.course_name, " +
               "t.name AS teacher_name, t.email AS teacher_email, t.phone_number AS teacher_phone, t.class_id AS teacher_class_id, " +
               "sc.class_id AS sc_class_id, sc.grade AS class_grade, sc.section AS class_section, " +
               "r.room_id AS r_room_id, r.room_name, r.description AS room_description, " +
               "ay.year_id AS ay_year_id, ay.year_name AS aca_year_name, ay.start_date AS aca_start_date, ay.weeks AS aca_weeks " +
               "FROM schedule_entry se " +
               "JOIN course_offering co ON se.offering_id = co.offering_id " +
               "JOIN course c ON co.course_id = c.course_id " +
               "JOIN teacher t ON co.teacher_id = t.teacher_id " +
               "JOIN class sc ON se.class_id = sc.class_id " +
               "JOIN room r ON se.room_id = r.room_id " +
               "JOIN aca_year ay ON se.year_id = ay.year_id ";
    }

    public List<ScheduleEntry> getAllScheduleEntries() throws SQLException {
        List<ScheduleEntry> entries = new ArrayList<>();
        String sql = getBaseScheduleEntryQuery();
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                entries.add(mapRowToScheduleEntry(rs));
            }
        }
        return entries;
    }

    public Optional<ScheduleEntry> getScheduleEntryById(int entryId) throws SQLException {
        String sql = getBaseScheduleEntryQuery() + " WHERE se.entry_id = ?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToScheduleEntry(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateScheduleEntry(ScheduleEntry entry) throws SQLException {
        String sql = "UPDATE schedule_entry SET offering_id=?, class_id=?, room_id=?, year_id=?, date=?, start_period=?, end_period=? WHERE entry_id=?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entry.getCourseOffering().getOfferingId());
            pstmt.setInt(2, entry.getSchoolClass().getClassId());
            pstmt.setInt(3, entry.getRoom().getRoomId());
            pstmt.setInt(4, entry.getAcaYear().getYearId());
            pstmt.setDate(5, Date.valueOf(entry.getDate()));
            pstmt.setInt(6, entry.getStartPeriod());
            pstmt.setInt(7, entry.getEndPeriod());
            pstmt.setInt(8, entry.getEntryId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteScheduleEntry(int entryId) throws SQLException {
        String sql = "DELETE FROM schedule_entry WHERE entry_id=?";
        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<ScheduleEntry> getPotentialConflictEntries(LocalDate date, int roomId, int teacherId, int classId) throws SQLException {
        List<ScheduleEntry> potentialConflicts = new ArrayList<>();
        // Cần điều chỉnh câu truy vấn này để lấy teacher_id từ course_offering
        String sql = getBaseScheduleEntryQuery() +
                     "WHERE se.date = ? AND (se.room_id = ? OR co.teacher_id = ? OR se.class_id = ?)";

        try (Connection conn = connector.getConnection(DATABASE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(date));
            pstmt.setInt(2, roomId);
            pstmt.setInt(3, teacherId);
            pstmt.setInt(4, classId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    potentialConflicts.add(mapRowToScheduleEntry(rs));
                }
            }
        }
        return potentialConflicts;
    }

    //</editor-fold>


    private boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(countQuery)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private void setGeneratedId(ResultSet rs, Object entityWithIdSetter) throws SQLException {
        if (rs.next()) {
            int id = rs.getInt(1);
            if (entityWithIdSetter instanceof AcaYear) ((AcaYear) entityWithIdSetter).setYearId(id);
            else if (entityWithIdSetter instanceof Teacher) ((Teacher) entityWithIdSetter).setTeacherId(id);
            else if (entityWithIdSetter instanceof SchoolClass) ((SchoolClass) entityWithIdSetter).setClassId(id);
            else if (entityWithIdSetter instanceof Course) ((Course) entityWithIdSetter).setCourseId(id);
            else if (entityWithIdSetter instanceof Room) ((Room) entityWithIdSetter).setRoomId(id);
            else if (entityWithIdSetter instanceof CourseOffering) ((CourseOffering) entityWithIdSetter).setOfferingId(id);
            else if (entityWithIdSetter instanceof ScheduleEntry) ((ScheduleEntry) entityWithIdSetter).setEntryId(id);
        }
    }
    
    /**
     * Khởi tạo dữ liệu về số tiết/tuần cho mỗi môn học theo từng khối.
     * Cần được gọi sau khi seededCoursesList đã được populate.
     */
    private void _initializeCurriculum() {
        System.out.println("Initializing curriculum (periods per week per course per grade)...");
        weeklyPeriodsPerCoursePerGrade = new HashMap<>();

        // --- KHỐI 10 ---
        Map<Course, Integer> grade10Curriculum = new HashMap<>();
        // Lưu ý: findCourseByCode sẽ tìm trong seededCoursesList đã có ID
        Course toan10 = findCourseByCode("TOAN"); if (toan10 != null) grade10Curriculum.put(toan10, 4); else System.err.println("Curriculum Error: TOAN not found");
        Course van10 = findCourseByCode("VAN");   if (van10 != null) grade10Curriculum.put(van10, 4); else System.err.println("Curriculum Error: VAN not found");
        Course anh10 = findCourseByCode("ANH");   if (anh10 != null) grade10Curriculum.put(anh10, 3); else System.err.println("Curriculum Error: ANH not found");
        Course ly10 = findCourseByCode("LY");     if (ly10 != null) grade10Curriculum.put(ly10, 2); else System.err.println("Curriculum Error: LY not found");
        Course hoa10 = findCourseByCode("HOA");   if (hoa10 != null) grade10Curriculum.put(hoa10, 2); else System.err.println("Curriculum Error: HOA not found");
        Course sinh10 = findCourseByCode("SINH"); if (sinh10 != null) grade10Curriculum.put(sinh10, 2); else System.err.println("Curriculum Error: SINH not found");
        Course su10 = findCourseByCode("SU");     if (su10 != null) grade10Curriculum.put(su10, 1); else System.err.println("Curriculum Error: SU not found"); // Có thể 1.5, làm tròn
        Course dia10 = findCourseByCode("DIA");   if (dia10 != null) grade10Curriculum.put(dia10, 1); else System.err.println("Curriculum Error: DIA not found"); // Có thể 1.5, làm tròn
        Course tin10 = findCourseByCode("TIN");   if (tin10 != null) grade10Curriculum.put(tin10, 2); else System.err.println("Curriculum Error: TIN not found");
        Course gdcd10 = findCourseByCode("GDCD"); if (gdcd10 != null) grade10Curriculum.put(gdcd10, 1); else System.err.println("Curriculum Error: GDCD not found");
        Course cn10 = findCourseByCode("CN");     if (cn10 != null) grade10Curriculum.put(cn10, 1); else System.err.println("Curriculum Error: CN not found");
        Course td10 = findCourseByCode("TD");     if (td10 != null) grade10Curriculum.put(td10, 2); else System.err.println("Curriculum Error: TD not found");
        Course qp10 = findCourseByCode("QP");     if (qp10 != null) grade10Curriculum.put(qp10, 2); else System.err.println("Curriculum Error: QP not found"); // GDQP thường gộp
        weeklyPeriodsPerCoursePerGrade.put(10, grade10Curriculum);

        // --- KHỐI 11 --- (Tương tự, điều chỉnh số tiết nếu cần)
        Map<Course, Integer> grade11Curriculum = new HashMap<>(grade10Curriculum); // Copy từ khối 10 rồi điều chỉnh
        // Ví dụ: Khối 11 có thể tăng/giảm một số môn
        // Course ly11 = findCourseByCode("LY"); if (ly11 != null) grade11Curriculum.put(ly11, 3);
        weeklyPeriodsPerCoursePerGrade.put(11, grade11Curriculum);

        // --- KHỐI 12 --- (Tương tự)
        Map<Course, Integer> grade12Curriculum = new HashMap<>(grade11Curriculum);
        // Ví dụ: Khối 12 tập trung ôn thi
        // Course toan12 = findCourseByCode("TOAN"); if (toan12 != null) grade12Curriculum.put(toan12, 5);
        weeklyPeriodsPerCoursePerGrade.put(12, grade12Curriculum);
        System.out.println("Curriculum initialized.");
    }

    /**
     * Helper để tìm Course từ seededCoursesList bằng mã môn.
     */
    private Course findCourseByCode(String courseCode) {
        if (seededCoursesList == null) return null;
        return seededCoursesList.stream()
                .filter(c -> c.getCourseCode().equalsIgnoreCase(courseCode))
                .findFirst().orElse(null);
    }

    /**
     * Cấu trúc tạm thời để lưu trữ một slot trong template tuần.
     */
    private static class ScheduleEntryTemplate {
        Course course;
        int dayOfWeekOffset; // 0=Thứ 2, ..., 4=Thứ 6
        int startPeriod;
        int endPeriod;

        public ScheduleEntryTemplate(Course course, int dayOfWeekOffset, int startPeriod, int endPeriod) {
            this.course = course;
            this.dayOfWeekOffset = dayOfWeekOffset;
            this.startPeriod = startPeriod;
            this.endPeriod = endPeriod;
        }

        @Override
        public String toString() {
            return String.format("Day %d, P%d-%d: %s", dayOfWeekOffset, startPeriod, endPeriod, course.getCourseCode());
        }
    }

    /**
     * Tạo ra một "khung" thời khóa biểu tuần cho mỗi lớp dựa trên curriculum.
     * Đây là một logic đơn giản, có thể cải thiện nhiều.
     * Cần được gọi sau _initializeCurriculum() và seededSchoolClassesList đã được populate.
     */
    private void _generateWeeklyTimetableTemplates() {
        System.out.println("Generating weekly timetable templates for each class...");
        weeklyTimetableTemplatePerClass = new HashMap<>();
        if (seededSchoolClassesList == null || weeklyPeriodsPerCoursePerGrade == null) {
            System.err.println("Error: Cannot generate templates. Missing school classes or curriculum.");
            return;
        }

        Random random = new Random();

        for (SchoolClass sc : seededSchoolClassesList) {
            Map<Course, Integer> curriculum = weeklyPeriodsPerCoursePerGrade.get(sc.getGrade());
            if (curriculum == null) {
                System.err.println("Warning: No curriculum found for grade " + sc.getGrade() + ". Skipping template for class " + sc.getName());
                continue;
            }

            List<ScheduleEntryTemplate> classTemplate = new ArrayList<>();
            // Tạo danh sách các "tiết đơn lẻ" cần xếp
            List<Course> singlePeriodUnitsToSchedule = new ArrayList<>();
            for (Map.Entry<Course, Integer> entry : curriculum.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) { // Mỗi đơn vị là 1 tiết
                    singlePeriodUnitsToSchedule.add(entry.getKey());
                }
            }
            Collections.shuffle(singlePeriodUnitsToSchedule); // Xáo trộn để có sự đa dạng

            // Tạo một lưới trống cho tuần của lớp: [ngày][tiết] -> đã được sử dụng chưa
            boolean[][] weeklySlotUsed = new boolean[DAYS_IN_WEEK][MAX_PERIODS_PER_DAY + 1];

            // Cố gắng xếp các môn 2 tiết trước (Toán, Văn, Anh, Lý, Hóa, Sinh, Tin, QP)
            List<Course> twoPeriodCourses = new ArrayList<>();
            curriculum.forEach((course, periods) -> {
                if (periods >= 2 && (course.getCourseCode().equals("TOAN") || course.getCourseCode().equals("VAN") ||
                                      course.getCourseCode().equals("ANH") || course.getCourseCode().equals("LY") ||
                                      course.getCourseCode().equals("HOA") || course.getCourseCode().equals("SINH") ||
                                      course.getCourseCode().equals("TIN") || course.getCourseCode().equals("QP"))) {
                    // Mỗi 2 tiết trong curriculum tương ứng 1 lần xếp môn 2 tiết
                    for (int i = 0; i < periods / 2; i++) {
                        twoPeriodCourses.add(course);
                    }
                }
            });
            Collections.shuffle(twoPeriodCourses);

            for (Course course : twoPeriodCourses) {
                boolean placed = false;
                for (int attempt = 0; attempt < 20 && !placed; attempt++) { // Thử tìm slot 20 lần
                    int day = random.nextInt(DAYS_IN_WEEK);
                    // Ưu tiên buổi sáng cho môn 2 tiết
                    int startP = random.nextInt(1, MORNING_SESSIONS_COUNT); // Tiết 1->4 (để vừa 2 tiết sáng)
                    if (startP + 1 > MORNING_SESSIONS_COUNT) startP = MORNING_SESSIONS_COUNT -1; // đảm bảo không vượt

                    if (!weeklySlotUsed[day][startP] && !weeklySlotUsed[day][startP + 1]) {
                        classTemplate.add(new ScheduleEntryTemplate(course, day, startP, startP + 1));
                        weeklySlotUsed[day][startP] = true;
                        weeklySlotUsed[day][startP + 1] = true;
                        // Xóa 2 đơn vị tiết của môn này khỏi singlePeriodUnitsToSchedule
                        removePeriodsFromList(singlePeriodUnitsToSchedule, course, 2);
                        placed = true;
                    }
                }
            }

            // Xếp các tiết đơn lẻ còn lại
            for (Course course : singlePeriodUnitsToSchedule) {
                boolean placed = false;
                for (int attempt = 0; attempt < 20 && !placed; attempt++) {
                    int day = random.nextInt(DAYS_IN_WEEK);
                    int period = random.nextInt(1, MAX_PERIODS_PER_DAY + 1);
                    if (!weeklySlotUsed[day][period]) {
                        classTemplate.add(new ScheduleEntryTemplate(course, day, period, period));
                        weeklySlotUsed[day][period] = true;
                        placed = true;
                    }
                }
            }
            Collections.sort(classTemplate, (t1, t2) -> { // Sắp xếp lại template cho dễ nhìn (theo ngày, rồi theo tiết)
                if (t1.dayOfWeekOffset != t2.dayOfWeekOffset) {
                    return Integer.compare(t1.dayOfWeekOffset, t2.dayOfWeekOffset);
                }
                return Integer.compare(t1.startPeriod, t2.startPeriod);
            });
            weeklyTimetableTemplatePerClass.put(sc.getClassId(), classTemplate);
            // System.out.println("Template for " + sc.getClassName() + ": " + classTemplate.size() + " entries.");
        }
        System.out.println("Weekly timetable templates generated.");
    }

    private void removePeriodsFromList(List<Course> list, Course courseToRemove, int numPeriods) {
        int removedCount = 0;
        for (int i = 0; i < list.size() && removedCount < numPeriods; ) {
            if (list.get(i).getCourseId() == courseToRemove.getCourseId()) {
                list.remove(i);
                removedCount++;
            } else {
                i++;
            }
        }
    }
}