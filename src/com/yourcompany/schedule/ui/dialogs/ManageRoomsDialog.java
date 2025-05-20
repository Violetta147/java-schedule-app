package com.yourcompany.schedule.ui.dialogs;

import com.yourcompany.schedule.data.DataManager;
import com.yourcompany.schedule.model.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageRoomsDialog extends JDialog {
    private DataManager dataManager;
    private JTable table;
    private DefaultTableModel tableModel;

    public ManageRoomsDialog(JFrame parent, DataManager dataManager) {
        super(parent, "Manage Rooms", true);
        this.dataManager = dataManager;
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Capacity", "Description"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addRoom());
        editButton.addActionListener(e -> editRoom());
        deleteButton.addActionListener(e -> deleteRoom());

        loadRooms();
    }

    private void loadRooms() {
        tableModel.setRowCount(0);
        try {
            List<Room> rooms = dataManager.getAllRooms();
            for (Room r : rooms) {
                tableModel.addRow(new Object[]{r.getRoomId(), r.getRoomName(), r.getCapacity(), r.getDescription()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRoom() {
        RoomFormDialog dialog = new RoomFormDialog((JFrame) getParent(), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                dataManager.addRoom(dialog.getRoom());
                loadRooms();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a room to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Room room = new Room(
            (int) tableModel.getValueAt(row, 0),
            (String) tableModel.getValueAt(row, 1),
            (int) tableModel.getValueAt(row, 2),
            (String) tableModel.getValueAt(row, 3)
        );
        RoomFormDialog dialog = new RoomFormDialog((JFrame) getParent(), room);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                dataManager.updateRoom(dialog.getRoom());
                loadRooms();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a room to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this room?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int roomId = (int) tableModel.getValueAt(row, 0);
                dataManager.deleteRoom(roomId);
                loadRooms();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 