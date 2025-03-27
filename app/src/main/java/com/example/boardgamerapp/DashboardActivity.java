package com.example.boardgamerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boardgamerapp.library.UserStory1NextMatchDay;
import com.example.boardgamerapp.library.UserStory2RotateHost;
import com.example.boardgamerapp.library.UserStory4PreVoting;
import com.example.boardgamerapp.store.Store;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private TextView headline;
    private TextView nextHost;
    private Button createEventButton;
    private Button messagingButton;
    private Store store;
    private UserStory1NextMatchDay matchDay;
    private UserStory4PreVoting preVoting;

    private ImageView cardImage;
    private TextView cardTitle;
    private TextView cardDescription;
    private LinearLayout voteContainer;

    private String currentEventId; // Store current event ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        store = new Store(this);
        initializeUI();

        String groupName = store.getGroupName();
        if (groupName == null || groupName.isEmpty()) {
            Log.e(TAG, "Group name is null or empty!");
            return;
        }

        matchDay = new UserStory1NextMatchDay(this);

        matchDay.getCurrentMatchday(groupName, new UserStory1NextMatchDay.getCurrentMatchdayCallback() {
            @Override
            public void onSuccess(String host, String matchday, String eventId, List<Map<String, Object>> gameVotes) {
                cardTitle.setText("Next Game Night: " + matchday);
                cardDescription.setText("Host: " + host);

                voteContainer.removeAllViews(); // Clear old buttons
                for (Map<String, Object> game : gameVotes) {
                    String gameName = (String) game.get("game");
                    int votes = ((Long) game.get("votes")).intValue(); // Convert Long to int

                    Button gameButton = new Button(DashboardActivity.this);
                    gameButton.setText(gameName + " (" + votes + " votes)");
                    gameButton.setOnClickListener(v -> voteForGame(eventId, gameName, gameButton));

                    voteContainer.addView(gameButton);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                cardTitle.setText(errorMessage);
            }
        });
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
        voteContainer = findViewById(R.id.vote_container); // LinearLayout to hold vote buttons

        headline.setText("Dashboard");

        // Initialize Store
        store = new Store(this);
        String groupName = store.getGroupName();

        // Initialize UserStory instances
        matchDay = new UserStory1NextMatchDay(this);
        preVoting = new UserStory4PreVoting(this);

        // Fetch match day details & game voting
        matchDay.getCurrentMatchday(groupName, new UserStory1NextMatchDay.getCurrentMatchdayCallback() {
            @Override
            public void onSuccess(String player, String matchday, String eventId, List<Map<String, Object>> gameVotes) {
                cardImage.setImageResource(R.drawable.gamenight);
                cardTitle.setText("Der Nächste Spieleabend ist am: " + matchday);
                cardDescription.setText("Dein Gastgeber ist " + player);

                currentEventId = eventId; // Save the event ID

                displayGameVoteButtons(gameVotes, eventId);
            }

            @Override
            public void onFailure(String errorMessage) {
                cardTitle.setText(errorMessage);
                voteContainer.removeAllViews(); // Clear buttons if no event found
            }
        });

        // Fetch next host
        new UserStory2RotateHost().fetchNextHost(groupName, new UserStory2RotateHost.OnNextHostFetched() {
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
        createEventButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AddEventActivity.class)));
        messagingButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MessagingActivity.class)));
    }

    private void displayGameVoteButtons(List<Map<String, Object>> gameVotes, String eventId) {
        voteContainer.removeAllViews(); // Clear previous buttons

        if (gameVotes == null || gameVotes.isEmpty()) {
            Toast.makeText(this, "No games found for voting!", Toast.LENGTH_LONG).show();
            return;
        }

        for (Map<String, Object> game : gameVotes) {
            String gameName = (String) game.get("game");
            long votes = (long) game.get("votes");

            Button gameButton = new Button(this);
            gameButton.setText(gameName + " (" + votes + " votes)");

            // Pass the button reference to the voteForGame method
            gameButton.setOnClickListener(v -> voteForGame(eventId, gameName, gameButton));

            voteContainer.addView(gameButton);
        }
    }


    private void voteForGame(String eventId, String gameName, Button gameButton) {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current votes and increment by 1
        String buttonText = gameButton.getText().toString();
        int currentVotes = Integer.parseInt(buttonText.substring(buttonText.indexOf('(') + 1, buttonText.indexOf(" votes")));
        int updatedVotes = currentVotes + 1;

        // Immediately update the button text to show new vote count
        gameButton.setText(gameName + " (" + updatedVotes + " votes)");

        // Call the backend to save the vote
        preVoting.voteForGame(eventId, gameName);

        Snackbar.make(voteContainer, "Vote submitted for " + gameName, Snackbar.LENGTH_LONG).show();
    }

}
