package com.example.travelexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelexpensetracker.adapter.TripAdapter;
import com.example.travelexpensetracker.database.DatabaseHelper;
import com.example.travelexpensetracker.model.Trip;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MainActivity serves as the app's primary dashboard.
 * It displays the list of all trips and provides a search/filter interface.
 */
public class MainActivity extends AppCompatActivity implements TripAdapter.OnTripActionListener {

    private RecyclerView recyclerView;
    private TripAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView textNoTrips;
    private EditText editSearch;
    private Button btnAddTrip;
    private List<Trip> allTrips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display (drawing behind status bar/navigation bar)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Handle System Insets to ensure the UI doesn't overlap with the notch or system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Reset vertical padding of root view and apply it specifically to header/footer
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            
            View header = findViewById(R.id.text_header_title);
            if (header != null) {
                // Add extra top padding to the header title to push it below the notch
                header.setPadding(header.getPaddingLeft(), systemBars.top + 16, header.getPaddingRight(), header.getPaddingBottom());
            }

            if (bottomNav != null) {
                // Add bottom padding to nav bar to sit flush with the screen bottom
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }
            return insets;
        });

        // Initialize Database and UI elements
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recycler_trips);
        textNoTrips = findViewById(R.id.text_no_trips);
        editSearch = findViewById(R.id.edit_search);
        btnAddTrip = findViewById(R.id.btn_add_trip);

        // Configure RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TripAdapter(new ArrayList<>(), this, dbHelper);
        recyclerView.setAdapter(adapter);

        // Button Click: Navigate to Add Trip screen
        btnAddTrip.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTripActivity.class);
            startActivity(intent);
        });

        // Setup Live Search filtering
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTrips(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup Bottom Navigation logic
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true; // Already on Home
            } else if (id == R.id.nav_summary) {
                // Navigate to Overall Analytics
                Intent intent = new Intent(this, GlobalSummaryActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                // Navigate to Settings
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    /**
     * Refreshes the trip list every time the user returns to this screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home); // Reset highlight to Home
        }
        loadTrips();
    }

    /**
     * Fetches all trips from SQLite and updates the filter view.
     */
    private void loadTrips() {
        allTrips = dbHelper.getAllTrips();
        filterTrips(editSearch.getText().toString());
    }

    /**
     * Filters the trip list based on the user's search query.
     */
    private void filterTrips(String query) {
        List<Trip> filteredTrips;
        if (query.isEmpty()) {
            filteredTrips = allTrips;
        } else {
            // Case-insensitive destination matching
            filteredTrips = allTrips.stream()
                    .filter(t -> t.getDestination().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Manage Empty States
        if (filteredTrips.isEmpty()) {
            textNoTrips.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textNoTrips.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateData(filteredTrips);
        }
    }

    /**
     * Callback: User clicked "Expenses" on a trip card.
     */
    @Override
    public void onExpensesClick(Trip trip) {
        if (trip != null) {
            Log.d("MainActivity", "onExpensesClick: Destination=" + trip.getDestination());
            Intent intent = new Intent(MainActivity.this, TripDetailsActivity.class);
            intent.putExtra("trip", trip);
            startActivity(intent);
        }
    }

    /**
     * Callback: User clicked "Edit" on a trip card.
     */
    @Override
    public void onEditClick(Trip trip) {
        if (trip != null) {
            Intent intent = new Intent(MainActivity.this, AddEditTripActivity.class);
            intent.putExtra("trip", trip);
            startActivity(intent);
        }
    }

    /**
     * Callback: User clicked "Delete" on a trip card.
     */
    @Override
    public void onDeleteClick(Trip trip) {
        dbHelper.deleteTrip(trip.getId());
        loadTrips(); // Refresh UI
        Toast.makeText(this, "Trip deleted", Toast.LENGTH_SHORT).show();
    }
}
