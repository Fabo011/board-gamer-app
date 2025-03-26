package com.example.boardgamerapp.library;

import static android.content.ContentValues.TAG;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

                        // Initialize a variable to track the next event
                        Map<String, String> nextEvent = null;
                        Date currentDate = new Date();  // Get the current date

                        // Loop through the events to find the closest future matchday
                        for (Map<String, String> event : events) {
                            String matchdayString = event.get("date");
                            if (matchdayString != null) {
                                try {
                                    // Convert the matchday string to a Date object
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                                    Date matchdayDate = dateFormat.parse(matchdayString);

                                    // Check if this event is in the future and closer than the previously found one
                                    if (matchdayDate != null && matchdayDate.after(currentDate)) {
                                        if (nextEvent == null || matchdayDate.before(dateFormat.parse(nextEvent.get("date")))) {
                                            nextEvent = event;
                                        }
                                    }
                                } catch (ParseException e) {
                                    Log.e(TAG, "Error parsing matchday date", e);
                                }
                            }
                        }

                        // If a future event was found, return it
                        if (nextEvent != null) {
                            String host = nextEvent.get("host");
                            String matchday = nextEvent.get("date");
                            Log.i(TAG, "onComplete: Host=" + host + ", Matchday=" + matchday);
                            callback.onSuccess(host, matchday);
                        } else {
                            callback.onFailure("No future events found.");
                        }

                    } else {
                        callback.onFailure("Document not found.");
                        Log.i(TAG, "onComplete: Document not found");
                    }
                } else {
                    callback.onFailure("Error retrieving document: " + task.getException());
                }
            }
        });
    }

    public interface getCurrentMatchdayCallback {
        void onSuccess(String host, String matchday);
        void onFailure(String errorMessage);
    }
}

