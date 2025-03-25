package com.example.boardgamerapp.messaging;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// https://www.youtube.com/watch?v=oNoRw69ro2k
// https://firebase.google.com/docs/cloud-messaging/android/client
public class MessagingService extends Service {

    private static final String TAG = "MessagingService";
    private static final String SERVER_KEY = "75ceaafed6efe92a9e8d15a0100924303e9c0624"; // Replace with Firebase Server Key
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/board-game-app-4d0db/messages:send";

    // Hardcoded FCM Token (replace this with a real user's token)
    private static final String HARD_CODED_FCM_TOKEN = "f7IvfqktTbeJ5QVpfQD7oF:APA91bEjFfZ9ENtQxmEXBfq8DDyuvNCUiCgk9kuV5Dwvevdc_XJf2tcnVBjaOZkz25qMiI_oF3k8l9F7kYRBY5g-YUlWvrYNmLm-NxVMAaYeYG03Nq3xolo";

    // Method to get the current FCM token
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

    // Method to send a notification to a specific user
    public void sendNotification(String message) {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        new Thread(() -> {
            try {
                URL url = new URL(FCM_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + SERVER_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject notification = new JSONObject();
                notification.put("title", "New Message");
                notification.put("body", message);

                JSONObject json = new JSONObject();
                json.put("to", HARD_CODED_FCM_TOKEN);
                json.put("notification", notification);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "FCM Response Code: " + responseCode);

                Log.d(TAG, "FCM Response: " + new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce("", (a, b) -> a + b));

            } catch (Exception e) {
                Log.e(TAG, "Error sending FCM notification", e);
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Interface to listen to token retrieval results
    public interface OnTokenReceivedListener {
        void onTokenReceived(String token);
        void onError(String error);
    }
}
