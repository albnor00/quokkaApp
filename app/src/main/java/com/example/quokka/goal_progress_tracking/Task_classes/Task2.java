package com.example.quokka.goal_progress_tracking.Task_classes;

public class Task2 implements TaskItem {
    private String name;
    private String taskDescription;
    private String startGoal;
    private String endGoal;
    private String startDate;
    private String endDate;
    private String taskId;


    // Constructor
    public Task2(String name, String taskDescription, String startGoal, String endGoal, String startDate, String endDate, String taskId) {
        this.name = name;
        this.taskDescription = taskDescription;
        this.startGoal = startGoal;
        this.endGoal = endGoal;
        this.startDate = startDate;
        this.endDate = endDate;
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

}
