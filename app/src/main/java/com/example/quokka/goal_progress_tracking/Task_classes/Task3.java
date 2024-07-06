package com.example.quokka.goal_progress_tracking.Task_classes;

public class Task3 implements TaskItem {
    private String name;
    private String taskDescription;
    private String Goal;
    private String startDate;
    private String dueDate;
    private String taskId;

    // Constructor
    public Task3(String name, String taskDescription, String Goal, String startDate, String dueDate, String taskId) {
        this.name = name;
        this.taskDescription = taskDescription;
        this.Goal = Goal;
        this.startDate = startDate;
        this.dueDate = dueDate;
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
        return Goal;
    }

    public String getStartDate() {
        return startDate;
    }
    public String getDueDate() {
        return dueDate;
    }

}
