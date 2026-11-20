package com.example.travelexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelexpensetracker.database.DatabaseHelper;
import com.example.travelexpensetracker.model.Currency;
import com.example.travelexpensetracker.model.Expense;
import com.example.travelexpensetracker.model.Trip;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SpendingSummaryActivity provides a per-trip categorical spending breakdown.
 * It features a pie chart, a detailed category list, and summary statistics.
 */
public class SpendingSummaryActivity extends AppCompatActivity {

    private TextView textTripName, textBudget, textSpent, textRemaining;
    private TextView textNoOfEntries, textTotalSummary, textAverageSummary, textHighestSummary, textLowestSummary;
    private LinearLayout layoutCategoryBreakdown;
    private PieChart pieChart;
    private DatabaseHelper dbHelper;
    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spending_summary);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Edge-to-Edge System Bar Handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_spending_summary), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            View appBar = findViewById(R.id.appBarLayout);
            if (appBar != null) appBar.setPadding(0, systemBars.top, 0, 0);
            if (bottomNav != null) bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Task 1: Navigation back to home page on click event of the page
        findViewById(R.id.main_spending_summary).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Retrieve current Trip context
        trip = (Trip) getIntent().getSerializableExtra("trip");
        if (trip == null) {
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        textTripName = findViewById(R.id.text_summary_trip_name);
        textBudget = findViewById(R.id.text_summary_budget);
        textSpent = findViewById(R.id.text_summary_spent);
        textRemaining = findViewById(R.id.text_summary_remaining);
        
        // Summary View Lookups
        textNoOfEntries = findViewById(R.id.text_no_of_entries);
        textTotalSummary = findViewById(R.id.text_total_summary);
        textAverageSummary = findViewById(R.id.text_average_summary);
        textHighestSummary = findViewById(R.id.text_highest_summary);
        textLowestSummary = findViewById(R.id.text_lowest_summary);
        
        layoutCategoryBreakdown = findViewById(R.id.layout_category_breakdown);
        pieChart = findViewById(R.id.pie_chart);

        // Toolbar setup
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trip Summary");
        }

        textTripName.setText(trip.getDestination());
        textBudget.setText("RM " + (int)trip.getBudget());

        // Navigation logic
        bottomNav.setSelectedItemId(R.id.nav_summary);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        calculateSummary();
    }

    /**
     * Calculates the categorical breakdown of spending for the selected trip.
     */
    private void calculateSummary() {
        List<Expense> expenses = dbHelper.getExpensesForTrip(trip.getId());
        List<Currency> allCurrencies = dbHelper.getAllCurrencies();
        
        // Map exchange rates for fast lookup
        Map<Integer, Double> rateMap = new HashMap<>();
        for (Currency c : allCurrencies) {
            rateMap.put(c.getId(), c.getExchangeRate());
        }

        // Use Task 1 methods to calculate statistics
        int entryCount = getNoOfEntries(expenses);
        double totalSpentInBase = getTotalExpenses(expenses, rateMap);
        double averageSpent = getAverageExpenses(totalSpentInBase, entryCount);
        double highest = getHighestExpense(expenses, rateMap);
        double lowest = getLowestExpense(expenses, rateMap);

        // Update financial display
        textSpent.setText("RM " + (int)totalSpentInBase);
        double remaining = trip.getBudget() - totalSpentInBase;
        textRemaining.setText("RM " + (int)remaining);

        // Update Summary Card (Task 1)
        textNoOfEntries.setText("No of entries: " + entryCount);
        textTotalSummary.setText(String.format("Total: RM%,.2f", totalSpentInBase));
        textAverageSummary.setText(String.format("Average: RM%,.2f", averageSpent));
        textHighestSummary.setText(String.format("Highest: RM%,.2f", highest));
        textLowestSummary.setText(String.format("Lowest: RM%,.2f", lowest));

        // Visual indicator for budget status
        if (remaining < 0) {
            textRemaining.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            textRemaining.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }

        // Aggregation for Pie Chart and List
        Map<String, Double> categoryMap = new HashMap<>();
        for (Expense e : expenses) {
            Double rateObj = rateMap.get(e.getCurrencyId());
            double rate = (rateObj != null) ? rateObj : 1.0;
            double amountInBase = e.getAmount() * rate;
            
            String catName = e.getCategoryName();
            categoryMap.put(catName, categoryMap.getOrDefault(catName, 0.0) + amountInBase);
        }

        // Build list and chart
        populateCategoryList(categoryMap);
        populateChart(categoryMap);
    }

    // --- Task 1 Implementation Methods ---

    public int getNoOfEntries(List<Expense> expenses) {
        return expenses.size();
    }

    public double getTotalExpenses(List<Expense> expenses, Map<Integer, Double> rateMap) {
        double total = 0;
        for (Expense e : expenses) {
            Double rateObj = rateMap.get(e.getCurrencyId());
            double rate = (rateObj != null) ? rateObj : 1.0;
            total += (e.getAmount() * rate);
        }
        return total;
    }

    public double getAverageExpenses(double total, int count) {
        if (count == 0) return 0;
        return total / count;
    }

    public double getHighestExpense(List<Expense> expenses, Map<Integer, Double> rateMap) {
        if (expenses.isEmpty()) return 0;
        double max = -1;
        for (Expense e : expenses) {
            Double rateObj = rateMap.get(e.getCurrencyId());
            double rate = (rateObj != null) ? rateObj : 1.0;
            double amountInBase = e.getAmount() * rate;
            if (amountInBase > max) {
                max = amountInBase;
            }
        }
        return max;
    }

    public double getLowestExpense(List<Expense> expenses, Map<Integer, Double> rateMap) {
        if (expenses.isEmpty()) return 0;
        double min = Double.MAX_VALUE;
        for (Expense e : expenses) {
            Double rateObj = rateMap.get(e.getCurrencyId());
            double rate = (rateObj != null) ? rateObj : 1.0;
            double amountInBase = e.getAmount() * rate;
            if (amountInBase < min) {
                min = amountInBase;
            }
        }
        return min;
    }

    /**
     * Populates the category breakdown list view.
     */
    private void populateCategoryList(Map<String, Double> categoryMap) {
        layoutCategoryBreakdown.removeAllViews();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, layoutCategoryBreakdown, false);
            TextView text1 = row.findViewById(android.R.id.text1);
            TextView text2 = row.findViewById(android.R.id.text2);
            text1.setText(entry.getKey());
            text2.setText("RM " + entry.getValue().intValue());
            layoutCategoryBreakdown.addView(row);
        }
    }

    /**
     * Configures and displays the categorical Pie Chart.
     */
    private void populateChart(Map<String, Double> categoryMap) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        // Theme-aware color handling for the chart labels
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int themeTextColor = typedValue.data;

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Vibrant color scheme
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(themeTextColor);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses (RM)");
        pieChart.setCenterTextColor(themeTextColor);
        pieChart.setEntryLabelColor(themeTextColor);
        pieChart.getLegend().setTextColor(themeTextColor);
        pieChart.setHoleColor(android.R.color.transparent);
        pieChart.animateY(1000); // Intro animation
        pieChart.invalidate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
