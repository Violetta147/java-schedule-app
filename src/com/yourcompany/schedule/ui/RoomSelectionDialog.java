package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;

public class RoomSelectionDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Room> allRooms;
    private Room selectedRoom;
    private boolean confirmed = false;

    public RoomSelectionDialog(JFrame parent, List<Room> rooms) {
        super(parent, "Select Room", true);
        this.allRooms = rooms;
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        searchField = new JTextField();
        add(searchField, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Capacity", "Description"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                selectedRoom = allRooms.stream().filter(r -> r.getRoomId() == id).findFirst().orElse(null);
                confirmed = true;
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a room.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterRooms();
            }
        });

        loadRooms("");
    }

    private void loadRooms(String filter) {
        tableModel.setRowCount(0);
        List<Room> filtered = allRooms;
        if (filter != null && !filter.trim().isEmpty()) {
            String f = filter.trim().toLowerCase();
            filtered = allRooms.stream().filter(r ->
                r.getRoomName().toLowerCase().contains(f) ||
                r.getDescription().toLowerCase().contains(f)
            ).collect(Collectors.toList());
        }
        for (Room r : filtered) {
            tableModel.addRow(new Object[]{r.getRoomId(), r.getRoomName(), r.getCapacity(), r.getDescription()});
        }
    }

    private void filterRooms() {
        loadRooms(searchField.getText());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Room getSelectedRoom() {
        return selectedRoom;
    }
} 