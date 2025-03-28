package com.example.boardgamerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boardgamerapp.messaging.MessagingService;
import com.example.boardgamerapp.store.Store;

public class MessagingActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private MessagingService messagingService;
    private Store store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagingService = new MessagingService();
        store = new Store(this);
        String groupName = store.getGroupName();
        String playerName = store.getPlayerName();

        sendButton.setOnClickListener(v -> {
            String message = playerName + ": " + messageInput.getText().toString().trim();
            // Send the message to the group's topic
            messagingService.sendFCMMessage(MessagingActivity.this, groupName, message, true);
            Toast.makeText(MessagingActivity.this, getString(R.string.message_sent_info) + " " + groupName, Toast.LENGTH_SHORT).show();

            // Navigate back to DashboardActivity after sending the message
            Intent intent = new Intent(MessagingActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
