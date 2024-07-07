package com.example.quokka.goal_progress_tracking.average_task_template;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.Date;
import java.util.UUID;

public class add_new_average_log extends AppCompatActivity {

    private EditText numericInput;
    private EditText notesInput;
    private ImageView checkMark;
    private static final int ADD_LOG_REQUEST_CODE = 1;

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
        setContentView(R.layout.activity_add_new_average_log);

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

        // Initialize views
        ImageView backButton = findViewById(R.id.img_back);
        numericInput = findViewById(R.id.edit_text_numeric_input);
        notesInput = findViewById(R.id.edit_text_notes_input);
        checkMark = findViewById(R.id.img_check_mark);

        // Set click listener for the check mark
        checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate and save input
                if (validateInput()) {
                    Log.d("Firestore", "ID: " + taskId);
                    saveLog(taskId);
                }
            }
        });

        // Handle back button click
        backButton.setOnClickListener(v -> {
            // Navigate back to the previous activity
            Intent intent2 = new Intent(getApplicationContext(), average_task_log_history_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent2.putExtra("taskName", taskName);
            intent2.putExtra("taskDescription", taskDescription);
            intent2.putExtra("goal", goal);
            intent2.putExtra("dueDate", dueDate);
            intent2.putExtra("startDate", startDate);

            // Pass the position of the clicked task
            intent2.putExtra("taskPosition", taskPosition);
            intent2.putExtra("taskId", taskId);

            startActivity(intent2);
            finish();
        });

        notesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No implementation needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Dynamically adjust the height of EditText based on the content
                notesInput.post(() -> {
                    int lineCount = notesInput.getLineCount();
                    if (lineCount > 10) {
                        // Restrict the number of lines to 10
                        notesInput.setLines(10);
                    } else {
                        notesInput.setMinLines(lineCount);
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check and enforce character limit
                int maxLength = 500;
                if (s.length() > maxLength) {
                    notesInput.setText(s.subSequence(0, maxLength));
                    notesInput.setSelection(maxLength);
                    Toast.makeText(getApplicationContext(), "Character limit reached", Toast.LENGTH_SHORT).show();
                }
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
                    MainActivity.checkUserRole2(user, add_new_average_log.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
    }

    private boolean validateInput() {
        String numericValue = numericInput.getText().toString().trim();
        if (numericValue.isEmpty()) {
            Toast.makeText(this, "Please enter a numeric value", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveLog(String taskId) {
        // Get input values
        int numericValue = Integer.parseInt(numericInput.getText().toString().trim());
        String notes = notesInput.getText().toString().trim();

        // Generate a unique ID for the log
        String logId = UUID.randomUUID().toString(); // Using UUID to generate a random ID

        // Create a new log object with the generated ID and current date
        average_log newLog = new average_log(logId, numericValue, notes, new Date()); // Ensure that the date is set correctly

        // Get the Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current user's ID (you need to implement this part)
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String userId = auth.getCurrentUser().getUid(); // Get the current user's ID

        // Add the log to Firestore under the specific task
        db.collection("users").document(userId)
                .collection("Goal").document("averageTasks").collection("average_tasks").document(taskId)
                .collection("loggedLogs") // Assuming this is the collection for logs
                .document(logId) // Use the generated ID as the document ID
                .set(newLog)
                .addOnSuccessListener(aVoid -> {
                    // Log added successfully
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Error occurred while adding log
                    Toast.makeText(this, "Error saving log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



}
