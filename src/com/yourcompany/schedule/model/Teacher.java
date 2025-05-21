package com.yourcompany.schedule.model;

import java.util.Objects;
// import java.util.ArrayList; // No longer needed for subjects
// import java.util.List; // No longer needed for subjects

public class Teacher {
    private int teacherId;
    private String name;
    private String email;
    private String phoneNumber;
    // private List<String> subjects; // Removed, relationship now through CourseOffering
    private Integer classId; // FK to SchoolClass, nullable if teacher is not a head teacher

    public Teacher() {
        // this.subjects = new ArrayList<>(); // Removed
    }

    public Teacher(int teacherId, String name, String email, String phoneNumber) {
        this.teacherId = teacherId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        // this.subjects = new ArrayList<>(); // Removed
    }
    
    public Teacher(int teacherId, String name, String email, String phoneNumber, Integer classId) {
        this.teacherId = teacherId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.classId = classId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    // Methods related to 'subjects' are removed as this relationship is now handled by CourseOffering
    // public List<String> getSubjects() { return subjects; }
    // public void setSubjects(List<String> subjects) { this.subjects = subjects; }
    // public void addSubject(String subject) { ... }
    // public void removeSubject(String subject) { ... }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return teacherId == teacher.teacherId; // So sánh dựa trên ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId); // Hash dựa trên ID
    }
    
    @Override
    public String toString() {
        return name;
    }
}