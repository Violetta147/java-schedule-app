package com.yourcompany.schedule.ui;

import javax.swing.*;
import java.awt.*; // Import java.awt.*
import java.awt.event.ActionEvent; // Import nếu dùng ActionListener riêng

public class ConflictDialog extends JDialog {

    public ConflictDialog(Frame parent, String message) { // Thay JFrame bằng Frame cho tổng quát
        super(parent, "Xung Đột Lịch Trình", true); // Tiêu đề tiếng Việt, modal = true
        // Hoặc giữ nguyên "Schedule Conflict" nếu bạn muốn tiếng Anh

        initComponents(message);

        pack(); // Tự động điều chỉnh kích thước dialog cho vừa với nội dung
        setMinimumSize(new Dimension(300, 150)); // Đảm bảo kích thước tối thiểu
        setLocationRelativeTo(parent); // Căn giữa so với parent
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Giải phóng tài nguyên khi đóng
    }

    private void initComponents(String message) {
        setLayout(new BorderLayout(10, 10)); // Layout chính
        // Ép kiểu content pane và đặt border để có padding
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Panel chứa thông báo và biểu tượng
        JPanel messagePanel = new JPanel(new BorderLayout(10, 0));

        // Biểu tượng cảnh báo (lấy từ JOptionPane)
        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
        messagePanel.add(iconLabel, BorderLayout.WEST);

        // Thông báo lỗi (cho phép xuống dòng với HTML)
        // Sử dụng JLabel với HTML để có thể xuống dòng nếu message quá dài
        JLabel messageLabel = new JLabel("<html><body style='width: 200px; text-align:center;'>" + escapeHtml(message) + "</body></html>", SwingConstants.CENTER);
        // messageLabel.setVerticalAlignment(SwingConstants.CENTER); // Đã CENTER theo mặc định của BorderLayout.CENTER trong messagePanel
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        add(messagePanel, BorderLayout.CENTER);

        // Nút OK
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        okButton.addActionListener((ActionEvent e) -> {
            setVisible(false);
            dispose();
        });
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Đặt nút OK làm nút mặc định (khi nhấn Enter)
        getRootPane().setDefaultButton(okButton);
    }

    // Helper để escape HTML trong message nếu cần (đề phòng trường hợp message chứa ký tự HTML)
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&")
                   .replace("<", "<")
                   .replace(">", ">")
                   .replace("\"", "&quot")
                   .replace("'", "'");
    }

    /**
     * Phương thức tĩnh tiện ích để hiển thị dialog.
     * @param parent Frame cha.
     * @param message Thông báo xung đột.
     */
    public static void showDialog(Frame parent, String message) {
        ConflictDialog dialog = new ConflictDialog(parent, message);
        dialog.setVisible(true);
    }

}