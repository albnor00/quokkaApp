package com.example.quokka.tasks.profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.R;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {
    AppCompatButton back;

    AppCompatButton updateUsername;

    AppCompatButton updateEmail;

    AppCompatButton updatePassword;

    AppCompatButton deleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

       back = findViewById(R.id.accountSettings_back);
       updateUsername = findViewById(R.id.username_change);
       updateEmail = findViewById(R.id.email_change);
       updatePassword = findViewById(R.id.password_change);
       deleteAccount = findViewById(R.id.delete_account);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        updateUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeUsername();

            }
        });

        updateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeEmailDialog();
            }
        });


        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showChangePasswordDialog();
            }
        });


        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
    }

    private void deleteAccount(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");

        // Add the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Delete button
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    String userId = user.getUid();
                    DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);
                    userRef.delete();
                    user.delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Account deleted successfully
                                    Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SettingsActivity.this, Login.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Failed to delete account
                                    Toast.makeText(SettingsActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Cancel button
                dialog.dismiss(); // Dismiss the dialog
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void changeUsername() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.activity_change_username, null);

        // Set the view for the dialog
        dialogBuilder.setView(dialogView);

        // Find views inside the dialog layout
        EditText newUsernameEditText = dialogView.findViewById(R.id.newUsername);

        // Set dialog title
        dialogBuilder.setTitle("Change Username");

        dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle username change confirmation here
                String newUsername = newUsernameEditText.getText().toString();

                // Update the username in Firestore
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

                    userRef.update("username", newUsername)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Username updated successfully
                                    Toast.makeText(SettingsActivity.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Username update failed
                                    Toast.makeText(SettingsActivity.this, "Failed to update username", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        // Set negative button and its click listener
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel button clicked, do nothing
            }
        });

        // Create and show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }


    private void showChangeEmailDialog() {
        // Create a dialog builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.activity_change_email, null);

        // Set the view for the dialog
        dialogBuilder.setView(dialogView);

        // Find views inside the dialog layout
        EditText newEmailEditText = dialogView.findViewById(R.id.newEmail);
        EditText passwordEditText = dialogView.findViewById(R.id.password_change_mail);

        // Set dialog title
        dialogBuilder.setTitle("Change Email");

        // Set positive button and its click listener
        dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle email change confirmation here
                String newEmail = newEmailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                // Re-authenticate the user with their current email and password
                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> reauthTask) {
                                if (reauthTask.isSuccessful()) {
                                    // Re-authentication successful, now update the email
                                    currentUser.verifyBeforeUpdateEmail(newEmail)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> updateEmailTask) {
                                                    if (updateEmailTask.isSuccessful()) {
                                                        // Email updated successfully, send verification email
                                                        sendVerificationEmail(currentUser);
                                                    } else {
                                                        // Email update failed
                                                        Toast.makeText(SettingsActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    // Re-authentication failed
                                    Toast.makeText(SettingsActivity.this, "Failed to re-authenticate user", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Set negative button and its click listener
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel button clicked, do nothing
            }
        });

        // Create and show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Email verification sent successfully
                            Toast.makeText(SettingsActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();

                            // Listen for email verification completion
                            FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                    if (currentUser != null && currentUser.isEmailVerified()) {
                                        // Email is verified, update email in database
                                        updateEmailInDatabase();
                                    } else {
                                        // Email is not yet verified
                                        Toast.makeText(SettingsActivity.this, "Please verify your email to complete the process", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // Failed to send verification email
                            Toast.makeText(SettingsActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateEmailInDatabase() {


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("email", user.getEmail())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Email updated in database successfully
                        Toast.makeText(SettingsActivity.this, "Email updated in database", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update email in database
                        Toast.makeText(SettingsActivity.this, "Failed to update email in database", Toast.LENGTH_SHORT).show();
                    }
                });
    }








    private void showChangePasswordDialog() {
        // Create a dialog builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.activity_change_password, null);

        // Set the view for the dialog
        dialogBuilder.setView(dialogView);

        // Find views inside the dialog layout
        EditText currentPasswordEditText = dialogView.findViewById(R.id.currentPassword);
        EditText newPasswordEditText = dialogView.findViewById(R.id.newPassword);
        EditText newPasswordRepeatEditText = dialogView.findViewById(R.id.newPasswordRepeat);

        // Set dialog title
        dialogBuilder.setTitle("Change Password");

        // Set positive button and its click listener
        dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle password
                String currentPassword = currentPasswordEditText.getText().toString();
                String newPassword = newPasswordEditText.getText().toString();
                String newPasswordRepeat = newPasswordRepeatEditText.getText().toString();


                // Check if new passwords match
                if (newPassword.equals(newPasswordRepeat)) {
                    // Call method to change password
                    changePassword(currentPassword, newPassword);
                } else {
                    // Show error message if new passwords don't match
                    Toast.makeText(SettingsActivity.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set negative button and its click listener
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel button clicked, do nothing
            }
        });

        // Create and show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }


    private void changePassword(String currentPassword, String newPassword) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // authenticate user with current password if success change password to new
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // User re-authenticated successfully, update the password
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Password updated successfully
                                                Toast.makeText(SettingsActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Password update failed
                                                Toast.makeText(SettingsActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Re-authentication failed, show an error message
                            Toast.makeText(SettingsActivity.this, "Failed to re-authenticate user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
