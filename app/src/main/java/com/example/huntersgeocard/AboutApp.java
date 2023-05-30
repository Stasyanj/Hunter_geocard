package com.example.huntersgeocard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AboutApp extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    public void onBackPressed(){
        Intent exit_intent = new Intent(AboutApp.this, MainActivity.class);
        AboutApp.this.startActivity(exit_intent);
    }
}
