package com.example.donation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private RadioGroup radioGroupUserType;
    private RadioButton radioDonor, radioOrphanage;


    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userType = prefs.getString("userType", null);
        String userId = prefs.getString("userId", null);

        if (currentUser != null && userType != null && userId != null) {
            if ("Donor".equalsIgnoreCase(userType)) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, OrphanageActivity.class));
            }
            finish(); // redirect without login
            return;
        }

        editTextEmail = findViewById(R.id.editTextLoginEmail);
        editTextPassword = findViewById(R.id.editTextLoginPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = (TextView) findViewById(R.id.textViewRegister);

        radioGroupUserType = (RadioGroup) findViewById(R.id.radioGroupUserType);
        radioDonor = (RadioButton) findViewById(R.id.radioDonor);
        radioOrphanage = (RadioButton) findViewById(R.id.radioOrphanage);

        buttonLogin.setOnClickListener(v -> {
                    String email = editTextEmail.getText().toString().trim();
                    String password = editTextPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                        Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int selectedId = radioGroupUserType.getCheckedRadioButtonId();
            String selectedUserType;

            if (selectedId == R.id.radioDonor) {
                selectedUserType = "Donor";
            } else if (selectedId == R.id.radioOrphanage) {
                selectedUserType = "Orphanage";
            } else {
                selectedUserType = "";
                Toast.makeText(this, "Please select user type", Toast.LENGTH_SHORT).show();
                return;
            }

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user == null) {
                                        Toast.makeText(this, "Failed to get user", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String userIdFb = user.getUid();

                                    // Fetch userType from Firebase Realtime Database
                                    DatabaseReference ref = FirebaseDatabase.getInstance()
                                            .getReference("Users")
                                            .child(selectedUserType)
                                            .child(userIdFb);

                                    ref.get().addOnCompleteListener(dataTask -> {
                                        if (dataTask.isSuccessful() && dataTask.getResult().exists()) {
                                            // Save login info to SharedPreferences
                                            SharedPreferences loginPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                            loginPrefs.edit()
                                                    .putString("userType", selectedUserType)
                                                    .putString("userId", userIdFb)
                                                    .apply();
                                            // Navigate based on user type
                                            if (selectedUserType.equals("Donor")) {
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.putExtra("userType", "Donor");
                                                intent.putExtra("userId", userIdFb);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                FirebaseMessaging.getInstance().getToken()
                                                        .addOnCompleteListener(tokenTask -> {
                                                            if (tokenTask.isSuccessful()) {
                                                                String token = tokenTask.getResult();
                                                                FirebaseDatabase.getInstance().getReference("Users")
                                                                        .child("Orphanage")
                                                                        .child(userIdFb)
                                                                        .child("fcmToken")
                                                                        .setValue(token);
                                                            }
                                                        });
                                                Intent intent = new Intent(LoginActivity.this, OrphanageActivity.class);
                                                intent.putExtra("userType", "Orphanage");
                                                intent.putExtra("userId", userIdFb);
                                                startActivity(intent);
                                                finish();
                                            }
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this,
                                                    "User not found under selected user type",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            "Login failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                });
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}