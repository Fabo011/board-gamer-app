package com.example.boardgamerapp.messaging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.example.boardgamerapp.R;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";
    private static final String CHANNEL_ID = "fcm_default_channel";

    // Called when a new token is generated or refreshed.
    public void getFCMToken(OnTokenReceivedListener listener) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult();
                Log.d(TAG, "FCM Token: " + fcmToken);
                listener.onTokenReceived(fcmToken);  // Callback with the token
            } else {
                Log.e(TAG, "Error retrieving FCM token", task.getException());
                listener.onError("Error retrieving FCM token");  // Callback with error message
            }
        });
    }

    // Handle incoming FCM messages
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if the message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleNow();
        }

        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            // Handle notification payload
            sendNotification(remoteMessage.getNotification().getBody());
        }
    }

    // Handle short-running tasks (i.e., within 10 seconds)
    private void handleNow() {
        Log.d(TAG, "Handling message in foreground.");
        // Implement your immediate handling logic here
    }

    // Display a notification to the user
    public void sendNotification(String messageBody) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0+ (Oreo and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "FCM Channel";
            String description = "Channel for FCM notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification with your custom icon
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("New FCM Message")
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.notification) // Use the custom icon here
                .build();

        // Display the notification
        notificationManager.notify(0, notification);
    }

    // Subscribe to a topic
    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ? "Subscribed to topic: " + topic : "Failed to subscribe to topic: " + topic;
                    Log.d(TAG, msg);
                });
    }

    // Unsubscribe from a topic
    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ? "Unsubscribed from topic: " + topic : "Failed to unsubscribe from topic: " + topic;
                    Log.d(TAG, msg);
                });
    }

    // Interface to listen to token retrieval results
    public interface OnTokenReceivedListener {
        void onTokenReceived(String token);
        void onError(String error);
    }
}

