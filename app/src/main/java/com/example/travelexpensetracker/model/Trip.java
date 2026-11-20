package com.example.travelexpensetracker.model;

import java.io.Serializable;

/**
 * Data model representing a travel Trip.
 */
public class Trip implements Serializable {
    private long id;
    private String destination;
    private String startDate;
    private String endDate;
    private double budget;

    public Trip() {}

    public Trip(long id, String destination, String startDate, String endDate, double budget) {
        this.id = id;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    /**
     * Used for Spinner display and debugging.
     */
    @Override
    public String toString() {
        return destination;
    }
}
