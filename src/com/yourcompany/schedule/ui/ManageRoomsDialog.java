package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.service.ScheduleService; // Sử dụng ScheduleService

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter; // Thêm nếu muốn sắp xếp
import java.awt.*;
import java.awt.event.ActionEvent;
// import java.sql.SQLException; // Không cần trực tiếp nếu dùng Service
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManageRoomsDialog extends JDialog {
    private ScheduleService scheduleService; // Thay DataManager bằng ScheduleService
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Room> currentRoomsList; // Để lưu danh sách phòng hiện tại

    public ManageRoomsDialog(Frame parent, ScheduleService scheduleService) { // Nhận ScheduleService
        super(parent, "Quản Lý Phòng Học", true);
        this.scheduleService = scheduleService;
        this.currentRoomsList = new ArrayList<>();

        initComponents();
        loadInitialRooms();

        // setSize(600, 400); // pack() sẽ làm điều này tốt hơn
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        // Cột: ID, Tên Phòng, Mô tả (Bỏ Capacity)
        tableModel = new DefaultTableModel(new Object[]{"ID", "Tên Phòng", "Mô tả"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // Bật sắp xếp cột

        // Thiết lập chiều rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Tên Phòng
        table.getColumnModel().getColumn(2).setPreferredWidth(300); // Mô tả

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

        addButton.addActionListener(this::addRoomAction);
        editButton.addActionListener(this::editRoomAction);
        deleteButton.addActionListener(this::deleteRoomAction);
        closeButton.addActionListener(e -> dispose());
    }

    private void loadInitialRooms() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ trên bảng
        currentRoomsList.clear(); // Xóa dữ liệu cũ trong list nội bộ

        List<Room> rooms = scheduleService.getAllRooms(); // Lấy từ service
        if (rooms != null) {
            currentRoomsList.addAll(rooms);
            for (Room r : currentRoomsList) {
                tableModel.addRow(new Object[]{
                        r.getRoomId(),
                        r.getRoomName(),
                        // r.getCapacity(), // ĐÃ LOẠI BỎ
                        r.getDescription()
                });
            }
        } else {
             JOptionPane.showMessageDialog(this, "Không thể tải danh sách phòng học.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRoomAction(ActionEvent e) {
        // Gọi RoomFormDialog tĩnh
        Optional<Room> newRoomOpt = RoomFormDialog.showDialog((Frame) getParent(), null);

        if (newRoomOpt.isPresent()) {
            Room roomFromDialog = newRoomOpt.get(); // Đây là Room mới với dữ liệu từ form
            Optional<Room> addedRoomOpt = scheduleService.addRoom(roomFromDialog); // Gọi service để thêm

            if (addedRoomOpt.isPresent()) {
                JOptionPane.showMessageDialog(this, "Thêm phòng học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialRooms(); // Nạp lại danh sách
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm phòng học. Vui lòng kiểm tra log.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editRoomAction(ActionEvent e) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng để chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < currentRoomsList.size()) {
            Room roomToEdit = currentRoomsList.get(modelRow); // Lấy đối tượng Room gốc

            // Tạo một bản sao để truyền vào dialog, tránh thay đổi trực tiếp đối tượng trong list
            // nếu người dùng hủy dialog. RoomFormDialog sẽ cập nhật bản sao này.
            Room roomCopyForDialog = new Room(roomToEdit.getRoomId(), roomToEdit.getRoomName(), roomToEdit.getDescription());

            Optional<Room> updatedRoomDataOpt = RoomFormDialog.showDialog((Frame) getParent(), roomCopyForDialog);

            if (updatedRoomDataOpt.isPresent()) {
                Room roomWithUpdatesFromDialog = updatedRoomDataOpt.get();
                // Bây giờ cập nhật đối tượng gốc hoặc đối tượng mới (nếu ID khác) thông qua service
                // Service sẽ xử lý việc update Room trong DB
                boolean success = scheduleService.updateRoom(roomWithUpdatesFromDialog);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Cập nhật phòng học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    loadInitialRooms(); // Nạp lại danh sách
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật phòng học. Vui lòng kiểm tra log.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
             JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ trên bảng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRoomAction(ActionEvent e) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng để xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < currentRoomsList.size()) {
            Room roomToDelete = currentRoomsList.get(modelRow);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa phòng: " + roomToDelete.getRoomName() + "?\n" +
                    "(Lưu ý: Các lịch trình trong phòng này cũng có thể bị ảnh hưởng hoặc xóa.)",
                    "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = scheduleService.deleteRoom(roomToDelete.getRoomId());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa phòng học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    loadInitialRooms(); // Nạp lại danh sách
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa phòng học. Vui lòng kiểm tra log.\nCó thể phòng đang được sử dụng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ trên bảng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}