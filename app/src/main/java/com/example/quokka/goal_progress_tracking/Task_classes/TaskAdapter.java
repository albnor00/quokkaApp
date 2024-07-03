package com.example.quokka.goal_progress_tracking.Task_classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.R;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TASK = 0;
    private static final int TYPE_TASK2 = 1;
    private static final int TYPE_TASK3 = 2;

    private List<TaskItem> tasks;
    private OnTaskClickListener listener;

    public TaskAdapter(List<TaskItem> tasks) {
        this.tasks = tasks;
    }

    public interface OnTaskClickListener {
        void onTaskClick(int position);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (tasks.get(position) instanceof Task) {
            return TYPE_TASK;
        } else if (tasks.get(position) instanceof Task2) {
            return TYPE_TASK2;
        } else if (tasks.get(position) instanceof Task3)
            return TYPE_TASK3;
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TASK) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        } else if (viewType == TYPE_TASK2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_target_task, parent, false);
            return new Task2ViewHolder(view);
        } else if (viewType == TYPE_TASK3) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit_task, parent, false);
            return new Task3ViewHolder(view);
        }
        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TaskViewHolder) {
            Task task = (Task) tasks.get(position);
            ((TaskViewHolder) holder).bind(task);

        } else if (holder instanceof Task2ViewHolder) {
            Task2 task2 = (Task2) tasks.get(position);
            ((Task2ViewHolder) holder).bind(task2);

        } else if (holder instanceof Task3ViewHolder) {
            Task3 task3 = (Task3) tasks.get(position);
            ((Task3ViewHolder) holder).bind(task3);

        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(clickedPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView taskNameTextView;
        private TextView taskTodaysValue;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.task_name);
            taskTodaysValue = itemView.findViewById(R.id.todays_value);
        }

        public void bind(Task task) {
            taskNameTextView.setText(task.getName());
            taskTodaysValue.setText(task.getGoal());
        }
    }

    static class Task2ViewHolder extends RecyclerView.ViewHolder {
        private TextView taskNameTextView;
        private TextView taskTodaysValue;

        public Task2ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.task_name);
            taskTodaysValue = itemView.findViewById(R.id.todays_value);
        }

        public void bind(Task2 task2) {

            taskNameTextView.setText(task2.getName());
            taskTodaysValue.setText(task2.getEndGoal());
        }
    }

    static class Task3ViewHolder extends RecyclerView.ViewHolder {
        private TextView taskNameTextView;
        private TextView taskTodaysValue;

        public Task3ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.textView);
            taskTodaysValue = itemView.findViewById(R.id.todays_value);
        }

        public void bind(Task3 task3) {
            taskNameTextView.setText(task3.getName());
            taskTodaysValue.setText(task3.getGoal());
        }
    }
}
