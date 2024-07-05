package com.example.quokka.goal_progress_tracking.habit_task_template;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.average_task_template.average_task_page;
import com.example.quokka.goal_progress_tracking.average_task_template.average_task_settings_page;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class habit_task_settings_page extends AppCompatActivity {
    private EditText editTextGoal;
    private TextView StartDateTextView;
    private TextView DueDateTextView;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private EditText name_card;
    private EditText description_card;


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
        setContentView(R.layout.activity_habit_settings_page);

        // Initialize views
        ImageView back_btn = findViewById(R.id.img_back);
        ImageView finish_btn = findViewById(R.id.img_check_mark);
        CardView goalCard = findViewById(R.id.edit_goal);
        CardView start_date = findViewById(R.id.edit_startdate);
        CardView due_date = findViewById(R.id.edit_dueDate);
        CardView deleteTaskCard = findViewById(R.id.delete_task);

        name_card = findViewById(R.id.edit_task_name);
        description_card = findViewById(R.id.edit_task_description);
        editTextGoal = findViewById(R.id.editTextGoal);
        StartDateTextView = findViewById(R.id.textView3);
        DueDateTextView = findViewById(R.id.dueDateTextView);

        // Initialize calendar and date format
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());


        fetchIntentData();

        // Set current values to the views
        name_card.setText(taskName);
        description_card.setText(taskDescription);
        editTextGoal.setText(goal);
        StartDateTextView.setText(startDate);
        DueDateTextView.setText(dueDate);

        // Handle finish button click
        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInputValid()) {
                    updateTaskDetails();
                } else {
                    Toast.makeText(habit_task_settings_page.this, "Please enter task name and goal", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendIntentData(habit_task_page.class);
            }
        });

        // Handle card click to show numerical keyboard
        goalCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumericKeyboard();
            }
        });

        // Handle date picker dialog
        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialogStartDate();
            }
        });

        // Setup Switch listener
        due_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDueDateOptionsDialog();
            }
        });

        // Handle task deletion
        deleteTaskCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(habit_task_settings_page.this);
                builder.setTitle("Delete Task");
                builder.setMessage("Are you sure you want to delete this Task? You wont be able to recover the data.");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete the log
                        deleteTaskFromFirestore();
                        Intent intent = new Intent(getApplicationContext(), Goal_non_empty_page.class);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
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

    // Method to show numerical keyboard for editTextGoal
    private void showNumericKeyboard() {
        editTextGoal.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editTextGoal, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // Method to show date picker dialog
    private void showDatePickerDialogStartDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Set selected date to calendar
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Format the selected date
                        String formattedDate = dateFormat.format(calendar.getTime());

                        // Update DateTextView with the formatted date
                        StartDateTextView.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR), // Initial year
                calendar.get(Calendar.MONTH), // Initial month
                calendar.get(Calendar.DAY_OF_MONTH) // Initial day
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

    private void showDueDateOptionsDialog() {
        final CharSequence[] options = {"Every Day", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Due Date");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which].toString();
                DueDateTextView.setText(selectedOption); // Update the TextView with the selected option
            }
        });
        builder.show();
    }

    // Method to update task details in Firestore
    private void updateTaskDetails() {
        // Get updated task details from the EditText and TextView
        String updatedTaskName = name_card.getText().toString();
        String updatedDescription = description_card.getText().toString();
        String updatedGoal = editTextGoal.getText().toString();
        String updatedStartDate = StartDateTextView.getText().toString();
        String updatedDueDate = DueDateTextView.getText().toString();

        // Update task details in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Create a map to hold the updated task details
        Map<String, Object> updatedTask = new HashMap<>();
        updatedTask.put("name", updatedTaskName);
        updatedTask.put("taskDescription", updatedDescription);
        updatedTask.put("goal", updatedGoal);
        updatedTask.put("startDate", updatedStartDate);
        updatedTask.put("dueDate", updatedDueDate);

        // Update the task document in Firestore
        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks").collection("habit_tasks")
                .document(taskId)
                .update(updatedTask)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task details updated successfully
                        Toast.makeText(habit_task_settings_page.this, "Task details updated", Toast.LENGTH_SHORT).show();

                        // Navigate back to the previous activity
                        navigateToPreviousActivity(updatedTaskName, updatedDescription, updatedGoal, updatedStartDate, updatedDueDate);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update task details
                        Toast.makeText(habit_task_settings_page.this, "Failed to update task details", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating task details: " + e.getMessage());
                    }
                });
    }

    // Method to navigate back to the previous activity
    private void navigateToPreviousActivity(String taskName, String taskDescription, String goal, String startDate, String dueDate) {
        Intent intent = new Intent(getApplicationContext(), habit_task_page.class);
        intent.putExtra("taskName", taskName);
        intent.putExtra("taskDescription", taskDescription);
        intent.putExtra("goal", goal);
        intent.putExtra("startDate", startDate);
        intent.putExtra("startDate", dueDate);
        intent.putExtra("taskPosition", taskPosition);
        intent.putExtra("taskId", taskId);
        startActivity(intent);
    }

    private boolean isInputValid() {
        String taskName = name_card.getText().toString().trim();
        String taskDescription = description_card.getText().toString().trim();
        String goal = editTextGoal.getText().toString().trim();
        return !taskName.isEmpty() && !goal.isEmpty() && !taskDescription.isEmpty();
    }

    private void deleteTaskFromFirestore() {
        // Update task details in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();


        // Update the task document in Firestore
        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks").collection("habit_tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task details updated successfully
                        Toast.makeText(habit_task_settings_page.this, "Task details deleted", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update task details
                        Toast.makeText(habit_task_settings_page.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting task: " + e.getMessage());
                    }
                });
    }
}
