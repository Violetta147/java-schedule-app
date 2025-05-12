package com.yourcompany.schedule.model;

import java.util.ArrayList;
import java.util.List;

public class SchoolClass {
    private int classId;
    private String name; // e.g., "10/1", "11/2"
    private int grade; // 10, 11, 12
    private String section; // "1", "2", etc.
    private List<String> students; // Simple implementation - could be expanded to Student objects

    public SchoolClass() {
        this.students = new ArrayList<>();
    }

    public SchoolClass(int classId, String name, int grade, String section) {
        this.classId = classId;
        this.name = name;
        this.grade = grade;
        this.section = section;
        this.students = new ArrayList<>();
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        
        // Try to parse grade and section from name (format: "grade/section")
        if (name != null && name.contains("/")) {
            String[] parts = name.split("/");
            try {
                this.grade = Integer.parseInt(parts[0]);
                this.section = parts[1];
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                // Keep existing values if parsing fails
            }
        }
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
        updateName();
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
        updateName();
    }

    private void updateName() {
        if (grade > 0 && section != null && !section.isEmpty()) {
            this.name = grade + "/" + section;
        }
    }

    public List<String> getStudents() {
        return students;
    }

    public void setStudents(List<String> students) {
        this.students = students != null ? students : new ArrayList<>();
    }

    public void addStudent(String student) {
        if (student != null && !student.isEmpty() && !students.contains(student)) {
            students.add(student);
        }
    }

    public void removeStudent(String student) {
        students.remove(student);
    }

    public int getStudentCount() {
        return students.size();
    }

    @Override
    public String toString() {
        return name + " (" + students.size() + " students)";
    }
} 