package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Room;

import javax.swing.*;
import java.awt.*;
// import java.awt.event.ActionEvent; // Không cần thiết nếu dùng lambda
// import java.awt.event.ActionListener; // Không cần thiết nếu dùng lambda

public class RoomFormDialog extends JDialog {
    private JTextField nameField;
    // private JTextField capacityField; // ĐÃ LOẠI BỎ
    private JTextField descriptionField;
    private boolean confirmed = false;
    private Room roomToEdit; // Đổi tên để rõ ràng hơn khi edit

    public RoomFormDialog(Frame parent, Room room) { // Thay JFrame bằng Frame
        super(parent, room == null ? "Thêm Phòng Học" : "Chỉnh Sửa Phòng Học", true);
        this.roomToEdit = room;

        initComponents();
        populateFieldsIfEditing();

        pack(); // Điều chỉnh kích thước dialog cho vừa vặn
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        // Sử dụng GridBagLayout để linh hoạt hơn một chút
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Khoảng cách giữa các component
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Room Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Tên phòng:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Cho phép trường text mở rộng
        gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(20); // Kích thước gợi ý
        add(nameField, gbc);
        gbc.weightx = 0; // Reset weightx

        // Description
        gbc.gridx = 0;
        gbc.gridy = 1; // Dòng tiếp theo
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Mô tả:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        descriptionField = new JTextField(20);
        add(descriptionField, gbc);
        gbc.weightx = 0;

        // Capacity field đã được loại bỏ

        // Panel nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Lưu");
        JButton cancelButton = new JButton("Hủy");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 2; // Dòng tiếp theo cho nút bấm
        gbc.gridwidth = 2; // Nút bấm chiếm 2 cột
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // Không cho panel nút mở rộng
        add(buttonPanel, gbc);


        okButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                setVisible(false);
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
            dispose();
        });
    }

    private void populateFieldsIfEditing() {
        if (roomToEdit != null) {
            nameField.setText(roomToEdit.getRoomName());
            // capacityField.setText(String.valueOf(roomToEdit.getCapacity())); // ĐÃ LOẠI BỎ
            descriptionField.setText(roomToEdit.getDescription());
        }
    }

    private boolean validateInput() {
        String roomName = nameField.getText().trim();
        if (roomName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên phòng không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocusInWindow();
            return false;
        }
        // Không còn validate capacity
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Trả về đối tượng Room với dữ liệu từ form.
     * Nếu là form edit, nó sẽ cập nhật roomToEdit.
     * Nếu là form add, nó sẽ tạo một Room mới.
     * @return Room object, hoặc null nếu không confirmed.
     */
    public Room getRoomData() {
        if (!confirmed) {
            return null; // Hoặc ném exception tùy theo thiết kế
        }

        Room resultRoom = (roomToEdit == null) ? new Room() : roomToEdit;
        
        resultRoom.setRoomName(nameField.getText().trim());
        // resultRoom.setCapacity(Integer.parseInt(capacityField.getText().trim())); // ĐÃ LOẠI BỎ
        resultRoom.setDescription(descriptionField.getText().trim());

        // Nếu là thêm mới và roomToEdit ban đầu là null,
        // ID của resultRoom sẽ là 0 (hoặc giá trị mặc định).
        // DataManager sẽ xử lý việc gán ID từ DB sau khi thêm.
        // Nếu là edit, resultRoom (chính là roomToEdit) đã có ID.
        return resultRoom;
    }

     /**
     * Phương thức tiện ích để hiển thị dialog và trả về phòng đã được tạo/chỉnh sửa.
     * @param parent Frame cha.
     * @param roomToEdit Room cần chỉnh sửa, hoặc null nếu muốn thêm mới.
     * @return Optional chứa Room đã tạo/chỉnh sửa, hoặc Optional.empty() nếu người dùng hủy.
     */
    public static java.util.Optional<Room> showDialog(Frame parent, Room roomToEdit) {
        RoomFormDialog dialog = new RoomFormDialog(parent, roomToEdit);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            return java.util.Optional.ofNullable(dialog.getRoomData());
        }
        return java.util.Optional.empty();
    }
}