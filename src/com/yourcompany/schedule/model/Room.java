package com.yourcompany.schedule.model;

public class Room {
    private int roomId;
    private String roomName;
    private int capacity;
    private String description;

    public Room() {}

    public Room(int roomId, String roomName, int capacity, String description) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
        this.description = description;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return roomName + " (Capacity: " + capacity + ")";
    }
} 