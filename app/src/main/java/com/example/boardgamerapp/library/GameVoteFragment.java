package com.example.boardgamerapp.library;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.boardgamerapp.R;
import com.example.boardgamerapp.database.Database;
import com.example.boardgamerapp.store.Store;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GameVoteFragment extends Fragment {

    Button addButton;
    EditText editTextGameName;
    EditText editTextBeschreibung;
    String eventid;
    String groupName;
    String spielname;
    Database database = new Database();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_game_vote, container, false);
        addButton = view.findViewById(R.id.submitGameSuggestionButton);
        editTextGameName = view.findViewById(R.id.gameNameEditText);
        editTextBeschreibung = view.findViewById(R.id.gameDescriptionEditText);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Add game suggestion to database
                Store store = new Store(getContext());
                groupName = store.getGroupName();
                spielname = editTextGameName.getText().toString();
                events(groupName,spielname);

                Snackbar.make(view, "Du hast einen neuen Spielvorschlag eingereicht", Snackbar.LENGTH_LONG).show();
            }
        });
        return view;
    }

    private void events(String groupName,String spielname) {
        database.getEvents(groupName, new Database.OnDocumentReceivedListener() {
            @Override
            public void onDocumentReceived(DocumentSnapshot document) {
                Log.i(TAG, "onDocumentReceived: " + document.getData());

                // Hole die Liste von Events aus dem DocumentSnapshot
                List<Map<String, Object>> events = (List<Map<String, Object>>) document.get("events");
                Log.i(TAG, "onDocumentReceived: " + events.size() + " Anzahl der Events");

                //Prüfen auf das nächste Datum in der Zukunft, also der anstehende Event
                Date naechstesDatum = null;

                Date heute = Calendar.getInstance().getTime();

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

                    for (Map<String, Object> event : events) {
                        String dateString = event.get("date").toString();
                        Date datum = sdf.parse(dateString);
                        Log.i(TAG, "onDocumentReceived: " + event.get("date"));

                        if (datum.after(heute)) {
                            if (naechstesDatum == null || datum.before(naechstesDatum)) {
                                naechstesDatum = datum;
                                eventid = event.get("event_id").toString();

                            }
                        }
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "ParseException: " + e.getMessage());

                }

                if (naechstesDatum != null) {
                    Log.i(TAG, "onDocumentReceived: " + naechstesDatum + " Spieltag" + eventid + " Event ID");
                    database.updateGameSuggestion(groupName,eventid,spielname);
                } else {
                    Log.i(TAG, "onDocumentReceived: Kein zukünftiger Spieltag gefunden.");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.i(TAG, "onDocumentReceived: Fehler " + e );
            }
        });
    }

}