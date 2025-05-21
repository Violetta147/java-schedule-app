package com.yourcompany.schedule.model;

import java.util.Objects;

public class Room {
    private int roomId;
    private String roomName;
    // private int capacity; // Removed, not in ERD
    private String description;

    public Room() {}

    // Constructor updated
    public Room(int roomId, String roomName, String description) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.description = description;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    // Capacity getter/setter removed
    // public int getCapacity() { return capacity; }
    // public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        // return roomName + " (Capacity: " + capacity + ")"; // Old
        return roomName + (description != null && !description.isEmpty() ? " (" + description + ")" : ""); // New
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomId == room.roomId; // So sánh dựa trên roomId
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId); // Hash dựa trên roomId
    }
}