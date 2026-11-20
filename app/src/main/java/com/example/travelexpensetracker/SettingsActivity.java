package com.example.travelexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelexpensetracker.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * SettingsActivity allows users to manage app data, preferences, and view info.
 * Includes a safety mechanism for clearing all local database records.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Edge-to-Edge System Bar Handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            
            View appBar = findViewById(R.id.appBarLayout);
            if (appBar != null) {
                appBar.setPadding(0, systemBars.top, 0, 0);
            }

            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }
            return insets;
        });

        // Toolbar Setup
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Feature Placeholder Click Listeners
        findViewById(R.id.btn_currency_mgmt).setOnClickListener(v -> 
            Toast.makeText(this, "Currency Management coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btn_category_mgmt).setOnClickListener(v -> 
            Toast.makeText(this, "Category Customization coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btn_backup).setOnClickListener(v -> 
            Toast.makeText(this, "Exporting to CSV...", Toast.LENGTH_SHORT).show());

        // Destructive Data Management
        findViewById(R.id.btn_clear_data).setOnClickListener(v -> showClearDataConfirmation());

        // Navigation Logic
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_summary) {
                Intent intent = new Intent(this, GlobalSummaryActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    /**
     * Resets the navigation state when returning to this screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_settings);
        }
    }

    /**
     * Displays a warning dialog before wiping the SQLite database.
     */
    private void showClearDataConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to permanently delete all trips and expenses? This action cannot be undone.")
                .setPositiveButton("Clear Everything", (dialog, which) -> {
                    // Writable access for deletion
                    DatabaseHelper dbHelper = new DatabaseHelper(this);
                    dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_EXPENSES);
                    dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_TRIPS);
                    Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null) // Dismiss dialog
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
