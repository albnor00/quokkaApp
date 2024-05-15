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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.Task_classes.TaskAdapter;
import com.example.quokka.goal_progress_tracking.Task_classes.Task;
import com.example.quokka.goal_progress_tracking.average_task_template.create_new_average_task;
import com.example.quokka.goal_progress_tracking.goal_setup.Question1;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Goal_non_empty_page extends AppCompatActivity {
    private TextView currentDateTextView;
    private ImageButton previousDayButton;
    private ImageButton nextDayButton;
    private ImageView backButton;
    private ImageView addTaskButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private TextView goalTitleTextView;
    private TaskAdapter taskAdapter;


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

        // Get task details from intent
        Intent intent = getIntent();
        if (intent != null) {
            String taskName = intent.getStringExtra("taskName");
            String goal = intent.getStringExtra("goal");
            String timePeriod = intent.getStringExtra("timePeriod");
            String startDate = intent.getStringExtra("startDate");

            // Create a new task with the received details
            Task newTask = new Task(taskName, goal, timePeriod, startDate);

            // Add the new task to the taskList
            List<Task> taskList = new ArrayList<>();
            taskList.add(newTask);

            // Initialize RecyclerView
            RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            taskAdapter = new TaskAdapter(taskList); // taskList is an ArrayList<Task>
            recyclerView.setAdapter(taskAdapter);

            // Notify the adapter of the data change
            taskAdapter.notifyDataSetChanged();
        }


        // Display User Goal Information
        fetchAndDisplayUserData();

        // Set initial date text
        updateDateText();


        // Set click listeners for arrow buttons
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

        // Set click listener for current date text view
        currentDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Set click listener for back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for "add task" button
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), create_new_average_task.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Method to update the displayed date
    private void updateDateText() {
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String dateText = dateFormat.format(calendar.getTime());
        currentDateTextView.setText(dateText);
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
}
