package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter; // Thêm để có thể sắp xếp nếu muốn
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class RoomSelectionDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Room> allRoomsMasterList; // Danh sách gốc, không thay đổi
    private List<Room> currentlyDisplayedRooms; // Danh sách đang hiển thị trên bảng (sau khi lọc)
    private Room selectedRoom;
    private boolean confirmed = false;

    public RoomSelectionDialog(Frame parent, List<Room> rooms) { // Thay JFrame bằng Frame cho tổng quát hơn
        super(parent, "Chọn Phòng Học", true); // Tiêu đề tiếng Việt
        this.allRoomsMasterList = new ArrayList<>(rooms); // Tạo bản sao để tránh thay đổi list gốc từ bên ngoài
        this.currentlyDisplayedRooms = new ArrayList<>(this.allRoomsMasterList);

        initComponents();
        loadRoomsToTable(searchField.getText()); // Load ban đầu
        pack(); // Điều chỉnh kích thước dialog cho vừa với nội dung
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(450, 350); // Kích thước ban đầu hợp lý
        setLayout(new BorderLayout(5, 5));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);


        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchField = new JTextField(25);
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Bảng hiển thị phòng
        // Cột: ID, Tên Phòng, Mô tả (Bỏ Capacity)
        tableModel = new DefaultTableModel(new Object[]{"ID", "Tên Phòng", "Mô tả"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // Cho phép sắp xếp cột

        // Thiết lập chiều rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Tên Phòng
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // Mô tả

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Chọn");
        JButton cancelButton = new JButton("Hủy");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        okButton.addActionListener(e -> confirmSelection());
        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
            dispose(); // Giải phóng tài nguyên dialog
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadRoomsToTable(searchField.getText());
            }
        });

        // Double-click trên dòng để chọn
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirmSelection();
                }
            }
        });
    }

    private void loadRoomsToTable(String filterText) {
        tableModel.setRowCount(0); // Xóa các dòng cũ
        currentlyDisplayedRooms.clear();

        String filter = (filterText != null) ? filterText.trim().toLowerCase() : "";

        if (filter.isEmpty()) {
            currentlyDisplayedRooms.addAll(allRoomsMasterList);
        } else {
            currentlyDisplayedRooms.addAll(
                allRoomsMasterList.stream().filter(room ->
                    (room.getRoomName() != null && room.getRoomName().toLowerCase().contains(filter)) ||
                    (room.getDescription() != null && room.getDescription().toLowerCase().contains(filter))
                ).collect(Collectors.toList())
            );
        }

        for (Room r : currentlyDisplayedRooms) {
            tableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomName(),
                    // r.getCapacity(), // Đã loại bỏ
                    r.getDescription()
            });
        }
    }

    private void confirmSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow >= 0) {
            // Chuyển đổi viewRow sang modelRow nếu có sắp xếp
            int modelRow = table.convertRowIndexToModel(viewRow);
            // Lấy room từ danh sách đang hiển thị (đã được lọc)
            if (modelRow >= 0 && modelRow < currentlyDisplayedRooms.size()) {
                 selectedRoom = currentlyDisplayedRooms.get(modelRow);
                 confirmed = true;
                 setVisible(false);
                 dispose();
            } else {
                 JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Room getSelectedRoom() {
        return selectedRoom;
    }

    /**
     * Phương thức tiện ích để hiển thị dialog và trả về phòng được chọn.
     * @param parent Frame cha.
     * @param rooms Danh sách các phòng để lựa chọn.
     * @return Optional chứa Room được chọn, hoặc Optional.empty() nếu người dùng hủy.
     */
    public static Optional<Room> selectRoom(Frame parent, List<Room> rooms) {
        RoomSelectionDialog dialog = new RoomSelectionDialog(parent, rooms);
        dialog.setVisible(true); // Lệnh này sẽ block cho đến khi dialog đóng
        if (dialog.isConfirmed()) {
            return Optional.ofNullable(dialog.getSelectedRoom());
        }
        return Optional.empty();
    }
}