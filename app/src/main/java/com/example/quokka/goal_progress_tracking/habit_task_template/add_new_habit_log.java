package com.example.quokka.goal_progress_tracking.habit_task_template;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.average_task_template.average_log;
import com.example.quokka.goal_progress_tracking.average_task_template.average_task_log_history_page;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class add_new_habit_log extends AppCompatActivity {

    private EditText numericInput;
    private EditText notesInput;
    private static final int ADD_LOG_REQUEST_CODE = 1;

    //Intent variables
    private String taskName;
    private String taskDescription;
    private String goal;
    private String startDate;
    private String dueDate;
    private int taskPosition;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_habit_log);

        // Get task information from intent
        fetchIntentData();

        // Initialize views
        ImageView backButton = findViewById(R.id.img_back);
        ImageView finishButton = findViewById(R.id.img_check_mark);
        numericInput = findViewById(R.id.edit_text_numeric_input);
        notesInput = findViewById(R.id.edit_text_notes_input);


        // Handle back button click
        backButton.setOnClickListener(v -> {
            sendResultData();
        });

        finishButton.setOnClickListener(v -> {
            if (validateInput()) {
                createNewLogForToday();
            }
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

    private void fetchIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra("taskId");
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

    private void sendResultData() {
        Intent intent = new Intent();
        intent.putExtra("taskName", taskName);
        intent.putExtra("taskDescription", taskDescription);
        intent.putExtra("goal", goal);
        intent.putExtra("startDate", startDate);
        intent.putExtra("dueDate", dueDate);
        intent.putExtra("taskPosition", taskPosition);
        intent.putExtra("taskId", taskId);
        setResult(Activity.RESULT_OK, intent);
        finish(); // Finish the activity without starting a new one
    }

    private void createNewLogForToday() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        String logId = UUID.randomUUID().toString();
        int numericValue = Integer.parseInt(numericInput.getText().toString().trim());
        String notes = notesInput.getText().toString();

        // Get the current date
        Date todayDate = new Date();

        habit_log newLog = new habit_log(logId, numericValue, notes, todayDate);

        db.collection("users")
                .document(userId)
                .collection("Goal")
                .document("habitTasks")
                .collection("habit_tasks")
                .document(taskId)
                .collection("loggedLogs")
                .document(logId)
                .set(newLog)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Log created successfully", Toast.LENGTH_SHORT).show();
                    sendResultData();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error creating log", e);
                    Toast.makeText(this, "Error creating log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

}


