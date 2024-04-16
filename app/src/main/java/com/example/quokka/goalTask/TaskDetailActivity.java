package com.example.quokka.goalTask;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {
    ImageView back_btn;
    ImageView check_btn;
    boolean isChecked = false;
    String taskId; // Unique task ID
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail_activity);

        // Initialize views
        back_btn = findViewById(R.id.task_back);
        check_btn = findViewById(R.id.circleWithCheck);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve taskId from intent extras
        taskId = getIntent().getStringExtra("task_id");

        // Set up back button click listener
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskDetailActivity.this, goalTask_mainpage.class);
                startActivity(intent);
                finish();
            }
        });

        // Set up check button click listener
        check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the task has already been completed today
                if (isChecked) {
                    Toast.makeText(TaskDetailActivity.this, "Task already completed today!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update completion status and timestamp
                isChecked = true;
                updateTaskCompletionStatus(taskId);

                // Update UI
                check_btn.setImageResource(R.drawable.circle_check_green_btn);
                Toast.makeText(TaskDetailActivity.this, "Task completed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTaskCompletionStatus(String taskId) {
        // Get the current timestamp in milliseconds
        long currentTimestamp = System.currentTimeMillis();

        // Update Firestore document with completion status and timestamp
        db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("todo_tasks")
                .document(taskId)
                .update("completed", true, "lastCompletedTimestamp", currentTimestamp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task completion status updated successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure to update task completion status
                        Toast.makeText(TaskDetailActivity.this, "Failed to update task completion status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
