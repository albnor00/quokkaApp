package com.example.quokka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
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
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
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
    TextView welcome;
    CardView logout, tasks, group, account;
    LinearLayout Icon_group, Icon_profile, Icon_tasks, Icon_logout;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        welcome = findViewById(R.id.welcome_text);

        logout = findViewById(R.id.logout);
        tasks = findViewById(R.id.tasks);
        group = findViewById(R.id.group);
        account = findViewById(R.id.profile);

        Icon_group = findViewById(R.id.icon_group);
        Icon_profile = findViewById(R.id.icon_profile);
        Icon_tasks = findViewById(R.id.icon_tasks);
        Icon_logout = findViewById(R.id.icon_logout);

        // Fetch username and set to welcome TextView
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null) {
                                welcome.setText("Welcome, " + username + "!");
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to fetch username.", Toast.LENGTH_SHORT).show());
        }

        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check user role only when group icon is clicked
                checkUserRole();
            }
        });

        Icon_group.setOnClickListener(new View.OnClickListener() {
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

        Icon_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), tasksMain.class);
                startActivity(intent);
                finish();
            }
        });

        Icon_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), tasksMain.class);
                startActivity(intent);
                finish();
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Icon_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
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

    public static void checkUserRole2(FirebaseUser user, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        // Check if the user is an admin
        db.collection("groups")
                .whereEqualTo("creator_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Intent intent = new Intent(context, group_admin_page.class);
                        context.startActivity(intent);
                    } else {
                        // Check if the user is a member
                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String role = documentSnapshot.getString("role");
                                    if (role != null && role.equals("Coachee/(Member)")) {
                                        Intent intent = new Intent(context, group_member_page.class);
                                        context.startActivity(intent);
                                    } else {
                                        // If the user is neither admin nor member
                                        Intent intent = new Intent(context, group_main.class);
                                        context.startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    Toast.makeText(context, "Failed to check user role.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(context, "Failed to check user role.", Toast.LENGTH_SHORT).show();
                });
    }


}

