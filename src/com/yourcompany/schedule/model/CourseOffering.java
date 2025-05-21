package com.yourcompany.schedule.model;

public class CourseOffering {
    private int offeringId;
    private Course course; // Represents course_id FK
    private Teacher teacher; // Represents teacher_id FK

    public CourseOffering() {
    }

    public CourseOffering(int offeringId, Course course, Teacher teacher) {
        this.offeringId = offeringId;
        this.course = course;
        this.teacher = teacher;
    }

    public int getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(int offeringId) {
        this.offeringId = offeringId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    @Override
    public String toString() {
        String courseName = course != null ? course.getCourseName() : "N/A";
        String teacherName = teacher != null ? teacher.getName() : "N/A";
        return "Offering ID: " + offeringId + " (Course: " + courseName + ", Teacher: " + teacherName + ")";
    }

	public AcaYear getAcademicYear() {
		// TODO Auto-generated method stub
		return null;
	}
}