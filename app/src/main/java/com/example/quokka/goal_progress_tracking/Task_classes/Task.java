package com.example.quokka.goal_progress_tracking.Task_classes;

public class Task {
    private String name;
    private String goal;
    private String timePeriod;
    private String startDate;

    // Constructor
    public Task(String name, String goal, String timePeriod, String startDate) {
        this.name = name;
        this.goal = goal;
        this.timePeriod = timePeriod;
        this.startDate = startDate;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getGoal() {
        return goal;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public String getStartDate() {
        return startDate;
    }
}
