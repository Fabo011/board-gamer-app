package com.example.boardgamerapp.library;

import static android.content.ContentValues.TAG;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class UserStory1NextMatchDay {
    private FirebaseFirestore database;

    public UserStory1NextMatchDay() {
        database = FirebaseFirestore.getInstance();
    }

    public void getCurrentMatchday(String groupName, final getCurrentMatchdayCallback callback) {
        DocumentReference docRef = database.collection("groups").document(groupName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                Log.i(TAG, "onComplete: ");
                if (task.isSuccessful()) {
                    Log.i(TAG, "onComplete: task success");
                    DocumentSnapshot document = task.getResult();
                    Log.i(TAG, "onComplete: task success " + document.getId());

                    if (document.exists()) {
                        Log.i(TAG, "onComplete: document exists");

                        // Get the list of events
                        List<Map<String, String>> events = (List<Map<String, String>>) document.get("events");

                        // Check if the events list is not empty
                        if (events == null || events.isEmpty()) {
                            callback.onFailure("No events found.");
                            return;
                        }

                        // Get the current event, which is the latest in the events list
                        Map<String, String> currentEvent = events.get(events.size() - 1);

                        // Extract host and matchday from the current event
                        String host = currentEvent.get("host");
                        String matchday = currentEvent.get("date");

                        // Check if host and matchday are available
                        if (host == null || matchday == null) {
                            callback.onFailure("Host or matchday data missing.");
                            return;
                        }

                        Log.i(TAG, "onComplete: Host=" + host + ", Matchday=" + matchday);
                        callback.onSuccess(host, matchday);
                    } else {
                        callback.onFailure("Dokument nicht gefunden!");
                        Log.i(TAG, "onComplete: Dokument nicht gefunden");
                    }
                } else {
                    callback.onFailure("Fehler beim Abrufen des Dokuments: " + task.getException());
                }
            }
        });
    }

    public interface getCurrentMatchdayCallback {
        void onSuccess(String player, String matchday);
        void onFailure(String errorMessage);
    }
}
