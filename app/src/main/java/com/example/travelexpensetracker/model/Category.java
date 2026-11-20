package com.example.travelexpensetracker.model;

import java.io.Serializable;

/**
 * Data model for Expense Categories (e.g., Food, Transport).
 */
public class Category implements Serializable {
    private int id;
    private String name;

    public Category() {}

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * Overridden to display the Category Name in Spinners/Dropdowns.
     */
    @Override
    public String toString() {
        return name;
    }
}
