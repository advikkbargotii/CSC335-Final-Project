import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ReportManager {

    private BudgetManager budgetManager; 
    private ExpenseManager expenseManager;
    private Runnable guiUpdateCallback;


    public ReportManager(ExpenseManager expenseManager) {
    	this.expenseManager = expenseManager;
        this.budgetManager = expenseManager.getBudgetManager();
    }


    public String generateMonthlySummaryReport() {
    	
    	Map<String, Double> budgets = this.budgetManager.getAllBudgets();
    	List<Expense> expenses = this.expenseManager.getAllExpenses();
    	List<String> categories = expenseManager.getPredefinedCategories();
    	StringBuilder report = new StringBuilder();
    	
    	categories.forEach(category -> {
    		
    		report.append(category + "\n");
    		
    		List<Expense> filteredCategories =  expenseManager.filterByCategory(category);
    		
    		filteredCategories.forEach(expense -> {
    			report.append(expense.toString() + "\n");
    		});
    		
    		double categoryBudget = this.budgetManager.getBudget(category);
    		double categorySpent = this.budgetManager.calculateTotalExpensesByCategory(category);
    		
    		report.append("\n" + category + " Budget: " + categoryBudget + " | " + category + " Expenses: " + categorySpent + "\n\n");    		
    	});
    	
    	report.append("Total Budget: " + this.getTotalBudget() + " | " + "Total Expenses: " + this.getTotalExpenses());
    	
    	return report.toString();
    	
    }


    public double getTotalExpenses() {
    	
    	Map<String, Double> budgets = this.budgetManager.getAllBudgets();
    	
    	double totalExpenses = 0.0;
    	
    	
    	for (Map.Entry<String, Double> entry : budgets.entrySet()) {
    		
    		totalExpenses += this.budgetManager.calculateTotalExpensesByCategory(entry.getKey());
    		
    	}
    	
        return totalExpenses;
    }
    
    public double getTotalBudget() {
    	
    	Map<String, Double> budgets = this.budgetManager.getAllBudgets();
    	
    	double totalBudget = 0.0;
    	
    	
    	for (Map.Entry<String, Double> entry : budgets.entrySet()) {
    		
    		totalBudget += this.budgetManager.getBudget(entry.getKey());
    		
    	}
    	
        return totalBudget;
    }
    
    public Map<String, Double> getCategoryWiseSpending() {
        Map<String, Double> spendingByCategory = new HashMap<>();
        List<String> categories = expenseManager.getPredefinedCategories();

        for (String category : categories) {
            double totalSpent = budgetManager.calculateTotalExpensesByCategory(category);
            spendingByCategory.put(category, totalSpent);
        }

        return spendingByCategory;
    }

    

}
