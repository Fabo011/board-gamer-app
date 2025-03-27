package com.example.boardgamerapp.library;

import static android.content.ContentValues.TAG;
import android.util.Log;

import com.example.boardgamerapp.database.Database;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserStory1NextMatchDay {
    private final Database database;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;

    public UserStory1NextMatchDay() {
        database = new Database();
    }

    public void getCurrentMatchday(String groupName, final getCurrentMatchdayCallback callback) {
        database.getGroupDocument(groupName, task -> {
            if (task.isSuccessful()) {
                processDocument(task.getResult(), callback);
            } else {
                callback.onFailure("Error retrieving document: " + task.getException());
            }
        });
    }

    private void processDocument(DocumentSnapshot document, getCurrentMatchdayCallback callback) {
        if (document == null || !document.exists()) {
            callback.onFailure("Document not found.");
            Log.i(TAG, "Document not found.");
            return;
        }

        List<Map<String, String>> events = (List<Map<String, String>>) document.get("events");
        if (events == null || events.isEmpty()) {
            callback.onFailure("No events found.");
            return;
        }

        findNextMatchday(events, callback);
    }

    private void findNextMatchday(List<Map<String, String>> events, getCurrentMatchdayCallback callback) {
        Date currentDate = new Date();
        Map<String, String> closestEvent = null;
        Map<String, String> nextEvent = null;

        for (Map<String, String> event : events) {
            Date matchdayDate = parseDate(event.get("date"));
            if (matchdayDate != null) {
                Date matchdayEndTime = new Date(matchdayDate.getTime() + ONE_DAY_MILLIS);

                if (matchdayEndTime.after(currentDate) && isCloser(matchdayDate, closestEvent)) {
                    closestEvent = event;
                }

                if (matchdayDate.after(currentDate) && isCloser(matchdayDate, nextEvent)) {
                    nextEvent = event;
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
    }

    private Date parseDate(String dateString) {
        if (dateString == null) return null;
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing matchday date: " + dateString, e);
            return null;
        }
    }

    private boolean isCloser(Date matchdayDate, Map<String, String> existingEvent) {
        if (existingEvent == null) return true;
        Date existingDate = parseDate(existingEvent.get("date"));
        return existingDate == null || matchdayDate.before(existingDate);
    }

    public interface getCurrentMatchdayCallback {
        void onSuccess(String host, String matchday);
        void onFailure(String errorMessage);
    }
}

