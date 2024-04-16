package com.example.quokka.goal_progress_tracking.task_templates;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.R;

import java.time.LocalDate;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> tasks;
    private OnTaskClickListener listener;

    public TaskAdapter(Context context, List<Task> tasks, OnTaskClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.taskNameTextView.setText(task.getTaskName());
        holder.taskDescriptionTextView.setText(task.getTaskDescription());
        holder.taskTargetTextView.setText(String.valueOf(task.getTarget()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task); // Notify listener on item click
            }
        });

        // Example: Display last entry (assuming dailyEntries is not empty)
        if (!task.getDailyEntries().isEmpty()) {
            Pair<LocalDate, Double> lastEntry = task.getDailyEntries().get(task.getDailyEntries().size() - 1);
            holder.lastEntryDateTextView.setText(lastEntry.first.toString());
            holder.lastEntryValueTextView.setText(String.valueOf(lastEntry.second));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskNameTextView;
        TextView taskDescriptionTextView;
        TextView taskTargetTextView;
        TextView lastEntryDateTextView;
        TextView lastEntryValueTextView;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.text_task_name);
            taskDescriptionTextView = itemView.findViewById(R.id.text_task_description);
            taskTargetTextView = itemView.findViewById(R.id.text_task_target);
            lastEntryDateTextView = itemView.findViewById(R.id.text_last_entry_date);
            lastEntryValueTextView = itemView.findViewById(R.id.text_last_entry_value);
        }
    }
}