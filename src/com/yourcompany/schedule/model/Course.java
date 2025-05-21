package com.yourcompany.schedule.model;

public class Course {
    private int courseId;
    private String courseCode;
    private String courseName;
    // private Teacher teacher; // Removed, relationship now through CourseOffering
    // private SchoolClass schoolClass; // Removed, schedule entry links offering to class
    // private int credits; // Removed, not in ERD

    public Course() {}

    // Constructor updated
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
    
    // Teacher, SchoolClass, and credits getters/setters are removed
    // public Teacher getTeacher() { return teacher; }
    // public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    // public SchoolClass getSchoolClass() { return schoolClass; }
    // public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    // public int getCredits() { return credits; }
    // public void setCredits(int credits) { this.credits = credits; }

    // Backward compatibility methods might need re-evaluation or removal
    // If getInstructor was crucial, it implies a Course had one designated instructor.
    // With CourseOffering, a Course can be taught by multiple teachers in different offerings.
    // This method as it was written is no longer directly applicable.
    /*
    public String getInstructor() { 
        // This logic is no longer valid as Teacher is not a direct attribute
        return null; // Or throw UnsupportedOperationException
    }
    
    public void setInstructor(String instructor) { 
        // This logic is no longer valid
        // Consider removing or adapting if there's a new meaning for a "default" instructor
    }
    */

    @Override
    public String toString() {
        // Updated to reflect changes
        return courseCode + " - " + courseName;
    }
}