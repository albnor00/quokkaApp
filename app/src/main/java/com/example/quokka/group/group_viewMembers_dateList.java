package com.example.quokka.group;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.profile.ProfileActivity;
import com.example.quokka.tasks.balance_wheel;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class group_viewMembers_dateList extends AppCompatActivity {

    private LinearLayout answerListDateContainer;

    private ImageView back;



    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_member_list_date);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        answerListDateContainer = findViewById(R.id.answerListDateContainer);
        back = findViewById(R.id.back);
        TextView username = findViewById(R.id.textViewMemberName);

        String name = getIntent().getStringExtra("username");

        username.setText(name);



        // Load timestamps from Firestore
        loadTimestamps();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve user ID and submission ID from the intent that started this activity
                String userId = getIntent().getStringExtra("userId");
                String submissionId = getIntent().getStringExtra("submissionId");

                // Start group_viewMembers_dateList activity and pass user ID and submission ID as extras
                Intent intent = new Intent(getApplicationContext(), group_viewMembers.class);
                intent.putExtra("userId", userId);
                intent.putExtra("submissionId", submissionId);
                startActivity(intent);
                finish();
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
                    MainActivity.checkUserRole(user, group_viewMembers_dateList.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });


    }

    private void loadTimestamps() {
        String name = getIntent().getStringExtra("username");
        String userId = getIntent().getStringExtra("userId");

        // Query Firestore to get submission timestamps
        db.collection("userresponses")
                .document(userId)
                .collection("submissions")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Get timestamp
                            Date timestamp = documentSnapshot.getDate("timestamp");
                            if (timestamp != null) {
                                String submissionId = documentSnapshot.getId();
                                // Create a new AppCompatButton
                                AppCompatButton button = new AppCompatButton(group_viewMembers_dateList.this);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                layoutParams.setMargins(0, convertDpToPx(16), 0, 0); // Set margin programmatically
                                button.setLayoutParams(layoutParams);

                                //Background color for the button
                                button.setBackgroundResource(R.drawable.btn_background_1);
                                // Format the timestamp and set it as the button text
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                button.setText("Balance wheel submitted on: " + sdf.format(timestamp));
                                button.setTypeface(Typeface.DEFAULT_BOLD);
                                button.setGravity(Gravity.CENTER);
                                // Set click listener for the button
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // When button is clicked, retrieve the submission document and display its fields
                                        Intent intent = new Intent(getApplicationContext(), group_viewMembers_answers.class);
                                        intent.putExtra("submissionId", submissionId); // Pass the submission ID
                                        intent.putExtra("userId", userId); // Pass the user ID
                                        intent.putExtra("username",name);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                // Add the button to the LinearLayout
                                answerListDateContainer.addView(button);
                            }
                        }
                    }
                });
    }







    private int convertDpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
