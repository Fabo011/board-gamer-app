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
//Attribute
    private FirebaseFirestore database;

    private String matchday;
    private String host;


//Konstruktoren
    public UserStory1NextMatchDay() {
        database = FirebaseFirestore.getInstance();

    }

//Methoden

    public void getCurrentMatchday(String groupName, final getCurrentMatchdayCallback callback) {

        DocumentReference docRef = database.collection("groups").document(groupName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                Log.i(TAG, "onComplete: ");
                if (task.isSuccessful()) {
                    Log.i(TAG, "onComplete: " + "task success");
                    DocumentSnapshot document = task.getResult();
                    Log.i(TAG, "onComplete: " + "task success " + document.getId());
                    if (document.exists()) {
                        Log.i(TAG, "onComplete: " + "dokument exist");
                        Long nextHostIndex = document.getLong("next_host_index"); // Holt den nächsten Spieler
                        List<Map<String, String>> players = (List<Map<String, String>>) document.get("players");
                        List<Map<String, String>> events = (List<Map<String, String>>) document.get("events");


                        if (!players.isEmpty() && !events.isEmpty()){
                            String player = players.get(nextHostIndex.intValue() - 1).get("name");
                            String matchday = events.get(events.size() -1 ).get("date");
                            Log.i(TAG, "onComplete: " + nextHostIndex);
                            callback.onSuccess(player, matchday);
                        }



                    } else {
                        callback.onFailure("Dokument nicht gefunden!");
                        Log.i(TAG, "onComplete: " + "Dokument nicht gefunden");
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

//Getter und Setter
    public String getMatchday() {
        return matchday;
    }

    public void setMatchday(String matchday) {
        this.matchday = matchday;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


}
