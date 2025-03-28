package com.example.boardgamerapp.messaging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.boardgamerapp.R;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Scanner;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";
    private static final String CHANNEL_ID = "fcm_default_channel";
    private static final String PROJECT_ID = "board-game-app-4d0db"; // Replace with Firebase Project ID

    // Called when a new token is generated or refreshed.
    public void getFCMToken(OnTokenReceivedListener listener) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult();
                Log.d(TAG, "FCM Token: " + fcmToken);
                listener.onTokenReceived(fcmToken);
            } else {
                Log.e(TAG, "Error retrieving FCM token", task.getException());
                listener.onError("Error retrieving FCM token");
            }
        });
    }

    // Handle incoming FCM messages
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleNow();
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }
    }

    private void handleNow() {
        Log.d(TAG, "Handling message in foreground.");
    }

    public void sendNotification(String messageBody) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "FCM Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel for FCM notifications");
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("New FCM Message")
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.notification)
                .build();

        notificationManager.notify(0, notification);
    }

    // Subscribe to a topic
    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(task -> {
            String msg = task.isSuccessful() ? "Subscribed to topic: " + topic : "Failed to subscribe to topic: " + topic;
            Log.d(TAG, msg);
        });
    }

    // Unsubscribe from a topic
    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
            String msg = task.isSuccessful() ? "Unsubscribed from topic: " + topic : "Failed to unsubscribe from topic: " + topic;
            Log.d(TAG, msg);
        });
    }

    // New method to send a notification using Firebase v1 API
    public void sendFCMMessage(Context context, String target, String message, boolean isTopic) {
        new Thread(() -> {
            try {
                // Load Google credentials from service account JSON file
                InputStream serviceAccount = context.getResources().openRawResource(R.raw.service_account);
                GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccount)
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));

                googleCredentials.refresh();
                String accessToken = googleCredentials.getAccessToken() != null
                        ? googleCredentials.getAccessToken().getTokenValue()
                        : "";

                if (accessToken.isEmpty()) {
                    Log.e(TAG, "Failed to obtain access token.");
                    return;
                }

                // Firebase v1 API URL
                String apiUrl = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";

                // Create JSON payload
                JSONObject json = new JSONObject();
                JSONObject messageObject = new JSONObject();
                JSONObject notification = new JSONObject();

                notification.put("title", "New Message");
                notification.put("body", message);

                messageObject.put("notification", notification);
                if (isTopic) {
                    messageObject.put("topic", target);  // Send to topic
                } else {
                    messageObject.put("token", target);  // Send to specific FCM token
                }
                json.put("message", messageObject);

                // Open connection to Firebase Cloud Messaging v1 API
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Send JSON payload
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(json.toString().getBytes());
                outputStream.flush();
                outputStream.close();

                // Get response
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                Log.d(TAG, "FCM v1 Response: " + response);
            } catch (Exception e) {
                Log.e(TAG, "Error sending FCM v1 message", e);
            }
        }).start();
    }

    public interface OnTokenReceivedListener {
        void onTokenReceived(String token);
        void onError(String error);
    }
}

