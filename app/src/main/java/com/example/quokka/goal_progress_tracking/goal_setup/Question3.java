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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Question3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_goal_question3);

        fetchAndDisplayPreviousExplanation();

        Button nextQuestion = findViewById(R.id.next_question);
        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserExplanation();

                moveToQuestion4();
            }
        });


        ImageView back_btn = findViewById(R.id.img_back);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToQuestion2();
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
                    .document("Q3_user_explanation") // Use a specific document ID for the user's data
                    .set(userData) // Use update to add or update fields within the document
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Question3.this, "Explanation saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Question3.this, "Failed to save explanation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    .document("Q3_user_explanation")
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
                        Toast.makeText(Question3.this, "Failed to fetch previous explanation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void moveToQuestion2() {
        Intent intent = new Intent(getApplicationContext(), Question2.class);
        startActivity(intent);
        finish(); // Finish current activity
    }

    private void moveToQuestion4() {
        Intent intent = new Intent(getApplicationContext(), Question4.class);
        startActivity(intent);
        finish(); // Finish current activity
    }
}
