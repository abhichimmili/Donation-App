package com.example.donation;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrphanageActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    ActionBarDrawerToggle toggle;
    private static final int REQUEST_PERMISSION_READ_STORAGE = 100;
    CircleImageView ivProfilePhoto;
    TextView userName;
    ImageButton btnChangePhoto;
    // Modern Activity Result API launcher for picking image
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_PROFILE_IMAGE_URI = "profile_image_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orphanage);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        bottomNavigationView= (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView= (NavigationView) findViewById(R.id.nav_view);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof OrphHomeFragment) {
                bottomNavigationView.setSelectedItemId(R.id.orphhome);
            } else if (currentFragment instanceof DashBoardFragment) {
                bottomNavigationView.setSelectedItemId(R.id.dashboard);
            } else if (currentFragment instanceof SellFragment) {
                bottomNavigationView.setSelectedItemId(R.id.sell);
            }
            // add other fragments as needed
        });

        toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open_nav,R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // Handle menu item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                int id = item.getItemId();
                if (id == R.id.orphhome) {
                    Toast.makeText(OrphanageActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.profile) {
                    startActivity(new Intent(OrphanageActivity.this, ProfileActivity.class));
                } else if (id == R.id.settings) {
                    Toast.makeText(OrphanageActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.about) {
                    startActivity(new Intent(OrphanageActivity.this, ProfileActivity.class));

                } else if (id == R.id.logout) {
                    // Clear login info and logout
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    Toast.makeText(OrphanageActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OrphanageActivity.this, LoginActivity.class));
                    finish();
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });
        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new OrphHomeFragment()).commit();
            navigationView.setCheckedItem(R.id.orphhome);
        }
        replaceFragment(new OrphHomeFragment());
//        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.orphhome) {
                replaceFragment(new OrphHomeFragment());
                return true;
            } else if (id == R.id.sell) {
                replaceFragment(new SellFragment());
                return true;
            } else if (id == R.id.dashboard) {
                replaceFragment(new DashBoardFragment());
                return true;
            }

            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.orphhome);

        View headerView = navigationView.getHeaderView(0);
        ivProfilePhoto = headerView.findViewById(R.id.ivProfile);
        userName = headerView.findViewById(R.id.username);
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userType = prefs.getString("userType", null);
        String userId = prefs.getString("userId", null);

        if (userType != null && userId != null) {
            loadUserName(userType, userId);
        } else {
            Toast.makeText(this, "User info missing", Toast.LENGTH_SHORT).show();
        }

        btnChangePhoto= headerView.findViewById(R.id.btnChangePhoto);
        btnChangePhoto.setOnClickListener(v -> {
            // Check permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            REQUEST_PERMISSION_READ_STORAGE);
                } else {
                    openGallery();
                }
            } else {
                // For Android 12 and below
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_READ_STORAGE);
                } else {
                    openGallery();
                }
            }

        });
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                // Copy image to local storage
                                String filename = "profile_image.jpg";
                                File file = new File(getFilesDir(), filename);

                                try (InputStream in = getContentResolver().openInputStream(selectedImageUri);
                                     OutputStream out = new FileOutputStream(file)) {
                                    byte[] buffer = new byte[1024];
                                    int len;
                                    while ((len = in.read(buffer)) > 0) {
                                        out.write(buffer, 0, len);
                                    }
                                }

                                // Display the image
                                ivProfilePhoto.setImageURI(Uri.fromFile(file));

                                // Save path in SharedPreferences
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                        .edit()
                                        .putString("profile_image_path", file.getAbsolutePath())
                                        .apply();

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        String imagePath = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString("profile_image_path", null);
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                ivProfilePhoto.setImageURI(Uri.fromFile(imgFile));
            }
        }

    }
    private void loadUserName(String userType, String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userType)
                .child(userId);

        userRef.child("contactPerson").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String name = task.getResult().getValue(String.class);
                if (name != null) {
                    userName.setText(name);
                } else {
                    userName.setText("Name not found");
                }
            } else {
                Toast.makeText(OrphanageActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                // Handle permission denied case
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notification) {
            Toast.makeText(this, "No New Notifications", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (currentFragment instanceof OrphHomeFragment) {
                // Show exit confirmation
                new AlertDialog.Builder(this)
                        .setTitle("Exit App")
                        .setCancelable(true)
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Do you really want to exit?")
                        .setPositiveButton("Yes", (dialog, which) -> finish())
                        .setNegativeButton("No", null)
                        .show();
            } else {
                // Go back to previous fragment or Home
                super.onBackPressed();
            }
        }
    }
    public void navigateToFragment(Fragment fragment, int menuItemId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        bottomNavigationView.setSelectedItemId(menuItemId);
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.commit();
    }
}