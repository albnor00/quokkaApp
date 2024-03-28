package com.example.quokka.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.ui.login.Register;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class group_main extends AppCompatActivity {

    ImageView back_btn;
    Button create_group;
    Button join_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_main); // Set the layout XML file
        // Initialize any views or components here
        back_btn = findViewById(R.id.img_back);

        create_group = findViewById(R.id.btn_create_group);

        join_group = findViewById(R.id.btn_join_group);

        // Check if the user is already an admin of a group
        checkIfUserIsAdmin();
        checkIfUserIsMember();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), group_create.class);
                startActivity(intent);
                finish();
            }
        });

        join_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), group_join.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void checkIfUserIsAdmin() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        checkIfUserIsAdminOfGroup(userId)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isAdmin) {
                        // If user is an admin, navigate to another page
                        if (isAdmin) {
                            Intent intent = new Intent(getApplicationContext(), group_admin_page.class); // Replace AdminActivity with the actual activity
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(group_main.this, "Failed to check if user is admin", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public Task<Boolean> checkIfUserIsAdminOfGroup(String userID){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new TaskCompletionSource to manually complete the task
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        db.collection("groups")
                .whereEqualTo("creator_id", userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean isAdmin = !task.getResult().getDocuments().isEmpty();
                            taskCompletionSource.setResult(isAdmin);
                        } else {
                            taskCompletionSource.setException(Objects.requireNonNull(task.getException()));
                        }
                    }
                });

        // Return the task from the TaskCompletionSource
        return taskCompletionSource.getTask();
    }

    public Task<Boolean> checkIfUserIsMemberOfGroup(String userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new TaskCompletionSource to manually complete the task
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        db.collection("users")
                .document(userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Check if the user has a valid group ID associated with them
                                String groupID = document.getString("groupID");
                                String role = document.getString("role");
                                boolean isMember = groupID != null && Objects.equals(role, "Coachee/(Member)");
                                taskCompletionSource.setResult(isMember);
                            } else {
                                // User document does not exist, which may indicate an error
                                taskCompletionSource.setException(new Exception("User document does not exist"));
                            }
                        } else {
                            // Error occurred while fetching user document
                            taskCompletionSource.setException(task.getException());
                        }
                    }
                });

        // Return the task from the TaskCompletionSource
        return taskCompletionSource.getTask();
    }

    private void checkIfUserIsMember() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        checkIfUserIsMemberOfGroup(userId)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isMember) {
                        // If user is a member, navigate to another page
                        if (isMember) {
                            Intent intent = new Intent(getApplicationContext(), group_member_page.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(group_main.this, "Failed to check if user is a member", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}