package com.example.quokka.goal_progress_tracking.average_task_template;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quokka.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AverageLogAdapter extends RecyclerView.Adapter<AverageLogAdapter.LogViewHolder> {
    private List<average_log> logList;
    private OnItemClickListener onItemClickListener;

    public AverageLogAdapter(List<average_log> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_previous_logs, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        average_log log = logList.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public class LogViewHolder extends RecyclerView.ViewHolder {
        private TextView logTextView;
        private TextView noteTextView;
        private TextView dateTextView;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logTextView = itemView.findViewById(R.id.textView_value);
            noteTextView = itemView.findViewById(R.id.textView_notes);
            dateTextView = itemView.findViewById(R.id.textView_date);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
        }

        public void bind(average_log log) {
            logTextView.setText(String.valueOf(log.getLog()));
            noteTextView.setText(log.getNote());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            dateTextView.setText(dateFormat.format(log.getDate()));
        }
    }
}