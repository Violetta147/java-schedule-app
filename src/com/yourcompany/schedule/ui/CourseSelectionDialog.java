package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Course;

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
import java.util.Optional; // Sử dụng Optional cho phương thức tĩnh
import java.util.stream.Collectors;

public class CourseSelectionDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Course> allCoursesMasterList; // Danh sách gốc
    private List<Course> currentlyDisplayedCourses; // Danh sách đang hiển thị sau khi lọc
    private Course selectedCourse;
    private boolean confirmed = false;

    public CourseSelectionDialog(Frame parent, List<Course> courses) { // Thay JFrame bằng Frame
        super(parent, "Chọn Môn Học", true);
        this.allCoursesMasterList = new ArrayList<>(courses); // Tạo bản sao
        this.currentlyDisplayedCourses = new ArrayList<>(this.allCoursesMasterList);

        initComponents();
        loadCoursesToTable(searchField.getText()); // Load ban đầu

        pack();
        setMinimumSize(new Dimension(450, 300));
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm (Mã hoặc Tên MH):"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Bảng hiển thị môn học
        // Cột: ID, Mã Môn Học, Tên Môn Học (Bỏ Instructor, Credits)
        tableModel = new DefaultTableModel(new Object[]{"ID", "Mã MH", "Tên Môn Học"}, 0) {
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
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Mã MH
        table.getColumnModel().getColumn(2).setPreferredWidth(250); // Tên Môn Học


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
            dispose();
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadCoursesToTable(searchField.getText());
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirmSelection();
                }
            }
        });
    }

    private void loadCoursesToTable(String filterText) {
        tableModel.setRowCount(0);
        currentlyDisplayedCourses.clear();

        String filter = (filterText != null) ? filterText.trim().toLowerCase() : "";

        if (filter.isEmpty()) {
            currentlyDisplayedCourses.addAll(allCoursesMasterList);
        } else {
            currentlyDisplayedCourses.addAll(
                allCoursesMasterList.stream().filter(course ->
                    (course.getCourseCode() != null && course.getCourseCode().toLowerCase().contains(filter)) ||
                    (course.getCourseName() != null && course.getCourseName().toLowerCase().contains(filter))
                    // Không còn lọc theo c.getInstructor()
                ).collect(Collectors.toList())
            );
        }

        for (Course c : currentlyDisplayedCourses) {
            tableModel.addRow(new Object[]{
                    c.getCourseId(),
                    c.getCourseCode(),
                    c.getCourseName()
                    // Không còn c.getInstructor(), c.getCredits()
            });
        }
    }

    // filterCourses() không cần thiết nữa vì logic đã nằm trong loadCoursesToTable()
    // private void filterCourses() {
    //     loadCoursesToTable(searchField.getText());
    // }

    private void confirmSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = table.convertRowIndexToModel(viewRow);
             if (modelRow >= 0 && modelRow < currentlyDisplayedCourses.size()) {
                selectedCourse = currentlyDisplayedCourses.get(modelRow);
                confirmed = true;
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một môn học.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Course getSelectedCourse() {
        return selectedCourse;
    }

    /**
     * Phương thức tiện ích để hiển thị dialog và trả về môn học được chọn.
     * @param parent Frame cha.
     * @param courses Danh sách các môn học để lựa chọn.
     * @return Optional chứa Course được chọn, hoặc Optional.empty() nếu người dùng hủy.
     */
    public static Optional<Course> selectCourse(Frame parent, List<Course> courses) {
        CourseSelectionDialog dialog = new CourseSelectionDialog(parent, courses);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            return Optional.ofNullable(dialog.getSelectedCourse());
        }
        return Optional.empty();
    }
}