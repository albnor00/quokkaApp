package com.example.quokka.goal_progress_tracking.goal_page_v2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_setup.Question1;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Goal_non_empty_page extends AppCompatActivity {
    private TextView currentDateTextView;
    private ImageButton previousDayButton;
    private ImageButton nextDayButton;
    private ImageView backButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private int currentDayOfMonth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_empty_goal_page);
        View includedLayout = findViewById(R.id.task_progress_overview);
        TextView goalTitleTextView = includedLayout.findViewById(R.id.textView);

        // Initialize views
        currentDateTextView = findViewById(R.id.text_current_date);
        previousDayButton = findViewById(R.id.btn_previous_day);
        nextDayButton = findViewById(R.id.btn_next_day);
        backButton = findViewById(R.id.img_back);

        // Initialize calendar
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());

        // Display User Goal Information
        fetchAndDisplayUserData();

        // Set initial date text
        updateDateText();

        // Set click listeners for arrow buttons
        previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                updateDateText();
            }
        });

        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                updateDateText();
            }
        });

        // Set click listener for current date text view
        currentDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Method to update the displayed date
    private void updateDateText() {
        currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String dateText = dateFormat.format(calendar.getTime());
        currentDateTextView.setText(dateText);
    }

    // Method to show date picker dialog
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateText();
                    }
                },
                calendar.get(Calendar.YEAR), // Initial year
                calendar.get(Calendar.MONTH), // Initial month
                calendar.get(Calendar.DAY_OF_MONTH) // Initial day
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the calendar to current date when activity resumes (e.g., coming back from another activity)
        calendar = Calendar.getInstance();
        updateDateText();
    }

    // Method to fetch and display user's goal data
    private void fetchAndDisplayUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            Query query = db.collection("users").document(userId).collection("Goal");

            // Perform the query
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract data from each document in the subcollection
                            String goalTitle = document.getString("Q1_user_aspect");

                            // Update TextViews with retrieved data
                            updateTextViews(goalTitle);
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(Goal_non_empty_page.this, "Failed to fetch goal data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // User is not authenticated or session expired
            Toast.makeText(Goal_non_empty_page.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to update TextViews with fetched data
    private void updateTextViews(String goalTitle) {
        // Find the included layout (CardView)
        View includedLayout = findViewById(R.id.task_progress_overview);

        // Find the TextView inside the included layout
        TextView goalTitleTextView = includedLayout.findViewById(R.id.textView);

        // Update the TextView with the retrieved goal title
        goalTitleTextView.setText(goalTitle);
    }
}
