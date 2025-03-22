package com.example.boardgamerapp.library;

import com.example.boardgamerapp.database.Database;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;

public class UserStory2RotateHost {

    final Database database;

    public UserStory2RotateHost() {
        database = new Database();  // Initialize the database class
    }

    public void fetchNextHost(String groupName, OnNextHostFetched callback) {
        database.fetchGroup(groupName, task -> {
            DocumentSnapshot documentSnapshot = task.getResult();

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Retrieve the next_host_index
                long nextHostIndex = documentSnapshot.getLong("next_host_index");
                // Get the list of players
                List<Map<String, String>> players = (List<Map<String, String>>) documentSnapshot.get("players");

                // Ensure the index is within bounds of the players list
                if (nextHostIndex >= 0 && nextHostIndex < players.size()) {
                    // Get the player name at the next_host_index
                    String playerName = players.get((int) nextHostIndex).get("name");
                    // Return the player name via the callback
                    callback.onNextHostFetched(playerName);
                } else {
                    callback.onNextHostFetched(null);
                }
            } else {
                callback.onNextHostFetched(null);
            }
        });
    }

    // Callback interface to pass the player name
    public interface OnNextHostFetched {
        void onNextHostFetched(String playerName);
    }
}


