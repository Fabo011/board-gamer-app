package com.example.boardgamerapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.boardgamerapp.library.UserStory2RotateHost;

public class DashboardActivity extends AppCompatActivity {

    private TextView headline;
    private TextView nextHost;
    private UserStory2RotateHost userStory2RotateHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        headline = findViewById(R.id.dashboard_headline);
        nextHost = findViewById(R.id.notice_text);
        headline.setText("Dashboard");

        // Initialize UserStory2RotateHost
        userStory2RotateHost = new UserStory2RotateHost();

        // Fetch the next host's name
        String groupName = "FridayGamer";  // Replace with actual group name
        userStory2RotateHost.fetchNextHost(groupName, new UserStory2RotateHost.OnNextHostFetched() {
            @Override
            public void onNextHostFetched(String playerName) {
                if (playerName != null) {
                    // Display the name in the TextView
                    nextHost.setText(playerName + " is the next host. " + playerName + " please create the next event.");
                } else {
                    nextHost.setText("Failed to load next host.");
                }
            }
        });
    }
}