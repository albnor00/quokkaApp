package com.example.quokka.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.group.group_member_page;
import com.example.quokka.template.templateMain;

public class tasksMain extends AppCompatActivity {

AppCompatButton balanceWheel, goalTracking, back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_main);

        back = findViewById(R.id.back);
        balanceWheel = findViewById(R.id.balanceWheelTask);
        goalTracking = findViewById(R.id.goalTracking);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        balanceWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), balance_wheel.class);
                startActivity(intent);
                finish();
            }
        });

        goalTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), templateMain.class);
                startActivity(intent);
                finish();
            }
        });
    }

}