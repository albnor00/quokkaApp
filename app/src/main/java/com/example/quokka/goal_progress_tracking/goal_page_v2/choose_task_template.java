package com.example.quokka.goal_progress_tracking.goal_page_v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.average_task_template.create_new_average_task;
import com.example.quokka.goal_progress_tracking.habit_task_template.create_new_habit_task;
import com.example.quokka.goal_progress_tracking.target_task_template.create_new_target_task;
import com.example.quokka.template.habit;

public class  choose_task_template extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_task_template);

        ImageView back_btn = findViewById(R.id.img_back);
        CardView averageTask = findViewById(R.id.average_task);
        CardView targetTask = findViewById(R.id.target_task);
        CardView habitTask = findViewById(R.id.habit_task);


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Goal_non_empty_page.class);
                startActivity(intent);
                finish();
            }
        });

        averageTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), create_new_average_task.class);
                startActivity(intent);
                finish();
            }
        });

        targetTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), create_new_target_task.class);
                startActivity(intent);
                finish();
            }
        });

        habitTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), create_new_habit_task.class);
                startActivity(intent);
                finish();
            }
        });

    }

}
