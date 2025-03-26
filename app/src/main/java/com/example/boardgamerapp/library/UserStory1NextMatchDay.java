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

                        List<Map<String, String>> events = (List<Map<String, String>>) document.get("events");

                        if (events == null || events.isEmpty()) {
                            callback.onFailure("No events found.");
                            return;
                        }

                        Map<String, String> closestEvent = null;
                        Map<String, String> nextEvent = null;
                        Date currentDate = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                        long oneDayInMillis = 24 * 60 * 60 * 1000; // 1 day in milliseconds

                        for (Map<String, String> event : events) {
                            String matchdayString = event.get("date");
                            if (matchdayString != null) {
                                try {
                                    Date matchdayDate = dateFormat.parse(matchdayString);
                                    if (matchdayDate != null) {
                                        Date matchdayEndTime = new Date(matchdayDate.getTime() + oneDayInMillis); // Add 1 day

                                        if (matchdayEndTime.after(currentDate)) {
                                            if (closestEvent == null || matchdayDate.before(dateFormat.parse(closestEvent.get("date")))) {
                                                closestEvent = event;
                                            }
                                        }

                                        if (matchdayDate.after(currentDate)) {
                                            if (nextEvent == null || matchdayDate.before(dateFormat.parse(nextEvent.get("date")))) {
                                                nextEvent = event;
                                            }
                                        }
                                    }
                                } catch (ParseException e) {
                                    Log.e(TAG, "Error parsing matchday date", e);
                                }
                            }
                        }

                        if (closestEvent != null) {
                            callback.onSuccess(closestEvent.get("host"), closestEvent.get("date"));
                        } else if (nextEvent != null) {
                            callback.onSuccess(nextEvent.get("host"), nextEvent.get("date"));
                        } else {
                            callback.onFailure("No upcoming events found.");
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

