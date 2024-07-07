package com.example.quokka.goal_progress_tracking.average_task_template;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_setup.Question2;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class average_task_log_history_page extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView previousLogsTextView;
    private List<average_log> previousLogsList;
    private AverageLogAdapter adapter;
    private int LOAD_FLAG = 1;

    //Intent variables
    private String taskName;
    private String taskDescription;
    private String goal;
    private String dueDate;
    private String startDate;
    private int taskPosition;
    private String taskId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_log_history_page);

        // Initialize views
        ImageView backButton = findViewById(R.id.img_back);
        recyclerView = findViewById(R.id.recycler_view_previous_logs);
        previousLogsTextView = findViewById(R.id.previous_logs);
        CardView cardLayout = findViewById(R.id.card_layout);

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Populate RecyclerView with initial data
        previousLogsList = new ArrayList<>();

        // Create adapter and set it to RecyclerView
        adapter = new AverageLogAdapter(previousLogsList);
        recyclerView.setAdapter(adapter);

        // Get task information from intent
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra("taskId");
            taskName = intent.getStringExtra("taskName");
            taskDescription = intent.getStringExtra("taskDescription");
            goal = intent.getStringExtra("goal");
            dueDate = intent.getStringExtra("dueDate");
            startDate = intent.getStringExtra("startDate");
            taskPosition = intent.getIntExtra("taskPosition", -1);
        }


        // Handle back button click
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous activity
                Intent intent = new Intent(getApplicationContext(), average_task_page.class);
                intent.putExtra("taskName", taskName);
                intent.putExtra("taskDescription", taskDescription);
                intent.putExtra("goal", goal);
                intent.putExtra("dueDate", dueDate);
                intent.putExtra("startDate", startDate);

                // Pass the position of the clicked task
                intent.putExtra("taskPosition", taskPosition);
                intent.putExtra("taskId", taskId);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the card layout
        cardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to the add_new_average_log activity
                Intent intent = new Intent(getApplicationContext(), add_new_average_log.class);

                intent.putExtra("taskName", taskName);
                intent.putExtra("taskDescription", taskDescription);
                intent.putExtra("goal", goal);
                intent.putExtra("dueDate", dueDate);
                intent.putExtra("startDate", startDate);

                // Pass the position of the clicked task
                intent.putExtra("taskPosition", taskPosition);
                intent.putExtra("taskId", taskId);

                startActivityForResult(intent, RequestCodes.ADD_LOG_REQUEST_CODE);
            }
        });

        // Set item click listener for RecyclerView items
        adapter.setOnItemClickListener(new AverageLogAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Redirect to the edit_existing_average_log activity
                average_log log = previousLogsList.get(position);
                Intent intent = new Intent(getApplicationContext(), edit_existing_average_log.class);
                intent.putExtra("logId", log.getId()); // Pass the log ID
                intent.putExtra("taskId", taskId); // Pass the task ID
                intent.putExtra("position", position);
                startActivityForResult(intent, RequestCodes.EDIT_LOG_REQUEST_CODE);
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
                    MainActivity.checkUserRole2(user, average_task_log_history_page.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
    }

    private void loadLogsFromFirestore() {
        // Clear the previous logs list to avoid duplication
        previousLogsList.clear();

        // Get the Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current user's ID (you need to implement this part)
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String userId = auth.getCurrentUser().getUid(); // Get the current user's ID

        // Query Firestore to fetch the logs for the current user
        db.collection("users").document(userId)
                .collection("Goal").document("averageTasks").collection("average_tasks")
                .document(taskId).collection("loggedLogs")
                .orderBy("date", Query.Direction.DESCENDING) // Ensure logs are ordered by date
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        // Convert each document to an average_log object
                        average_log log = documentSnapshot.toObject(average_log.class);
                        log.setId(documentSnapshot.getId()); // Set the document ID as the log ID
                        // Add the log to the list
                        previousLogsList.add(log);
                    }
                    // Notify the adapter that the data set has changed
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Error occurred while fetching logs
                    Toast.makeText(this, "Error fetching logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LOAD_FLAG == 1) {
            loadLogsFromFirestore();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RequestCodes.ADD_LOG_REQUEST_CODE:
                    if (data != null && data.hasExtra("newLog")) {
                        average_log newLog = (average_log) data.getSerializableExtra("newLog");
                        previousLogsList.add(0, newLog); // Add to the top of the list
                        adapter.notifyItemInserted(0);
                        recyclerView.scrollToPosition(0); // Scroll to the top
                    }
                    break;
                case RequestCodes.EDIT_LOG_REQUEST_CODE:
                    if (data != null && data.hasExtra("newLog") && data.hasExtra("position")) {
                        average_log updatedLog = (average_log) data.getSerializableExtra("newLog");
                        int position = data.getIntExtra("position", -1);

                        if (position != -1 && position < previousLogsList.size()) {
                            previousLogsList.set(position, updatedLog); // Update the log at the given position
                            adapter.notifyItemChanged(position);
                        } else {
                            Log.e(TAG, "Invalid position: " + position + ", list size: " + previousLogsList.size());
                        }
                    }
                    break;
                case RequestCodes.DELETE_LOG_REQUEST_CODE:
                    if (data != null && data.hasExtra("position")) {
                        int position = data.getIntExtra("position", -1);

                        if (position != -1 && position < previousLogsList.size()) {
                            previousLogsList.remove(position); // Remove the log at the given position
                            adapter.notifyItemRemoved(position);
                        } else {
                            Log.e(TAG, "Invalid position: " + position + ", list size: " + previousLogsList.size());
                        }
                    }
                    break;
            }
        }
    }

}


