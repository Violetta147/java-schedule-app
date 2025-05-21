package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.SchoolClass;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

public class SchoolClassFormDialog extends JDialog { // Đổi thành public
    private JSpinner gradeSpinner;
    private JTextField sectionField;
    private boolean confirmed = false;
    private SchoolClass classToEdit;

    public SchoolClassFormDialog(Frame parent, SchoolClass schoolClass) {
        super(parent, schoolClass == null ? "Thêm Lớp Học" : "Sửa Lớp Học", true);
        this.classToEdit = schoolClass;

        initComponents();
        populateFieldsIfEditing();

        pack();
        setMinimumSize(new Dimension(350, 180));
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

        // Grade Spinner
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Khối Lớp:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        // Giả sử khối lớp từ 1 đến 12
        gradeSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 12, 1)); // Mặc định khối 10
        add(gradeSpinner, gbc);
        gbc.weightx = 0;
        gridY++;

        // Section Field
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Tên Lớp/Ban:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        sectionField = new JTextField(15); // Ví dụ: A1, Chuyên Toán, 10/1
        add(sectionField, gbc);
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
        if (classToEdit != null) {
            gradeSpinner.setValue(classToEdit.getGrade());
            sectionField.setText(classToEdit.getSection());
        } else {
            // Giá trị mặc định khi thêm mới (ví dụ)
            gradeSpinner.setValue(10); // Mặc định khối 10
            sectionField.setText("");
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
        // Grade đã được JSpinner giới hạn, nhưng vẫn có thể kiểm tra nếu cần
        int grade = (Integer) gradeSpinner.getValue();
        if (grade < 1 || grade > 12) { // Ví dụ giới hạn
            JOptionPane.showMessageDialog(this, "Khối lớp không hợp lệ.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            gradeSpinner.requestFocusInWindow();
            return false;
        }

        String section = sectionField.getText().trim();
        if (section.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên lớp/ban không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            sectionField.requestFocusInWindow();
            return false;
        }
        // Bạn có thể thêm các kiểm tra khác cho section (ví dụ: không chứa ký tự đặc biệt)
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public SchoolClass getSchoolClassData() {
        if (!confirmed) {
            return null;
        }

        SchoolClass result = (classToEdit == null) ? new SchoolClass() : classToEdit;
        // ID của result sẽ là ID của classToEdit nếu đang sửa, hoặc 0 (mặc định) nếu thêm mới.

        result.setGrade((Integer) gradeSpinner.getValue());
        result.setSection(sectionField.getText().trim());
        // Thuộc tính 'name' của SchoolClass sẽ được tự động cập nhật bên trong
        // các setter setGrade/setSection (nếu bạn đã implement updateName() ở đó).

        return result;
    }

    public static Optional<SchoolClass> showDialog(Frame parent, SchoolClass classToEdit) {
        SchoolClassFormDialog dialog = new SchoolClassFormDialog(parent, classToEdit);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            return Optional.ofNullable(dialog.getSchoolClassData());
        }
        return Optional.empty();
    }
}