package com.yourcompany.schedule.ui.dialogs;

import com.yourcompany.schedule.controller.RoomSelectionController;
import com.yourcompany.schedule.model.Room;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class RoomSelectionDialog extends JDialog {
    private RoomSelectionController controller;

    public RoomSelectionDialog(Frame parent, List<Room> availableRooms) {
        super(parent, "Select Room", true);
        this.controller = new RoomSelectionController(availableRooms);

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JTextField searchField = new JTextField();
        add(searchField, BorderLayout.NORTH);

        JList<Room> roomList = new JList<>(controller.getAvailableRooms().toArray(new Room[0]));
        roomList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controller.setSelectedRoom(roomList.getSelectedValue());
            }
        });
        add(new JScrollPane(roomList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener((ActionEvent e) -> {
            controller.handleConfirm();
            if (controller.isConfirmed()) {
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((ActionEvent e) -> {
            controller.handleCancel();
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { // Fixed method signature
                String searchText = searchField.getText().trim().toLowerCase();
                controller.filterRooms(searchText);
                roomList.setListData(controller.getAvailableRooms().toArray(new Room[0]));
            }
        });
    }

    public Room getSelectedRoom() {
        return controller.getSelectedRoom();
    }

    public boolean isConfirmed() {
        return controller.isConfirmed();
    }
}