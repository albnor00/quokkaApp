package com.example.quokka.goal_progress_tracking.average_task_template;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class average_task_page extends AppCompatActivity {
    private ArrayList<BarEntry> barArrayList;
    private BarChart barChart;
    private TextView averageTextView;
    private TextView averageDescription;
    private TextView successRateTextView;
    private TextView successRateDescription;
    private YAxis leftAxis;
    private float goalLimit;
    private SimpleDateFormat dateFormat;
    private ArrayList<String> dateLabels;
    private HashMap<String, Float> dateLogMap;

    private Spinner spinnerTimePeriod;
    private int selectedTimePeriod = 7; //Default

    //Intent variables
    private String taskName;
    private String taskDescription;
    private String goal;
    private String timePeriod;
    private String startDate;
    private int taskPosition;
    private String taskId;
    private boolean isMoreOrLess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_task_page);

        // Initialize views
        ImageView backBtn = findViewById(R.id.img_back);
        ImageView imgMenu = findViewById(R.id.img_menu);
        TextView title = findViewById(R.id.text_task_name_at_top);
        TextView description = findViewById(R.id.text_task_description);
        TextView goalLabel = findViewById(R.id.text_task_goal);
        barChart = findViewById(R.id.bar_chart);
        averageTextView = findViewById(R.id.text_average_value);
        successRateTextView = findViewById(R.id.text_success_rate_value);
        spinnerTimePeriod = findViewById(R.id.spinner_time_period);
        successRateDescription = findViewById(R.id.text_success_rate_description);
        averageDescription = findViewById(R.id.text_average_description);

        // Get task information from intent
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra("taskId");
            taskDescription = intent.getStringExtra("taskDescription");
            taskName = intent.getStringExtra("taskName");
            goal = intent.getStringExtra("goal");
            timePeriod = intent.getStringExtra("timePeriod");
            startDate = intent.getStringExtra("startDate");
            taskPosition = intent.getIntExtra("taskPosition", -1);
        }

        // Call the method to fetch isMoreOrLess value
        isItMoreOrLess();

        // Set description
        getDescriptionFromFirestore();

        // Initialize the map
        dateLogMap = new HashMap<>();

        // Value for the goal line
        goalLimit = Float.parseFloat(goal);

        // Set title
        title.setText(taskName);

        // Set description
        description.setText(taskDescription);

        // Set goal label
        goalLabel.setText("Current Goal: " + goal);

        // Initialize date format
        dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.time_period_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimePeriod.setAdapter(adapter);
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPeriod = parent.getItemAtPosition(position).toString();
                isItMoreOrLess();
                switch (selectedPeriod) {
                    case "7 days":
                        selectedTimePeriod = 7;
                        successRateDescription.setText("Over the last " + 7 + " days");
                        averageDescription.setText("Over the last " + 7 + " days");
                        break;
                    case "14 days":
                        selectedTimePeriod = 14;
                        successRateDescription.setText("Over the last " + 14 + " days");
                        averageDescription.setText("Over the last " + 14 + " days");
                        break;
                    case "30 days":
                        selectedTimePeriod = 30;
                        successRateDescription.setText("Over the last " + 30 + " days");
                        averageDescription.setText("Over the last " + 30 + " days");
                        break;
                }
                fetchDataFromFirestore(taskId, selectedTimePeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void isItMoreOrLess() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("Goal").document("averageTasks").collection("tasks")
                .document(taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            isMoreOrLess = document.getBoolean("goalMoreOrLess");
                            Log.d("Firestore", "goalMoreOrLess: " + isMoreOrLess);
                        }
                    } else {
                        Log.e("Firestore", "Error getting More or Less: ", task.getException());
                        Toast.makeText(this, "Failed to load goalMoreOrLess. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getDescriptionFromFirestore() {
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
                            taskDescription = document.getString("taskDescription");
                            // Update the description TextView
                            TextView description = findViewById(R.id.text_task_description);
                            description.setText(taskDescription);
                        }
                    } else {
                        Log.e("Firestore", "Error getting description: ", task.getException());
                        Toast.makeText(this, "Failed to load description. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
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
            Intent intent = new Intent(getApplicationContext(), average_task_log_history_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent.putExtra("taskName", taskName);
            intent.putExtra("taskDescription", taskDescription);
            intent.putExtra("goal", goal);
            intent.putExtra("timePeriod", timePeriod);
            intent.putExtra("startDate", startDate);

            // Pass the position of the clicked task
            intent.putExtra("taskPosition", taskPosition);
            intent.putExtra("taskId", taskId);

            // Start the activity
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            // Create an intent to start the add_new_average_log activity
            Intent intent = new Intent(getApplicationContext(), average_task_settings_page.class);

            // Pass necessary data to the add_new_average_log activity
            intent.putExtra("taskName", taskName);
            intent.putExtra("taskDescription", taskDescription);
            intent.putExtra("goal", goal);
            intent.putExtra("timePeriod", timePeriod);
            intent.putExtra("startDate", startDate);

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

    private void fetchDataFromFirestore(String taskId, int timePeriod) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Query the specific logs for the task using taskId
        db.collection("users").document(userId)
                .collection("Goal").document("averageTasks").collection("average_tasks")
                .document(taskId).collection("loggedLogs")
                .orderBy("date", Query.Direction.ASCENDING) // Ensure logs are ordered by date
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        barArrayList = new ArrayList<>();
                        dateLabels = new ArrayList<>();
                        dateLogMap = new HashMap<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            average_log log = document.toObject(average_log.class);
                            Date logDate = log.getDate();
                            String formattedDate = dateFormat.format(logDate); // Ensure that dateFormat is correctly initialized

                            // Aggregate log values by date
                            if (dateLogMap.containsKey(formattedDate)) {
                                dateLogMap.put(formattedDate, dateLogMap.get(formattedDate) + log.getLog());
                            } else {
                                dateLogMap.put(formattedDate, (float) log.getLog());
                            }
                        }

                        // Fill in missing logs for the past 7 days
                        fillMissingLogsAndSaveToFirestore(taskId, timePeriod);

                        // Create entries for the bar chart
                        createBarChartEntries(timePeriod);

                        // Setup the bar chart with the fetched data
                        setupBarChart();
                        calculateAndDisplayAverage(timePeriod);
                        calculateAndDisplayStreak();
                        calculateAndDisplaySuccessRate(timePeriod);
                    } else {
                        Log.e("Firestore", "Error getting logs: ", task.getException());
                        Toast.makeText(this, "Failed to load logs. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fillMissingLogsAndSaveToFirestore(String taskId, int timePeriod) {
        if (dateLogMap.isEmpty()) {
            Log.d("Debug", "dateLogMap is empty.");
            return;
        }

        // Use a SimpleDateFormat that matches the date strings in dateLogMap
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        Calendar currentYearCalendar = Calendar.getInstance();
        int currentYear = currentYearCalendar.get(Calendar.YEAR);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        CollectionReference logsCollection = db.collection("users").document(userId)
                .collection("Goal").document("averageTasks").collection("average_tasks")
                .document(taskId).collection("loggedLogs");

        List<Date> sortedDates = new ArrayList<>();
        for (String dateString : dateLogMap.keySet()) {
            try {
                // Parse date string and set the year to the current year
                Date parsedDate = dateFormat.parse(dateString);
                Calendar parsedCalendar = Calendar.getInstance();
                parsedCalendar.setTime(parsedDate);
                parsedCalendar.set(Calendar.YEAR, currentYear); // Set the current year
                sortedDates.add(parsedCalendar.getTime());

                // Log the parsed dates
                Log.d("Debug", "Parsed Date: " + dateFormat.format(parsedCalendar.getTime()));
            } catch (ParseException e) {
                Log.e("Debug", "Error parsing date: " + dateString, e);
            }
        }

        // Sort dates in ascending order
        Collections.sort(sortedDates);

        // Ensure we have logs for the last selected days
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -timePeriod); // Move selected days back
        Date selectedDaysAgo = calendar.getTime();

        while (!calendar.getTime().after(new Date())) {
            String dateString = dateFormat.format(calendar.getTime());
            if (!dateLogMap.containsKey(dateString)) {
                if (sortedDates.size() < timePeriod || calendar.getTime().after(selectedDaysAgo)) {
                    dateLogMap.put(dateString, 0f); // Add missing date with zero value

                    // Generate a unique ID for the log
                    String logId = UUID.randomUUID().toString(); // Using UUID to generate a random ID

                    // Create a new log object with the generated ID and correct date
                    Date newDate = calendar.getTime();
                    average_log newLog = new average_log(logId, 0, "", newDate);

                    // Check if log already exists before adding
                    logsCollection.document(logId).get().addOnCompleteListener(task -> {
                        if (!task.isSuccessful() || !task.getResult().exists()) {
                            // Log does not exist, create a new one
                            logsCollection.document(logId).set(newLog);
                        }
                    });
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }


    private void createBarChartEntries(int timePeriod) {
        barArrayList = new ArrayList<>();
        dateLabels = new ArrayList<>();

        // Get the current date
        Calendar currentDate = Calendar.getInstance();

        // Iterate over the last seven days
        for (int i = timePeriod - 1; i >= 0; i--) {
            // Calculate the date for the current iteration
            Calendar date = (Calendar) currentDate.clone();
            date.add(Calendar.DAY_OF_YEAR, -i);

            // Format the date string
            String dateString = dateFormat.format(date.getTime());

            // Get the log value for the date (or 0 if no log exists)
            float logValue = dateLogMap.containsKey(dateString) ? dateLogMap.get(dateString) : 0f;

            // Add a bar entry for the date
            barArrayList.add(new BarEntry(timePeriod - 1 - i, logValue));
            dateLabels.add(dateString);
        }
    }

    private void setupBarChart() {
        if (barArrayList == null) {
            barArrayList = new ArrayList<>();
        }

        BarDataSet barDataSet = new BarDataSet(barArrayList, "");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);



        // Customize chart appearance
        if (selectedTimePeriod == 30) {
            barDataSet.setDrawValues(false); // Hide values for 30 days
        } else {
            barDataSet.setValueTextColor(Color.BLACK);
            barDataSet.setValueTextSize(16f); // Show values for other time periods
        }

        // Define custom colors
        int lessBrightGreen = Color.parseColor("#388E3C"); // Less bright green
        int red = Color.RED; // Red

        // Set colors for bars based on their values and isMoreOrLess flag
        ArrayList<Integer> colors = new ArrayList<>();
        for (BarEntry entry : barArrayList) {
            if (isMoreOrLess) {
                if (entry.getY() >= goalLimit) {
                    colors.add(red); // Above goal limit
                } else {
                    colors.add(lessBrightGreen); // Below goal limit
                }
            } else {
                if (entry.getY() >= goalLimit) {
                    colors.add(lessBrightGreen); // above limit line
                } else {
                    colors.add(red); // below limit line
                }
            }
        }
        barDataSet.setColors(colors);

        // Format x-axis with date labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45f);
        xAxis.setDrawGridLines(false);

        // Get left Y-axis
        leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false); // Disable grid lines on the left Y-axis

        // Disable the right Y-axis and its labels
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable the right Y-axis

        // Draw average line
        drawAverageLine();

        // Draw Goal line (only drawn once)
        drawGoalLine(goalLimit);

        // Disable the legend
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        // Remove the description
        barChart.getDescription().setEnabled(false);

        barChart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                barChart.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                barChart.setExtraBottomOffset(20f); // Increase bottom offset to ensure x-axis labels are not cut off
                barChart.invalidate(); // Refresh chart
            }
        });

        // Refresh chart
        barChart.invalidate();

        // Add legend items
        addLegendItems();
    }

    private void addLegendItems() {
        LinearLayout legendLayout = findViewById(R.id.legendLayout);
        legendLayout.removeAllViews(); // Clear existing legend items

        if (isMoreOrLess) {
            addLegendItem(legendLayout, Color.BLUE, "Limit");
        } else {
            addLegendItem(legendLayout, Color.BLUE, "Goal");
        }

        // Average Legend Item
        addLegendItem(legendLayout, Color.RED, "Average");
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

    private void drawAverageLine() {
        // Clear existing limit lines before adding new ones
        leftAxis.removeAllLimitLines();

        // Calculate the average for the selected time period
        int numDays = Math.min(barArrayList.size(), selectedTimePeriod);
        float sum = 0;
        int count = 0;

        // Calculate sum of y-values and count the entries for the selected time period
        for (int i = 0; i < numDays; i++) {
            sum += barArrayList.get(i).getY();
            count++;
        }

        // Calculate average
        float average = (count == 0) ? 0 : sum / count;

        // Create a limit line for the average value
        LimitLine limitLine = new LimitLine(average, "");
        limitLine.setLineWidth(2f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        limitLine.setTextSize(10f);

        // Add the limit line to the left Y-axis
        leftAxis.addLimitLine(limitLine);
    }

    private void drawGoalLine(float goal) {
        // Create a limit line for the goal value
        LimitLine goalLimitLine = new LimitLine(goal, "");
        goalLimitLine.setLineWidth(2f);
        goalLimitLine.enableDashedLine(10f, 10f, 0f);
        goalLimitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        goalLimitLine.setLineColor(Color.BLUE); // Set the color here
        goalLimitLine.setTextSize(10f);

        // Add the goal limit line to the left Y-axis
        leftAxis.addLimitLine(goalLimitLine);
    }

    private void calculateAndDisplayAverage(int timePeriod) {
        int numDays = Math.min(barArrayList.size(), timePeriod); // Use the minimum of the list size and 7
        float sum = 0;
        int count = 0;

        // Calculate sum of y-values and count the entries for the last 7 entries
        for (int i = 0; i < numDays; i++) {
            sum += barArrayList.get(i).getY();
            count++;
        }

        // Calculate average
        float average = (count == 0) ? 0 : sum / count;

        // Display the average in TextView
        averageTextView.setText(String.format(Locale.getDefault(), "%.2f", average));
    }

    private void calculateAndDisplaySuccessRate(int timePeriod) {
        if (barArrayList.isEmpty()) {
            successRateTextView.setText("0%");
            return;
        }

        int successfulDays = 0;

        // Iterate over the bar entries to calculate the success rate
        for (BarEntry entry : barArrayList) {
            if (isMoreOrLess) {
                // If isMoreOrLess is true, consider entries less than the goal limit as successful
                if (entry.getY() < goalLimit) {
                    successfulDays++;
                }
            } else {
                // If isMoreOrLess is false, consider entries greater than or equal to the goal limit as successful
                if (entry.getY() >= goalLimit) {
                    successfulDays++;
                }
            }
        }

        // Calculate the success rate as a percentage
        float successRate = (successfulDays / (float) timePeriod) * 100;

        // Update the success rate text view
        successRateTextView.setText(String.format(Locale.getDefault(), "%.2f%%", successRate));
    }

    private void calculateAndDisplayStreak() {
        if (dateLogMap.isEmpty()) {
            // No logs available
            return;
        }

        List<Date> sortedDates = new ArrayList<>();
        for (String dateString : dateLogMap.keySet()) {
            try {
                // Parse the date string and set the year to the current year
                Date date = dateFormat.parse(dateString);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                sortedDates.add(calendar.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Sort dates in descending order to start from the most recent date
        Collections.sort(sortedDates, Collections.reverseOrder());

        int latestStreak = 0; // Initialize streak count
        boolean endCountFlag = false;

        for (int i = 0; i < sortedDates.size(); i++) {
            Date currentDate = sortedDates.get(i);
            Date nextDate = (i < sortedDates.size() - 1) ? sortedDates.get(i + 1) : null;

            // Check if the log value for the current date is zero or if there's a gap in dates
            if (dateLogMap.get(dateFormat.format(currentDate)) == 0 || (nextDate != null && isDateGap(currentDate, nextDate))) {
                endCountFlag = true;
            } else {
                latestStreak++; // increment streak
            }

            if (endCountFlag){
                // Display the streak
                TextView streakTextView = findViewById(R.id.text_streak_value);
                streakTextView.setText(String.valueOf(latestStreak));

                return;
            } else {
                if(i == sortedDates.size() - 1){
                    TextView streakTextView = findViewById(R.id.text_streak_value);
                    streakTextView.setText(String.valueOf(latestStreak));
                    return;
                }
            }
        }


    }

    // Method to check if there is a gap in dates
    private boolean isDateGap(Date currentDate, Date nextDate) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTime(nextDate);
        return (nextCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis() > TimeUnit.DAYS.toMillis(1));
    }

}