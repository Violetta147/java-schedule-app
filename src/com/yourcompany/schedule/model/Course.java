package com.yourcompany.schedule.model;

public class Course {
    private int courseId;
    private String courseCode;
    private String courseName;
    private Teacher teacher;
    private SchoolClass schoolClass;
    private int credits;

    public Course() {}

    public Course(int courseId, String courseCode, String courseName, Teacher teacher, SchoolClass schoolClass, int credits) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.teacher = teacher;
        this.schoolClass = schoolClass;
        this.credits = credits;
    }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    // For backward compatibility
    public String getInstructor() { 
        return teacher != null ? teacher.getName() : null; 
    }
    
    public void setInstructor(String instructor) { 
        // This is kept for backward compatibility
        // In new code, use setTeacher instead
    }

    @Override
    public String toString() {
        String teacherName = teacher != null ? teacher.getName() : "No Teacher";
        String className = schoolClass != null ? schoolClass.getName() : "No Class";
        return courseCode + " - " + courseName + " (" + teacherName + ", " + className + ")";
    }
} 