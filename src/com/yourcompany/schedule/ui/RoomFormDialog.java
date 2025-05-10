package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Room;

import javax.swing.*;
import java.awt.*;

public class RoomFormDialog extends JDialog {
    private JTextField nameField, capacityField, descriptionField;
    private boolean confirmed = false;
    private Room room;

    public RoomFormDialog(JFrame parent, Room room) {
        super(parent, room == null ? "Add Room" : "Edit Room", true);
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4, 2, 5, 5));

        add(new JLabel("Room Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Capacity:"));
        capacityField = new JTextField();
        add(capacityField);

        add(new JLabel("Description:"));
        descriptionField = new JTextField();
        add(descriptionField);

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        add(okButton);
        add(cancelButton);

        if (room != null) {
            nameField.setText(room.getRoomName());
            capacityField.setText(String.valueOf(room.getCapacity()));
            descriptionField.setText(room.getDescription());
            this.room = room;
        }

        okButton.addActionListener(e -> {
            if (validateFields()) {
                confirmed = true;
                setVisible(false);
            }
        });
        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty() || capacityField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Integer.parseInt(capacityField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacity must be a number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Room getRoom() {
        if (room == null) {
            room = new Room();
        }
        room.setRoomName(nameField.getText().trim());
        room.setCapacity(Integer.parseInt(capacityField.getText().trim()));
        room.setDescription(descriptionField.getText().trim());
        return room;
    }
} 