package com.example.quokka.template;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.profile.ProfileActivity;
import com.example.quokka.tasks.balance_wheel;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.template.habit;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class habitTrackerDay extends AppCompatActivity {
    private AppCompatButton back;
    private CalendarView calendarView;
    private ProgressBar circularProgressBar;

    private FirebaseAuth auth;
    private FirebaseUser user;

    private TextView progressTextView, progressNumber;
    private HashMap<String, Integer> progressMap; // Map to store progress for each date
    private String lastClickedDate;
    private boolean isDoubleClick;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_tracker);

        auth = FirebaseAuth.getInstance();
        calendarView = findViewById(R.id.calendar);
        circularProgressBar = findViewById(R.id.circularProgressbar);
        progressMap = new HashMap<>();
        back = findViewById(R.id.back);
        progressNumber = findViewById(R.id.text2);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the habit activity
                Intent intent = new Intent(getApplicationContext(), habit.class);
                startActivity(intent);
                finish();
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Generate a unique key for the selected date
                //String selectedDate = year + "/" + (month + 1) + "/" + dayOfMonth;
                String selectedDate = dayOfMonth + "/" +(month+1)+ "/" +year;

                if (!isDoubleClick) {
                    // If it's a single click, show the progress for the selected date
                    updateProgress(progressMap.getOrDefault(selectedDate, 0));
                    isDoubleClick = true;
                    lastClickedDate = selectedDate;

                    // Reset isDoubleClick after a delay
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isDoubleClick = false;
                        }
                    }, 500); // Adjust the delay as needed
                } else {
                    // If it's a double click, increment the progress for the selected date
                    if (selectedDate.equals(lastClickedDate)) {
                        incrementProgress(selectedDate);
                        isDoubleClick = false;
                    }
                }
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
                    MainActivity.checkUserRole(user, habitTrackerDay.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
    }

    // Method to increment progress for the selected date
    private void incrementProgress(final String selectedDate) {
        Log.d(TAG, "Selected Date: " + selectedDate);
        user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        // Retrieve the habit start date from the database
        db.collection("users")
                .document(userId)
                .collection("habit")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            if (documentSnapshot.contains("startDate")) {
                                String habitStartDate = documentSnapshot.getString("startDate");
                                // Compare the selected date with the habit start date
                                if (compareDates(selectedDate, habitStartDate) >= 0) {
                                    // If selected date is equal to or greater than habit start date, proceed with incrementing progress
                                    incrementProgressForDate(selectedDate);
                                } else {
                                    // If selected date is before habit start date, show a message or handle it accordingly
                                    // For example, you can display a toast indicating that the selected date is invalid
                                    Toast.makeText(getApplicationContext(), "Selected date is invalid", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error retrieving habit start date from database", e);
                        // Handle failure
                    }
                });
    }

    // Method to increment progress for the selected date
    private void incrementProgressForDate(final String selectedDate) {
        user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        db.collection("users")
                .document(userId)
                .collection("habit")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            if (documentSnapshot.contains("goal")) {
                                int goal = documentSnapshot.getLong("goal").intValue();
                                circularProgressBar.setMax(goal); // Set the maximum value of the progress bar
                                int progress = progressMap.getOrDefault(selectedDate, 0);
                                if (progress < goal) {
                                    progress += 1; // Increment the progress by 1
                                    progressMap.put(selectedDate, progress);
                                    updateProgress(progress);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error retrieving goal from database", e);
                        // Handle failure
                    }
                });
    }

    // Helper method to compare two dates
    private int compareDates(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);

        try {
            Date dateObj1 = sdf.parse(date1);
            Date dateObj2 = sdf.parse(date2);

            return dateObj1.compareTo(dateObj2);
        } catch (ParseException e) {
            // Handle parsing exception
            e.printStackTrace();
            return 0; // Return 0 as a default value
        }
    }

    // Method to update progress of the circular progress bar
    private void updateProgress(int progress) {
        circularProgressBar.setProgress(progress);

        // Get the goal from the progress bar's maximum value
        int goal = circularProgressBar.getMax();

        // Calculate the percentage completed
        int percentage = (int) ((float) progress / goal * 100);

        progressNumber.setText(progress +"/"+ goal);

        progressTextView = findViewById(R.id.text);
        String progressText = percentage + "%";
        progressTextView.setText(progressText);
    }
}

