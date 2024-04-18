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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
        back = findViewById(R.id.memberanswer_back);
        textViewMemberName = findViewById(R.id.textViewMemberName);

        profilePictureImageView = findViewById(R.id.profile_picture);

        // Get user ID from intent
        String userID = getIntent().getStringExtra("userID");


        // Fetch answers from Firestore based on the user ID, group ID, and username
        fetchAnswers(userID);
        fetchProfileImage(userID);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), group_viewMembers.class);
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

    private void fetchAnswers(String userID) {


        FirebaseFirestore.getInstance().collection("userresponses")
                .document(userID) // Query document by constructed path
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //set text to username
                            String username = documentSnapshot.getString("username");
                            textViewMemberName.setText(username);
                            // Call displayAnswers method with the DocumentSnapshot
                            displayAnswers(documentSnapshot);
                        } else {
                            // Handle accordingly if document does not exist
                            Log.d("FetchAnswers", "Document does not exist for userID: " + userID);
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
    }


    private void displayAnswers(DocumentSnapshot documentSnapshot) {
        // Define an array of answer fields
        String[] fields = {"Career", "CloseRelations", "Development", "Economy", "Environment", "Relations", "Rest", "Wellbeing"};

        for (String field : fields) {
            Integer value = documentSnapshot.getLong(field).intValue(); // Extract value from documentSnapshot

            TextView textView;
            if (value != 0) {
                // Create and configure TextView
                textView = createStyledTextView(field + ": " + value, field);
                // Add TextView to the answerListContainer
            } else { //user prefer not to answer
                textView = createStyledTextView(field + ": " + "Prefer not to answer", field);
            }
            answerListContainer.addView(textView);
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
        // Get userId from the person we clicked
        String userID = getIntent().getStringExtra("userID");

        // Inflate the popup layout
        View popupView = getLayoutInflater().inflate(R.layout.activity_view_comment_popup, null);

        // Find the TextView in the popup layout
        final TextView popupMessageTextView = popupView.findViewById(R.id.popup_message);
        TextView HeaderMessageTextView = popupView.findViewById(R.id.header_popup);

        // Header text
        HeaderMessageTextView.setText("Additional comments from coachee on " + field.toLowerCase());
        HeaderMessageTextView.setTypeface(Typeface.DEFAULT_BOLD);

        // Fetch the comment from Firestore
        FirebaseFirestore.getInstance().collection("userresponses")
                .document(userID)
                .collection("comments")
                .document(field)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Get the comment from the document
                            String comment = documentSnapshot.getString("comment");
                            // Set the message text
                            popupMessageTextView.setText(comment);
                        } else {
                            // If the document doesn't exist, display a message
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
                    }
                });
    }

}

