/**
  Description: The ExpenseManager class manages a list of expenses, provides filtering and categorization
			  capabilities, and integrates with the BudgetManager for budget tracking.
			  It includes methods for adding, editing, deleting, and querying expenses.
*/

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseManager {
    private List<Expense> expenses; // List of expenses
    public static final List<String> predefinedCategories = List.of(
        "Food", "Transportation", "Entertainment", "Utilities", "Miscellaneous"
    );
    private BudgetManager budgetManager;
    private Runnable guiUpdateCallback;

    /**
    Constructs an ExpenseManager with an empty list of expenses and initializes
    the BudgetManager.
    */
    public ExpenseManager() {
        this.expenses = new ArrayList<>();
        this.budgetManager = new BudgetManager(this);
    }

    /**
    Sets a callback function to trigger GUI updates.
    @param callback A Runnable function to be called when updates occur.
    */
    public void setGuiUpdateCallback(Runnable callback) {
        this.guiUpdateCallback = callback;
    }
    
    /**
    Gets the current GUI update callback function.
    @return The Runnable GUI update callback.
    */
    public Runnable getGuiUpdateCallback() {
        return guiUpdateCallback;
    }
    
    /**
    Triggers the GUI update callback if it is set.
    */
    public void triggerUpdate() {
        if (guiUpdateCallback != null) {
            guiUpdateCallback.run();
        }
    }

    /**
    Adds a new expense to the list, updates budgets, and triggers GUI updates.
    @param expense The Expense object to add.
    */
    public void addExpense(Expense expense) {
    	
        expenses.add(expense);
        updateBudgets();
        if (guiUpdateCallback != null) {
            guiUpdateCallback.run(); //trigger GUI updates
        }
    }


    /**
    Edits an existing expense in the list by index.
    @param index The index of the expense to edit.
    @param newExpense The new Expense object to replace the existing one.
    @throws IndexOutOfBoundsException If the index is invalid.
    */
    public void editExpense(int index, Expense newExpense) {
        if (index >= 0 && index < expenses.size()) {
            expenses.set(index, newExpense);
            updateBudgets();
            if (guiUpdateCallback != null) {
                guiUpdateCallback.run();
            }
        } else {
            throw new IndexOutOfBoundsException("Invalid expense index.");
        }
    }

    /**
    Deletes an expense from the list by index.
    @param index The index of the expense to delete.
    @throws IndexOutOfBoundsException If the index is invalid.
    */
    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
            updateBudgets();
            if (guiUpdateCallback != null) {
                guiUpdateCallback.run();
            }
        } else {
            throw new IndexOutOfBoundsException("Invalid expense index.");
        }
    }

    /**
    Retrieves all expenses.
    @return A list of all Expense objects.
    */
    public List<Expense> getAllExpenses() {
        return expenses;
    }

    /**
    Filters expenses by category.
    @param category The category to filter by.
    @return A list of expenses in the specified category.
    */
    public List<Expense> filterByCategory(String category) {
        return expenses.stream()
                .filter(expense -> expense.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
    Filters expenses within a specific date range.
    @param start The start date of the range.
    @param end The end date of the range.
    @return A list of expenses within the date range.
    */
    public List<Expense> filterByDateRange(LocalDate start, LocalDate end) {
        return expenses.stream()
                .filter(expense -> (expense.getDate().isEqual(start) || expense.getDate().isAfter(start)) &&
                                   (expense.getDate().isEqual(end) || expense.getDate().isBefore(end)))
                .collect(Collectors.toList());
    }

    /**
    Gets the predefined categories.
    @return A list of predefined categories.
    */
    public static List<String> getPredefinedCategories() {
        return predefinedCategories;
    }

    /**
    Retrieves the BudgetManager instance, setting up a callback for updates.
    @return The BudgetManager instance.
    */
    public BudgetManager getBudgetManager() {
    	budgetManager.setUpdateCallback(this::triggerUpdate);
        return budgetManager;
    }

    /**
    Updates budgets whenever expenses are added, edited, or removed.
    */
    private void updateBudgets() {
        YearMonth currentMonth = YearMonth.now();
        budgetManager.getAllBudgets(currentMonth);
    }
    
    /**
    Gets expenses for a specific month.
    @param yearMonth The YearMonth to filter expenses by.
    @return A list of expenses for the specified month.
    */
    public List<Expense> getExpensesForMonth(YearMonth yearMonth) {
        return expenses.stream()
            .filter(expense -> YearMonth.from(expense.getDate()).equals(yearMonth))
            .collect(Collectors.toList());
    }

    /**
    Calculates the total expenses for a specific category in a given month.
    @param category The category to calculate expenses for.
    @param yearMonth The month to calculate expenses for.
    @return The total expenses for the category in the given month.
    */
    public double calculateMonthlyExpensesByCategory(String category, YearMonth yearMonth) {
        return getExpensesForMonth(yearMonth).stream()
            .filter(expense -> expense.getCategory().equals(category))
            .mapToDouble(Expense::getAmount)
            .sum();
    }

    /**
    Calculates the total expenses by category for a given month.
    @param yearMonth The month to calculate totals for.
    @return A map of category names to total expenses.
    */
    public Map<String, Double> getMonthlyTotalsByCategory(YearMonth yearMonth) {
        Map<String, Double> monthlyTotals = new HashMap<>();
        for (String category : predefinedCategories) {
            double total = calculateMonthlyExpensesByCategory(category, yearMonth);
            monthlyTotals.put(category, total);
        }
        return monthlyTotals;
    }
    
}
