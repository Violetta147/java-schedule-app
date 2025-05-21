package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.*; // Import tất cả model

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime; // Cần cho việc hiển thị
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException; // Thêm import này

public class SchedulePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField filterField;
    private JComboBox<String> filterColumnComboBox;
    private JCheckBox weekFilterCheckBox;
    private LocalDate currentWeekStart;
    // Định dạng ngày giờ cho hiển thị trong bảng
    private static final DateTimeFormatter DISPLAY_DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // Danh sách này lưu trữ các ScheduleEntry gốc, tương ứng với các dòng trong tableModel
    // QUAN TRỌNG: Cần đảm bảo danh sách này và tableModel luôn đồng bộ về thứ tự và số lượng
    // nếu có các thao tác phức tạp hơn việc chỉ thêm và xóa toàn bộ.
    private List<ScheduleEntry> displayedEntriesOriginalOrder = new ArrayList<>();

    public SchedulePanel() {
        setLayout(new BorderLayout(5, 5)); // Thêm khoảng cách nhỏ
        // Các cột: Course Code, Course Name, Teacher, Class, Room, Start Time, End Time
        tableModel = new DefaultTableModel(
                new Object[]{"Mã MH", "Tên Môn Học", "Giáo Viên", "Lớp", "Phòng", "Bắt Đầu", "Kết Thúc"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép chỉnh sửa trực tiếp trên bảng
            }
        };

        sorter = new TableRowSorter<>(tableModel);
        table = new JTable(tableModel);
        table.setRowSorter(sorter);
        // table.setAutoCreateRowSorter(false); // Không cần thiết nếu đã setRowSorter
        table.setFillsViewportHeight(true); // Bảng chiếm toàn bộ chiều cao của scroll pane
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 dòng

        // Đặt chiều rộng cột ưu tiên
        // "Mã MH", "Tên Môn Học", "Giáo Viên", "Lớp", "Phòng", "Bắt Đầu", "Kết Thúc"
        int[] widths = {80, 200, 150, 80, 80, 140, 140};
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Cho phép thanh cuộn ngang


        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Panel bộ lọc
        JPanel filterPanelContainer = new JPanel(new BorderLayout());
        filterPanelContainer.setBorder(BorderFactory.createTitledBorder("Lọc và Tìm kiếm"));

        JPanel topFilterControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        weekFilterCheckBox = new JCheckBox("Chỉ tuần hiện tại");
        weekFilterCheckBox.addActionListener(e -> applyFilter());
        topFilterControls.add(weekFilterCheckBox);

        JPanel bottomFilterControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomFilterControls.add(new JLabel("Lọc theo cột:"));
        filterColumnComboBox = new JComboBox<>(new String[]{"Tất cả các cột", "Mã MH", "Tên Môn Học", "Giáo Viên", "Lớp", "Phòng", "Ngày (dd/MM/yyyy)"});
        filterColumnComboBox.addActionListener(e -> applyFilter());
        bottomFilterControls.add(filterColumnComboBox);

        bottomFilterControls.add(new JLabel("Tìm kiếm:"));
        filterField = new JTextField(25);
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        bottomFilterControls.add(filterField);

        JButton clearFilterButton = new JButton("Xóa lọc");
        clearFilterButton.addActionListener(e -> {
            filterField.setText("");
            filterColumnComboBox.setSelectedIndex(0); // Đặt lại về "Tất cả các cột"
            weekFilterCheckBox.setSelected(false); // Bỏ chọn lọc tuần
            applyFilter(); // Áp dụng lại để xóa bộ lọc
        });
        bottomFilterControls.add(clearFilterButton);
        
        filterPanelContainer.add(topFilterControls, BorderLayout.NORTH);
        filterPanelContainer.add(bottomFilterControls, BorderLayout.CENTER);

        add(filterPanelContainer, BorderLayout.NORTH);

        setCurrentWeek(LocalDate.now()); // Khởi tạo tuần hiện tại
    }

    public void setCurrentWeek(LocalDate dateInWeek) {
        this.currentWeekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (weekFilterCheckBox.isSelected()) {
            applyFilter();
        }
        // Cập nhật label hiển thị tuần (nếu có) ở đây
    }
    
    public LocalDate getCurrentWeekStart() {
        return currentWeekStart;
    }


    private void applyFilter() {
        String text = filterField.getText();
        int selectedColumnIndexInView = filterColumnComboBox.getSelectedIndex(); // Index trong ComboBox
        boolean weekFilterEnabled = weekFilterCheckBox.isSelected();
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Text filter
        if (text != null && !text.trim().isEmpty()) {
            String regexText;
            try {
                // (?i) for case-insensitive. Pattern.quote để xử lý các ký tự đặc biệt trong text.
                regexText = "(?i)" + Pattern.quote(text.trim());
            } catch (PatternSyntaxException e) {
                // Nếu người dùng nhập một regex không hợp lệ, không lọc gì cả hoặc báo lỗi
                System.err.println("Invalid regex pattern in filter: " + text.trim());
                sorter.setRowFilter(null); // Hoặc chỉ xóa text filter
                // return; // Hoặc tiếp tục với các filter khác nếu có
                regexText = "(?i)" + Pattern.quote(text.trim().replaceAll("[\\\\\\[\\](){}.*+?^$|]", "")); // Cố gắng escape cơ bản
            }


            if (selectedColumnIndexInView == 0) { // "Tất cả các cột"
                List<RowFilter<Object,Object>> orFilters = new ArrayList<>();
                for(int i=0; i < tableModel.getColumnCount(); i++) {
                    // Chỉ thêm filter nếu cột đó có thể chứa text cần tìm
                     orFilters.add(RowFilter.regexFilter(regexText, i));
                }
                 if (!orFilters.isEmpty()) {
                    filters.add(RowFilter.orFilter(orFilters));
                }
            } else if (selectedColumnIndexInView == 6) { // Cột "Ngày (dd/MM/yyyy)"
                // Lọc dựa trên cột "Bắt Đầu" (index 5 trong model) vì nó chứa ngày
                // Người dùng có thể nhập "25/12" hoặc "25/12/2023"
                filters.add(RowFilter.regexFilter(regexText, 5)); 
            }
            else { // Một cột cụ thể (Mã MH, Tên MH, GV, Lớp, Phòng)
                // Index trong tableModel = selectedColumnIndexInView - 1
                filters.add(RowFilter.regexFilter(regexText, selectedColumnIndexInView - 1));
            }
        }

        // Week filter
        if (weekFilterEnabled && currentWeekStart != null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    int modelRow = (Integer) entry.getIdentifier();
                    if (modelRow >= displayedEntriesOriginalOrder.size() || modelRow < 0) return false;

                    ScheduleEntry scheduleEntry = displayedEntriesOriginalOrder.get(modelRow);
                    LocalDate entryDate = scheduleEntry.getDate(); 

                    if (entryDate == null) return false;
                    
                    LocalDate weekEnd = currentWeekStart.plusDays(6);
                    return !entryDate.isBefore(currentWeekStart) && !entryDate.isAfter(weekEnd);
                }
            });
        }
        
        try {
            if (filters.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.andFilter(filters));
            }
        } catch (PatternSyntaxException e) {
            System.err.println("Error applying row filter: " + e.getMessage());
            sorter.setRowFilter(null); // Reset filter nếu có lỗi regex nghiêm trọng
        }
    }

    /**
     * Thêm một dòng ScheduleEntry vào bảng và vào danh sách lưu trữ nội bộ.
     */
    private void addRowToTableAndLocalList(ScheduleEntry entry) {
        if (entry == null) return;

        Course course = entry.getCourse();
        CourseOffering offering = entry.getCourseOffering();
        Teacher teacher = (offering != null) ? offering.getTeacher() : null;
        SchoolClass schoolClass = entry.getSchoolClass();
        Room room = entry.getRoom();
        LocalDateTime startDateTime = entry.getStartDateTime();
        LocalDateTime endDateTime = entry.getEndDateTime();

        String courseCodeStr = (course != null) ? course.getCourseCode() : "N/A";
        String courseNameStr = (course != null) ? course.getCourseName() : "N/A";
        String teacherNameStr = (teacher != null) ? teacher.getName() : "N/A";
        String classNameStr = (schoolClass != null) ? schoolClass.getName() : "N/A";
        String roomNameStr = (room != null) ? room.getRoomName() : "N/A";
        String startStr = (startDateTime != null) ? startDateTime.format(DISPLAY_DTF) : "N/A";
        String endStr = (endDateTime != null) ? endDateTime.format(DISPLAY_DTF) : "N/A";

        tableModel.addRow(new Object[]{
                courseCodeStr, courseNameStr, teacherNameStr,
                classNameStr, roomNameStr, startStr, endStr
        });
        displayedEntriesOriginalOrder.add(entry);
    }
    
    /**
     * Nạp lại toàn bộ dữ liệu cho bảng.
     * Phương thức này sẽ được gọi từ bên ngoài (ví dụ: MainFrame sau khi lấy dữ liệu từ ScheduleService).
     */
    public void setScheduleEntries(List<ScheduleEntry> entries) {
        clearTableData(); // Xóa dữ liệu cũ
        if (entries != null) {
            for (ScheduleEntry entry : entries) {
                // Gọi phương thức private để thêm vào cả tableModel và displayedEntriesOriginalOrder
                addRowToTableAndLocalList(entry);
            }
        }
        // Không gọi applyFilter() ở đây nữa, vì dữ liệu mới đã được load.
        // Bộ lọc sẽ được áp dụng khi người dùng tương tác với các control lọc.
        // Hoặc nếu muốn tự động áp dụng filter hiện tại:
        applyFilter(); 
    }


    /**
     * Lấy ScheduleEntry tương ứng với dòng đang được chọn trên bảng.
     * @return ScheduleEntry nếu có dòng được chọn và hợp lệ, ngược lại trả về null.
     */
    public ScheduleEntry getSelectedEntry() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null; // Không có dòng nào được chọn
        }
        
        try {
            // Chuyển đổi view row index sang model row index (quan trọng khi có sắp xếp/lọc)
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow >= 0 && modelRow < displayedEntriesOriginalOrder.size()) {
                return displayedEntriesOriginalOrder.get(modelRow);
            }
        } catch (IndexOutOfBoundsException e) {
            // Có thể xảy ra nếu model thay đổi bất thường trong khi view chưa cập nhật
            System.err.println("Error getting selected entry: Model-View index out of sync. " + e.getMessage());
        }
        return null;
    }

    /**
     * Xóa tất cả dữ liệu khỏi bảng và danh sách lưu trữ nội bộ.
     */
    public void clearTableData() {
        // Cẩn thận khi xóa các dòng nếu có listener nào đó đang hoạt động
        // Ngăn listener của sorter kích hoạt không cần thiết khi xóa nhiều dòng
        sorter.setRowFilter(null); // Tạm thời bỏ filter để tableModel.setRowCount(0) không gây lỗi index
        tableModel.setRowCount(0);
        displayedEntriesOriginalOrder.clear();
        // Áp dụng lại filter (thường là null filter sau khi clear)
        // applyFilter(); // Không cần thiết nếu đã setRowCount(0) và clear list
    }

    /**
     * Lấy ID của ScheduleEntry đang được chọn.
     * @return ID của entry được chọn, hoặc -1 nếu không có gì được chọn hoặc entry không hợp lệ.
     */
    public int getSelectedEntryId() {
        ScheduleEntry selected = getSelectedEntry();
        return (selected != null) ? selected.getEntryId() : -1;
    }

    /**
     * Làm mới dữ liệu bảng với danh sách ScheduleEntry mới.
     * Đây là phương thức chính để cập nhật dữ liệu cho panel này từ bên ngoài.
     * @param newEntries Danh sách các ScheduleEntry mới để hiển thị.
     */
    public void refreshTableData(List<ScheduleEntry> newEntries) {
        setScheduleEntries(newEntries);
    }
}