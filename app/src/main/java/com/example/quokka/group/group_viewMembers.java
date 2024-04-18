package com.example.quokka.group;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class group_viewMembers extends AppCompatActivity {

    LinearLayout memberListContainer;
    ImageView back;
    String groupID; // Define groupID as a class-level variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_members);
        memberListContainer = findViewById(R.id.memberListContainer);
        back = findViewById(R.id.membersList_back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), group_admin_page.class);
                startActivity(intent);
                finish();
            }
        });

        // Get the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Query Firestore to get the groupID of the current user
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                groupID = documentSnapshot.getString("groupID"); // Assign groupID here
                                if (groupID != null && !groupID.isEmpty()) {
                                    fetchMembersByGroupID(groupID);
                                } else {
                                    // Handle case where groupID is not available for the user
                                }
                            } else {
                                // Handle case where user document doesn't exist
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failures
                        }
                    });
        } else {
            // Handle case where no user is logged in
        }
    }

    private void fetchMembersByGroupID(String groupID) {
        // Query Firestore to get users with the same groupID
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("groupID", groupID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> members = queryDocumentSnapshots.getDocuments();
                        displayMembers(members);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failures
                    }
                });
    }

    private void displayMembers(List<DocumentSnapshot> users) {
        for (DocumentSnapshot user : users) {
            String role = user.getString("role"); // role is stored as a field in Firestore
            String name = user.getString("username"); //  name is stored as a field in Firestore
            String userID = user.getId();

            // Create a TextView to display member info
            if (!role.equals("Coach/(Admin)")) {
                TextView textView = new TextView(this);
                textView.setText(name);
                textView.setTextSize(30);
                textView.setClickable(true);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.BLACK); // Change text color
                textView.setTypeface(Typeface.DEFAULT_BOLD); // Make text bold
                textView.setPadding(16, 16, 16, 16);

                // Add click listener to the TextView to view member's answers
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Call method to handle click event and pass the user ID and group ID
                        if (groupID != null && !groupID.isEmpty()) {
                            handleMemberClick(userID, groupID);
                            ObjectAnimator colorAnimator = ObjectAnimator.ofArgb(textView, "textColor", Color.BLACK, Color.LTGRAY);
                            colorAnimator.setDuration(1000); // Duration of the animation in milliseconds
                            colorAnimator.start();
                        } else {
                            // Handle case where groupID is not available
                            Toast.makeText(group_viewMembers.this, "Group ID is not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Add the TextView to the LinearLayout
                memberListContainer.addView(textView);
            }
        }
    }

    private void handleMemberClick(String userID, String groupID) {
        // Navigate to a new activity to display member's answers
        Intent intent = new Intent(group_viewMembers.this, group_viewMembers_answers.class);
        intent.putExtra("userID", userID); // Pass the member's user ID
        intent.putExtra("groupID", groupID); // Pass the group ID
        startActivity(intent);
    }
}
