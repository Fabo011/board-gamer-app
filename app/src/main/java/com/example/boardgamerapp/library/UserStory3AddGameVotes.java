package com.example.boardgamerapp.library;
import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.boardgamerapp.database.Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserStory3AddGameVotes {

    private Database database;
    private DocumentReference eventsCollection;

    public UserStory3AddGameVotes() {
        database = new Database();

    }

public void getEvents(String groupname){
    database.getEvents(groupname,new OnCompleteListener<List<Map<String, Object>>>() {
        @Override
        public void onComplete(Task<List<Map<String, Object>>> task) {
            if (task.isSuccessful()) {
                List<Map<String, Object>> events = task.getResult();
                if (events != null) {
                    // Hier kannst du die Liste der Events verarbeiten
                    for (Map<String, Object> event : events) {
                        System.out.println("Event: " + event);
                        // Hier kannst du auf die einzelnen Felder des Events zugreifen
                        String eventId = (String) event.get("event_id");
                        String date = (String) event.get("date");
                        String host = (String) event.get("host");
                        String location = (String) event.get("location");
                        String eventStatus = (String) event.get("event_status");
                        List<Map<String, Object>> gameVotes = (List<Map<String, Object>>) event.get("game_votes");

                        Log.i(TAG, "onComplete: " + eventId);
                        Log.i(TAG, "onComplete: " + date);
                        Log.i(TAG, "onComplete: " + host);
                        Log.i(TAG, "onComplete: " + location);
                        Log.i(TAG, "onComplete: " + eventStatus);
                        Log.i(TAG, "onComplete: " + gameVotes);

                    }
                } else {
                    System.out.println("Keine Events gefunden");
                }
            } else {
                System.out.println("Fehler beim Abrufen der Events: " + task.getException());
            }
        }
    });

}

}