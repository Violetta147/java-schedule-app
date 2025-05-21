package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.CourseOffering;
import com.yourcompany.schedule.model.Teacher;
import com.yourcompany.schedule.service.ScheduleService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
// import java.util.Vector; // Không cần thiết nếu CourseOfferingFormDialog dùng List

public class ManageCourseOfferingsDialog extends JDialog {
    private ScheduleService scheduleService;
    private JTable offeringTable;
    private DefaultTableModel tableModel;
    private List<CourseOffering> currentOfferingsList;

    // Dữ liệu cho ComboBoxes trong CourseOfferingFormDialog
    private List<Course> availableCourses;
    private List<Teacher> availableTeachers;

    public ManageCourseOfferingsDialog(Frame parent, ScheduleService scheduleService) {
        super(parent, "Quản Lý Phân Công Giảng Dạy", true);
        this.scheduleService = scheduleService;
        this.currentOfferingsList = new ArrayList<>();

        // Tải dữ liệu cần thiết cho form dialog con
        this.availableCourses = scheduleService.getAllCourses();
        this.availableTeachers = scheduleService.getAllTeachers();

        initComponents();
        loadInitialOfferings();

        pack();
        setMinimumSize(new Dimension(750, 450)); // Điều chỉnh nếu cần
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(new Object[]{"ID Phân Công", "Mã MH", "Tên Môn Học", "ID Giáo Viên", "Tên Giáo Viên"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        offeringTable = new JTable(tableModel);
        offeringTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        offeringTable.setAutoCreateRowSorter(true);

        // Thiết lập chiều rộng cột
        offeringTable.getColumnModel().getColumn(0).setPreferredWidth(100); // ID Phân Công
        offeringTable.getColumnModel().getColumn(0).setMaxWidth(120);
        offeringTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Mã MH
        offeringTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Tên Môn Học
        offeringTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // ID Giáo Viên
        offeringTable.getColumnModel().getColumn(3).setMaxWidth(100);
        offeringTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Tên Giáo Viên

        add(new JScrollPane(offeringTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Thêm Mới Phân Công");
        // Chỉnh sửa CourseOffering thường có nghĩa là chọn lại Course hoặc Teacher.
        // Việc này có thể coi như xóa cái cũ và thêm cái mới,
        // hoặc nếu DB cho phép update course_id, teacher_id thì có thể làm nút Edit.
        // Hiện tại, tôi sẽ bỏ qua nút Edit để đơn giản.
        // JButton editButton = new JButton("Chỉnh Sửa");
        JButton deleteButton = new JButton("Xóa Phân Công");
        JButton closeButton = new JButton("Đóng");

        buttonPanel.add(addButton);
        // buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(this::addOfferingAction);
        // editButton.addActionListener(this::editOfferingAction);
        deleteButton.addActionListener(this::deleteOfferingAction);
        closeButton.addActionListener(e -> dispose());
    }

    private void loadInitialOfferings() {
        tableModel.setRowCount(0);
        currentOfferingsList.clear();
        List<CourseOffering> offerings = scheduleService.getAllCourseOfferings();
        if (offerings != null) {
            currentOfferingsList.addAll(offerings);
            for (CourseOffering co : currentOfferingsList) {
                String courseCode = "N/A";
                String courseName = "N/A";
                if (co.getCourse() != null) {
                    courseCode = co.getCourse().getCourseCode();
                    courseName = co.getCourse().getCourseName();
                }

                String teacherIdStr = "N/A";
                String teacherName = "N/A";
                if (co.getTeacher() != null) {
                    teacherIdStr = String.valueOf(co.getTeacher().getTeacherId());
                    teacherName = co.getTeacher().getName();
                }

                tableModel.addRow(new Object[]{
                        co.getOfferingId(),
                        courseCode,
                        courseName,
                        teacherIdStr,
                        teacherName
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách phân công giảng dạy.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addOfferingAction(ActionEvent e) {
        if (availableCourses.isEmpty() || availableTeachers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Cần có ít nhất một Môn học và một Giáo viên trong hệ thống để tạo Phân công.",
                "Thiếu Dữ Liệu Nền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Optional<CourseOffering> offeringDataOpt = CourseOfferingFormDialog.showDialog(
                (Frame) getParent(), null, availableCourses, availableTeachers
        );

        if (offeringDataOpt.isPresent()) {
            CourseOffering newOffering = offeringDataOpt.get();
            // CourseOfferingFormDialog đã tạo đối tượng newOffering với Course và Teacher đã chọn.
            // Service sẽ xử lý việc chèn và lấy ID cho offeringId.
            Optional<CourseOffering> addedOpt = scheduleService.addCourseOffering(newOffering);
            if (addedOpt.isPresent()) {
                JOptionPane.showMessageDialog(this, "Thêm phân công giảng dạy thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialOfferings(); // Nạp lại danh sách
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm phân công.\nCó thể phân công này (Môn học - Giáo viên) đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Phương thức chỉnh sửa có thể được thêm nếu bạn quyết định hỗ trợ nó
    // Nó sẽ tương tự như addOfferingAction nhưng truyền offeringToEdit vào CourseOfferingFormDialog
    // và gọi scheduleService.updateCourseOffering()
    /*
    private void editOfferingAction(ActionEvent e) {
        int viewRow = offeringTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phân công để chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = offeringTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentOfferingsList.size()){
            JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CourseOffering offeringToEdit = currentOfferingsList.get(modelRow);

        Optional<CourseOffering> updatedDataOpt = CourseOfferingFormDialog.showDialog(
                (Frame) getParent(), offeringToEdit, availableCourses, availableTeachers
        );

        if (updatedDataOpt.isPresent()) {
            CourseOffering updatedOffering = updatedDataOpt.get();
            boolean success = scheduleService.updateCourseOffering(updatedOffering);
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật phân công thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialOfferings();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật phân công.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    */

    private void deleteOfferingAction(ActionEvent e) {
        int viewRow = offeringTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phân công để xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = offeringTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentOfferingsList.size()){
            JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
           return;
       }
        CourseOffering offeringToDelete = currentOfferingsList.get(modelRow);

        String courseInfo = (offeringToDelete.getCourse() != null) ?
                            offeringToDelete.getCourse().getCourseName() : "N/A";
        String teacherInfo = (offeringToDelete.getTeacher() != null) ?
                             offeringToDelete.getTeacher().getName() : "N/A";

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Bạn có chắc chắn muốn xóa phân công:\nMôn học: %s\nGiáo viên: %s?\n" +
                              "(Lưu ý: Tất cả các lịch học liên quan đến phân công này cũng sẽ bị xóa.)",
                              courseInfo, teacherInfo),
                "Xác Nhận Xóa Phân Công", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = scheduleService.deleteCourseOffering(offeringToDelete.getOfferingId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa phân công thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialOfferings();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa phân công. Vui lòng kiểm tra log.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}