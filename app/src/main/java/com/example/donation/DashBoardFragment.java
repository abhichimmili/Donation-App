package com.example.donation;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashBoardFragment extends Fragment {

    public DashBoardFragment() {
    }
    RadioButton radioDonations, radioPurchases;
    TextView summaryText, amountText;
    ListView historyList;

    String[] donationHistory ;
    String[] purchaseHistory = {"Purchase - April 20 - $50", "Purchase - April 16 - $100", "Purchase - April 12 - $75"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dash_board, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Actually go back
                        requireActivity().onBackPressed();
                    }
                }
        );
        String userId = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("userId", null);

        if (userId == null) {
            Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(userId)
                .child("History");

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> historyList = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String entry = child.getValue(String.class);
                    if (entry != null) {
                        historyList.add("Donation - "+entry);
                    }
                }
                Collections.reverse(historyList);
                donationHistory = historyList.toArray(new String[0]);
                loadDonations();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching donation history", error.toException());
            }
        });
        radioDonations = view.findViewById(R.id.radioDonations);
        radioPurchases = view.findViewById(R.id.radioPurchases);
        summaryText = view.findViewById(R.id.summaryText);
        amountText = view.findViewById(R.id.amountText);
        historyList = view.findViewById(R.id.historyList);
        radioDonations.setOnClickListener(v -> loadDonations());
        radioPurchases.setOnClickListener(v -> loadPurchases());
    }
    private void loadDonations() {
        summaryText.setText("Donated");
        amountText.setText("$1,200");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, donationHistory) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.black)); // Change to your desired color
                return view;
            }
        };
        historyList.setAdapter(adapter);
    }
    private void loadPurchases() {
        summaryText.setText("Purchased");
        amountText.setText("$225");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, purchaseHistory);
        historyList.setAdapter(adapter);
    }
}