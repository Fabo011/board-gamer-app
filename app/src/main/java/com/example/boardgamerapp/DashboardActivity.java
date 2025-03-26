package com.example.boardgamerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boardgamerapp.library.UserStory1NextMatchDay;
import com.example.boardgamerapp.library.UserStory2RotateHost;
import com.example.boardgamerapp.store.Store;
import com.google.android.material.snackbar.Snackbar;

public class DashboardActivity extends AppCompatActivity {

    private TextView headline;
    private TextView nextHost;
    private Button createEventButton;
    private Button messagingButton;
    private UserStory2RotateHost userStory2RotateHost;
    private Store store;

    private UserStory1NextMatchDay matchDay;

    ImageView cardImage;
    TextView cardTitle;
    TextView cardDescription;

    Button voteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI elements
        headline = findViewById(R.id.dashboard_headline);
        nextHost = findViewById(R.id.notice_text);
        createEventButton = findViewById(R.id.create_event_button);
        messagingButton = findViewById(R.id.messaging_button); // New button
        createEventButton = findViewById(R.id.create_event_button); // Add the button
        cardImage = findViewById(R.id.card_image);
        cardTitle = findViewById(R.id.card_title);
        cardDescription = findViewById(R.id.card_description);
        voteButton = findViewById(R.id.voteButton);

        headline.setText("Dashboard");

        // Initialize Store to retrieve group name from SharedPreferences
        store = new Store(this);
        String groupName = store.getGroupName();

        // Initialize UserStory2RotateHost
        userStory2RotateHost = new UserStory2RotateHost();

        //Initialize UserStory1Matchday
        matchDay = new UserStory1NextMatchDay();

        //Get player Name and Matchdate from current event
        matchDay.getCurrentMatchday(groupName, new UserStory1NextMatchDay.getCurrentMatchdayCallback() {
            @Override
            public void onSuccess(String player, String matchday) {
                cardImage.setImageResource(R.drawable.gamenight);
                cardTitle.setText("Der Nächste Spieleabend ist am: " + matchday);
                cardDescription.setText("Dein Gastgeber ist " + player);
            }
            @Override
            public void onFailure(String errorMessage) {
            cardTitle.setText(errorMessage);
            }
        });

        voteButton.setOnClickListener(v -> {
            Snackbar snackbar = Snackbar.make(v, "Hier die Funktion für die Votes einfügen", Snackbar.LENGTH_LONG);
            snackbar.show();
        });

        // Fetch the next host's name dynamically using the group name
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

        // Set up button click listener to navigate to AddEventActivity
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

