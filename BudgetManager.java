import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BudgetManager {
    private Map<String, Map<String, Double>> budgets; // month (as string) -> category -> amount
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
        String currentMonth = YearMonth.now().toString();
        budgets.put(currentMonth, new HashMap<>());
        for (String category : ExpenseManager.predefinedCategories) {
            budgets.get(currentMonth).put(category, 1000.0);
        }
    }

    public void setBudget(String category, double amount, YearMonth month) {
        String monthKey = month.toString();
        if (!budgets.containsKey(monthKey)) {
            budgets.put(monthKey, new HashMap<>());
        }
        budgets.get(monthKey).put(category, amount);
        if (updateCallback != null) {
            updateCallback.run();
        }
    }

    public double getBudget(String category, YearMonth month) {
        String monthKey = month.toString();
        if (budgets.containsKey(monthKey) && budgets.get(monthKey).containsKey(category)) {
            return budgets.get(monthKey).get(category);
        }
        return 0.0;
    }

    public Map<String, Double> getAllBudgets(YearMonth month) {
        String monthKey = month.toString();
        return budgets.getOrDefault(monthKey, new HashMap<>());
    }

    public double calculateTotalExpensesByCategory(String category, YearMonth month) {
        double total = 0.0;
        for (Expense expense : expenseManager.filterByCategory(category)) {
            if (YearMonth.from(expense.getDate()).equals(month)) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    public Map<String, Double> calculateBudgetUtilization(YearMonth month) {
        Map<String, Double> utilization = new HashMap<>();
        for (String category : ExpenseManager.predefinedCategories) {
            double expenses = calculateTotalExpensesByCategory(category, month);
            double budget = getBudget(category, month);
            double usage = budget > 0 ? (expenses / budget) * 100 : 0;
            utilization.put(category, usage);
        }
        return utilization;
    }

    public ArrayList<YearMonth> getAvailableMonths() {
        ArrayList<YearMonth> months = new ArrayList<>();
        for (String monthKey : budgets.keySet()) {
            months.add(YearMonth.parse(monthKey));
        }
        for (Expense expense : expenseManager.getAllExpenses()) {
            YearMonth expenseMonth = YearMonth.from(expense.getDate());
            if (!months.contains(expenseMonth)) {
                months.add(expenseMonth);
            }
        }
        Collections.sort(months);
        return months;
    }
}
