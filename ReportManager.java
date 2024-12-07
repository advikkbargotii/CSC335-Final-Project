import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authors: Primary Author(Richard Posthuma)
 * The ReportManager class generates monthly summary reports, calculates total expenses and budgets
 * for a given month, and categorizes spending using the BudgetManager and ExpenseManager classes.
 */

    /**
     * Constructs a ReportManager with a linked ExpenseManager.
     * @param expenseManager An instance of ExpenseManager for managing expenses.
     */

public class ReportManager {
    BudgetManager budgetManager; 
    private ExpenseManager expenseManager;
    private Runnable guiUpdateCallback;

    public ReportManager(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
        this.budgetManager = expenseManager.getBudgetManager();
    }
    

    /**
     * Generates a detailed monthly summary report for a given month.
     * @param month The month for which the report is generated.
     * @return A formatted string containing the summary report.
     */
    
    public String generateMonthlySummaryReport(YearMonth month) {

        // Filters expenses to include only those from the specified month
        List<Expense> monthlyExpenses = expenseManager.getAllExpenses().stream()
            .filter(expense -> YearMonth.from(expense.getDate()).equals(month))
            .collect(Collectors.toList());
            
        List<String> categories = expenseManager.getPredefinedCategories();
        StringBuilder report = new StringBuilder();
        
        report.append("Monthly Report for ").append(month.toString()).append("\n\n");
        
        categories.forEach(category -> {
            report.append(category).append("\n");
            
            List<Expense> categoryExpenses = monthlyExpenses.stream()
                .filter(expense -> expense.getCategory().equals(category))
                .collect(Collectors.toList());
            
            categoryExpenses.forEach(expense -> report.append(expense.toString()).append("\n"));
            
            double categoryBudget = budgetManager.getBudget(category, month);
            double categorySpent = budgetManager.calculateTotalExpensesByCategory(category, month);

        // Adds budget and spending information for the category
            
            report.append("\n").append(category)
                  .append(" Budget: ").append(categoryBudget)
                  .append(" | ").append(category)
                  .append(" Expenses: ").append(categorySpent).append("\n\n");
        });
        
        report.append("Total Budget: ").append(getTotalBudget(month))
              .append(" | Total Expenses: ").append(getTotalExpenses(month));
        
        return report.toString();
    }

    

    /**
     * Calculates the total expenses for a given month.
     * @param month The month for which the total expenses are calculated.
     * @return The total expenses for the specified month.
     */
    
    public double getTotalExpenses(YearMonth month) {

        // Sums the amounts of all expenses in the specified month
        return expenseManager.getAllExpenses().stream()
            .filter(expense -> YearMonth.from(expense.getDate()).equals(month))
            .mapToDouble(Expense::getAmount)
            .sum();
    }

    

    /**
     * Calculates the total budget for a given month.
     * @param month The month for which the total budget is calculated.
     * @return The total budget for the specified month.
     */
    
    public double getTotalBudget(YearMonth month) {

        // Sums all category budgets for the specified month
        Map<String, Double> budgets = budgetManager.getAllBudgets(month);
        return budgets.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    

    /**
     * Retrieves category-wise spending for a specific month.
     * @param month The month for which spending is calculated.
     * @return A map of categories to their total spending.
     */
    
    public Map<String, Double> getCategoryWiseSpending(YearMonth month) {
        Map<String, Double> spendingByCategory = new HashMap<>();
        List<String> categories = expenseManager.getPredefinedCategories();
    // Loops through each category to calculate spending
        for (String category : categories) {
            double totalSpent = budgetManager.calculateTotalExpensesByCategory(category, month);
            spendingByCategory.put(category, totalSpent);
        }

        return spendingByCategory;
    }
}
