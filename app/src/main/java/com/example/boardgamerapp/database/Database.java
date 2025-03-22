package com.example.boardgamerapp.database;

import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static final String TAG = "Database";
    private static final String COLLECTION_NAME = "groups"; // Firestore collection
    private FirebaseFirestore db;

    public Database() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * 1. Create a new gaming group in Firestore
     */
    public void createGroup(String groupName, String groupPassword, String playerName, String fcmToken) {
        Map<String, Object> group = new HashMap<>();
        group.put("groupname", groupName);
        group.put("grouppassword", groupPassword);
        group.put("created_at", FieldValue.serverTimestamp());
        group.put("players", List.of(Map.of("name", playerName, "fcm_token", fcmToken))); // First player
        group.put("next_host_index", 0);
        group.put("events", List.of()); // Empty list of events

        db.collection(COLLECTION_NAME)
                .document(groupName) // Group name as document ID
                .set(group)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Group successfully created"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating group", e));
    }

    /**
     * 2. Fetch group data from Firestore
     */
    public void fetchGroup(String groupName, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        db.collection(COLLECTION_NAME)
                .document(groupName)
                .get()
                .addOnCompleteListener(onCompleteListener);
    }

    /**
     * 3. Add player to group
     */
    public void addPlayerToGroup(String groupName, String playerName, String fcmToken) {
        DocumentReference groupRef = db.collection(COLLECTION_NAME).document(groupName);

        groupRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, String>> players = (List<Map<String, String>>) documentSnapshot.get("players");

                // Add the new player
                Map<String, String> newPlayer = Map.of("name", playerName, "fcm_token", fcmToken);
                players.add(newPlayer);

                // Update Firestore
                groupRef.update("players", players)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Player added successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error adding player", e));
            } else {
                Log.e(TAG, "Group not found");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching group", e));
    }

    /**
     * 4. Add an event to the group's events list
     */
    public void addEventToGroup(String groupName, Map<String, Object> event) {
        DocumentReference groupRef = db.collection(COLLECTION_NAME).document(groupName);

        groupRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> events = (List<Map<String, Object>>) documentSnapshot.get("events");
                events.add(event);

                groupRef.update("events", events)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Event successfully added"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error adding event", e));
            } else {
                Log.e(TAG, "Group not found");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching group", e));
    }

    /**
     * 5. Update the next_host_index of the group
     */
    public void updateNextHostIndex(String groupName, long newNextHostIndex) {
        DocumentReference groupRef = db.collection(COLLECTION_NAME).document(groupName);

        groupRef.update("next_host_index", newNextHostIndex)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Next host index updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating next host index", e));
    }

    /**
     * 6. Update game_votes votes
     */
    public void updateEventGameVotes(String groupName, String eventId, List<Map<String, Object>> updatedEvents, Runnable onSuccess) {
        db.collection(COLLECTION_NAME)
                .document(groupName)
                .update("events", updatedEvents)
                .addOnSuccessListener(aVoid -> onSuccess.run());
    }

}


