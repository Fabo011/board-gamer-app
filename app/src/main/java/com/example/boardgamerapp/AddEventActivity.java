package com.example.boardgamerapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boardgamerapp.library.UserStory2RotateHost;
import com.example.boardgamerapp.store.Store;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {

    private EditText editTextLocation;
    private EditText editTextDateTime;
    private LinearLayout gamesLayout;
    private Button btnAddGame;
    private Button btnCreateEvent;
    private UserStory2RotateHost userStory2RotateHost;
    private Store store;

    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Initialize UI elements
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDateTime = findViewById(R.id.editTextDateTime);
        gamesLayout = findViewById(R.id.gamesLayout);
        btnAddGame = findViewById(R.id.btnAddGame);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        // Initialize Store to retrieve group name and player name
        store = new Store(this);

        // Initialize UserStory2RotateHost
        userStory2RotateHost = new UserStory2RotateHost();

        // Add game input fields dynamically
        List<EditText> gameFields = new ArrayList<>();

        btnAddGame.setOnClickListener(v -> {
            // Add a new game input field
            EditText newGameField = new EditText(AddEventActivity.this);
            newGameField.setHint("Game Name");
            gamesLayout.addView(newGameField);
            gameFields.add(newGameField);
        });

        // Date and Time picker for the dateTime input field
        editTextDateTime.setOnClickListener(v -> {
            // Get the current date and time
            Calendar calendar = Calendar.getInstance();
            selectedYear = calendar.get(Calendar.YEAR);
            selectedMonth = calendar.get(Calendar.MONTH);
            selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
            selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
            selectedMinute = calendar.get(Calendar.MINUTE);

            // Open DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;

                        // Open TimePickerDialog after selecting the date
                        TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this,
                                (view1, hourOfDay, minute) -> {
                                    selectedHour = hourOfDay;
                                    selectedMinute = minute;
                                    // Format the selected date and time
                                    String dateTimeString = formatDateTime(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
                                    editTextDateTime.setText(dateTimeString);
                                }, selectedHour, selectedMinute, true);
                        timePickerDialog.show();
                    }, selectedYear, selectedMonth, selectedDay);
            datePickerDialog.show();
        });

        btnCreateEvent.setOnClickListener(v -> {
            // Get event details
            String location = editTextLocation.getText().toString().trim();
            String dateTime = editTextDateTime.getText().toString().trim();
            List<Map<String, Object>> gameVotes = new ArrayList<>();

            for (EditText gameField : gameFields) {
                String gameName = gameField.getText().toString().trim();
                if (!gameName.isEmpty()) {
                    // Add a game to the list with default votes set to 0
                    gameVotes.add(Map.of("game", gameName, "votes", 0));
                }
            }

            if (location.isEmpty() || dateTime.isEmpty() || gameVotes.isEmpty()) {
                Toast.makeText(AddEventActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            String groupName = store.getGroupName();
            String hostName = store.getPlayerName(); // Assuming this method returns the player name

            // Generate a unique event ID
            String eventId = "evt" + System.currentTimeMillis();

            // Create event and update host
            userStory2RotateHost.createEventAndUpdateHost(groupName, eventId, location, dateTime, hostName, gameVotes);

            // Return to DashboardActivity
            Intent intent = new Intent(AddEventActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the stack
            startActivity(intent);
            finish();
        });
    }

    // Helper method to format the date and time string
    private String formatDateTime(int year, int month, int day, int hour, int minute) {
        // Month is 0-indexed, so we add 1 to it
        String formattedDate = String.format("%02d.%02d.%d %02d:%02d", day, month + 1, year, hour, minute);
        return formattedDate;
    }
}
