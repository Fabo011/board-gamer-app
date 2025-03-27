package com.example.boardgamerapp.library;

import android.content.Context;
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
    private Context context; // Add context reference
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;

    // ✅ New constructor accepting Context
    public UserStory1NextMatchDay(Context context) {
        this.context = context;
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
            return;
        }

        List<Map<String, Object>> events = (List<Map<String, Object>>) document.get("events");
        if (events == null || events.isEmpty()) {
            callback.onFailure("No events found.");
            return;
        }

        findNextMatchday(events, callback);
    }

    private void findNextMatchday(List<Map<String, Object>> events, getCurrentMatchdayCallback callback) {
        Date currentDate = new Date();
        Map<String, Object> closestEvent = null;

        for (Map<String, Object> event : events) {
            Date matchdayDate = parseDate((String) event.get("date"));
            if (matchdayDate != null) {
                Date matchdayEndTime = new Date(matchdayDate.getTime() + ONE_DAY_MILLIS);
                if (matchdayEndTime.after(currentDate) && isCloser(matchdayDate, closestEvent)) {
                    closestEvent = event;
                }
            }
        }

        if (closestEvent != null) {
            String eventId = (String) closestEvent.get("event_id");
            List<Map<String, Object>> gameVotes = (List<Map<String, Object>>) closestEvent.get("game_votes");
            callback.onSuccess((String) closestEvent.get("host"), (String) closestEvent.get("date"), eventId, gameVotes);
        } else {
            callback.onFailure("No upcoming events found.");
        }
    }

    private Date parseDate(String dateString) {
        if (dateString == null) return null;
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("UserStory1NextMatchDay", "Error parsing matchday date: " + dateString, e);
            return null;
        }
    }

    private boolean isCloser(Date matchdayDate, Map<String, Object> existingEvent) {
        if (existingEvent == null) return true;
        Date existingDate = parseDate((String) existingEvent.get("date"));
        return existingDate == null || matchdayDate.before(existingDate);
    }

    public interface getCurrentMatchdayCallback {
        void onSuccess(String host, String matchday, String eventId, List<Map<String, Object>> gameVotes);
        void onFailure(String errorMessage);
    }
}
