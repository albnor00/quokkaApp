package com.example.quokka.goal_progress_tracking.average_task_template;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class create_new_average_task extends AppCompatActivity {

    private EditText editTextGoal;
    private TextView textViewTimePeriod;
    private Switch switchGoalMoreOrLess;
    private TextView DateTextView;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_task_configuration);

        // Initialize views
        ImageView back_btn = findViewById(R.id.img_back);
        ImageView finish_btn = findViewById(R.id.img_check_mark);
        EditText name_card = findViewById(R.id.edit_task_name);
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
                Intent intent = new Intent(getApplicationContext(), Goal_non_empty_page.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle finish button click
        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = name_card.getText().toString();
                String goal = editTextGoal.getText().toString();
                String timePeriod = textViewTimePeriod.getText().toString();
                String startDate = DateTextView.getText().toString();

                // Pass task details back to Goal_non_empty_page
                Intent intent = new Intent(create_new_average_task.this, Goal_non_empty_page.class);
                intent.putExtra("taskName", taskName);
                intent.putExtra("goal", goal);
                intent.putExtra("timePeriod", timePeriod);
                intent.putExtra("startDate", startDate);
                startActivity(intent);
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
}

