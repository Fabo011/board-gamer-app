package com.example.boardgamerapp.messaging;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessaging;

public class MessagingService extends Service {

    private static final String TAG = "MessagingService";

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
