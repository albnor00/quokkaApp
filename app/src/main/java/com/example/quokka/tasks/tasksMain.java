package com.example.quokka.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_empty_page;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.example.quokka.goal_progress_tracking.goal_setup.Question1;
import com.example.quokka.goal_progress_tracking.goal_setup.Question2;
import com.example.quokka.goal_progress_tracking.goal_setup.Question3;
import com.example.quokka.goal_progress_tracking.goal_setup.Question4;
import com.example.quokka.goal_progress_tracking.goal_setup.Question5;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.ui.login.Login;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class tasksMain extends AppCompatActivity {
    AppCompatButton balanceWheel, goalTracking, back;
    private String user_goal_state;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_main);

        back = findViewById(R.id.back);
        balanceWheel = findViewById(R.id.balanceWheelTask);
        goalTracking = findViewById(R.id.goalTracking);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        fetchUserGoalState();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        balanceWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), balance_wheel.class);
                startActivity(intent);
                finish();
            }
        });

        goalTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserGoalStatus();
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
                    MainActivity.checkUserRole2(user, tasksMain.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });

    }

    private void fetchUserGoalState() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Retrieve previously selected aspect from Firestore
            db.collection("users").document(userId).collection("Goal")
                    .document("User_Goal_State")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userGoalState = documentSnapshot.getString("user_goal_setup_state");
                            if (userGoalState != null) {
                                user_goal_state = userGoalState;
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(tasksMain.this, "Failed to fetch previous aspect: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void checkUserGoalStatus() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid(); // Assuming 'user' is your FirebaseUser object

        // Reference to the user's Goal subcollection
        CollectionReference goalCollectionRef = db.collection("users").document(userId)
                .collection("Goal");

        // Check if the 'Goal' subcollection exists
        goalCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                // The 'Goal' subcollection exists, check for required documents
                Set<String> existingDocuments = new HashSet<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    existingDocuments.add(document.getId());
                }

                // Check which documents are missing
                List<Integer> missingQuestions = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : getRequiredDocuments().entrySet()) {
                    if (!existingDocuments.contains(entry.getKey())) {
                        if (entry.getKey().equals("User_Goal_State")) {
                            // Handle missing User_Goal_State document
                            redirectToEmptyGoalPage();
                            return;
                        } else {
                            // Add missing question number to the list
                            missingQuestions.add(entry.getValue());
                        }
                    }
                }

                // Determine the next question to redirect based on missing documents
                if (!missingQuestions.isEmpty()) {
                    // Get the smallest missing question number (next question in sequence)
                    int nextQuestionNumber = Collections.min(missingQuestions);
                    redirectUserToQuestionClass(nextQuestionNumber);
                } else {
                    // All required documents exist, handle valid goal state
                    handleValidGoalState(goalCollectionRef);
                }

            } else {
                // The 'Goal' subcollection does not exist, redirect to empty goal page
                redirectToEmptyGoalPage();
            }
        });
    }

    private Map<String, Integer> getRequiredDocuments() {
        // Define the list of required documents and their corresponding question numbers
        Map<String, Integer> requiredDocuments = new HashMap<>();
        requiredDocuments.put("Q1_user_aspect", 1);
        requiredDocuments.put("Q2_user_current_rating", 2);
        requiredDocuments.put("Q3_user_explanation", 3);
        requiredDocuments.put("Q4_user_goal_rating", 4);
        requiredDocuments.put("Q5_user_explanation", 5);
        requiredDocuments.put("User_Goal_State", -1); // Special case for User_Goal_State
        return requiredDocuments;
    }

    private void handleValidGoalState(CollectionReference goalCollectionRef) {
        // Fetch the User_Goal_State document to check the goal setup state
        DocumentReference userGoalStateRef = goalCollectionRef.document("User_Goal_State");
        userGoalStateRef.get().addOnCompleteListener(stateTask -> {
            if (stateTask.isSuccessful()) {
                String userGoalState = stateTask.getResult().getString("user_goal_setup_state");
                if (userGoalState != null && userGoalState.equals("Completed")) {
                    // User has all documents and the goal state is valid
                    // Proceed with your logic here
                    Log.d("GoalStatus", "User has all required documents and valid goal state");
                    redirectToNonEmptyGoalPage();
                } else if (userGoalState != null && userGoalState.equals("Initiated")){
                    // Redirect user to update goal state
                    Log.d("GoalStatus", "User goal state is not valid");
                    redirectUserToQuestionClass(5);
                    // Redirect to appropriate screen for updating goal state
                }
            } else {
                // Error fetching User_Goal_State document
                Log.e("GoalStatus", "Error fetching User_Goal_State", stateTask.getException());
            }
        });
    }

    private void redirectUserToQuestionClass(int questionNumber) {
        // Redirect the user to the corresponding question class based on the missing question
        Intent intent;

        switch (questionNumber) {
            case 1:
                // Redirect to Question1Activity
                intent = new Intent(getApplicationContext(), Question1.class);
                startActivity(intent);
                finish();
                break;
            case 2:
                // Redirect to Question2Activity
                intent = new Intent(getApplicationContext(), Question2.class);
                startActivity(intent);
                finish();
                break;
            case 3:
                // Redirect to Question3Activity
                intent = new Intent(getApplicationContext(), Question3.class);
                startActivity(intent);
                finish();
                break;
            case 4:
                // Redirect to Question4Activity
                intent = new Intent(getApplicationContext(), Question4.class);
                startActivity(intent);
                finish();
                break;
            case 5:
                // Redirect to Question5Activity
                intent = new Intent(getApplicationContext(), Question5.class);
                startActivity(intent);
                finish();
                break;
            default:
                // Handle unexpected case
                break;
        }
    }

    private void redirectToNonEmptyGoalPage() {
        Intent intent = new Intent(getApplicationContext(), Goal_non_empty_page.class);
        startActivity(intent);
        finish();
    }

    private void redirectToEmptyGoalPage() {
        Intent intent = new Intent(getApplicationContext(), Goal_empty_page.class);
        startActivity(intent);
        finish();
    }

}