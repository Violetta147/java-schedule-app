package com.yourcompany.schedule.ui.dialogs;

import javax.swing.*;

public class ConflictDialog extends JDialog {
    public ConflictDialog(JFrame parent, String message) {
        super(parent, true);
        setTitle("Schedule Conflict");
        setSize(300, 150);
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        add(label);
        // TODO: Add more UI elements if needed
    }
} 