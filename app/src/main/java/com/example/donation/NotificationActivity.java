package com.example.donation;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.appcompat.widget.Toolbar;

public class NotificationActivity extends AppCompatActivity {
    private LinearLayout notificationsContainer;
    private String userId;
    private DatabaseReference notifRef;
    private ValueEventListener notifListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = findViewById(R.id.toolbarnotify);
        setSupportActionBar(toolbar);

        // Enable the back (up) button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        notificationsContainer = findViewById(R.id.notificationsContainer);
        userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", null);

        if (userId != null) {
            notifRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId).child("History");
            startListeningForNotifications();
        }
        // Handle back arrow click
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void startListeningForNotifications() {
        notifListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationsContainer.removeAllViews();

                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String message = child.getValue(String.class);
                        if (message != null) {
                            View card = getLayoutInflater().inflate(R.layout.item_notification_card, notificationsContainer, false);
                            TextView textNotification = card.findViewById(R.id.textNotification);
                            textNotification.setText("You have new donation of "+message+" by a donor.");
                            notificationsContainer.addView(card);
                        }
                    }
                } else {
                    TextView empty = new TextView(NotificationActivity.this);
                    empty.setText("No notifications yet.");
                    empty.setTextSize(16);
                    empty.setPadding(8, 8, 8, 8);
                    notificationsContainer.addView(empty);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Failed to load notifications.", Toast.LENGTH_SHORT).show();
            }
        };

        notifRef.addValueEventListener(notifListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifRef != null && notifListener != null) {
            notifRef.removeEventListener(notifListener);
        }
    }
}
