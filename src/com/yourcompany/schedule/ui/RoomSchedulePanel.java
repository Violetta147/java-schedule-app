package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class RoomSchedulePanel extends JPanel {
    private TimetablePanel timetablePanel;
    private Room room;

    public RoomSchedulePanel() {
        setLayout(new BorderLayout());
        timetablePanel = new TimetablePanel();
        add(timetablePanel, BorderLayout.CENTER);
    }

    public void setRoomAndEntries(Room room, List<ScheduleEntry> allEntries) {
        this.room = room;
        List<ScheduleEntry> roomEntries = allEntries.stream()
                .filter(e -> e.getRoom() != null && e.getRoom().equals(room))
                .collect(Collectors.toList());
        timetablePanel.setEntries(roomEntries);
    }
} 