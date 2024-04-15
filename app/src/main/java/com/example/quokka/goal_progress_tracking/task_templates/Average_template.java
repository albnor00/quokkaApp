package com.example.quokka.goal_progress_tracking.task_templates;

import java.util.ArrayList;
import java.util.List;

public class Average_template {
    private String taskName;
    private String taskDescription;
    private double target;
    private List<Double> dailyEntries;


    public Average_template(String taskName, String taskDescription, double target){
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.target = target;
        this.dailyEntries = new ArrayList<>();
    }

    public void addDailyEntry(double entry) {
        dailyEntries.add(entry);
    }

    public double calculateAverage() {
        if (dailyEntries.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Double entry : dailyEntries) {
            sum += entry;
        }
        return sum / dailyEntries.size();
    }

    // Getters and setters
    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public double getTarget() {
        return target;
    }
}
