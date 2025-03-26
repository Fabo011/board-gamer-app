package com.example.boardgamerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boardgamerapp.messaging.MessagingService;

public class MessagingActivity extends AppCompatActivity {

    private static final String TAG = "MessagingActivity";
    private EditText messageInput;
    private Button sendButton;
    private MessagingService messagingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagingService = new MessagingService(); // Initialize MessagingService

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    messagingService.sendNotification(message);
                    Toast.makeText(MessagingActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();

                    // Redirect back to DashboardActivity after message is sent
                    Intent intent = new Intent(MessagingActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish(); // Close MessagingActivity so user can't go back with back button
                } else {
                    Toast.makeText(MessagingActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
