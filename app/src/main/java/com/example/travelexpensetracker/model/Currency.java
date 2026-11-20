package com.example.travelexpensetracker.model;

import java.io.Serializable;

/**
 * Data model for Currencies and their conversion rates.
 */
public class Currency implements Serializable {
    private int id;
    private String code;
    private double exchangeRate;

    public Currency() {}

    public Currency(int id, String code, double exchangeRate) {
        this.id = id;
        this.code = code;
        this.exchangeRate = exchangeRate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }

    /**
     * Overridden to display the Currency Code (e.g., USD) in Spinners.
     */
    @Override
    public String toString() {
        return code;
    }
}
