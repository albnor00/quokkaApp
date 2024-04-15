package com.example.quokka.goal_progress_tracking.goal_page;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.R;

import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {
    private List<Goal> goals;
    private Context context;

    public GoalAdapter(Context context, List<Goal> goals) {
        this.goals = goals;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.goalNameTextView.setText(goal.getName());
        holder.goalDescriptionTextView.setText(goal.getDescription());

        // Set click listener for the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle item click here
                // For example, start a new activity to show goal details
                Intent intent = new Intent(context, GoalDetailsActivity.class);
                intent.putExtra("goal_name", goal.getName());
                intent.putExtra("goal_description", goal.getDescription());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView goalNameTextView;
        public TextView goalDescriptionTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            goalNameTextView = itemView.findViewById(R.id.goal_name);
            goalDescriptionTextView = itemView.findViewById(R.id.goal_description);
        }
    }
}
