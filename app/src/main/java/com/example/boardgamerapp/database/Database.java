package com.example.boardgamerapp.database;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    public void getGroupDocument(String groupName, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        db.collection(COLLECTION_NAME)
                .document(groupName)
                .get()
                .addOnCompleteListener(onCompleteListener);
    }

    public void addFieldToDocument(String groupname, String fieldName, Object fieldValue, Runnable onSuccess) {
        DocumentReference documentReference = db.collection(COLLECTION_NAME).document(groupname);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {

                Map<String, Object> updates = new HashMap<>();
                updates.put(fieldName, fieldValue);

                documentReference.update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Feld erfolgreich zum Dokument hinzugefügt");
                            onSuccess.run();
                            // Hier könnte zusätzlicher Code ausgeführt werden, wenn das Feld erfolgreich hinzugefügt wurde
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Fehler beim Hinzufügen des Feldes zum Dokument", e);
                            // Hier könnte Fehlerbehandlung implementiert werden
                        });
            }
            else {
                Log.e(TAG, "Dokument nicht gefunden");
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Fehler beim Abrufen des Dokuments", e);
        });
    }

    /**
     * 7. Add GameSuggestion
     */

    public void updateCuisineSelection(String groupName, String eventId, String selectedCuisine) {
        DocumentReference groupRef = db.collection(COLLECTION_NAME).document(groupName);

        groupRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> events = (List<Map<String, Object>>) documentSnapshot.get("events");

                // Iterate through events and find the correct event
                for (Map<String, Object> event : events) {
                    if (event.get("event_id").equals(eventId)) {
                        List<Map<String, Object>> cuisines = (List<Map<String, Object>>) event.get("cuisine");

                        // If the cuisine list is null, initialize it as an empty list
                        if (cuisines == null) {
                            cuisines = new ArrayList<>();
                        }

                        // Check if the selected cuisine already exists in the list
                        boolean cuisineFound = false;
                        for (Map<String, Object> cuisine : cuisines) {
                            if (cuisine.get("kind").equals(selectedCuisine)) {
                                // Cuisine exists, increment count
                                long currentCount = (long) cuisine.get("count");
                                cuisine.put("count", currentCount + 1);
                                cuisineFound = true;
                                break;
                            }
                        }

                        // If the cuisine does not exist, create a new entry
                        if (!cuisineFound) {
                            Map<String, Object> newCuisine = new HashMap<>();
                            newCuisine.put("kind", selectedCuisine);
                            newCuisine.put("count", 1); // Initialize with count 1
                            cuisines.add(newCuisine);
                        }

                        // Update the event with the new cuisine data
                        event.put("cuisine", cuisines);

                        // Update Firestore
                        groupRef.update("events", events)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Cuisine count updated successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating cuisine count", e));

                        break; // Exit loop after updating the event
                    }
                }
            } else {
                Log.e(TAG, "Group not found");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching group", e));
    }


    public void getEvents(String groupName, final OnDocumentReceivedListener listener) {
        db.collection(COLLECTION_NAME)
                .document(groupName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            listener.onDocumentReceived(document);
                        } else {
                            // Fehler beim Abrufen des Dokuments
                            listener.onError(task.getException());
                        }
                    }
                });
    }

    public void updateGameSuggestion(String groupName, String eventId, String game) {
        DocumentReference groupRef = db.collection(COLLECTION_NAME).document(groupName);

        groupRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> events = (List<Map<String, Object>>) documentSnapshot.get("events");

                // Iterate through events and find the correct event
                for (Map<String, Object> event : events) {
                    if (event.get("event_id").equals(eventId)) {
                        List<Map<String, Object>> games = (List<Map<String, Object>>) event.get("game_votes");


                         //Check if the selected game already exists in the list
                        boolean gameFound = false;
                        for (Map<String, Object> spiel : games) {
                            if (spiel.get("game").equals(game)) {
                                gameFound = true;
                                break;
                            }
                        }

                        // If the game does not exist, create a new entry

                            Map<String, Object> newGame = new HashMap<>();
                            newGame.put("game", game);
                            newGame.put("votes", 1); // Initialize with count 1
                            games.add(newGame);

                        // Update the event with the new gamevotes data
                        event.put("game_votes", games);

                        // Update Firestore
                        groupRef.update("events", events)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Games count updated successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating games count", e));

                        break; // Exit loop after updating the event
                    }
                }
            } else {
                Log.e(TAG, "Group not found");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching group", e));
    }

    /**
     * 8. Add NightVotes
     */

    public void updateNightVotes(String groupName, String eventId, String gastgeber, String essen,String allgemein,String kommentar,int sterne) {
        DocumentReference groupRef = db.collection(COLLECTION_NAME).document(groupName);

        groupRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> events = (List<Map<String, Object>>) documentSnapshot.get("events");

                // Iterate through events and find the correct event
                for (Map<String, Object> event : events) {
                    if (event.get("event_id").equals(eventId)) {
                        List<Map<String, Object>> nightVotes = (List<Map<String, Object>>) event.get("nightVotes");

                        // If the nightVotes list is null, initialize it as an empty list
                        if (nightVotes == null) {
                            nightVotes = new ArrayList<>();
                        }

                        // Check if the selected nightVote already exists in the list
                        boolean nightVoteFound = false;
                        for (Map<String, Object> nightVote : nightVotes) {
                            if (nightVote.get("gastgeber").equals(gastgeber)) {
                                nightVoteFound = true;
                                break;
                            }
                        }

                        // If the nightVote does not exist, create a new entry
                        if (!nightVoteFound) {
                            Map<String, Object> newNightVote = new HashMap<>();
                            newNightVote.put("gastgeber", gastgeber);
                            newNightVote.put("essen", essen);
                            newNightVote.put("abendallgemein", allgemein);
                            newNightVote.put("kommentar", kommentar);
                            newNightVote.put("starCount", sterne);
                            nightVotes.add(newNightVote);
                        }

                        // Update the event with the new cuisine data
                        event.put("nightVotes", nightVotes);

                        // Update Firestore
                        groupRef.update("events", events)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Cuisine count updated successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating cuisine count", e));


                        break; // Exit loop after updating the event
                    }
                }
            } else {
                Log.e(TAG, "Group not found");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching group", e));

    }

    public void getDateAndHost(String groupName, DateAndHostCallback callback) {
        DocumentReference groupRef = db.collection(COLLECTION_NAME).document(groupName);
        groupRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> events = (List<Map<String, Object>>) documentSnapshot.get("events");
                ArrayList<String> dateAndHost = new ArrayList<>();
                Date letztesDatum = null;
                Date heute = Calendar.getInstance().getTime();

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

                    for (Map<String, Object> event : events) {
                        String dateString = event.get("date").toString();
                        Date datum = sdf.parse(dateString);
                        Log.i(TAG, "onDocumentReceived: " + event.get("date"));

                        if (datum.before(heute)) {
                            if (letztesDatum == null || datum.after(letztesDatum)) {
                                letztesDatum = datum;
                                dateAndHost.add((String) event.get("host"));
                                dateAndHost.add((String) event.get("date"));


                            }
                        }
                    }
                    callback.onDateAndHostReceived(dateAndHost); // Callback aufrufen
                } catch (ParseException e) {
                    Log.e(TAG, "ParseException: " + e.getMessage());
                    callback.onError(e);
                }

            } else {
                Log.e(TAG, "Group not found");
                callback.onError(new Exception("Group not found"));
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching group", e);
            callback.onError(e);
        });
    }

    public void getNextDateAndHost(String groupName, DateAndHostCallback callback) {
        DocumentReference groupRef = db.collection(COLLECTION_NAME).document(groupName);
        groupRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> events = (List<Map<String, Object>>) documentSnapshot.get("events");
                ArrayList<String> dateAndHost = new ArrayList<>();
                Date naechstesDatum = null;
                Date heute = Calendar.getInstance().getTime();

                Date tommorow = null;

                Date today = Calendar.getInstance().getTime();

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

                    for (Map<String, Object> event : events) {
                        String dateString = event.get("date").toString();
                        Date datum = sdf.parse(dateString);
                        Log.i(TAG, "onDocumentReceived: " + event.get("date"));

                        if (datum.after(today)) {
                            if (tommorow == null || datum.before(tommorow)) {
                                tommorow = datum;
                                dateAndHost.add((String) event.get("host"));
                                dateAndHost.add((String) event.get("date"));
                            }
                        }
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "ParseException: " + e.getMessage());

                }
                callback.onDateAndHostReceived(dateAndHost); // Callback aufrufen

            } else {
                Log.e(TAG, "Group not found");
                callback.onError(new Exception("Group not found"));
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching group", e);
            callback.onError(e);
        });
    }



    public interface OnDocumentReceivedListener {
        void onDocumentReceived(DocumentSnapshot document);
        void onError(Exception e);
    }



    // Interface für den Callback
    public interface FirestoreCallback {
        void onSuccess(Long nextHostIndex);
        void onFailure(String errorMessage);
    }

    public interface DateAndHostCallback {
        void onDateAndHostReceived(ArrayList<String> dateAndHost);
        void onError(Exception e);
    }

}
