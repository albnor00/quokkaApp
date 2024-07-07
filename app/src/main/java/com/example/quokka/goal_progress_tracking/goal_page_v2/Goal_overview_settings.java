package com.example.quokka.goal_progress_tracking.goal_page_v2;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.goal_setup.Question2;
import com.example.quokka.goal_progress_tracking.habit_task_template.habit_task_settings_page;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Goal_overview_settings extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView imgBack;
    private ImageView imgCheckMark;
    private EditText answerQ3EditText;
    private EditText answerQ5EditText;
    private CardView resetGoalView; // Assuming it's a custom view and not standard Android view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_overview_settings_page);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        imgBack = findViewById(R.id.img_back);
        imgCheckMark = findViewById(R.id.img_check_mark);
        answerQ3EditText = findViewById(R.id.answerQ3EditText);
        answerQ5EditText = findViewById(R.id.answerQ5EditText);
        resetGoalView = findViewById(R.id.reset_goal);

        // Example of setting onClickListener on imgBack
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Goal_overview.class);
                startActivity(intent);
                finish();
            }
        });

        // Example of setting onClickListener on imgCheckMark
        imgCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateExplanations();
                Intent intent = new Intent(getApplicationContext(), Goal_overview.class);
                startActivity(intent);
                finish();
            }
        });

        // Example of setting onClickListener on resetGoalView
        resetGoalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Goal_overview_settings.this);
                builder.setTitle("Reset Goal");
                builder.setMessage("Are you sure you want to reset your Goal? You wont be able to recover the data from your tasks.");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete the log
                        deleteGoalCollection();
                        deleteTasksAndLogs();

                        Intent intent = new Intent(getApplicationContext(), Goal_empty_page.class);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
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
                    MainActivity.checkUserRole2(user, Goal_overview_settings.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });
    }

    private void updateExplanations() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User not logged in");
            return;
        }
        String userId = user.getUid();

        // Get the text from EditText views
        String explanationQ3 = answerQ3EditText.getText().toString();
        String explanationQ5 = answerQ5EditText.getText().toString();

        // Update Q3_user_explanation
        db.collection("users").document(userId).collection("Goal").document("Q3_user_explanation")
                .update("user_explanation", explanationQ3)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Q3_user_explanation updated successfully");
                        // Handle success as needed
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error updating Q3_user_explanation", e);
                        // Handle failure as needed
                    }
                });

        // Update Q5_user_explanation
        db.collection("users").document(userId).collection("Goal").document("Q5_user_explanation")
                .update("user_explanation", explanationQ5)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Q5_user_explanation updated successfully");
                        // Handle success as needed
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error updating Q5_user_explanation", e);
                        // Handle failure as needed
                    }
                });
    }

    private void deleteGoalCollection() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User not logged in");
            return;
        }
        String userId = user.getUid();

        // Define the subcollections to delete within the Goal collection
        String[] subcollectionNames = {"averageTasks", "targetTasks", "habitTasks"};

        // Get a reference to the Goal collection
        CollectionReference goalCollectionRef = db.collection("users").document(userId).collection("Goal");

        // Delete the main Goal collection and all its subcollections recursively
        deleteCollectionAndSubcollections(goalCollectionRef, subcollectionNames)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Goal collection and its subcollections deleted successfully");
                        // Handle success as needed (e.g., navigate to a new activity)
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting goal collection and its subcollections", e);
                        // Handle failure (e.g., display an error message)
                    }
                });
    }

    // Helper method to recursively delete a collection and its documents
    private Task<Void> deleteCollection(CollectionReference collectionRef) {
        // Limit batch size because deleting large collections in one go can fail
        final int batchSize = 50;
        Query query = collectionRef.limit(batchSize);

        return deleteQueryBatch(query);
    }

    // Helper method to delete documents in a batch recursively
    private Task<Void> deleteQueryBatch(Query query) {
        return query.get()
                .continueWithTask(new Continuation<QuerySnapshot, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        List<Task<Void>> tasks = new ArrayList<>();

                        for (DocumentSnapshot doc : task.getResult()) {
                            tasks.add(doc.getReference().delete());
                        }

                        // Recursively delete documents in batches
                        if (tasks.size() > 0) {
                            return Tasks.whenAll(tasks).continueWithTask(new Continuation<Void, Task<Void>>() {
                                @Override
                                public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                                    return deleteQueryBatch(query);
                                }
                            });
                        }

                        return Tasks.forResult(null);
                    }
                });
    }

    // Helper method to delete a document and its subcollections
    private Task<Void> deleteCollectionAndSubcollections(CollectionReference collectionRef, String[] subcollectionNames) {
        // Delete the main collection and all its known subcollections
        return deleteCollection(collectionRef)  // First delete the main collection
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Delete each subcollection recursively
                        List<Task<Void>> tasks = new ArrayList<>();
                        for (String subcollectionName : subcollectionNames) {
                            CollectionReference subcollectionRef = collectionRef.document(subcollectionName).collection(subcollectionName);
                            tasks.add(deleteCollection(subcollectionRef));
                        }
                        // Return combined task of deleting the main collection and its subcollections
                        Tasks.whenAll(tasks);
                    }
                });
    }

    private void deleteTasksAndLogs() {
        deleteAverageTasksAndLogs();
        deleteTargetTasksAndLogs();
        deleteHabitTasksAndLogs();
    }

    private void deleteAverageTasksAndLogs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User not logged in");
            return;
        }
        String userId = user.getUid();

        // Reference to the averageTasks document
        DocumentReference averageTasksDocRef = FirebaseFirestore.getInstance().collection("users")
                .document(userId).collection("Goal").document("averageTasks");

        // Reference to the average_tasks collection
        CollectionReference averageTasksRef = averageTasksDocRef.collection("average_tasks");

        // Get all documents in the average_tasks collection
        averageTasksRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> taskDocuments = task.getResult().getDocuments();
                List<Task<Void>> deleteTasks = new ArrayList<>();

                // Loop through each task document
                for (DocumentSnapshot taskDocument : taskDocuments) {
                    String taskId = taskDocument.getId();

                    // Reference to the loggedLogs collection under this task
                    CollectionReference loggedLogsRef = averageTasksRef.document(taskId)
                            .collection("loggedLogs");

                    // Get all logs for this task and delete them
                    loggedLogsRef.get().addOnCompleteListener(logsTask -> {
                        if (logsTask.isSuccessful()) {
                            List<DocumentSnapshot> logDocuments = logsTask.getResult().getDocuments();
                            List<Task<Void>> deleteLogTasks = new ArrayList<>();

                            // Delete each log document
                            for (DocumentSnapshot logDocument : logDocuments) {
                                deleteLogTasks.add(logDocument.getReference().delete());
                            }

                            // After deleting logs, delete the task document itself
                            Tasks.whenAll(deleteLogTasks).addOnCompleteListener(deleteLogsTask -> {
                                if (deleteLogsTask.isSuccessful()) {
                                    deleteTasks.add(averageTasksRef.document(taskId).delete());
                                } else {
                                    Log.e(TAG, "Error deleting logs for task: " + taskId, deleteLogsTask.getException());
                                    // Handle error deleting logs
                                }
                            });
                        } else {
                            Log.e(TAG, "Error getting logs for task: " + taskId, logsTask.getException());
                            // Handle error getting logs
                        }
                    });
                }

                // After deleting all tasks and logs, delete the averageTasks document itself
                Tasks.whenAll(deleteTasks).addOnSuccessListener(aVoid -> {
                    // Delete the averageTasks document
                    averageTasksDocRef.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d(TAG, "Deleted averageTasks document and all related tasks and logs");
                                // Handle success (e.g., update UI)
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting averageTasks document", e);
                                // Handle failure deleting averageTasks document
                            });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting tasks and logs", e);
                    // Handle failure deleting tasks and logs
                });
            } else {
                Log.e(TAG, "Error getting average_tasks collection", task.getException());
                // Handle error getting average_tasks collection
            }
        });
    }

    private void deleteTargetTasksAndLogs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User not logged in");
            return;
        }
        String userId = user.getUid();

        // Reference to the averageTasks document
        DocumentReference targetTasksDocRef = FirebaseFirestore.getInstance().collection("users")
                .document(userId).collection("Goal").document("targetTasks");

        // Reference to the average_tasks collection
        CollectionReference targetTasksRef = targetTasksDocRef.collection("target_tasks");

        // Get all documents in the average_tasks collection
        targetTasksRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> taskDocuments = task.getResult().getDocuments();
                List<Task<Void>> deleteTasks = new ArrayList<>();

                // Loop through each task document
                for (DocumentSnapshot taskDocument : taskDocuments) {
                    String taskId = taskDocument.getId();

                    // Reference to the loggedLogs collection under this task
                    CollectionReference loggedLogsRef = targetTasksRef.document(taskId)
                            .collection("loggedLogs");

                    // Get all logs for this task and delete them
                    loggedLogsRef.get().addOnCompleteListener(logsTask -> {
                        if (logsTask.isSuccessful()) {
                            List<DocumentSnapshot> logDocuments = logsTask.getResult().getDocuments();
                            List<Task<Void>> deleteLogTasks = new ArrayList<>();

                            // Delete each log document
                            for (DocumentSnapshot logDocument : logDocuments) {
                                deleteLogTasks.add(logDocument.getReference().delete());
                            }

                            // After deleting logs, delete the task document itself
                            Tasks.whenAll(deleteLogTasks).addOnCompleteListener(deleteLogsTask -> {
                                if (deleteLogsTask.isSuccessful()) {
                                    deleteTasks.add(targetTasksRef.document(taskId).delete());
                                } else {
                                    Log.e(TAG, "Error deleting logs for task: " + taskId, deleteLogsTask.getException());
                                    // Handle error deleting logs
                                }
                            });
                        } else {
                            Log.e(TAG, "Error getting logs for task: " + taskId, logsTask.getException());
                            // Handle error getting logs
                        }
                    });
                }

                // After deleting all tasks and logs, delete the averageTasks document itself
                Tasks.whenAll(deleteTasks).addOnSuccessListener(aVoid -> {
                    // Delete the averageTasks document
                    targetTasksDocRef.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d(TAG, "Deleted averageTasks document and all related tasks and logs");
                                // Handle success (e.g., update UI)
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting averageTasks document", e);
                                // Handle failure deleting averageTasks document
                            });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting tasks and logs", e);
                    // Handle failure deleting tasks and logs
                });
            } else {
                Log.e(TAG, "Error getting average_tasks collection", task.getException());
                // Handle error getting average_tasks collection
            }
        });
    }

    private void deleteHabitTasksAndLogs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User not logged in");
            return;
        }
        String userId = user.getUid();

        // Reference to the averageTasks document
        DocumentReference habitTasksDocRef = FirebaseFirestore.getInstance().collection("users")
                .document(userId).collection("Goal").document("habitTasks");

        // Reference to the average_tasks collection
        CollectionReference habitTasksRef = habitTasksDocRef.collection("habit_tasks");

        // Get all documents in the average_tasks collection
        habitTasksRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> taskDocuments = task.getResult().getDocuments();
                List<Task<Void>> deleteTasks = new ArrayList<>();

                // Loop through each task document
                for (DocumentSnapshot taskDocument : taskDocuments) {
                    String taskId = taskDocument.getId();

                    // Reference to the loggedLogs collection under this task
                    CollectionReference loggedLogsRef = habitTasksRef.document(taskId)
                            .collection("loggedLogs");

                    // Get all logs for this task and delete them
                    loggedLogsRef.get().addOnCompleteListener(logsTask -> {
                        if (logsTask.isSuccessful()) {
                            List<DocumentSnapshot> logDocuments = logsTask.getResult().getDocuments();
                            List<Task<Void>> deleteLogTasks = new ArrayList<>();

                            // Delete each log document
                            for (DocumentSnapshot logDocument : logDocuments) {
                                deleteLogTasks.add(logDocument.getReference().delete());
                            }

                            // After deleting logs, delete the task document itself
                            Tasks.whenAll(deleteLogTasks).addOnCompleteListener(deleteLogsTask -> {
                                if (deleteLogsTask.isSuccessful()) {
                                    deleteTasks.add(habitTasksRef.document(taskId).delete());
                                } else {
                                    Log.e(TAG, "Error deleting logs for task: " + taskId, deleteLogsTask.getException());
                                    // Handle error deleting logs
                                }
                            });
                        } else {
                            Log.e(TAG, "Error getting logs for task: " + taskId, logsTask.getException());
                            // Handle error getting logs
                        }
                    });
                }

                // After deleting all tasks and logs, delete the averageTasks document itself
                Tasks.whenAll(deleteTasks).addOnSuccessListener(aVoid -> {
                    // Delete the averageTasks document
                    habitTasksDocRef.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d(TAG, "Deleted averageTasks document and all related tasks and logs");
                                // Handle success (e.g., update UI)
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting averageTasks document", e);
                                // Handle failure deleting averageTasks document
                            });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting tasks and logs", e);
                    // Handle failure deleting tasks and logs
                });
            } else {
                Log.e(TAG, "Error getting average_tasks collection", task.getException());
                // Handle error getting average_tasks collection
            }
        });
    }

}
