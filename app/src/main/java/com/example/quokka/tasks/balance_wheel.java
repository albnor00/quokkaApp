package com.example.quokka.tasks;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.example.quokka.group.group_member_page;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class balance_wheel extends AppCompatActivity {

    ImageView back;
    Dialog seekBarDialog;
    TextView btn_Economy;
    TextView btn_Career;
    TextView btn_Wellbeing;
    TextView btn_CloseRelations;
    TextView btn_Relations;
    TextView btn_Development;
    TextView btn_Environment;
    TextView btn_Rest;
    RadioButtons radioButtons;

    // HashMap to store button IDs and corresponding SeekBar values
    HashMap<Integer, Integer> buttonValuesMap = new HashMap<>();

    // SharedPreferences file name
    private static final String PREFS_NAME = "BalanceWheelPrefs";

    // Firestore instance
    FirebaseFirestore db;

    // Firebase Auth instance
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_wheel);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        radioButtons = findViewById(R.id.radio);

        // Initialize views
        back = findViewById(R.id.balanceWheel_back);
        btn_Economy = findViewById(R.id.balanceWheel_btn1);
        btn_Career = findViewById(R.id.balanceWheel_btn2);
        btn_Wellbeing = findViewById(R.id.balanceWheel_btn3);
        btn_CloseRelations = findViewById(R.id.balanceWheel_btn4);
        btn_Relations = findViewById(R.id.balanceWheel_btn5);
        btn_Development = findViewById(R.id.balanceWheel_btn6);
        btn_Environment = findViewById(R.id.balanceWheel_btn7);
        btn_Rest = findViewById(R.id.balanceWheel_btn8);
        back.bringToFront();



        // Restore values from SharedPreferences
        restoreValuesFromSharedPreferences();

        // Set onClickListener for back button

        radioButtons.setOnSliceClickListener(new RadioButtons.OnSliceClickListener() {
            @Override
            public void onSlickClick(int slicePosition) {
                // Handle click events for each slice position
                switch (slicePosition) {
                    case 0:
                        showSeekBarDialog(btn_Career.getId());
                        break;
                    case 1:
                        showSeekBarDialog(btn_Wellbeing.getId());
                        break;
                    case 2:
                        showSeekBarDialog(btn_CloseRelations.getId());
                        break;
                    case 3:
                        showSeekBarDialog(btn_Relations.getId());
                        break;
                    case 4:
                        showSeekBarDialog(btn_Development.getId());
                        break;
                    case 5:
                        showSeekBarDialog(btn_Environment.getId());
                        break;
                    case 6:
                        showSeekBarDialog(btn_Rest.getId());
                        break;
                    case 7:
                        showSeekBarDialog(btn_Economy.getId());
                        break;
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), group_member_page.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void showSeekBarDialog(final int buttonId) {
        // Initialize dialog
        seekBarDialog = new Dialog(balance_wheel.this);
        seekBarDialog.setContentView(R.layout.activity_balance_wheel_seekbar);
        seekBarDialog.setTitle("Select Value");

        // Set dialog dimensions
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(seekBarDialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        seekBarDialog.getWindow().setAttributes(layoutParams);
        EditText addComment = seekBarDialog.findViewById(R.id.addComment);

        // Find views inside the dialog
        SeekBar seekBar = seekBarDialog.findViewById(R.id.seekBar);
        final TextView valueTextView = seekBarDialog.findViewById(R.id.valueTextView);

        // Get the saved value from SharedPreferences
        int savedValue = getSavedValue(buttonId);

        // Set max value for SeekBar
        seekBar.setMax(9);

        // Set saved value if available, otherwise set default value
        seekBar.setProgress(savedValue != -1 ? savedValue : 0);
        valueTextView.setText(String.valueOf(seekBar.getProgress()));

        // Set progress change listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update TextView with current value
                valueTextView.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { //uncheck box if the seekbar is touched
                CheckBox noAnswerCheckBox = seekBarDialog.findViewById(R.id.noAnswerCheckBox);
                if (noAnswerCheckBox != null) {
                    noAnswerCheckBox.setChecked(false);
                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Store the button ID and its corresponding SeekBar value in the HashMap
                buttonValuesMap.put(buttonId, seekBar.getProgress());
                // Save the value to SharedPreferences
                saveValueToSharedPreferences(buttonId, seekBar.getProgress());
                // Upload button values to Firestore
                getGroupIdFromFirestore();

            }
        });
        seekBarDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Upload comment if not empty when the dialog is dismissed
                String comment = addComment.getText().toString();
                if (!comment.isEmpty()) {
                    uploadCommentToFirestore(buttonId, comment);
                }
            }
        });

        // Show the dialog
        seekBarDialog.show();

        // Find the checkbox inside the dialog
        CheckBox noAnswerCheckBox = seekBarDialog.findViewById(R.id.noAnswerCheckBox);

        // Set checkbox listener
        noAnswerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // If checkbox is checked, set value to 0 and store in SharedPreferences
                    seekBar.setProgress(0); // Set SeekBar progress to 0
                    valueTextView.setText("1"); // Update TextView
                    buttonValuesMap.put(buttonId, 0); // Store 0 in the HashMap
                    saveValueToSharedPreferences(buttonId, 0); // Store 0 in SharedPreferences
                    // Upload button values to Firestore
                    getGroupIdFromFirestore();
                }
                // If the checkbox is unchecked
            }
        });


    }

    private void uploadCommentToFirestore(int buttonId, String comment) {
        // Get the field name based on the buttonId
        String field = getButtonName(buttonId);
        String userId = mAuth.getCurrentUser().getUid();

        // Construct the document reference path
        DocumentReference commentDocRef = FirebaseFirestore.getInstance()
                .collection("userresponses")
                .document(userId)
                .collection("comments")
                .document(field);

        // Create a map with the comment data
        Map<String, Object> data = new HashMap<>();
        data.put("comment", comment);
        data.put("timestamp", FieldValue.serverTimestamp());

        // Set the comment data in Firestore
        commentDocRef.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Comment added successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add comment
                    }
                });
    }



    private void handleGroupId(String groupId) {
        // Now that you have the group ID, you can proceed with your logic
        // For example, you can save the group ID in SharedPreferences for later use
        saveGroupIdToSharedPreferences(groupId);

        // You can also perform any other actions based on the retrieved group ID
        // For example, you may want to display the group ID in a TextView
        //textViewGroupId.setText(groupId);

        // Or you can call another method to perform additional tasks
        //performAdditionalTasks(groupId);
    }

    private void saveGroupIdToSharedPreferences(String groupId) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("groupID", groupId);
        editor.apply();
    }

    private void getGroupIdFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Check if groupID field exists and is not null
                                if (documentSnapshot.contains("groupID")) {
                                    String groupId = documentSnapshot.getString("groupID");
                                    if (groupId != null) {
                                        // Successfully retrieved group ID, now you can use it
                                        // Call a method to handle further actions with the group ID
                                        handleGroupId(groupId);
                                        // Upload button values to Firestore here
                                        uploadButtonValuesToFirestore(userId, groupId);
                                    } else {
                                        // Group ID is null
                                        // Handle this scenario accordingly
                                    }
                                } else {
                                    // 'groupID' field not found in document
                                    // Handle this scenario accordingly
                                }
                            } else {
                                // User document does not exist
                                // Handle this scenario accordingly
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error occurred while retrieving group ID
                            // Handle the failure scenario
                        }
                    });
        }
    }

    // Method to save button value to SharedPreferences
    private void saveValueToSharedPreferences(int buttonId, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(String.valueOf(buttonId), value + 1);
        editor.apply();
    }

    // Method to retrieve button value from SharedPreferences
    private int getSavedValue(int buttonId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(String.valueOf(buttonId), -1); // -1 indicates no value found
    }

    // Method to upload button values to Firestore
    private void uploadButtonValuesToFirestore(String userId, String groupId) {
        String documentId = userId;
        // Create a map with button names and values
        Map<String, Object> data = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : buttonValuesMap.entrySet()) {
            int buttonId = entry.getKey();
            int value = entry.getValue();
            String buttonName = getButtonName(buttonId); // Get the name of the button
            data.put(buttonName, value); // Store button name as key and value as value
        }

        // Retrieve username and add it to the data map
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Retrieve username from users collection
                            String username = documentSnapshot.getString("username");

                            // Add username and groupID to the data map
                            data.put("username", username);
                            data.put("groupID", groupId);

                            // Upload button values to Firestore
                            db.collection("userresponses").document(documentId)
                                    .set(data, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Handle success
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle failure
                                        }
                                    });
                        } else {
                            // User document does not exist
                            // Handle this scenario accordingly
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error occurred while retrieving username
                        // Handle the failure scenario
                    }
                });
    }



    // Method to get the name of the button based on its ID
    private String getButtonName(int buttonId) {
        if (buttonId == R.id.balanceWheel_btn1) {
            return "Economy";
        } else if (buttonId == R.id.balanceWheel_btn2) {
            return "Career";
        } else if (buttonId == R.id.balanceWheel_btn3) {
            return "Wellbeing";
        } else if (buttonId == R.id.balanceWheel_btn4) {
            return "CloseRelations";
        } else if (buttonId == R.id.balanceWheel_btn5) {
            return "Relations";
        } else if (buttonId == R.id.balanceWheel_btn6) {
            return "Development";
        } else if (buttonId == R.id.balanceWheel_btn7) {
            return "Environment";
        } else if (buttonId == R.id.balanceWheel_btn8) {
            return "Rest";
        } else {
            return "Unknown";
        }
    }



    // Method to restore button values from SharedPreferences
    private void restoreValuesFromSharedPreferences() {
        // Here you need to implement the logic to restore button values from SharedPreferences
        // Iterate over each button and restore its value
        // For each button ID, call getSavedValue() method and set its value accordingly
        int[] buttonIds = {R.id.balanceWheel_btn1, R.id.balanceWheel_btn2, R.id.balanceWheel_btn3, R.id.balanceWheel_btn4, R.id.balanceWheel_btn5, R.id.balanceWheel_btn6, R.id.balanceWheel_btn7, R.id.balanceWheel_btn8};

        for (int buttonId : buttonIds) {
            int savedValue = getSavedValue(buttonId);
            if (savedValue != -1) {
                buttonValuesMap.put(buttonId, savedValue);
            }
        }
    }
}
