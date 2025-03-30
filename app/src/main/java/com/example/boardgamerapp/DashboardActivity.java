package com.example.boardgamerapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.boardgamerapp.library.GameVoteFragment;
import com.example.boardgamerapp.library.UserStory1NextMatchDay;
import com.example.boardgamerapp.library.UserStory4PreVoting;
import com.example.boardgamerapp.library.UserStory5NightVote;
import com.example.boardgamerapp.library.UserStoryOptional;
import com.example.boardgamerapp.store.Store;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private TextView headline;
    private TextView nextHost;
    //private Button createEventButton;
    //private Button messagingButton;
    private Store store;
    private UserStory1NextMatchDay matchDay;
    private UserStory4PreVoting preVoting;

    private ImageView cardImage;
    private TextView cardTitle;
    private TextView cardDescription;
    private LinearLayout voteContainer;

    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;
    private Fragment currentFragment;
    private FloatingActionButton sendMessage;


    private String currentEventId; // Store current event ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        store = new Store(this);
        initializeUI();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                showActivityContent();
                return true;
            } else if (item.getItemId() == R.id.navigation_dashboard) {
                hideActivityContent();
                loadFragment(new GameVoteFragment());
                return true;
            } else if (item.getItemId() == R.id.navigation_date) {
                hideActivityContent();
                loadFragment(new AddEventFragment());
                return true;
            } else if (item.getItemId() == R.id.navigation_notifications) {
                hideActivityContent();
                loadFragment(new UserStory5NightVote());
                return true;
            }
            return false;
        });

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
        //createEventButton = findViewById(R.id.create_event_button);
        //messagingButton = findViewById(R.id.messaging_button);
        cardImage = findViewById(R.id.card_image);
        cardTitle = findViewById(R.id.card_title);
        cardDescription = findViewById(R.id.card_description);
        voteContainer = findViewById(R.id.vote_container); // LinearLayout to hold vote buttons
        fragmentContainer = findViewById(R.id.fragment_container);
        headline.setText("Dashboard");
        sendMessage = findViewById(R.id.fab);


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

        // Set up button click listeners
        //createEventButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AddEventActivity.class)));
        //messagingButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MessagingActivity.class)));
        sendMessage.setOnClickListener(v -> {
                    hideActivityContent();
                    loadFragment(new MessagingFragment());
                });


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

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment) // Ersetze fragment_container mit der ID deines Containers
                    .commit();
            currentFragment = fragment;
        }
    }

    private void showActivityContent() {
        if (currentFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(currentFragment).commit();
            currentFragment = null;
        }
        findViewById(R.id.activity_content).setVisibility(View.VISIBLE);
    }

    private void hideActivityContent() {
        findViewById(R.id.activity_content).setVisibility(View.GONE);
    }
}
