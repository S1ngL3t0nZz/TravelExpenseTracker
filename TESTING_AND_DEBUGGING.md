# Testing and Debugging Documentation

## Test Cases

| Test Case ID | Feature | Description | Expected Result |
|--------------|---------|-------------|-----------------|
| TC-01 | Add Trip | Create a new trip with destination "Paris", dates, and budget 5000. | Trip appears in the dashboard list with correct details. |
| TC-02 | Log Expense | Record an expense of RM 50 for "Food" in the "Paris" trip. | Expense history shows the new entry; remaining budget updates to 4950. |
| TC-03 | Budget Calculation | Add multiple expenses (RM 1000 + RM 500). | "Spent" shows RM 1500 and "Remaining" shows RM 3500 on the trip card. |
| TC-04 | Edge Case: Negative Amount | Try to enter -10 as an expense amount. | App shows "Amount must be positive" error message; record is not saved. |
| TC-05 | Edge Case: Exceed Budget | Log an expense of RM 6000 for a trip with RM 5000 budget. | Expense is saved, but remaining budget indicator in details turns red or shows a negative value. |

## Bugs Encountered and Resolved

### Bug 1: Unnormalized Category Data
- **Problem**: Initially, expenses were stored with a flat string for the category. This made it difficult to generate reports if the user made a typo (e.g., "Food" vs "food").
- **Resolution**: Implemented a separate `CATEGORIES` table and used foreign keys. Populated a spinner from the database to ensure data integrity.

### Bug 2: NullPointerException on Search
- **Problem**: The app crashed when searching for a trip if the filter results were empty because the adapter was not properly handling empty lists in some cases.
- **Resolution**: Added a check in `MainActivity.filterTrips()` to toggle visibility between the `RecyclerView` and a `text_no_trips` TextView when the list is empty.

## Implementation Details
- **Charts**: Integrated MPAndroidChart for visualising spending by category in the `SpendingSummaryActivity`.
- **Database**: SQLite database versioning handled for schema updates.
- **UI**: Consistent use of Material Design components and Floating Action Buttons.
