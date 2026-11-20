package com.example.travelexpensetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.travelexpensetracker.model.Category;
import com.example.travelexpensetracker.model.Currency;
import com.example.travelexpensetracker.model.Expense;
import com.example.travelexpensetracker.model.Trip;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper manages the SQLite database for the Travel Expense Tracker.
 * It handles table creation, prepopulating static data, and all CRUD operations.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "travel_tracker.db";
    private static final int DATABASE_VERSION = 3; // Incremented to version 3 for global currency expansion

    // Trip table constants
    public static final String TABLE_TRIPS = "trips";
    public static final String COLUMN_TRIP_ID = "TripID";
    public static final String COLUMN_TRIP_DESTINATION = "Destination";
    public static final String COLUMN_TRIP_START_DATE = "StartDate";
    public static final String COLUMN_TRIP_END_DATE = "EndDate";
    public static final String COLUMN_TRIP_BUDGET = "Budget";

    // Category table constants
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_ID = "CategoryID";
    public static final String COLUMN_CATEGORY_NAME = "CategoryName";

    // Currency table constants
    public static final String TABLE_CURRENCY = "currency";
    public static final String COLUMN_CURRENCY_ID = "CurrencyID";
    public static final String COLUMN_CURRENCY_CODE = "CurrencyCode";
    public static final String COLUMN_CURRENCY_RATE = "ExchangeRate";

    // Expense table constants
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_EXPENSE_ID = "ExpenseID";
    public static final String COLUMN_EXPENSE_AMOUNT = "Amount";
    public static final String COLUMN_EXPENSE_DATE = "Date";
    public static final String COLUMN_EXPENSE_NOTES = "Notes";
    public static final String COLUMN_EXPENSE_TRIP_ID = "TripID";
    public static final String COLUMN_EXPENSE_CATEGORY_ID = "CategoryID";
    public static final String COLUMN_EXPENSE_CURRENCY_ID = "CurrencyID";

    // SQL statements for table creation
    private static final String CREATE_TABLE_TRIPS = "CREATE TABLE " + TABLE_TRIPS + "("
            + COLUMN_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TRIP_DESTINATION + " TEXT,"
            + COLUMN_TRIP_START_DATE + " TEXT,"
            + COLUMN_TRIP_END_DATE + " TEXT,"
            + COLUMN_TRIP_BUDGET + " REAL" + ")";

    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + "("
            + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CATEGORY_NAME + " TEXT" + ")";

    private static final String CREATE_TABLE_CURRENCY = "CREATE TABLE " + TABLE_CURRENCY + "("
            + COLUMN_CURRENCY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CURRENCY_CODE + " TEXT,"
            + COLUMN_CURRENCY_RATE + " REAL" + ")";

    private static final String CREATE_TABLE_EXPENSES = "CREATE TABLE " + TABLE_EXPENSES + "("
            + COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EXPENSE_AMOUNT + " REAL,"
            + COLUMN_EXPENSE_DATE + " TEXT,"
            + COLUMN_EXPENSE_NOTES + " TEXT,"
            + COLUMN_EXPENSE_TRIP_ID + " INTEGER,"
            + COLUMN_EXPENSE_CATEGORY_ID + " INTEGER,"
            + COLUMN_EXPENSE_CURRENCY_ID + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_EXPENSE_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_EXPENSE_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + "),"
            + "FOREIGN KEY(" + COLUMN_EXPENSE_CURRENCY_ID + ") REFERENCES " + TABLE_CURRENCY + "(" + COLUMN_CURRENCY_ID + ")" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TRIPS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_CURRENCY);
        db.execSQL(CREATE_TABLE_EXPENSES);

        // Prepopulate standard Expense Categories
        String[] categories = {"Accommodation", "Food", "Transport", "Activities", "Shopping"};
        for (String cat : categories) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY_NAME, cat);
            db.insert(TABLE_CATEGORIES, null, values);
        }

        // Global Currency Expansion: Prepopulate common world currencies
        // Exchange rates are approximate and relative to MYR (1.0)
        addCurrency(db, "MYR", 1.0);     // Malaysia Ringgit (Base)
        addCurrency(db, "USD", 4.41);    // US Dollar
        addCurrency(db, "EUR", 4.75);    // Euro
        addCurrency(db, "GBP", 5.60);    // British Pound
        addCurrency(db, "SGD", 3.28);    // Singapore Dollar
        addCurrency(db, "JPY", 0.028);   // Japanese Yen (per 1 Yen)
        addCurrency(db, "KRW", 0.0032);  // Korean Won
        addCurrency(db, "AUD", 2.92);    // Australian Dollar
        addCurrency(db, "CNY", 0.61);    // Chinese Yuan
        addCurrency(db, "THB", 0.13);    // Thai Baht
        addCurrency(db, "IDR", 0.00028); // Indonesian Rupiah
        addCurrency(db, "VND", 0.00017); // Vietnamese Dong
        addCurrency(db, "INR", 0.053);   // Indian Rupee
        addCurrency(db, "CAD", 3.23);    // Canadian Dollar
        addCurrency(db, "CHF", 4.98);    // Swiss Franc
    }

    /**
     * Private helper to insert currency data during creation.
     */
    private void addCurrency(SQLiteDatabase db, String code, double rate) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CURRENCY_CODE, code);
        values.put(COLUMN_CURRENCY_RATE, rate);
        db.insert(TABLE_CURRENCY, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Warning: onUpgrade currently recreates the DB. This clears user data but ensures schema consistency during dev.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENCY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // --- Trip Operations ---

    public long addTrip(Trip trip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRIP_DESTINATION, trip.getDestination());
        values.put(COLUMN_TRIP_START_DATE, trip.getStartDate());
        values.put(COLUMN_TRIP_END_DATE, trip.getEndDate());
        values.put(COLUMN_TRIP_BUDGET, trip.getBudget());
        return db.insert(TABLE_TRIPS, null, values);
    }

    public int updateTrip(Trip trip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRIP_DESTINATION, trip.getDestination());
        values.put(COLUMN_TRIP_START_DATE, trip.getStartDate());
        values.put(COLUMN_TRIP_END_DATE, trip.getEndDate());
        values.put(COLUMN_TRIP_BUDGET, trip.getBudget());
        return db.update(TABLE_TRIPS, values, COLUMN_TRIP_ID + " = ?", new String[]{String.valueOf(trip.getId())});
    }

    public void deleteTrip(long tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRIPS, COLUMN_TRIP_ID + " = ?", new String[]{String.valueOf(tripId)});
    }

    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        String selectQuery = "SELECT " + COLUMN_TRIP_ID + ", " + COLUMN_TRIP_DESTINATION + ", " +
                COLUMN_TRIP_START_DATE + ", " + COLUMN_TRIP_END_DATE + ", " + COLUMN_TRIP_BUDGET +
                " FROM " + TABLE_TRIPS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Trip trip = new Trip();
                trip.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID)));
                trip.setDestination(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_DESTINATION)));
                trip.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_START_DATE)));
                trip.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_END_DATE)));
                trip.setBudget(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRIP_BUDGET)));
                trips.add(trip);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trips;
    }

    // --- Category and Currency Operations ---

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_CATEGORY_ID + ", " + COLUMN_CATEGORY_NAME +
                " FROM " + TABLE_CATEGORIES, null);
        if (cursor.moveToFirst()) {
            do {
                Category cat = new Category(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
                );
                categories.add(cat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public List<Currency> getAllCurrencies() {
        List<Currency> currencies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_CURRENCY_ID + ", " + COLUMN_CURRENCY_CODE +
                ", " + COLUMN_CURRENCY_RATE + " FROM " + TABLE_CURRENCY, null);
        if (cursor.moveToFirst()) {
            do {
                Currency cur = new Currency(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENCY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CURRENCY_CODE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CURRENCY_RATE))
                );
                currencies.add(cur);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return currencies;
    }

    // --- Expense Operations ---

    public long addExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_TRIP_ID, expense.getTripId());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_DATE, expense.getDate());
        values.put(COLUMN_EXPENSE_NOTES, expense.getNotes());
        values.put(COLUMN_EXPENSE_CATEGORY_ID, expense.getCategoryId());
        values.put(COLUMN_EXPENSE_CURRENCY_ID, expense.getCurrencyId());
        return db.insert(TABLE_EXPENSES, null, values);
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_TRIP_ID, expense.getTripId());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_DATE, expense.getDate());
        values.put(COLUMN_EXPENSE_NOTES, expense.getNotes());
        values.put(COLUMN_EXPENSE_CATEGORY_ID, expense.getCategoryId());
        values.put(COLUMN_EXPENSE_CURRENCY_ID, expense.getCurrencyId());
        return db.update(TABLE_EXPENSES, values, COLUMN_EXPENSE_ID + " = ?", new String[]{String.valueOf(expense.getId())});
    }

    public void deleteExpense(int expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_ID + " = ?", new String[]{String.valueOf(expenseId)});
    }

    public List<Expense> getExpensesForTrip(long tripId) {
        List<Expense> expenses = new ArrayList<>();
        String selectQuery = "SELECT e." + COLUMN_EXPENSE_ID + ", e." + COLUMN_EXPENSE_AMOUNT +
                ", e." + COLUMN_EXPENSE_DATE + ", e." + COLUMN_EXPENSE_NOTES +
                ", e." + COLUMN_EXPENSE_TRIP_ID + ", e." + COLUMN_EXPENSE_CATEGORY_ID +
                ", e." + COLUMN_EXPENSE_CURRENCY_ID + ", c." + COLUMN_CATEGORY_NAME +
                ", cur." + COLUMN_CURRENCY_CODE +
                " FROM " + TABLE_EXPENSES + " e" +
                " JOIN " + TABLE_CATEGORIES + " c ON e." + COLUMN_EXPENSE_CATEGORY_ID + " = c." + COLUMN_CATEGORY_ID +
                " JOIN " + TABLE_CURRENCY + " cur ON e." + COLUMN_EXPENSE_CURRENCY_ID + " = cur." + COLUMN_CURRENCY_ID +
                " WHERE e." + COLUMN_EXPENSE_TRIP_ID + " = " + tripId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)));
                expense.setTripId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_TRIP_ID)));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT)));
                expense.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DATE)));
                expense.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_NOTES)));
                expense.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_CATEGORY_ID)));
                expense.setCurrencyId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_CURRENCY_ID)));
                expense.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)));
                expense.setCurrencyCode(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CURRENCY_CODE)));
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    public double getTotalSpentForTrip(long tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(e." + COLUMN_EXPENSE_AMOUNT + " * cur." + COLUMN_CURRENCY_RATE + ") " +
                " FROM " + TABLE_EXPENSES + " e " +
                " JOIN " + TABLE_CURRENCY + " cur ON e." + COLUMN_EXPENSE_CURRENCY_ID + " = cur." + COLUMN_CURRENCY_ID +
                " WHERE e." + COLUMN_EXPENSE_TRIP_ID + " = " + tripId;
        
        Cursor cursor = db.rawQuery(query, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public List<TripSpending> getGlobalTripSpending() {
        List<TripSpending> results = new ArrayList<>();
        String query = "SELECT t." + COLUMN_TRIP_ID + ", t." + COLUMN_TRIP_DESTINATION + ", t." + COLUMN_TRIP_BUDGET + 
                ", SUM(e." + COLUMN_EXPENSE_AMOUNT + " * cur." + COLUMN_CURRENCY_RATE + ") as total_spent " +
                " FROM " + TABLE_TRIPS + " t " +
                " LEFT JOIN " + TABLE_EXPENSES + " e ON t." + COLUMN_TRIP_ID + " = e." + COLUMN_EXPENSE_TRIP_ID + 
                " LEFT JOIN " + TABLE_CURRENCY + " cur ON e." + COLUMN_EXPENSE_CURRENCY_ID + " = cur." + COLUMN_CURRENCY_ID +
                " GROUP BY t." + COLUMN_TRIP_ID;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                results.add(new TripSpending(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_DESTINATION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRIP_BUDGET)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("total_spent"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return results;
    }

    public static class TripSpending {
        public String destination;
        public double budget;
        public double spent;

        public TripSpending(String destination, double budget, double spent) {
            this.destination = destination;
            this.budget = budget;
            this.spent = spent;
        }
    }
}
