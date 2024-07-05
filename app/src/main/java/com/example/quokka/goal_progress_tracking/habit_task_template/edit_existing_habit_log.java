package com.example.quokka.goal_progress_tracking.habit_task_template;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.average_task_template.average_log;
import com.example.quokka.ui.login.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class edit_existing_habit_log extends AppCompatActivity {
    private Button yes_btn;
    private Button no_btn;
    private EditText notesInput;
    private ImageView deleteLog;
    private habit_log log;
    private int position;
    private String logId;
    private String taskId;
    private int goalValue;

    //Intent variables
    private String taskName;
    private String taskDescription;
    private String goal;
    private String startDate;
    private String dueDate;
    private int taskPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_existing_habit_log);

        // Get the log ID and position from the intent
        fetchIntentData();

        ImageView backButton = findViewById(R.id.img_back);
        notesInput = findViewById(R.id.edit_text_notes_input);
        deleteLog = findViewById(R.id.delete_button);
        yes_btn = findViewById(R.id.button_yes);
        no_btn = findViewById(R.id.button_no);

        // Fetch the log details using the log ID
        fetchLogDetails(logId, taskId);

        // Fetch the goal for the task
        fetchGoalForTask(taskId);

        // Set click listener for the delete button
        deleteLog.setOnClickListener(v -> {
            // Show confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Log");
            builder.setMessage("Are you sure you want to delete this log?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Delete the log
                    deleteLog(logId);
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        });

        // Handle back button click
        backButton.setOnClickListener(v -> {
            sendIntentData(habit_task_log_history_page.class);
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

        yes_btn.setOnClickListener(v -> {
            saveLog(logId);
            finish();
        });

        no_btn.setOnClickListener(v -> {
            resetLog(logId);
            sendIntentData(habit_task_log_history_page.class);
            finish();
        });

    }

    private void fetchIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra("taskId");
            logId = intent.getStringExtra("logId");
            position = intent.getIntExtra("position", -1);
            taskName = intent.getStringExtra("taskName");
            taskDescription = intent.getStringExtra("taskDescription");
            goal = intent.getStringExtra("goal");
            startDate = intent.getStringExtra("startDate");
            dueDate = intent.getStringExtra("dueDate");
            taskPosition = intent.getIntExtra("taskPosition", -1);
        }
    }

    private void sendIntentData(Class<?> destination) {
        Intent intent = new Intent(getApplicationContext(), destination);
        intent.putExtra("taskName", taskName);
        intent.putExtra("taskDescription", taskDescription);
        intent.putExtra("goal", goal);
        intent.putExtra("startDate", startDate);
        intent.putExtra("dueDate", dueDate);
        intent.putExtra("taskPosition", taskPosition);
        intent.putExtra("taskId", taskId);
        startActivity(intent);
    }

    private void fetchLogDetails(String logId, String taskId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        Log.d(TAG, "Fetching log details for logId: " + logId + ", taskId: " + taskId);

        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks").collection("habit_tasks")
                .document(taskId).collection("loggedLogs")
                .document(logId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        log = documentSnapshot.toObject(habit_log.class);
                        if (log != null) {
                            Log.d(TAG, "Log details fetched successfully: " + log.toString());
                            notesInput.setText(log.getNote());

                        } else {
                            Log.e(TAG, "Log object is null");
                        }
                    } else {
                        Log.d(TAG, "Log not found in Firestore");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching log details: " + e.getMessage()));
    }

    private void fetchGoalForTask(String taskId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        Log.d(TAG, "Fetching goal for taskId: " + taskId);

        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks").collection("habit_tasks")
                .document(taskId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String goalString = documentSnapshot.getString("goal");
                        if (goalString != null) {
                            try {
                                goalValue = Integer.parseInt(goalString);
                                Log.d(TAG, "Goal fetched successfully: " + goal);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing goal string to integer: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Goal string is null");
                        }
                    } else {
                        Log.d(TAG, "Task not found in Firestore");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching goal: " + e.getMessage()));
    }

    private void saveLog(String logId) {
        // Get input values
        String notes = notesInput.getText().toString().trim();

        // Create a new log object with the updated value and note
        habit_log updatedLog = new habit_log(logId, goalValue, notes, log.getDate());

        // Update log in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks").collection("habit_tasks")
                .document(taskId).collection("loggedLogs")
                .document(logId)
                .set(updatedLog)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Log updated successfully in Firestore");
                    // Pass the updated log data back to the previous activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newLog", updatedLog); // Include the updated log
                    resultIntent.putExtra("position", position); // Include the position
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish(); // Finish the activity immediately after setting the result
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating log: " + e.getMessage());
                    Toast.makeText(this, "Failed to update log. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void resetLog(String logId) {
        // Get input values
        String notes = notesInput.getText().toString().trim();

        // Create a new log object with the updated value and note
        habit_log updatedLog = new habit_log(logId, 0, notes, log.getDate());

        // Update log in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks").collection("habit_tasks")
                .document(taskId).collection("loggedLogs")
                .document(logId)
                .set(updatedLog)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Log updated successfully in Firestore");
                    // Pass the updated log data back to the previous activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newLog", updatedLog); // Include the updated log
                    resultIntent.putExtra("position", position); // Include the position
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish(); // Finish the activity immediately after setting the result
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating log: " + e.getMessage());
                    Toast.makeText(this, "Failed to update log. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteLog(String logId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks").collection("habit_tasks")
                .document(taskId).collection("loggedLogs")
                .document(logId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Log deleted successfully from Firestore");
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("position", position); // Include the position
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting log: " + e.getMessage());
                    Toast.makeText(this, "Failed to delete log. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
