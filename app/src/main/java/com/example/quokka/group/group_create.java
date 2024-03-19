package com.example.quokka.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.WriteBatch;

import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;


public class group_create extends AppCompatActivity {
    EditText editText_groupName, editText_dec;
    Button button;

    ImageView back;
    String groupName, groupDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        editText_groupName = findViewById(R.id.edit_text_group_name);
        button = findViewById(R.id.btn_create_group);
        back = findViewById(R.id.image_back);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from EditText fields
                groupName = editText_groupName.getText().toString();


                // Check if group name and description are not empty
                if (!groupName.isEmpty()) {
                    // Create the group in Firestore
                    createGroupInFirestore(groupName, groupDescription);
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
    }




    private void createGroupInFirestore(String groupName, String groupDescription) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String userId = auth.getCurrentUser().getUid(); // Get the current user's ID

        // Create a new group document with a unique ID
        DocumentReference groupRef = db.collection("groups").document();
        String groupId = groupRef.getId();

        // Add the group details to the group document
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("group_name", groupName);
        groupData.put("group_description", groupDescription);
        groupData.put("creator_id", userId);

        // Set the group document with the group data
        groupRef.set(groupData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Add the group to the creator's list of groups with role "Coach"
                        Map<String, Object> userGroupData = new HashMap<>();
                        userGroupData.put("role", "Coach");

                        // Set the user's role in the group under the "members" subcollection
                        groupRef.collection("members").document(userId)
                                .set(userGroupData)
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
                                        Toast.makeText(group_create.this, "Failed to add group creator as a member", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show a failure message
                        Toast.makeText(group_create.this, "Failed to create group", Toast.LENGTH_SHORT).show();
                    }
                });
    }





}


