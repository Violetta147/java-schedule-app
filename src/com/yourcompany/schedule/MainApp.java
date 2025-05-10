package com.yourcompany.schedule;

import com.yourcompany.schedule.ui.MainScheduleFrame;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainScheduleFrame frame = new MainScheduleFrame();
            frame.setVisible(true);
        });
    }
} 