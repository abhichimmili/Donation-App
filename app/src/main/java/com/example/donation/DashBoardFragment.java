package com.example.donation;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class DashBoardFragment extends Fragment {

    public DashBoardFragment() {
    }
    RadioButton radioDonations, radioPurchases;
    TextView summaryText, amountText;
    ListView historyList;

    String[] donationHistory = {"Donation - April 19 - $200", "Donation - April 15 - $150", "Donation - April 10 - $350"};
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
        radioDonations = view.findViewById(R.id.radioDonations);
        radioPurchases = view.findViewById(R.id.radioPurchases);
        summaryText = view.findViewById(R.id.summaryText);
        amountText = view.findViewById(R.id.amountText);
        historyList = view.findViewById(R.id.historyList);

        loadDonations();

        radioDonations.setOnClickListener(v -> loadDonations());
        radioPurchases.setOnClickListener(v -> loadPurchases());
    }
    private void loadDonations() {
        summaryText.setText("Donated");
        amountText.setText("$1,200");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, donationHistory);
        historyList.setAdapter(adapter);
    }
    private void loadPurchases() {
        summaryText.setText("Purchased");
        amountText.setText("$225");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, purchaseHistory);
        historyList.setAdapter(adapter);
    }
}