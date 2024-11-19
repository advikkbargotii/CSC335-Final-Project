import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseManager {
    private List<Expense> expenses;
    public static final List<String> predefinedCategories = List.of(
        "Food", "Transportation", "Entertainment", "Utilities", "Miscellaneous"
    );
    private BudgetManager budgetManager;
    private Runnable guiUpdateCallback; // Callback to update GUI

    public ExpenseManager() {
        this.expenses = new ArrayList<>();
        this.budgetManager = new BudgetManager(this);
    }

    public void setGuiUpdateCallback(Runnable callback) {
        this.guiUpdateCallback = callback;
    }

    // Add an expense
    public void addExpense(Expense expense) {
    	
        expenses.add(expense);
        updateBudgets();
        if (guiUpdateCallback != null) {
            guiUpdateCallback.run(); //trigger GUI updates
        }
    }


    // Edit an expense
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

    // Delete an expense
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

    // Get all expenses
    public List<Expense> getAllExpenses() {
        return expenses;
    }

    // Filter expenses by category
    public List<Expense> filterByCategory(String category) {
        return expenses.stream()
                .filter(expense -> expense.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    // Filter expenses by date range
    public List<Expense> filterByDateRange(LocalDate start, LocalDate end) {
        return expenses.stream()
                .filter(expense -> (expense.getDate().isEqual(start) || expense.getDate().isAfter(start)) &&
                                   (expense.getDate().isEqual(end) || expense.getDate().isBefore(end)))
                .collect(Collectors.toList());
    }

    // Get predefined categories
    public static List<String> getPredefinedCategories() {
        return predefinedCategories;
    }

    public BudgetManager getBudgetManager() {
        return budgetManager;
    }

    // Update budgets when expenses are added, edited, or removed
    private void updateBudgets() {
        budgetManager.getAllBudgets(); 
    }
}
