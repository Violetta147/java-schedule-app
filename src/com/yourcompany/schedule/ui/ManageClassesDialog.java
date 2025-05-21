package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.SchoolClass;
import com.yourcompany.schedule.service.ScheduleService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManageClassesDialog extends JDialog {
    private ScheduleService scheduleService;
    private JTable classTable;
    private DefaultTableModel tableModel;
    private List<SchoolClass> currentClassesList;

    public ManageClassesDialog(Frame parent, ScheduleService scheduleService) {
        super(parent, "Quản Lý Lớp Học", true);
        this.scheduleService = scheduleService;
        this.currentClassesList = new ArrayList<>();

        initComponents();
        loadInitialClasses();

        pack();
        setMinimumSize(new Dimension(550, 400)); // Điều chỉnh kích thước nếu cần
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(new Object[]{"ID", "Tên Lớp", "Khối", "Ban/Số TT"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        classTable = new JTable(tableModel);
        classTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classTable.setAutoCreateRowSorter(true);

        classTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        classTable.getColumnModel().getColumn(0).setMaxWidth(70);
        classTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Tên lớp
        classTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Khối
        classTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Ban

        add(new JScrollPane(classTable), BorderLayout.CENTER);

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

        addButton.addActionListener(this::addClassAction);
        editButton.addActionListener(this::editClassAction);
        deleteButton.addActionListener(this::deleteClassAction);
        closeButton.addActionListener(e -> dispose());
    }

    private void loadInitialClasses() {
        tableModel.setRowCount(0);
        currentClassesList.clear();
        List<SchoolClass> classes = scheduleService.getAllSchoolClasses();
        if (classes != null) {
            currentClassesList.addAll(classes);
            for (SchoolClass sc : currentClassesList) {
                tableModel.addRow(new Object[]{
                        sc.getClassId(),
                        sc.getName(), // name được suy ra từ grade và section
                        sc.getGrade(),
                        sc.getSection()
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách lớp học.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addClassAction(ActionEvent e) {
        Optional<SchoolClass> classDataOpt = SchoolClassFormDialog.showDialog((Frame) getParent(), null);
        if (classDataOpt.isPresent()) {
            SchoolClass newClass = classDataOpt.get();
            Optional<SchoolClass> addedClassOpt = scheduleService.addSchoolClass(newClass);
            if (addedClassOpt.isPresent()) {
                JOptionPane.showMessageDialog(this, "Thêm lớp học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialClasses();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm lớp học.\nCó thể lớp với Khối và Ban/Số Thứ Tự này đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editClassAction(ActionEvent e) {
        int viewRow = classTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lớp học để chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = classTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentClassesList.size()){ // Kiểm tra modelRow hợp lệ
             JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ trên bảng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SchoolClass classToEdit = currentClassesList.get(modelRow);

        // Tạo bản sao để truyền vào dialog, tránh thay đổi trực tiếp nếu người dùng hủy
        SchoolClass classCopy = new SchoolClass(classToEdit.getClassId(), classToEdit.getGrade(), classToEdit.getSection());
        // classCopy.setName(classToEdit.getName()); // Không cần thiết vì name được suy ra

        Optional<SchoolClass> updatedDataOpt = SchoolClassFormDialog.showDialog((Frame) getParent(), classCopy);
        if (updatedDataOpt.isPresent()) {
            SchoolClass updatedClass = updatedDataOpt.get(); // Đây là classCopy đã được cập nhật
            boolean success = scheduleService.updateSchoolClass(updatedClass);
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật lớp học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialClasses();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật lớp học.\nCó thể lớp với Khối và Ban/Số Thứ Tự mới đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteClassAction(ActionEvent e) {
        int viewRow = classTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lớp học để xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = classTable.convertRowIndexToModel(viewRow);
         if (modelRow < 0 || modelRow >= currentClassesList.size()){
             JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ trên bảng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SchoolClass classToDelete = currentClassesList.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa lớp: " + classToDelete.getName() + "?\n" +
                "(Lưu ý: Giáo viên chủ nhiệm của lớp này (nếu có) sẽ không còn chủ nhiệm lớp này nữa.\n" +
                "Tất cả các lịch học của lớp này cũng sẽ bị xóa.)",
                "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = scheduleService.deleteSchoolClass(classToDelete.getClassId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa lớp học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialClasses();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa lớp học. Vui lòng kiểm tra log.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}