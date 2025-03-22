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

    public void createEventAndUpdateHost(String groupName, String eventId, String location, String date, String hostName, List<Map<String, Object>> gameVotes) {
        // Create the new event based on the provided schema
        Map<String, Object> newEvent = Map.of(
                "event_id", eventId,
                "event_status", "created", // default status is 'created'
                "date", date,
                "location", location,
                "host", hostName,
                "game_votes", gameVotes
        );

        // Add the event to the group
        database.addEventToGroup(groupName, newEvent);

        // Fetch the group to update the next_host_index
        database.fetchGroup(groupName, task -> {
            DocumentSnapshot documentSnapshot = task.getResult();

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Retrieve the current next_host_index and increment it
                long nextHostIndex = documentSnapshot.getLong("next_host_index");
                long newNextHostIndex = nextHostIndex + 1;

                // Get the total number of players to ensure we don't exceed the list size
                List<Map<String, String>> players = (List<Map<String, String>>) documentSnapshot.get("players");
                if (newNextHostIndex >= players.size()) {
                    newNextHostIndex = 0; // Reset to 0 if it exceeds the number of players
                }

                // Update the next_host_index
                database.updateNextHostIndex(groupName, newNextHostIndex);
            }
        });
    }

    // Callback interface to pass the player name
    public interface OnNextHostFetched {
        void onNextHostFetched(String playerName);
    }
}
