package com.example.quokka.goal_progress_tracking.task_templates;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Average_task_configuration {
    private Average_template template;
    private List<TaskEntry> taskEntries;


    public Average_task_configuration(Average_template template){
        this.template = template;
        this.taskEntries = new ArrayList<>();
    }

    public void addTaskEntry(LocalDate date, double value) {
        TaskEntry entry = new TaskEntry(date, value);
        taskEntries.add(entry);
        template.addDailyEntry(value);
    }

    // Getters and setters
    public List<TaskEntry> getTaskEntries() {
        return taskEntries;
    }

    public Average_template getTemplate() {
        return template;
    }
}
