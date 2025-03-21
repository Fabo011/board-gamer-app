package com.example.boardgamerapp.library;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.boardgamerapp.database.Database;
import com.google.firebase.firestore.DocumentSnapshot;

public class MainActivityLibrary {

    final Database database;

    public MainActivityLibrary() {
        database = new Database();
    }

    public void handleFormSubmission(Context context, String playerName, String groupName, String groupPassword, boolean isCreatingGroup, Runnable onSuccess) {
        if (playerName.isEmpty() || groupName.isEmpty() || groupPassword.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DEBUG", "Submit button clicked");

        if (isCreatingGroup) {
            // Creating a new group
            Log.d("DEBUG", "Creating a new group");
            database.fetchGroup(groupName, task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toast.makeText(context, "Group already exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Create group with the first player
                        database.createGroup(groupName, groupPassword, playerName, "sample_fcm_token");
                        onSuccess.run();
                    }
                } else {
                    Toast.makeText(context, "Error checking group", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Joining an existing group
            database.fetchGroup(groupName, task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String storedPassword = task.getResult().getString("grouppassword");
                    if (storedPassword != null && storedPassword.equals(groupPassword)) {
                        onSuccess.run();
                    } else {
                        Toast.makeText(context, "Incorrect group password!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Group not found!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

