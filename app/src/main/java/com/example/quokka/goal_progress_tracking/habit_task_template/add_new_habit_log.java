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

    private Button yes_btn;
    private Button no_btn;
    private EditText notesInput;
    private static final int ADD_LOG_REQUEST_CODE = 1;

    //Intent variables
    private String taskName;
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
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra("taskId");
            taskName = intent.getStringExtra("taskName");
            goal = intent.getStringExtra("goal");
            startDate = intent.getStringExtra("startDate");
            dueDate = intent.getStringExtra("dueDate");
            taskPosition = intent.getIntExtra("taskPosition", -1);
        }

        // Initialize views
        ImageView backButton = findViewById(R.id.img_back);
        notesInput = findViewById(R.id.edit_text_notes_input);
        yes_btn = findViewById(R.id.button_yes);
        no_btn = findViewById(R.id.button_no);


        // Handle back button click
        backButton.setOnClickListener(v -> {
            // Navigate back to the previous activity
            Intent intent2 = new Intent(getApplicationContext(), habit_task_log_history_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent2.putExtra("taskName", taskName);
            intent2.putExtra("goal", goal);
            intent2.putExtra("startDate", startDate);
            intent2.putExtra("dueDate", dueDate);

            // Pass the position of the clicked task
            intent2.putExtra("taskPosition", taskPosition);
            intent2.putExtra("taskId", taskId);

            startActivity(intent2);
            finish();
        });

        yes_btn.setOnClickListener(v -> {
            updateOrCreateLogForToday();
            Intent intent2 = new Intent(getApplicationContext(), habit_task_log_history_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent2.putExtra("taskName", taskName);
            intent2.putExtra("goal", goal);
            intent2.putExtra("startDate", startDate);
            intent2.putExtra("dueDate", dueDate);

            // Pass the position of the clicked task
            intent2.putExtra("taskPosition", taskPosition);
            intent2.putExtra("taskId", taskId);

            startActivity(intent2);
            finish();
        });

        no_btn.setOnClickListener(v -> {
            // Navigate back to the previous activity
            Intent intent2 = new Intent(getApplicationContext(), habit_task_log_history_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent2.putExtra("taskName", taskName);
            intent2.putExtra("goal", goal);
            intent2.putExtra("startDate", startDate);
            intent2.putExtra("dueDate", dueDate);

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
    }


    private void updateOrCreateLogForToday() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Define the start and end of today
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startOfDay = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfDay = calendar.getTime();

        // Query for logs created between startOfDay and endOfDay
        Query query = db.collection("users")
                .document(userId)
                .collection("Goal")
                .document("habitTasks")
                .collection("habit_tasks")
                .document(taskId)
                .collection("loggedLogs")
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Log exists for today's date, update it
                        DocumentSnapshot logSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        incrementExistingLog(logSnapshot);
                    } else {
                        // No log exists for today's date, create a new log
                        createNewLogForToday(today);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking for existing logs", e);
                    Toast.makeText(this, "Error checking for existing logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void incrementExistingLog(DocumentSnapshot logSnapshot) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        String logId = logSnapshot.getId();
        habit_log existingLog = logSnapshot.toObject(habit_log.class);

        if (existingLog != null) {
            existingLog.setLog(existingLog.getLog() + 1);
            existingLog.setNote(notesInput.getText().toString());

            db.collection("users")
                    .document(userId)
                    .collection("Goal")
                    .document("habitTasks")
                    .collection("habit_tasks")
                    .document(taskId)
                    .collection("loggedLogs")
                    .document(logId)
                    .set(existingLog)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Log updated successfully", Toast.LENGTH_SHORT).show();
                        navigateBack();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error updating log", e);
                        Toast.makeText(this, "Error updating log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Firestore", "Existing log is null");
        }
    }

    private void createNewLogForToday(Date todayDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        String logId = UUID.randomUUID().toString();
        String notes = notesInput.getText().toString();

        habit_log newLog = new habit_log(logId, 1, notes, new Date());
        newLog.setDateString(todayDate); // Set the dateString field

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
                    navigateBack();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error creating log", e);
                    Toast.makeText(this, "Error creating log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void navigateBack() {
        // Navigate back to the previous activity
        Intent intent = new Intent(getApplicationContext(), habit_task_log_history_page.class);
        intent.putExtra("taskId", taskId);
        startActivity(intent);
        finish();
    }


}


