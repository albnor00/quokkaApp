package com.example.quokka.goal_progress_tracking.goal_setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.tasks.balance_wheel;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Question2 extends AppCompatActivity {
    private SeekBar ratingSeekBar;
    private TextView currentRatingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_goal_question2);

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
                updateCurrentRating(progress + 1); // Add 1 to convert 0-9 to 1-10 range
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
                // Get the selected rating from the SeekBar
                int selectedRating = ratingSeekBar.getProgress() + 1; // Add 1 to convert 0-9 to 1-10 range

                // Save selected rating to Firestore
                saveRatingToFirestore(selectedRating);

                // Start Question3 activity
                moveToQuestion3();
            }
        });

        ImageView back_btn = findViewById(R.id.img_back);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToQuestion1();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home_bottom) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else if (item.getItemId() == R.id.profile_bottom) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else if (item.getItemId() == R.id.tasks_bottom) {
                    startActivity(new Intent(getApplicationContext(), tasksMain.class));
                } else if (item.getItemId() == R.id.group_bottom) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    MainActivity.checkUserRole2(user, Question2.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
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
                    .document("Q2_user_current_rating")
                    .set(ratingData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Question2.this, "Rating saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Question2.this, "Failed to save rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    .document("Q2_user_current_rating")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            int previousRating = documentSnapshot.getLong("selectedRating").intValue();
                            ratingSeekBar.setProgress(previousRating - 1); // Set progress (0-9) based on previous rating (1-10)
                            updateCurrentRating(previousRating); // Update TextView with previous rating
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Question2.this, "Failed to fetch previous rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void moveToQuestion1() {
        Intent intent = new Intent(getApplicationContext(), Question1.class);
        startActivity(intent);
        finish(); // Finish current activity
    }

    private void moveToQuestion3() {
        Intent intent = new Intent(getApplicationContext(), Question3.class);
        startActivity(intent);
        finish(); // Finish current activity
    }
}