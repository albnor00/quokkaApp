package com.example.quokka.goal_progress_tracking.goal_page_v2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.habit_task_template.habit_task_log_history_page;
import com.example.quokka.goal_progress_tracking.habit_task_template.habit_task_settings_page;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Goal_overview extends AppCompatActivity {
    private static final String TAG = "Goal_overview";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView answer1TextView;
    private TextView answer2TextView;
    private TextView answer3TextView;
    private TextView answer4TextView;
    private TextView answer5TextView;
    private ImageView settings;
    private ImageView back_btn;
    private Button rateSatisfactionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_overview_page);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        answer1TextView = findViewById(R.id.answer1_textview);
        answer2TextView = findViewById(R.id.answer2_textview);
        answer3TextView = findViewById(R.id.answer3_textview);
        answer4TextView = findViewById(R.id.answer4_textview);
        answer5TextView = findViewById(R.id.answer5_textview);
        settings = findViewById(R.id.settings);
        back_btn = findViewById(R.id.img_back);
        rateSatisfactionButton = findViewById(R.id.rate_satisfaction_button);

        fetchGoalData();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Goal_non_empty_page.class);
                startActivity(intent);
                finish();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Goal_overview_settings.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.task_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        popupMenu.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_history) {
            // Create an intent to start the add_new_average_log activity
            Intent intent = new Intent(getApplicationContext(), habit_task_log_history_page.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            // Create an intent to start the add_new_average_log activity
            Intent intent = new Intent(getApplicationContext(), habit_task_settings_page.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void fetchGoalData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User not logged in");
            return;
        }
        String userId = user.getUid();

        // Define an array of document IDs (questions)
        String[] documentIds = {"Q1_user_aspect", "Q2_user_current_rating", "Q3_user_explanation", "Q4_user_goal_rating", "Q5_user_explanation"};

        // Define a map of field names and their types for each document ID
        Map<String, FieldData> fieldDataMap = new HashMap<>();
        fieldDataMap.put("Q1_user_aspect", new FieldData("aspectName", FieldType.STRING));
        fieldDataMap.put("Q2_user_current_rating", new FieldData("selectedRating", FieldType.INTEGER));
        fieldDataMap.put("Q3_user_explanation", new FieldData("user_explanation", FieldType.STRING));
        fieldDataMap.put("Q4_user_goal_rating", new FieldData("selectedRating", FieldType.INTEGER));
        fieldDataMap.put("Q5_user_explanation", new FieldData("user_explanation", FieldType.STRING));
        // Add more entries for other document IDs and their respective field names and types

        // Iterate through each document ID (question)
        for (String documentId : documentIds) {
            // Fetch data for the current document ID
            db.collection("users").document(userId).collection("Goal").document(documentId)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Determine the field data for the current document ID
                                    FieldData fieldData = fieldDataMap.get(documentId);
                                    if (fieldData != null) {
                                        // Fetch the answer field based on its type
                                        Object answer;
                                        switch (fieldData.getType()) {
                                            case STRING:
                                                answer = document.getString(fieldData.getFieldName());
                                                break;
                                            case INTEGER:
                                                answer = document.getLong(fieldData.getFieldName()); // Use getLong() for integers
                                                break;
                                            default:
                                                answer = null;
                                                break;
                                        }

                                        // Determine which TextView to set based on the document ID
                                        TextView answerTextView;
                                        switch (documentId) {
                                            case "Q1_user_aspect":
                                                answerTextView = answer1TextView;
                                                break;
                                            case "Q2_user_current_rating":
                                                answerTextView = answer2TextView;
                                                break;
                                            case "Q3_user_explanation":
                                                answerTextView = answer3TextView;
                                                break;
                                            case "Q4_user_goal_rating":
                                                answerTextView = answer4TextView;
                                                break;
                                            case "Q5_user_explanation":
                                                answerTextView = answer5TextView;
                                                break;

                                            default:
                                                // Handle unexpected document ID if needed
                                                return;
                                        }

                                        // Set answer to the corresponding TextView
                                        if (answer != null) {
                                            answerTextView.setText(String.valueOf(answer));
                                        } else {
                                            // Handle null or empty answers if needed
                                        }
                                    } else {
                                        Log.d(TAG, "No field data mapped for document ID: " + documentId);
                                    }
                                } else {
                                    Log.d(TAG, "No such document for " + documentId);
                                }
                            } else {
                                Log.d(TAG, "get failed for " + documentId + " with ", task.getException());
                            }
                        }
                    });
        }
    }

    enum FieldType {
        STRING,
        INTEGER,
        // Add more types as needed (Boolean, etc.)
    }

    // Class to hold field name and type data
    class FieldData {
        private String fieldName;
        private FieldType type;

        public FieldData(String fieldName, FieldType type) {
            this.fieldName = fieldName;
            this.type = type;
        }

        public String getFieldName() {
            return fieldName;
        }

        public FieldType getType() {
            return type;
        }
    }

}
