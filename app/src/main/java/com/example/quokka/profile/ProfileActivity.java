package com.example.quokka.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.group.NotificationsActivity;
import com.example.quokka.tasks.balance_wheel;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class ProfileActivity extends AppCompatActivity {

     ImageView imageView2;
     FloatingActionButton selectImageButton;
     AppCompatButton notificationButton;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseAuth mAuth;

    TextView usernameText;
    TextView emailText;

    FirebaseFirestore db;
    FirebaseUser mUser;



    AppCompatButton settingsButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usernameText = findViewById(R.id.username_profile);
        emailText = findViewById(R.id.email_profile);

        settingsButton = findViewById(R.id.accountSettings);


        imageView2 = findViewById(R.id.imageView2);
        selectImageButton = findViewById(R.id.select_picture);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("profileImages").child(user.getUid());
        db = FirebaseFirestore.getInstance();

        mUser = mAuth.getCurrentUser();


        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });


        notificationButton = findViewById(R.id.notificationButton);

        // Set click listener for notifications
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                markNotificationsAsRead();
                Intent intent = new Intent(ProfileActivity.this, NotificationsActivity.class);
                startActivity(intent);
            }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });




        FirebaseFirestore db = FirebaseFirestore.getInstance();




        db.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String email = user.getEmail();
                    usernameText.setText(username);
                    emailText.setText(email);
                } else {
                    emailText.setText("Unkown");
                    usernameText.setText("Unknown");
                }
            }
        });
        // Load image if already uploaded
        loadImageFromFirebaseStorage();


        //Check if there are unread notification
        checkUnreadNotifications();

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
                    MainActivity.checkUserRole(user,ProfileActivity.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            imageView2.setImageURI(filePath);
            uploadImage();
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            StorageReference ref = storageReference.child("profile.jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failed upload
                        }
                    });
        }
    }

    private void loadImageFromFirebaseStorage() {
        StorageReference ref = storageReference.child("profile.jpg");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the image using Glide
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache both original & resized image

                Glide.with(ProfileActivity.this)
                        .load(uri)
                        .apply(requestOptions)
                        .into(imageView2);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle any errors
            }
        });
    }

    private void checkUnreadNotifications() {
        db.collection("users").document(mUser.getUid()).collection("notifications")
                .whereEqualTo("read", false)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // There are unread notifications
                           // notificationButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_access_time, 0);
                        } else {
                            // No unread notifications
                            //notificationButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        }
                    }
                });
    }


    private void markNotificationsAsRead() {
        db.collection("notifications").whereEqualTo("userId", mUser.getUid()).whereEqualTo("read", false)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            db.collection("notifications").document(document.getId()).update("read", true);
                        }
                        // Update the notification button drawable
                        notificationButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_notifications_24, 0, 0, 0);
                    }
                });
    }
}


