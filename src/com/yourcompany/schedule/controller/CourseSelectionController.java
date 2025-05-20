package com.yourcompany.schedule.controller;

import com.yourcompany.schedule.model.Course;
import java.util.List;
import java.util.stream.Collectors;

public class CourseSelectionController {
    private List<Course> availableCourses;
    private Course selectedCourse;
    private boolean confirmed;

    public CourseSelectionController(List<Course> availableCourses) {
        this.availableCourses = availableCourses;
        this.confirmed = false;
    }

    public List<Course> getAvailableCourses() {
        return availableCourses;
    }

    public Course getSelectedCourse() {
        return selectedCourse;
    }

    public void setSelectedCourse(Course selectedCourse) {
        this.selectedCourse = selectedCourse;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void handleConfirm() {
        if (selectedCourse != null) {
            confirmed = true;
        }
    }

    public void handleCancel() {
        confirmed = false;
    }

    public void filterCourses(String query) {
        if (query == null || query.isEmpty()) {
            return; // No filtering needed
        }
        availableCourses = availableCourses.stream()
            .filter(course -> course.getCourseName().toLowerCase().contains(query.toLowerCase())) // Ensure Course has getName()
            .collect(Collectors.toList());
    }
}
