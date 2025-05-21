package com.yourcompany.schedule.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener; // Không cần thiết nếu dùng lambda
import java.util.Optional;
// import java.util.Date; // Không dùng java.util.Date, dùng java.time.LocalDate
import java.time.LocalDate; // Import LocalDate

import com.yourcompany.schedule.model.AcaYear;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

public class AcaYearFormDialog extends JDialog { // Đổi thành public nếu cần truy cập từ package khác
    private JTextField nameField;
    private DatePicker startDatePicker;
    private JSpinner weeksSpinner;
    private boolean confirmed = false;
    private AcaYear acaYearToEdit;

    public AcaYearFormDialog(Frame parent, AcaYear acaYear) {
        super(parent, acaYear == null ? "Thêm Năm Học" : "Sửa Năm Học", true);
        this.acaYearToEdit = acaYear;

        initComponents();
        populateFieldsIfEditing();

        pack(); // Tự động điều chỉnh kích thước
        setMinimumSize(new Dimension(350, 200)); // Đặt kích thước tối thiểu
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Quan trọng
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int gridY = 0;

        // Year Name
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Tên Năm Học:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        add(nameField, gbc);
        gbc.weightx = 0;
        gridY++;

        // Start Date
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Ngày Bắt Đầu:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setAllowEmptyDates(false); // Không cho phép để trống ngày
        startDatePicker = new DatePicker(dateSettings);
        add(startDatePicker, gbc);
        gbc.weightx = 0;
        gridY++;

        // Weeks
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Số Tuần Học:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        // Spinner cho số tuần, ví dụ từ 1 đến 52, mặc định 35
        weeksSpinner = new JSpinner(new SpinnerNumberModel(35, 1, 52, 1));
        add(weeksSpinner, gbc);
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

        // Action Listeners
        okButton.addActionListener(this::confirmAction);
        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
            dispose();
        });
    }

    private void populateFieldsIfEditing() {
        if (acaYearToEdit != null) {
            nameField.setText(acaYearToEdit.getYearName());
            if (acaYearToEdit.getStartDate() != null) {
                startDatePicker.setDate(acaYearToEdit.getStartDate());
            }
            weeksSpinner.setValue(acaYearToEdit.getWeeks());
        } else {
            // Giá trị mặc định khi thêm mới
            startDatePicker.setDateToToday(); // Hoặc một ngày gợi ý khác
            weeksSpinner.setValue(35); // Số tuần mặc định
        }
    }

    private void confirmAction(ActionEvent e) {
        if (validateInput()) {
            confirmed = true;
            setVisible(false);
            dispose();
        }
    }

    private boolean validateInput() {
        String yearName = nameField.getText().trim();
        if (yearName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên năm học không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocusInWindow();
            return false;
        }
        // Kiểm tra định dạng tên năm học nếu cần (ví dụ: "YYYY-YYYY")
        // if (!yearName.matches("^\\d{4}-\\d{4}$")) {
        //     JOptionPane.showMessageDialog(this, "Định dạng tên năm học không hợp lệ (ví dụ: 2023-2024).", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
        //     nameField.requestFocusInWindow();
        //     return false;
        // }

        if (startDatePicker.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            startDatePicker.requestFocusInWindow();
            return false;
        }

        int weeks = 0;
        try {
            weeks = (Integer) weeksSpinner.getValue();
            if (weeks <= 0) {
                JOptionPane.showMessageDialog(this, "Số tuần học phải là một số dương.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
                weeksSpinner.requestFocusInWindow();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Số tuần học không hợp lệ.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            weeksSpinner.requestFocusInWindow();
            return false;
        }
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public AcaYear getAcaYearData() {
        if (!confirmed) {
            return null;
        }
        AcaYear result = (acaYearToEdit == null) ? new AcaYear() : acaYearToEdit;
        // ID của result sẽ là ID của acaYearToEdit nếu đang sửa, hoặc 0 (mặc định) nếu thêm mới
        // DataManager sẽ xử lý việc gán ID từ DB khi thêm mới.

        result.setYearName(nameField.getText().trim());
        result.setStartDate(startDatePicker.getDate()); // getDate() của DatePicker trả về LocalDate
        result.setWeeks((Integer) weeksSpinner.getValue());
        return result;
    }

    public static Optional<AcaYear> showDialog(Frame parent, AcaYear acaYearToEdit) {
        AcaYearFormDialog dialog = new AcaYearFormDialog(parent, acaYearToEdit);
        dialog.setVisible(true); // Lệnh này sẽ block cho đến khi dialog đóng
        if (dialog.isConfirmed()) {
            return Optional.ofNullable(dialog.getAcaYearData());
        }
        return Optional.empty();
    }
}