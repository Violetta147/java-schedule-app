package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.*; // Import tất cả model
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector; // Dùng cho JComboBox model

public class AddEditEntryDialog extends JDialog {
    // Các ComboBox để chọn các thực thể liên quan
    private JComboBox<CourseOffering> courseOfferingComboBox;
    private JComboBox<SchoolClass> schoolClassComboBox;
    private JComboBox<Room> roomComboBox;
    private JComboBox<AcaYear> acaYearComboBox;

    // Thành phần chọn ngày và tiết
    private DatePicker datePicker;
    private JSpinner startPeriodSpinner;
    private JSpinner endPeriodSpinner;

    private boolean confirmed = false;
    private ScheduleEntry entryToEdit; // ScheduleEntry được truyền vào để chỉnh sửa

    // Danh sách các lựa chọn được truyền từ MainFrame
    private final List<CourseOffering> availableOfferings;
    private final List<SchoolClass> availableClasses;
    private final List<Room> availableRooms;
    private final List<AcaYear> availableAcaYears;

    public AddEditEntryDialog(Frame parent, ScheduleEntry entry,
                              List<CourseOffering> offerings, List<SchoolClass> classes,
                              List<Room> rooms, List<AcaYear> acaYears) {
        super(parent, true);
        setTitle(entry == null ? "Thêm Lịch Học Mới" : "Chỉnh Sửa Lịch Học");
        this.entryToEdit = entry;
        this.availableOfferings = offerings;
        this.availableClasses = classes;
        this.availableRooms = rooms;
        this.availableAcaYears = acaYears;

        initComponents();
        populateFieldsIfEditing();

        pack();
        setMinimumSize(new Dimension(500, 350));
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int gridY = 0;

        // Course Offering (Môn học - Giáo viên)
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Phân công (Môn - GV):"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        courseOfferingComboBox = new JComboBox<>(new Vector<>(availableOfferings)); // Vector để JComboBox hoạt động tốt
        courseOfferingComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CourseOffering) {
                    CourseOffering co = (CourseOffering) value;
                    setText(String.format("%s (%s) - GV: %s",
                            co.getCourse() != null ? co.getCourse().getCourseCode() : "N/A",
                            co.getCourse() != null ? co.getCourse().getCourseName() : "N/A",
                            co.getTeacher() != null ? co.getTeacher().getName() : "N/A"));
                } else if (value == null && index == -1) { // Mục đang được chọn nếu null
                    setText("Chọn phân công...");
                }
                return this;
            }
        });
        courseOfferingComboBox.setSelectedIndex(-1); // Không chọn gì ban đầu
        add(courseOfferingComboBox, gbc);
        gbc.weightx = 0;
        gridY++;

        // School Class
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Lớp học:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        schoolClassComboBox = new JComboBox<>(new Vector<>(availableClasses));
        schoolClassComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SchoolClass) {
                    setText(((SchoolClass) value).getName());
                } else if (value == null && index == -1) {
                    setText("Chọn lớp học...");
                }
                return this;
            }
        });
        schoolClassComboBox.setSelectedIndex(-1);
        add(schoolClassComboBox, gbc);
        gbc.weightx = 0;
        gridY++;

        // Room
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Phòng học:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        roomComboBox = new JComboBox<>(new Vector<>(availableRooms));
         roomComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Room) {
                    setText(((Room) value).getRoomName());
                } else if (value == null && index == -1) {
                    setText("Chọn phòng học...");
                }
                return this;
            }
        });
        roomComboBox.setSelectedIndex(-1);
        add(roomComboBox, gbc);
        gbc.weightx = 0;
        gridY++;

        // Academic Year
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Năm học:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        acaYearComboBox = new JComboBox<>(new Vector<>(availableAcaYears));
        acaYearComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AcaYear) {
                    setText(((AcaYear) value).getYearName());
                } else if (value == null && index == -1) {
                    setText("Chọn năm học...");
                }
                return this;
            }
        });
        acaYearComboBox.setSelectedIndex(-1);
        add(acaYearComboBox, gbc);
        gbc.weightx = 0;
        gridY++;
        
        // Date Picker
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Ngày học:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setAllowEmptyDates(false);
        datePicker = new DatePicker(dateSettings);
        datePicker.setDateToToday(); // Mặc định là ngày hôm nay
        add(datePicker, gbc);
        gbc.weightx = 0;
        gridY++;

        // Start Period Spinner
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Tiết bắt đầu:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        // Giả sử có tối đa 12 tiết
        startPeriodSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        add(startPeriodSpinner, gbc);
        gbc.weightx = 0;
        gridY++;

        // End Period Spinner
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Tiết kết thúc:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        endPeriodSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        add(endPeriodSpinner, gbc);
        gbc.weightx = 0;
        gridY++;

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Lưu");
        JButton cancelButton = new JButton("Hủy");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        add(buttonPanel, gbc);

        okButton.addActionListener(this::confirmAction);
        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
            dispose();
        });
    }

    private void populateFieldsIfEditing() {
        if (entryToEdit != null) {
            // Chọn CourseOffering
            if (entryToEdit.getCourseOffering() != null) {
                for (int i = 0; i < courseOfferingComboBox.getItemCount(); i++) {
                    CourseOffering item = courseOfferingComboBox.getItemAt(i);
                    if (item != null && item.getOfferingId() == entryToEdit.getCourseOffering().getOfferingId()) {
                        courseOfferingComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            // Chọn SchoolClass
            if (entryToEdit.getSchoolClass() != null) {
                 for (int i = 0; i < schoolClassComboBox.getItemCount(); i++) {
                    SchoolClass item = schoolClassComboBox.getItemAt(i);
                    if (item != null && item.getClassId() == entryToEdit.getSchoolClass().getClassId()) {
                        schoolClassComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            // Chọn Room
            if (entryToEdit.getRoom() != null) {
                 for (int i = 0; i < roomComboBox.getItemCount(); i++) {
                    Room item = roomComboBox.getItemAt(i);
                    if (item != null && item.getRoomId() == entryToEdit.getRoom().getRoomId()) {
                        roomComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            // Chọn AcaYear
            if (entryToEdit.getAcaYear() != null) {
                for (int i = 0; i < acaYearComboBox.getItemCount(); i++) {
                    AcaYear item = acaYearComboBox.getItemAt(i);
                    if (item != null && item.getYearId() == entryToEdit.getAcaYear().getYearId()) {
                        acaYearComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            datePicker.setDate(entryToEdit.getDate());
            startPeriodSpinner.setValue(entryToEdit.getStartPeriod());
            endPeriodSpinner.setValue(entryToEdit.getEndPeriod());
        }
    }

    private void confirmAction(ActionEvent e) {
        if (validateInputs()) {
            confirmed = true;
            setVisible(false);
            dispose();
        }
    }

    private boolean validateInputs() {
        if (courseOfferingComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Phân công (Môn - GV).", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (schoolClassComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Lớp học.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (roomComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Phòng học.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (acaYearComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Năm học.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (datePicker.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Ngày học.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int startPeriod = (Integer) startPeriodSpinner.getValue();
        int endPeriod = (Integer) endPeriodSpinner.getValue();

        if (startPeriod > endPeriod) {
            JOptionPane.showMessageDialog(this, "Tiết bắt đầu phải nhỏ hơn hoặc bằng tiết kết thúc.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Kiểm tra ràng buộc cùng buổi học (quan trọng)
        // Giả định ScheduleEntry có phương thức static hoặc bạn có cách truy cập logic này
        if (!ScheduleEntry.arePeriodsInSameSession(startPeriod, endPeriod)) {
             JOptionPane.showMessageDialog(this, "Tiết bắt đầu và tiết kết thúc phải thuộc cùng một buổi học (Sáng/Chiều).", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ScheduleEntry getEntryData() {
        if (!confirmed) {
            return null;
        }

        // Lấy các đối tượng đã chọn từ ComboBox
        CourseOffering selectedOffering = (CourseOffering) courseOfferingComboBox.getSelectedItem();
        SchoolClass selectedClass = (SchoolClass) schoolClassComboBox.getSelectedItem();
        Room selectedRoom = (Room) roomComboBox.getSelectedItem();
        AcaYear selectedAcaYear = (AcaYear) acaYearComboBox.getSelectedItem();
        LocalDate selectedDate = datePicker.getDate();
        int startPeriod = (Integer) startPeriodSpinner.getValue();
        int endPeriod = (Integer) endPeriodSpinner.getValue();

        // Tạo hoặc cập nhật ScheduleEntry
        // Nếu là edit, entryToEdit đã có ID. Nếu là add, ID sẽ là 0 hoặc được gán sau.
        int entryId = (entryToEdit != null) ? entryToEdit.getEntryId() : 0;

        try {
            return new ScheduleEntry(entryId, selectedOffering, selectedClass, selectedRoom,
                                 selectedAcaYear, selectedDate, startPeriod, endPeriod);
        } catch (IllegalArgumentException e) {
            // Lỗi này có thể xảy ra nếu constructor của ScheduleEntry có validate (ví dụ, cùng buổi)
            // và validateInputs() ở đây chưa bắt hết.
            JOptionPane.showMessageDialog(this, "Lỗi dữ liệu không hợp lệ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Phương thức tiện ích để hiển thị dialog và trả về ScheduleEntry đã được tạo/chỉnh sửa.
     */
    public static Optional<ScheduleEntry> showDialog(Frame parent, ScheduleEntry entryToEdit,
                                                   List<CourseOffering> offerings, List<SchoolClass> classes,
                                                   List<Room> rooms, List<AcaYear> acaYears) {
        AddEditEntryDialog dialog = new AddEditEntryDialog(parent, entryToEdit, offerings, classes, rooms, acaYears);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            return Optional.ofNullable(dialog.getEntryData());
        }
        return Optional.empty();
    }
}