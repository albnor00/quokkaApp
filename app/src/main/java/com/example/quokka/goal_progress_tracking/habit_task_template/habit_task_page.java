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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class habit_task_page extends AppCompatActivity {
    private CalendarView calendarView;
    private ProgressBar circularProgressBar;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private TextView progressTextView, progressNumber, goalMet_or_goalNotMet, bestStreakTextView, currentStreakTextView;
    private HashMap<String, Integer> progressMap; // Map to store progress for each date
    private String lastClickedDate;

    // Intent variables
    private String taskName;
    private String taskDescription;
    private String goal;
    private String startDate;
    private String dueDate;
    private int taskPosition;
    private String taskId;

    private boolean isDoubleClick = false;
    private static final long DOUBLE_CLICK_THRESHOLD = 300; // 300ms for double click detection
    private Handler doubleClickHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_task_page);

        ImageView back = findViewById(R.id.img_back);
        ImageView menu = findViewById(R.id.img_menu);

        fetchIntentData();
        initializeUIComponents();

        // Call the method to create logs for missed days
        createLogsForMissedDays();

        // Fetch progress logs initially and set up UI
        fetchProgressLogsAndSetUpUI();

        // Do the same fo todays date
        fetchProgressFromFirestore(getCurrentDate());

        // Calculate streaks
        fetchAndCalculateStreaks();

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
                handleDateChange(year, month, dayOfMonth);
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

    private void initializeUIComponents() {
        auth = FirebaseAuth.getInstance();
        calendarView = findViewById(R.id.calendar);

        circularProgressBar = findViewById(R.id.circularProgressbar);
        progressMap = new HashMap<>();
        progressTextView = findViewById(R.id.progress_in_percent);
        progressNumber = findViewById(R.id.habit_tracker);
        goalMet_or_goalNotMet = findViewById(R.id.goal_met_or_not_met_label);
        bestStreakTextView = findViewById(R.id.text_best_streak_value);
        currentStreakTextView = findViewById(R.id.text_streak_value);

        TextView title = findViewById(R.id.text_task_name);
        TextView description = findViewById(R.id.text_task_description);
        title.setText(taskName);
        description.setText(taskDescription);
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
            sendIntentData(habit_task_log_history_page.class);
            return true;
        } else if (id == R.id.action_settings) {
            sendIntentData(habit_task_settings_page.class);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
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

    private void handleDateChange(int year, int month, int dayOfMonth) {
        final String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;

        // Convert startDate to use slashes (/) instead of dots (.)
        String habitStartDateConverted = startDate.replace(".", "/");

        // Compare the selected date with the start date
        if (compareDates(selectedDate, habitStartDateConverted) < 0) {
            // Show a message to the user that the selected date is before the start date
            Toast.makeText(this, "Cannot save progress for a date before the start date.", Toast.LENGTH_SHORT).show();
            return; // Exit the method without updating progress
        }

        if (selectedDate.equals(lastClickedDate) && isDoubleClick) {
            doubleClickHandler.removeCallbacksAndMessages(null);
            isDoubleClick = false;
            incrementProgressLog(taskId, selectedDate);
            fetchAndCalculateStreaks();
        } else {
            lastClickedDate = selectedDate;
            isDoubleClick = true;
            doubleClickHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isDoubleClick = false;
                    fetchProgressForDate(selectedDate);
                }
            }, DOUBLE_CLICK_THRESHOLD);

            fetchProgressForDate(selectedDate);
        }
    }

    private void fetchProgressForDate(String selectedDate) {
        // Retrieve progress for the selected date from progressMap
        if (progressMap.containsKey(selectedDate)) {
            updateProgress(progressMap, selectedDate);
        } else {
            fetchProgressFromFirestore(selectedDate);
        }
    }

    private void fetchProgressFromFirestore(String selectedDate) {
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date;
        try {
            date = dateFormat.parse(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks")
                .collection("habit_tasks").document(taskId)
                .collection("loggedLogs")
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        int progress = document.getLong("log").intValue();
                        progressMap.put(selectedDate, progress);
                        updateProgress(progressMap, selectedDate);
                    } else {
                        updateProgress(progressMap, selectedDate); // No progress found, update with 0
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProgressLogsAndSetUpUI();
        fetchProgressFromFirestore(getCurrentDate());
        fetchAndCalculateStreaks();
    }

    private int compareDates(String selectedDate, String habitStartDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date1 = sdf.parse(selectedDate);
            Date date2 = sdf.parse(habitStartDate);
            return date1.compareTo(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
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
                .collection("loggedLogs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressMap.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String date = formatDate(document.getDate("date"));
                            int progress = document.getLong("log").intValue();
                            progressMap.put(date, progress);
                        }
                        // Update the UI with the progress data for the current or selected date
                        String selectedDate = lastClickedDate != null ? lastClickedDate : getCurrentDate();
                        updateProgress(progressMap, selectedDate);
                    } else {
                        Log.d(TAG, "Error getting progress logs: ", task.getException());
                    }
                });
    }

    private void incrementProgressLog(final String taskId, final String selectedDate) {
        // Convert startDate to use slashes (/) instead of dots (.)
        String habitStartDateConverted = startDate.replace(".", "/");

        // Compare the selected date with the start date
        if (compareDates(selectedDate, habitStartDateConverted) < 0) {
            // Show a message to the user that the selected date is before the start date
            Toast.makeText(this, "Cannot save progress for a date before the start date.", Toast.LENGTH_SHORT).show();
            return; // Exit the method without updating progress
        }

        user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date;
        try {
            date = dateFormat.parse(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        db.collection("users").document(userId)
                .collection("Goal").document("habitTasks")
                .collection("habit_tasks").document(taskId)
                .collection("loggedLogs")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // If the document exists, update the progress
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String logId = document.getId();
                        int currentProgress = document.getLong("log").intValue();
                        updateExistingLog(taskId, logId, currentProgress + 1);
                    } else {
                        // If the document doesn't exist, create a new one
                        String logId = UUID.randomUUID().toString();
                        habit_log newLog = new habit_log(logId, 1, "", date);
                        db.collection("users").document(userId)
                                .collection("Goal").document("habitTasks")
                                .collection("habit_tasks").document(taskId)
                                .collection("loggedLogs")
                                .document(logId)
                                .set(newLog)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Progress log saved successfully.");
                                    // Update the progress map and UI after creating the log
                                    progressMap.put(selectedDate, 1);
                                    updateProgress(progressMap, selectedDate);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error saving progress log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
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
                    // Update progress map and UI after updating log
                    progressMap.put(lastClickedDate, progress);
                    updateProgress(progressMap, lastClickedDate);
                })
                .addOnFailureListener(e -> {
                    // Error occurred while updating log
                    Toast.makeText(this, "Error updating progress log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

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

        // Convert startDate to use slashes (/) instead of dots (.)
        String habitStartDateConverted = startDate.replace(".", "/");

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date startDateObj;
        try {
            startDateObj = dateFormat.parse(habitStartDateConverted);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        Date todayDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDateObj);

        while (!calendar.getTime().after(todayDate)) {
            final Date currentDate = calendar.getTime();
            final String currentDateString = dateFormat.format(currentDate);

            db.collection("users").document(userId)
                    .collection("Goal").document("habitTasks")
                    .collection("habit_tasks").document(taskId)
                    .collection("loggedLogs")
                    .whereEqualTo("date", currentDate)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            String logId = UUID.randomUUID().toString();
                            habit_log newLog = new habit_log(logId, 0, "", currentDate);

                            db.collection("users").document(userId)
                                    .collection("Goal").document("habitTasks")
                                    .collection("habit_tasks").document(taskId)
                                    .collection("loggedLogs")
                                    .document(logId)
                                    .set(newLog)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Progress log for " + currentDateString + " saved successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error saving progress log for " + currentDateString + ": " + e.getMessage(), e);
                                    });
                        } else {
                            Log.d(TAG, "Progress log for " + currentDateString + " already exists.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking progress log for " + currentDateString + ": " + e.getMessage(), e);
                    });

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void updateProgress(Map<String, Integer> progressMap, String selectedDate) {
        int currentProgress = progressMap.getOrDefault(selectedDate, 0);
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

        Log.d("ProgressDebug", "Current Progress: " + currentProgress);
        Log.d("ProgressDebug", "Goal: " + goal);
        Log.d("ProgressDebug", "Percentage: " + percentage);


        circularProgressBar.setProgress(percentage);
        progressTextView.setText(percentage + "%");
        progressNumber.setText(currentProgress + "/" + goal);

        if (currentProgress >= goal) {
            goalMet_or_goalNotMet.setText("Goal Met");
        } else {
            goalMet_or_goalNotMet.setText("Goal Not Met");
        }
    }

    private void fetchAndCalculateStreaks() {
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        db.collection("users")
                .document(userId)
                .collection("Goal")
                .document("habitTasks")
                .collection("habit_tasks")
                .document(taskId)
                .collection("loggedLogs")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<habit_log> logs = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            int log = document.getLong("log").intValue();
                            String note = document.getString("note");
                            Date date = document.getDate("date");
                            logs.add(new habit_log(id, log, note, date));
                        }
                        calculateStreaks(logs);
                    } else {
                        Log.d(TAG, "Error fetching logs: ", task.getException());
                    }
                });
    }

    private void calculateStreaks(List<habit_log> logs) {
        if (logs.isEmpty()) {
            updateStreakUI(0, 0);
            return;
        }

        int currentStreak = 0;
        int bestStreak = 0;
        Date lastLogDate = null;

        // Iterate through logs starting from the oldest date
        for (int i = logs.size() - 1; i >= 0; i--) {
            habit_log log = logs.get(i);
            Date logDate = log.getDate();
            int logValue = log.getLog();

            if (logValue > 0) {
                if (lastLogDate == null) {
                    // First log, start the current streak
                    currentStreak = 1;
                } else {
                    if (isNextDay(lastLogDate, logDate)) {
                        // Continue the streak
                        currentStreak++;
                    } else {
                        // Streak is broken, update the best streak if necessary
                        if (currentStreak > bestStreak) {
                            bestStreak = currentStreak;
                        }
                        currentStreak = 1; // Start a new streak with the current log
                    }
                }
            } else {
                // Non-contributing log, streak is broken, update the best streak if necessary
                if (currentStreak > bestStreak) {
                    bestStreak = currentStreak;
                }
                currentStreak = 0;
            }

            lastLogDate = logDate;
        }

        // Final check after the loop
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak;
        }

        updateStreakUI(currentStreak, bestStreak);
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isNextDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        cal1.add(Calendar.DAY_OF_YEAR, 1);
        return isSameDay(cal1.getTime(), cal2.getTime());
    }

    private void updateStreakUI(int currentStreak, int bestStreak) {
        currentStreakTextView.setText(String.valueOf(currentStreak));
        bestStreakTextView.setText(String.valueOf(bestStreak));
    }
}
