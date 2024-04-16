package com.example.quokka.goal_progress_tracking.task_templates;
import com.example.quokka.goal_progress_tracking.goal_page.GoalDetailsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NewTaskActivity extends AppCompatActivity {

    private EditText editTextTaskName;
    private EditText editTextTaskDescription;
    private Spinner spinnerTaskTemplate;
    private Button buttonCreateTask;
    private List<Task> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        // Initialize views
        editTextTaskName = findViewById(R.id.edit_text_task_name);
        editTextTaskDescription = findViewById(R.id.edit_text_task_description);
        spinnerTaskTemplate = findViewById(R.id.spinner_task_template);
        buttonCreateTask = findViewById(R.id.button_create_task);

        // Populate spinner with task template options
        String[] taskTemplates = {"Average Template", "Other Template", "Custom Template"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, taskTemplates);
        spinnerTaskTemplate.setAdapter(adapter);

        // Set click listener for create task button
        buttonCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = editTextTaskName.getText().toString();
                String taskDescription = editTextTaskDescription.getText().toString();
                String selectedTemplate = spinnerTaskTemplate.getSelectedItem().toString();

                Task newTask = null;
                switch (selectedTemplate) {
                    case "Average Template":
                        newTask = new Average_template(taskName, taskDescription, 0.0);
                        break;
                    // Add cases for other templates

                    default:
                        break;
                }

                if (newTask != null) {
                    returnTaskToGoalDetails(newTask);
                }
            }
        });
    }

    private void returnTaskToGoalDetails(Task newTask) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_task", (Serializable) newTask);
        setResult(RESULT_OK, resultIntent);
        finish(); // Finish this activity and return to the previous activity (GoalDetailsActivity)
    }

    private void createNewTask(String taskName, String taskDescription, String selectedTemplate) {
        Task task2 = new Average_template("Task 2", "Task 2 description", 5.0);
        task2.addDailyEntry(LocalDate.now(), 4.5); // Add daily entry for today
        tasks.add(task2);
    }

    private void navigateToGoalMainActivity() {
        Intent intent = new Intent(this, GoalDetailsActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity to prevent returning to NewTaskActivity
    }
}