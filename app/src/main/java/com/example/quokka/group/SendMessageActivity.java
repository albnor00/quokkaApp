package com.example.quokka.group;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private Button sendButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        userSpinner = findViewById(R.id.userSpinner);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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
    }

    private void loadUsersIntoSpinner() {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<String> userNames = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String username = document.getString("username");
                    if (username != null) {
                        userNames.add(username);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSpinner.setAdapter(adapter);
            }
        });
    }

    private void sendMessageToUser(String username, String message) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Handle the case where user is null (not authenticated properly)
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String senderUsername = documentSnapshot.getString("username");
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("senderUsername", senderUsername);
                    messageData.put("message", message);
                    messageData.put("timestamp", System.currentTimeMillis());

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






