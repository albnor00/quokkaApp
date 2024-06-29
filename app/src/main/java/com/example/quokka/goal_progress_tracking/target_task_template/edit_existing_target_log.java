package com.example.quokka.goal_progress_tracking.target_task_template;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class edit_existing_target_log extends AppCompatActivity {
    private EditText numericInput;
    private EditText notesInput;
    private ImageView deleteLog;
    private target_log log;
    private int position;
    private String logId;

    //Intent variables
    private String taskName;
    private String goal;
    private String timePeriod;
    private String startDate;
    private int taskPosition;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_existing_target_log);

        // Initialize views
        ImageView backButton = findViewById(R.id.img_back);
        numericInput = findViewById(R.id.edit_text_numeric_input);
        notesInput = findViewById(R.id.edit_text_notes_input);
        deleteLog = findViewById(R.id.delete_button);

        // Get the log ID and position from the intent
        logId = getIntent().getStringExtra("logId");
        taskId = getIntent().getStringExtra("taskId");
        position = getIntent().getIntExtra("position", -1);

        // Fetch the log details using the log ID
        fetchLogDetails(logId, taskId);


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
            if (validateInput()) {
                saveLog(logId);
            }
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
    }

    private boolean validateInput() {
        String numericValue = numericInput.getText().toString().trim();
        if (numericValue.isEmpty()) {
            Toast.makeText(this, "Please enter a numeric value", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void fetchLogDetails(String logId, String taskId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        Log.d(TAG, "Fetching log details for logId: " + logId + ", taskId: " + taskId);

        db.collection("users").document(userId)
                .collection("Goal").document("targetTasks").collection("target_tasks")
                .document(taskId).collection("loggedLogs")
                .document(logId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        log = documentSnapshot.toObject(target_log.class);
                        if (log != null) {
                            Log.d(TAG, "Log details fetched successfully: " + log.toString());
                            numericInput.setText(String.valueOf(log.getLog()));
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

    private void saveLog(String logId) {
        // Get input values
        int numericValue = Integer.parseInt(numericInput.getText().toString().trim());
        String notes = notesInput.getText().toString().trim();

        // Create a new log object with the updated value and note
        target_log updatedLog = new target_log(logId, numericValue, notes, log.getDate());

        // Update log in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("Goal").document("targetTasks").collection("target_tasks")
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
                .collection("Goal").document("targetTasks").collection("target_tasks")
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
