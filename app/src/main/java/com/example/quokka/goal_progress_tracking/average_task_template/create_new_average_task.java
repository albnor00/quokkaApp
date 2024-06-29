package com.example.quokka.goal_progress_tracking.average_task_template;

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

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.example.quokka.goal_progress_tracking.goal_page_v2.choose_task_template;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class create_new_average_task extends AppCompatActivity {

    private EditText editTextGoal;
    private TextView textViewTimePeriod;
    private Switch switchGoalMoreOrLess;
    private TextView DateTextView;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_task_configuration);

        // Initialize views
        ImageView back_btn = findViewById(R.id.img_back);
        ImageView finish_btn = findViewById(R.id.img_check_mark);
        EditText name_card = findViewById(R.id.edit_task_name);
        EditText description_card = findViewById(R.id.edit_task_description);
        CardView time_period = findViewById(R.id.edit_timeperiod);
        CardView start_date = findViewById(R.id.edit_startdate);

        editTextGoal = findViewById(R.id.editTextGoal);
        switchGoalMoreOrLess = findViewById(R.id.switchGoalMoreOrLess);
        textViewTimePeriod = findViewById(R.id.textView2);
        DateTextView = findViewById(R.id.textView3);

        // Initialize calendar and date format
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());

        // Set today's date in DateTextView
        String todayDate = dateFormat.format(calendar.getTime());
        DateTextView.setText(todayDate);

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
                String taskName = name_card.getText().toString();
                String taskDescription = description_card.getText().toString();
                String goal = editTextGoal.getText().toString();
                String timePeriod = textViewTimePeriod.getText().toString();
                String startDate = DateTextView.getText().toString();
                boolean isGoalMoreOrLess = switchGoalMoreOrLess.isChecked();

                // Save task details to Firestore
                saveTaskToFirestore(taskName, goal, timePeriod, startDate, isGoalMoreOrLess, taskDescription);

                // Pass task details back to Goal_non_empty_page
                Intent intent = new Intent(create_new_average_task.this, Goal_non_empty_page.class);
                intent.putExtra("taskName", taskName);
                intent.putExtra("goal", goal);
                intent.putExtra("timePeriod", timePeriod);
                intent.putExtra("startDate", startDate);
                intent.putExtra("taskId", taskId);
                intent.putExtra("taskDescription", taskDescription);
                startActivity(intent);
                finish();
            }
        });

        // Handle card click to show numerical keyboard
        CardView goalCard = findViewById(R.id.edit_goal);
        goalCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumericKeyboard();
            }
        });

        // Handle time period options dialog
        time_period.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePeriodOptionsDialog();
            }
        });

        // Handle date picker dialog
        start_date.setOnClickListener(new View.OnClickListener() {
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
    private void showTimePeriodOptionsDialog() {
        final CharSequence[] options = {"Per Day", "Per Week", "Per Month", "Per Year"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Time Period");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which].toString();
                textViewTimePeriod.setText(selectedOption); // Update the TextView with the selected option
            }
        });
        builder.show();
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

    private void saveTaskToFirestore(String taskName, String goal, String timePeriod, String startDate, boolean isGoalMoreOrLess, String taskDescription) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Generate a new ID for the task
        taskId = UUID.randomUUID().toString();

        // Create a new task object
        Map<String, Object> task = new HashMap<>();
        task.put("taskId", taskId);
        task.put("name", taskName);
        task.put("goal", goal);
        task.put("timePeriod", timePeriod);
        task.put("startDate", startDate);
        task.put("goalMoreOrLess", isGoalMoreOrLess);
        task.put("taskDescription", taskDescription);

        // Access the 'averageTasks' subcollection under the user's 'Goal' collection
        CollectionReference averageTasksRef = db.collection("users").document(userId)
                .collection("Goal").document("averageTasks").collection("average_tasks");

        // Add the task document to the 'averageTasks' subcollection with the generated ID
        averageTasksRef.document(taskId)
                .set(task)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task successfully saved
                        Log.d("Firestore", "Task successfully saved with ID: " + taskId);
                        Toast.makeText(create_new_average_task.this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error saving task
                        Log.e("Firestore", "Error saving task", e);
                        Toast.makeText(create_new_average_task.this, "Failed to save task. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

