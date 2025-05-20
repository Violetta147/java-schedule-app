package com.yourcompany.schedule.controller;

import com.yourcompany.schedule.model.Room;
import java.util.List;
import java.util.stream.Collectors;

public class RoomSelectionController {
    private List<Room> availableRooms;
    private List<Room> filteredRooms;
    private Room selectedRoom;
    private boolean confirmed;

    public RoomSelectionController(List<Room> availableRooms) {
        this.availableRooms = availableRooms;
        this.filteredRooms = availableRooms;
        this.confirmed = false;
    }

    public List<Room> getAvailableRooms() {
        return filteredRooms;
    }

    public Room getSelectedRoom() {
        return selectedRoom;
    }

    public void setSelectedRoom(Room selectedRoom) {
        this.selectedRoom = selectedRoom;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void handleConfirm() {
        if (selectedRoom != null) {
            confirmed = true;
        }
    }

    public void handleCancel() {
        confirmed = false;
    }

    public void filterRooms(String searchText) {
        filteredRooms = availableRooms.stream()
                .filter(room -> room.getRoomName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
    }
}
