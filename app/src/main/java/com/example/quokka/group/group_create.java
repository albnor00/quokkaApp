package com.example.quokka.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import androidx.appcompat.widget.AppCompatButton;

import java.util.HashMap;
import java.util.Map;


public class group_create extends AppCompatActivity {
    EditText editText_groupName, editText_dec;
    AppCompatButton button;
    ImageView back;
    String groupName, groupDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        editText_groupName = findViewById(R.id.edit_text_group_name);
        button = findViewById(R.id.create_group);
        back = findViewById(R.id.image_back);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from EditText fields
                groupName = editText_groupName.getText().toString();


                // Check if group name and description are not empty
                if (!groupName.isEmpty()) {
                    // Create the group in Firestore
                    createGroupInFirestore(groupName);
                    Intent intent = new Intent(getApplicationContext(), group_admin_page.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Show a message if group name or description is empty
                    Toast.makeText(group_create.this, "Please enter group name and description", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), group_main.class);
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
                    MainActivity.checkUserRole2(user, group_create.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
    }


    private void createGroupInFirestore(String groupName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        final String userId = auth.getCurrentUser().getUid(); // Get the current user's ID

        // Create a new group document with a unique ID
        final DocumentReference groupRef = db.collection("groups").document();

        // Add the group details to the group document
        final Map<String, Object> groupData = new HashMap<>();
        groupData.put("group_name", groupName);
        groupData.put("creator_id", userId);

        // Set the group document with the group data
        groupRef.set(groupData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Show a success message
                        Toast.makeText(group_create.this, "Group created successfully", Toast.LENGTH_SHORT).show();

                        // Finish the activity
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show a failure message
                        Toast.makeText(group_create.this, "Failed to create group", Toast.LENGTH_SHORT).show();
                    }
                });

        // Update the user's document with the group ID
        final String groupId = groupRef.getId();
        final Map<String, Object> userData = new HashMap<>();
        userData.put("groupID", groupId);
        userData.put("role", "Coach/(Admin)");

        db.collection("users")
                .document(userId)
                .update(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Show a success message
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show a failure message
                        Toast.makeText(group_create.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


