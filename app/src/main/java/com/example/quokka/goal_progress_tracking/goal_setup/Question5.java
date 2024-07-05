package com.example.quokka.goal_progress_tracking.goal_setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_empty_page;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Question5 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_goal_question_5);

        fetchAndDisplayPreviousExplanation();

        Button nextQuestion = findViewById(R.id.next_question);
        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserExplanation();
                setGoalSetupTaskToCompleted();
                moveToNonEmptyGoalPage();
            }
        });


        ImageView back_btn = findViewById(R.id.img_back);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToQuestion4();
            }
        });
    }

    private void saveUserExplanation() {
        EditText answerEditText = findViewById(R.id.answerEditText);
        String userExplanation = answerEditText.getText().toString().trim();

        // Save user's explanation to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            // Create a map to represent the user's data
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_explanation", userExplanation);

            // Save the user's explanation under the "Goal" collection
            db.collection("users").document(userId).collection("Goal")
                    .document("Q5_user_explanation") // Use a specific document ID for the user's data
                    .set(userData) // Use update to add or update fields within the document
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Question5.this, "Explanation saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Question5.this, "Failed to save explanation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void fetchAndDisplayPreviousExplanation() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Retrieve previously saved user explanation from Firestore
            db.collection("users").document(userId).collection("Goal")
                    .document("Q5_user_explanation")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String previousExplanation = documentSnapshot.getString("user_explanation");
                            // Display previous explanation in EditText (if needed)
                            EditText answerEditText = findViewById(R.id.answerEditText);
                            answerEditText.setText(previousExplanation);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Question5.this, "Failed to fetch previous explanation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setGoalSetupTaskToCompleted() {

        // Save user's goal state to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            // Create a map to represent the user's data
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_goal_setup_state", "Completed");

            // Save the user's explanation under the "Goal" collection
            db.collection("users").document(userId).collection("Goal")
                    .document("User_Goal_State") // Use a specific document ID for the user's data
                    .set(userData) // Use update to add or update fields within the document
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Question5.this, "Goal saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Question5.this, "Failed to save goal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void moveToQuestion4() {
        Intent intent = new Intent(getApplicationContext(), Question4.class);
        startActivity(intent);
        finish(); // Finish current activity
    }

    private void moveToNonEmptyGoalPage(){
        Intent intent = new Intent(getApplicationContext(), Goal_non_empty_page.class);
        startActivity(intent);
        finish(); // Finish current activity
    }
}
