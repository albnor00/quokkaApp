package com.example.quokka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quokka.goalTask.goalTask_mainpage;
import com.example.quokka.group.group_admin_page;
import com.example.quokka.group.group_main;
import com.example.quokka.group.group_member_page;
import com.example.quokka.ui.login.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    LinearLayout logout, groupIcon, taskIcon;
    TextView textView;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        logout = findViewById(R.id.logout);
        textView = findViewById(R.id.user_Details);
        groupIcon = findViewById(R.id.icon_group);
        taskIcon = findViewById(R.id.tasks);
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String username = documentSnapshot.getString("username");
                                textView.setText("User: " + username);
                            } else {
                                textView.setText("Unknown");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            textView.setText("Unknown");
                        }
                    });
        }

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
                Intent intent = new Intent(getApplicationContext(), goalTask_mainpage.class);
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
}

