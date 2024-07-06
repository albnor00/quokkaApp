package com.example.quokka.goal_progress_tracking.target_task_template;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.average_task_template.average_task_log_history_page;
import com.example.quokka.goal_progress_tracking.average_task_template.average_task_settings_page;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class target_task_page extends AppCompatActivity {
    //Intent variables
    private String taskName;
    private String taskDescription;
    private String startGoal;
    private String endGoal;
    private String startDate;
    private String endDate;
    private int taskPosition;
    private String taskId;

    private TextView minValueTextView;
    private TextView maxValueTextView;
    private TextView avgValueTextView;
    private LineChart lineChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_task_page);

        // Initialize views
        ImageView backBtn = findViewById(R.id.img_back);
        ImageView imgMenu = findViewById(R.id.img_menu);
        TextView title = findViewById(R.id.text_task_name_at_top);
        TextView description = findViewById(R.id.text_task_description);
        TextView startGoalLabel = findViewById(R.id.text_task_startGoal);
        TextView endGoalLabel = findViewById(R.id.text_task_endGoal);
        minValueTextView = findViewById(R.id.text_min_value);
        maxValueTextView = findViewById(R.id.text_max_value);
        avgValueTextView = findViewById(R.id.text_average_value);
        lineChart = findViewById(R.id.line_chart);


        // Get task information from intent
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra("taskId");
            taskPosition = intent.getIntExtra("taskPosition", -1);
            taskName = intent.getStringExtra("taskName");
            taskDescription = intent.getStringExtra("taskDescription");
            startGoal = intent.getStringExtra("startGoal");
            endGoal = intent.getStringExtra("endGoal");
            startDate = intent.getStringExtra("startDate");
            endDate = intent.getStringExtra("endDate");
        }

        // Set description
        getDescriptionFromFirestore();

        // Set title
        title.setText(taskName);

        // Set description
        description.setText(taskDescription);

        // Set goal label
        startGoalLabel.setText("Start Goal: " + startGoal);
        endGoalLabel.setText("End Goal: " + endGoal);


        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        // Handle back button click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Goal_non_empty_page.class);
                startActivity(intent);
                finish();
            }
        });


        fillMissingLogsBetweenDates(taskId, startDate, endDate);

        fetchAndDisplayProgressData(7);
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
            Intent intent = new Intent(getApplicationContext(), target_task_log_history_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent.putExtra("taskName", taskName);
            intent.putExtra("taskDescription", taskDescription);
            intent.putExtra("startGoal", startGoal);
            intent.putExtra("endGoal", endGoal);
            intent.putExtra("startDate", startDate);
            intent.putExtra("endDate", endDate);

            // Pass the position of the clicked task
            intent.putExtra("taskPosition", taskPosition);
            intent.putExtra("taskId", taskId);

            // Start the activity
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            // Create an intent to start the add_new_average_log activity
            Intent intent = new Intent(getApplicationContext(), target_task_settings_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent.putExtra("taskName", taskName);
            intent.putExtra("taskDescription", taskDescription);
            intent.putExtra("startGoal", startGoal);
            intent.putExtra("endGoal", endGoal);
            intent.putExtra("startDate", startDate);
            intent.putExtra("endDate", endDate);

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

    private void getDescriptionFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("Goal").document("targetTasks").collection("target_tasks")
                .document(taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            taskName = document.getString("name");
                            taskDescription = document.getString("taskDescription");
                            startGoal = document.getString("startGoal");
                            endGoal = document.getString("endGoal");
                            // Update the description TextView
                            TextView title = findViewById(R.id.text_task_name_at_top);
                            TextView description = findViewById(R.id.text_task_description);
                            TextView startGoalLabel = findViewById(R.id.text_task_startGoal);
                            TextView endGoalLabel = findViewById(R.id.text_task_endGoal);
                            title.setText(taskName);
                            description.setText(taskDescription);
                            startGoalLabel.setText("Start Value: " + startGoal);
                            endGoalLabel.setText("End Goal: " + endGoal);
                        }
                    } else {
                        Log.e("Firestore", "Error getting description: ", task.getException());
                        Toast.makeText(this, "Failed to load description. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAndDisplayProgressData(int days) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);

        db.collection("users").document(userId)
                .collection("Goal").document("targetTasks").collection("target_tasks")
                .document(taskId).collection("loggedLogs")
                .orderBy("date")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        Map<String, Double> dateToProgressMap = new TreeMap<>();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        // Aggregate progress values by date
                        for (DocumentSnapshot document : documents) {
                            Date date = document.getDate("date");
                            double progress = document.getDouble("log");

                            if (date != null) {
                                String formattedDate = dateFormat.format(date);
                                dateToProgressMap.put(formattedDate, dateToProgressMap.getOrDefault(formattedDate, 0.0) + progress);
                            }
                        }

                        // Convert aggregated data into Entries for the chart
                        List<Entry> entries = new ArrayList<>();
                        double minValue = Double.MAX_VALUE;
                        double maxValue = Double.MIN_VALUE;
                        double sum = 0;
                        int count = 0;

                        for (Map.Entry<String, Double> entry : dateToProgressMap.entrySet()) {
                            try {
                                Date date = dateFormat.parse(entry.getKey());
                                if (date != null) {
                                    long millis = date.getTime();
                                    double progress = entry.getValue();

                                    entries.add(new Entry(millis + TimeUnit.HOURS.toMillis(12), (float) progress));

                                    // Update min, max, sum, and count for average calculation
                                    if (progress < minValue) {
                                        minValue = progress;
                                    }
                                    if (progress > maxValue) {
                                        maxValue = progress;
                                    }
                                    sum += progress;
                                    count++;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        // Calculate average
                        double averageValue = (count > 0) ? (sum / count) : 0;

                        // Update the text views with min, max, and average values
                        updateMinMaxAvgValues((float) minValue, (float) maxValue, (float) averageValue);

                        updateLineChart(entries);

                    } else {
                        Log.e("Firestore", "Error fetching progress logs: ", task.getException());
                        Toast.makeText(this, "Failed to load progress data. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fillMissingLogsBetweenDates(String taskId, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        CollectionReference logsCollection = db.collection("users").document(userId)
                .collection("Goal").document("targetTasks").collection("target_tasks")
                .document(taskId).collection("loggedLogs");

        try {
            if (startDate != null && endDate != null) {
                Date start = dateFormat.parse(startDate);
                Date end = dateFormat.parse(endDate);
                calendar.setTime(start);

                List<Task<Void>> tasks = new ArrayList<>();

                while (!calendar.getTime().after(end)) {
                    Date currentDate = calendar.getTime();
                    Task<Void> task = checkAndCreateLog(logsCollection, currentDate);
                    tasks.add(task);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }

                Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> fetchAndDisplayProgressData(7));
            } else {
                Log.e("Debug", "startDate or endDate is null");
                // Handle this situation as per your application's logic
            }
        } catch (ParseException e) {
            Log.e("Debug", "Error parsing date: ", e);
        }
    }

    private Task<Void> checkAndCreateLog(CollectionReference logsCollection, Date logDate) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        logsCollection.whereEqualTo("date", logDate).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot.isEmpty()) {
                    // If log does not exist for the date, create it
                    createLog(logsCollection, logDate).addOnCompleteListener(logTask -> {
                        if (logTask.isSuccessful()) {
                            taskCompletionSource.setResult(null);
                        } else {
                            taskCompletionSource.setException(logTask.getException());
                        }
                    });
                } else {
                    Log.d("Debug", "Log already exists for date: " + logDate);
                    taskCompletionSource.setResult(null);
                }
            } else {
                Log.e("Debug", "Error fetching logs: ", task.getException());
                taskCompletionSource.setException(task.getException());
            }
        });

        return taskCompletionSource.getTask();
    }

    private Task<Void> createLog(CollectionReference logsCollection, Date logDate) {
        target_log newLog = new target_log(UUID.randomUUID().toString(), 0, "", logDate);
        return logsCollection.document().set(newLog)
                .addOnSuccessListener(aVoid -> Log.d("Debug", "Log created for date: " + logDate))
                .addOnFailureListener(e -> Log.e("Debug", "Error creating log: ", e));
    }

    private void updateMinMaxAvgValues(float minValue, float maxValue, float avgValue) {
        minValueTextView.setText(String.valueOf(minValue));
        maxValueTextView.setText(String.valueOf(maxValue));
        avgValueTextView.setText(String.valueOf(avgValue)); // Update average value TextView
    }


    private void updateLineChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Progress");

        // Customize line dataset if needed (color, thickness, etc.)
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);

        // Configure X axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Display X axis at the bottom
        xAxis.setGranularity(1f); // Minimum axis-step (interval) is 1
        xAxis.setLabelCount(entries.size(), true); // Ensure labels match number of entries
        xAxis.setLabelRotationAngle(45f); // Rotate labels for better readability
        xAxis.setDrawGridLines(false); // Disable grid lines on the X-axis

        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                // Convert float value to Date and format it
                return dateFormat.format(new Date((long) value));
            }
        });

        // Configure Y axis (left)
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false); // Disable grid lines on the left axis


        try {
            float startGoalValue = Float.parseFloat(startGoal);
            leftAxis.setAxisMinimum(startGoalValue); // Set the minimum value to start goal
        } catch (NumberFormatException e) {
            Log.e("updateLineChart", "Invalid startGoal value: " + startGoal);
        }

        try {
            float endGoalValue = Float.parseFloat(endGoal);
            float maxValue = getMaxYValue(entries);
            leftAxis.setAxisMaximum(Math.max(endGoalValue, maxValue)); // Set the maximum value to the higher of end goal or max entry value

            // Add a limit line for the end goal value
            LimitLine limitLine = new LimitLine(endGoalValue, "");
            limitLine.setLineWidth(2f);
            limitLine.setLineColor(Color.parseColor("#388E3C")); // Set the specific green color
            limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            limitLine.setTextSize(10f);
            limitLine.enableDashedLine(10f, 10f, 0f);
            leftAxis.addLimitLine(limitLine);
        } catch (NumberFormatException e) {
            Log.e("updateLineChart", "Invalid endGoal value: " + endGoal);
        }

        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Format Y axis values as needed
                return "" + ((int) value); // Display integers
            }
        });

        // Configure Y axis (right)
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y axis

        // Remove description label
        Description description = new Description();
        description.setEnabled(false);
        lineChart.setDescription(description);

        // Disable the legend
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        // Apply data to chart
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh chart

        addLegendItems();
    }

    private float getMaxYValue(List<Entry> entries) {
        float max = 0f;
        for (Entry entry : entries) {
            if (entry.getY() > max) {
                max = entry.getY();
            }
        }
        return max;
    }

    private void addLegendItems() {
        LinearLayout legendLayout = findViewById(R.id.legendLayout);
        legendLayout.removeAllViews(); // Clear existing legend items

        int lessBrightGreen = Color.parseColor("#388E3C"); // Less bright green

        // Item
        addLegendItem(legendLayout, lessBrightGreen, "Goal");
        addLegendItem(legendLayout, Color.BLUE, "Your Progress");
    }

    private void addLegendItem(LinearLayout legendLayout, int color, String label) {
        // Create a new LinearLayout for each legend item
        LinearLayout legendItemLayout = new LinearLayout(this);
        legendItemLayout.setOrientation(LinearLayout.HORIZONTAL);
        legendItemLayout.setGravity(Gravity.CENTER_VERTICAL);
        legendItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        legendItemLayout.setPadding(10, 10, 10, 10);

        // Create a colored box
        View colorBox = new View(this);
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(30, 30);
        boxParams.setMargins(0, 0, 20, 0);
        colorBox.setLayoutParams(boxParams);
        colorBox.setBackgroundColor(color);

        // Create a TextView for the label
        TextView labelTextView = new TextView(this);
        labelTextView.setText(label);
        labelTextView.setTextSize(16);
        labelTextView.setTextColor(Color.BLACK);

        // Add the colored box and label to the legend item layout
        legendItemLayout.addView(colorBox);
        legendItemLayout.addView(labelTextView);

        // Add the legend item layout to the legend layout
        legendLayout.addView(legendItemLayout);
    }
}
