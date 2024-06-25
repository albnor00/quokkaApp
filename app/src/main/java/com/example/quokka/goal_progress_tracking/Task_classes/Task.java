package com.example.quokka.goal_progress_tracking.Task_classes;

public class Task {
    private String name;
    private String description;
    private String goal;
    private String timePeriod;
    private String startDate;
    private String taskId;

    // Constructor
    public Task(String name, String description, String goal, String timePeriod, String startDate, String taskId) {
        this.name = name;
        this.description = description;
        this.goal = goal;
        this.timePeriod = timePeriod;
        this.startDate = startDate;
        this.taskId = taskId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public String getTaskId(){return taskId;}
}