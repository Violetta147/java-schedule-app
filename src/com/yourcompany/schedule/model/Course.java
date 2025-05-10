package com.yourcompany.schedule.model;

public class Course {
    private int courseId;
    private String courseCode;
    private String courseName;
    private String instructor;
    private int credits;

    public Course() {}

    public Course(int courseId, String courseCode, String courseName, String instructor, int credits) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.instructor = instructor;
        this.credits = credits;
    }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
} 