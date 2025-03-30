package com.example.boardgamerapp.library;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.boardgamerapp.R;
import com.example.boardgamerapp.database.Database;
import com.example.boardgamerapp.store.Store;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class UserStory5NightVote extends Fragment {

    Database database = new Database();
    private String groupName;
    private String eventid, hostrate,food,comment, generelly,user, host,date;
    private Button nightButtton;
    private EditText gastgeber, essen, allgemein, kommentar;
    private TextView title, cardTitle, cardSuptitle;
    int sterneInt;
    private RatingBar sterne;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_story5_night_vote, container, false);
        Store store = new Store(getContext());
        groupName = store.getGroupName();
        user = store.getPlayerName();

        //Views initialisieren
        nightButtton = view.findViewById(R.id.submitButton);
        gastgeber = view.findViewById(R.id.hostEditText);
        essen = view.findViewById(R.id.foodEditText);
        allgemein = view.findViewById(R.id.eveningEditText);
        kommentar = view.findViewById(R.id.commentEditText);
        sterne = view.findViewById(R.id.eveningRatingBar);
        title = view.findViewById(R.id.titleTextView);
        cardTitle = view.findViewById(R.id.cardTitleTextView);
        cardSuptitle = view.findViewById(R.id.cardSubtitleTextView);

       database.getDateAndHost(groupName, new Database.DateAndHostCallback() {
           @Override
           public void onDateAndHostReceived(ArrayList<String> dateAndHost) {

               host = dateAndHost.get(0);
               date = dateAndHost.get(1);
               SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN);
               SimpleDateFormat outputFormat = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
               String dateString = date;
               Date date = null;
               try {
                   date = inputFormat.parse(dateString);
                   String outputString = outputFormat.format(date);
                   title.setText("Game Night Bewertung");
                   cardTitle.setText("Event: " + outputString);
                   cardSuptitle.setText("Gastgeber: " + host);
               } catch (ParseException e) {
                   throw new RuntimeException(e);
               }

           }

           @Override
           public void onError(Exception e) {
                title.setText("Es gibt keinen Spieleabend der Bewertet werden kann");
           }


       });

        nightButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostrate = gastgeber.getText().toString();
                food = essen.getText().toString();
                comment = kommentar.getText().toString();
                generelly = allgemein.getText().toString();
                sterneInt = (int) sterne.getRating();

                if (user.equals(host)) {
                    Snackbar.make(getView(), "Du kannst dich nicht selbst bewerten", Snackbar.LENGTH_LONG).show();
                } else {
                    newNightVote(hostrate,food,comment,generelly,sterneInt);
                }
            }
        });

        return view;
    }



private void newNightVote(String host, String food,String comment,String generally,int sterne) {
    {
        database.getEvents(groupName, new Database.OnDocumentReceivedListener() {
            @Override
            public void onDocumentReceived(DocumentSnapshot document) {
                Log.i(TAG, "onDocumentReceived: " + document.getData());

                // Hole die Liste von Events aus dem DocumentSnapshot
                List<Map<String, Object>> events = (List<Map<String, Object>>) document.get("events");
                Log.i(TAG, "onDocumentReceived: " + events.size() + " Anzahl der Events");

                //Prüfen auf das erste Datum in der Vergangenheit, der letzte Spieleabend
                Date letztesDatum = null;
                Date heute = Calendar.getInstance().getTime();

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

                    for (Map<String, Object> event : events) {
                        String dateString = event.get("date").toString();
                        Date datum = sdf.parse(dateString);
                        Log.i(TAG, "onDocumentReceived: " + event.get("date"));

                        if (datum.before(heute)) {
                            if (letztesDatum == null || datum.after(letztesDatum)) { // Hier ist die Änderung
                                letztesDatum = datum;
                                eventid = event.get("event_id").toString();
                                user = event.get("host").toString();
                                date = event.get("date").toString();
                            }
                        }
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "ParseException: " + e.getMessage());

                }
                if (letztesDatum != null) {
                    Log.i(TAG, "onDocumentReceived: " + letztesDatum + " Spieltag " + eventid + " Event ID");
                    database.updateNightVotes(groupName, eventid, host, food, generally, comment, sterneInt);
                    Snackbar.make(getView(), user +" Du hast eine neue Bewertung eingereicht", Snackbar.LENGTH_LONG).show();
                } else {
                    Log.i(TAG, "onDocumentReceived: Kein zukünftiger Spieltag gefunden.");

                }
            }
            @Override
            public void onError(Exception e) {
                Snackbar.make(getView(), "Du hast bereits eine Bewertung abgegeben", Snackbar.LENGTH_LONG).show();
            }
        });
    }


}
}
