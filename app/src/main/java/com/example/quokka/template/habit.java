package com.example.quokka.template;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class habit extends AppCompatActivity {

    AppCompatButton goal,timePeriod,startDate,due,reminders;

    ImageView back,finish;

    TextView startDateText;

    FirebaseAuth auth;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_template);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        goal = findViewById(R.id.goal);
        timePeriod = findViewById(R.id.timePeriod);
        startDate = findViewById(R.id.startDate);
        due = findViewById(R.id.due);
        reminders = findViewById(R.id.reminders);
        back = findViewById(R.id.back);
        finish = findViewById(R.id.finish);

        setCurrentDate();

        goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a dialog with an EditText field for entering the goal
                showGoalInputDialog();
            }
        });

        timePeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a dialog with an EditText field for entering the goal
                showTimePeriodInputDialog();
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }

        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), templateMain.class);
                startActivity(intent);
                finish();
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = user.getUid();

                // Retrieve data from TextViews
                String goalText = ((TextView) findViewById(R.id.goalTime)).getText().toString();
                // Extract the integer part from the goalText
                int goal = Integer.parseInt(goalText.split("\\s+")[0]);

                String timePeriod = ((TextView) findViewById(R.id.perDay)).getText().toString().toLowerCase();
                String startDate = ((TextView) findViewById(R.id.date)).getText().toString();

                // Create a HashMap to store the data
                Map<String, Object> habitData = new HashMap<>();
                habitData.put("goal", goal);
                habitData.put("timePeriod", timePeriod);
                habitData.put("startDate", startDate);

                // Add data to Firestore
                db.collection("users")
                        .document(userId)
                        .collection("habit")
                        .add(habitData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                // Redirect to habitTracker activity
                                if (timePeriod.equals("day")) {
                                    Intent intent = new Intent(getApplicationContext(), habitTrackerDay.class);
                                    startActivity(intent);
                                    finish();
                                } if (timePeriod.equals("week")){
                                    Intent intent = new Intent(getApplicationContext(), habitTrackerWeek.class);
                                    startActivity(intent);
                                    finish();

                                }

                                if (timePeriod.equals("month")){
                                    Intent intent = new Intent(getApplicationContext(), habitTrackerMonth.class);
                                    startActivity(intent);
                                    finish();
                                }

                                if (timePeriod.equals("year")){
                                    Intent intent = new Intent(getApplicationContext(), habitTrackerYear.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                                // Handle failure
                            }
                        });
            }
        });





    }

    private void showDatePicker() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Do something with the selected date
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        TextView date = findViewById(R.id.date);
                        date.setText(selectedDate);
                        getIntent().putExtra("date",selectedDate);
                    }
                }, year, month, dayOfMonth);

        // Show DatePickerDialog
        datePickerDialog.show();
    }


    private void showTimePeriodInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Time Period");

        final String[] timePeriodOptions = {"Day", "Week", "Month", "Year"};

        builder.setItems(timePeriodOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedTimePeriod = timePeriodOptions[which];
                // Handle the selected time period for the timePeriod button
                TextView perDay = findViewById(R.id.perDay);
                perDay.setText(selectedTimePeriod);
                getIntent().putExtra("perDay",selectedTimePeriod);


            }
        });

        builder.show();
    }

    private void setCurrentDate() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        startDateText = findViewById(R.id.date);

        // Format the date as required (assuming the format is "dd/MM/yyyy")
        String currentDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);

        // Set the current date to the TextView
        startDateText.setText(currentDate);
    }





    // Method to show a dialog with an EditText for entering the goal
    private void showGoalInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Goal");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextView goalTime = findViewById(R.id.goalTime);
                // Retrieve the entered goal
                String goalText = input.getText().toString();
                goalTime.setText(goalText);
                getIntent().putExtra("goal",goalText);
                // Do something with the entered goal (e.g., save it)
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
