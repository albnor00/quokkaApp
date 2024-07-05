package com.example.quokka.goal_progress_tracking.goal_setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
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

public class Question4 extends AppCompatActivity {
    private SeekBar ratingSeekBar;
    private TextView currentRatingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_goal_question_4);

        // Initialize views
        ratingSeekBar = findViewById(R.id.ratingSeekBar);
        currentRatingTextView = findViewById(R.id.currentRatingTextView);

        // Set initial text for current rating
        updateCurrentRating(ratingSeekBar.getProgress() + 1); // Add 1 to account for range adjustment

        fetchAndDisplayPreviousRating();

        ratingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the TextView with the current SeekBar progress
                updateCurrentRating(progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle touch event (optional)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle touch release (optional)
            }
        });

        Button nextQuestion = findViewById(R.id.next_question);
        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedRating = ratingSeekBar.getProgress() + 1; // Add 1 to convert 0-9 to 1-10 range

                saveRatingToFirestore(selectedRating);

                moveToQuestion5();
            }
        });


        ImageView back_btn = findViewById(R.id.img_back);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToQuestion3();
            }
        });
    }

    // Helper method to update the current rating TextView
    private void updateCurrentRating(int rating) {
        currentRatingTextView.setText("Current Rating: " + rating);
    }

    private void saveRatingToFirestore(int selectedRating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            // Create a map to store the rating data
            Map<String, Object> ratingData = new HashMap<>();
            ratingData.put("selectedRating", selectedRating); // Store the selected rating

            // Add the rating data to the "Goals" collection for the user
            db.collection("users").document(userId).collection("Goal")
                    .document("Q4_user_goal_rating")
                    .set(ratingData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Question4.this, "Rating saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Question4.this, "Failed to save rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void fetchAndDisplayPreviousRating() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Retrieve previously selected rating from Firestore
            db.collection("users").document(userId).collection("Goal")
                    .document("Q4_user_goal_rating")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            int previousRating = documentSnapshot.getLong("selectedRating").intValue();
                            ratingSeekBar.setProgress(previousRating - 1); // Set progress (0-9) based on previous rating (1-10)
                            updateCurrentRating(previousRating); // Update TextView with previous rating
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Question4.this, "Failed to fetch previous rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void moveToQuestion3() {
        Intent intent = new Intent(getApplicationContext(), Question3.class);
        startActivity(intent);
        finish(); // Finish current activity
    }

    private void moveToQuestion5() {
        Intent intent = new Intent(getApplicationContext(), Question5.class);
        startActivity(intent);
        finish(); // Finish current activity
    }
}
