package com.example.boardgamerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boardgamerapp.library.MainActivityLibrary;
import com.example.boardgamerapp.store.Store;
import com.example.boardgamerapp.messaging.MessagingService;

public class MainActivity extends AppCompatActivity {

    private EditText etPlayerName, etGroupName, etGroupPassword;
    private Switch toggleSwitch;
    private Button btnSubmit;
    private MainActivityLibrary library;
    private Store store;
    private MessagingService messagingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize library and Store
        library = new MainActivityLibrary();
        store = new Store(this); // Pass context to Store

        // Initialize the MessagingService
        messagingService = new MessagingService();

        // Subscribe to the group topic using the group name stored in the Store
        String groupName = store.getGroupName();
        if (groupName != null && !groupName.isEmpty()) {
            messagingService.subscribeToTopic(groupName);
            Log.d("MessagingService", "Subscribed to group topic: " + groupName);
        }

        // Initialize UI elements
        etPlayerName = findViewById(R.id.etPlayerName);
        etGroupName = findViewById(R.id.etGroupName);
        etGroupPassword = findViewById(R.id.etGroupPassword);
        toggleSwitch = findViewById(R.id.toggleSwitch);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Handle form submission
        btnSubmit.setOnClickListener(view -> {
            String playerName = etPlayerName.getText().toString().trim();
            String groupNameInput = etGroupName.getText().toString().trim();
            String groupPassword = etGroupPassword.getText().toString().trim();
            boolean isCreatingGroup = toggleSwitch.isChecked();

            // Save group name to Store (SharedPreferences)
            store.saveGroupName(groupNameInput, playerName);

            // Pass the player and group info to the library
            library.handleFormSubmission(this, playerName, groupNameInput, groupPassword, isCreatingGroup, this::navigateToDashboard);
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }
}
