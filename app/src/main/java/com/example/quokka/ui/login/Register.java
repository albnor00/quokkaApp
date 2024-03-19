package com.example.quokka.ui.login;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    TextInputLayout inputLayoutpassword2, inputLayoutemail2, inputLayoutpasswordRepeat, inputLayoutusername;
    TextInputEditText editTextEmail, editTextPassword, editUsername, editTextRepeatPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editUsername = findViewById(R.id.username);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextRepeatPassword = findViewById(R.id.passwordRepeat);
        editTextPassword = findViewById(R.id.password);
        inputLayoutpasswordRepeat = findViewById(R.id.repeatpassword);
        inputLayoutemail2 = findViewById(R.id.layoutEmail);
        inputLayoutusername = findViewById(R.id.layoutUsername);
        inputLayoutpassword2 = findViewById(R.id.layoutPassword);

        buttonReg = findViewById(R.id.btn_signup);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        db = FirebaseFirestore.getInstance();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });


        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, username, passwordRepeat;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                username = String.valueOf(editUsername.getText());
                passwordRepeat = String.valueOf(editTextRepeatPassword.getText());
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(Register.this, "Enter username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(passwordRepeat)) {
                    Toast.makeText(Register.this, "Repeat password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!passwordRepeat.equals(password)) {
                    Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(passwordRepeat)) {
                    Toast.makeText(Register.this, "Repeat password", Toast.LENGTH_SHORT).show();
                    return; // Return if repeat password is empty
                }


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(Register.this, "Account Registered", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        String userId = user.getUid();
                                        String userEmail = user.getEmail();

                                        // Create a new user document in Firestore
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("email", userEmail);
                                        userData.put("username", username);

                                        // Add the user document to Firestore
                                        db.collection("users").document(userId)
                                                .set(userData)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "DocumentSnapshot added with ID: " + userId);
                                                        } else {
                                                            Log.w(TAG, "Error adding document", task.getException());
                                                        }
                                                    }
                                                });
                                    }
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        //remove hint when focus
        editTextEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && editTextEmail.getText().toString().isEmpty()) {
                    inputLayoutemail2.setHintEnabled(true);
                } else {
                    inputLayoutemail2.setHintEnabled(false);
                }
            }
        });

        editTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && editTextPassword.getText().toString().isEmpty()) {
                    inputLayoutpassword2.setHintEnabled(true);
                } else {
                    inputLayoutpassword2.setHintEnabled(false);
                }
            }
        });

        editTextRepeatPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && editTextRepeatPassword.getText().toString().isEmpty()) {
                    inputLayoutpasswordRepeat.setHintEnabled(true);
                } else {
                    inputLayoutpasswordRepeat.setHintEnabled(false);
                }
            }
        });

        editUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && editUsername.getText().toString().isEmpty()) {
                    inputLayoutusername.setHintEnabled(true);
                } else {
                    inputLayoutusername.setHintEnabled(false);
                }
            }
        });

    }
}
