import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ReportManager {
    BudgetManager budgetManager; 
    private ExpenseManager expenseManager;
    private Runnable guiUpdateCallback;

    public ReportManager(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
        this.budgetManager = expenseManager.getBudgetManager();
    }

    public String generateMonthlySummaryReport(YearMonth month) {
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
            
            report.append("\n").append(category)
                  .append(" Budget: ").append(categoryBudget)
                  .append(" | ").append(category)
                  .append(" Expenses: ").append(categorySpent).append("\n\n");
        });
        
        report.append("Total Budget: ").append(getTotalBudget(month))
              .append(" | Total Expenses: ").append(getTotalExpenses(month));
        
        return report.toString();
    }

    public double getTotalExpenses(YearMonth month) {
        return expenseManager.getAllExpenses().stream()
            .filter(expense -> YearMonth.from(expense.getDate()).equals(month))
            .mapToDouble(Expense::getAmount)
            .sum();
    }
    
    public double getTotalBudget(YearMonth month) {
        Map<String, Double> budgets = budgetManager.getAllBudgets(month);
        return budgets.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    public Map<String, Double> getCategoryWiseSpending(YearMonth month) {
        Map<String, Double> spendingByCategory = new HashMap<>();
        List<String> categories = expenseManager.getPredefinedCategories();

        for (String category : categories) {
            double totalSpent = budgetManager.calculateTotalExpensesByCategory(category, month);
            spendingByCategory.put(category, totalSpent);
        }

        return spendingByCategory;
    }
}
