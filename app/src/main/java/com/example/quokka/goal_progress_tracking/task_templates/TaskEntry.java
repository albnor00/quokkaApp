package com.example.quokka.goal_progress_tracking.task_templates;

import java.time.LocalDate;

public class TaskEntry {
    private LocalDate date;
    private double value;

    public TaskEntry(LocalDate date, double value) {
        this.date = date;
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }
}
