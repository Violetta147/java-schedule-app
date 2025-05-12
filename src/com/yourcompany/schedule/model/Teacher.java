package com.yourcompany.schedule.model;

import java.util.ArrayList;
import java.util.List;

public class Teacher {
    private int teacherId;
    private String name;
    private String email;
    private String phoneNumber;
    private List<String> subjects;

    public Teacher() {
        this.subjects = new ArrayList<>();
    }

    public Teacher(int teacherId, String name, String email, String phoneNumber) {
        this.teacherId = teacherId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.subjects = new ArrayList<>();
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

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public void addSubject(String subject) {
        if (!subjects.contains(subject)) {
            subjects.add(subject);
        }
    }

    public void removeSubject(String subject) {
        subjects.remove(subject);
    }

    @Override
    public String toString() {
        return name;
    }
} 