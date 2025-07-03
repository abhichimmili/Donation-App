package com.example.donation;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PurchaseFragment extends Fragment implements ProductAdapter.OnAddToCartClickListener {
    private static final String TAG = "OrphanageShop"; // Tag for logging

    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> products;
    private Map<Product, Integer> cart; // Stores Product -> Quantity
    private TextView fabCartItemCount;
    private FloatingActionButton mobileCartButton;
    private Button viewCartButton; // For the header cart button, though FAB is primary

    // References to cart dialog views
    private AlertDialog cartDialog;
    private LinearLayout cartItemsListLayout;
    private TextView cartTotalTextView;
    private Button checkoutButton;
    private TextView emptyCartMessage;


    public PurchaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            Log.e("CRASH", "Uncaught exception", e);
        });

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_purchase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Custom back button behavior
                        Toast.makeText(getContext(), "Back pressed in fragment", Toast.LENGTH_SHORT).show();

                        // Example: Go back to previous fragment
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }
        );
        // Initialize views
        viewCartButton = view.findViewById(R.id.view_cart_button);
        productRecyclerView = view.findViewById(R.id.product_recycler_view);
        fabCartItemCount = view.findViewById(R.id.fab_cart_item_count);
        mobileCartButton = view.findViewById(R.id.mobile_cart_button);

        cart = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order of items added

        // Prepare product data (mock data)
        products = new ArrayList<>();
        products.add(new Product(1, "Beaded Bracelet", "Hand-strung bracelet with colorful beads, a charming accessory.", 8.00, R.drawable.g));
        products.add(new Product(2, "Knitted Scarf", "Warm and cozy hand-knitted scarf, perfect for chilly evenings.", 18.50,R.drawable.b));
        products.add(new Product(3, "Handmade Greeting Cards (Set of 5)", "Beautifully crafted cards for any occasion, with unique designs.", 12.00, R.drawable.d));
        products.add(new Product(4, "Ceramic Mug", "Artisanal ceramic mug, perfect for your favorite hot beverage.", 15.00, R.drawable.e));
        products.add(new Product(5, "Wooden Trinket Box", "Intricately carved wooden box, ideal for storing small treasures.", 35.00, R.drawable.c));
        products.add(new Product(6, "Embroidered Pouch", "Small fabric pouch with delicate embroidery, versatile for everyday use.", 10.00, R.drawable.f));
        products.add(new Product(7, "Hand-Painted Canvas", "Vibrant acrylic painting on canvas, expressing dreams and hopes.", 25.00, R.drawable.a));
        products.add(new Product(8, "Baked Cookies (Pack of 6)", "Delicious homemade cookies, baked with love and care.", 9.50, R.drawable.h));

        // Setup RecyclerView
        productRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns
        productAdapter = new ProductAdapter(getContext(), products, this);
        productRecyclerView.setAdapter(productAdapter);

        // Set up click listener for the Floating Action Button (FAB)
        mobileCartButton.setOnClickListener(v -> {
            Log.d(TAG, "mobileCartButton clicked.");
            showCartDialog();
        });
        // Also for the header button, if it becomes visible (e.g., on tablet layouts)
        viewCartButton.setOnClickListener(v -> {
            Log.d(TAG, "viewCartButton clicked.");
            showCartDialog();
        });

        updateCartUI(); // Initial update of cart count
    }
    @Override
    public void onAddToCartClick(Product product) {
        Log.d(TAG, "onAddToCartClick: Adding " + product.getName() + " to cart.");
        // Add or update item in cart
        if (cart.containsKey(product)) {
            cart.put(product, cart.get(product) + 1);
        } else {
            cart.put(product, 1);
        }
        updateCartUI();
        showMessageBox("Item Added!", product.getName() + " has been added to your cart.");
    }

    private void updateCartUI() {
        int totalItems = 0;
        for (int quantity : cart.values()) {
            totalItems += quantity;
        }
        fabCartItemCount.setText(String.valueOf(totalItems));
        // You might want to update the header button text here as well
        viewCartButton.setText(String.format(Locale.getDefault(), "View Cart (%d)", totalItems));

        // Optionally, hide the count bubble if 0 items
        if (totalItems == 0) {
            fabCartItemCount.setVisibility(View.GONE);
        } else {
            fabCartItemCount.setVisibility(View.VISIBLE);
        }

        // If cart dialog is already open, refresh its contents
        if (cartDialog != null && cartDialog.isShowing()) {
            refreshCartDialogContent();
        }
    }
    private void showCartDialog() {
        Log.d(TAG, "showCartDialog: Creating/showing cart dialog.");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cart, null);
        builder.setView(dialogView);

        // Initialize cart dialog views once
        cartItemsListLayout = dialogView.findViewById(R.id.cart_items_list);
        cartTotalTextView = dialogView.findViewById(R.id.cart_total);
        checkoutButton = dialogView.findViewById(R.id.checkout_button);
        emptyCartMessage = dialogView.findViewById(R.id.empty_cart_message);
        AppCompatButton closeCartButton = dialogView.findViewById(R.id.close_cart_modal);


        // Setup listeners for dialog
        checkoutButton.setOnClickListener(v -> {
            Log.d(TAG, "Checkout button clicked.");
            if (!cart.isEmpty()) {

                showMessageBox("Checkout", "Thank you for your purchase! (This is a demo, no actual transaction occurs).");
                cart.clear(); // Clear cart after "checkout"
                updateCartUI(); // Update UI after clearing cart
                if (cartDialog != null && cartDialog.isShowing()) {
                    cartDialog.dismiss(); // Close cart dialog
                }
            } else {
                showMessageBox("Cart Empty", "Your cart is empty. Please add items before checking out.");
            }
        });

        closeCartButton.setOnClickListener(v -> {
            Log.d(TAG, "Close cart modal button clicked.");
            if (cartDialog != null && cartDialog.isShowing()) {
                cartDialog.dismiss();
            }
        });

        cartDialog = builder.create();
        cartDialog.setCanceledOnTouchOutside(true); // Allow dismissing by tapping outside

        // Populate cart content initially
        refreshCartDialogContent();

        cartDialog.show();
    }

    // New method to refresh the content of an *existing* cart dialog
    private void refreshCartDialogContent() {
        Log.d(TAG, "refreshCartDialogContent: Refreshing cart items and total.");
        cartItemsListLayout.removeAllViews(); // Clear existing items

        double totalCost = 0;

        if (cart.isEmpty()) {
            emptyCartMessage.setVisibility(View.VISIBLE);
            cartItemsListLayout.setVisibility(View.GONE);
        } else {
            emptyCartMessage.setVisibility(View.GONE);
            cartItemsListLayout.setVisibility(View.VISIBLE);
            for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                double itemTotal = product.getPrice() * quantity;
                totalCost += itemTotal;

                View cartItemView = getLayoutInflater().inflate(R.layout.item_cart_dialog, cartItemsListLayout, false);
                TextView itemName = cartItemView.findViewById(R.id.cart_item_name);
                TextView itemPriceTotal = cartItemView.findViewById(R.id.cart_item_price); // Renamed for clarity
                NumberPicker quantityPicker = cartItemView.findViewById(R.id.cart_item_quantity_picker);
                AppCompatButton removeButton = cartItemView.findViewById(R.id.cart_item_remove_button);

                itemName.setText(product.getName());
                // Display price per item and total for that line item
                itemPriceTotal.setText(String.format(Locale.getDefault(), "$%.2f x %d = $%.2f", product.getPrice(), quantity, itemTotal));

                quantityPicker.setMinValue(1);
                quantityPicker.setMaxValue(99); // Set a reasonable max quantity
                quantityPicker.setValue(quantity);
                quantityPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                    Log.d(TAG, "Quantity changed for " + product.getName() + " to " + newVal);
                    cart.put(product, newVal);
                    updateCartUI(); // This will also call refreshCartDialogContent if dialog is open
                });

                removeButton.setOnClickListener(v -> {
                    Log.d(TAG, "Removing " + product.getName() + " from cart.");
                    cart.remove(product);
                    updateCartUI(); // This will also call refreshCartDialogContent if dialog is open
                });
                cartItemsListLayout.addView(cartItemView);
            }
        }
        cartTotalTextView.setText(String.format(Locale.getDefault(), "Total: $%.2f", totalCost));
        checkoutButton.setEnabled(!cart.isEmpty());
        checkoutButton.setAlpha(cart.isEmpty() ? 0.5f : 1.0f); // Visually disable
    }
    private void showMessageBox(String title, String message) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null);

        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        Button okButton = dialogView.findViewById(R.id.ok_button);

        titleTextView.setText(title);
        messageTextView.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        });

        okButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

}