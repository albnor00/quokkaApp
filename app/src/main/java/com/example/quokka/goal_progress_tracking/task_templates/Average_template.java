package com.example.quokka.goal_progress_tracking.task_templates;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import android.util.Pair;

public class Average_template implements Task, Serializable {
    private String taskName;
    private String taskDescription;
    private double target;
    private List<Pair<LocalDate, Double>> dailyEntries;

    public Average_template(String taskName, String taskDescription, double target) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.target = target;
        this.dailyEntries = new ArrayList<>();
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public String getTaskDescription() {
        return taskDescription;
    }

    @Override
    public double getTarget() {
        return target;
    }

    @Override
    public List<Pair<LocalDate, Double>> getDailyEntries() {
        return dailyEntries;
    }

    @Override
    public void addDailyEntry(LocalDate date, double entry) {
        dailyEntries.add(new Pair<>(date, entry));
    }

    @Override
    public double calculateAverage() {
        if (dailyEntries.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Pair<LocalDate, Double> entry : dailyEntries) {
            sum += entry.second;
        }
        return sum / dailyEntries.size();
    }
}