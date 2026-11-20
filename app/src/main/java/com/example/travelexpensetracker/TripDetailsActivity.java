package com.example.travelexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelexpensetracker.adapter.ExpenseAdapter;
import com.example.travelexpensetracker.database.DatabaseHelper;
import com.example.travelexpensetracker.model.Expense;
import com.example.travelexpensetracker.model.Trip;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TripDetailsActivity displays the expense history for a specific selected trip.
 * It provides features for sorting expenses and navigating to trip-specific analytics.
 */
public class TripDetailsActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseActionListener {

    private static final String TAG = "TripDetailsActivity";
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private DatabaseHelper dbHelper;
    private Trip trip;
    private Spinner spinnerSort;
    private Button btnAddExpense;
    private List<Expense> currentExpenses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting TripDetailsActivity");
        
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_trip_details);

            // Retrieve the selected Trip object from the Intent
            trip = (Trip) getIntent().getSerializableExtra("trip");
            if (trip == null) {
                Log.e(TAG, "onCreate: Trip data is NULL");
                Toast.makeText(this, "Error: Trip data missing", Toast.LENGTH_SHORT).show();
                finish(); // Exit if no trip context
                return;
            }

            dbHelper = new DatabaseHelper(this);
            
            // UI Initialization
            recyclerView = findViewById(R.id.recycler_expenses);
            spinnerSort = findViewById(R.id.spinner_sort);
            btnAddExpense = findViewById(R.id.btn_add_expense);
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

            // Configure professional "integrated" Toolbar with a back button
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle(trip.getDestination());
                }
            }

            // Handle notch and system navigation bar padding
            View mainRoot = findViewById(R.id.main_trip_details);
            if (mainRoot != null) {
                ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, 0, systemBars.right, 0);
                    View appBar = findViewById(R.id.appBarLayout);
                    if (appBar != null) appBar.setPadding(0, systemBars.top, 0, 0);
                    if (bottomNav != null) bottomNav.setPadding(0, 0, 0, systemBars.bottom);
                    return insets;
                });
            }

            // Setup Expense list RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ExpenseAdapter(currentExpenses, this, dbHelper);
            recyclerView.setAdapter(adapter);

            // Navigate to Add Expense form
            btnAddExpense.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddExpenseActivity.class);
                intent.putExtra("trip_id", trip.getId());
                startActivity(intent);
            });

            // Initialize Sort dropdown
            setupSortSpinner();

            // Bottom Navigation Logic (Home / Summary / Settings)
            if (bottomNav != null) {
                // Ensure the highlight remains on Home as this is part of the Home drill-down flow
                bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                
                bottomNav.setOnNavigationItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_home) {
                        // Return to Dashboard and clear backstack
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        return true;
                    } else if (id == R.id.nav_summary) {
                        // View analytics for THIS specific trip (Pie Chart)
                        Intent intent = new Intent(this, SpendingSummaryActivity.class);
                        intent.putExtra("trip", trip);
                        startActivity(intent);
                        return true;
                    } else if (id == R.id.nav_settings) {
                        Intent intent = new Intent(this, SettingsActivity.class);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                });
            }
            
            Log.d(TAG, "onCreate: Setup complete");

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Crash during initialization", e);
            Toast.makeText(this, "Failed to load expenses screen", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Populates the sorting spinner with available criteria.
     */
    private void setupSortSpinner() {
        String[] options = {"Date", "Category", "Amount"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        // Listen for sort selection changes
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortExpenses(options[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Sorts the current list of expenses based on the user's selection.
     */
    private void sortExpenses(String criterion) {
        if (currentExpenses == null) return;
        
        if ("Date".equals(criterion)) {
            Collections.sort(currentExpenses, (e1, e2) -> e1.getDate().compareTo(e2.getDate()));
        } else if ("Category".equals(criterion)) {
            Collections.sort(currentExpenses, (e1, e2) -> {
                String c1 = e1.getCategoryName() != null ? e1.getCategoryName() : "";
                String c2 = e2.getCategoryName() != null ? e2.getCategoryName() : "";
                return c1.compareTo(c2);
            });
        } else if ("Amount".equals(criterion)) {
            Collections.sort(currentExpenses, (e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()));
        }
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Reloads the expense data from SQLite whenever the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Updating data");
        loadExpenses();
    }

    /**
     * Fetches expenses for the current trip and updates the list adapter.
     */
    private void loadExpenses() {
        if (trip == null || dbHelper == null || adapter == null) return;
        
        try {
            currentExpenses = dbHelper.getExpensesForTrip(trip.getId());
            // Apply current sort after loading
            if (spinnerSort != null && spinnerSort.getSelectedItem() != null) {
                sortExpenses(spinnerSort.getSelectedItem().toString());
            }
            adapter.updateData(currentExpenses, dbHelper);
        } catch (Exception e) {
            Log.e(TAG, "loadExpenses: Error loading from DB", e);
        }
    }

    /**
     * Callback from adapter: User wants to edit a specific expense.
     */
    @Override
    public void onEditExpense(Expense expense) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra("expense", expense);
        startActivity(intent);
    }

    /**
     * Callback from adapter: User wants to delete a specific expense.
     */
    @Override
    public void onDeleteExpense(Expense expense) {
        if (dbHelper != null) {
            dbHelper.deleteExpense(expense.getId());
            loadExpenses(); // Refresh view
            Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the back button in the toolbar.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
