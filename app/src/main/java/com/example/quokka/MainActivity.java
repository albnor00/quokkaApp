package com.example.quokka;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quokka.group.group_admin_page;
import com.example.quokka.group.group_main;
import com.example.quokka.group.group_member_page;
import com.example.quokka.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.template.templateMain;
import com.example.quokka.ui.login.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    CardView logout, group,tasks,profile;

    LinearLayout icon_logout,icon_group,icon_tasks,icon_profile;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        icon_group = findViewById(R.id.icon_group);
        icon_logout = findViewById(R.id.icon_logout);
        icon_tasks = findViewById(R.id.icon_tasks);
        icon_profile = findViewById(R.id.icon_profile);


        logout = findViewById(R.id.logout);
        tasks = findViewById(R.id.tasks);
        group = findViewById(R.id.group);
        profile = findViewById(R.id.profile);

        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        group.setOnClickListener(new View.OnClickListener() {
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

        tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), tasksMain.class);
                startActivity(intent);
                finish();
            }
        });


// Listener for icons so you can press both card and icon

        icon_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });


        icon_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        icon_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), tasksMain.class);
                startActivity(intent);
                finish();
            }
        });

        icon_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             checkUserRole();
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

