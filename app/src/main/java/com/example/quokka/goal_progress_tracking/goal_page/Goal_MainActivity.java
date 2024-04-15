package com.example.quokka.goal_progress_tracking.goal_page;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.R;

import java.util.ArrayList;
import java.util.List;

public class Goal_MainActivity extends AppCompatActivity {

    private List<Goal> goals = new ArrayList<>();
    private GoalAdapter goalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_mainpage);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        goalAdapter = new GoalAdapter(this, goals);
        recyclerView.setAdapter(goalAdapter);

        goals.add(new Goal("Goal 1", "Description for Goal 1"));
        goals.add(new Goal("Goal 2", "Description for Goal 2"));



        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddGoalDialog();
            }
        });
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Goal");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        final EditText nameEditText = view.findViewById(R.id.edit_text_name);
        final EditText descriptionEditText = view.findViewById(R.id.edit_text_description);

        builder.setView(view);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameEditText.getText().toString();
                String description = descriptionEditText.getText().toString();

                Goal newGoal = new Goal(name, description);
                goals.add(newGoal);
                goalAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }
}