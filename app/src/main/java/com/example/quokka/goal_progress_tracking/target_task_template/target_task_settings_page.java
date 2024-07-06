package com.example.quokka.goal_progress_tracking.target_task_template;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class target_task_settings_page extends AppCompatActivity {
    private EditText name_card;
    private EditText description_card;
    private EditText editStartTextGoal;
    private EditText editEndTextGoal;
    private TextView StartDateTextView;
    private TextView EndDateTextView;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private TextView reminderTimeTextView;
    private TextView dueDateTextView;

    //Intent variables
    private String taskName;
    private String taskDescription;
    private String startGoal;
    private String endGoal;
    private String startDate;
    private String endDate;
    private int taskPosition;
    private String taskId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_settings_page);

        // Initialize views
        ImageView back_btn = findViewById(R.id.img_back);
        ImageView finish_btn = findViewById(R.id.img_check_mark);
        name_card = findViewById(R.id.edit_task_name);
        description_card = findViewById(R.id.edit_task_description);

        CardView start_value = findViewById(R.id.edit_start_goal);
        CardView end_value = findViewById(R.id.edit_end_goal);
        CardView start_date = findViewById(R.id.edit_start_date);
        CardView due_date = findViewById(R.id.edit_dueDate);
        CardView reminderTime = findViewById(R.id.edit_reminder);
        CardView end_date = findViewById(R.id.edit_end_date);
        CardView deleteTaskCard = findViewById(R.id.task_delete);

        editStartTextGoal = findViewById(R.id.editStartTextGoal);
        editEndTextGoal = findViewById(R.id.editEndTextGoal);
        StartDateTextView = findViewById(R.id.startDateTextView);
        EndDateTextView = findViewById(R.id.endDateTextView);

        dueDateTextView = findViewById(R.id.dueDateTextView);
        reminderTimeTextView = findViewById(R.id.reminderTextView);

        // Initialize calendar and date format
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());


        // Reset calendar to current date for further use
        calendar = Calendar.getInstance();

        // Get task information from intent
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra("taskId");
            taskName = intent.getStringExtra("taskName");
            taskDescription = intent.getStringExtra("taskDescription");
            startGoal = intent.getStringExtra("startGoal");
            endGoal = intent.getStringExtra("endGoal");
            startDate = intent.getStringExtra("startDate");
            endDate = intent.getStringExtra("endDate");
            taskPosition = intent.getIntExtra("taskPosition", -1);
        } else {
            Log.e("Intent Data", "Intent is null");
        }

        fetchDueDateFromFirestore();
        fetchReminderTimeFromFirestore();

        // Set current values to the views
        name_card.setText(taskName);
        description_card.setText(taskDescription);
        editStartTextGoal.setText(startGoal);
        editEndTextGoal.setText(endGoal);
        StartDateTextView.setText(startDate);
        EndDateTextView.setText(endDate);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous activity
                Intent intent = new Intent(getApplicationContext(), target_task_page.class);
                intent.putExtra("taskId", taskId);
                intent.putExtra("taskPosition", taskPosition);
                intent.putExtra("taskName", taskName);
                intent.putExtra("taskDescription", taskDescription);
                intent.putExtra("startGoal", startGoal);
                intent.putExtra("endGoal", endGoal);
                intent.putExtra("startDate", startDate);
                intent.putExtra("endDate", endDate);

                startActivity(intent);
                finish();
            }
        });

        // Handle finish button click
        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInputValid()) {
                    updateTaskDetails();
                } else {
                    Toast.makeText(target_task_settings_page.this, "Please enter task name and goal", Toast.LENGTH_SHORT).show();
                }
            }
        });

        start_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumericKeyboardStartGoal();
            }
        });

        end_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumericKeyboardEndGoal();
            }
        });

        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialogStartDate();
            }
        });

        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialogEndDate();
            }
        });

        // Handle task deletion
        deleteTaskCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(target_task_settings_page.this);
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

        due_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDueDateOptionsDialog();
            }
        });

        reminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
    }

    private void showNumericKeyboardStartGoal() {
        editStartTextGoal.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editStartTextGoal, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void showNumericKeyboardEndGoal() {
        editEndTextGoal.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editEndTextGoal, InputMethodManager.SHOW_IMPLICIT);
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

    // Method to show date picker dialog
    private void showDatePickerDialogEndDate() {
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
                        EndDateTextView.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR), // Initial year
                calendar.get(Calendar.MONTH), // Initial month
                calendar.get(Calendar.DAY_OF_MONTH) // Initial day
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

    // Method to show time period options dialog
    private void showDueDateOptionsDialog() {
        final CharSequence[] options = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        boolean[] selectedDays = new boolean[options.length]; // Initialize all days as unselected

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Due Dates");
        builder.setMultiChoiceItems(options, selectedDays, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedDays[which] = isChecked; // Update selected days array
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Check if all days are selected
                boolean allDaysSelected = true;
                for (boolean selected : selectedDays) {
                    if (!selected) {
                        allDaysSelected = false;
                        break;
                    }
                }

                // Build the selected days string
                StringBuilder selectedDaysString = new StringBuilder();
                if (allDaysSelected) {
                    selectedDaysString.append("Every Day");
                } else {
                    for (int i = 0; i < options.length; i++) {
                        if (selectedDays[i]) {
                            if (selectedDaysString.length() > 0) {
                                selectedDaysString.append(", ");
                            }
                            selectedDaysString.append(options[i]);
                        }
                    }
                }

                if (selectedDaysString.length() == 0) {
                    dueDateTextView.setText("None"); // If no days selected, show "None"
                } else {
                    dueDateTextView.setText(selectedDaysString.toString()); // Update the TextView with selected days
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle cancelation if needed
            }
        });

        builder.show();
    }

    private void showTimePickerDialog() {
        final Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                String reminderTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                reminderTimeTextView.setText(reminderTime); // Update the TextView with the selected time
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    // Method to update task details in Firestore
    private void updateTaskDetails() {
        // Get updated task details from the EditText and TextView
        String updatedTaskName = name_card.getText().toString();
        String updatedDescription = description_card.getText().toString();
        String updatedStartGoal = editStartTextGoal.getText().toString();
        String updatedEndGoal = editEndTextGoal.getText().toString();
        String updatedStartDate = StartDateTextView.getText().toString();
        String updatedEndDate = EndDateTextView.getText().toString();
        String updatedDueDate = dueDateTextView.getText().toString();
        String updatedReminderTime = reminderTimeTextView.getText().toString();

        // Update task details in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Create a map to hold the updated task details
        Map<String, Object> updatedTask = new HashMap<>();
        updatedTask.put("name", updatedTaskName);
        updatedTask.put("taskDescription", updatedDescription);
        updatedTask.put("startGoal", updatedStartGoal);
        updatedTask.put("endGoal", updatedEndGoal);
        updatedTask.put("startDate", updatedStartDate);
        updatedTask.put("endDate", updatedEndDate);
        updatedTask.put("dueDate", updatedDueDate);
        updatedTask.put("reminderTime", updatedReminderTime);

        // Update the task document in Firestore
        db.collection("users").document(userId)
                .collection("Goal").document("targetTasks").collection("target_tasks")
                .document(taskId)
                .update(updatedTask)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task details updated successfully
                        Toast.makeText(target_task_settings_page.this, "Task details updated", Toast.LENGTH_SHORT).show();

                        // Navigate back to the previous activity
                        navigateToPreviousActivity(updatedTaskName, updatedDescription, updatedStartGoal, updatedEndGoal, updatedStartDate, updatedEndDate);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update task details
                        Toast.makeText(target_task_settings_page.this, "Failed to update task details", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating task details: " + e.getMessage());
                    }
                });
    }

    // Method to navigate back to the previous activity
    private void navigateToPreviousActivity(String taskName, String taskDescription, String startGoal, String endGoal, String startDate, String endDate) {
        Intent intent = new Intent(getApplicationContext(), target_task_page.class);
        intent.putExtra("taskName", taskName);
        intent.putExtra("taskDescription", taskDescription);
        intent.putExtra("startGoal", startGoal);
        intent.putExtra("endGoal", endGoal);
        intent.putExtra("startDate", startDate);
        intent.putExtra("endDate", endDate);
        intent.putExtra("taskPosition", taskPosition);
        intent.putExtra("taskId", taskId);
        startActivity(intent);
    }

    private void deleteTaskFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Reference to the task document
        DocumentReference taskRef = db.collection("users").document(userId)
                .collection("Goal").document("targetTasks").collection("target_tasks").document(taskId);

        // First, retrieve and delete all logs in the loggedLogs subcollection
        taskRef.collection("loggedLogs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            batch.delete(document.getReference());
                        }

                        // Commit the batch to delete all logs
                        batch.commit().addOnCompleteListener(batchTask -> {
                            if (batchTask.isSuccessful()) {
                                // After successfully deleting all logs, delete the task document itself
                                taskRef.delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "Task and its logs successfully deleted");
                                            Toast.makeText(target_task_settings_page.this, "Task deleted successfully!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error deleting task", e);
                                            Toast.makeText(target_task_settings_page.this, "Failed to delete task. Please try again.", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.e("Firestore", "Error deleting logs", batchTask.getException());
                                Toast.makeText(target_task_settings_page.this, "Failed to delete logs. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e("Firestore", "Error retrieving logs", task.getException());
                        Toast.makeText(target_task_settings_page.this, "Failed to retrieve logs. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to check if input is valid
    private boolean isInputValid() {
        String taskName = name_card.getText().toString().trim();
        String taskDescription = description_card.getText().toString().trim();
        String startGoal = editStartTextGoal.getText().toString().trim();
        String endGoal = editEndTextGoal.getText().toString().trim();
        String startDate = StartDateTextView.getText().toString().trim();
        String endDate = EndDateTextView.getText().toString().trim();

        // Check for empty fields
        if (taskName.isEmpty() || taskDescription.isEmpty() || startGoal.isEmpty() || endGoal.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            return false;
        }

        // Check for numeric goals
        try {
            float startGoalValue = Float.parseFloat(startGoal);
            float endGoalValue = Float.parseFloat(endGoal);
            if (startGoalValue > endGoalValue) {
                return false;
            }
        } catch (NumberFormatException e) {
            // Handle invalid numeric input
            return false;
        }

        // Check for valid date range
        SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());
        try {
            Date startDateParsed = dateFormat.parse(startDate);
            Date endDateParsed = dateFormat.parse(endDate);

            if (startDateParsed != null && endDateParsed != null && startDateParsed.after(endDateParsed)) {
                return false;
            }
        } catch (ParseException e) {
            // Handle parsing error, invalid date format
            return false;
        }

        return true;
    }

    private void fetchReminderTimeFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Assuming 'habit_tasks' is your collection and 'taskId' is the document ID of the task
        db.collection("users").document(userId)
                .collection("Goal").document("targetTasks").collection("target_tasks")
                .document(taskId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Retrieve reminder time from Firestore document
                            String reminderTime = documentSnapshot.getString("reminderTime");

                            // Update TextView with reminder time
                            reminderTimeTextView.setText(reminderTime);
                        } else {
                            Log.d("fetchReminderTime", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("fetchReminderTime", "Error fetching document", e);
                    }
                });
    }

    private void fetchDueDateFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Assuming 'habit_tasks' is your collection and 'taskId' is the document ID of the task
        db.collection("users").document(userId)
                .collection("Goal").document("targetTasks").collection("target_tasks")
                .document(taskId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Retrieve reminder time from Firestore document
                            String dueDate = documentSnapshot.getString("dueDate");

                            // Update TextView with reminder time
                            dueDateTextView.setText(dueDate);
                        } else {
                            Log.d("fetchReminderTime", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("fetchReminderTime", "Error fetching document", e);
                    }
                });
    }

}
