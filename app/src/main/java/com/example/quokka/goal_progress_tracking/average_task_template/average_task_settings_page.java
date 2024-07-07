package com.example.quokka.goal_progress_tracking.average_task_template;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
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

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.example.quokka.goal_progress_tracking.goal_setup.Question2;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class average_task_settings_page extends AppCompatActivity {
    private EditText editTextGoal;
    private TextView dueDateTextView;
    private Switch switchGoalMoreOrLess;
    private TextView DateTextView;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private EditText name_card;
    private EditText description_card;
    private boolean isMoreOrLess = false;
    private TextView reminderTimeTextView;


    //Intent variables
    private String taskName;
    private String taskDescription;
    private String goal;
    private String dueDate;
    private String startDate;
    private int taskPosition;
    private String taskId;
    private String reminderTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_settings_page);

        // Initialize views
        ImageView back_btn = findViewById(R.id.img_back);
        ImageView finish_btn = findViewById(R.id.img_check_mark);
        name_card = findViewById(R.id.edit_task_name);
        description_card = findViewById(R.id.edit_task_description);
        CardView due_Date = findViewById(R.id.edit_dueDate);
        CardView start_Date = findViewById(R.id.edit_startdate);
        CardView goalCard = findViewById(R.id.edit_goal);
        CardView deleteTaskCard = findViewById(R.id.task_delete);
        CardView reminder_Time = findViewById(R.id.edit_reminder);

        editTextGoal = findViewById(R.id.editTextGoal);
        switchGoalMoreOrLess = findViewById(R.id.switchGoalMoreOrLess);
        dueDateTextView = findViewById(R.id.dueDateTextView);
        DateTextView = findViewById(R.id.startDateTextView);

        // Initialize calendar and date format
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());


        // Get task information from intent
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra("taskId");
            taskName = intent.getStringExtra("taskName");
            taskDescription = intent.getStringExtra("taskDescription");
            goal = intent.getStringExtra("goal");
            dueDate = intent.getStringExtra("dueDate");
            startDate = intent.getStringExtra("startDate");
            reminderTime = intent.getStringExtra("reminderTime");
            taskPosition = intent.getIntExtra("taskPosition", -1);

        } else {
            Log.e("Intent Data", "Intent is null");
        }

        // Check switch
        if (taskId != null) {
            isItMoreOrLess();
        } else {
            Log.e("Firestore", "taskId is null. Cannot fetch goalMoreOrLess.");
        }

        fetchReminderTimeFromFirestore();
        fetchDueDateFromFirestore();

        // Set current values to the views
        name_card.setText(taskName);
        description_card.setText(taskDescription);
        editTextGoal.setText(goal);
        dueDateTextView.setText(dueDate);
        DateTextView.setText(startDate);


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start the add_new_average_log activity
                Intent intent = new Intent(getApplicationContext(), average_task_page.class);

                // Pass necessary data to the add_new_average_log activity
                intent.putExtra("taskName", taskName);
                intent.putExtra("taskDescription", taskDescription);
                intent.putExtra("goal", goal);
                intent.putExtra("duePeriod", dueDate);
                intent.putExtra("startDate", startDate);

                // Pass the position of the clicked task
                intent.putExtra("taskPosition", taskPosition);
                intent.putExtra("taskId", taskId);

                // Start the activity
                startActivity(intent);
            }
        });

        // Handle finish button click
        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInputValid()) {
                    updateTaskDetails();
                } else {
                    Toast.makeText(average_task_settings_page.this, "Please enter task name and goal", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle card click to show numerical keyboard
        goalCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumericKeyboard();
            }
        });

        // Handle time period options dialog
        due_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDueDateOptionsDialog();
            }
        });

        // Handle date picker dialog
        start_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Setup Switch listener
        switchGoalMoreOrLess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Handle switch state change

            }
        });

        reminder_Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        // Handle task deletion
        deleteTaskCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(average_task_settings_page.this);
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
                    MainActivity.checkUserRole2(user, average_task_settings_page.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
    }

    private void isItMoreOrLess() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("Goal").document("averageTasks").collection("average_tasks")
                .document(taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            isMoreOrLess = document.getBoolean("goalMoreOrLess");
                            Log.d("Firestore", "goalMoreOrLess: " + isMoreOrLess);

                            // Set the switch state based on the value of isMoreOrLess
                            switchGoalMoreOrLess.setChecked(isMoreOrLess);
                        }
                    } else {
                        Log.e("Firestore", "Error getting More or Less: ", task.getException());
                        Toast.makeText(this, "Failed to load goalMoreOrLess. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to show numerical keyboard for editTextGoal
    private void showNumericKeyboard() {
        editTextGoal.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editTextGoal, InputMethodManager.SHOW_IMPLICIT);
        }
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
                reminderTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                reminderTimeTextView.setText(reminderTime); // Update the TextView with the selected time
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    // Method to show date picker dialog
    private void showDatePickerDialog() {
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
                        DateTextView.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR), // Initial year
                calendar.get(Calendar.MONTH), // Initial month
                calendar.get(Calendar.DAY_OF_MONTH) // Initial day
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

    // Method to update task details in Firestore
    private void updateTaskDetails() {
        // Get updated task details from the EditText and TextView
        String updatedTaskName = name_card.getText().toString();
        String updatedDescription = description_card.getText().toString();
        String updatedGoal = editTextGoal.getText().toString();
        String updatedDueDate = dueDateTextView.getText().toString();
        String updatedStartDate = DateTextView.getText().toString();
        boolean isGoalMoreOrLess = switchGoalMoreOrLess.isChecked();
        String updatedReminderTime = reminderTimeTextView.getText().toString();

        // Update task details in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Create a map to hold the updated task details
        Map<String, Object> updatedTask = new HashMap<>();
        updatedTask.put("name", updatedTaskName);
        updatedTask.put("taskDescription", updatedDescription);
        updatedTask.put("goal", updatedGoal);
        updatedTask.put("dueDate", updatedDueDate);
        updatedTask.put("startDate", updatedStartDate);
        updatedTask.put("goalMoreOrLess", isGoalMoreOrLess);
        updatedTask.put("reminderTime", updatedReminderTime);

        // Update the task document in Firestore
        db.collection("users").document(userId)
                .collection("Goal").document("averageTasks").collection("average_tasks")
                .document(taskId)
                .update(updatedTask)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task details updated successfully
                        Toast.makeText(average_task_settings_page.this, "Task details updated", Toast.LENGTH_SHORT).show();

                        // Navigate back to the previous activity
                        navigateToPreviousActivity(updatedTaskName, updatedDescription, updatedGoal, updatedDueDate, updatedStartDate, updatedReminderTime);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update task details
                        Toast.makeText(average_task_settings_page.this, "Failed to update task details", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating task details: " + e.getMessage());
                    }
                });
    }

    // Method to navigate back to the previous activity
    private void navigateToPreviousActivity(String taskName, String taskDescription, String goal, String dueDate, String startDate, String reminderTime) {
        Intent intent = new Intent(getApplicationContext(), average_task_page.class);
        intent.putExtra("taskName", taskName);
        intent.putExtra("taskDescription", taskDescription);
        intent.putExtra("goal", goal);
        intent.putExtra("dueDate", dueDate);
        intent.putExtra("startDate", startDate);
        intent.putExtra("reminderTime", reminderTime);
        intent.putExtra("taskPosition", taskPosition);
        intent.putExtra("taskId", taskId);
        startActivity(intent);
    }

    // Method to check if input is valid
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
                .collection("Goal").document("averageTasks").collection("average_tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task details updated successfully
                        Toast.makeText(average_task_settings_page.this, "Task details deleted", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update task details
                        Toast.makeText(average_task_settings_page.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting task: " + e.getMessage());
                    }
                });
    }

    private void fetchReminderTimeFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Assuming 'habit_tasks' is your collection and 'taskId' is the document ID of the task
        db.collection("users").document(userId)
                .collection("Goal").document("averageTasks").collection("average_tasks")
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
                .collection("Goal").document("averageTasks").collection("average_tasks")
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
