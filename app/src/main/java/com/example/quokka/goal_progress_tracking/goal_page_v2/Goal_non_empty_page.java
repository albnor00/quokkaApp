package com.example.quokka.goal_progress_tracking.goal_page_v2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.Task_classes.Task;
import com.example.quokka.goal_progress_tracking.Task_classes.Task2;
import com.example.quokka.goal_progress_tracking.Task_classes.TaskAdapter;
import com.example.quokka.goal_progress_tracking.Task_classes.TaskItem;
import com.example.quokka.goal_progress_tracking.average_task_template.average_task_page;
import com.example.quokka.goal_progress_tracking.target_task_template.target_task_page;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Goal_non_empty_page extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {
    private TextView currentDateTextView;
    private ImageButton previousDayButton;
    private ImageButton nextDayButton;
    private ImageView backButton;
    private ImageView addTaskButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private TextView goalTitleTextView;
    private TaskAdapter taskAdapter;
    private List<TaskItem> taskList;

    // Intent variables
    private String taskName;
    private String taskDescription;
    private String goal;
    private String timePeriod;
    private String startDate;
    private int taskPosition;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_empty_goal_page);

        // Find the included layout (CardView)
        View overview_box = findViewById(R.id.task_progress_overview);

        // Find the TextView inside the included layout
        goalTitleTextView = overview_box.findViewById(R.id.textView);

        // Initialize other views
        currentDateTextView = findViewById(R.id.text_current_date);
        previousDayButton = findViewById(R.id.btn_previous_day);
        nextDayButton = findViewById(R.id.btn_next_day);
        backButton = findViewById(R.id.img_back);
        addTaskButton = findViewById(R.id.add_task_btn);

        // Initialize calendar
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());

        // Initialize task list and adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Set click listener for tasks
        taskAdapter.setOnTaskClickListener(this);

        // Notify the adapter of the data change
        taskAdapter.notifyDataSetChanged();

        // Get task details from intent
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra("taskId");
            taskName = intent.getStringExtra("taskName");
            taskDescription = intent.getStringExtra("taskDescription");
            goal = intent.getStringExtra("goal");
            timePeriod = intent.getStringExtra("timePeriod");
            startDate = intent.getStringExtra("startDate");
            taskPosition = intent.getIntExtra("taskPosition", -1);
        }

        // Display User Goal Information
        fetchAndDisplayUserData();

        // Initialize current date
        updateDateText();

        // Set click listeners for date navigation buttons
        previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                updateDateText();
            }
        });

        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                updateDateText();
            }
        });

        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Add task button click listener
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show Date Picker Dialog
                Intent intent = new Intent(getApplicationContext(), choose_task_template.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onTaskClick(int position) {
        // Get the task at the clicked position
        TaskItem clickedTask = taskList.get(position);

        if (clickedTask instanceof Task) {
            Task task = (Task) clickedTask;
            // Create an intent to start the average_task_page activity
            Intent intent = new Intent(getApplicationContext(), average_task_page.class);
            // Pass necessary data to the average_task_page activity
            intent.putExtra("taskId", task.getTaskId()); // Pass taskId
            intent.putExtra("taskPosition", position);
            intent.putExtra("taskName", task.getName());
            intent.putExtra("taskDescription", task.getDescription());
            intent.putExtra("goal", task.getGoal());
            intent.putExtra("timePeriod", task.getTimePeriod());
            intent.putExtra("startDate", task.getStartDate());
            // Start the activity
            startActivity(intent);
        } else if (clickedTask instanceof Task2) {
            Task2 task2 = (Task2) clickedTask;
            // Create an intent to start the target_task_page activity
            Intent intent = new Intent(getApplicationContext(), target_task_page.class);
            // Pass necessary data to the target_task_page activity
            intent.putExtra("taskId", task2.getTaskId()); // Pass taskId
            intent.putExtra("taskPosition", position);
            intent.putExtra("taskName", task2.getName());
            intent.putExtra("taskDescription", task2.getDescription());
            intent.putExtra("startGoal", task2.getStartGoal());
            intent.putExtra("endGoal", task2.getEndGoal());
            intent.putExtra("startDate", task2.getStartDate());
            intent.putExtra("endDate", task2.getEndDate());
            // Start the activity
            startActivity(intent);
        }
    }

    // Method to load tasks from Firestore
    private void loadTasksFromFirestore() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            taskList.clear();

            // Query average tasks from Firestore
            db.collection("users").document(userId)
                    .collection("Goal").document("averageTasks").collection("average_tasks")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String taskName = documentSnapshot.getString("name");
                            String taskDescription = documentSnapshot.getString("taskDescription");
                            String goal = documentSnapshot.getString("goal");
                            String timePeriod = documentSnapshot.getString("timePeriod");
                            String startDate = documentSnapshot.getString("startDate");
                            String taskId = documentSnapshot.getString("taskId");

                            Task task = new Task(taskName, taskDescription, goal, timePeriod, startDate, taskId);
                            taskList.add(task);
                        }
                        taskAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load average tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            // Query target tasks from Firestore
            db.collection("users").document(userId)
                    .collection("Goal").document("targetTasks").collection("target_tasks")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String taskName = documentSnapshot.getString("name");
                            String taskDescription = documentSnapshot.getString("taskDescription");
                            String startGoal = documentSnapshot.getString("startGoal");
                            String endGoal = documentSnapshot.getString("endGoal");
                            String startDate = documentSnapshot.getString("startDate");
                            String endDate = documentSnapshot.getString("endDate");
                            String taskId = documentSnapshot.getString("taskId");

                            Task2 task2 = new Task2(taskName, taskDescription, startGoal, endGoal, startDate, endDate, taskId);
                            taskList.add(task2);
                        }
                        taskAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load target tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Method to show date picker dialog
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateText();
                    }
                },
                calendar.get(Calendar.YEAR), // Initial year
                calendar.get(Calendar.MONTH), // Initial month
                calendar.get(Calendar.DAY_OF_MONTH) // Initial day
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadTasksFromFirestore();

        // Update the calendar to current date when activity resumes (e.g., coming back from another activity)
        calendar = Calendar.getInstance();
        updateDateText();
    }

    // Method to fetch and display user's goal data
    private void fetchAndDisplayUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Retrieve previously selected aspect from Firestore
            db.collection("users").document(userId).collection("Goal")
                    .document("Q1_user_aspect")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userAspect = documentSnapshot.getString("aspectName");
                            if (userAspect != null) {
                                updateTextViews(userAspect);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Goal_non_empty_page.this, "Failed to fetch previous aspect: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Method to update TextViews with fetched data
    private void updateTextViews(String goalTitle) {
        // Update the TextView with the retrieved goal title
        goalTitleTextView.setText(goalTitle);
    }

    // Method to update date text
    private void updateDateText() {
        String currentDate = dateFormat.format(calendar.getTime());
        currentDateTextView.setText(currentDate);
    }
}