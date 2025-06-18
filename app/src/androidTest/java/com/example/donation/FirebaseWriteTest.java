package com.example.donation;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FirebaseWriteTest {

    private DatabaseReference databaseRef;
    private String userId = "testUserId";
    private String selectedUserType = "Orphanage";

    @Before
    public void setup() {
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().getTargetContext());
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Test
    public void testWriteOrphanageToDatabase() {
        String email = "test@example.com";
        Orphanage testOrphanage = new Orphanage("Test Orphanage", "123", "rbrwfadvgs","John Doe", "Some Address", "1234567890", email, selectedUserType);

        CountDownLatch latch = new CountDownLatch(1);

        databaseRef.child(selectedUserType).child(userId).setValue(testOrphanage)
                .addOnCompleteListener(task -> {
                    assertTrue("Database write failed: " + (task.getException() != null ? task.getException().getMessage() : ""), task.isSuccessful());
                    latch.countDown();
                });

        try {
            assertTrue("Firebase operation timed out", latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }
}

