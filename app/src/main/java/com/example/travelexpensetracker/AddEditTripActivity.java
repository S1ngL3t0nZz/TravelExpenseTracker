package com.example.travelexpensetracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelexpensetracker.database.DatabaseHelper;
import com.example.travelexpensetracker.model.Trip;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * AddEditTripActivity provides a form to create a new trip or update an existing one.
 * It features a DatePicker for intuitive date entry.
 */
public class AddEditTripActivity extends AppCompatActivity {

    private TextInputEditText editDestination, editStartDate, editEndDate, editBudget;
    private Button buttonSave;
    private DatabaseHelper dbHelper;
    private Trip existingTrip;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_trip);

        // Handle System Insets to clear notch and system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_add_edit_trip), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            
            View appBar = findViewById(R.id.appBarLayout);
            if (appBar != null) {
                appBar.setPadding(0, systemBars.top, 0, 0);
            }
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        // UI Initialization
        editDestination = findViewById(R.id.edit_destination);
        editStartDate = findViewById(R.id.edit_start_date);
        editEndDate = findViewById(R.id.edit_end_date);
        editBudget = findViewById(R.id.edit_budget);
        buttonSave = findViewById(R.id.button_save_trip);

        // UX Improvement: Date fields open a Picker instead of keyboard entry
        editStartDate.setFocusable(false);
        editStartDate.setClickable(true);
        editEndDate.setFocusable(false);
        editEndDate.setClickable(true);

        editStartDate.setOnClickListener(v -> showDatePicker(editStartDate));
        editEndDate.setOnClickListener(v -> showDatePicker(editEndDate));

        // Toolbar setup with high-visibility back arrow
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Determine if we are in "Edit" mode or "New" mode
        existingTrip = (Trip) getIntent().getSerializableExtra("trip");
        if (existingTrip != null) {
            // Edit Mode: Pre-fill existing data
            editDestination.setText(existingTrip.getDestination());
            editStartDate.setText(existingTrip.getStartDate());
            editEndDate.setText(existingTrip.getEndDate());
            editBudget.setText(String.valueOf(existingTrip.getBudget()));
            buttonSave.setText("Update Trip");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Trip");
        }

        buttonSave.setOnClickListener(v -> saveTrip());
    }

    /**
     * Closes activity when back arrow in toolbar is clicked.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Displays a native Android calendar dialog to pick a date.
     */
    private void showDatePicker(TextInputEditText targetField) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            targetField.setText(dateFormatter.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Validates input and persists the Trip object to SQLite.
     */
    private void saveTrip() {
        String destination = editDestination.getText().toString().trim();
        String startDate = editStartDate.getText().toString().trim();
        String endDate = editEndDate.getText().toString().trim();
        String budgetStr = editBudget.getText().toString().trim();

        // Basic validation
        if (destination.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || budgetStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid budget amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingTrip == null) {
            // Add New Trip
            Trip trip = new Trip(0, destination, startDate, endDate, budget);
            dbHelper.addTrip(trip);
            Toast.makeText(this, "Trip added", Toast.LENGTH_SHORT).show();
        } else {
            // Update Existing Trip
            existingTrip.setDestination(destination);
            existingTrip.setStartDate(startDate);
            existingTrip.setEndDate(endDate);
            existingTrip.setBudget(budget);
            dbHelper.updateTrip(existingTrip);
            Toast.makeText(this, "Trip updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // Return to dashboard
    }
}
