package com.example.huntersgeocard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            super.onCreate(savedInstanceState);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_menu);
        Intent myIntentMap = new Intent(MainActivity.this, MapActivity.class);
        Intent myIntent_about = new Intent(MainActivity.this, AboutApp.class);
        Button button_exit = (Button)findViewById(R.id.button5);
        Button button_map = (Button)findViewById(R.id.button);
        Button button_aboutApp = (Button)findViewById(R.id.button4);
        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(myIntentMap);
            }
        });
        button_aboutApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(myIntent_about);
            }
        });
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });
    }
}