package com.example.quokka.goal_progress_tracking.Task_classes;

public class Task implements TaskItem {
    private String name;
    private String taskDescription;
    private String goal;
    private String timePeriod;
    private String startDate;
    private String taskId;

    // Constructor
    public Task(String name, String taskDescription, String goal, String timePeriod, String startDate, String taskId) {
        this.name = name;
        this.taskDescription = taskDescription;
        this.goal = goal;
        this.timePeriod = timePeriod;
        this.startDate = startDate;
        this.taskId = taskId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return taskDescription;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    // Other getters
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

