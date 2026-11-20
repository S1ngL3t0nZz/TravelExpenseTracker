package com.example.travelexpensetracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelexpensetracker.R;
import com.example.travelexpensetracker.database.DatabaseHelper;
import com.example.travelexpensetracker.model.Trip;

import java.util.List;

/**
 * TripAdapter binds the list of Trip data to the CardViews in the dashboard.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<Trip> trips;
    private final OnTripActionListener listener;
    private final DatabaseHelper dbHelper;

    /**
     * Interface for handling actions triggered from individual trip cards.
     */
    public interface OnTripActionListener {
        void onExpensesClick(Trip trip);
        void onEditClick(Trip trip);
        void onDeleteClick(Trip trip);
    }

    public TripAdapter(List<Trip> trips, OnTripActionListener listener, DatabaseHelper dbHelper) {
        this.trips = trips;
        this.listener = listener;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom trip item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        // Live-calculate spent amount for this trip from SQLite
        double spent = dbHelper.getTotalSpentForTrip(trip.getId());
        holder.bind(trip, spent, listener);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    /**
     * Updates the internal data set and refreshes the RecyclerView.
     */
    public void updateData(List<Trip> newTrips) {
        this.trips = newTrips;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder pattern for managing trip card view references.
     */
    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView destinationText, datesText, budgetText, spentText, remainingText;
        Button btnExpenses, btnEdit, btnDelete;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            destinationText = itemView.findViewById(R.id.text_destination);
            datesText = itemView.findViewById(R.id.text_dates);
            budgetText = itemView.findViewById(R.id.text_budget);
            spentText = itemView.findViewById(R.id.text_spent);
            remainingText = itemView.findViewById(R.id.text_remaining);
            btnExpenses = itemView.findViewById(R.id.btn_expenses);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        /**
         * Populates views and sets up click listeners for a specific trip.
         */
        public void bind(Trip trip, double spent, OnTripActionListener listener) {
            destinationText.setText(trip.getDestination());
            datesText.setText(trip.getStartDate() + " - " + trip.getEndDate());
            budgetText.setText("RM " + (int)trip.getBudget());
            spentText.setText("RM " + (int)spent);
            
            // Calculate and display remaining balance
            double remaining = trip.getBudget() - spent;
            remainingText.setText("RM " + (int)remaining);

            // Dynamic color indicator based on budget status
            if (remaining < 0) {
                // Red for overspending
                remainingText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
            } else {
                // Green for healthy budget
                remainingText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
            }

            // Set up button actions
            btnExpenses.setOnClickListener(v -> listener.onExpensesClick(trip));
            btnEdit.setOnClickListener(v -> listener.onEditClick(trip));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(trip));
        }
    }
}
