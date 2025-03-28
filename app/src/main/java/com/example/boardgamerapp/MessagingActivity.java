package com.example.boardgamerapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boardgamerapp.messaging.MessagingService;

public class MessagingActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private MessagingService messagingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagingService = new MessagingService();

        // Subscribe to topic when activity starts
        messagingService.subscribeToTopic("topic1");

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                messagingService.sendFCMMessage(MessagingActivity.this, "topic1", message, true);  // Send to topic
                Toast.makeText(MessagingActivity.this, "Message sent to topic1!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MessagingActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


// sendNotificationToTopic("topic1", message);
// Function to send notification to a topic
    /*private void sendNotificationToTopic(String topic, String message) {
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder("your_project_id@fcm.googleapis.com")
                .setMessageId("1")
                .addData("message", message)
                .setTopic(topic)
                .build());
    }*/