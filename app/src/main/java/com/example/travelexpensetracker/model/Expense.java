package com.example.travelexpensetracker.model;

import java.io.Serializable;

/**
 * Data model representing an individual financial transaction (Expense).
 */
public class Expense implements Serializable {
    private int id;
    private double amount;
    private String date;
    private String notes;
    private int tripId;
    private int categoryId;
    private int currencyId;

    // Transient helper fields for easier UI display (populated via DB Joins)
    private String categoryName;
    private String currencyCode;

    public Expense() {}

    public Expense(int id, double amount, String date, String notes, int tripId, int categoryId, int currencyId) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.tripId = tripId;
        this.categoryId = categoryId;
        this.currencyId = currencyId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getCurrencyId() { return currencyId; }
    public void setCurrencyId(int currencyId) { this.currencyId = currencyId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
}
