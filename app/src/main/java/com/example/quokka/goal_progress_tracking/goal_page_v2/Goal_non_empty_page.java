package com.example.quokka.goal_progress_tracking.goal_page_v2;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.Task_classes.Task;
import com.example.quokka.goal_progress_tracking.Task_classes.Task2;
import com.example.quokka.goal_progress_tracking.Task_classes.Task3;
import com.example.quokka.goal_progress_tracking.Task_classes.TaskAdapter;
import com.example.quokka.goal_progress_tracking.Task_classes.TaskItem;
import com.example.quokka.goal_progress_tracking.average_task_template.average_log;
import com.example.quokka.goal_progress_tracking.average_task_template.average_task_page;
import com.example.quokka.goal_progress_tracking.average_task_template.create_new_average_task;
import com.example.quokka.goal_progress_tracking.goal_setup.Question2;
import com.example.quokka.goal_progress_tracking.habit_task_template.habit_log;
import com.example.quokka.goal_progress_tracking.habit_task_template.habit_task_page;
import com.example.quokka.goal_progress_tracking.target_task_template.target_log;
import com.example.quokka.goal_progress_tracking.target_task_template.target_task_page;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
    private ProgressBar progressBar;
    private TextView percentageView;
    private TaskAdapter taskAdapter;
    private List<TaskItem> taskList;
    private int completedTasks = 0;
    private int totalTasks = 0;

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
        progressBar = overview_box.findViewById(R.id.progressBar);
        percentageView = overview_box.findViewById(R.id.percentageView);

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

        checkLogsAndUpdateProgressBar();

        overview_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Goal_overview.class);
                startActivity(intent);
                finish();
            }
        });

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

        currentDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), tasksMain.class);
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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home_bottom) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else if (item.getItemId() == R.id.profile_bottom) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else if (item.getItemId() == R.id.tasks_bottom) {
                    startActivity(new Intent(getApplicationContext(), tasksMain.class));
                } else if (item.getItemId() == R.id.group_bottom) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    MainActivity.checkUserRole2(user, Goal_non_empty_page.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });

        ImageView tooltip = findViewById(R.id.img_help);
        tooltip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Goal_non_empty_page.this);
                builder.setTitle("The Goal Page");
                builder.setMessage("This is the Goal page. In this page you can create and oversee the tasks you have created for this goal. To create a new task, press the plus sign in the bottom right corner. " +
                        "Once the task is created it will show up in the list below the date picker. To oversee your Goal and possible reset it, press the box at the top of the page.");
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
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
            intent.putExtra("dueDate", task.getDueDate());
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
            intent.putExtra("dueDate", task2.getDueDate());
            // Start the activity
            startActivity(intent);
        } else if (clickedTask instanceof Task3) {
            Task3 task3 = (Task3) clickedTask;
            // Create an intent to start the average_task_page activity
            Intent intent = new Intent(getApplicationContext(), habit_task_page.class);
            // Pass necessary data to the average_task_page activity
            intent.putExtra("taskId", task3.getTaskId()); // Pass taskId
            intent.putExtra("taskPosition", position);
            intent.putExtra("taskName", task3.getName());
            intent.putExtra("taskDescription", task3.getDescription());
            intent.putExtra("goal", task3.getGoal());
            intent.putExtra("dueDate", task3.getDueDate());
            intent.putExtra("startDate", task3.getStartDate());
            // Start the activity
            startActivity(intent);
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
                            String dueDate = documentSnapshot.getString("dueDate");
                            String startDate = documentSnapshot.getString("startDate");
                            String taskId = documentSnapshot.getString("taskId");

                            Task task = new Task(taskName, taskDescription, goal, dueDate, startDate, taskId);
                            taskList.add(task);
                        }
                        taskAdapter.notifyDataSetChanged();
                        checkLogsAndUpdateProgressBar();
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
                            String dueDate = documentSnapshot.getString("dueDate");
                            String taskId = documentSnapshot.getString("taskId");

                            Task2 task2 = new Task2(taskName, taskDescription, startGoal, endGoal, startDate, endDate, dueDate, taskId);
                            taskList.add(task2);
                        }
                        taskAdapter.notifyDataSetChanged();
                        checkLogsAndUpdateProgressBar();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load target tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            // Query habit tasks from Firestore
            db.collection("users").document(userId)
                    .collection("Goal").document("habitTasks").collection("habit_tasks")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String taskName = documentSnapshot.getString("name");
                            String taskDescription = documentSnapshot.getString("taskDescription");
                            String goal = documentSnapshot.getString("goal");
                            String timePeriod = documentSnapshot.getString("timePeriod");
                            String startDate = documentSnapshot.getString("startDate");
                            String dueDate = documentSnapshot.getString("dueDate");
                            String taskId = documentSnapshot.getString("taskId");

                            Task3 task3 = new Task3(taskName, taskDescription, goal, startDate, dueDate, taskId);
                            taskList.add(task3);
                        }
                        taskAdapter.notifyDataSetChanged();
                        checkLogsAndUpdateProgressBar();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load habit tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
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


    private void checkLogsAndUpdateProgressBar() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        totalTasks = taskList.size(); // Update totalTasks with the size of the task list
        completedTasks = 0; // Reset the completedTasks counter

        Log.d("ProgressBar", "Current Date: " + currentDate);
        Log.d("ProgressBar", "Total tasks: " + totalTasks);

        for (TaskItem taskItem : taskList) {
            if (taskItem instanceof Task) {
                Task task = (Task) taskItem;
                Log.d("ProgressBar", "Checking Task: " + task.getTaskId());

                db.collection("users").document(userId)
                        .collection("Goal").document("averageTasks").collection("average_tasks")
                        .document(task.getTaskId())
                        .collection("loggedLogs").whereEqualTo("date", currentDate)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                average_log log = documentSnapshot.toObject(average_log.class);
                                if (log != null && log.getLog() > 0) {
                                    synchronized (this) {
                                        completedTasks++;
                                        Log.d("ProgressBar", "Task " + task.getTaskId() + " completed. Total completed: " + completedTasks);
                                        Log.d("ProgressBar", "Task Log value: " + log.getLog());
                                    }
                                }
                            }
                            updateProgressBar(); // Update progress bar after checking each task
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProgressBar", "Failed to load logs for task " + task.getTaskId() + ": " + e.getMessage());
                            Toast.makeText(this, "Failed to load logs for task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            updateProgressBar(); // Ensure progress bar is updated even on failure
                        });
            } else if (taskItem instanceof Task2) {
                Task2 task2 = (Task2) taskItem;
                Log.d("ProgressBar", "Checking Task2: " + task2.getTaskId());

                db.collection("users").document(userId)
                        .collection("Goal").document("targetTasks").collection("target_tasks")
                        .document(task2.getTaskId())
                        .collection("loggedLogs").whereEqualTo("date", currentDate)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                target_log log = documentSnapshot.toObject(target_log.class);
                                if (log != null && log.getLog() > 0) {
                                    synchronized (this) {
                                        completedTasks++;
                                        Log.d("ProgressBar", "Task2 " + task2.getTaskId() + " completed. Total completed: " + completedTasks);
                                        Log.d("ProgressBar", "Task2 Log value: " + log.getLog());
                                    }
                                }
                            }
                            updateProgressBar(); // Update progress bar after checking each task
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProgressBar", "Failed to load logs for task " + task2.getTaskId() + ": " + e.getMessage());
                            Toast.makeText(this, "Failed to load logs for task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            updateProgressBar(); // Ensure progress bar is updated even on failure
                        });
            } else if (taskItem instanceof Task3) {
                Task3 task3 = (Task3) taskItem;
                Log.d("ProgressBar", "Checking Task3: " + task3.getTaskId());

                db.collection("users").document(userId)
                        .collection("Goal").document("habitTasks").collection("habit_tasks")
                        .document(task3.getTaskId())
                        .collection("loggedLogs").whereEqualTo("date", currentDate)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                habit_log log = documentSnapshot.toObject(habit_log.class);
                                if (log != null && log.getLog() > 0) {
                                    synchronized (this) {
                                        completedTasks++;
                                        Log.d("ProgressBar", "Task3 " + task3.getTaskId() + " completed. Total completed: " + completedTasks);
                                        Log.d("ProgressBar", "Task3 Log value: " + log.getLog());
                                    }
                                }
                            }
                            updateProgressBar(); // Update progress bar after checking each task
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProgressBar", "Failed to load logs for task " + task3.getTaskId() + ": " + e.getMessage());
                            Toast.makeText(this, "Failed to load logs for task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            updateProgressBar(); // Ensure progress bar is updated even on failure
                        });
            }
        }
    }

    private synchronized void updateProgressBar() {
        if (totalTasks > 0) {
            int progress = (int) ((double) completedTasks / totalTasks * 100);
            progressBar.setProgress(progress);
            percentageView.setText(progress + "%");

            Log.d("ProgressBar", "Progress updated: " + progress + "%");
        } else {
            progressBar.setProgress(0);
            percentageView.setText("0%");

            Log.d("ProgressBar", "No tasks to update progress.");
        }
    }

}