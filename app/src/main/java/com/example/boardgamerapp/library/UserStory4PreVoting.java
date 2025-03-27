package com.example.boardgamerapp.library;

import android.content.Context;
import android.util.Log;
import com.example.boardgamerapp.database.Database;
import com.example.boardgamerapp.store.Store;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;

public class UserStory4PreVoting {
    private static final String TAG = "UserStory4PreVoting";
    private Database database;
    private Store store;

    public UserStory4PreVoting(Context context) {
        this.database = new Database();
        this.store = new Store(context);
    }

    public void voteForGame(String eventId, String gameName, VoteCallback callback) {
        String groupName = store.getGroupName();
        if (groupName == null || groupName.isEmpty()) {
            Log.e(TAG, "Group name is null or empty. Cannot proceed with voting.");
            return;
        }

        database.fetchGroup(groupName, task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Failed to fetch group data", task.getException());
                return;
            }

            DocumentSnapshot documentSnapshot = task.getResult();
            if (documentSnapshot == null || !documentSnapshot.exists()) {
                Log.e(TAG, "Group document does not exist");
                return;
            }

            List<Map<String, Object>> events = (List<Map<String, Object>>) documentSnapshot.get("events");
            if (events == null || events.isEmpty()) {
                Log.e(TAG, "No events found in group");
                return;
            }

            updateEventVotes(groupName, eventId, events, gameName, callback);
        });
    }

    private void updateEventVotes(String groupName, String eventId, List<Map<String, Object>> events, String gameName, VoteCallback callback) {
        for (Map<String, Object> event : events) {
            if (event.get("event_id").equals(eventId)) {
                List<Map<String, Object>> gameVotes = (List<Map<String, Object>>) event.get("game_votes");
                if (gameVotes == null) {
                    Log.e(TAG, "No game_votes found in event: " + eventId);
                    return;
                }

                updateGameVotes(groupName, eventId, events, gameVotes, gameName, callback);
                return;
            }
        }
        Log.e(TAG, "Event not found with ID: " + eventId);
    }

    private void updateGameVotes(String groupName, String eventId, List<Map<String, Object>> events, List<Map<String, Object>> gameVotes, String gameName, VoteCallback callback) {
        for (Map<String, Object> game : gameVotes) {
            if (game.get("game").equals(gameName)) {
                long currentVotes = (long) game.get("votes");
                long updatedVotes = currentVotes + 1;

                game.put("votes", updatedVotes);

                // Save updated votes to the database
                database.updateEventGameVotes(groupName, eventId, events, () -> {
                    Log.d(TAG, "Vote added successfully for game: " + gameName);
                    if (callback != null) {
                        callback.onVoteUpdated(updatedVotes);
                    }
                });
                return;
            }
        }
        Log.e(TAG, "Game not found in game_votes for event: " + eventId);
    }

    // Callback interface to pass back the updated vote count
    public interface VoteCallback {
        void onVoteUpdated(long updatedVotes);
    }
}