import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseManager {
    private List<Expense> expenses;
    private static final List<String> predefinedCategories = List.of(
            "Food", "Transportation", "Entertainment", "Utilities", "Miscellaneous"
    );

    public ExpenseManager() {
        this.expenses = new ArrayList<>();
    }

    // Add an expense
    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    // Edit an expense
    public void editExpense(int index, Expense newExpense) {
        if (index >= 0 && index < expenses.size()) {
            expenses.set(index, newExpense);
        } else {
            throw new IndexOutOfBoundsException("Invalid expense index.");
        }
    }

    // Delete an expense
    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
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
    public List<String> getPredefinedCategories() {
        return predefinedCategories;
    }
}
