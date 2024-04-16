package com.example.quokka.goal_progress_tracking.task_templates;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_page.GoalDetailsActivity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

public class TaskDetailActivity extends AppCompatActivity {
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        Intent intent = getIntent();
        if (intent != null) {
            String taskName = intent.getStringExtra("task_name");
            String taskDescription = intent.getStringExtra("task_description");

            // Retrieve the list of Serializable entries
            ArrayList<Serializable> serializableEntries = (ArrayList<Serializable>) intent.getSerializableExtra("task_entries");

            // Now you have all the task details, create an Average_template instance
            Average_template averageTemplate = new Average_template(taskName, taskDescription, 0.0); // 0.0 as a placeholder for target

            // Iterate through the list of serializable entries
            for (int i = 0; i < serializableEntries.size(); i += 2) {
                LocalDate date = (LocalDate) serializableEntries.get(i);
                Double value = (Double) serializableEntries.get(i + 1);
                averageTemplate.addDailyEntry(date, value);
            }

            // Calculate and display average value
            double averageValue = averageTemplate.calculateAverage();

            // Display task details and average value in the activity
            TextView nameTextView = findViewById(R.id.task_details_name);
            TextView descriptionTextView = findViewById(R.id.task_details_description);
            TextView averageTextView = findViewById(R.id.task_average_value);

            nameTextView.setText(taskName);
            descriptionTextView.setText(taskDescription);
            averageTextView.setText(String.format("%.2f", averageValue)); // Format average value

            barChart = findViewById(R.id.barChart);

            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(1, 20f));
            entries.add(new BarEntry(2, 35f));
            entries.add(new BarEntry(3, 28f));
            entries.add(new BarEntry(4, 32f));
            entries.add(new BarEntry(5, 25f));

            BarDataSet dataSet = new BarDataSet(entries, "Bar Chart Example");
            BarData barData = new BarData(dataSet);
            barChart.setData(barData);
            barChart.invalidate(); // Refresh the chart
        }



        ImageView back_btn = findViewById(R.id.img_back);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GoalDetailsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


}

