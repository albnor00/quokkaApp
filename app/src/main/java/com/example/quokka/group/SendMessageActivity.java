package com.example.quokka.group;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.balance_wheel;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendMessageActivity extends AppCompatActivity {

    private Spinner userSpinner;
    private EditText messageEditText;
    private AppCompatButton sendButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        userSpinner = findViewById(R.id.userSpinner);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Load users into the spinner
        loadUsersIntoSpinner();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedUser = userSpinner.getSelectedItem().toString();
                String message = messageEditText.getText().toString().trim();

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(SendMessageActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessageToUser(selectedUser, message);
                }
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
                    MainActivity.checkUserRole2(user, SendMessageActivity.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
    }

    private void loadUsersIntoSpinner() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Handle case where user is not authenticated
            return;
        }

        // Get the current user's groupID
        FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userGroupId = documentSnapshot.getString("groupID");
                        if (userGroupId != null) {
                            // Query users where groupID matches the current user's groupID
                            db.collection("users")
                                    .whereEqualTo("groupID", userGroupId)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        List<String> userNames = new ArrayList<>();
                                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                                            String userId = document.getId(); // Get user ID
                                            String username = document.getString("username");
                                            if (username != null && !userId.equals(currentUser.getUid())) {
                                                userNames.add(username);
                                            }
                                        }
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNames);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        userSpinner.setAdapter(adapter);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure to fetch users based on groupID
                                        Log.e(TAG, "Failed to load users based on groupID", e);
                                    });
                        }
                    } else {
                        Log.e(TAG, "Current user document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to fetch current user's document
                    Log.e(TAG, "Failed to fetch current user document", e);
                });
    }



    private void sendMessageToUser(String username, String message) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Boolean read = false;
                    String senderUsername = documentSnapshot.getString("username");
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("senderUsername", senderUsername);
                    messageData.put("message", message);
                    messageData.put("timestamp", System.currentTimeMillis());
                    messageData.put("read", read);

                    db.collection("users").whereEqualTo("username", username).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot recipientDocument = queryDocumentSnapshots.getDocuments().get(0);
                            String recipientUserId = recipientDocument.getId();

                            db.collection("users").document(recipientUserId).collection("notifications").add(messageData)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(SendMessageActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                            messageEditText.setText("");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SendMessageActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(SendMessageActivity.this, "Recipient not found", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SendMessageActivity.this, "Failed to find recipient", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(SendMessageActivity.this, "Sender information not found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SendMessageActivity.this, "Failed to retrieve sender information", Toast.LENGTH_SHORT).show();
            }
        });
    }


}






