package com.example.donation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DonateFragment extends Fragment {

    // UI Elements
 private LinearLayout mainContentLayout;
//    private LinearLayout locationPopup;
    private LinearLayout orphanageListScreen;
    private LinearLayout locationPopup;
    private LinearLayout layout;
    private LinearLayout donationDetailScreen;
    private TextView donationHeader;
    private LinearLayout donationContentContainer;
    private LinearLayout orphanagesContainer;

    private Button allowLocationButton, denyLocationButton;
    private ImageButton backToMainButton;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Location currentLocation;

    // Donation State
    private String selectedDonationType = "";
    private String selectedOrphanageName = "";

    public DonateFragment() {
        // Required empty public constructor
    }
    private static class Orphanage {
        String name;
        String address;
        double latitude;
        double longitude;
        String description;
        String userId;

        public Orphanage(String name, String address, double latitude, double longitude, String description) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.description = description;
            this.userId = null;
        }
    }
    private List<Orphanage> allOrphanages =new ArrayList<>( Arrays.asList(
            new Orphanage("Hopeful Hearts Orphanage",
                    "123 Main St",
                    17.6868,
                    83.2185,
                    "Providing care for children since 1990."
            ),
            new Orphanage("Children's Haven Foundation",
                    "456 Oak Ave",
                    17.7000,
                    83.2500,
                    "A safe home for over 50 children."
            ),
            new Orphanage("Sunshine Kids Home",
                    "789 Pine Ln",
                    17.6500,
                    83.1900,
                    "Nurturing bright futures for every child."
            ),
            new Orphanage("Future Stars Center",
                    "101 Maple Rd",
                    17.6950,
                    83.2350,
                    "Empowering youth through education."
            ),
            new Orphanage("Kindness Kids Shelter",
                    "202 Elm St",
                    17.7100,
                    83.2000,
                    "Emergency shelter and support."
            ),
            new Orphanage("Compassionate Care Home",
                    "789 Charity Rd",
                    14.99134134977231,
                    79.68109808037292,
                    "Serving vulnerable children with love."
            ),
            new Orphanage(
                    "Bright Future Orphanage",
                    "101 Sunrise Blvd",
                    14.993883352773445,
                    79.6962956725344,
                    "Dedicated to nurturing young minds."
            ),
            new Orphanage(
                    "Safe Haven Home",
                    "202 Peace St",
                    14.983489756804653,
                    79.65729185736369,
                    "A secure and loving environment for children in need."
            ),
            new Orphanage(
                    "Little Steps Shelter",
                    "303 Harmony Ln",
                    14.975261135422281,
                    79.68239776138162,
                    "Helping children take confident steps into the future."
            ),
            new Orphanage(
                    "Hope Springs Foundation",
                    "404 Unity Ave",
                    15.003699063092336,
                    79.6867315186228,
                    "Fostering hope and education for every child."
            )));
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_donate, container, false);
        Spinner pickupSpinner = view.findViewById(R.id.pickupOptionSpinner);

        if (pickupSpinner == null) {
            Toast.makeText(requireContext(), "pickupOptionSpinner not found in fragment_donate layout!", Toast.LENGTH_SHORT).show();
            return view;
        }
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.pickup_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pickupSpinner.setAdapter(adapter);
        pickupSpinner.setSelection(0);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Example: Go back to previous fragment
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }
        );
        getParentFragmentManager().setFragmentResultListener("chipKey", this, (requestKey, result) -> {
            String chipId = result.getString("chipToSelect");
            if ("nearby".equals(chipId)) {
                view.post(() -> {
                    Chip chip = view.findViewById(R.id.chip_nearby);
                    if (chip != null) {
                        chip.setChecked(true);
                        chip.callOnClick();}
                });
            }
        });
        // Initialize UI Elements
        mainContentLayout = view.findViewById(R.id.mainContentLayout);
        orphanageListScreen = view.findViewById(R.id.orphanageListScreen);
        layout = view.findViewById(R.id.layout1);
        donationDetailScreen = view.findViewById(R.id.donationDetailScreen);
        donationHeader = view.findViewById(R.id.donationHeader);
        donationContentContainer = view.findViewById(R.id.donationContentContainer);
        orphanagesContainer = view.findViewById(R.id.orphanagesContainer);

        locationPopup = view.findViewById(R.id.locationPopup);
        allowLocationButton = view.findViewById(R.id.allowLocationButton);
        denyLocationButton = view.findViewById(R.id.denyLocationButton);
        backToMainButton = view.findViewById(R.id.backToMainButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        // Set Click Listeners for location popup
        allowLocationButton.setOnClickListener(v-> requestLocationPermission());
        denyLocationButton.setOnClickListener(v -> hideLocationPopup());
        backToMainButton.setOnClickListener(v -> {
            orphanageListScreen.setVisibility(View.GONE);
            mainContentLayout.setVisibility(View.VISIBLE);
        });
        // Initialize Location Callback for ongoing updates if needed
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;
                    Log.d("Location", "Current Location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                    // If you need real-time updates for nearby places, process here
                }
            }
        };
        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        searchView.setQueryHint("Search orphanages or type...");
        // Delay customizations until view is properly initialized
        searchView.post(() -> {
            EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            Drawable drawableRight = ContextCompat.getDrawable(requireContext(), R.drawable.outline_search_24); // your drawable
            searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null);
            if (searchEditText != null) {
                searchEditText.setTextColor(Color.BLACK);
                searchEditText.setHintTextColor(Color.GRAY);
                searchEditText.setTextSize(17f);
            }
        });
        // Now it's safe to use findViewById
        ChipGroup chipGroup = view.findViewById(R.id.chipGroup);
        Chip chipFood = view.findViewById(R.id.chip_nearby);
        chipFood.setChecked(true);

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if (checkedId != View.NO_ID) {
                    Chip selectedChip = group.findViewById(checkedId);
                    String selectedText = selectedChip.getText().toString();
                    if(selectedText.equals("📍 NearBy  ")){
                        handleDonationTypeClick("essentials");
                        requestLocationPermission();
                    } else if (selectedText.equals(" Food")) {
                        handleDonationTypeClick("food");
                        requestLocationPermission();
                    }else if (selectedText.equals(" Clothes")) {
                        handleDonationTypeClick("clothes");
                        requestLocationPermission();
                    }else if (selectedText.equals(" Money")) {
                        handleDonationTypeClick("funds");
                        requestLocationPermission();
                    }
                    Toast.makeText(getContext(), "Selected: " + selectedText, Toast.LENGTH_SHORT).show();
                }
            }
        });
        getAndDisplayNearbyOrphanages();
    }
    private void fetchOrphanagesFromFirebase(Location location) {

        Log.d("FIREBASE_FETCH", "Started fetching orphanages...");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child("Orphanage");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        String uid = child.getKey();
                        String name = child.child("orphanageName").getValue(String.class);
                        String address = child.child("orphanageAddress").getValue(String.class);
                        Double lat = child.child("latitude").getValue(Double.class);
                        Double lon = child.child("longitude").getValue(Double.class);
                        Log.d("FIREBASE_FETCH", "Fetched values - name: " + name + ", address: " + address + ", lat: " + lat + ", lon: " + lon);
                        if (name != null && address != null && lat != null && lon != null) {
                            String description = "Serving the community with care.";
                            Orphanage o = new Orphanage(name, address, lat, lon, description);
                            o.userId = child.getKey();
                            if (!isDuplicate(o)) {
                                allOrphanages.add(o);
                                Log.d("FIREBASE_FETCH", "Added: " + name);
                            } else {
                                Log.d("FIREBASE_FETCH", "Duplicate skipped: " + name);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseError", "Error parsing orphanage: " + e.getMessage());
                    }
                }
                Log.d("FIREBASE_FETCH", "Calling displayNearbyOrphanages after fetch.");
                if (location != null) {
                    displayNearbyOrphanages(location);
                } else {
                    displayAllOrphanagesFallback();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "DB error: " + error.getMessage());
            }
        });
    }
    private boolean isDuplicate(Orphanage newOrphanage) {
        for (Orphanage existing : allOrphanages) {
            if (existing.name.equalsIgnoreCase(newOrphanage.name)
                    && existing.address.equalsIgnoreCase(newOrphanage.address)) {
                return true; // It's a duplicate
            }
        }
        return false; // It's new
    }


    private void handleDonationTypeClick(String type) {
        selectedDonationType = type;
        layout.setVisibility(GONE);
        orphanageListScreen.setVisibility(VISIBLE);
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            showLocationPopup();
        }
    }
    private void showLocationPopup() {
        mainContentLayout.setVisibility(View.GONE);
        locationPopup.setVisibility(VISIBLE);
    }
    private void hideLocationPopup() {
        locationPopup.setVisibility(View.GONE);
        mainContentLayout.setVisibility(VISIBLE); // Go back to main screen if denied
    }
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            hideLocationPopup();
            getAndDisplayNearbyOrphanages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hideLocationPopup();
                getAndDisplayNearbyOrphanages();
            } else {
                Toast.makeText(requireActivity(), "Location permission denied. Cannot find nearby orphanages.", Toast.LENGTH_LONG).show();
                showLocationPopup();
            }
        }
    }

    private void getAndDisplayNearbyOrphanages() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireActivity(), "Location permission not granted.", Toast.LENGTH_SHORT).show();
            hideLocationPopup(); // Ensure popup is hidden
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Log.d("Location", "Last known location: " + location.getLatitude() + ", " + location.getLongitude());
                    fetchOrphanagesFromFirebase(location);
                } else {
                    Log.w("Location", "Last known location is null. Requesting new updates.");
                    // If last location is null, request location updates
                    requestLocationUpdates();
                    // For demo, if no location, show all hardcoded orphanages
                    displayAllOrphanagesFallback();
                }
            }
        });
        showOrphanageListScreen();
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void displayNearbyOrphanages(Location userLocation) {
        orphanagesContainer.removeAllViews(); // Clear previous list
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        boolean foundOrphanage = false;

        for (Orphanage orphanage : allOrphanages) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                    orphanage.latitude, orphanage.longitude, results);
            float distanceInMeters = results[0];
            double distanceInMiles = distanceInMeters * 0.000621371; // Convert meters to miles

            // Display orphanages within, say, 10 miles radius
            if (distanceInMiles < 100) { // Increased radius for demo as locations are hardcoded
                foundOrphanage = true;
                View orphanageCard = inflater.inflate(R.layout.item_orphanage_card, orphanagesContainer, false);
                TextView nameTv = orphanageCard.findViewById(R.id.orphanageName);
                TextView addressTv = orphanageCard.findViewById(R.id.orphanageAddress);
                TextView descTv = orphanageCard.findViewById(R.id.orphanageDescription);
                Button selectBtn = orphanageCard.findViewById(R.id.selectOrphanageButton);

                nameTv.setText(orphanage.name);
                addressTv.setText(String.format("%s (%.1f miles away)", orphanage.address, distanceInMiles));
                descTv.setText(orphanage.description);

                selectBtn.setOnClickListener(v -> {
                    selectedOrphanageName = orphanage.name;
                    hideOrphanageListScreen();
                    renderDonationDetailsScreen();
                    showDonationDetailScreen();
                });
                orphanagesContainer.addView(orphanageCard);
            }
        }

        if (!foundOrphanage) {
            TextView noOrphanagesTv = new TextView(requireActivity());
            noOrphanagesTv.setText("No orphanages found nearby. Please try again later or adjust your location settings.");
            noOrphanagesTv.setTextSize(16);
            noOrphanagesTv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
            noOrphanagesTv.setPadding(16, 16, 16, 16);
            noOrphanagesTv.setGravity(View.TEXT_ALIGNMENT_CENTER);
            orphanagesContainer.addView(noOrphanagesTv);
        }
    }

    private void displayAllOrphanagesFallback() {
        // Fallback if no location or for broader testing
        orphanagesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        for (Orphanage orphanage : allOrphanages) {
            View orphanageCard = inflater.inflate(R.layout.item_orphanage_card, orphanagesContainer, false);
            TextView nameTv = orphanageCard.findViewById(R.id.orphanageName);
            TextView addressTv = orphanageCard.findViewById(R.id.orphanageAddress);
            TextView descTv = orphanageCard.findViewById(R.id.orphanageDescription);
            Button selectBtn = orphanageCard.findViewById(R.id.selectOrphanageButton);

            nameTv.setText(orphanage.name);
            addressTv.setText(orphanage.address + " (Distance unknown)"); // No distance without location
            descTv.setText(orphanage.description);

            selectBtn.setOnClickListener(v -> {
                selectedOrphanageName = orphanage.name;
                hideOrphanageListScreen();
                renderDonationDetailsScreen();
                showDonationDetailScreen();
            });
            orphanagesContainer.addView(orphanageCard);
        }
        if (allOrphanages.isEmpty()) {
            TextView noOrphanagesTv = new TextView(requireActivity());
            noOrphanagesTv.setText("No orphanages available in the list.");
            noOrphanagesTv.setTextSize(16);
            noOrphanagesTv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
            noOrphanagesTv.setPadding(16, 16, 16, 16);
            noOrphanagesTv.setGravity(View.TEXT_ALIGNMENT_CENTER);
            orphanagesContainer.addView(noOrphanagesTv);
        }
    }


    private void showOrphanageListScreen() {
        mainContentLayout.setVisibility(VISIBLE);
        locationPopup.setVisibility(GONE); // Ensure popup is hidden
        orphanageListScreen.setVisibility(VISIBLE);
    }

    private void hideOrphanageListScreen() {
        orphanageListScreen.setVisibility(GONE);
    }

    private void showDonationDetailScreen() {
        mainContentLayout.setVisibility(GONE);
        orphanageListScreen.setVisibility(GONE);
        donationDetailScreen.setVisibility(VISIBLE);
    }

    private void renderDonationDetailsScreen() {
        donationHeader.setText("Donate " + capitalize(selectedDonationType));
        donationContentContainer.removeAllViews(); // Clear previous content
        Orphanage selected = null;
        for (Orphanage o : allOrphanages) {
            if (o.name.equals(selectedOrphanageName)) {
                selected = o;
                break;
            }
        }
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        Orphanage finalSelected = selected;
        if ("funds".equals(selectedDonationType)) {
            View fundsLayout = inflater.inflate(R.layout.layout_donate_funds, donationContentContainer, false);

            TextView infoText = fundsLayout.findViewById(R.id.infoText);
            infoText.setText("You are donating funds to " + selectedOrphanageName.toUpperCase() + ".");

            EditText amountInput = fundsLayout.findViewById(R.id.amountInput);
            Button btn10 = fundsLayout.findViewById(R.id.btn10);
            Button btn25 = fundsLayout.findViewById(R.id.btn25);
            Button btn50 = fundsLayout.findViewById(R.id.btn50);
            Button btn100 = fundsLayout.findViewById(R.id.btn100);
            Button donateNowButton = fundsLayout.findViewById(R.id.donateNowButton);

            btn10.setOnClickListener(v -> amountInput.setText("10"));
            btn25.setOnClickListener(v -> amountInput.setText("25"));
            btn50.setOnClickListener(v -> amountInput.setText("50"));
            btn100.setOnClickListener(v -> amountInput.setText("100"));

            donateNowButton.setOnClickListener(v -> {
                String amount = amountInput.getText().toString();
                if (amount.isEmpty()) {
                    Toast.makeText(requireActivity(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (finalSelected != null && finalSelected.userId != null) {
                    DatabaseReference notifRef = FirebaseDatabase.getInstance()
                            .getReference("Notifications")
                            .child(finalSelected.userId).child("History");

                    String notifId = notifRef.push().getKey();

                    notifRef.child(notifId).setValue("$" + amount );
                }

                // Simulate payment processing
                Toast.makeText(requireActivity(), "Donating $" + amount + " to " + selectedOrphanageName, Toast.LENGTH_LONG).show();
                // Optionally go back to main or show a thank you screen
                donationDetailScreen.setVisibility(GONE);
                mainContentLayout.setVisibility(VISIBLE);
            });
            donationContentContainer.addView(fundsLayout);

        } else {
            View otherDonationLayout = inflater.inflate(R.layout.layout_donate_other, donationContentContainer, false);

            TextView infoText = otherDonationLayout.findViewById(R.id.infoText);
            infoText.setText("You have selected to donate " + capitalize(selectedDonationType) + " to " + selectedOrphanageName.toUpperCase() + ".");

            EditText descriptionInput = otherDonationLayout.findViewById(R.id.donationDescriptionInput);
            Button confirmDonationButton = otherDonationLayout.findViewById(R.id.confirmDonationButton);

            confirmDonationButton.setOnClickListener(v -> {
                String description = descriptionInput.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(requireActivity(), "Please describe your donation.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (finalSelected != null && finalSelected.userId != null) {
                    DatabaseReference notifRef = FirebaseDatabase.getInstance()
                            .getReference("Notifications")
                            .child(finalSelected.userId).child("History");

                    String notifId = notifRef.push().getKey();

                    notifRef.child(notifId).setValue( selectedDonationType+" - "+description);
                }
                Toast.makeText(requireActivity(), "Confirmed donation of " + description + " (" + selectedDonationType + ") to " + selectedOrphanageName, Toast.LENGTH_LONG).show();
                // Optionally go back to main or show a thank you screen
                donationDetailScreen.setVisibility(GONE);
                mainContentLayout.setVisibility(VISIBLE);
            });
            donationContentContainer.addView(otherDonationLayout);
        }
    }
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    @Override
    public void onResume() {
        super.onResume();
        // If app comes back from background and permission was granted, restart location updates
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates when activity is paused to save battery
        stopLocationUpdates();
    }
}