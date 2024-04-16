package com.example.quokka.goalTask.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quokka.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import android.widget.ImageView;
import java.util.HashMap;
import java.util.Map;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<String> toDoList;
    private OnTaskClickListener listener;
    private FirebaseFirestore db;

    public ToDoAdapter(List<String> toDoList, OnTaskClickListener listener) {
        this.toDoList = toDoList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_todo_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String task = toDoList.get(position);
        holder.textViewTask.setText(task);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(position); // Notify the listener that an item was clicked
            }
        });

        // Load completion status from Firestore
        loadCompletionStatus(task, holder);
    }

    private void loadCompletionStatus(String task, ViewHolder holder) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("todo_tasks")
                .whereEqualTo("task", task)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        Boolean isCompleted = documentSnapshot.getBoolean("completed");
                        if (isCompleted != null) {
                            setCompletionStatus(task, isCompleted, holder);
                        } else {
                            // Handle case where 'completed' field is missing or null
                            setCompletionStatus(task, false, holder); // Assuming not completed by default
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to load completion status
                    setCompletionStatus(task, false, holder); // Assuming not completed on failure
                });
    }

    private void setCompletionStatus(String task, boolean isCompleted, ViewHolder holder) {
        if (isCompleted) {
            holder.checkImageView.setImageResource(R.drawable.circle_check_green_btn);
            holder.checkImageView.setEnabled(false); // Disable further clicks
        } else {
            holder.checkImageView.setImageResource(R.drawable.circle_check_btn);
            holder.checkImageView.setEnabled(true); // Enable clicks
        }
    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTask;
        ImageView checkImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTask = itemView.findViewById(R.id.textViewTitle);
            checkImageView = itemView.findViewById(R.id.circleWithCheck);
        }
    }

    // Interface for item click listener
    public interface OnTaskClickListener {
        void onTaskClick(int position);
    }
}