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
import android.widget.Button;
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

    Button submit;

    // HashMaps to store button values, checkbox states, and comments
    HashMap<Integer, Integer> buttonValuesMap = new HashMap<>();
    HashMap<Integer, Boolean> checkBoxValuesMap = new HashMap<>();
    HashMap<Integer, String> commentValuesMap = new HashMap<>();

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
        submit = findViewById(R.id.submit);

        back.bringToFront();

        // Restore values from SharedPreferences
        restoreValuesFromSharedPreferences();

        // Set onClickListener for back button
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), tasksMain.class);
                startActivity(intent);
                finish();
            }
        });

        // Set onClickListener for submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), tasksMain.class);
                startActivity(intent);


                // Upload button values, checkbox states, and comments to Firestore
                uploadDataToFirestore();
                // Finish activity after submitting
                finish();
            }
        });

        // Set onClickListener for radio buttons
        radioButtons.setOnSliceClickListener(new RadioButtons.OnSliceClickListener() {
            @Override
            public void onSlickClick(int slicePosition) {
                // Handle click events for each slice position
                switch (slicePosition) {
                    case 0:
                        showSeekBarDialog(btn_Career.getId(), "Why do you think your career is good or bad?");

                        break;
                    case 1:
                        showSeekBarDialog(btn_Wellbeing.getId(), "Why do you think your wellbeing is good or bad?");
                        break;
                    case 2:
                        showSeekBarDialog(btn_CloseRelations.getId(), "Why do you think your close relationships is good or bad?");
                        break;
                    case 3:
                        showSeekBarDialog(btn_Relations.getId(), "Why do you think your relations is good or bad?");
                        break;
                    case 4:
                        showSeekBarDialog(btn_Development.getId(), "Why do you think your development is good or bad?");
                        break;
                    case 5:
                        showSeekBarDialog(btn_Environment.getId(), "Why do you think your environment is good or bad?");
                        break;
                    case 6:
                        showSeekBarDialog(btn_Rest.getId(), "Why do you think your rest is good or bad?");
                        break;
                    case 7:
                        showSeekBarDialog(btn_Economy.getId(), "Why do you think your economy is good or bad?");
                        break;
                }
            }
        });
    }

    private void showSeekBarDialog(final int buttonId,String hint) {
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

        addComment.setHint(hint);



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
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Uncheck checkbox if the seekbar is touched
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
            }
        });

        seekBarDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Upload comment if not empty when the dialog is dismissed
                String comment = addComment.getText().toString();
                if (!comment.isEmpty()) {
                    // Store the comment in the commentValuesMap
                    commentValuesMap.put(buttonId, comment);
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
                    // If checkbox is checked, store the state in the HashMap
                    checkBoxValuesMap.put(buttonId, true);
                    // Set SeekBar progress to 0
                    seekBar.setProgress(0);
                    // Update TextView
                    valueTextView.setText("1");
                    // Store 0 in the SharedPreferences
                    saveValueToSharedPreferences(buttonId, 0);
                } else {
                    // If checkbox is unchecked, remove the state from the HashMap
                    checkBoxValuesMap.remove(buttonId);
                }
            }
        });
    }

    private void uploadDataToFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Create a map to store button values
            Map<String, Object> buttonValues = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : buttonValuesMap.entrySet()) {
                int buttonId = entry.getKey();
                int value = entry.getValue();
                String buttonName = getButtonName(buttonId);
                buttonValues.put(buttonName, value);
            }

            // Create a map to store submission data
            Map<String, Object> submissionData = new HashMap<>();
            submissionData.put("values", buttonValues);
            submissionData.put("timestamp", FieldValue.serverTimestamp());

            // Add the submission data to Firestore
            db.collection("userresponses")
                    .document(userId)
                    .collection("submissions")
                    .add(submissionData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            // Handle success
                            // Call method to upload comments after uploading main data
                            uploadCommentsToFirestore(userId, documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure
                        }
                    });
        }
    }

    private void uploadCommentsToFirestore(String userId, String submissionId) {
        // Create a map to store comments
        Map<String, Object> commentsData = new HashMap<>();
        for (Map.Entry<Integer, String> entry : commentValuesMap.entrySet()) {
            int buttonId = entry.getKey();
            String comment = entry.getValue();
            String buttonName = getButtonName(buttonId);
            commentsData.put(buttonName, comment);
        }

        // Add the comments data to Firestore
        db.collection("userresponses")
                .document(userId)
                .collection("submissions")
                .document(submissionId)
                .set(commentsData, SetOptions.merge())
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
    }


    private void saveValueToSharedPreferences(int buttonId, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(String.valueOf(buttonId), value + 1);
        editor.apply();
    }

    private int getSavedValue(int buttonId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(String.valueOf(buttonId), -1); // -1 indicates no value found
    }

    private void restoreValuesFromSharedPreferences() {
        int[] buttonIds = {R.id.balanceWheel_btn1, R.id.balanceWheel_btn2, R.id.balanceWheel_btn3, R.id.balanceWheel_btn4,
                R.id.balanceWheel_btn5, R.id.balanceWheel_btn6, R.id.balanceWheel_btn7, R.id.balanceWheel_btn8};

        for (int buttonId : buttonIds) {
            int savedValue = getSavedValue(buttonId);
            if (savedValue != -1) {
                buttonValuesMap.put(buttonId, savedValue);
            }
        }
    }

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
}
