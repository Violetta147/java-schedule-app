package com.yourcompany.schedule.model;

import java.util.Objects;
// import java.util.ArrayList; // No longer needed for students
// import java.util.List; // No longer needed for students

public class SchoolClass {
    private int classId;
    private String name; // Derived from grade and section
    private int grade;
    private String section;
    // private List<String> students; // Removed, ERD does not have student list here

    public SchoolClass() {
        // this.students = new ArrayList<>(); // Removed
    }

    public SchoolClass(int classId, int grade, String section) {
        this.classId = classId;
        this.grade = grade;
        this.section = section;
        // this.students = new ArrayList<>(); // Removed
        updateName(); // Ensure name is set based on grade and section
    }
    
    // Constructor including name for convenience, though name is derived
    public SchoolClass(int classId, String name, int grade, String section) {
        this.classId = classId;
        this.name = name; // Will be overwritten by updateName if grade/section are valid
        this.grade = grade;
        this.section = section;
        updateName(); // Ensure name consistency
        // this.students = new ArrayList<>(); // Removed
    }


    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getName() {
        // Ensure name is up-to-date if grade/section might have changed elsewhere
        // (though setters for grade/section already call updateName)
        if ((this.name == null || this.name.isEmpty()) && grade > 0 && section != null && !section.isEmpty()){
            updateName();
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
        // Try to parse grade and section from name (format: "grade/section")
        if (name != null && name.contains("/")) {
            String[] parts = name.split("/");
            if (parts.length == 2) {
                try {
                    int parsedGrade = Integer.parseInt(parts[0]);
                    // Only update if parsing is successful and different from current
                    if (this.grade != parsedGrade) this.grade = parsedGrade;
                    if (!parts[1].equals(this.section)) this.section = parts[1];
                } catch (NumberFormatException e) {
                    // If parsing fails, it's better to clear grade/section
                    // or leave them as they are, depending on desired behavior.
                    // Here, we leave them, and name remains as set.
                }
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
        } else {
             // If grade/section are not valid, name might be explicitly set or null
             // To avoid issues, if name was derived, clear it.
             if (this.name != null && this.name.equals(this.grade + "/" + this.section)) {
                 this.name = null; // Or some default
             }
        }
    }

    // Methods related to 'students' are removed
    // public List<String> getStudents() { ... }
    // public void setStudents(List<String> students) { ... }
    // public void addStudent(String student) { ... }
    // public void removeStudent(String student) { ... }
    // public int getStudentCount() { ... }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchoolClass that = (SchoolClass) o;
        return classId == that.classId; // So sánh dựa trên ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId); // Hash dựa trên ID
    }
    
    @Override
    public String toString() {
        // return name + " (" + students.size() + " students)"; // Old
        return getName() != null ? getName() : "Class ID: " + classId; // New
    }
}