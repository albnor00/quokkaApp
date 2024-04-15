package com.example.quokka.goal_progress_tracking.goal_page;

public class Goal {
    private String name;
    private String description;

    public Goal(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
