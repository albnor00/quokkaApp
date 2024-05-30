package com.example.quokka.group;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class group_viewMembers_answers extends AppCompatActivity {

    LinearLayout answerListContainer;
    TextView textViewMemberName;

    ImageView profilePictureImageView;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_answers);

        answerListContainer = findViewById(R.id.answerListContainer);
        back = findViewById(R.id.memberAnswer_back);
        textViewMemberName = findViewById(R.id.textViewMemberName);
        profilePictureImageView = findViewById(R.id.profile_picture);

        // Get user ID and submission ID from intent
        String userID = getIntent().getStringExtra("userId");
        String submissionId = getIntent().getStringExtra("submissionId");
        String name = getIntent().getStringExtra("username");

        textViewMemberName.setText(name);

        // Fetch answers from Firestore based on the user ID and submission ID
        fetchAnswers(userID, submissionId);
        fetchProfileImage(userID);

        // Set click listener for back button
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve user ID and submission ID from the intent that started this activity
                String userId = getIntent().getStringExtra("userId");
                String submissionId = getIntent().getStringExtra("submissionId");

                // Start group_viewMembers_dateList activity and pass user ID and submission ID as extras
                Intent intent = new Intent(getApplicationContext(), group_viewMembers_dateList.class);
                intent.putExtra("userId", userId);
                intent.putExtra("submissionId", submissionId);
                intent.putExtra("username",name);
                startActivity(intent);
                finish();
            }
        });
    }


    private void fetchProfileImage(String userID) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("profileImages").child(userID).child("profile.jpg");

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the image using Glide
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache both original & resized image

                Glide.with(group_viewMembers_answers.this)
                        .load(uri)
                        .apply(requestOptions)
                        .into(profilePictureImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle any errors
            }
        });
    }

    private void fetchAnswers(String userID, String submissionId) {
        if (userID != null && submissionId != null) {
            FirebaseFirestore.getInstance().collection("userresponses")
                    .document(userID)
                    .collection("submissions")
                    .document(submissionId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Call displayAnswers method with the DocumentSnapshot
                                displayAnswers(documentSnapshot);
                            } else {
                                // Handle accordingly if document does not exist
                                Log.d("FetchAnswers", "Document does not exist for submissionId: " + submissionId);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FetchAnswers", "Error fetching answers: " + e.getMessage());
                            // Handle failures
                        }
                    });
        } else {
            Log.e("FetchAnswers", "userID or submissionId is null");
        }
    }



    private void displayAnswers(DocumentSnapshot documentSnapshot) {
        // Get the nested map under the "values" field
        Map<String, Object> valuesMap = (Map<String, Object>) documentSnapshot.get("values");

        if (valuesMap != null) {
            // Loop through the keys in the nested map
            for (String field : valuesMap.keySet()) {
                Object value = valuesMap.get(field); // Get the value corresponding to the field

                if (value != null) {
                    String text;
                    if (value instanceof Long) {
                        // If the value is a Long, convert it to an integer
                        int intValue = ((Long) value).intValue();
                        text = field + ": " + intValue;
                    } else {
                        // If the value is not a Long, handle it accordingly (e.g., it could be a string)
                        text = field + ": " + value.toString();
                    }

                    TextView textView = createStyledTextView(text, field);
                    answerListContainer.addView(textView);
                } else {
                    // Handle the case where the value is null
                    Log.d("DisplayAnswers", "Value for field '" + field + "' is null.");
                }
            }
        } else {
            // Handle the case where the "values" field is null
            Log.d("DisplayAnswers", "Values map is null.");
        }
    }



    // Method to create a TextView with common styling attributes
    private TextView createStyledTextView(String text, final String field) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK); // Change text color
        textView.setTypeface(Typeface.DEFAULT_BOLD); // Make text bold
        textView.setPadding(16, 30, 16, 30);

        // Set OnClickListener to show popup when clicked
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show popup with details for the clicked field
                String message = "Your custom message for " + field; // Replace this with your actual message
                showPopup(field);
            }
        });

        return textView;
    }


    // Method to show popup when a TextView is clicked
    private void showPopup(final String field) {
        // Get user ID from the intent
        String submissionId = getIntent().getStringExtra("submissionId");
        String userID = getIntent().getStringExtra("userId");

        // Fetch the document from Firestore
        FirebaseFirestore.getInstance().collection("userresponses")
                .document(userID)
                .collection("submissions")
                .document(submissionId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Fetch the value associated with the clicked field
                            Object comment = documentSnapshot.get(field);

                            // Create a popup view
                            View popupView = getLayoutInflater().inflate(R.layout.activity_view_comment_popup, null);

                            // Find the TextView in the popup layout
                            TextView popupMessageTextView = popupView.findViewById(R.id.popup_message);
                            TextView popupMessageTextViewHeader = popupView.findViewById(R.id.header_popup);

                            // Set header text
                            popupMessageTextViewHeader.setText("Additional comments on " + field.toLowerCase());

                            if (comment != null) {
                                // Set the message text
                                popupMessageTextView.setText("" + comment);
                            } else {
                                // If comment is null, display a message
                                popupMessageTextView.setText("No comment available");
                            }

                            // Create a PopupWindow with the inflated layout as its content view
                            PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                            // Set background drawable for the popup window (optional)
                            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                            // Set whether the popup window should be dismissed when touched outside of it
                            popupWindow.setOutsideTouchable(true);

                            // Set whether the popup window should be focusable
                            popupWindow.setFocusable(true);

                            // Show the popup window
                            popupWindow.showAtLocation(answerListContainer, Gravity.CENTER, 0, 0);
                        } else {
                            // Handle accordingly if document does not exist
                            Log.d("ShowPopup", "Document does not exist for userID: " + userID);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failures
                        Log.e("ShowPopup", "Error fetching document: " + e.getMessage());
                    }
                });
    }








}


