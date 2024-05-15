package com.example.quokka.goal_progress_tracking.average_task_template;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class average_task_page extends AppCompatActivity {
    private ArrayList<BarEntry> barArrayList;
    private BarChart barChart;
    private EditText editLogValue;
    private TextView averageTextView;
    private YAxis leftAxis;
    private float goalLimit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_task_page);

        // Initialize views
        ImageView backBtn = findViewById(R.id.img_back);
        barChart = findViewById(R.id.bar_chart);
        editLogValue = findViewById(R.id.edit_text_log_value);
        averageTextView = findViewById(R.id.text_average_value);

        // Value for the goal line
        goalLimit = getRandomValue();

        // Populate initial data for the last 7 days
        getDataForLast7Days();
        setupBarChart();

        // Handle back button click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getDataForLast7Days() {
        barArrayList = new ArrayList<>();

        // Generate dates for the last 7 days
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            Date date = calendar.getTime();
            String formattedDate = dateFormat.format(date);

            // Simulate data (replace with actual data retrieval)
            float logValue = getRandomValue(); // Replace with your actual data retrieval method
            BarEntry entry = new BarEntry(i, logValue);
            barArrayList.add(entry);
        }
    }

    private void setupBarChart() {
        BarDataSet barDataSet = new BarDataSet(barArrayList, "Logged Average");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Customize chart appearance
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        // Show chart description
        barChart.getDescription().setEnabled(true);

        // Format x-axis with date labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getXAxisLabels()));
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


        // Refresh chart
        barChart.invalidate();
    }

    private float calculateMaximumBarValue() {
        float maxBarValue = 0f;
        for (BarEntry entry : barArrayList) {
            if (entry.getY() > maxBarValue) {
                maxBarValue = entry.getY();
            }
        }
        return maxBarValue;
    }

    private ArrayList<String> getXAxisLabels() {
        ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            Date date = calendar.getTime();
            labels.add(dateFormat.format(date));
        }
        return labels;
    }

    private float getRandomValue() {
        // Simulated method to get random log values (replace with actual data retrieval)
        return (float) (Math.random() * 100);
    }

    public void addLog(View view) {
        String logValueText = editLogValue.getText().toString();
        if (!logValueText.isEmpty()) {
            float logValue = Float.parseFloat(logValueText);

            // Get today's date as a string in the same format used for x-axis labels
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            String todayDate = dateFormat.format(new Date());

            // Check if a bar entry for today's date already exists
            boolean entryExists = false;
            int indexToUpdate = -1;
            for (int i = 0; i < barArrayList.size(); i++) {
                BarEntry entry = barArrayList.get(i);
                if (getXAxisLabels().get((int) entry.getX()).equals(todayDate)) {
                    entryExists = true;
                    indexToUpdate = i;
                    break;
                }
            }

            // Update the existing bar entry for today's date or add a new one
            if (entryExists && indexToUpdate != -1) {
                BarEntry existingEntry = barArrayList.get(indexToUpdate);
                existingEntry.setY(existingEntry.getY() + logValue); // Update the y-value
            } else {
                // Add a new bar entry for today's date
                BarEntry newEntry = new BarEntry(barArrayList.size(), logValue);
                barArrayList.add(newEntry);
            }

            // Recalculate and display the average for the last 7 days
            calculateAndDisplayAverage();

            // Update chart with new data
            updateBarChart();
            editLogValue.getText().clear(); // Clear input field
        }
    }

    private void calculateAndDisplayAverage() {
        int numDays = 7;
        float sum = 0;

        // Calculate sum of y-values for the last 7 entries
        int startIndex = Math.max(0, barArrayList.size() - numDays);
        for (int i = startIndex; i < barArrayList.size(); i++) {
            sum += barArrayList.get(i).getY();
        }

        // Calculate average
        float average = sum / numDays;

        // Display the average in TextView
        averageTextView.setText(String.format(Locale.getDefault(), "%.2f", average));

    }

    private void updateBarChart() {
        // Update chart with new data
        barChart.notifyDataSetChanged();
        barChart.invalidate();

        // Configure axis settings to adapt to dynamic data
        leftAxis.setAxisMinimum(0f);  // Minimum value (start from zero)
        leftAxis.setAxisMaximum(calculateMaximumBarValue() * 1.5f); // Adjust maximum value with a buffer


        // Remove existing limit lines before adding a new one
        leftAxis.removeAllLimitLines();

        // Draw new average line
        drawAverageLine();

        // Re-draw the goal line
        drawGoalLine(goalLimit);

    }

    private void drawAverageLine() {
        // Calculate the average for the last 7 days
        int numDays = 7;
        float sum = 0;
        int startIndex = Math.max(0, barArrayList.size() - numDays);
        for (int i = startIndex; i < barArrayList.size(); i++) {
            sum += barArrayList.get(i).getY();
        }
        float average = sum / numDays;

        // Create a limit line for the average value
        LimitLine limitLine = new LimitLine(average, "Average");
        limitLine.setLineWidth(2f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        limitLine.setTextSize(10f);

        // Add the limit line to the left Y-axis
        leftAxis.addLimitLine(limitLine);
    }

    private void drawGoalLine(float goal){

        // Create a limit line for the goal value
        LimitLine goalLimitLine = new LimitLine(goal, "Goal");
        goalLimitLine.setLineWidth(2f);
        goalLimitLine.enableDashedLine(10f, 10f, 0f);
        goalLimitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        goalLimitLine.setLineColor(Color.BLUE); // Set the color here
        goalLimitLine.setTextSize(10f);

        // Add the goal limit line to the left Y-axis
        leftAxis.addLimitLine(goalLimitLine);
    }
}