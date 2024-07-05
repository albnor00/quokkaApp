package com.example.quokka.group;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.util.Objects;



public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout notificationContainer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView noMessagesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationContainer = findViewById(R.id.notificationContainer);
        noMessagesTextView = findViewById(R.id.noMessagesTextView);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadNotifications();

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
                    MainActivity.checkUserRole(user, NotificationsActivity.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
    }

    private void loadNotifications() {
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        db.collection("users").document(userId).collection("notifications")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            noMessagesTextView.setVisibility(View.VISIBLE);
                            notificationContainer.setVisibility(View.GONE);
                        } else {
                            noMessagesTextView.setVisibility(View.GONE);
                            notificationContainer.setVisibility(View.VISIBLE);

                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                String message = documentSnapshot.getString("message");
                                String senderUsername = documentSnapshot.getString("senderUsername");
                                Object timestampObj = documentSnapshot.get("timestamp");

                                if (timestampObj instanceof com.google.firebase.Timestamp) {
                                    // Handle Firestore Timestamp
                                    com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) timestampObj;
                                    Date date = timestamp.toDate();
                                    displayNotification(message, date, senderUsername);
                                } else if (timestampObj instanceof Long) {
                                    // Handle Long timestamp (milliseconds)
                                    Long timestampLong = (Long) timestampObj;
                                    Date date = new Date(timestampLong);
                                    displayNotification(message, date, senderUsername);
                                } else {
                                    // Handle unknown timestamp format
                                    Log.w(TAG, "Invalid timestamp format for document: " + documentSnapshot.getId());
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading notifications", e);
                    }
                });
    }

    private void displayNotification(String message, Date date, String senderUsername) {
        TextView textView = new TextView(NotificationsActivity.this);
        textView.setText(String.format("Message: %s\nReceived from %s on: %s",
                message,
                senderUsername,
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)));
        textView.setPadding(16, 16, 16, 16);
        textView.setBackgroundResource(R.drawable.btn_background_2);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16); // Add bottom margin of 16dp
        textView.setLayoutParams(layoutParams);

        notificationContainer.addView(textView);
    }
}



