package com.example.boardgamerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
<<<<<<< Updated upstream
import android.widget.Toast;

=======
>>>>>>> Stashed changes
import androidx.appcompat.app.AppCompatActivity;
import com.example.boardgamerapp.library.UserStory1NextMatchDay;
<<<<<<< Updated upstream
import com.example.boardgamerapp.library.UserStory4PreVoting;
import com.example.boardgamerapp.library.UserStoryOptional;
=======
import com.example.boardgamerapp.library.UserStory2RotateHost;
import com.example.boardgamerapp.library.UserStory3AddGameVotes;
>>>>>>> Stashed changes
import com.example.boardgamerapp.store.Store;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

<<<<<<< Updated upstream
import java.util.List;
import java.util.Map;
=======
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
    private String currentEventId; // Store current event ID
=======
    Button voteButton;
    BottomNavigationView bottomNav;
>>>>>>> Stashed changes

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
                store.saveCurrentEventId(eventId);
                cardTitle.setText("Next Game Night: " + matchday);
                cardDescription.setText("Host: " + host);

                currentEventId = eventId; // Save the event ID

                voteContainer.removeAllViews(); // Clear old buttons

                String groupName = store.getGroupName();
                Spinner cuisineDropdown = UserStoryOptional.createCuisineDropdown(DashboardActivity.this, groupName, eventId);
                voteContainer.addView(cuisineDropdown);

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
        headline = findViewById(R.id.dashboard_headline);
        createEventButton = findViewById(R.id.create_event_button);
        messagingButton = findViewById(R.id.messaging_button);
        cardImage = findViewById(R.id.card_image);
        cardTitle = findViewById(R.id.card_title);
        cardDescription = findViewById(R.id.card_description);
<<<<<<< Updated upstream
        voteContainer = findViewById(R.id.vote_container); // LinearLayout to hold vote buttons

        headline.setText("Dashboard");

        // Initialize Store
=======
        voteButton = findViewById(R.id.voteButton);
        bottomNav = findViewById(R.id.bottom_navigation);
        headline.setText("Dashboard");




        // Initialize Store to retrieve group name from SharedPreferences
>>>>>>> Stashed changes
        store = new Store(this);
        String groupName = store.getGroupName();

        // Initialize UserStory instances
        matchDay = new UserStory1NextMatchDay(this);
        preVoting = new UserStory4PreVoting(this);

<<<<<<< Updated upstream
        // Fetch match day details & game voting
        matchDay.getCurrentMatchday(groupName, new UserStory1NextMatchDay.getCurrentMatchdayCallback() {
=======
        //Initialize UserStory1Matchday
        matchDay = new UserStory1NextMatchDay();





        // Alle Spielvorschläge abrufen und in Logcat ausgeben
        UserStory3AddGameVotes story3 = new UserStory3AddGameVotes();
        story3.getEvents(groupName);



        //Get player Name and Matchdate from current event
        /*matchDay.getCurrentMatchday(groupName, new UserStory1NextMatchDay.getCurrentMatchdayCallback() {
>>>>>>> Stashed changes
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
        });*/

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

        // Add the cuisine dropdown
        String groupName = store.getGroupName();
        Spinner cuisineDropdown = UserStoryOptional.createCuisineDropdown(this, groupName, eventId);
        voteContainer.addView(cuisineDropdown);

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

        // Call UserStory4PreVoting to update the vote count
        preVoting.voteForGame(eventId, gameName, updatedVotes -> {
            // Update the button text with the updated vote count
            String updatedText = gameName + " (" + updatedVotes + " votes)";
            gameButton.setText(updatedText);

            Snackbar.make(voteContainer, "Vote submitted for " + gameName, Snackbar.LENGTH_LONG).show();
        });
    }




}
