package com.example.boardgamerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boardgamerapp.library.UserStory2RotateHost;
import com.example.boardgamerapp.store.Store;

public class DashboardActivity extends AppCompatActivity {

    private TextView headline;
    private TextView nextHost;
    private Button createEventButton;
    private Button messagingButton;
    private UserStory2RotateHost userStory2RotateHost;
    private Store store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI elements
        headline = findViewById(R.id.dashboard_headline);
        nextHost = findViewById(R.id.notice_text);
        createEventButton = findViewById(R.id.create_event_button);
        messagingButton = findViewById(R.id.messaging_button); // New button

        headline.setText("Dashboard");

        // Initialize Store to retrieve group name from SharedPreferences
        store = new Store(this);
        String groupName = store.getGroupName();

        // Initialize UserStory2RotateHost
        userStory2RotateHost = new UserStory2RotateHost();

        // Fetch the next host's name dynamically using the group name
        userStory2RotateHost.fetchNextHost(groupName, new UserStory2RotateHost.OnNextHostFetched() {
            @Override
            public void onNextHostFetched(String playerName) {
                if (playerName != null) {
                    nextHost.setText(playerName + " is the next host. " + playerName + " please create the next event.");
                } else {
                    nextHost.setText("Failed to load next host.");
                }
            }
        });

        // Navigate to AddEventActivity
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddEventActivity.class);
            startActivity(intent);
        });

        // Navigate to MessagingActivity
        messagingButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MessagingActivity.class);
            startActivity(intent);
        });
    }
}

