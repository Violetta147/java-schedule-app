package com.yourcompany.schedule.model;

import java.time.LocalDate;

public class AcaYear {
    private int yearId;
    private String yearName; // e.g., "2023-2024"
    private LocalDate startDate;
    private int weeks;

    public AcaYear() {
    }

    public AcaYear(int yearId, String yearName, LocalDate startDate, int weeks) {
        this.yearId = yearId;
        this.yearName = yearName;
        this.startDate = startDate;
        this.weeks = weeks;
    }

    public int getYearId() {
        return yearId;
    }

    public void setYearId(int yearId) {
        this.yearId = yearId;
    }

    public String getYearName() {
        return yearName;
    }

    public void setYearName(String yearName) {
        this.yearName = yearName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getWeeks() {
        return weeks;
    }

    public void setWeeks(int weeks) {
        this.weeks = weeks;
    }

    @Override
    public String toString() {
        return yearName;
    }
}