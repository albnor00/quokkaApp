package com.example.quokka.goalTask;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goalTask.Adapter.ToDoAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class goalTask_mainpage extends AppCompatActivity implements ToDoAdapter.OnTaskClickListener {

    private RecyclerView recyclerViewToDo;
    private EditText editTextNewToDo;
    private Button buttonAddToDo;
    private List<String> toDoList;
    private ToDoAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_task_mainpage);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerViewToDo = findViewById(R.id.recyclerViewToDo);
        editTextNewToDo = findViewById(R.id.editTextNewToDo);
        buttonAddToDo = findViewById(R.id.buttonAddToDo);

        toDoList = new ArrayList<>();
        adapter = new ToDoAdapter(toDoList, this);
        recyclerViewToDo.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewToDo.setAdapter(adapter);

        loadTasksFromFirestore();

        buttonAddToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTask = editTextNewToDo.getText().toString().trim();
                if (!newTask.isEmpty()) {
                    addTaskToFirestore(newTask);
                    editTextNewToDo.setText("");
                }
            }
        });
    }

    private void loadTasksFromFirestore() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("todo_tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    toDoList.clear(); // Clear the existing list
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String task = documentSnapshot.getString("task");
                        toDoList.add(task);
                        adapter.notifyDataSetChanged(); // Notify adapter
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(goalTask_mainpage.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                });
    }

    private void addTaskToFirestore(String task) {
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("task", task);
        taskData.put("completed", false);

        db.collection("users")
                .document(userId)
                .collection("todo_tasks")
                .add(taskData)
                .addOnSuccessListener(documentReference -> {
                    toDoList.add(task);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(goalTask_mainpage.this, "Failed to add task", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onTaskClick(int position) {
        String task = toDoList.get(position);
        Intent intent = new Intent(goalTask_mainpage.this, TaskDetailActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }
}