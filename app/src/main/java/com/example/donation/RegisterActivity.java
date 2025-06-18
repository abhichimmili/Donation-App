package com.example.donation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextRegEmail;
    private EditText editTextRegPassword;
    private EditText editTextRegConfirmPassword;
    private Button buttonRegister;
    private TextView textViewBackToLogin;

    // Toggle Button Group
    private MaterialButtonToggleGroup toggleButtonGroup;
    private Button buttonDonor;
    private Button buttonOrphanage;

    // Donor specific fields layout and EditTexts
    private LinearLayout layoutDonorFields;
    private EditText editTextDonorName;
    private EditText editTextDonorPhone;
    private EditText editTextDonorAddress;

    // Orphanage specific fields layout and EditTexts
    private LinearLayout layoutOrphanageFields;
    private EditText editTextOrphanageName;
    private EditText editTextOrphanageRegNumber;
    private EditText editTextOrphanageContactPerson;
    private EditText editTextOrphanageAddress;
    private EditText editTextOrphanagePhone;

    private String selectedUserType = "Donor";
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef; // Realtime DB


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users"); // Root node

        // Initialize common UI components
        editTextRegEmail = (EditText) findViewById(R.id.editTextRegEmail);
        editTextRegPassword = (EditText) findViewById(R.id.editTextRegPassword);
        editTextRegConfirmPassword = (EditText) findViewById(R.id.editTextRegConfirmPassword);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        textViewBackToLogin = (TextView) findViewById(R.id.textViewBackToLogin);

        // Initialize ToggleButtonGroup and buttons
        toggleButtonGroup = (MaterialButtonToggleGroup) findViewById(R.id.toggleButtonGroup);
        buttonDonor = (Button) findViewById(R.id.buttonDonor);
        buttonOrphanage = (Button) findViewById(R.id.buttonOrphanage);

        // Initialize Donor specific fields
        layoutDonorFields = (LinearLayout) findViewById(R.id.layoutDonorFields);
        editTextDonorName = (EditText) findViewById(R.id.editTextDonorName);
        editTextDonorPhone = (EditText) findViewById(R.id.editTextDonorPhone);
        editTextDonorAddress = (EditText) findViewById(R.id.editTextDonorAddress);

        // Initialize Orphanage specific fields
        layoutOrphanageFields = (LinearLayout) findViewById(R.id.layoutOrphanageFields);
        editTextOrphanageName = (EditText) findViewById(R.id.editTextOrphanageName);
        editTextOrphanageRegNumber = (EditText) findViewById(R.id.editTextOrphanageRegNumber);
        editTextOrphanageContactPerson = (EditText) findViewById(R.id.editTextOrphanageContactPerson);
        editTextOrphanageAddress = (EditText) findViewById(R.id.editTextOrphanageAddress);
        editTextOrphanagePhone = (EditText) findViewById(R.id.editTextOrphanagePhone);

        // Set initial state: Donor fields visible, Orphanage fields hidden
        layoutDonorFields.setVisibility(View.VISIBLE);
        layoutOrphanageFields.setVisibility(View.GONE);
        // Initially select the Donor button
        toggleButtonGroup.check(R.id.buttonDonor);
        toggleButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonDonor) {
                    selectedUserType = "Donor";
                    layoutDonorFields.setVisibility(View.VISIBLE);
                    layoutOrphanageFields.setVisibility(View.GONE);
                } else if (checkedId == R.id.buttonOrphanage) {
                    selectedUserType = "Orphanage";
                    layoutDonorFields.setVisibility(View.GONE);
                    layoutOrphanageFields.setVisibility(View.VISIBLE);
                }
            }
        });
        // Set click listener for the Register button
        buttonRegister.setOnClickListener(v -> {
            // Get common fields
            String email = editTextRegEmail.getText().toString().trim();
            String password = editTextRegPassword.getText().toString().trim();
            String confirmPassword = editTextRegConfirmPassword.getText().toString().trim();

            // Basic validation
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all common fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            if (selectedUserType.equals("Donor")) {
                                String donorName = editTextDonorName.getText().toString().trim();
                                String donorPhone = editTextDonorPhone.getText().toString().trim();
                                String donorAddress = editTextDonorAddress.getText().toString().trim();

                                if (donorName.isEmpty() || donorPhone.isEmpty() || donorAddress.isEmpty()) {
                                    Toast.makeText(this, "Please fill in all donor details", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (donorPhone.length() < 10) {
                                    Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Donor donor = new Donor(donorName,password, donorPhone, donorAddress, email, selectedUserType);
                                databaseRef.child(selectedUserType).child(userId).setValue(donor)
                                        .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this, selectedUserType + " registration successful", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Failed to save user data: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else if (selectedUserType.equals("Orphanage")) {
                                String orphanageName = editTextOrphanageName.getText().toString().trim();
                                String regNumber = editTextOrphanageRegNumber.getText().toString().trim();
                                String contactPerson = editTextOrphanageContactPerson.getText().toString().trim();
                                String orphanageAddress = editTextOrphanageAddress.getText().toString().trim();
                                String orphanagePhone = editTextOrphanagePhone.getText().toString().trim();

                                if (orphanageName.isEmpty() || regNumber.isEmpty() || contactPerson.isEmpty() || orphanageAddress.isEmpty() || orphanagePhone.isEmpty()) {
                                    Toast.makeText(this, "Please fill in all orphanage details", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Orphanage orphanage = new Orphanage(orphanageName, regNumber,password, contactPerson, orphanageAddress, orphanagePhone, email, selectedUserType);
                                databaseRef.child(selectedUserType).child(userId).setValue(orphanage).addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this, selectedUserType + " registration successful", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Failed to save user data: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                        }
                        else {
                            Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        // Set click listener for "Already have an account? Login here."
        textViewBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}