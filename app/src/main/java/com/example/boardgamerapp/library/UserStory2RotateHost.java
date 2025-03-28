package com.example.boardgamerapp.library;

import android.content.Context;

import com.example.boardgamerapp.R;
import com.example.boardgamerapp.messaging.MessagingService;
import com.example.boardgamerapp.store.Store;
import com.example.boardgamerapp.database.Database;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;

public class UserStory2RotateHost {

    final Database database;
    private MessagingService messagingService;
    private Store store;
    private Context context;

    // Constructor accepts Context to initialize Store and MessagingService

    public UserStory2RotateHost(Context context) {
        this.context = context;
        database = new Database();
        messagingService = new MessagingService();
        store = new Store(context);
    }

    public void createEventAndUpdateHost(Context context, String groupName, String eventId, String location, String date, String hostName, List<Map<String, Object>> gameVotes) {
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

                // Get the name of the new host
                String newHost = players.get((int) newNextHostIndex).get("name");
                database.updateNextHostIndex(groupName, newNextHostIndex);

                // Send a notification that the next host is set
                messagingService.sendFCMMessage(
                        context,
                        groupName,
                        newHost + " " + context.getString(R.string.next_host_text) + " " + newHost + " > " + context.getString(R.string.next_host_text_2),
                        true
                );
            }
        });
    }

    // Optional UserStory
    public void reminderTeamCuisine(Context context, String groupName) {
        messagingService.sendFCMMessage(
                context,
                groupName,
                context.getString(R.string.cuisine_reminder_1) + " " + groupName + ". " + context.getString(R.string.cuisine_reminder),
                true
        );
    }
}
