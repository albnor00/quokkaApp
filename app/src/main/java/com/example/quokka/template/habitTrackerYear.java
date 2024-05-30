package com.example.quokka.template;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class habitTrackerYear extends AppCompatActivity {
    private ProgressBar circularProgressBar;
    private TextView progressTextView,progressNumber;

    private AppCompatButton back;
    private int goal = 0; // Default goal
    private CalendarView calendarView;

    private Map<Integer, Integer> yearProgressMap = new HashMap<>(); // Map to store progress for each year

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_tracker);

        progressNumber = findViewById(R.id.text2);
        circularProgressBar = findViewById(R.id.circularProgressbar);
        progressTextView = findViewById(R.id.text);
        calendarView = findViewById(R.id.calendar);
        back = findViewById(R.id.back);

        // Retrieve goal from the database and store it
        goal = retrieveGoalFromDatabase();

        // Add listener to CalendarView
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                final int yearValue = year; // Year is the identifier for yearly progress
                final int progress = getYearProgress(yearValue);

                // Check if it's a double click
                if (view.getTag() != null) {
                    // Second click (double click) - increment the progress
                    incrementYearProgress(yearValue);
                    view.setTag(null); // Reset tag
                } else {
                    // First click (single click) - just show the progress
                    view.setTag("clicked"); // Set tag to mark first click
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Reset tag after a delay
                            view.setTag(null);
                        }
                    }, 300); // Adjust this delay as needed (300 milliseconds here)
                    updateProgress(progress); // Update UI with the current progress
                }
            }

        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the habit activity
                Intent intent = new Intent(getApplicationContext(), habit.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private int retrieveGoalFromDatabase() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("habit")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the first document (assuming there's only one)
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            // Retrieve the "goal" field from the document
                            Object goalObj = documentSnapshot.get("goal");
                            if (goalObj != null) {
                                // Convert the goal object to an integer and return it
                                int goal = Integer.parseInt(goalObj.toString());
                                circularProgressBar.setMax(goal); // Update the circular progress bar
                                // Call a method to update UI with the retrieved goal
                                updateUIWithGoal(goal);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error retrieving goal from database", e);
                    }
                });

        // Return a default value (you can change this as needed)
        return 0;
    }

    private void updateUIWithGoal(int goal) {
        // Update UI elements with the retrieved goal value
        this.goal = goal; // Update the class variable with the retrieved goal
        circularProgressBar.setMax(goal); // Update the maximum progress of the circular progress bar
    }

    private void incrementYearProgress(int year) {
        int progress = getYearProgress(year);
        progress++; // Increment progress
        yearProgressMap.put(year, progress); // Update progress for the year
        updateProgress(progress); // Update UI
    }

    private void updateProgress(int progress) {
        // Ensure progress doesn't exceed the goal
        progress = Math.min(progress, goal);

        // Calculate the percentage completed relative to the goal
        int percentage = (int) ((float) progress / goal * 100);

        // Update progress text, capped at 100%
        percentage = Math.min(percentage, 100);
        progressTextView.setText(percentage + "%");
        progressNumber.setText(progress +"/"+ goal);

        // Update progress bar
        circularProgressBar.setProgress(progress);
    }

    // Method to retrieve the progress for a given year
    private int getYearProgress(int year) {
        return yearProgressMap.containsKey(year) ? yearProgressMap.get(year) : 0;
    }
}
