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

    private ImageView cardImage;
    private TextView cardTitle;
    private TextView cardDescription;
    private Button voteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeUI(); // Initialize UI and fetch data
    }

    @Override
    protected void onResume() {
        super.onResume();

        initializeUI(); // Refresh UI when activity resumes
    }

    private void initializeUI() {
        // Initialize UI elements
        headline = findViewById(R.id.dashboard_headline);
        nextHost = findViewById(R.id.notice_text);
        createEventButton = findViewById(R.id.create_event_button);
        messagingButton = findViewById(R.id.messaging_button);
        cardImage = findViewById(R.id.card_image);
        cardTitle = findViewById(R.id.card_title);
        cardDescription = findViewById(R.id.card_description);
        voteButton = findViewById(R.id.voteButton);

        headline.setText("Dashboard");

        // Initialize Store
        store = new Store(this);
        String groupName = store.getGroupName();

        // Initialize UserStory instances
        userStory2RotateHost = new UserStory2RotateHost();
        matchDay = new UserStory1NextMatchDay();

        // Fetch match day details
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

        // Set vote button action
        voteButton.setOnClickListener(v -> {
            Snackbar snackbar = Snackbar.make(v, "Hier die Funktion für die Votes einfügen", Snackbar.LENGTH_LONG);
            snackbar.show();
        });

        // Fetch next host
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

        // Set up button click listeners
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddEventActivity.class);
            startActivity(intent);
        });

        messagingButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MessagingActivity.class);
            startActivity(intent);
        });
    }
}
