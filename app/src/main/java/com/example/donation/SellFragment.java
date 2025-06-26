package com.example.donation; // Replace with your actual package name

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SellFragment extends Fragment {
    private static final int REQUEST_IMAGE_PICK = 1001;
    private ImageView imagePlaceholder;
    private SharedPreferences prefs;
    public SellFragment() {
    }
    private EditText titleEditText;
    private EditText priceEditText;
    private EditText descriptionEditText;
    private Button addPhotoButton;
    private Button sellOnlineButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sell, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Clear URI from SharedPreferences
                SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                prefs.edit().remove("image_uri").apply();
                // Optional: Clear the ImageView
                ImageView imagePlaceholder = requireView().findViewById(R.id.image_placeholder);
                imagePlaceholder.setImageDrawable(null); // or setImageResource(R.drawable.placeholder)

                // Actually go back
                requireActivity().onBackPressed();
            }
        });

        titleEditText = view.findViewById(R.id.edit_text_title);
        priceEditText = view.findViewById(R.id.edit_text_price);
        descriptionEditText = view.findViewById(R.id.edit_text_description);
        addPhotoButton = view.findViewById(R.id.button_add_photo);
        sellOnlineButton = view.findViewById(R.id.button_sell_online);

        prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        imagePlaceholder = view.findViewById(R.id.image_placeholder);

        String uriStr = prefs.getString("image_uri", null);
        if (uriStr != null) {
            try {
                Uri imageUri = Uri.parse(uriStr);

                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                requireContext().getContentResolver().takePersistableUriPermission(imageUri, takeFlags);

                imagePlaceholder.setImageURI(imageUri);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }


        addPhotoButton.setOnClickListener(v -> openGallery());
        // Set an OnClickListener for the "SELL ONLINE" button
        sellOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve text from the input fields
                String title = titleEditText.getText().toString().trim();
                String price = priceEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();

                // Perform validation (basic example)
                if (title.isEmpty()) {
                    titleEditText.setError("Title cannot be empty");
                    Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (price.isEmpty()) {
                    priceEditText.setError("Price cannot be empty");
                    Toast.makeText(requireContext(), "Please enter a price", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (description.isEmpty()) {
                    descriptionEditText.setError("Description cannot be empty");
                    Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If all fields are filled, you can proceed with selling logic
                // For demonstration, we'll show a Toast with the collected data.
                String message = "Item ready to sell:\n" +
                        "Title: " + title + "\n" +
                        "Price: $" + price + "\n" +
                        "Description: " + description;
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                // In a real app, you would send this data to a server, database, etc.
                // Example:
                // uploadItem(title, price, description);
            }
        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            requireContext().getContentResolver().takePersistableUriPermission(
                    imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            prefs.edit().putString("image_uri", imageUri.toString()).apply();

            imagePlaceholder.setImageURI(imageUri);
        }
    }
}
