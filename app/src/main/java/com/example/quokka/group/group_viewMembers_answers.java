package com.example.quokka.group;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class group_viewMembers_answers extends AppCompatActivity {

    LinearLayout answerListContainer;

    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_answers);

        answerListContainer = findViewById(R.id.answerListContainer);

        back = findViewById(R.id.memberanswer_back);



        // Get user ID, group ID, and username from intent
        String userID = getIntent().getStringExtra("userID");
        String groupID = getIntent().getStringExtra("groupID");

        // Fetch answers from Firestore based on the user ID, group ID, and username
        fetchAnswers(userID,groupID);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), group_viewMembers.class);
                startActivity(intent);
                finish();
            }
        });
    }



    private void fetchAnswers(String userID, String groupID) {
        String documentPath = userID + "_" + groupID; // Construct the document path

        FirebaseFirestore.getInstance().collection("userresponses")
                .document(documentPath) // Query document by constructed path
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
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

            // Create and configure TextView
            TextView textView = createStyledTextView(field + ": " + value);

            // Add TextView to the answerListContainer
            answerListContainer.addView(textView);
        }
    }

    // Method to create a TextView with common styling attributes
    private TextView createStyledTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK); // Change text color
        textView.setTypeface(Typeface.DEFAULT_BOLD); // Make text bold
        textView.setPadding(16, 8, 16, 8);
        return textView;
    }



}
