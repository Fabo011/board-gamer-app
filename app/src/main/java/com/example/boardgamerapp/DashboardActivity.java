package com.example.boardgamerapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Optionally, you can manipulate UI elements if needed
        TextView headline = findViewById(R.id.dashboard_headline);
        headline.setText("Dashboard");
    }
}
