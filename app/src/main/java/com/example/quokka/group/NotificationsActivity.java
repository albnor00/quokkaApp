package com.example.quokka.group;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationContainer = findViewById(R.id.notificationContainer);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadNotifications();
    }

    private void loadNotifications() {
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        db.collection("users").document(userId).collection("notifications")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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

                                displayNotification(message, date,senderUsername);

                            } else {
                                // Handle unknown timestamp format
                                Log.w(TAG, "Invalid timestamp format for document: " + documentSnapshot.getId());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading notifications", e);
                        // Handle any errors
                    }
                });
    }

    private void displayNotification(String message, Date date, String senderUsername) {
        TextView textView = new TextView(NotificationsActivity.this);
        textView.setText(String.format("Message: %s\nReceived from "+ senderUsername+ " on: %s",
                message,
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


