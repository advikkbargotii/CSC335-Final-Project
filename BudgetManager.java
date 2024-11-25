import java.util.HashMap;
import java.util.Map;

public class BudgetManager {
    private Map<String, Double> budgets;
    private ExpenseManager expenseManager;
    private Runnable updateCallback;

    public BudgetManager(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
        this.budgets = new HashMap<>();
        initializeDefaultBudgets();
    }
    
    public void setUpdateCallback(Runnable callback) {
        this.updateCallback = callback;
    }

    private void initializeDefaultBudgets() {
        // Setting initial budgets to a default value (e.g., $1000) to avoid division by zero
        for (String category : ExpenseManager.predefinedCategories) {
            budgets.put(category, 1000.0);
        }
    }

    public void setBudget(String category, double amount) {
        budgets.put(category, amount);
        if (updateCallback != null) {
            updateCallback.run();
        }
    }

    public double getBudget(String category) {
        return budgets.getOrDefault(category, 0.0);
    }

    public Map<String, Double> getAllBudgets() {
        return budgets;
    }

    public double calculateTotalExpensesByCategory(String category) {
        return expenseManager.filterByCategory(category).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public Map<String, Double> calculateBudgetUtilization() {
        Map<String, Double> utilization = new HashMap<>();
        for (String category : ExpenseManager.predefinedCategories) {
            double expenses = calculateTotalExpensesByCategory(category);
            double budget = getBudget(category);
            double usage = (expenses / budget) * 100;
            utilization.put(category, usage);
        }
        return utilization;
    }
}
