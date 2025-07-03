package com.example.donation;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Determine user type from SharedPreferences
            String userType = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    .getString("userType", null);

            if (userType != null) {
                FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(userType)
                        .child(uid)
                        .child("fcmToken")
                        .setValue(token)
                        .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved"))
                        .addOnFailureListener(e -> Log.e("FCM", "Token save failed: " + e.getMessage()));
            }
        } else {
            Log.w("FCM", "User is null, token not saved");
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            NotificationUtils.showNotification(this, title, body);
        }
    }
}
