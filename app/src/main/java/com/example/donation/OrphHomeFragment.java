package com.example.donation;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrphHomeFragment extends Fragment {
    ImageView backgroundHome;
    CircleImageView orphProfile;
    private List<Product> products;
    private Map<Product, Integer> cart;
    TextView headerText;
    public OrphHomeFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orph_home, container, false);
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

        backgroundHome=view.findViewById(R.id.background_home);
        orphProfile=view.findViewById(R.id.orph_profile);
        headerText=view.findViewById(R.id.headertxt);
        CardView cardView = view.findViewById(R.id.orphanage_card);
        LinearLayout expandableSection = view.findViewById(R.id.expandable_section);

        cardView.setOnClickListener(v -> {
            if (expandableSection.getVisibility() == View.GONE) {
                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                expandableSection.setVisibility(View.VISIBLE);
            } else {
                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                expandableSection.setVisibility(View.GONE);
            }
        });
        RecyclerView horizontalRecyclerView = view.findViewById(R.id.eventRecyclerView);
        horizontalRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        cart = new LinkedHashMap<>();
        products = new ArrayList<>();
        products.add(new Product(3, "Handmade Greeting Cards", "Beautifully crafted cards for any occasion, with unique designs.", 12.00, R.drawable.d));
        products.add(new Product(1, "Beaded Bracelet", "Hand-strung bracelet with colorful beads, a charming accessory.", 8.00, R.drawable.g));
        products.add(new Product(2, "Knitted Scarf", "Warm and cozy hand-knitted scarf, perfect for chilly evenings.", 18.50,R.drawable.b));
        products.add(new Product(5, "Wooden Trinket Box", "Intricately carved wooden box, ideal for storing small treasures.", 35.00, R.drawable.c));
        products.add(new Product(6, "Embroidered Pouch", "Small fabric pouch with delicate embroidery, versatile for everyday use.", 10.00, R.drawable.f));
        products.add(new Product(7, "Hand-Painted Canvas", "Vibrant acrylic painting on canvas, expressing dreams and hopes.", 25.00, R.drawable.a));
        products.add(new Product(8, "Baked Cookies (Pack of 6)", "Delicious homemade cookies, baked with love and care.", 9.50, R.drawable.h));

        ProductAdapterHorizontal adapter = new ProductAdapterHorizontal(getContext(), products);
        horizontalRecyclerView.setAdapter(adapter);

    }
}