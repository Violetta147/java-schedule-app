package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.CourseOffering;
import com.yourcompany.schedule.model.Teacher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
import java.util.Vector; // Dùng cho JComboBox model

public class CourseOfferingFormDialog extends JDialog { // Đổi thành public
    private JComboBox<Course> courseComboBox;
    private JComboBox<Teacher> teacherComboBox;
    private boolean confirmed = false;
    private CourseOffering offeringToEdit;

    private final List<Course> availableCourses;
    private final List<Teacher> availableTeachers;

    public CourseOfferingFormDialog(Frame parent, CourseOffering offering, List<Course> courses, List<Teacher> teachers) {
        super(parent, offering == null ? "Thêm Phân Công Giảng Dạy" : "Sửa Phân Công Giảng Dạy", true);
        this.offeringToEdit = offering;
        this.availableCourses = courses; // Lưu lại để dùng
        this.availableTeachers = teachers; // Lưu lại để dùng

        initComponents();
        populateFieldsIfEditing();

        pack();
        setMinimumSize(new Dimension(450, 200));
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

        // Course ComboBox
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Môn học:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        courseComboBox = new JComboBox<>(new Vector<>(availableCourses)); // Sử dụng Vector
        courseComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) {
                    Course c = (Course) value;
                    setText(String.format("%s - %s", c.getCourseCode(), c.getCourseName()));
                } else if (value == null && index == -1) {
                    setText("Chọn môn học...");
                }
                return this;
            }
        });
        courseComboBox.setSelectedIndex(-1); // Không chọn gì ban đầu
        add(courseComboBox, gbc);
        gbc.weightx = 0;
        gridY++;

        // Teacher ComboBox
        gbc.gridx = 0; gbc.gridy = gridY; add(new JLabel("Giáo viên:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY; gbc.weightx = 1.0;
        teacherComboBox = new JComboBox<>(new Vector<>(availableTeachers)); // Sử dụng Vector
        teacherComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Teacher) {
                    setText(((Teacher) value).getName());
                } else if (value == null && index == -1) {
                    setText("Chọn giáo viên...");
                }
                return this;
            }
        });
        teacherComboBox.setSelectedIndex(-1); // Không chọn gì ban đầu
        add(teacherComboBox, gbc);
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
        if (offeringToEdit != null) {
            // Chọn Course trong ComboBox
            if (offeringToEdit.getCourse() != null) {
                for (int i = 0; i < courseComboBox.getItemCount(); i++) {
                    Course item = courseComboBox.getItemAt(i);
                    if (item != null && item.getCourseId() == offeringToEdit.getCourse().getCourseId()) {
                        courseComboBox.setSelectedItem(item); // Hoặc setSelectedIndex(i)
                        break;
                    }
                }
            } else {
                courseComboBox.setSelectedIndex(-1); // Hoặc 0 nếu có item placeholder
            }

            // Chọn Teacher trong ComboBox
            if (offeringToEdit.getTeacher() != null) {
                for (int i = 0; i < teacherComboBox.getItemCount(); i++) {
                    Teacher item = teacherComboBox.getItemAt(i);
                    if (item != null && item.getTeacherId() == offeringToEdit.getTeacher().getTeacherId()) {
                        teacherComboBox.setSelectedItem(item); // Hoặc setSelectedIndex(i)
                        break;
                    }
                }
            } else {
                teacherComboBox.setSelectedIndex(-1); // Hoặc 0 nếu có item placeholder
            }
        } else {
            // Thêm mới: Đảm bảo không có gì được chọn hoặc chọn placeholder
            if (courseComboBox.getItemCount() > 0) courseComboBox.setSelectedIndex(-1); // Hoặc 0 nếu có item placeholder
            if (teacherComboBox.getItemCount() > 0) teacherComboBox.setSelectedIndex(-1); // Hoặc 0 nếu có item placeholder
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
        if (courseComboBox.getSelectedItem() == null || !(courseComboBox.getSelectedItem() instanceof Course) ) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một Môn học.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (teacherComboBox.getSelectedItem() == null || !(teacherComboBox.getSelectedItem() instanceof Teacher)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một Giáo viên.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // Có thể thêm kiểm tra xem cặp (Course, Teacher) này đã tồn tại chưa ở đây
        // nếu bạn muốn bắt lỗi trước khi gọi service.
        // Tuy nhiên, service và DB (với UNIQUE constraint) cũng sẽ xử lý việc này.
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public CourseOffering getCourseOfferingData() {
        if (!confirmed) {
            return null;
        }

        Course selectedCourse = (Course) courseComboBox.getSelectedItem();
        Teacher selectedTeacher = (Teacher) teacherComboBox.getSelectedItem();

        // Nếu validateInput đã đảm bảo selectedCourse và selectedTeacher không null và đúng kiểu,
        // thì không cần kiểm tra null ở đây nữa.

        CourseOffering result = (offeringToEdit == null) ? new CourseOffering() : offeringToEdit;
        // ID của result sẽ là ID của offeringToEdit nếu đang sửa, hoặc 0 (mặc định) nếu thêm mới.

        result.setCourse(selectedCourse);
        result.setTeacher(selectedTeacher);

        return result;
    }

    public static Optional<CourseOffering> showDialog(Frame parent, CourseOffering offeringToEdit,
                                                    List<Course> courses, List<Teacher> teachers) {
        CourseOfferingFormDialog dialog = new CourseOfferingFormDialog(parent, offeringToEdit, courses, teachers);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            return Optional.ofNullable(dialog.getCourseOfferingData());
        }
        return Optional.empty();
    }
}