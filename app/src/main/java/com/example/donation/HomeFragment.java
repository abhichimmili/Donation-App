package com.example.donation;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerCarousel;
    private CarouselAdapter carouselAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());

    private int black;


    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int currentPosition = viewPagerCarousel.getCurrentItem();
            int itemCount = carouselAdapter.getItemCount();
            int nextPosition = (currentPosition + 1) % itemCount;
            viewPagerCarousel.setCurrentItem(nextPosition, true);
            sliderHandler.postDelayed(this, 3000); // Scroll every 3 seconds
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        black = ContextCompat.getColor(requireContext(), R.color.black);


        View root = inflater.inflate(R.layout.fragment_home, container, false);

        viewPagerCarousel = root.findViewById(R.id.viewPagerCarousel);
        GridLayout cardGrid = root.findViewById(R.id.cardGrid);

        int[] homeimg = {
                R.drawable.donation, R.drawable.find,
                R.drawable.voulnteer, R.drawable.purchase
        };

        String[] titles = {
                "Donate Now", "Find Nearby", "Become a Volunteer", "Purchase Items"
        };

        String[] descriptions = {
                "Donate now to make a difference. Your contribution helps provide food, clothing, and essential support to those in urgent need.",
                "Find nearby helping centers including shelters, donation points, and community aid centers where you can seek or provide help.",
                "Join as a volunteer and actively participate in outreach programs, distribution drives, and community support efforts to help others.",
                "Discover and purchase quality handmade items created by shelter residents. Your support helps them build skills and independence."

        };
        int[] cardColors = {
                R.color.colorDonate,
                R.color.colorPurchase,
                R.color.colorVolunteer,
                R.color.colorEvents
        };

        for (int i = 0; i < 4; i++) {
            View card = getLayoutInflater().inflate(R.layout.card_item, cardGrid, false);
            CardView cardView = (CardView) card;
            int bgColor = ContextCompat.getColor(getContext(), cardColors[i]);
            cardView.setCardBackgroundColor(bgColor);

            ImageView cardImage = card.findViewById(R.id.cardImage);
            TextView cardTitle = card.findViewById(R.id.cardTitle);
            TextView cardDescription = card.findViewById(R.id.cardDescription);
            cardTitle.setTextSize(21);
            cardTitle.setTextColor(black);
            cardDescription.setTextSize(16);
            cardDescription.setTextColor(black);
            cardImage.setImageResource(homeimg[i]);
            cardTitle.setText(titles[i]);
            cardDescription.setText(descriptions[i]);

            int finalI = i;
            card.setOnClickListener(v -> {
                BottomNavigationView navView = requireActivity().findViewById(R.id.bottomNavigationView); // Replace with your BottomNavView ID
                switch (finalI) {
                    case 0:
                        navView.setSelectedItemId(R.id.donation);

//                        if (getActivity() instanceof MainActivity) {
//                            try {
//                                ((MainActivity) getActivity()).navigateToFragment(new DonateFragment(), R.id.donation);
//                            } catch (Exception e) {
//                                e.printStackTrace(); // log in Logcat
//                            }
//                        }
                        break;
                    case 1:
                        DonateFragment donateFragment = new DonateFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("selected_chip", "nearby");
                        donateFragment.setArguments(bundle);

                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, donateFragment)
                                .addToBackStack(null)
                                .commit();
                        break;
                    case 2:
                        // volunteer action

                        break;
                    case 3:
                        // purchase iteams // Replace with your BottomNavView ID
                        navView.setSelectedItemId(R.id.purchase);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + finalI);
                }
            });

            cardGrid.addView(card);
        }

        //Marquee Text
        TextView marqueeText = root.findViewById(R.id.textMarquee);
        marqueeText.setSelected(true);

        // Sample images (replace with your own drawable resources)
        List<Integer> images = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3,
                R.drawable.banner4,
                R.drawable.banner5
        );
        ImageView btnLeft = root.findViewById(R.id.btnLeftArrow);
        ImageView btnRight = root.findViewById(R.id.btnRightArrow);
        ViewPager2 viewPager = root.findViewById(R.id.viewPagerCarousel);

        btnLeft.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current > 0) {
                viewPager.setCurrentItem(current - 1, true);
            }
        });

        btnRight.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            int last = viewPager.getAdapter().getItemCount() - 1;
            if (current < last) {
                viewPager.setCurrentItem(current + 1, true);
            }
        });


        carouselAdapter = new CarouselAdapter(images);
        viewPagerCarousel.setAdapter(carouselAdapter);
        // Start auto-scroll
        sliderHandler.postDelayed(sliderRunnable, 3000);

        // Reset auto-scroll timer on user interaction
        viewPagerCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
        TabLayout tabLayout = root.findViewById(R.id.tabLayoutDots);
        new TabLayoutMediator(tabLayout, viewPagerCarousel,(tab, position) -> {
            // no text needed, just dots
        }).attach();
        return root;
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
    }
    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}
