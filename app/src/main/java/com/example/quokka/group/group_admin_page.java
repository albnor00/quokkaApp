package com.example.quokka.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class group_admin_page extends AppCompatActivity {
    ImageView back;
    Button deletebutton, viewMemebers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_admin);

        // Initialize back ImageView
        viewMemebers = findViewById(R.id.btn_view_members);
        back = findViewById(R.id.admin_back);
        deletebutton = findViewById(R.id.btn_delete_group);


        viewMemebers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), group_viewMembers.class);
                startActivity(intent);
                finish();

            }
        });

        deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGroup();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set OnClickListener for back ImageView
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button click
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void deleteGroup() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String adminId = auth.getCurrentUser().getUid();

        // Query the groups collection to find the group document created by the admin
        db.collection("groups")
                .whereEqualTo("creator_id", adminId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document (assuming there's only one group per admin)
                        String groupId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Delete the group document from the Firestore collection
                        db.collection("groups").document(groupId)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Show success message
                                        Toast.makeText(group_admin_page.this, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                                        // Update all user documents to set role and groupID to null
                                        updateUsers(groupId);
                                        // Navigate to group creation page
                                        startActivity(new Intent(getApplicationContext(), group_main.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Show failure message
                                        Toast.makeText(group_admin_page.this, "Failed to delete group", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // If no group found for the admin
                        Toast.makeText(group_admin_page.this, "No group found for the admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show failure message
                        Toast.makeText(group_admin_page.this, "Failed to fetch group details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUsers(String groupId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the users collection to find users with the specified group ID
        db.collection("users")
                .whereEqualTo("groupID", groupId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        String userId = queryDocumentSnapshots.getDocuments().get(i).getId();
                        db.collection("users").document(userId)
                                .update("role", null, "groupID", null)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Show success message if needed

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Show failure message if needed
                                        Toast.makeText(group_admin_page.this, "Failed to update user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(group_admin_page.this, "Failed to fetch users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
