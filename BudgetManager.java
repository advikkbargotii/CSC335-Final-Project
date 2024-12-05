import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages budgets for different categories across various months. It interacts with an ExpenseManager to track expenses.
 */
public class BudgetManager {
    // Encapsulation: Keeping budget data private to control access through public methods
    private Map<String, Map<String, Double>> budgets; // Maps month to a map of categories and their budget amounts
    private ExpenseManager expenseManager; // Manages related expenses, showcasing the use of composition
    private Runnable updateCallback; // Callback for notifying updates, illustrating dependency injection

    /**
     * Constructs a BudgetManager with a linked ExpenseManager.
     * @param expenseManager An instance of ExpenseManager to interact with expense data.
     */
    public BudgetManager(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
        this.budgets = new HashMap<>();
        initializeDefaultBudgets();
    }

    /**
     * Initializes default budgets for all predefined categories in the current month.
     */
    private void initializeDefaultBudgets() {
        try {
            String currentMonth = YearMonth.now().toString();
            budgets.put(currentMonth, new HashMap<>()); // Safe initialization of maps to avoid null pointers
            for (String category : ExpenseManager.predefinedCategories) {
                // Setting initial budget values, demonstrates default settings management
                budgets.get(currentMonth).put(category, 1000.0);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize default budgets: " + e.getMessage());
        }
    }

    /**
     * Sets a callback function that will be executed whenever the budget updates.
     * @param callback A Runnable callback to be triggered on budget updates.
     */
    public void setUpdateCallback(Runnable callback) {
        this.updateCallback = callback; // Setting a callback, allows for flexible event handling
    }

    /**
     * Sets or updates the budget for a given category in a specific month.
     * @param category The budget category.
     * @param amount The budget amount.
     * @param month The month for which the budget is applicable.
     */
    public void setBudget(String category, double amount, YearMonth month) {
        try {
            String monthKey = month.toString();
            budgets.computeIfAbsent(monthKey, k -> new HashMap<>()); // Using computeIfAbsent for lazy map initialization
            budgets.get(monthKey).put(category, amount);
            if (updateCallback != null) {
                updateCallback.run(); // Execute callback if set, showing dynamic behavior based on state
            }
        } catch (NullPointerException e) {
            System.err.println("Invalid category or month: " + e.getMessage()); // Exception handling to catch runtime errors
        }
    }

    /**
     * Retrieves the budget amount for a specified category and month.
     * @param category The budget category.
     * @param month The month for which the budget is queried.
     * @return The budget amount, or 0.0 if no budget is set.
     */
    public double getBudget(String category, YearMonth month) {
        try {
            String monthKey = month.toString();
            // Safe retrieval with default values to prevent null pointers
            return budgets.getOrDefault(monthKey, new HashMap<>()).getOrDefault(category, 0.0);
        } catch (Exception e) {
            System.err.println("Error retrieving budget for " + category + ": " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Retrieves all budget entries for a specific month.
     * @param month The month for which budgets are requested.
     * @return A map of category names to their respective budget amounts.
     */
    public Map<String, Double> getAllBudgets(YearMonth month) {
        String monthKey = month.toString();
        // Return a new HashMap to encapsulate the original map and prevent external modifications
        return new HashMap<>(budgets.getOrDefault(monthKey, new HashMap<>()));
    }

    /**
     * Calculates the total expenses for a given category within a specific month.
     * @param category The expense category to calculate.
     * @param month The month for which expenses are summed.
     * @return The total amount of expenses in that category for the specified month.
     */
    public double calculateTotalExpensesByCategory(String category, YearMonth month) {
        try {
            double total = 0.0;
            // Iterative calculation to sum expenses, uses safe navigation of data structures
            for (Expense expense : expenseManager.filterByCategory(category)) {
                if (YearMonth.from(expense.getDate()).equals(month)) {
                    total += expense.getAmount();
                }
            }
            return total;
        } catch (Exception e) {
            System.err.println("Error calculating expenses for " + category + ": " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Calculates the utilization percentage of each budget category for a specified month.
     * @param month The month for which utilization is calculated.
     * @return A map of categories to their budget utilization percentages.
     */
    public Map<String, Double> calculateBudgetUtilization(YearMonth month) {
        Map<String, Double> utilization = new HashMap<>();
        try {
            // Calculation logic demonstrating data retrieval and mathematical computation
            for (String category : ExpenseManager.predefinedCategories) {
                double expenses = calculateTotalExpensesByCategory(category, month);
                double budget = getBudget(category, month);
                double usage = budget > 0 ? (expenses / budget) * 100 : 0;
                utilization.put(category, usage);
            }
        } catch (Exception e) {
            System.err.println("Error calculating budget utilization for " + month + ": " + e.getMessage());
        }
        return utilization;
    }

    /**
     * Retrieves a list of all months for which budget or expense data is available.
     * @return An ArrayList of YearMonth objects representing the months available.
     */
    public ArrayList<YearMonth> getAvailableMonths() {
        ArrayList<YearMonth> months = new ArrayList<>();
        try {
            // Using a loop to add month keys safely to a list, demonstrates use of advanced collection manipulation
            for (String monthKey : budgets.keySet()) {
                months.add(YearMonth.parse(monthKey));
            }
            // Ensuring no duplicate months are added, illustrates defensive programming
            expenseManager.getAllExpenses().forEach(expense -> {
                YearMonth expenseMonth = YearMonth.from(expense.getDate());
                if (!months.contains(expenseMonth)) {
                    months.add(expenseMonth);
                }
            });
            Collections.sort(months); // Sorting to provide ordered results
        } catch (Exception e) {
            System.err.println("Error retrieving available months: " + e.getMessage());
        }
        return months;
    }
}
