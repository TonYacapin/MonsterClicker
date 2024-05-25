package com.example.monterclicker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoadingActivity extends AppCompatActivity {

    private static int LOADING_TIME = 5000; // Adjust the loading time as needed
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        imageView = findViewById(R.id.imageView);

        // Display the intro picture for a few seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the next activity after the loading time
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            }
        }, LOADING_TIME);
    }
}