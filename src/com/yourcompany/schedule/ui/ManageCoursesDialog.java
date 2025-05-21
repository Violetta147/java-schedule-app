package com.yourcompany.schedule.ui;

// import com.yourcompany.schedule.data.DataManager; // Sẽ không dùng trực tiếp nữa
import com.yourcompany.schedule.model.Course;
// import com.yourcompany.schedule.model.Teacher; // Không cần cho quản lý Course cơ bản
// import com.yourcompany.schedule.model.SchoolClass; // Không cần cho quản lý Course cơ bản
import com.yourcompany.schedule.service.ScheduleService; // Sử dụng ScheduleService

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
// import java.sql.SQLException; // Không cần trực tiếp nếu dùng Service
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManageCoursesDialog extends JDialog {
    private ScheduleService scheduleService; // Sử dụng ScheduleService
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Course> currentCoursesList; // Để lưu danh sách môn học hiện tại

    public ManageCoursesDialog(Frame parent, ScheduleService scheduleService) { // Nhận ScheduleService
        super(parent, "Quản Lý Môn Học", true);
        this.scheduleService = scheduleService;
        this.currentCoursesList = new ArrayList<>();

        initComponents();
        loadInitialCourses();

        pack(); // Tự động điều chỉnh kích thước
        setMinimumSize(new Dimension(500, 300)); // Đặt kích thước tối thiểu
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Cột: ID, Mã Môn Học, Tên Môn Học
        // Bỏ Teacher, Class, Credits
        tableModel = new DefaultTableModel(new Object[]{"ID", "Mã MH", "Tên Môn Học"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // Bật sắp xếp

        // Thiết lập chiều rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(0).setMaxWidth(80); // Giới hạn chiều rộng ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120);  // Mã MH
        table.getColumnModel().getColumn(2).setPreferredWidth(300);  // Tên Môn Học
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN); // Cho cột cuối mở rộng

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Thêm Mới");
        JButton editButton = new JButton("Chỉnh Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton closeButton = new JButton("Đóng");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(this::addCourseAction);
        editButton.addActionListener(this::editCourseAction);
        deleteButton.addActionListener(this::deleteCourseAction);
        closeButton.addActionListener(e -> dispose());
    }

    private void loadInitialCourses() {
        tableModel.setRowCount(0);
        currentCoursesList.clear();

        List<Course> courses = scheduleService.getAllCourses(); // Lấy từ service
        if (courses != null) {
            currentCoursesList.addAll(courses);
            for (Course c : currentCoursesList) {
                tableModel.addRow(new Object[]{
                    c.getCourseId(),
                    c.getCourseCode(),
                    c.getCourseName()
                    // Không còn teacherName, className, credits
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách môn học.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCourseAction(ActionEvent e) {
        // CourseFormDialog giờ đây chỉ cần Frame cha và một đối tượng Course (null nếu thêm mới)
        // Nó không cần List<Teacher> hay List<SchoolClass> nữa.
        // Bạn cần sửa CourseFormDialog.java cho phù hợp.
        Optional<Course> courseFromDialogOpt = CourseFormDialog.showDialog((Frame) getParent(), null);

        if (courseFromDialogOpt.isPresent()) {
            Course courseToAdd = courseFromDialogOpt.get();
            Optional<Course> addedCourseOpt = scheduleService.addCourse(courseToAdd);

            if (addedCourseOpt.isPresent()) {
                JOptionPane.showMessageDialog(this, "Thêm môn học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialCourses();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm môn học. Vui lòng kiểm tra log.\n" +
                                                "Có thể mã môn học đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCourseAction(ActionEvent e) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một môn học để chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < currentCoursesList.size()) {
            Course courseToEdit = currentCoursesList.get(modelRow); // Lấy đối tượng Course gốc

            // Tạo bản sao để truyền vào dialog
            Course courseCopyForDialog = new Course(courseToEdit.getCourseId(),
                                                    courseToEdit.getCourseCode(),
                                                    courseToEdit.getCourseName());

            Optional<Course> updatedCourseDataOpt = CourseFormDialog.showDialog((Frame) getParent(), courseCopyForDialog);

            if (updatedCourseDataOpt.isPresent()) {
                Course courseWithUpdatesFromDialog = updatedCourseDataOpt.get();
                boolean success = scheduleService.updateCourse(courseWithUpdatesFromDialog);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Cập nhật môn học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    loadInitialCourses();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật môn học. Vui lòng kiểm tra log.\n" +
                                                    "Có thể mã môn học mới bị trùng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ trên bảng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCourseAction(ActionEvent e) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một môn học để xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < currentCoursesList.size()) {
            Course courseToDelete = currentCoursesList.get(modelRow);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa môn học: " + courseToDelete.getCourseName() + " (" + courseToDelete.getCourseCode() + ")?\n" +
                    "(Lưu ý: Tất cả các phân công giảng dạy và lịch học liên quan đến môn này cũng sẽ bị xóa.)",
                    "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = scheduleService.deleteCourse(courseToDelete.getCourseId());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa môn học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    loadInitialCourses();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa môn học. Vui lòng kiểm tra log.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ trên bảng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}