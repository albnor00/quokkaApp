package com.example.quokka.group;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.tasks.profile.ProfileActivity;
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

import java.util.List;

public class group_viewMembers extends AppCompatActivity {

    LinearLayout memberListContainer;
    ImageView back;
    String groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_members);
        memberListContainer = findViewById(R.id.memberListContainer);
        memberListContainer.setGravity(Gravity.CENTER_HORIZONTAL); // Set gravity to center horizontal
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
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

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
                    MainActivity.checkUserRole2(user, group_viewMembers.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
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
            String role = user.getString("role");
            String name = user.getString("username");
            String userID = user.getId();

            if (!role.equals("Coach/(Admin)")) {
                // Create a new AppCompatButton for each member
                AppCompatButton button = new AppCompatButton(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(0, convertDpToPx(30), 0, 0); // Set margin programmatically
                button.setLayoutParams(layoutParams);

                button.setGravity(Gravity.CENTER);

                // Set background color for the button
                button.setBackgroundResource(R.drawable.btn_background_2);
                // Set text and style for the button
                button.setText(name);
                button.setTextSize(15);
                button.setTextColor(Color.BLACK);
                button.setTypeface(Typeface.DEFAULT_BOLD);
                button.setGravity(Gravity.CENTER);
                button.setPadding(16, 16, 16, 16);

                // Add click listener to the button
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle button click event
                        if (groupID != null && !groupID.isEmpty()) {
                            handleMemberClick(userID, groupID, name);
                        } else {
                            Toast.makeText(group_viewMembers.this, "Group ID is not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Add the button to the LinearLayout
                memberListContainer.addView(button);
            }
        }
    }


    private void handleMemberClick(String userID, String groupID, String username) {
        // Navigate to a new activity to display member's answers
        Intent intent = new Intent(group_viewMembers.this, group_viewMembers_dateList.class);
        intent.putExtra("userId", userID); // Pass the member's user ID
        intent.putExtra("groupId", groupID); // Pass the group ID
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private int convertDpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
