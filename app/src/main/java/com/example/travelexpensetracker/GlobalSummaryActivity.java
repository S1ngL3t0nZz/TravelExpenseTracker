package com.example.travelexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GlobalSummaryActivity provides a high-level overview of financials across all trips.
 * It features a comparative bar chart comparing budgets against actual spending.
 */
public class GlobalSummaryActivity extends AppCompatActivity {

    private TextView textBudget, textSpent, textRemaining;
    private LinearLayout layoutTripBreakdown;
    private BarChart barChart;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_global_summary);

        View mainView = findViewById(R.id.main_global_summary);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Standard Edge-to-Edge handling
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            View appBar = findViewById(R.id.appBarLayout);
            if (appBar != null) appBar.setPadding(0, systemBars.top, 0, 0);
            if (bottomNav != null) bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        textBudget = findViewById(R.id.text_global_budget);
        textSpent = findViewById(R.id.text_global_spent);
        textRemaining = findViewById(R.id.text_global_remaining);
        layoutTripBreakdown = findViewById(R.id.layout_trip_breakdown);
        barChart = findViewById(R.id.global_bar_chart);

        // Navigation bar Logic
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
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Force visual highlight to Summary tab
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_summary);
        }
        calculateGlobalSummary();
    }

    /**
     * Calculates the aggregated financials for all trips in the system.
     */
    private void calculateGlobalSummary() {
        // Use optimized JOIN query for performance
        List<DatabaseHelper.TripSpending> spendingList = dbHelper.getGlobalTripSpending();

        double totalBudget = 0;
        double totalSpent = 0;

        List<BarEntry> budgetEntries = new ArrayList<>();
        List<BarEntry> spentEntries = new ArrayList<>();
        List<String> tripNames = new ArrayList<>();

        layoutTripBreakdown.removeAllViews();

        for (int i = 0; i < spendingList.size(); i++) {
            DatabaseHelper.TripSpending item = spendingList.get(i);
            totalBudget += item.budget;
            totalSpent += item.spent;

            // Prepare data for the comparative bar chart
            budgetEntries.add(new BarEntry(i, (float) item.budget));
            spentEntries.add(new BarEntry(i, (float) item.spent));
            tripNames.add(item.destination);

            // Populate the breakdown list below the chart
            addTripToListView(item.destination, item.budget, item.spent);
        }

        // Update the primary financial metrics
        textBudget.setText("RM " + (int) totalBudget);
        textSpent.setText("RM " + (int) totalSpent);
        double remaining = totalBudget - totalSpent;
        textRemaining.setText("RM " + (int) remaining);
        
        // Visual indicator: Turn red if over budget overall
        if (remaining < 0) {
            textRemaining.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            textRemaining.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }

        // Build and display the chart
        setupChart(budgetEntries, spentEntries, tripNames);
    }

    /**
     * Injects a trip breakdown row into the scrollable list.
     */
    private void addTripToListView(String name, double budget, double spent) {
        View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, layoutTripBreakdown, false);
        TextView text1 = row.findViewById(android.R.id.text1);
        TextView text2 = row.findViewById(android.R.id.text2);
        text1.setText(name);
        text2.setText("Budget: RM " + (int)budget + " | Spent: RM " + (int)spent);
        layoutTripBreakdown.addView(row);
    }

    /**
     * Configures the MPAndroidChart BarChart with adaptive colors and centered labels.
     */
    private void setupChart(List<BarEntry> budgetEntries, List<BarEntry> spentEntries, List<String> tripNames) {
        if (tripNames.isEmpty()) return;

        // Fetch theme-aware text color for chart labels
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int themeTextColor = typedValue.data;

        BarDataSet budgetSet = new BarDataSet(budgetEntries, "Budget");
        budgetSet.setColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));
        budgetSet.setValueTextColor(themeTextColor);
        
        BarDataSet spentSet = new BarDataSet(spentEntries, "Spent");
        spentSet.setColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        spentSet.setValueTextColor(themeTextColor);

        BarData data = new BarData(budgetSet, spentSet);
        data.setValueTextColor(themeTextColor);
        
        // Define spacing for the grouped bar layout
        float groupSpace = 0.3f;
        float barSpace = 0.05f; 
        float barWidth = 0.3f; 
        
        data.setBarWidth(barWidth);
        barChart.setData(data);
        
        // Center labels under the pairs of bars
        barChart.groupBars(0f, groupSpace, barSpace);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(tripNames));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(tripNames.size());
        xAxis.setTextColor(themeTextColor);

        barChart.getAxisLeft().setTextColor(themeTextColor);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(themeTextColor);
        
        // UI Stability: Disable confusing interactive features for static summary
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
