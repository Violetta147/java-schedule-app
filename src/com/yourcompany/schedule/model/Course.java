package com.yourcompany.schedule.model;

public class Course {
    private int courseId;
    private String courseCode;
    private String courseName;
    private String name;

    public Course() {}

    public Course(int courseId, String courseCode, String courseName) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
    }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // For backward compatibility
    public String getInstructor() { 
        return null; 
    }
    
    public void setInstructor(String instructor) { 
        // This is kept for backward compatibility
        // In new code, use setTeacher instead
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}