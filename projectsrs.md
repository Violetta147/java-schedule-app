# Yêu cầu phát triển ứng dụng quản lý lịch học

## Tổng quan
Phát triển ứng dụng desktop đơn giản để quản lý lịch học, sử dụng Java với AWT/Swing cho giao diện và MySQL làm cơ sở dữ liệu.

## Công nghệ sử dụng
- Java SE
- GUI: AWT/Swing (javax.swing)
- Database: MySQL (quản lý bằng phpMyAdmin)
- JDBC cho kết nối database

## Cấu trúc dự án

project-root/
├── src/
│   └── com/
│       └── yourcompany/
│           └── schedule/
│               ├── MainApp.java                // Lớp chứa phương thức main(), khởi chạy ứng dụng
│               ├── ui/                         // Các lớp giao diện người dùng
│               │   ├── MainScheduleFrame.java  // Cửa sổ chính
│               │   ├── SchedulePanel.java      // Panel hiển thị lịch
│               │   ├── RoomSchedulePanel.java  // Panel hiển thị lịch phòng học
│               │   ├── AddEditEntryDialog.java // Hộp thoại thêm/sửa môn học
│               │   └── ConflictDialog.java     // Hộp thoại cảnh báo trùng giờ
│               ├── core/                       // Xử lý logic nghiệp vụ
│               │   ├── Scheduler.java          // Xử lý lịch và kiểm tra xung đột
│               │   └── ConflictChecker.java    // Kiểm tra trùng giờ
│               ├── data/                       // Xử lý dữ liệu
│               │   ├── DatabaseConnector.java  // Kết nối CSDL
│               │   └── DataManager.java        // Quản lý truy vấn dữ liệu
│               └── model/                      // Các đối tượng dữ liệu
│                   ├── Course.java             // Lớp biểu diễn môn học
│                   ├── Room.java               // Lớp biểu diễn phòng học
│                   ├── ScheduleEntry.java      // Lớp biểu diễn mục lịch
│                   └── Schedule.java           // Lớp biểu diễn toàn bộ lịch


## Cơ sở dữ liệu MySQL
```sql
-- Tạo database
CREATE DATABASE schedule_manager;
USE schedule_manager;

-- Bảng phòng học
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(50) NOT NULL,
    capacity INT,
    description VARCHAR(255)
);

-- Bảng môn học
CREATE TABLE courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    instructor VARCHAR(100),
    credits INT
);

-- Bảng lịch học
CREATE TABLE schedule_entries (
    entry_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT,
    room_id INT,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

Chi tiết các lớp chính
1. Model

Course.java

public class Course {
    private int courseId;
    private String courseCode;
    private String courseName;
    private String instructor;
    private int credits;
    
    // Constructors, getters, setters
}

Room.java
public class Room {
    private int roomId;
    private String roomName;
    private int capacity;
    private String description;
    
    // Constructors, getters, setters
}
ScheduleEntry.java
public class ScheduleEntry {
    private int entryId;
    private Course course;
    private Room room;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    
    // Xác định xung đột
    public boolean conflictsWith(ScheduleEntry other) {
        return dayOfWeek.equals(other.dayOfWeek) &&
               !startTime.isAfter(other.endTime) &&
               !endTime.isBefore(other.startTime);
    }
    
    // Constructors, getters, setters
}
2. Data

DatabaseConnector.java
public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/java";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Thay đổi theo cấu hình
    
    private Connection connection;
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class DataManager {
    private DatabaseConnector connector;
    
    public DataManager() {
        connector = new DatabaseConnector();
    }
    
    // Các phương thức CRUD cho Course
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
    DataManager.java
    // Các phương thức CRUD tương tự cho Room và ScheduleEntry
    
    // Phương thức kiểm tra xung đột lịch
    public List<ScheduleEntry> findConflictingEntries(ScheduleEntry entry) throws SQLException {
        List<ScheduleEntry> conflicts = new ArrayList<>();
        String query = "SELECT e.*, c.*, r.* FROM schedule_entries e " +
                       "JOIN courses c ON e.course_id = c.course_id " +
                       "JOIN rooms r ON e.room_id = r.room_id " +
                       "WHERE e.day_of_week = ? " +
                       "AND ((e.start_time <= ? AND e.end_time > ?) OR " +
                       "     (e.start_time < ? AND e.end_time >= ?) OR " +
                       "     (e.start_time >= ? AND e.end_time <= ?))";
        
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, entry.getDayOfWeek().toString());
            pstmt.setTime(2, Time.valueOf(entry.getEndTime()));
            pstmt.setTime(3, Time.valueOf(entry.getStartTime()));
            pstmt.setTime(4, Time.valueOf(entry.getEndTime()));
            pstmt.setTime(5, Time.valueOf(entry.getStartTime()));
            pstmt.setTime(6, Time.valueOf(entry.getStartTime()));
            pstmt.setTime(7, Time.valueOf(entry.getEndTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Tạo đối tượng ScheduleEntry, Course, Room từ ResultSet
                    // Thêm vào danh sách conflicts
                }
            }
        }
        
        return conflicts;
    }
}

3. Core
Scheduler.java

public class Scheduler {
    private DataManager dataManager;
    
    public Scheduler() {
        dataManager = new DataManager();
    }
    
    public boolean addScheduleEntry(ScheduleEntry entry) throws SQLException {
        // Kiểm tra xung đột
        List<ScheduleEntry> conflicts = dataManager.findConflictingEntries(entry);
        
        if (!conflicts.isEmpty()) {
            return false; // Có xung đột
        }
        
        // Thêm vào CSDL nếu không có xung đột
        dataManager.addScheduleEntry(entry);
        return true;
    }
    
    public List<ScheduleEntry> getScheduleForRoom(Room room) throws SQLException {
        return dataManager.getEntriesByRoom(room);
    }
    
    public List<ScheduleEntry> getScheduleForCourse(Course course) throws SQLException {
        return dataManager.getEntriesByCourse(course);
    }
    
    // Các phương thức khác
}
4. UI
MainScheduleFrame.java

public class MainScheduleFrame extends JFrame {
    private Scheduler scheduler;
    private SchedulePanel schedulePanel;
    private JButton addButton, editButton, deleteButton;
    private JButton checkConflictsButton, viewRoomButton;
    
    public MainScheduleFrame() {
        this.scheduler = new Scheduler();
        
        setTitle("Quản lý lịch học");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        initComponents();
        layoutComponents();
        registerEventHandlers();
    }
    
    private void initComponents() {
        schedulePanel = new SchedulePanel(scheduler);
        addButton = new JButton("Thêm");
        editButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        checkConflictsButton = new JButton("Kiểm tra xung đột");
        viewRoomButton = new JButton("Xem lịch phòng");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        add(schedulePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(checkConflictsButton);
        buttonPanel.add(viewRoomButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void registerEventHandlers() {
        addButton.addActionListener(e -> showAddEntryDialog());
        editButton.addActionListener(e -> showEditEntryDialog());
        deleteButton.addActionListener(e -> deleteSelectedEntry());
        checkConflictsButton.addActionListener(e -> checkConflicts());
        viewRoomButton.addActionListener(e -> showRoomSchedule());
    }
    
    private void showAddEntryDialog() {
        AddEditEntryDialog dialog = new AddEditEntryDialog(this, null, scheduler);
        dialog.setVisible(true);
        // Refresh schedule panel after dialog closes
        schedulePanel.refreshSchedule();
    }
    
    // Các phương thức xử lý sự kiện khác
}
SchedulePanel.java
public class SchedulePanel extends JPanel {
    private Scheduler scheduler;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    
    public SchedulePanel(Scheduler scheduler) {
        this.scheduler = scheduler;
        
        setLayout(new BorderLayout());
        initTable();
        refreshSchedule();
    }
    
    private void initTable() {
        String[] columnNames = {"Môn học", "Phòng", "Thứ", "Bắt đầu", "Kết thúc"};
        tableModel = new DefaultTableModel(columnNames, 0);
        scheduleTable = new JTable(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void refreshSchedule() {
        tableModel.setRowCount(0);
        
        try {
            List<ScheduleEntry> entries = scheduler.getAllScheduleEntries();
            for (ScheduleEntry entry : entries) {
                Object[] rowData = {
                    entry.getCourse().getCourseName(),
                    entry.getRoom().getRoomName(),
                    getDayOfWeekDisplay(entry.getDayOfWeek()),
                    entry.getStartTime().toString(),
                    entry.getEndTime().toString()
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), 
                                         "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getDayOfWeekDisplay(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            case SUNDAY: return "Chủ nhật";
            default: return "";
        }
    }
}

AddEditEntryDialog.java
public class AddEditEntryDialog extends JDialog {
    private Scheduler scheduler;
    private ScheduleEntry existingEntry;
    
    private JComboBox<Course> courseComboBox;
    private JComboBox<Room> roomComboBox;
    private JComboBox<DayOfWeek> dayComboBox;
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;
    
    private JButton saveButton;
    private JButton cancelButton;
    
    public AddEditEntryDialog(Frame owner, ScheduleEntry entry, Scheduler scheduler) {
        super(owner, entry == null ? "Thêm lịch học" : "Sửa lịch học", true);
        this.scheduler = scheduler;
        this.existingEntry = entry;
        
        initComponents();
        layoutComponents();
        registerEventHandlers();
        loadData();
        
        pack();
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        courseComboBox = new JComboBox<>();
        roomComboBox = new JComboBox<>();
        
        dayComboBox = new JComboBox<>(new DayOfWeek[]{
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        });
        
        SpinnerDateModel startModel = new SpinnerDateModel();
        startTimeSpinner = new JSpinner(startModel);
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
        startTimeSpinner.setEditor(startEditor);
        
        SpinnerDateModel endModel = new SpinnerDateModel();
        endTimeSpinner = new JSpinner(endModel);
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
        endTimeSpinner.setEditor(endEditor);
        
        saveButton = new JButton("Lưu");
        cancelButton = new JButton("Hủy");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        inputPanel.add(new JLabel("Môn học:"));
        inputPanel.add(courseComboBox);
        
        inputPanel.add(new JLabel("Phòng học:"));
        inputPanel.add(roomComboBox);
        
        inputPanel.add(new JLabel("Thứ:"));
        inputPanel.add(dayComboBox);
        
        inputPanel.add(new JLabel("Giờ bắt đầu:"));
        inputPanel.add(startTimeSpinner);
        
        inputPanel.add(new JLabel("Giờ kết thúc:"));
        inputPanel.add(endTimeSpinner);
        
        add(inputPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void registerEventHandlers() {
        saveButton.addActionListener(e -> saveEntry());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void loadData() {
        try {
            // Load courses
            List<Course> courses = scheduler.getAllCourses();
            for (Course course : courses) {
                courseComboBox.addItem(course);
            }
            
            // Load rooms
            List<Room> rooms = scheduler.getAllRooms();
            for (Room room : rooms) {
                roomComboBox.addItem(room);
            }
            
            // Set existing values if editing
            if (existingEntry != null) {
                courseComboBox.setSelectedItem(existingEntry.getCourse());
                roomComboBox.setSelectedItem(existingEntry.getRoom());
                dayComboBox.setSelectedItem(existingEntry.getDayOfWeek());
                
                // Set time values
                Calendar cal = Calendar.getInstance();
                
                cal.set(Calendar.HOUR_OF_DAY, existingEntry.getStartTime().getHour());
                cal.set(Calendar.MINUTE, existingEntry.getStartTime().getMinute());
                startTimeSpinner.setValue(cal.getTime());
                
                cal.set(Calendar.HOUR_OF_DAY, existingEntry.getEndTime().getHour());
                cal.set(Calendar.MINUTE, existingEntry.getEndTime().getMinute());
                endTimeSpinner.setValue(cal.getTime());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), 
                                         "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveEntry() {
        try {
            Course selectedCourse = (Course) courseComboBox.getSelectedItem();
            Room selectedRoom = (Room) roomComboBox.getSelectedItem();
            DayOfWeek selectedDay = (DayOfWeek) dayComboBox.getSelectedItem();
            
            // Get time values
            Date startDate = (Date) startTimeSpinner.getValue();
            Date endDate = (Date) endTimeSpinner.getValue();
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            LocalTime startTime = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            
            cal.setTime(endDate);
            LocalTime endTime = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                JOptionPane.showMessageDialog(this, "Thời gian kết thúc phải sau thời gian bắt đầu", 
                                             "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            ScheduleEntry entry;
            if (existingEntry == null) {
                entry = new ScheduleEntry();
            } else {
                entry = existingEntry;
            }
            
            entry.setCourse(selectedCourse);
            entry.setRoom(selectedRoom);
            entry.setDayOfWeek(selectedDay);
            entry.setStartTime(startTime);
            entry.setEndTime(endTime);
            
            boolean success;
            if (existingEntry == null) {
                success = scheduler.addScheduleEntry(entry);
            } else {
                success = scheduler.updateScheduleEntry(entry);
            }
            
            if (!success) {
                List<ScheduleEntry> conflicts = scheduler.findConflictingEntries(entry);
                showConflictDialog(conflicts);
                return;
            }
            
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu: " + e.getMessage(), 
                                         "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showConflictDialog(List<ScheduleEntry> conflicts) {
        ConflictDialog dialog = new ConflictDialog(this, conflicts);
        dialog.setVisible(true);
    }
}

ConflictDialog.java

public class ConflictDialog extends JDialog {
    private List<ScheduleEntry> conflicts;
    private JTable conflictTable;
    private JButton closeButton;
    
    public ConflictDialog(Dialog owner, List<ScheduleEntry> conflicts) {
        super(owner, "Phát hiện xung đột lịch", true);
        this.conflicts = conflicts;
        
        initComponents();
        layoutComponents();
        registerEventHandlers();
        
        setSize(600, 300);
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        String[] columnNames = {"Môn học", "Phòng", "Thứ", "Bắt đầu", "Kết thúc"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        
        for (ScheduleEntry entry : conflicts) {
            Object[] rowData = {
                entry.getCourse().getCourseName(),
                entry.getRoom().getRoomName(),
                getDayOfWeekDisplay(entry.getDayOfWeek()),
                entry.getStartTime().toString(),
                entry.getEndTime().toString()
            };
            tableModel.addRow(rowData);
        }
        
        conflictTable = new JTable(tableModel);
        closeButton = new JButton("Đóng");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        JLabel messageLabel = new JLabel("Phát hiện xung đột với các mục lịch sau:");
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(messageLabel, BorderLayout.NORTH);
        
        add(new JScrollPane(conflictTable), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void registerEventHandlers() {
        closeButton.addActionListener(e -> dispose());
    }
    
    private String getDayOfWeekDisplay(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            case SUNDAY: return "Chủ nhật";
            default: return "";
        }
    }
}

5. Main Application
MainApp.java


public class MainApp {
    public static void main(String[] args) {
        // Thiết lập Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Khởi tạo và hiển thị giao diện chính
        SwingUtilities.invokeLater(() -> {
            MainScheduleFrame frame = new MainScheduleFrame();
            frame.setVisible(true);
        });
    }
}

Hướng dẫn triển khai
Tạo cơ sở dữ liệu MySQL theo schema đã định nghĩa
Cài đặt các thư viện cần thiết (MySQL JDBC Connector)
Tạo các package và class theo cấu trúc đã nêu
Triển khai các lớp theo thứ tự: Model > Data > Core > UI
Chạy ứng dụng và kiểm tra các chức năng

Các chức năng chính
Xem lịch học tổng thể
Thêm/sửa/xóa các mục lịch
Kiểm tra xung đột lịch tự động
Xem lịch theo phòng học