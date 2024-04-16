package com.example.quokka.goal_progress_tracking.goal_page;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.task_templates.Average_template;
import com.example.quokka.goal_progress_tracking.task_templates.NewTaskActivity;
import com.example.quokka.goal_progress_tracking.task_templates.Task;
import com.example.quokka.goal_progress_tracking.task_templates.TaskAdapter;
import com.example.quokka.goal_progress_tracking.task_templates.TaskDetailActivity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GoalDetailsActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener{

    private TaskAdapter taskAdapter;
    private List<Task> tasks = new ArrayList<>();
    private static final int REQUEST_CODE_NEW_TASK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail);

        // Get data from intent
        Intent intent = getIntent();
        String goalName = intent.getStringExtra("goal_name");
        String goalDescription = intent.getStringExtra("goal_description");

        // Display goal details in the activity
        TextView nameTextView = findViewById(R.id.goal_details_name);
        TextView descriptionTextView = findViewById(R.id.goal_details_description);

        nameTextView.setText(goalName);
        descriptionTextView.setText(goalDescription);

        // Initialize RecyclerView and set adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        taskAdapter = new TaskAdapter(this, tasks, this); // Initialize and assign adapter
        recyclerView.setAdapter(taskAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Populate tasks (replace with actual task data)
        Task task1 = new Average_template("Sleep 7 hours each night", "Improve my sleep schedule by tracking my sleeping habits", 7.0);
        task1.addDailyEntry(LocalDate.now(), 6.5); // Add daily entry for today
        task1.addDailyEntry(LocalDate.now().minusDays(1), 7.0); // Add daily entry for yesterday
        tasks.add(task1);

        Task task2 = new Average_template("Take a 30 minute walk", "A healthy habit", 7.0);
        task1.addDailyEntry(LocalDate.now(), 6.5); // Add daily entry for today
        task1.addDailyEntry(LocalDate.now().minusDays(1), 7.0); // Add daily entry for yesterday
        tasks.add(task2);

        // Notify adapter of data changes
        taskAdapter.notifyDataSetChanged();


        // Handle Add New Task button click
        Button addTaskButton = findViewById(R.id.button_add_task);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to NewTaskActivity
                Intent newTaskIntent = new Intent(getApplicationContext(), NewTaskActivity.class);
                startActivity(newTaskIntent);
            }
        });

        // Handle back button click
        ImageView backBtn = findViewById(R.id.img_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to Goal_MainActivity
                Intent intent = new Intent(getApplicationContext(), Goal_MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_NEW_TASK && resultCode == RESULT_OK && data != null) {
            Task newTask = (Task) data.getSerializableExtra("new_task");
            if (newTask != null) {
                tasks.add(newTask);
                taskAdapter.notifyDataSetChanged(); // Notify adapter of data change
            }
        }
    }


    @Override
    public void onTaskClick(Task task) {
        // Handle task item click
        // Launch a new activity to display detailed task information
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra("task_name", task.getTaskName());
        intent.putExtra("task_description", task.getTaskDescription());

        // Convert daily entries to a list of Serializable objects
        ArrayList<Serializable> serializableEntries = new ArrayList<>();
        for (Pair<LocalDate, Double> entry : task.getDailyEntries()) {
            serializableEntries.add(entry.first);
            serializableEntries.add(entry.second);
        }
        intent.putExtra("task_entries", serializableEntries);
        // Pass any additional task-related data as extras
        startActivity(intent);
    }
}