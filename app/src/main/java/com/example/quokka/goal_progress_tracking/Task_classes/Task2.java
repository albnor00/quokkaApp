package com.example.quokka.goal_progress_tracking.Task_classes;

public class Task2 {

    private String name;
    private String description;
    private String startGoal;
    private String endGoal;
    private String startDate;
    private String endDate;
    private String taskId;

    // Constructor
    public Task2(String name, String description, String startGoal, String endGoal, String startDate, String endDate, String taskId) {
        this.name = name;
        this.description = description;
        this.startGoal = startGoal;
        this.endGoal = endGoal;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskId = taskId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    public String getStartGoal() {
        return startGoal;
    }
    public String getEndGoal() {
        return endGoal;
    }

    public String getStartDate() {
        return startDate;
    }
    public String getEndDate() {
        return endDate;
    }

    public String getTaskId(){return taskId;}
}
