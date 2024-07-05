package com.example.quokka.goal_progress_tracking.goal_setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Question1 extends AppCompatActivity {
    private Spinner aspectSpinner;
    private String selectedAspect = ""; // Variable to store selected aspect
    private String[] aspects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_goal_question1);

        aspectSpinner = findViewById(R.id.aspectSpinner);

        // Define the items for the Spinner
        aspects = new String[]{"Economy", "Career", "Physical Wellbeing", "Close Relationships",
                "Relationships", "Personal Development", "Physical Environment", "Rest/Relaxation"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aspects);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        aspectSpinner.setAdapter(adapter);

        fetchAndDisplayPreviousAspect();

        Button nextQuestion = findViewById(R.id.next_question);
        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected item from the Spinner
                selectedAspect = aspectSpinner.getSelectedItem().toString();

                // Save selected aspect to Firestore
                saveAspectToFirestore(selectedAspect);

                moveToQuestion2();
            }
        });


        ImageView back_btn = findViewById(R.id.img_back);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToMainPage();
            }
        });
    }

    // Method to save selected aspect to Firestore
    private void saveAspectToFirestore(String selectedAspect) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            // Create a map to store the aspect data
            Map<String, Object> aspectData = new HashMap<>();
            aspectData.put("aspectName", selectedAspect); // Store the selected aspect

            // Add the aspect data to the "Goals" collection for the user
            db.collection("users").document(userId).collection("Goal")
                    .document("Q1_user_aspect")
                    .set(aspectData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Question1.this, "Aspect saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Question1.this, "Failed to save aspect: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void fetchAndDisplayPreviousAspect() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Retrieve previously selected aspect from Firestore
            db.collection("users").document(userId).collection("Goal")
                    .document("Q1_user_aspect")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String previousAspect = documentSnapshot.getString("aspectName");
                            if (previousAspect != null) {
                                int position = Arrays.asList(aspects).indexOf(previousAspect);
                                if (position != -1) {
                                    aspectSpinner.setSelection(position);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Question1.this, "Failed to fetch previous aspect: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void moveToQuestion2() {
        Intent intent = new Intent(getApplicationContext(), Question2.class);
        startActivity(intent);
        finish(); // Finish current activity
    }

    private void moveToMainPage() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish(); // Finish current activity
    }

}