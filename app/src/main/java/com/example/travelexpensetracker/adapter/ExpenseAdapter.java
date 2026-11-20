package com.example.travelexpensetracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelexpensetracker.R;
import com.example.travelexpensetracker.database.DatabaseHelper;
import com.example.travelexpensetracker.model.Currency;
import com.example.travelexpensetracker.model.Expense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExpenseAdapter binds individual transaction records to the history list items.
 * Supports a hybrid display showing both local and converted currencies.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private final OnExpenseActionListener listener;
    // Cache for exchange rates to avoid redundant DB calls while scrolling
    private final Map<Integer, Double> rateMap = new HashMap<>();

    /**
     * Interface for edit/delete actions on expense items.
     */
    public interface OnExpenseActionListener {
        void onEditExpense(Expense expense);
        void onDeleteExpense(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenses, OnExpenseActionListener listener, DatabaseHelper dbHelper) {
        this.expenses = expenses;
        this.listener = listener;
        updateRates(dbHelper);
    }

    /**
     * Refreshes the local currency rate cache.
     */
    private void updateRates(DatabaseHelper dbHelper) {
        List<Currency> allCurrencies = dbHelper.getAllCurrencies();
        for (Currency c : allCurrencies) {
            rateMap.put(c.getId(), c.getExchangeRate());
        }
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense, listener, rateMap);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    /**
     * Refreshes list data and currency rates.
     */
    public void updateData(List<Expense> newExpenses, DatabaseHelper dbHelper) {
        this.expenses = newExpenses;
        updateRates(dbHelper);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for managing expense list item views.
     */
    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText, amountText, dateText, notesText, convertedText;
        Button btnEdit, btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.text_expense_category);
            amountText = itemView.findViewById(R.id.text_expense_amount);
            convertedText = itemView.findViewById(R.id.text_expense_converted);
            dateText = itemView.findViewById(R.id.text_expense_date);
            notesText = itemView.findViewById(R.id.text_expense_notes);
            btnEdit = itemView.findViewById(R.id.btn_edit_expense);
            btnDelete = itemView.findViewById(R.id.btn_delete_expense);
        }

        /**
         * Populates views using the "Hybrid Display" logic.
         */
        public void bind(Expense expense, OnExpenseActionListener listener, Map<Integer, Double> rateMap) {
            categoryText.setText(expense.getCategoryName());
            
            // Primary View: Local Currency as entered (e.g., USD 200)
            amountText.setText(expense.getCurrencyCode() + " " + (int)expense.getAmount());
            
            // Secondary View: Converted amount in Home Currency (e.g., RM 880)
            Double rateObj = rateMap.get(expense.getCurrencyId());
            double rate = (rateObj != null) ? rateObj : 1.0;
            double converted = expense.getAmount() * rate;
            convertedText.setText("(RM " + (int)converted + ")");

            dateText.setText(expense.getDate());
            notesText.setText(expense.getNotes());

            // Bind interactions
            btnEdit.setOnClickListener(v -> listener.onEditExpense(expense));
            btnDelete.setOnClickListener(v -> listener.onDeleteExpense(expense));
        }
    }
}
