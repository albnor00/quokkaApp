package com.example.quokka.goal_progress_tracking.habit_task_template;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.average_task_template.create_new_average_task;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.example.quokka.goal_progress_tracking.goal_page_v2.choose_task_template;
import com.example.quokka.tasks.balance_wheel;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class create_new_habit_task extends AppCompatActivity {
    private EditText name_card;
    private EditText description_card;
    private EditText editGoalText;
    private TextView startDateTextView;
    private TextView dueDateTextView;
    private TextView reminderTimeTextView;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String taskId;
    private String reminderTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_task_configuration);

        // Initialize views
        ImageView back_btn = findViewById(R.id.img_back);
        ImageView finish_btn = findViewById(R.id.img_check_mark);

        CardView goalCard = findViewById(R.id.edit_goal);
        CardView startDate = findViewById(R.id.edit_startdate);
        CardView dueDate = findViewById(R.id.edit_dueDate);
        CardView reminderTime = findViewById(R.id.edit_reminder);

        name_card = findViewById(R.id.edit_task_name);
        description_card = findViewById(R.id.edit_task_description);
        editGoalText = findViewById(R.id.editTextGoal);
        startDateTextView = findViewById(R.id.startDateTextView);
        dueDateTextView = findViewById(R.id.dueDateTextView);
        reminderTimeTextView = findViewById(R.id.reminderTextView);

        // Initialize calendar and date format
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());

        // Set today's date in startDateTextView
        String todayDate = dateFormat.format(calendar.getTime());
        startDateTextView.setText(todayDate);


        // Handle back button click
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), choose_task_template.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle finish button click
        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInputValid()) {
                    String taskName = name_card.getText().toString();
                    String taskDescription = description_card.getText().toString();
                    String goal = editGoalText.getText().toString();
                    String startDate = startDateTextView.getText().toString();
                    String dueDate = dueDateTextView.getText().toString();
                    String reminderTime = reminderTimeTextView.getText().toString();

                    // Save task details to Firestore
                    saveTaskToFirestore(taskName, taskDescription, goal, startDate, dueDate, reminderTime);

                    // Pass task details back to Goal_non_empty_page
                    Intent intent = new Intent(create_new_habit_task.this, Goal_non_empty_page.class);
                    intent.putExtra("taskName", taskName);
                    intent.putExtra("taskDescription", taskDescription);
                    intent.putExtra("goal", goal);
                    intent.putExtra("startDate", startDate);
                    intent.putExtra("dueDate", dueDate);
                    intent.putExtra("reminderTime", reminderTime);
                    intent.putExtra("taskId", taskId);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(create_new_habit_task.this, "Please enter task name and goal", Toast.LENGTH_SHORT).show();
                }
            }
        });

        goalCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumericKeyboard();
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialogStartDate();
            }
        });

        dueDate.setOnClickListener(new View.OnClickListener() {
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
                    MainActivity.checkUserRole2(user, create_new_habit_task.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });

        ImageView tooltip = findViewById(R.id.img_help);
        tooltip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(create_new_habit_task.this);
                builder.setTitle("Task Customization");
                builder.setMessage("This is where you can choose customization options for your task. You can set the task name, description, due date, start date, goal and reminder time. " +
                        "The due date is which days you want notification reminders, and reminder time is at what time you get the notification. " +
                        "The task will present you with a calender where you can monitor and log how often you perform a habit. " +
                        "If you intend to create the task, make sure you press the checkmark, otherwise your task will not be saved.");
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    // Method to show numerical keyboard for editTextGoal
    private void showNumericKeyboard() {
        editGoalText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editGoalText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

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
                        startDateTextView.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR), // Initial year
                calendar.get(Calendar.MONTH), // Initial month
                calendar.get(Calendar.DAY_OF_MONTH) // Initial day
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

    // Method to show time picker dialog
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

    private void saveTaskToFirestore(String taskName, String taskDescription, String goal, String startDate, String dueDate, String reminderTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Generate a new ID for the task
        taskId = UUID.randomUUID().toString();

        // Create a new task object
        Map<String, Object> task = new HashMap<>();
        task.put("taskId", taskId);
        task.put("name", taskName);
        task.put("taskDescription", taskDescription);
        task.put("goal", goal);
        task.put("startDate", startDate);
        task.put("dueDate", dueDate);
        task.put("reminderTime", reminderTime);


        // Access the 'averageTasks' subcollection under the user's 'Goal' collection
        CollectionReference averageTasksRef = db.collection("users").document(userId)
                .collection("Goal").document("habitTasks").collection("habit_tasks");

        // Add the task document to the 'averageTasks' subcollection with the generated ID
        averageTasksRef.document(taskId)
                .set(task)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task successfully saved
                        Log.d("Firestore", "Task successfully saved with ID: " + taskId);
                        Toast.makeText(create_new_habit_task.this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error saving task
                        Log.e("Firestore", "Error saving task", e);
                        Toast.makeText(create_new_habit_task.this, "Failed to save task. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isInputValid() {
        String taskName = name_card.getText().toString().trim();
        String taskDescription = description_card.getText().toString().trim();
        String goal = editGoalText.getText().toString().trim();
        return !taskName.isEmpty() && !goal.isEmpty() && !taskDescription.isEmpty();
    }
}
