package com.example.boardgamerapp.library;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.boardgamerapp.database.Database;
import com.example.boardgamerapp.messaging.MessagingService;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Map;
import java.util.List;

public class MainActivityLibrary {

    private final Database database;
    private final MessagingService messagingService;

    public MainActivityLibrary() {
        database = new Database();
        messagingService = new MessagingService();
    }

    // Main handler for the form submission
    public void handleFormSubmission(Context context, String playerName, String groupName, String groupPassword, boolean isCreatingGroup, Runnable onSuccess) {
        if (playerName.isEmpty() || groupName.isEmpty() || groupPassword.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DEBUG", "Submit button clicked");

        if (isCreatingGroup) {
            createNewGroup(context, playerName, groupName, groupPassword, onSuccess);
        } else {
            joinExistingGroup(context, playerName, groupName, groupPassword, onSuccess);
        }
    }

    // Creating a new group with the first player
    private void createNewGroup(Context context, String playerName, String groupName, String groupPassword, Runnable onSuccess) {
        Log.d("DEBUG", "Creating a new group");
        database.fetchGroup(groupName, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Toast.makeText(context, "Group already exists!", Toast.LENGTH_SHORT).show();
                } else {
                    // Get FCM token and create the group with the first player
                    handleFCMToken(context, groupName, playerName, groupPassword, onSuccess, true);
                }
            } else {
                Toast.makeText(context, "Error checking group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Joining an existing group
    private void joinExistingGroup(Context context, String playerName, String groupName, String groupPassword, Runnable onSuccess) {
        Log.d("DEBUG", "Joining an existing group");
        database.fetchGroup(groupName, task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String storedPassword = task.getResult().getString("grouppassword");
                if (storedPassword != null && storedPassword.equals(groupPassword)) {
                    // Check if the player exists in the group or not
                    checkPlayerInGroup(context, groupName, playerName, groupPassword, onSuccess);
                } else {
                    Toast.makeText(context, "Incorrect group password!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Group not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check if the player exists in the group
    private void checkPlayerInGroup(Context context, String groupName, String playerName, String groupPassword, Runnable onSuccess) {
        // Fetch the players from the group and check if the player already exists
        database.fetchGroup(groupName, task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Retrieve the list of players from the group
                DocumentSnapshot groupDocument = task.getResult();
                List<Map<String, Object>> players = (List<Map<String, Object>>) groupDocument.get("players");

                // Check if the player is already in the players list
                boolean playerExists = false;
                for (Map<String, Object> player : players) {
                    if (player.get("name").equals(playerName)) {
                        playerExists = true;
                        break;
                    }
                }

                if (playerExists) {
                    // Player exists, navigate to the Dashboard
                    Log.d("DEBUG", "Player already exists in the group, navigating to Dashboard");
                    onSuccess.run(); // Navigate to the dashboard
                } else {
                    // Player does not exist, proceed to add the player
                    handleFCMToken(context, groupName, playerName, groupPassword, onSuccess, false);
                }
            } else {
                Toast.makeText(context, "Error fetching group data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle FCM token retrieval and subsequent actions
    private void handleFCMToken(Context context, String groupName, String playerName, String groupPassword, Runnable onSuccess, boolean isCreatingGroup) {
        messagingService.getFCMToken(new MessagingService.OnTokenReceivedListener() {
            @Override
            public void onTokenReceived(String fcmToken) {
                Log.d("DEBUG", "FCM Token: " + fcmToken);

                // Handle the different scenarios
                if (isCreatingGroup) {
                    // Create the group and add the first player
                    database.createGroup(groupName, groupPassword, playerName, fcmToken);
                } else {
                    // Add the player to the group only if they don't already exist
                    database.addPlayerToGroup(groupName, playerName, fcmToken);
                }

                onSuccess.run();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}


