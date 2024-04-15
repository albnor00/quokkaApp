package com.example.quokka.goal_progress_tracking.goal_page;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;

public class GoalDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail);

        // Get data from intent
        Intent intent = getIntent();
        String goalName = intent.getStringExtra("goal_name");
        String goalDescription = intent.getStringExtra("goal_description");

        // Display goal details in the activity
        TextView nameTextView = findViewById(R.id.goal_details_name);
        TextView descriptionTextView = findViewById(R.id.goal_details_description);

        nameTextView.setText(goalName);
        descriptionTextView.setText(goalDescription);
    }
}