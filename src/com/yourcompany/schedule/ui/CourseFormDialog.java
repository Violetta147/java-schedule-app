package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Course;
// import com.yourcompany.schedule.model.Teacher; // Không cần nữa
// import com.yourcompany.schedule.model.SchoolClass; // Không cần nữa

import javax.swing.*;
import java.awt.*;
// import java.util.List; // Không cần nữa
import java.util.Optional; // Sử dụng Optional cho phương thức showDialog tĩnh

public class CourseFormDialog extends JDialog {
    private JTextField codeField;
    private JTextField nameField;
    // private JTextField creditsField; // ĐÃ LOẠI BỎ
    // private JComboBox<Teacher> teacherComboBox; // ĐÃ LOẠI BỎ
    // private JComboBox<SchoolClass> classComboBox; // ĐÃ LOẠI BỎ

    private boolean confirmed = false;
    private Course courseToEdit; // Đối tượng Course được truyền vào để chỉnh sửa

    // Constructor được đơn giản hóa
    public CourseFormDialog(Frame parent, Course course) { // Thay JFrame bằng Frame
        super(parent, course == null ? "Thêm Môn Học" : "Chỉnh Sửa Môn Học", true);
        this.courseToEdit = course;

        initComponents();
        populateFieldsIfEditing();

        pack(); // Tự điều chỉnh kích thước
        setMinimumSize(new Dimension(350, 180)); // Kích thước tối thiểu
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Course Code
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Mã Môn Học:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        codeField = new JTextField(15); add(codeField, gbc);
        gbc.weightx = 0;

        // Course Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Tên Môn Học:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(25); add(nameField, gbc);
        gbc.weightx = 0;

        // Teacher, Class, Credits fields đã được loại bỏ

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Lưu");
        JButton cancelButton = new JButton("Hủy");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 2; // Dòng tiếp theo
        gbc.gridwidth = 2; // Chiếm 2 cột
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
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
        if (courseToEdit != null) {
            codeField.setText(courseToEdit.getCourseCode());
            nameField.setText(courseToEdit.getCourseName());
            // creditsField.setText(String.valueOf(courseToEdit.getCredits())); // LOẠI BỎ
            // Không còn set teacherComboBox và classComboBox
        }
    }

    private boolean validateInput() {
        String courseCode = codeField.getText().trim();
        String courseName = nameField.getText().trim();

        if (courseCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã môn học không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            codeField.requestFocusInWindow();
            return false;
        }
        if (courseName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên môn học không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocusInWindow();
            return false;
        }
        // Không còn validate credits
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Trả về đối tượng Course với dữ liệu từ form.
     * Nếu là form edit, nó sẽ cập nhật courseToEdit.
     * Nếu là form add, nó sẽ tạo một Course mới.
     * @return Course object, hoặc null nếu không confirmed.
     */
    public Course getCourseData() {
        if (!confirmed) {
            return null;
        }

        Course resultCourse = (courseToEdit == null) ? new Course() : courseToEdit;
        
        resultCourse.setCourseCode(codeField.getText().trim());
        resultCourse.setCourseName(nameField.getText().trim());
        // Không còn setTeacher, setSchoolClass, setCredits

        // Nếu là thêm mới, ID của resultCourse sẽ là 0 (hoặc giá trị mặc định).
        // DataManager sẽ xử lý việc gán ID từ DB sau khi thêm.
        // Nếu là edit, resultCourse (chính là courseToEdit) đã có ID.
        return resultCourse;
    }

    /**
     * Phương thức tiện ích để hiển thị dialog và trả về môn học đã được tạo/chỉnh sửa.
     * @param parent Frame cha.
     * @param courseToEdit Course cần chỉnh sửa, hoặc null nếu muốn thêm mới.
     * @return Optional chứa Course đã tạo/chỉnh sửa, hoặc Optional.empty() nếu người dùng hủy.
     */
    public static Optional<Course> showDialog(Frame parent, Course courseToEdit) {
        CourseFormDialog dialog = new CourseFormDialog(parent, courseToEdit);
        dialog.setVisible(true); // Lệnh này sẽ block cho đến khi dialog đóng
        if (dialog.isConfirmed()) {
            return Optional.ofNullable(dialog.getCourseData());
        }
        return Optional.empty();
    }
}