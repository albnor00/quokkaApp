package com.example.quokka.goal_progress_tracking.goal_page_v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_setup.Question1;
import com.example.quokka.goal_progress_tracking.goal_setup.Question4;
import com.example.quokka.goal_progress_tracking.goal_setup.Question5;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Goal_empty_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_goal_page);



        Button addButton = findViewById(R.id.getting_started_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGoalSetupTaskToInitiated();
                moveToQuestion1();
            }
        });


        ImageView back_btn = findViewById(R.id.img_back);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setGoalSetupTaskToInitiated() {

        // Save user's goal state to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            // Create a map to represent the user's data
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_goal_setup_state", "Initiated");

            // Save the user's explanation under the "Goal" collection
            db.collection("users").document(userId).collection("Goal")
                    .document("User_Goal_State") // Use a specific document ID for the user's data
                    .set(userData) // Use update to add or update fields within the document
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Goal_empty_page.this, "Goal initiated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Goal_empty_page.this, "Failed to initiate goal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void moveToQuestion1() {
        Intent intent = new Intent(getApplicationContext(), Question1.class);
        startActivity(intent);
        finish(); // Finish current activity
    }
}
