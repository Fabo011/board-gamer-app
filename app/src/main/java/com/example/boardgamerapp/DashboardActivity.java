package com.example.boardgamerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.boardgamerapp.database.Database;
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
    private Store store;
    private UserStory1NextMatchDay matchDay;
    private UserStory4PreVoting preVoting;
    private TextView cardTitle;
    private TextView cardDescription;
    private LinearLayout voteContainer;
    private LinearLayout voteGameContainer;
    private Fragment currentFragment;
    private FloatingActionButton sendMessage;
    private Spinner cuisineDropdown;
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
                currentEventId = eventId; // Save the event ID

                String groupName = store.getGroupName();

                // Erstellen von Layout-Parametern für den Spinner
                LinearLayout.LayoutParams layout_Params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, // Breite des Spinners
                        ViewGroup.LayoutParams.WRAP_CONTENT); // Höhe des Spinners

                // Anwenden der Layout-Parameter auf den Spinner
                cuisineDropdown.setLayoutParams(layout_Params);
                cuisineDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                        if (i > 0) {
                            String selectedItem = adapterView.getItemAtPosition(i).toString();
                            // Update the database with the selected cuisine and eventId
                            Database dbHelper = new Database();
                            dbHelper.updateCuisineSelection(groupName, eventId, selectedItem);
                            Snackbar.make(view , "Du hast " + selectedItem + " gewählt!", Snackbar.LENGTH_LONG).show();
                        }else {
                            Snackbar.make(view , "Bitte wähle dein Lieblingsessen aus!", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                                //Do nothing
                    }
                });


                // Laden des Button-Designs aus der XML-Datei
                Drawable buttonDesign = ContextCompat.getDrawable(DashboardActivity.this, R.drawable.button_design);

                for (Map<String, Object> game : gameVotes) {
                    String gameName = (String) game.get("game");
                    int votes = ((Long) game.get("votes")).intValue(); // Convert Long to int

                    // Abrufen des Abstandswerts aus den Ressourcen
                    Resources resources = getResources();
                    int margin = (int) resources.getDimension(R.dimen.button_margin);

                    // Erstellen von Layout-Parametern mit Abstand
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, margin, 0, margin); // Abstand oben und unten

                    Button gameButton = new Button(DashboardActivity.this);
                    gameButton.setBackground(buttonDesign);
                    gameButton.setTextColor(ContextCompat.getColor(DashboardActivity.this, R.color.white));
                    gameButton.setLayoutParams(layoutParams);
                    gameButton.setText(gameName + " (" + votes + " votes)");
                    gameButton.setOnClickListener(v -> voteForGame(eventId, gameName, gameButton));

                    voteGameContainer.addView(gameButton);
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
        cardTitle = findViewById(R.id.card_title);
        cardDescription = findViewById(R.id.card_description);
        voteContainer = findViewById(R.id.vote_container); // LinearLayout to hold vote buttons
        voteGameContainer = findViewById(R.id.vote_game_container);

        headline.setText("Dashboard");
        sendMessage = findViewById(R.id.fab);
        cuisineDropdown = findViewById(R.id.categorySpinner);


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
                cardTitle.setText("Der Nächste Spieleabend ist am: " + matchday);
                cardDescription.setText("Dein Gastgeber ist " + player);
                currentEventId = eventId; // Save the event ID
                displayGameVoteButtons(gameVotes, eventId);
            }

            @Override
            public void onFailure(String errorMessage) {
                cardTitle.setText(errorMessage);

            }
        });

        // Set up button click listeners
        sendMessage.setOnClickListener(v -> {
                    hideActivityContent();
                    loadFragment(new MessagingFragment());
                });

    }

    private void displayGameVoteButtons(List<Map<String, Object>> gameVotes, String eventId) {
        //voteGameContainer.removeAllViews(); // Clear previous buttons

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
