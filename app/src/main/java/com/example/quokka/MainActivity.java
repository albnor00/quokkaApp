package com.example.quokka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quokka.goal_progress_tracking.average_task_template.average_task_page;
import com.example.quokka.goal_progress_tracking.average_task_template.create_new_average_task;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_empty_page;
import com.example.quokka.goal_progress_tracking.goal_page_v2.Goal_non_empty_page;
import com.example.quokka.goal_progress_tracking.goal_setup.Question1;
import com.example.quokka.goal_progress_tracking.goal_setup.Question2;
import com.example.quokka.goal_progress_tracking.goal_setup.Question3;
import com.example.quokka.goal_progress_tracking.goal_setup.Question4;
import com.example.quokka.goal_progress_tracking.goal_setup.Question5;
import com.example.quokka.group.group_admin_page;
import com.example.quokka.group.group_main;
import com.example.quokka.group.group_member_page;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    LinearLayout logout, groupIcon, taskIcon, homeIcon;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        logout = findViewById(R.id.logout);
        groupIcon = findViewById(R.id.icon_group);
        taskIcon = findViewById(R.id.tasks);
        homeIcon = findViewById(R.id.layoutHome);
        user = auth.getCurrentUser();


        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check user role only when group icon is clicked
                checkUserRole();
            }
        });



        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });



        taskIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            checkUserGoalStatus();
            }
        });

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), average_task_page.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void checkUserRole() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        // Check if the user is an admin
        db.collection("groups")
                .whereEqualTo("creator_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Intent intent = new Intent(getApplicationContext(), group_admin_page.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Check if the user is a member
                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String role = documentSnapshot.getString("role");
                                    if (role != null && role.equals("Coachee/(Member)")) {
                                        Intent intent = new Intent(getApplicationContext(), group_member_page.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If the user is neither admin nor member
                                        Intent intent = new Intent(getApplicationContext(), group_main.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    Toast.makeText(MainActivity.this, "Failed to check user role.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(MainActivity.this, "Failed to check user role.", Toast.LENGTH_SHORT).show();
                });
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

    private void handleMultipleMissingDocuments(List<Integer> missingQuestions) {
        // Handle scenario where multiple documents are missing
        // For example, provide a consolidated message to the user
        Log.d("GoalStatus", "User is missing multiple required documents: " + missingQuestions);
        // You can decide on an appropriate action here
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

