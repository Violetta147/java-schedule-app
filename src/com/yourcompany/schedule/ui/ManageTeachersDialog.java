package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.SchoolClass; // Cần nếu quản lý lớp chủ nhiệm
import com.yourcompany.schedule.model.Teacher;
import com.yourcompany.schedule.service.ScheduleService; // Sử dụng ScheduleService

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter; // Thêm để sắp xếp
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
// import java.sql.SQLException; // Không cần trực tiếp nếu dùng Service
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Sử dụng Optional

public class ManageTeachersDialog extends JDialog {
    private JTable teacherTable;
    private DefaultTableModel tableModel;
    private ScheduleService scheduleService; // Thay DataManager bằng ScheduleService
    private List<Teacher> currentTeachersList; // Danh sách giáo viên đang hiển thị trên bảng
    private JTextField nameField, emailField, phoneField;
    private JComboBox<SchoolClass> classComboBox; // TÙY CHỌN: Cho lớp chủ nhiệm
    private JButton addButton, updateButton, deleteButton, clearButton, closeButton;
    // private int selectedModelRow = -1; // Không cần nếu lấy ID từ bảng

    public ManageTeachersDialog(Frame parent, ScheduleService scheduleService) { // Nhận ScheduleService
        super(parent, "Quản Lý Giáo Viên", true);
        this.scheduleService = scheduleService;
        this.currentTeachersList = new ArrayList<>();

        initComponents();
        loadInitialData();

        pack(); // Điều chỉnh kích thước
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table Panel
        // Cột: ID, Tên, Email, Điện thoại, (Tùy chọn: Lớp CN)
        String[] columns = {"ID", "Tên Giáo Viên", "Email", "Điện Thoại" /*, "Lớp Chủ Nhiệm" */};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        teacherTable = new JTable(tableModel);
        teacherTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        teacherTable.setAutoCreateRowSorter(true); // Bật sắp xếp cột
        // Thiết lập chiều rộng cột
        teacherTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        teacherTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Tên
        teacherTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Email
        teacherTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Điện thoại
        if (columns.length > 4) teacherTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Lớp CN


        JScrollPane scrollPane = new JScrollPane(teacherTable);
        add(scrollPane, BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Tên:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(20); formPanel.add(nameField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;


        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        emailField = new JTextField(20); formPanel.add(emailField, gbc);
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Điện thoại:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        phoneField = new JTextField(20); formPanel.add(phoneField, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // TÙY CHỌN: Thêm ComboBox cho Lớp chủ nhiệm
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Lớp CN:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        classComboBox = new JComboBox<>(); loadClassComboBox(); // Cần load danh sách lớp
        formPanel.add(classComboBox, gbc);
        gbc.fill = GridBagConstraints.NONE;

        add(formPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButton = new JButton("Thêm");
        updateButton = new JButton("Cập Nhật");
        deleteButton = new JButton("Xóa");
        clearButton = new JButton("Làm Mới Form");
        closeButton = new JButton("Đóng");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event Listeners
        teacherTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                populateFormFromSelectedRow();
            }
        });

        addButton.addActionListener(this::addTeacherAction);
        updateButton.addActionListener(this::updateTeacherAction);
        deleteButton.addActionListener(this::deleteTeacherAction);
        clearButton.addActionListener(e -> clearFormFields());
        closeButton.addActionListener(e -> dispose());
    }

    private void loadInitialData() {
        List<Teacher> teachers = scheduleService.getAllTeachers();
        currentTeachersList.clear();
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        if (teachers != null) {
            currentTeachersList.addAll(teachers);
            for (Teacher teacher : currentTeachersList) {
                Object[] row = {
                    teacher.getTeacherId(),
                    teacher.getName(),
                    teacher.getEmail(),
                    teacher.getPhoneNumber(),
                    getClassNameForTeacher(teacher.getClassId()) // TÙY CHỌN: Lấy tên lớp CN
                };
                tableModel.addRow(row);
            }
        }
    }

    private void populateFormFromSelectedRow() {
        int viewRow = teacherTable.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = teacherTable.convertRowIndexToModel(viewRow);
            if (modelRow >= 0 && modelRow < currentTeachersList.size()) {
                Teacher teacher = currentTeachersList.get(modelRow);
                nameField.setText(teacher.getName());
                emailField.setText(teacher.getEmail());
                phoneField.setText(teacher.getPhoneNumber());
                // TÙY CHỌN: Set lớp chủ nhiệm cho ComboBox
                selectClassInComboBox(teacher.getClassId());
            }
        }
    }

    private void addTeacherAction(ActionEvent e) {
        if (validateInputs()) {
            // Integer selectedClassId = getSelectedClassIdFromComboBox(); // TÙY CHỌN
            Teacher newTeacher = new Teacher(0, // ID sẽ do DB gán
                                            nameField.getText().trim(),
                                            emailField.getText().trim(),
                                            phoneField.getText().trim()
                                            // selectedClassId // TÙY CHỌN
                                            );
            Optional<Teacher> addedTeacherOpt = scheduleService.addTeacher(newTeacher);
            if (addedTeacherOpt.isPresent()) {
                JOptionPane.showMessageDialog(this, "Thêm giáo viên thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialData(); // Nạp lại dữ liệu bảng
                clearFormFields();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm giáo viên. Vui lòng kiểm tra log.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTeacherAction(ActionEvent e) {
        int viewRow = teacherTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một giáo viên để cập nhật.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (validateInputs()) {
            int modelRow = teacherTable.convertRowIndexToModel(viewRow);
            Teacher teacherToUpdate = currentTeachersList.get(modelRow); // Lấy đối tượng gốc
            
            // Cập nhật thông tin cho đối tượng gốc
            teacherToUpdate.setName(nameField.getText().trim());
            teacherToUpdate.setEmail(emailField.getText().trim());
            teacherToUpdate.setPhoneNumber(phoneField.getText().trim());
            // teacherToUpdate.setClassId(getSelectedClassIdFromComboBox()); // TÙY CHỌN

            boolean success = scheduleService.updateTeacher(teacherToUpdate);
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật giáo viên thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialData();
                clearFormFields();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật giáo viên. Vui lòng kiểm tra log.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTeacherAction(ActionEvent e) {
        int viewRow = teacherTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một giáo viên để xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = teacherTable.convertRowIndexToModel(viewRow);
        Teacher teacherToDelete = currentTeachersList.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa giáo viên: " + teacherToDelete.getName() + "?\n" +
                "(Lưu ý: Các phân công và lịch dạy của giáo viên này cũng có thể bị ảnh hưởng.)",
                "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = scheduleService.deleteTeacher(teacherToDelete.getTeacherId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa giáo viên thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadInitialData();
                clearFormFields();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa giáo viên. Vui lòng kiểm tra log.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFormFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        teacherTable.clearSelection();
        if (classComboBox != null) classComboBox.setSelectedIndex(-1); // Hoặc chọn item mặc định (null)
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên giáo viên không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocusInWindow();
            return false;
        }
        // Thêm validate email nếu muốn
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
             JOptionPane.showMessageDialog(this, "Định dạng email không hợp lệ.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            emailField.requestFocusInWindow();
            return false;
        }
        return true;
    }

    // --- TÙY CHỌN: Các phương thức liên quan đến ComboBox Lớp Chủ Nhiệm ---
    private void loadClassComboBox() {
        if (classComboBox == null) return;
        classComboBox.addItem(null); // Lựa chọn "Không có" hoặc để trống
        List<SchoolClass> allClasses = scheduleService.getAllSchoolClasses();
        if (allClasses != null) {
            for (SchoolClass sc : allClasses) {
                classComboBox.addItem(sc); // JComboBox sẽ gọi toString() của SchoolClass
            }
        }
        // Tùy chỉnh renderer cho ComboBox để hiển thị tên lớp thay vì toString() mặc định nếu cần
        classComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SchoolClass) {
                    setText(((SchoolClass) value).getName());
                } else {
                    setText("Chọn lớp..."); // Hoặc để trống
                }
                return this;
            }
        });
    }

    private Integer getSelectedClassIdFromComboBox() {
        if (classComboBox == null) return null;
        Object selectedItem = classComboBox.getSelectedItem();
        if (selectedItem instanceof SchoolClass) {
            return ((SchoolClass) selectedItem).getClassId();
        }
        return null;
    }

    private void selectClassInComboBox(Integer classId) {
        if (classComboBox == null) return;
        if (classId == null) {
            classComboBox.setSelectedItem(null); // Hoặc item "Không có"
            return;
        }
        for (int i = 0; i < classComboBox.getItemCount(); i++) {
            Object item = classComboBox.getItemAt(i);
            if (item instanceof SchoolClass && ((SchoolClass) item).getClassId() == classId) {
                classComboBox.setSelectedIndex(i);
                return;
            }
        }
        classComboBox.setSelectedIndex(-1); // Không tìm thấy
    }

    private String getClassNameForTeacher(Integer classId) {
        if (classId == null) return "";
        Optional<SchoolClass> scOpt = scheduleService.findSchoolClassById(classId);
        return scOpt.map(SchoolClass::getName).orElse("");
    }
}