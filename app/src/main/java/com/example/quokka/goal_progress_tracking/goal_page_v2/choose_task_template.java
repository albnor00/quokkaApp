package com.example.quokka.goal_progress_tracking.goal_page_v2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.goal_progress_tracking.average_task_template.average_task_settings_page;
import com.example.quokka.goal_progress_tracking.average_task_template.create_new_average_task;
import com.example.quokka.goal_progress_tracking.average_task_template.edit_existing_average_log;
import com.example.quokka.goal_progress_tracking.habit_task_template.create_new_habit_task;
import com.example.quokka.goal_progress_tracking.target_task_template.create_new_target_task;
import com.example.quokka.tasks.profile.ProfileActivity;
import com.example.quokka.tasks.tasksMain;
import com.example.quokka.ui.login.Login;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


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
                    MainActivity.checkUserRole2(user, choose_task_template.this);
                } else if (item.getItemId() == R.id.logout_bottom) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                return true; // Return true to indicate that the item selection has been handled
            }
        });

        ImageView tooltip = findViewById(R.id.img_help);
        tooltip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(choose_task_template.this);
                builder.setTitle("Template Description");
                builder.setMessage("This is where you can choose a template for your tasks. The Average template presents your progress in a bar chart and it enables you to observe your average over a extended period of time. " +
                        "Target template enables you to pick a start value and a goal and your progress towards that goal is presented in a line chart. The Habit template enables you to track the progress you have made with a specific habit via a calender.");
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

    }

}
