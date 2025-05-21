package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.AcaYear;
import com.yourcompany.schedule.service.ScheduleService;
import com.github.lgooddatepicker.components.DatePicker; // Cần cho form

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManageAcaYearsDialog extends JDialog {
    private ScheduleService scheduleService;
    private JTable acaYearTable;
    private DefaultTableModel tableModel;
    private List<AcaYear> currentAcaYearsList;

    public ManageAcaYearsDialog(Frame parent, ScheduleService scheduleService) {
        super(parent, "Quản Lý Năm Học", true);
        this.scheduleService = scheduleService;
        this.currentAcaYearsList = new ArrayList<>();

        initComponents();
        loadInitialAcaYears();

        pack();
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(new Object[]{"ID", "Tên Năm Học", "Ngày Bắt Đầu", "Số Tuần"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        acaYearTable = new JTable(tableModel);
        acaYearTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        acaYearTable.setAutoCreateRowSorter(true);

        acaYearTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        acaYearTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        acaYearTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        acaYearTable.getColumnModel().getColumn(3).setPreferredWidth(80);

        add(new JScrollPane(acaYearTable), BorderLayout.CENTER);

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

        addButton.addActionListener(this::addAcaYearAction);
        editButton.addActionListener(this::editAcaYearAction);
        deleteButton.addActionListener(this::deleteAcaYearAction);
        closeButton.addActionListener(e -> dispose());
    }

    private void loadInitialAcaYears() {
        tableModel.setRowCount(0);
        currentAcaYearsList.clear();
        List<AcaYear> acaYears = scheduleService.getAllAcaYears();
        if (acaYears != null) {
            currentAcaYearsList.addAll(acaYears);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (AcaYear ay : currentAcaYearsList) {
                tableModel.addRow(new Object[]{
                        ay.getYearId(),
                        ay.getYearName(),
                        ay.getStartDate() != null ? ay.getStartDate().format(dateFormatter) : "N/A",
                        ay.getWeeks()
                });
            }
        }
    }

    private void addAcaYearAction(ActionEvent e) {
        // Sử dụng một dialog form đơn giản (AcaYearFormDialog) để nhập liệu
        Optional<AcaYear> acaYearDataOpt = AcaYearFormDialog.showDialog((Frame) getParent(), null);
        if (acaYearDataOpt.isPresent()) {
            AcaYear newAcaYear = acaYearDataOpt.get();
            Optional<AcaYear> addedAcaYearOpt = scheduleService.addAcaYear(newAcaYear);
            if (addedAcaYearOpt.isPresent()) {
                JOptionPane.showMessageDialog(this, "Thêm năm học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialAcaYears();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm năm học (có thể tên năm học đã tồn tại).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editAcaYearAction(ActionEvent e) {
        int viewRow = acaYearTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một năm học để chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = acaYearTable.convertRowIndexToModel(viewRow);
        AcaYear acaYearToEdit = currentAcaYearsList.get(modelRow);

        // Tạo bản sao để chỉnh sửa, tránh thay đổi trực tiếp nếu người dùng hủy
        AcaYear acaYearCopy = new AcaYear(acaYearToEdit.getYearId(), acaYearToEdit.getYearName(),
                                        acaYearToEdit.getStartDate(), acaYearToEdit.getWeeks());

        Optional<AcaYear> updatedAcaYearDataOpt = AcaYearFormDialog.showDialog((Frame) getParent(), acaYearCopy);
        if (updatedAcaYearDataOpt.isPresent()) {
            AcaYear updatedAcaYear = updatedAcaYearDataOpt.get(); // Đây là bản sao đã được cập nhật
            boolean success = scheduleService.updateAcaYear(updatedAcaYear);
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật năm học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialAcaYears();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật năm học.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAcaYearAction(ActionEvent e) {
        int viewRow = acaYearTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một năm học để xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = acaYearTable.convertRowIndexToModel(viewRow);
        AcaYear acaYearToDelete = currentAcaYearsList.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa năm học: " + acaYearToDelete.getYearName() + "?\n" +
                "(Lưu ý: Các lịch học liên quan đến năm học này cũng sẽ bị xóa.)",
                "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = scheduleService.deleteAcaYear(acaYearToDelete.getYearId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa năm học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialAcaYears();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa năm học.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}