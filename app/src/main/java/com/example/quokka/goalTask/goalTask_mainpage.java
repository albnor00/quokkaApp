package com.example.quokka.goalTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class goalTask_mainpage extends AppCompatActivity {

    ImageView back_btn;
    RecyclerView recyclerViewToDo;
    EditText editTextNewToDo;
    Button buttonAddToDo;
    List<String> toDoList;
    ToDoAdapter adapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_task_mainpage); // Set the layout XML file

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String userId = auth.getCurrentUser().getUid(); // Get the current user's ID

        // Initialize views
        back_btn = findViewById(R.id.img_back);
        recyclerViewToDo = findViewById(R.id.recyclerViewToDo);
        editTextNewToDo = findViewById(R.id.editTextNewToDo);
        buttonAddToDo = findViewById(R.id.buttonAddToDo);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up the RecyclerView
        toDoList = new ArrayList<>();
        adapter = new ToDoAdapter(toDoList);
        recyclerViewToDo.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewToDo.setAdapter(adapter);

        // Load tasks from Firestore
        loadTasksFromFirestore(userId);

        // Set up back button
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set up add button
        buttonAddToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTask = editTextNewToDo.getText().toString().trim();
                if (!newTask.isEmpty()) {
                    // Add task to Firestore
                    addTaskToFirestore(userId, newTask);
                    editTextNewToDo.setText("");
                }
            }
        });
    }

    private void loadTasksFromFirestore(String userId) {
        db.collection("users")
                .document(userId)
                .collection("todo_tasks")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String task = documentSnapshot.getString("task");
                            toDoList.add(task);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(goalTask_mainpage.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addTaskToFirestore(String userId, String task) {
        db.collection("users")
                .document(userId)
                .collection("todo_tasks")
                .add(new HashMap<String, Object>() {{
                    put("task", task);
                }})
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        toDoList.add(task);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(goalTask_mainpage.this, "Failed to add task", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

