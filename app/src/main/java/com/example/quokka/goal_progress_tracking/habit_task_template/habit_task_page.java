package com.example.quokka.goal_progress_tracking.habit_task_template;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.example.quokka.goal_progress_tracking.target_task_template.target_task_log_history_page;
import com.example.quokka.goal_progress_tracking.target_task_template.target_task_settings_page;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.template.habit;
import com.example.quokka.template.habitTrackerDay;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class habit_task_page extends AppCompatActivity {
    private CalendarView calendarView;
    private ProgressBar circularProgressBar;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private TextView progressTextView, progressNumber, goalMet_or_goalNotMet;
    private HashMap<String, Integer> progressMap; // Map to store progress for each date
    private String lastClickedDate;
    private boolean isDoubleClick;
    private Handler handler = new Handler();

    // Intent variables
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
        setContentView(R.layout.activity_habit_task_page);

        auth = FirebaseAuth.getInstance();
        calendarView = findViewById(R.id.calendar);
        circularProgressBar = findViewById(R.id.circularProgressbar);
        progressMap = new HashMap<>();
        ImageView back = findViewById(R.id.img_back);
        ImageView menu = findViewById(R.id.img_menu);
        progressNumber = findViewById(R.id.habit_tracker);
        progressTextView = findViewById(R.id.progress_in_percent);
        goalMet_or_goalNotMet = findViewById(R.id.goal_met_or_not_met_label);

        // Get task details from intent
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

        // Call the method to create logs for missed days
        createLogsForMissedDays();

        // Fetch progress logs initially and set up UI
        fetchProgressLogsAndSetUpUI();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the habit activity
                Intent intent = new Intent(getApplicationContext(), Goal_non_empty_page.class);
                startActivity(intent);
                finish();
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
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
                    MainActivity.checkUserRole2(user, habit_task_page.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProgressLogsAndSetUpUI();
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.task_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        popupMenu.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_history) {
            // Create an intent to start the add_new_average_log activity
            Intent intent = new Intent(getApplicationContext(), habit_task_log_history_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent.putExtra("taskName", taskName);
            intent.putExtra("taskDescription", taskDescription);
            intent.putExtra("startGoal", goal);
            intent.putExtra("startDate", startDate);
            intent.putExtra("dueDate", dueDate);

            // Pass the position of the clicked task
            intent.putExtra("taskPosition", taskPosition);
            intent.putExtra("taskId", taskId);

            // Start the activity
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            // Create an intent to start the add_new_average_log activity
            Intent intent = new Intent(getApplicationContext(), habit_task_settings_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent.putExtra("taskName", taskName);
            intent.putExtra("taskDescription", taskDescription);
            intent.putExtra("goal", goal);
            intent.putExtra("startDate", startDate);
            intent.putExtra("dueDate", dueDate);

            // Pass the position of the clicked task
            intent.putExtra("taskPosition", taskPosition);
            intent.putExtra("taskId", taskId);

            // Start the activity
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

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
                .collection("Goal")
                .document("habitTasks")
                .collection("habit_tasks")
                .document(taskId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("goal")) {

                                Object goalObject = documentSnapshot.get("goal");
                                int goal = 0;
                                if (goalObject instanceof Number) {
                                    goal = ((Number) goalObject).intValue();
                                } else if (goalObject instanceof String) {
                                    try {
                                        goal = Integer.parseInt((String) goalObject);
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Goal string is not a valid integer: " + goalObject, e);
                                        Toast.makeText(habit_task_page.this, "Goal value is not a valid number", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else {
                                    Log.e(TAG, "Goal is neither a number nor a string: " + goalObject);
                                    Toast.makeText(habit_task_page.this, "Goal value is not valid", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                circularProgressBar.setMax(goal); // Set the maximum value of the progress bar
                                int progress = progressMap.getOrDefault(selectedDate, 0);
                                if (progress < goal) {
                                    progress += 1; // Increment the progress by 1
                                    progressMap.put(selectedDate, progress);
                                    updateProgress(progress);

                                    // Save progress log to Firestore
                                    saveProgressLog(taskId, selectedDate, progress);
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

    private void saveProgressLog(final String taskId, final String selectedDate, final int progress) {
        user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        // Convert the selectedDate string to a Date object
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date;
        try {
            date = dateFormat.parse(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle parse exception if needed
            return; // Exit the method if date parsing fails
        }

        // Check if a log for the selected date already exists
        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks")
                .collection("habit_tasks").document(taskId)
                .collection("loggedLogs")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // If a log exists, update it
                        DocumentSnapshot existingLog = queryDocumentSnapshots.getDocuments().get(0);
                        String logId = existingLog.getId();
                        updateExistingLog(taskId, logId, progress);
                    } else {
                        // If no log exists, create a new one
                        String logId = UUID.randomUUID().toString(); // Generate a unique ID for the log
                        habit_log newLog = new habit_log(logId, progress, "", date);

                        db.collection("users").document(userId)
                                .collection("Goal").document("habitTasks")
                                .collection("habit_tasks").document(taskId)
                                .collection("loggedLogs") // Collection for progress logs
                                .document(logId) // Use the generated ID as the document ID
                                .set(newLog)
                                .addOnSuccessListener(aVoid -> {
                                    // Log added successfully
                                    Log.d(TAG, "Progress log saved successfully.");
                                    // Update UI after saving log
                                    updateUIAfterProgressSave();
                                })
                                .addOnFailureListener(e -> {
                                    // Error occurred while adding log
                                    Toast.makeText(this, "Error saving progress log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Error occurred while checking existing logs
                    Toast.makeText(this, "Error checking progress log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateExistingLog(String taskId, String logId, int progress) {
        user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("log", progress);

        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks")
                .collection("habit_tasks").document(taskId)
                .collection("loggedLogs").document(logId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    // Log updated successfully
                    Log.d(TAG, "Progress log updated successfully.");
                    // Update UI after updating log
                    updateUIAfterProgressSave();
                })
                .addOnFailureListener(e -> {
                    // Error occurred while updating log
                    Toast.makeText(this, "Error updating progress log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchProgressLogsAndSetUpUI() {
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        db.collection("users")
                .document(userId)
                .collection("Goal")
                .document("habitTasks")
                .collection("habit_tasks")
                .document(taskId)
                .collection("loggedLogs") // Fetch progress from the loggedLogs collection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                progressMap.clear();
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    String date = formatDate(document.getDate("date"));
                                    int progress = document.getLong("log").intValue();
                                    progressMap.put(date, progress);
                                }
                                updateUIWithProgressData(progressMap);
                            } else {
                                Log.d(TAG, "No progress data found for this task");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void updateUIWithProgressData(Map<String, Integer> progress) {
        // Example to update UI for the current date
        String currentDate = getCurrentDate();
        if (progress.containsKey(currentDate)) {
            updateProgress(progress.get(currentDate));
        } else {
            updateProgress(0);
        }
    }

    private void updateUIAfterProgressSave() {
        // After saving or updating progress log, update UI components
        updateUI();
    }

    private void updateUI() {
        // Get the progress for the last clicked date or today's date as default
        String selectedDate = lastClickedDate != null ? lastClickedDate : getCurrentDate();
        int progress = progressMap.getOrDefault(selectedDate, 0);
        updateProgress(progress);
    }

    // Method to format Date object to String (dd/MM/yyyy)
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    // Helper method to get current date as String (dd/MM/yyyy)
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return day + "/" + month + "/" + year;
    }

    private void createLogsForMissedDays() {
        user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        // Convert startDate string to a Date object
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date startDateObj;
        try {
            startDateObj = dateFormat.parse(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle parse exception if needed
            return; // Exit the method if date parsing fails
        }

        // Get today's date
        Date todayDate = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDateObj);

        // Iterate from startDate to today's date
        while (!calendar.getTime().after(todayDate)) {
            final Date currentDate = calendar.getTime();
            final String currentDateString = dateFormat.format(currentDate);

            // Check if a log for the current date already exists
            db.collection("users").document(userId)
                    .collection("Goal").document("habitTasks")
                    .collection("habit_tasks").document(taskId)
                    .collection("loggedLogs")
                    .whereEqualTo("date", currentDate)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // If no log exists, create a new one with default progress value 0
                            String logId = UUID.randomUUID().toString();
                            habit_log newLog = new habit_log(logId, 0, "", currentDate);

                            db.collection("users").document(userId)
                                    .collection("Goal").document("habitTasks")
                                    .collection("habit_tasks").document(taskId)
                                    .collection("loggedLogs")
                                    .document(logId)
                                    .set(newLog)
                                    .addOnSuccessListener(aVoid -> {
                                        // Log added successfully
                                        Log.d(TAG, "Progress log for " + currentDateString + " saved successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error occurred while adding log
                                        Log.e(TAG, "Error saving progress log for " + currentDateString + ": " + e.getMessage(), e);
                                    });
                        } else {
                            Log.d(TAG, "Progress log for " + currentDateString + " already exists.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error occurred while checking existing logs
                        Log.e(TAG, "Error checking progress log for " + currentDateString + ": " + e.getMessage(), e);
                    });

            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    // Helper method to compare two dates
    private int compareDates(String date1, String date2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        try {
            Date d1 = dateFormat.parse(date1);
            Date d2 = dateFormat.parse(date2);
            return d1.compareTo(d2);
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing error", e);
        }
        return 0;
    }

    // Method to update progress of the circular progress bar
    private void updateProgress(int currentProgress) {
        String goalString = goal;
        int goal = 0;
        try {
            goal = Integer.parseInt(goalString);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Goal string is not a valid integer: " + goalString, e);
            Toast.makeText(habit_task_page.this, "Goal value is not a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        int percentage = (int) ((currentProgress / (float) goal) * 100);
        circularProgressBar.setProgress(percentage);
        progressTextView.setText(percentage + "%");
        progressNumber.setText(currentProgress + "/" + goal);

        if (currentProgress >= goal) {
            goalMet_or_goalNotMet.setText("Goal Met");
        } else {
            goalMet_or_goalNotMet.setText("Goal Not Met");
        }
    }
}
