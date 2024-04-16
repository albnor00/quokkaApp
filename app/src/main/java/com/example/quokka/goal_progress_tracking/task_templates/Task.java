package com.example.quokka.goal_progress_tracking.task_templates;

import android.util.Pair;
import java.time.LocalDate;
import java.util.List;

public interface Task {
    String getTaskName();
    String getTaskDescription();
    double getTarget();
    List<Pair<LocalDate, Double>> getDailyEntries();
    void addDailyEntry(LocalDate date, double entry);
    double calculateAverage();
}
