package com.example.quokka.group;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.quokka.MainActivity;
import com.example.quokka.R;
import com.example.quokka.ui.login.Register;

public class group_main extends AppCompatActivity {

    ImageView back_btn;
    Button create_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_main); // Set the layout XML file
        // Initialize any views or components here
        back_btn = findViewById(R.id.img_back);

        create_group = findViewById(R.id.btn_create_group);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), group_create.class);
                startActivity(intent);
                finish();
            }
        });

    }
}