package com.example.travelexpensetracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelexpensetracker.database.DatabaseHelper;
import com.example.travelexpensetracker.model.Category;
import com.example.travelexpensetracker.model.Currency;
import com.example.travelexpensetracker.model.Expense;
import com.example.travelexpensetracker.model.Trip;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * AddExpenseActivity provides a form to log a new expense or edit an existing one.
 * It uses Spinners to select from existing trips, categories, and currencies.
 */
public class AddExpenseActivity extends AppCompatActivity {

    private TextInputEditText editAmount, editDate, editNotes;
    private Spinner spinnerTrip, spinnerCurrency, spinnerCategory;
    private Button buttonSave, buttonCancel;
    private DatabaseHelper dbHelper;
    private Expense existingExpense;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_expense);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // System Inset handling to support notches and system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_add_expense), (v, insets) -> {
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

        dbHelper = new DatabaseHelper(this);

        // View Lookup
        editAmount = findViewById(R.id.edit_expense_amount);
        editDate = findViewById(R.id.edit_expense_date);
        editNotes = findViewById(R.id.edit_expense_notes);
        spinnerTrip = findViewById(R.id.spinner_trip);
        spinnerCurrency = findViewById(R.id.spinner_currency);
        spinnerCategory = findViewById(R.id.spinner_category);
        buttonSave = findViewById(R.id.button_save_expense);
        buttonCancel = findViewById(R.id.button_cancel);

        // Toolbar Setup
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Date field logic: Disable typing, use Picker
        editDate.setFocusable(false);
        editDate.setClickable(true);
        editDate.setOnClickListener(v -> showDatePicker());

        // Fill Spinners from Database
        populateSpinners();

        // Check if we are Editing an existing expense or creating a New one
        existingExpense = (Expense) getIntent().getSerializableExtra("expense");
        if (existingExpense != null) {
            // Edit Mode: Pre-load data
            editAmount.setText(String.valueOf(existingExpense.getAmount()));
            editDate.setText(existingExpense.getDate());
            editNotes.setText(existingExpense.getNotes());
            setSpinnerToTrip(existingExpense.getTripId());
            setSpinnerToCategory(existingExpense.getCategoryId());
            setSpinnerToCurrency(existingExpense.getCurrencyId());
            buttonSave.setText("Update Expense");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Expense");
        } else {
            // New Mode: Set defaults
            long tripIdFromIntent = getIntent().getLongExtra("trip_id", -1);
            if (tripIdFromIntent != -1) {
                setSpinnerToTrip(tripIdFromIntent); // Pre-select current trip context
            }
            // Default to today
            editDate.setText(dateFormatter.format(calendar.getTime()));
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Expense");
        }

        buttonSave.setOnClickListener(v -> saveExpense());
        buttonCancel.setOnClickListener(v -> finish());

        // Bottom Nav Redirection
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
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    /**
     * Shows a calendar dialog to select an expense date.
     */
    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editDate.setText(dateFormatter.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Fetches dynamic lists from SQLite to populate dropdown menus.
     */
    private void populateSpinners() {
        List<Trip> trips = dbHelper.getAllTrips();
        ArrayAdapter<Trip> tripAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, trips);
        tripAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrip.setAdapter(tripAdapter);

        List<Category> categories = dbHelper.getAllCategories();
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        List<Currency> currencies = dbHelper.getAllCurrencies();
        ArrayAdapter<Currency> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);
    }

    /**
     * Helper: Sets Trip spinner selection by ID.
     */
    private void setSpinnerToTrip(long tripId) {
        ArrayAdapter<Trip> adapter = (ArrayAdapter<Trip>) spinnerTrip.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getId() == tripId) {
                spinnerTrip.setSelection(i);
                break;
            }
        }
    }

    /**
     * Helper: Sets Category spinner selection by ID.
     */
    private void setSpinnerToCategory(int categoryId) {
        ArrayAdapter<Category> adapter = (ArrayAdapter<Category>) spinnerCategory.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getId() == categoryId) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    /**
     * Helper: Sets Currency spinner selection by ID.
     */
    private void setSpinnerToCurrency(int currencyId) {
        ArrayAdapter<Currency> adapter = (ArrayAdapter<Currency>) spinnerCurrency.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getId() == currencyId) {
                spinnerCurrency.setSelection(i);
                break;
            }
        }
    }

    /**
     * Validates input fields and saves the expense to SQLite.
     */
    private void saveExpense() {
        String amountStr = editAmount.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();

        Trip trip = (Trip) spinnerTrip.getSelectedItem();
        Category category = (Category) spinnerCategory.getSelectedItem();
        Currency currency = (Currency) spinnerCurrency.getSelectedItem();

        // Validation logic
        if (amountStr.isEmpty() || date.isEmpty() || trip == null || category == null || currency == null) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be positive", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingExpense == null) {
            // New Expense
            Expense expense = new Expense(0, amount, date, notes, (int) trip.getId(), category.getId(), currency.getId());
            dbHelper.addExpense(expense);
            Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
        } else {
            // Update Existing
            existingExpense.setAmount(amount);
            existingExpense.setDate(date);
            existingExpense.setNotes(notes);
            existingExpense.setTripId((int) trip.getId());
            existingExpense.setCategoryId(category.getId());
            existingExpense.setCurrencyId(currency.getId());
            dbHelper.updateExpense(existingExpense);
            Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // Return to previous screen
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
