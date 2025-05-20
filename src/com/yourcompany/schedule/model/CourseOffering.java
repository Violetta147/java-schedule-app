package com.yourcompany.schedule.model;

public class CourseOffering {
    private int offeringId;
    private Course course;
    private SchoolClass schoolClass;
    private Teacher teacher;

    public CourseOffering() {}

    public CourseOffering(int offeringId, Course course, SchoolClass schoolClass, Teacher teacher) {
        this.offeringId = offeringId;
        this.course = course;
        this.schoolClass = schoolClass;
        this.teacher = teacher;
    }

    public int getOfferingId() { return offeringId; }
    public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    @Override
    public String toString() {
        return course + " - " + schoolClass + " (" + teacher + ")";
    }
}