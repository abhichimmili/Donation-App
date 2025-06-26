package com.example.donation;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {

    ImageButton backToMainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about); // replace with your actual layout file
        Toolbar toolbar = findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);

        // Enable the back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About");
        }
        // Handle back arrow click
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Default behavior (also goes back)
    }
}
